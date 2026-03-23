# 캐시 교체 알고리즘을 밑바닥부터 구현하며 발견한 것들

> **TL;DR**: LRU와 LFU를 순수 Java로 직접 구현하고, 동일한 연산 시퀀스로 시뮬레이션한 결과, 접근 패턴에 따라 두 알고리즘이 정반대의 eviction 결정을 내렸다. "자주 쓴 항목"을 LRU는 죽이고 LFU는 살렸다. "더 이상 안 쓰는 항목"을 LRU는 바로 치우고 LFU는 과거 인기를 이유로 끝까지 붙잡았다. 만능 교체 알고리즘은 없다.

> 이 글에서 다루는 구현과 테스트는 [Claude Code](https://claude.ai/claude-code)와 페어 프로그래밍으로 진행했다.

---

## 캐시가 필요해졌다

e-commerce 프로젝트에서 상품 목록 조회 API의 응답 속도가 문제가 됐다. 매 요청마다 DB를 조회하는 건 비효율적이다. 자연스럽게 캐시 도입을 검토하게 됐고, Redis의 `allkeys-lru`가 가장 흔한 선택이라는 걸 알게 됐다.

그런데 Redis 설정을 보면 `allkeys-lru` 말고 `allkeys-lfu`도 있다. LRU와 LFU가 뭐가 다르길래 둘 다 제공하는 걸까?

Redis 공식 문서에는 이런 문장이 있다.[^1]

> "In general, an LFU policy is better at representing the access pattern of your requests when LRU is not a good fit."

LRU가 적합하지 않은 경우가 있다? 그 "적합하지 않은 경우"가 정확히 언제인지 궁금했다. 문서를 더 읽어봐도 추상적인 설명뿐이라 와닿지 않았다.

직접 구현해보면 알 수 있지 않을까.

Spring 없이 순수 Java로 LRU, LFU를 처음부터 만들고, 동일한 연산을 돌려 비교하기로 했다. 캐시의 동작 원리 자체를 체감하는 게 목적이라 프레임워크 도움 없이 자료구조부터 쌓아올렸다.

---

## LRU: "최근에 쓴 건 또 쓴다"

LRU(Least Recently Used)는 가장 오래전에 접근된 항목을 제거한다. "최근에 쓴 데이터는 가까운 미래에 다시 쓰일 가능성이 높다"는 temporal locality[^2]에 기반한다.

### 자료구조 선택

`get`과 `put` 모두 O(1)이어야 한다. 이 요건을 충족하려면 두 가지 연산이 빨라야 한다.

1. key로 항목을 찾기: **HashMap**
2. 접근 순서를 유지하고, 특정 항목을 빠르게 앞으로 이동: **Doubly Linked List**

HashMap만으로는 "가장 오래된 항목"을 O(1)에 찾을 수 없고, Linked List만으로는 key 기반 조회가 O(n)이다. 둘을 조합해야 한다.

```
HashMap<K, Node>
  ┌──────────┐
  │ key=1 ───┼──→ Node(1,"a")
  │ key=2 ───┼──→ Node(2,"b")
  │ key=3 ───┼──→ Node(3,"c")
  └──────────┘

Doubly Linked List (MRU ← → LRU)
  head ↔ [3] ↔ [2] ↔ [1] ↔ tail
  (MRU)                    (LRU)
```

HashMap으로 O(1) 조회, Linked List에서 해당 노드를 꺼내 head 쪽으로 이동하면 전체 O(1).

### Sentinel 노드

구현할 때 head와 tail을 더미(sentinel) 노드로 만들었다. 실제 데이터를 담지 않는 경계 노드다.

```java
this.head =new Node<>(null,null);  // dummy head (MRU 쪽)
    this.tail =new Node<>(null,null);  // dummy tail (LRU 쪽)
head.next =tail;
tail.prev =head;
```

이렇게 하면 삽입/삭제 시 "리스트가 비어있는가", "첫 번째 노드인가", "마지막 노드인가" 같은 경계 조건 분기가 사라진다. null 체크 없이 항상 `node.prev`와 `node.next`가 존재한다.

sentinel 없이 구현하면 `addToHead`, `removeNode`, `removeTail` 모두에 null 분기가 필요하다. 코드가 길어지는 건 둘째치고, 경계 조건 하나 빠뜨리면 NPE가 터진다.

### Node에 key를 저장하는 이유

처음에는 Node에 value만 넣으면 될 줄 알았다. 그런데 eviction 시점에 문제가 생긴다.

```java
private Node<K, V> removeTail() {
    Node<K, V> node = tail.prev;
    removeNode(node);
    return node;
}
```

tail.prev를 제거한 뒤 HashMap에서도 지워야 하는데, key를 모르면 `map.remove(key)`를 호출할 수 없다. Node에 key를 저장해둬야 `map.remove(evicted.key)`가 가능하다.

---

## LFU: "자주 쓴 건 또 쓴다"

LFU(Least Frequently Used)는 접근 빈도가 가장 낮은 항목을 제거한다. LRU가 "언제 마지막으로 썼나"를 보는 반면, LFU는 "지금까지 총 몇 번 썼나"를 본다.

### O(1) LFU라는 도전

LFU의 가장 단순한 구현은 min-heap이다. 빈도가 가장 낮은 항목을 O(1)에 찾을 수 있다. 하지만 `get` 시 빈도를 갱신하면 heap 재정렬이 필요해서 O(log n)이 된다.

2010년 Shah, Matani, Mitra가 제안한 구조[^3]는 이 문제를 O(1)로 해결한다.

```
keyMap:  HashMap<K, Node>                   key로 노드를 O(1) 조회
freqMap: HashMap<Integer, LinkedHashSet<K>> 빈도별 key 집합
minFreq: int                                현재 최소 빈도 추적
```

핵심은 `minFreq`다. eviction할 때 `freqMap.get(minFreq)`의 첫 번째 항목을 꺼내면 된다. "가장 낮은 빈도의 가장 오래된 항목"이 O(1)에 나온다.

### minFreq 불변식

구현 중 가장 까다로웠던 부분은 `minFreq` 관리였다. 두 가지 규칙이 있다.

**규칙 1**: `get`/`put`(기존 key)으로 빈도가 증가할 때, 이전 빈도 버킷이 비어지고 그 빈도가 minFreq였으면 minFreq를 +1한다.

```java
private void incrementFreq(Node<K, V> node) {
    int oldFreq = node.freq;
    int newFreq = oldFreq + 1;

    LinkedHashSet<K> oldBucket = freqMap.get(oldFreq);
    oldBucket.remove(node.key);
    if (oldBucket.isEmpty()) {
        freqMap.remove(oldFreq);
        if (minFreq == oldFreq) {
            minFreq = newFreq;  // ← 이 한 줄이 없으면 NPE
        }
    }

    freqMap.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(node.key);
    node.freq = newFreq;
}
```

**규칙 2**: 새 항목 삽입 시 minFreq는 무조건 1이다. 새 항목의 빈도는 1이고, 이보다 낮은 빈도는 존재할 수 없다.

```java
Node<K, V> newNode = new Node<>(key, value, 1);
keyMap.put(key, newNode);
freqMap.computeIfAbsent(1,k ->new LinkedHashSet<>()).add(key);
minFreq =1;  // ← 무조건 리셋
```

이 리셋을 빼먹으면 minFreq가 엉뚱한 값을 가리켜서 빈 버킷에 접근하게 된다. 테스트에서 이걸 잡았다:

```java

@Test
@DisplayName("increments 후 이전 버킷이 비면 minFreq 갱신")
void min_freq_updates_when_bucket_becomes_empty() {
    cache = new LFUCache<>(1);

    cache.put(1, "a");  // freq=1, minFreq=1
    cache.get(1);        // freq=2, 버킷(1) 비워짐 → minFreq=2

    cache.put(2, "b");   // eviction: minFreq=2에서 1 evict → 2 삽입

    assertNull(cache.get(1));
    assertEquals("b", cache.get(2));
}
```

### 빈도 동점 처리

빈도가 같은 항목이 여럿이면 어떤 걸 제거할까? LRU 순서를 tie-breaker로 쓴다. 같은 빈도 내에서 가장 먼저 들어온 항목을 제거한다.

이것이 `LinkedHashSet`을 쓰는 이유다. `HashSet`은 삽입 순서를 보장하지 않지만, `LinkedHashSet`은 보장한다. `getFirst()`로 O(1)에 가장 오래된 항목을 꺼낸다.

```java
private void evict() {
    LinkedHashSet<K> minBucket = freqMap.get(minFreq);
    K evictKey = minBucket.getFirst();  // Java 21+ SequencedCollection
    minBucket.remove(evictKey);
    if (minBucket.isEmpty()) {
        freqMap.remove(minFreq);
    }
    keyMap.remove(evictKey);
}
```

---

## 시뮬레이션: 같은 연산, 다른 결과

구현이 끝나고 가장 궁금했던 걸 해봤다. 동일한 연산 시퀀스를 LRU와 LFU에 동시에 적용하면 결과가 얼마나 다를까?

4가지 시나리오를 설계하고 각 연산 후 양쪽 캐시 상태를 출력했다. ([시뮬레이션 코드](https://github.com/riveroverflows/java-kotlin-playground/blob/main/cache/src/test/java/com/rofs/cache/algorithm/CacheComparisonSimulation.java))

### 시나리오 1: 삽입만 할 때, 차이 없음

```
  put(1,"a"), put(2,"b"), put(3,"c")  ← 꽉 참 (capacity=3)
    LRU  캐시: [1, 2, 3]                       (LRU→MRU 순)
    LFU  캐시: [1(f=1), 2(f=1), 3(f=1)]

  put(4,"d")  ← eviction 발생
    LRU  캐시: [2, 3, 4]                       (LRU→MRU 순)
    LFU  캐시: [2(f=1), 3(f=1), 4(f=1)]
```

접근 이력이 없으면 LFU도 LRU처럼 동작한다. 빈도가 모두 1로 동점이라 삽입 순서(= LRU)가 tie-breaker로 적용된다. 여기까진 "그냥 LRU 쓰면 되겠네"라고 생각했다.

### 시나리오 2: 핫 아이템. LRU는 자주 쓴 항목을 죽인다

key=1을 10번 조회한 뒤 새 항목들을 삽입했다.

```
  get(1) × 10회 → LFU: freq(1)=11

  put(2,"b"), put(3,"c")  ← 꽉 참
    LRU  캐시: [1, 2, 3]                       (LRU→MRU 순)
    LFU  캐시: [1(f=11), 2(f=1), 3(f=1)]

  put(4,"d")  ← eviction 발생
    LRU  캐시: [2, 3, 4]  ← key=1 제거!
    LFU  캐시: [1(f=11), 3(f=1), 4(f=1)]  ← key=2 제거
```

LRU는 key=1을 제거했다. 10번이나 조회했는데? `put(2)`, `put(3)`이 더 최근이라 1이 LRU 자리로 밀린 것이다. LRU는 빈도를 전혀 고려하지 않는다. "최근에 접근했느냐"가 유일한 기준이다.

LFU는 key=2를 제거했다. freq(1)=11 vs freq(2)=1. 빈도가 높은 1을 보호하고 빈도가 낮은 2를 버렸다. 직관적으로 맞는 판단이다.

여기까지 보면 LFU가 더 똑똑해 보인다. 하지만 다음 시나리오에서 뒤집어진다.

### 시나리오 3: 접근 패턴 변화. LFU는 과거에 집착한다

key=1을 10번 집중 접근한 뒤, 이제 key=2와 3을 각 8번씩 접근했다. 패턴이 바뀐 상황이다.

```
  [Phase 1] get(1) × 10
    LFU  캐시: [1(f=11), 2(f=1), 3(f=1)]

  [Phase 2] get(2)×8, get(3)×8
    LRU  캐시: [1, 2, 3]                       (LRU→MRU 순)
    LFU  캐시: [1(f=11), 2(f=9), 3(f=9)]

  put(4,"d")  ← eviction 발생
    LRU  캐시: [2, 3, 4]  ← key=1 제거 (현재 안 쓰이니까)
    LFU  캐시: [1(f=11), 3(f=9), 4(f=1)]  ← key=2 제거 (freq가 낮으니까)
```

LRU는 Phase 2에서 아무도 안 쓴 key=1을 정확히 제거했다. 현재 패턴에 빠르게 적응했다.

LFU는 key=2를 제거했다. key=1의 과거 빈도 11이 2의 현재 빈도 9보다 높다는 이유로 1을 계속 보호한다. **Phase 2에서 1은 단 한 번도 접근되지 않았는데.**

이게 LFU의 **빈도 편향(Frequency Bias)** 이다. 과거에 인기 있었지만 이제 필요 없는 항목이 높은 빈도를 방패삼아 캐시를 점거한다. CDN에서 한때 바이럴이었지만 지금은 아무도 안 보는 콘텐츠, SNS에서 트렌드가 지난 게시물이 대표적인 예시다. 이런 환경에서 LFU가 불리한 이유다.

### 시나리오 4: Cold Start. LFU는 새 항목을 죽인다

기존 항목 둘을 각 5번 접근해 고빈도로 만든 뒤, 새 항목을 삽입했다.

```
  put(1,"a"), put(2,"b")
  get(1)×5, get(2)×5 → freq(1)=6, freq(2)=6

  put(3,"c")  ← 삽입, eviction 발생
  get(3)×3    ← 새 항목에 빈도를 쌓아봄
    LFU  캐시: [2(f=6), 3(f=4)]

  put(4,"d")  ← 또 새 항목, eviction 발생
    LRU  캐시: [3, 4]  ← key=3 생존
    LFU  캐시: [2(f=6), 4(f=1)]  ← key=3 제거됨!
```

LFU에서 key=3은 3번이나 접근했는데도 freq=4 < freq(2)=6이라 제거됐다. 새 항목은 누적 빈도가 없어서 기존 고빈도 항목과 처음부터 경쟁이 안 된다.

LRU에서는 `get(3)` 3번으로 3이 MRU에 올라가서 생존했다.

---

## 테스트로 발견한 엣지 케이스

시뮬레이션은 동작 차이를 보여줬고, 단위 테스트는 구현의 정합성을 검증했다. 총 96개 테스트를 작성했다. 테스트 케이스 설계와 작성은 Claude Code의 도움을 받았다.

| 테스트 클래스                                                                                                                                                                     | 개수 | 대상                                                       |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----|----------------------------------------------------------|
| [LRUCacheTest](https://github.com/riveroverflows/java-kotlin-playground/blob/main/cache/src/test/java/com/rofs/cache/algorithm/LRUCacheTest.java)                           | 15 | 생성자, get, put, eviction 순서, 대규모(10만 건)                   |
| [LFUCacheTest](https://github.com/riveroverflows/java-kotlin-playground/blob/main/cache/src/test/java/com/rofs/cache/algorithm/LFUCacheTest.java)                           | 18 | 생성자, get, put, 빈도 기반 eviction, minFreq 추적, 대규모           |
| [InMemoryCacheTemplateTest](https://github.com/riveroverflows/java-kotlin-playground/blob/main/cache/src/test/java/com/rofs/cache/local/InMemoryCacheTemplateTest.java)     | 32 | get, set, setIfAbsent, remove, hasKey, pop, TTL, Weigher |
| [RedisCacheTemplateTest](https://github.com/riveroverflows/java-kotlin-playground/blob/main/cache/src/test/java/com/rofs/cache/redis/RedisCacheTemplateTest.java)           | 27 | 동일 인터페이스의 Redis 구현, 네임스페이스 격리, TTL                       |
| [CacheComparisonSimulation](https://github.com/riveroverflows/java-kotlin-playground/blob/main/cache/src/test/java/com/rofs/cache/algorithm/CacheComparisonSimulation.java) | 4  | LRU vs LFU 동작 비교 시나리오                                    |

### LRU의 thrashing

가장 인상적이었던 테스트는 **sequential scan thrashing**이다. capacity=3인 캐시에 4개 항목을 순환 삽입하면, 매 라운드마다 동일한 항목이 evict된다.

```java

@Test
@DisplayName("순차 접근(thrashing) 시나리오: capacity+1개 순환")
void sequential_scan_thrashing() {
    cache = new LRUCache<>(3);

    for (int round = 0; round < 5; round++) {
        for (int i = 1; i <= 4; i++) {
            cache.put(i, "v" + i);
        }
    }

    assertNull(cache.get(1));     // 매번 밀려남
    assertEquals("v2", cache.get(2));
    assertEquals("v3", cache.get(3));
    assertEquals("v4", cache.get(4));
}
```

capacity보다 딱 1개 많은 데이터를 순차 접근하면 LRU는 완전히 무력화된다. 히트율 0%. full-table scan이나 배치 처리에서 LRU가 캐시 오염에 취약한 이유가 이것이다.

### LFU의 고빈도 보호력

반대로, LFU에서는 고빈도 항목이 대량의 저빈도 삽입에도 끝까지 살아남는다.

```java

@Test
@DisplayName("10만 건 put/get 정합성: 높은 빈도 항목은 생존")
void large_scale_high_freq_items_survive() {
    int cap = 100;
    cache = new LFUCache<>(cap);

    // 0~9: 각 1000회 접근 (고빈도)
    for (int i = 0; i < 10; i++) {
        cache.put(i, "hot" + i);
        for (int j = 0; j < 1000; j++) {
            cache.get(i);
        }
    }

    // 10~10099: 한 번씩 삽입 (저빈도)
    for (int i = 10; i < 10_100; i++) {
        cache.put(i, "cold" + i);
    }

    assertEquals(cap, cache.size());

    // 고빈도 0~9는 10090개 저빈도 삽입에도 전부 생존
    for (int i = 0; i < 10; i++) {
        assertEquals("hot" + i, cache.get(i));
    }
}
```

10,090개의 저빈도 항목이 들어왔지만 freq=1001인 10개는 단 하나도 밀려나지 않았다. LRU였다면 10,090번째 삽입 시점에 고빈도 항목은 진작에 사라졌을 것이다.

---

## 같은 인터페이스, 다른 세계: InMemory vs Redis

LRU/LFU 외에 실제 캐시 인프라도 구현했다. `CacheOperations` 인터페이스 하나로 InMemory 구현체와 Redis 구현체를 만들었다.

```java
public interface CacheOperations<K, V> {

    V get(K key);

    void set(K key, V value, Duration ttl);

    void setIfAbsent(K key, V value, Duration ttl);

    boolean remove(K key);

    boolean hasKey(K key);

    V pop(K key);

    Set<K> keys();

    long size();
}
```

인터페이스가 같으니 사용하는 쪽에서는 구현체를 바꿔 끼울 수 있다. 하지만 테스트를 작성하면서 둘의 차이가 선명해졌다.

### setIfAbsent: TOCTOU vs 원자성

InMemory 구현:

```java
public void setIfAbsent(K key, V value, Duration ttl) {
    if (hasKey(key)) {  // Check
        return;
    }
    set(key, value, ttl);  // Act  ← 이 사이에 다른 스레드가 끼어들 수 있다
}
```

`hasKey()`와 `set()` 사이에 다른 스레드가 같은 key를 삽입하면 덮어쓴다. 전형적인 TOCTOU(Time-of-Check-Time-of-Use) 문제다.

Redis 구현:

```java
public void setIfAbsent(K key, V value, Duration ttl) {
    try (Jedis jedis = pool.getResource()) {
        jedis.set(redisKey(key), valueSerializer.apply(value),
                  SetParams.setParams().nx().px(ttl.toMillis()));
    }
}
```

`SET key value NX PX millis`는 Redis의 단일 명령이다. Redis는 단일 스레드로 명령을 실행하므로 이 명령 자체가 원자적이다. TOCTOU가 존재하지 않는다.

### TTL 만료 처리

InMemory에서는 만료를 직접 관리해야 한다. Lazy eviction(접근 시 만료 확인)과 Active eviction(주기적 스캔)을 조합했다.

```java
// Lazy: get 시점에 만료 확인
public V get(K key) {
    CacheEntry<V> entry = store.get(key);
    if (entry != null && entry.isExpired()) {
        store.remove(key);
        return null;
    }
    return entry.value();
}

// Active: 외부 스케줄러가 주기적 호출
void evictExpired() {
    for (Entry<K, CacheEntry<V>> entry : store.entrySet()) {
        if (entry.getValue().isExpired()) {
            store.remove(entry.getKey());
        }
    }
}
```

테스트에서 확인된 미묘한 차이: `keys()`는 `store.keySet()`을 직접 반환하기 때문에 만료된 항목도 포함될 수 있다. Lazy eviction은 `get`, `hasKey`, `pop` 시점에만 동작하지, `keys()`에서는 동작하지 않는다.

Redis는 이걸 자동으로 한다. Redis 내부에서 Lazy deletion(접근 시 확인)과 Active expiration(초당 10회 백그라운드 샘플링)을 알아서 처리하므로 애플리케이션이 만료를 신경 쓸 필요가 없다.

### pop: 직접 조립 vs 네이티브 명령

InMemory의 `pop`은 `store.remove()` + `isExpired()` 확인을 조합한 구현이다.

Redis의 `pop`은 `GETDEL`[^4] 명령 하나로 끝난다. get + delete가 원자적으로 실행된다. 이 명령은 Redis 6.2에서 추가됐는데, 그 전에는 Lua 스크립트로 같은 기능을 만들어야 했다.

### 테스트에서 드러난 차이 정리

| 항목              | InMemoryCacheTemplate     | RedisCacheTemplate |
|-----------------|---------------------------|--------------------|
| setIfAbsent 원자성 | X (TOCTOU)                | O (SET NX)         |
| TTL 만료 처리       | 직접 구현 (Lazy + Active)     | Redis 자동 관리        |
| keys() 만료 항목    | 포함될 수 있음                  | 포함되지 않음            |
| pop 원자성         | X (remove + isExpired 분리) | O (GETDEL)         |
| 네트워크 비용         | 없음                        | 있음                 |
| 서버 확장 시         | 서버마다 캐시가 다름               | 모든 서버가 동일 캐시 공유    |

---

## 어떤 캐시를 쓸 것인가

구현과 테스트를 통해 정리한 의사결정 기준이다.

### 교체 알고리즘 선택

```
접근 패턴이 시간에 따라 변하는가?
├─ Yes → LRU (최근성 기반으로 빠르게 적응)
└─ No  → 특정 키가 반복적으로 접근되는가?
         ├─ Yes → LFU (빈도 기반 보호)
         └─ No  → LRU (기본값으로 충분)
```

| 시나리오                 | LRU               | LFU                 |
|----------------------|-------------------|---------------------|
| 접근 패턴이 안정적, 핫 아이템 명확 | 핫 아이템을 보호 못 함     | **핫 아이템을 빈도로 보호**   |
| 접근 패턴이 자주 바뀜         | **현재 패턴에 즉시 적응**  | 과거 빈도에 집착           |
| 새 항목이 자주 유입          | **새 항목도 공평하게 경쟁** | 새 항목이 빈도 부족으로 즉시 퇴출 |
| 순차 스캔 워크로드           | 캐시 오염 (thrashing) | 상대적으로 강함            |

Redis의 `allkeys-lfu`가 `allkeys-lru`보다 히트율이 높은 경우는 대부분 **특정 키가 압도적으로 자주 접근되는 워크로드** 다. 그렇지 않으면 LRU가 더 범용적이다.

### 저장소 선택

```
서버가 1대인가?
├─ Yes → InMemory (네트워크 비용 없음, 단순)
└─ No  → 서버 간 캐시 일관성이 필요한가?
         ├─ Yes → Redis (공유 캐시)
         └─ No  → InMemory (각 서버 독립 캐시로 충분)
```

현실에서는 InMemory + Redis를 계층적으로 쓰는 경우가 많다. 로컬 캐시(InMemory)로 네트워크 비용을 줄이고, 캐시 미스 시 Redis를 확인하고, 그래도 없으면 DB를 조회하는 구조다.

---

## 마무리하며

캐시 원리를 제대로 이해해보려고 프레임워크 없이 직접 구현해봤다. 문서로만 읽었을 때는 "LRU는 최근 기준, LFU는 빈도 기준"이라는 한 줄 설명이 전부였는데, 직접 시뮬레이션을 돌려보니 그 한 줄 차이가 만들어내는 결과의 격차가 예상보다 컸다.

시나리오 2에서 LRU가 10번 조회된 항목을 가차 없이 버리는 걸 보고, 시나리오 3에서 LFU가 이미 쓸모없어진 항목을 과거 빈도만 보고 끝까지 지키는 걸 보면서, 두 알고리즘이 같은 데이터를 두고 정반대의 판단을 내린다는 게 명확해졌다.

만능 교체 알고리즘은 없다. 워크로드의 접근 패턴을 보고 골라야 한다. 그리고 그 패턴은 시간이 지나면 바뀔 수 있다. Caffeine(W-TinyLFU)이나 Redis의 근사 LFU 같은 현대적 구현들이 두 전략을 혼합하는 이유이기도 하다.

구현 코드와 시뮬레이션은 모두 순수 Java로 작성했고, 전체 소스는 [GitHub 리포지토리](https://github.com/riveroverflows/java-kotlin-playground/tree/main/cache)에서 확인할 수 있다.

---

## 출처

[^1]: Redis 공식 문서, "Using Redis as an LRU cache" https://redis.io/docs/latest/develop/reference/eviction/
[^2]: Denning, P. J. (1968). "The Working Set Model for Program Behavior." *Communications of the ACM*, 11(5), pp. 323-333 https://doi.org/10.1145/363095.363141
[^3]: Shah, K., Matani, A., & Mitra, D. (2010). "An O(1) algorithm for implementing the LFU cache eviction scheme." https://arxiv.org/abs/2110.11602
[^4]: Redis 공식 문서, "GETDEL" https://redis.io/docs/latest/commands/getdel/

| 주제                    | 출처                                                                                                                                |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| LRU 최초 체계적 분석         | Belady, L. A. (1966). "A study of replacement algorithms for a virtual-storage computer." *IBM Systems Journal*, 5(2), pp. 78-101 |
| Temporal Locality     | Denning, P. J. (1968). "The Working Set Model for Program Behavior." *Communications of the ACM*, 11(5), pp. 323-333              |
| LFU 공식 평가             | Mattson, R. L. et al. (1970). "Evaluation Techniques for Storage Hierarchies." *IBM Systems Journal*, 9(2), pp. 78-117            |
| O(1) LFU 구현           | Shah, K., Matani, A., & Mitra, D. (2010). "An O(1) algorithm for implementing the LFU cache eviction scheme." *arXiv preprint*    |
| Redis Eviction Policy | Redis 공식 문서, [Using Redis as an LRU cache](https://redis.io/docs/latest/develop/reference/eviction/)                              |
