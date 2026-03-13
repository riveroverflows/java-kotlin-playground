# ConcurrentHashMap 동작 원리

## 왜 HashMap을 그냥 쓰면 안 되는가?

캐시는 멀티스레드 환경에서 동시에 여러 요청이 접근한다. 일반 `HashMap`은 동시성 처리가 없기 때문에 동시 접근 시 데이터 손실, 무한 루프 등의 문제가 발생할 수 있다.

---

## 동시성 해결 방법 비교

| 방법                              | 설명                   | 단점                          |
|---------------------------------|----------------------|-----------------------------|
| `synchronized`                  | 메서드 전체에 락            | 한 번에 하나의 스레드만 접근 가능 → 성능 저하 |
| `Collections.synchronizedMap()` | HashMap을 동기화 래퍼로 감싸기 | 마찬가지로 전체 락                  |
| `ConcurrentHashMap`             | 버킷 단위로 락             | 같은 버킷에 접근하는 스레드만 대기         |

캐시는 읽기/쓰기가 빈번하므로 `ConcurrentHashMap`이 적합하다.

---

## 버킷(Bucket)이란?

`HashMap`은 키를 해시 함수에 넣어 나온 값으로 저장 위치를 결정한다. 그 위치 하나하나를 **버킷**이라고 한다.

```
key "apple"  → hash = 100 → 100 % 16 = 4번 버킷
key "mango"  → hash = 116 → 116 % 16 = 4번 버킷  ← 해시값 달라도 같은 버킷 가능
key "banana" → hash = 50  → 50  % 16 = 2번 버킷
```

- 버킷 수는 고정돼 있고, 키의 해시값 % 버킷수 = 버킷 인덱스로 결정된다.
- 같은 버킷에 여러 키가 들어오면(충돌) 연결 리스트나 트리로 이어붙인다.

---

## ConcurrentHashMap의 버킷 단위 락

`ConcurrentHashMap`은 맵 전체가 아닌 **버킷 단위**로 락을 건다.

```
스레드 A → 4번 버킷 접근 중
스레드 B → 2번 버킷 접근 중  ← 서로 다른 버킷이므로 동시 진행 가능

스레드 C → 4번 버킷 접근 시도 ← 스레드 A가 끝날 때까지 대기
```

키가 고르게 분산될수록 같은 버킷에 몰릴 확률이 낮아지므로 실제로는 대기가 거의 없다.

> 가상 스레드(Virtual Thread)를 사용해도 공유 자원의 동시성 문제는 동일하게 존재하므로 `ConcurrentHashMap`이 여전히 필요하다.

---

## Rehashing과 초기 용량

### Load Factor (부하 계수)

버킷의 일정 비율이 차면 버킷 수를 2배로 늘린다. 기본 load factor는 **0.75**다.

```
버킷 16개 × 0.75 = 12개 차면 → 32개로 확장
버킷 32개 × 0.75 = 24개 차면 → 64개로 확장
```

버킷 확장 시 기존 키를 전부 새 버킷에 재배치(rehashing)하는 비용이 크다.

### 초기 용량 계산

```
초기 용량 = 예상 최대 데이터 수 ÷ load factor

예) 최대 1000개 저장 → 1000 / 0.75 ≈ 1334
```

캐시는 최대 크기를 미리 알 수 있으므로 생성자에서 `capacity`를 받아 초기 용량을 계산하는 것이 좋다. 이렇게 하면 rehashing 없이 수용할 수 있고, 나중에 LRU/LFU eviction 정책과도 연결된다.

```java
int initialCapacity = (int) (capacity / 0.75) + 1;
new ConcurrentHashMap<>(initialCapacity);
```

---

## OOM 방지와 캐시 크기 제한

캐시 크기를 제한하지 않으면 데이터가 계속 쌓여 OOM(Out Of Memory)이 발생할 수 있다.
이를 방지하기 위해 두 가지 제한 방식을 고려할 수 있다.

### Count-based vs Size-based

| 방식 | 설명 | 장점 | 단점 |
|------|------|------|------|
| Count-based (`maxCount`) | 최대 항목 수 제한 | 구현 간단 | 항목마다 크기가 달라 메모리 예측 부정확 |
| Size-based (`maxBytes`) | 최대 바이트 크기 제한 | 메모리 정확히 제어 | Java에서 런타임 객체 크기 측정이 어렵고 비용이 큼 |

### Java에서 객체 크기 측정이 어려운 이유

제네릭 타입 `V`로 열려 있으면 런타임에 실제 힙 크기를 측정해야 한다. 주요 방법은 다음과 같다:

| 방법 | 정확도 | 성능 | 비고 |
|------|--------|------|------|
| 직렬화 (`ByteArrayOutputStream`) | 낮음 | 느림 | 힙 크기가 아닌 직렬화 크기 측정 |
| `Instrumentation.getObjectSize()` | 중간 | 보통 | `-javaagent` JVM 플래그 필요 |
| JAMM, Ehcache sizeof | 높음 | 느림 | 외부 라이브러리 의존 |
| Weigher (사용자 제공) | 도메인 지식 기반 | 빠름 | Caffeine, Guava 등 프로덕션 표준 방식 |

Caffeine, Guava 같은 프로덕션 캐시 라이브러리는 바이트 크기를 직접 측정하지 않는다.
대신 사용자가 **`Weigher`(가중치 함수)**를 제공하는 방식을 채택한다.

```java
// Caffeine 예시
Caffeine.newBuilder()
    .maximumWeight(10_000)
    .weigher((key, value) -> value.length())  // 사용자가 크기 정의
    .build();
```

`value.length()`는 정확한 바이트 크기가 아니라 도메인 지식 기반의 **근사치**다.
중요한 건 정확도가 아니라 "상대적으로 얼마나 큰가"를 일관되게 표현하는 것이다.

Weigher는 캐시에 값을 추가하는 시점(`set()` 호출 시)에 값이 무엇인지 알 수 있으면 충분히 적용 가능하다.
캐시 조회 미스 시 DB에서 조회 후 캐시에 추가하는 구조(Cache-Aside 패턴)라면, 그 시점에 값이 확정되므로 Weigher를 쓸 수 있다.

```
사용자 요청 → 캐시 미스 → DB 조회 → set(key, value, ttl) ← 이 시점에 value 알고 있음
```

- `maxCount` — 최대 항목 수. `map.size() >= maxCount`이면 새 항목 추가 전 evict
- `maxWeight` — Weigher 기반 최대 가중치. 누적 weight가 초과하면 evict

### 결론

**`maxCount`와 Weigher 기반 `maxWeight` 둘 다 적용한다.**

- `maxCount`는 기본값을 제공하고 생성자로 주입받아 코드 수정 없이 조정 가능하게 열어둔다
- 기본값은 운영 환경 모니터링을 통해 트래픽 패턴, 메모리 사용량을 보면서 점진적으로 조정한다

```java
public InMemoryCacheTemplate(int maxCount, long maxWeight, Weigher<K, V> weigher) { ... }
public InMemoryCacheTemplate() { this(1000, Long.MAX_VALUE, (k, v) -> 1); }  // 기본값
```

---

## TTL 만료 항목 정리 전략 (2026-03-13 06:19)

### Eviction 방식 비교

| 방식 | 설명 | 단점 |
|------|------|------|
| Lazy eviction | `get()`/`set()` 호출 시 해당 키 만료 체크 | 요청이 없으면 만료 항목이 메모리에 계속 잔류 |
| Active eviction | 주기적으로 전체 스캔해서 만료 항목 정리 | 스케줄러 스레드 필요 |
| Event-driven | 만료 이벤트 발행해서 처리 | Active eviction 위에서 동작하는 구조 — 독립적인 방법이 아님 |

**결론: Lazy + Active 조합** 사용
- Lazy: `get()`/`set()` 시 해당 키 즉각 정리
- Active: 주기적으로 전체 스캔해서 lazy가 놓친 항목 정리

### Active Eviction 책임 분리

`InMemoryCacheTemplate`은 `evictExpired()` 메서드만 제공하고, 스케줄링 인프라(언제, 어떤 스레드로)는 외부에서 담당한다.

- template이 스케줄러를 직접 생성하면 template 10개 = 스레드 10개
- 외부 스케줄러 하나가 모든 캐시의 `evictExpired()`를 주기적으로 호출하면 스레드 1~2개로 충분

### CacheRegistry

단일 스케줄러로 여러 캐시를 관리하는 구조.

```
CacheRegistry
    ├── productCache ──┐
    ├── userCache    ──┼──→ ScheduledExecutorService (스레드 1~2개)
    └── orderCache   ──┘         └─→ 각 캐시의 evictExpired() 주기적 호출
```

**캐시 생성은 팩토리 메서드 방식으로 CacheRegistry를 통해서만 가능하게 한다.**

```java
CacheRegistry registry = new CacheRegistry();
InMemoryCacheTemplate cache = registry.create(...);  // 생성과 동시에 자동 등록
```

생성자에서 전역 singleton에 직접 접근하는 방식은 숨겨진 의존성이 생겨 테스트가 어렵다.
팩토리 메서드는 registry를 직접 생성해서 주입할 수 있으므로 테스트가 쉽다.

**주의**: 스케줄 태스크에서 예외 발생 시 해당 태스크가 silently 중단된다. 반드시 try-catch로 감싸야 한다.

```java
scheduler.scheduleWithFixedDelay(() -> {
    try {
        cache.evictExpired();
    } catch (Exception e) {
        // 예외를 삼켜도 task는 계속 실행됨
    }
}, delay, period, TimeUnit.MILLISECONDS);
```

---

## setIfAbsent의 TOCTOU 문제 (2026-03-13 09:40)

### 문제

`setIfAbsent`를 `containsKey()` + `set()` 두 연산으로 구현하면 TOCTOU(Time-of-Check-Time-of-Use) 경쟁 조건이 발생한다.

```
스레드 A: containsKey("foo") → false
스레드 B: containsKey("foo") → false
스레드 A: set("foo", ...) → 삽입
스레드 B: set("foo", ...) → 중복 삽입 (의도하지 않은 덮어쓰기)
```

### ConcurrentHashMap.putIfAbsent()로 해결 가능?

`putIfAbsent(key, value)`는 "키 없는지 확인 + 넣기"를 원자적으로 처리한다. 단순한 경우엔 해결책이 된다.

하지만 이 구현에서는 `set()` 안에 maxCount 체크, weight 체크, expiresAt 계산이 포함되어 있어서 `putIfAbsent` 하나로 대체할 수 없다. `putIfAbsent` 이전의 체크 로직과 `putIfAbsent` 사이에 여전히 gap이 존재한다.

### 결론

완벽한 원자성을 보장하려면 해당 블록 전체를 `synchronized`로 감싸야 한다. 하지만 그러면 ConcurrentHashMap을 쓰는 이점(버킷 단위 락)이 사라진다.

이 구현에서는 학습 목적 상 non-atomic 구조를 유지하고, 주석으로 한계를 명시했다.
