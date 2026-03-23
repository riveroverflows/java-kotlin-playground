# LRU / LFU 캐시 구현 검증 — 테스트 실험 결과 (2026-03-13)

> Java 25, JUnit 5, Gradle 기반. 별도 프레임워크 없이 순수 자료구조로 구현한 LRUCache / LFUCache를 대상으로 한다.
> 단위 테스트(assertion 기반)와 동작 시뮬레이션(로그 출력 기반)으로 구성했다.

---

## 테스트 환경

| 항목 | 내용 |
|------|------|
| 언어 | Java 25 (Amazon Corretto) |
| 빌드 | Gradle 9.3.1 |
| 테스트 프레임워크 | JUnit 5.10 |
| 테스트 총 수 | 37개 (LRUCacheTest 15 + LFUCacheTest 18 + CacheComparisonSimulation 4) |
| 전체 결과 | **ALL PASS** |

```
BUILD SUCCESSFUL in 3s
```

---

## 1. LRUCacheTest — 15개 전체 통과

LRU의 핵심 불변식은 두 가지다:

1. **get/put 시 해당 항목은 MRU(Most Recently Used)로 승격된다.**
2. **capacity 초과 시 LRU(Least Recently Used) 항목이 제거된다.**

모든 테스트는 이 두 명제가 경계 조건에서도 성립함을 검증한다.

---

### 1-1. 생성자 검증 (3개)

| 테스트 | 검증 내용 | 결과 |
|--------|-----------|------|
| `capacity_zero_throws` | `capacity=0` → `IllegalArgumentException` | PASS |
| `capacity_negative_throws` | `capacity=-1` → `IllegalArgumentException` | PASS |
| `initial_size_is_zero` | 생성 직후 `size() == 0` | PASS |

양수가 아닌 capacity는 즉시 예외를 던진다. 캐시는 존재 자체가 의미 없어진다.

---

### 1-2. get 동작 검증 (4개)

#### `get_missing_key_returns_null`

없는 key를 조회하면 null을 반환한다. 당연해 보이지만, 내부적으로 HashMap 조회 실패 후 dummy head/tail 탐색 없이 조기 return하는지 확인한다.

```
cache.get(999) == null  // key 없음, PASS
```

#### `get_after_put_returns_value`

삽입 직후 조회가 값을 반환하는 기본 동작.

```
put(1, "a") → get(1) == "a"  // PASS
```

#### `get_promotes_to_mru` ← **핵심**

이 테스트가 LRU의 핵심 동작을 검증한다. `get`이 항목을 MRU 자리로 올리는지 확인한다.

```
초기 상태: put(1,"a"), put(2,"b"), put(3,"c")
  링크드 리스트: head ← [3] ← [2] ← [1] ← tail
  (head 쪽이 MRU, tail 쪽이 LRU)

get(1) 실행 → 1이 MRU로 이동
  링크드 리스트: head ← [1] ← [3] ← [2] ← tail

put(4,"d") → capacity 초과 → LRU인 2 evict
  링크드 리스트: head ← [4] ← [1] ← [3] ← tail

결과:
  get(2) == null     // 제거됨, PASS
  get(1) == "a"      // 생존, PASS
  get(3) == "c"      // 생존, PASS
  get(4) == "d"      // 생존, PASS
```

get 하나가 eviction 결과를 완전히 바꾼다. `get(1)`이 없었다면 1이 evict됐을 것이다.

#### `repeated_get_keeps_item_alive`

연속 get이 항목을 무한히 보호하는지 확인한다.

```
put(1,"a"), put(2,"b"), put(3,"c")

get(1) × 10회 — 1이 계속 MRU

put(4,"d") → LRU인 2 제거
put(5,"e") → LRU인 3 제거

get(1) == "a"   // 10번 조회로 계속 MRU 유지, 생존, PASS
get(2) == null  // PASS
get(3) == null  // PASS
```

10번이든 1000번이든 get이 있는 한 항목은 살아있다.

---

### 1-3. put 동작 검증 (5개)

#### `put_existing_key_updates_value_and_promotes`

기존 key에 put하면 값이 갱신되고 MRU로 승격된다. 이는 get과 동일한 "최근 접근" 처리다.

```
put(1,"a"), put(2,"b"), put(3,"c")
  순서: [3] ← [2] ← [1] (1이 LRU)

put(1,"updated") → 1의 값 갱신 + MRU 승격
  순서: [1] ← [3] ← [2] (2가 LRU)

put(4,"d") → 2 evict

get(1) == "updated"  // PASS
get(2) == null       // PASS
```

#### `evicts_lru_when_full`

기본 eviction 동작. 삽입 순서만 있을 때 가장 먼저 넣은 것이 제거된다.

```
put(1,"a") → put(2,"b") → put(3,"c") → put(4,"d")
  4 삽입 시 LRU인 1 제거

get(1) == null   // PASS
get(2) == "b"    // PASS
get(3) == "c"    // PASS
get(4) == "d"    // PASS
```

#### `capacity_one_always_evicts_previous` ← 경계 케이스

capacity=1은 극단적 경계다. 새 항목을 넣을 때마다 기존 항목이 사라진다.

```
cache = new LRUCache<>(1)

put(1,"a") → 캐시: [1]
put(2,"b") → 1 evict, 캐시: [2]

get(1) == null  // PASS
get(2) == "b"   // PASS
size() == 1     // PASS
```

#### `capacity_two_edge_case`

capacity=2에서 get이 LRU 순서를 바꾸는 동작.

```
cache = new LRUCache<>(2)

put(1,"a"), put(2,"b")
  순서: [2(MRU)] ← [1(LRU)]

get(1) → 1이 MRU로
  순서: [1(MRU)] ← [2(LRU)]

put(3,"c") → 2 evict

get(1) == "a"   // PASS
get(2) == null  // PASS
get(3) == "c"   // PASS
```

#### `repeated_put_same_key_does_not_increase_size`

같은 key를 반복 put해도 size가 늘지 않아야 한다.

```
put(1,"a") → put(1,"b") → put(1,"c")

size() == 1     // PASS
get(1) == "c"   // 마지막 값, PASS
```

---

### 1-4. eviction 순서 검증 (2개)

#### `eviction_follows_access_order`

get과 put이 섞인 시나리오에서 LRU가 정확히 추적되는지 확인한다.

```
put(1,"a"), put(2,"b"), put(3,"c")
  초기 순서(LRU→MRU): [1] ← [2] ← [3]

get(2) → 2가 MRU
  순서: [1] ← [3] ← [2]

get(1) → 1이 MRU
  순서: [3] ← [2] ← [1]  ← 3이 LRU

put(4,"d") → 3 evict
  순서: [2] ← [1] ← [4]

get(3) == null  // PASS
get(1) == "a"   // PASS
get(2) == "b"   // PASS
get(4) == "d"   // PASS
```

get 순서가 [2, 1]이라 LRU는 3이 됐다. put 순서와 완전히 다른 결과다.

#### `sequential_scan_thrashing` ← LRU의 구조적 약점

LRU의 가장 잘 알려진 약점인 **캐시 오염(cache pollution)** 을 재현한다. capacity보다 딱 1개 많은 항목을 순환하면 매번 캐시 미스가 발생한다.

```
capacity=3, 항목 4개를 5라운드 순환:

라운드 1: put(1), put(2), put(3), put(4) → 1 evict
라운드 2: put(1), put(2), put(3), put(4) → 1 evict
... (매 라운드 동일)

// 마지막 상태: 캐시에 2, 3, 4만 존재
// 다음 put(1) 하면 또 2가 evict되고 순환 반복

get(1) == null   // PASS — 순환 패턴에서 1은 항상 밀려남
get(2) == "v2"   // PASS
get(3) == "v3"   // PASS
get(4) == "v4"   // PASS
```

이 현상은 full-table scan, 배치 처리처럼 데이터를 순차적으로 한 번씩만 접근하는 워크로드에서 LRU가 완전히 무력화되는 이유다.

---

### 1-5. 대규모 검증 (1개)

#### `large_scale_correctness`

10만 건을 capacity=1000 캐시에 순차 삽입 후 정합성 검증.

```
for i in 0..99999: put(i, "v"+i)

size() == 1000             // capacity 초과 없음, PASS
get(99000..99999) == 각 값  // 최근 1000개 전부 생존, PASS
get(0) == null             // 오래된 항목 evict됨, PASS
```

10만 번의 삽입에도 capacity 제한이 한 번도 깨지지 않았다.

---

## 2. LFUCacheTest — 18개 전체 통과

LFU의 핵심 불변식:

1. **접근 빈도(frequency)가 가장 낮은 항목이 evict 대상이다.**
2. **빈도 동점 시 그 안에서 LRU 순서로 evict한다.**
3. **새 항목 삽입 시 minFreq는 반드시 1로 리셋된다.**

---

### 2-1. 생성자 검증 (3개)

LRUCacheTest와 동일한 구조. capacity ≤ 0이면 예외, 초기 size는 0.

---

### 2-2. get 동작 검증 (3개)

#### `get_increments_frequency` ← **LFU의 핵심 동작**

get이 빈도를 올리고, 그 결과 eviction 대상이 달라진다.

```
put(1,"a"), put(2,"b"), put(3,"c")
  초기 freqMap: {1: [1,2,3]}  (모두 freq=1)

get(1) × 3회
  → freq(1)=4, freq(2)=1, freq(3)=1
  freqMap: {1: [2,3], 4: [1]}

put(4,"d") → eviction: minFreq=1에서 LRU인 2 제거

get(1) == "a"  // freq=4, 생존, PASS
```

같은 삽입 순서라도 get 횟수로 eviction 결과가 완전히 달라진다.

---

### 2-3. put 동작 검증 (4개)

#### `put_existing_key_updates_value_and_increments_freq`

기존 key에 put하면 값 갱신 + freq 증가. LRU가 "최근성"으로 승격하는 것처럼, LFU는 "빈도"를 올린다.

```
put(1,"a"), put(2,"b"), put(3,"c")
  freqMap: {1: [1,2,3]}

put(1,"updated") → freq(1): 1→2
  freqMap: {1: [2,3], 2: [1]}

put(4,"d") → minFreq=1에서 LRU인 2 evict

get(1) == "updated"  // PASS
get(2) == null       // PASS
```

#### `new_item_resets_min_freq_to_one` ← **minFreq 리셋 불변식**

새 항목은 항상 freq=1로 시작하므로, 삽입 시 minFreq를 강제로 1로 리셋해야 한다. 이걸 빠뜨리면 eviction이 잘못된 버킷을 건드린다.

```
cache = new LFUCache<>(2)

put(1,"a"), get(1) → freq(1)=2
put(2,"b") → freq(2)=1, minFreq=1 리셋

  freqMap: {1: [2], 2: [1]}

put(3,"c") → eviction: minFreq=1에서 2 evict, 그 후 3 삽입

get(1) == "a"   // freq=2, 생존, PASS
get(2) == null  // PASS
get(3) == "c"   // PASS
```

put(1,"a") 후 get(1)으로 freq=2가 된 상태에서 put(2,"b")를 넣으면 minFreq는 1이 돼야 한다. 2의 freq가 1이기 때문이다. 이 리셋이 없으면 minFreq=2를 가리키게 되고 엉뚱한 항목이 evict된다.

---

### 2-4. eviction — 빈도 기반 검증 (4개)

#### `evicts_least_frequent`

주어진 freq에서 가장 낮은 것이 evict된다.

```
put(1,"a"), put(2,"b"), put(3,"c")

get(1) × 2 → freq(1)=3
get(2) × 1 → freq(2)=2
// freq(3)=1 (put만 했으므로)

freqMap: {1: [3], 2: [2], 3: [1]}
minFreq=1 → 3이 evict 대상

put(4,"d") → 3 evict

get(3) == null  // PASS
get(1) == "a"   // PASS
get(2) == "b"   // PASS
get(4) == "d"   // PASS
```

#### `tie_broken_by_lru_order` ← **동점 처리**

모든 항목의 freq가 같을 때 LRU 순서로 evict한다. 이것이 LFU가 "LRU를 tie-breaker로 쓴다"는 의미다.

```
put(1,"a"), put(2,"b"), put(3,"c")
  freqMap: {1: [1,2,3]}  ← LinkedHashSet이 삽입 순서 보장
  // 동점 시 1이 getFirst() → LRU

put(4,"d") → minFreq=1 버킷의 첫 번째 = 1 evict

get(1) == null  // 가장 먼저 삽입된 1이 evict됨, PASS
get(2) == "b"   // PASS
get(3) == "c"   // PASS
get(4) == "d"   // PASS
```

구현에서 `LinkedHashSet<K>`를 쓰는 이유가 바로 이것이다. 삽입 순서를 유지하면서 O(1) getFirst()로 LRU tie-breaking을 구현한다.

#### `get_moves_item_to_mru_within_same_freq`

같은 freq 버킷 내에서도 get된 항목은 그 버킷의 MRU 자리로 이동한다.

```
put(1,"a"), put(2,"b"), put(3,"c")
  freqMap: {1: [1,2,3]}

get(1) → freq(1): 1→2, freqMap: {1: [2,3], 2: [1]}
get(1) → freq(1): 2→3, freqMap: {1: [2,3], 3: [1]}
get(2) → freq(2): 1→2, freqMap: {1: [3], 2: [2], 3: [1]}
  // freq=1 버킷에 3만 남음

put(4,"d") → minFreq=1에서 3 evict

get(3) == null  // PASS
get(1) == "a"   // freq=3, 생존, PASS
get(2) == "b"   // freq=2, 생존, PASS
```

#### `cold_start_new_item_evicted_immediately` ← **Cold Start 약점**

LFU의 고질적 문제. 새 항목(freq=1)은 기존 고빈도 항목들과 경쟁이 안 된다.

```
cache = new LFUCache<>(2)

put(1,"a"), put(2,"b")
  freqMap: {1: [1,2]}

get(1)×2, get(2)×2
  → freq(1)=3, freq(2)=3
  freqMap: {3: [1,2]}

put(3,"c") → eviction: minFreq=3, LRU인 1 evict
  → 3 삽입(freq=1), minFreq=1
  freqMap: {1: [3], 3: [2]}

put(4,"d") → eviction: minFreq=1에서 3 evict
  → 4 삽입(freq=1)
  freqMap: {1: [4], 3: [2]}

get(3) == null  // 3회 접근했어도 고빈도들 사이에서 즉시 희생, PASS
get(4) == "d"   // PASS
```

새 항목 3은 삽입 직후 또 다른 새 항목 4가 들어오자 바로 evict됐다. 3이 아무리 "앞으로 자주 쓰일" 항목이라도 과거 freq가 없으면 생존을 보장받지 못한다.

---

### 2-5. minFreq 추적 검증 (2개)

#### `min_freq_updates_when_bucket_becomes_empty`

incrementFreq 후 기존 버킷이 비어지면 minFreq를 갱신해야 한다.

```
cache = new LFUCache<>(1)

put(1,"a") → freq(1)=1, minFreq=1
  freqMap: {1: [1]}

get(1) → freq(1): 1→2
  freqMap에서 버킷(1) 제거 → 버킷(2) 생성
  minFreq 갱신: 1→2 (버킷(1)이 비었고 minFreq==1이었으므로)
  freqMap: {2: [1]}

put(2,"b") → eviction: minFreq=2에서 1 evict → 2 삽입

get(1) == null  // PASS
get(2) == "b"   // PASS
```

만약 minFreq를 갱신하지 않으면 빈 버킷(1)을 evict하려다 NPE 또는 잘못된 eviction이 발생한다.

#### `inserting_new_item_always_resets_min_freq_to_one`

새 항목 삽입은 항상 minFreq=1로 리셋한다는 불변식을 집중 검증.

```
put(1,"a"), get(1) → freq(1)=2
put(2,"b"), get(2) → freq(2)=2
put(3,"c") → freq(3)=1, minFreq=1 리셋
  freqMap: {1: [3], 2: [1,2]}

put(4,"d") → eviction: minFreq=1에서 3 evict → 4 삽입

get(3) == null  // PASS
```

---

### 2-6. 대규모 검증 (2개)

#### `large_scale_high_freq_items_survive` ← **LFU 핵심 강점 실증**

고빈도 항목이 대규모 저빈도 삽입 공세를 이겨내는지 검증한다.

```
capacity=100

0~9: 각 1000회 접근 (고빈도)
  put(i), get(i)×1000 → freq=1001

10~10099: 한 번씩 삽입 (저빈도)
  총 10090개 삽입 → 매번 minFreq=1인 저빈도 항목이 evict됨

size() == 100           // PASS
for i in 0~9: get(i) == "hot"+i  // 고빈도 0~9 모두 생존, PASS
```

LRU였다면 10090번의 삽입으로 고빈도 항목들이 밀려났을 것이다. LFU는 freq=1001 항목들을 끝까지 보호한다.

#### `size_never_exceeds_capacity`

10만 번 삽입 내내 size가 capacity(500)를 단 한 번도 초과하지 않음.

```
for i in 0..99999: put(i, "v"+i), assertTrue(size() <= 500)
// 100,000번 모두 통과, PASS
```

---

## 3. CacheComparisonSimulation — 실제 실행 로그

assertion이 아닌 로그 출력 테스트다. 동일한 연산 시퀀스를 LRU, LFU에 동시 적용하고 상태 변화를 추적한다.

### 읽는 법

```
LRU  캐시: [1, 2, 3]          ← LRU → MRU 순서. 왼쪽이 제거 1순위
LFU  캐시: [1(f=1), 2(f=1)]  ← f=freq(접근 횟수). 낮을수록 제거 1순위
```

---

### 시나리오 1: 기본 Eviction — 삽입 순서만 있을 때 (capacity=3)

**목적**: 접근 이력이 없을 때 LRU와 LFU가 같은 항목을 evict하는지 확인

```
  put(1, "a")
    LRU  캐시: [1]                             (LRU→MRU 순)
    LFU  캐시: [1(f=1)]

  put(2, "b")
    LRU  캐시: [1, 2]                          (LRU→MRU 순)
    LFU  캐시: [1(f=1), 2(f=1)]

  put(3, "c")  ← 꽉 참
    LRU  캐시: [1, 2, 3]                       (LRU→MRU 순)
    LFU  캐시: [1(f=1), 2(f=1), 3(f=1)]

  [예상 eviction 대상]
    LRU  → 1  (가장 오래전에 삽입)
    LFU  → 1  (동점 중 LRU)

  put(4, "d")  ← eviction 발생
    LRU  캐시: [2, 3, 4]                       (LRU→MRU 순)
    LFU  캐시: [2(f=1), 3(f=1), 4(f=1)]
```

**결론**: 접근 이력이 없으면 LFU도 LRU처럼 삽입 순서 기준으로 동작한다. freq가 모두 1로 동점이라 LRU tie-breaking이 적용된다.

---

### 시나리오 2: 핫 아이템 보호 — 빈도 vs 최근성 (capacity=3)

**목적**: 자주 접근된 항목이 있을 때 두 캐시가 서로 다른 항목을 evict함

```
  put(1, "a")
    LRU  캐시: [1]                             (LRU→MRU 순)
    LFU  캐시: [1(f=1)]

  get(1) × 10회 — key=1의 빈도를 높임
    → get(1) × 10 완료
    LRU  캐시: [1]                             (LRU→MRU 순)
    LFU  캐시: [1(f=11)]

  put(2, "b")
    LRU  캐시: [1, 2]                          (LRU→MRU 순)
    LFU  캐시: [1(f=11), 2(f=1)]

  put(3, "c")  ← 꽉 참
    LRU  캐시: [1, 2, 3]                       (LRU→MRU 순)
    LFU  캐시: [1(f=11), 2(f=1), 3(f=1)]

  [예상 eviction 대상]
    LRU  → 1  (put 후 get이 없어 LRU 자리로 밀림)
    LFU  → 2  (freq=1 중 가장 오래된 것)

  put(4, "d")  ← eviction 발생
    LRU  캐시: [2, 3, 4]                       (LRU→MRU 순)
    LFU  캐시: [1(f=11), 3(f=1), 4(f=1)]
```

**결론**:

- **LRU**: key=1 제거. `put(2)`, `put(3)`이 더 최근이라 1이 LRU 자리로 밀렸다. LRU는 빈도를 전혀 고려하지 않는다. "10번이나 썼는데 제거됩니까" 하는 상황이다.
- **LFU**: key=2 제거. freq(1)=11 vs freq(2,3)=1. 빈도 낮은 2를 제거한다. 자주 쓴 1은 보호된다.

---

### 시나리오 3: 접근 패턴 변화 — LFU의 빈도 편향 (capacity=3)

**목적**: 과거에 인기 있던 항목이 이제 안 쓰일 때 두 캐시의 반응 차이

```
  put(1,2,3)  ← 초기 상태
    LRU  캐시: [1, 2, 3]                       (LRU→MRU 순)
    LFU  캐시: [1(f=1), 2(f=1), 3(f=1)]

  [Phase 1] key=1을 집중 접근 — 과거의 '핫' 아이템
    get(1) × 10 완료
    LRU  캐시: [2, 3, 1]                       (LRU→MRU 순)
    LFU  캐시: [1(f=11), 2(f=1), 3(f=1)]

  [Phase 2] 접근 패턴 변화 — 이제 key=2, 3이 핫해짐
    get(2)×8, get(3)×8 완료
    LRU  캐시: [1, 2, 3]                       (LRU→MRU 순)
    LFU  캐시: [1(f=11), 2(f=9), 3(f=9)]

  [예상 eviction 대상]
    LRU  → 1  (get(3)이 가장 최근, get(2) 그 다음, 1이 LRU)
    LFU  → 2  (freq: 1=11, 2=9, 3=9. 동점 중 LRU=2)

  put(4, "d")  ← eviction 발생
    LRU  캐시: [2, 3, 4]                       (LRU→MRU 순)
    LFU  캐시: [1(f=11), 3(f=9), 4(f=1)]
```

**결론**:

- **LRU**: key=1 제거. Phase 2 이후 아무도 1을 안 썼으니 LRU 자리로 밀렸다. 현재 패턴에 빠르게 적응한다.
- **LFU**: key=2 제거. key=1의 freq=11이 높아서 과거 인기를 이유로 계속 보호된다. **빈도 편향(Frequency Bias)**. 2와 3이 각 8번씩이나 접근됐는데도 1이 더 많이 쓰였다는 이유로 살아있다.

CDN이나 SNS 트렌드처럼 인기 콘텐츠가 빠르게 교체되는 환경에서 LFU가 불리한 이유가 이것이다. 과거의 영광으로 현재 자리를 차지하고 있다.

---

### 시나리오 4: Cold Start — 새 항목의 생존 가능성 (capacity=2)

**목적**: 기존 고빈도 항목들 사이에서 새 항목이 얼마나 버티는지

```
  put(1,2)  ← 초기
    LRU  캐시: [1, 2]                          (LRU→MRU 순)
    LFU  캐시: [1(f=1), 2(f=1)]

  key=1, 2 모두 고빈도로 만들기 (각 5회)
    get(1)×5, get(2)×5 완료
    LRU  캐시: [1, 2]                          (LRU→MRU 순)
    LFU  캐시: [1(f=6), 2(f=6)]

  [put(3) 예상 eviction 대상]
    LRU  → 1  (get(2) 최근 → 2=MRU, 1=LRU)
    LFU  → 1  (freq 동점, LRU 기준 1이 먼저)

  put(3, "c")  ← 새 항목 삽입, eviction 발생
    LRU  캐시: [2, 3]                          (LRU→MRU 순)
    LFU  캐시: [2(f=6), 3(f=1)]

  새 항목 key=3에 3회 접근 — 빈도를 쌓아봄
    get(3)×3 완료
    LRU  캐시: [2, 3]                          (LRU→MRU 순)
    LFU  캐시: [2(f=6), 3(f=4)]

  [put(4) 예상 eviction 대상]
    LRU  → 2
    LFU  → 3

  put(4, "d")  ← 또 새 항목, eviction 발생
    LRU  캐시: [3, 4]                          (LRU→MRU 순)
    LFU  캐시: [2(f=6), 4(f=1)]
```

**결론**:

- **LRU**: `get(3)`으로 3이 MRU → 2가 LRU → key=2 제거. 새 항목 3이 생존한다. LRU는 "최근에 썼다"는 사실만 봐서 새 항목도 공평하게 경쟁할 수 있다.
- **LFU**: freq(3)=4 < freq(2)=6 → key=3 제거. 3회나 접근했는데도 2의 6회를 못 넘어서 쫓겨난다. 새 항목은 누적 freq가 없어서 처음부터 불리한 싸움이다.

---

## 4. 결과 요약

### 테스트 통계

| 클래스 | 테스트 수 | 통과 | 실패 |
|--------|-----------|------|------|
| LRUCacheTest | 15 | 15 | 0 |
| LFUCacheTest | 18 | 18 | 0 |
| CacheComparisonSimulation | 4 (시나리오) | 4 | 0 |
| **합계** | **37** | **37** | **0** |

### 구현 검증된 핵심 불변식

**LRU**
- get/put 모두 해당 항목을 MRU로 승격시킨다
- capacity 초과 시 정확히 LRU 항목을 제거한다
- 연속 get은 항목을 무한히 보호한다
- 동일 key 반복 put은 size를 증가시키지 않는다

**LFU**
- get/put 모두 해당 항목의 freq를 증가시킨다
- minFreq 버킷의 getFirst()가 evict 대상이다
- 빈도 동점 시 LinkedHashSet의 삽입 순서로 LRU tie-breaking이 적용된다
- 새 항목 삽입 시 minFreq는 반드시 1로 리셋된다
- freq 버킷이 비면 minFreq를 정확히 갱신한다

### 동작 차이 확인

| 시나리오 | LRU 결과 | LFU 결과 |
|----------|----------|----------|
| 순수 삽입 순서 | key=1 제거 | key=1 제거 (동일) |
| 핫 아이템 보호 | key=1 제거 (자주 쓴 항목 희생) | key=2 제거 (저빈도 정확히 제거) |
| 접근 패턴 변화 | key=1 제거 (현재 패턴 반영) | key=2 제거 (과거 빈도에 집착) |
| Cold Start | key=2 제거 (새 항목 생존) | key=3 제거 (새 항목 희생) |
