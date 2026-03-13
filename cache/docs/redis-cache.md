# Redis 캐시 설계 결정 (2026-03-13 12:06)

## 왜 Redis를 쓰는가

In-memory 캐시(`InMemoryCacheTemplate`)는 단일 서버 프로세스 내에서만 유효하다. 서버가 여러 대로 늘어나면 각 서버의 캐시가 달라 데이터 정합성 문제가 생긴다. Redis는 별도 프로세스(또는 별도 서버)에서 동작하므로 여러 서버가 동일한 캐시를 공유할 수 있다.

|         | InMemoryCacheTemplate | RedisCacheTemplate |
|---------|-----------------------|--------------------|
| 저장 위치   | 애플리케이션 힙 메모리          | 별도 Redis 프로세스      |
| 서버 확장 시 | 서버마다 캐시가 다름           | 모든 서버가 동일한 캐시 공유   |
| 장애 시    | 서버 재시작 시 캐시 소멸        | Redis가 살아있으면 유지    |
| 네트워크 비용 | 없음                    | 있음 (로컬 캐시 대비 느림)   |

---

## Jedis vs Lettuce

### 선택: 이 모듈에서는 Jedis 사용

이 모듈의 목적은 **캐시 동작 원리 학습**이다. 단순하고 동기적인 API인 Jedis가 원리를 이해하는 데 더 적합하다.

> 실제 프로덕션(e-commerce round5)에서는 Lettuce 사용 — Spring Boot 2.0 이후 기본값.

---

### Jedis의 구조와 한계

Jedis는 **블로킹 I/O 기반 동기 방식**이다. 요청 시 스레드가 응답 올 때까지 블로킹된다.

- 멀티스레드 환경에서는 **커넥션 풀(JedisPool)** 필수
- 풀 없이 쓰면 커넥션을 스레드가 독점 → TPS 한계

#### 성능 수치 (출처: https://jojoldu.tistory.com/418)

> 테스트 환경: AWS r5d.4xlarge EC2 4대, R5.large Redis, 데이터 약 1천만 건

| 항목         | Jedis (풀 미적용) | Jedis (풀 적용) | Lettuce     |
|------------|---------------|--------------|-------------|
| TPS        | 31,000        | 55,000       | **100,000** |
| Redis CPU  | 20%           | 69.5%        | **7%**      |
| Redis 연결 수 | 35개           | 515개         | **6개**      |
| 응답 속도      | 100ms         | 50ms         | **7.5ms**   |

커넥션 풀을 적용해도 Redis 연결이 515개까지 치솟아 하드웨어 자원 낭비가 심하다.

---

### Lettuce의 구조

Lettuce는 **Netty 기반 비동기 이벤트 루프**로 동작한다. 소수의 스레드(커넥션 6개)로 대량 요청을 처리한다.

- Spring Boot 2.0 이후 기본 Redis 클라이언트
- 별도 커넥션 풀 불필요
- 비동기/반응형 API 지원

#### Lettuce Redis Cluster 주의사항

`ClusterTopology` 설정을 반드시 해야 한다. 설정하지 않으면 장애 발생 시 토폴로지 변경이 자동으로 이루어지지 않는다.

---

### 결론

|                 | Jedis         | Lettuce        |
|-----------------|---------------|----------------|
| I/O 방식          | 동기 (블로킹)      | 비동기 (Netty)    |
| 커넥션 관리          | 풀 직접 관리 필요    | 내부 자동 관리       |
| 학습 난이도          | 낮음 (API 단순)   | 높음 (비동기 개념 필요) |
| 프로덕션 적합성        | 낮음            | 높음             |
| Spring Boot 기본값 | ✗ (2.0 이후 제거) | ✓              |

**학습/원리 이해 → Jedis, 프로덕션 → Lettuce.**

---

## SET NX의 원자성 (2026-03-13)

### 왜 원자적인가

Redis는 **단일 스레드로 명령을 실행**한다. Redis 6.0에서 I/O 멀티스레딩이 도입됐지만 명령 실행 자체는 여전히 단일 스레드다. 단일 명령은 그 자체로 원자적이다 — 다른 명령이 끼어들 물리적 틈이 없다.

`SET key value NX PX millis`는 하나의 명령이다. "키가 없으면 set + TTL 설정"이 분리되지 않는다.

### 역사적 실수: SETNX + EXPIRE

```
# 위험한 과거 패턴
SETNX lock_key value    # 락 획득
EXPIRE lock_key 30      # TTL 설정
```

이 두 명령 사이에 프로세스가 죽으면 락이 영원히 남는다 — 데드락. 실제 운영 장애의 원인이었다.

```
# 안전한 현재 방식 (Redis 2.6.12 이후)
SET lock_key value NX EX 30
```

하나의 명령 = 원자성 보장.

### InMemoryCacheTemplate과의 비교

`InMemoryCacheTemplate.setIfAbsent()`는 `containsKey()` + `set()` 두 연산이라 TOCTOU 문제가 있다. Redis `SET NX`는 단일 명령으로 이 문제가 없다.

### 분산 락에서의 활용

```
SET lock:resource_id <unique_token> NX EX 30
```

- `NX`: 키가 없을 때만 set → 상호 배제 보장
- `EX 30`: 30초 자동 해제 → 데드락 방지
- `<unique_token>`: 락 소유자 식별 (UUID)

**락 해제는 Lua 스크립트로:**
```lua
if redis.call("GET", KEYS[1]) == ARGV[1] then
    return redis.call("DEL", KEYS[1])
else
    return 0
end
```

GET + DEL을 분리하면 타이밍 이슈로 남의 락을 삭제하는 버그가 생긴다. Lua 스크립트는 Redis 서버에서 원자적으로 실행된다.

### 한계

| 상황 | 문제 |
|------|------|
| Redis 단일 노드 장애 | 락 정보 소실 → 상호 배제 깨짐 |
| 네트워크 지연으로 TTL 초과 | 만료된 락 보유자가 여전히 작업 중 |

강한 일관성이 필요하면 **Redlock**(N개 Redis 과반수 획득) 또는 Zookeeper 같은 CP 시스템이 더 적합하다. 다만 Martin Kleppmann은 클록 신뢰성 문제로 Redlock도 완전하지 않다고 비판했다.

---

## KEYS vs SCAN (2026-03-13)

### KEYS가 위험한 이유

Redis 공식 문서의 경고:
> "Consider KEYS as a command that should only be used in production environments with extreme care. It may ruin performance when executed against large databases."

시간 복잡도 **O(N)** — 키 개수에 선형 비례. Redis 단일 스레드 특성과 결합되면 `KEYS *` 실행 중 **Redis는 다른 모든 요청을 처리하지 않는다**.

- 1백만 키: ~40ms 블로킹
- 1천만 키: ~400ms 블로킹
- 피크 트래픽 중: 타임아웃 → 재시도 → 부하 증폭 → 장애 연쇄

실제로 Drupal, MedusaJS 등 오픈소스 프로젝트에서 캐시 무효화 로직의 `KEYS` 사용이 운영 장애 원인으로 지목돼 `SCAN`으로 교체됐다.

### SCAN의 커서 기반 방식

```
# 커서 0으로 시작
SCAN 0 MATCH "namespace:*" COUNT 100
→ "17"                        # 다음 커서
→ ["namespace:a", "namespace:b", ...]

# 반환된 커서로 이어서
SCAN 17 MATCH "namespace:*" COUNT 100
→ "0"                         # 커서 0 = 순회 완료
```

**핵심 속성:**
- 매 호출마다 소량의 키만 처리하고 즉시 반환 → 비차단
- `COUNT`는 힌트일 뿐 보장이 아님 (기본값 10)
- 순회 시작~완료 사이 내내 존재한 키는 반드시 반환됨
- **중복 가능성 있음** — 순회 중 다른 클라이언트가 키를 추가/삭제하면 같은 키가 두 번 나올 수 있음

| | KEYS | SCAN |
|--|------|------|
| 복잡도 | O(N), 단번에 | O(N), 분산 처리 |
| 블로킹 | 전체 블로킹 | 비블로킹 |
| 운영 안전성 | 위험 | 안전 |
| 중복 | 없음 | 가능 |
| 원자성 | O | X |

---

## GETDEL — 원자적 pop (2026-03-13)

### 왜 추가됐나

Redis GitHub 이슈 #6460에서 확인된 동기:
> "우리는 한 번만 사용할 수 있는 토큰을 발급한다. 검증할 때 즉시 삭제해야 한다."

**OTP, 이메일 인증 코드, 결제 확인 토큰** 같은 일회용 데이터가 use case다.

### 원자성 없을 때 발생하는 문제

```
# GET + DEL 분리 시 레이스 컨디션
스레드 A: GET token → "abc123"
스레드 B: GET token → "abc123"   ← A가 DEL하기 전!
스레드 A: DEL token
스레드 B: DEL token               ← 이미 없지만 검증은 통과됨
```

일회용 토큰이 두 번 사용됨 → 보안 버그.

### GETDEL

```
GETDEL token_key   → "abc123"   (반환 + 즉시 삭제, 원자적)
```

Redis 6.2.0에서 추가. O(1). 이전에는 같은 기능을 Lua 스크립트로 구현해야 했다:

```lua
local val = redis.call("GET", KEYS[1])
if val then redis.call("DEL", KEYS[1]) end
return val
```

GETDEL은 이 패턴을 네이티브 명령으로 표준화한 것.

---

## Redis TTL 내부 동작 (2026-03-13)

### EX vs PX

| 옵션 | 단위 | 예시 |
|------|------|------|
| `EX n` | 초 | 30초 후 만료 |
| `PX n` | 밀리초 | 30,000ms 후 만료 |
| `TTL key` | 초 단위 잔여 시간 | |
| `PTTL key` | 밀리초 단위 잔여 시간 | |

내부적으로 Redis는 항상 **절대 만료 시각(Unix timestamp, 밀리초)**으로 저장한다. EX로 설정해도 내부는 밀리초 정밀도.

### 만료 처리 메커니즘: 두 가지 전략

**Lazy Deletion** — `InMemoryCacheTemplate`의 lazy eviction과 동일한 개념이다.

키에 접근(GET 등)할 때 만료 여부 확인. 만료됐으면 즉시 삭제하고 null 반환. 아무도 접근하지 않는 키는 메모리에 계속 남는다.

**Active Expiration** — `CacheRegistry`의 `evictExpired()`와 동일한 역할.

초당 10회 백그라운드 작업 실행:
```
1. TTL이 설정된 키 중 20개 무작위 샘플링
2. 만료된 키 모두 삭제
3. 삭제 비율이 25% 초과 → 즉시 다음 사이클
4. 25% 이하가 될 때까지 반복
```

최악의 경우 전체 만료 키의 **25%가 메모리에 잔존**할 수 있다. 이 hidden memory로 인해 Redis가 새 쓰기를 거부하거나 eviction을 조기 발동시킬 수 있다.

Redis 6.0에서 능동 만료 사이클이 재작성됐다. Radix Tree로 만료 시각 기준 키를 정렬 유지 → "곧 만료될 키"부터 우선 처리 → hidden memory 대폭 감소.

### 실무 함의

- 메모리 사이징 시 이론값보다 **최대 25% 버퍼** 필요
- `PTTL`로 밀리초 단위 잔여 시간 확인 가능 — 디버깅에 유용
- 만료 키 접근 시 항상 null 체크 필요

---

## JedisPool 설정 Best Practices (2026-03-13)

### 주요 파라미터

**maxTotal — 최대 커넥션 수**

```
이론값 = 예상 QPS / 커넥션당 QPS
커넥션당 QPS ≈ 1,000 (명령 처리 1ms 기준)

예: 초당 50,000 QPS → maxTotal = 50
```

실제로는 트래픽 급증 대응을 위해 이론값보다 크게 설정한다. 단, 너무 크게 설정하면 Redis 서버 쪽 커넥션 과부하가 된다.

**maxIdle — 유휴 커넥션 최대 유지 수**

트래픽이 가변적이면 `maxIdle = maxTotal` 권장. 피크 이후 커넥션을 유지해 다음 피크에 재활용한다. `maxIdle < maxTotal`이면 피크 후 초과 커넥션이 닫히고, 다음 피크에 새로 생성해야 해서 레이턴시가 올라간다.

**minIdle — 최소 유지 커넥션 수**

기본값 0이면 유휴 커넥션이 모두 제거되고 첫 요청 레이턴시가 높아진다. 트래픽이 주기적으로 변한다면 `minIdle = maxIdle / 2` 이상 권장.

```
권장 관계:
트래픽 변동 심함: minIdle <= maxIdle = maxTotal
트래픽 안정적:   minIdle < maxIdle < maxTotal
```

| 파라미터 | 권장값 | 설명 |
|----------|--------|------|
| `testOnBorrow` | false (고부하) | true 시 매 요청마다 ping 오버헤드 |
| `testWhileIdle` | true | 유휴 커넥션 주기적 검증, zombie 방지 |
| `blockWhenExhausted` | true (기본값 유지) | 커넥션 소진 시 대기 (즉시 실패 시 에러 폭발) |
| `timeBetweenEvictionRunsMillis` | 30,000ms | eviction 스캔 주기 |
| `minEvictableIdleTimeMillis` | 1,800,000ms | 이 시간 이상 유휴인 커넥션 제거 |

### 실무 주의사항

1. **Pool 크기 과대 설정 금지**: 수천 개 커넥션은 Redis 자체 성능 저하
2. **JMX 모니터링**: `numActive`, `numIdle` 지표 보면서 조정
3. **커넥션 예열**: 앱 기동 시 minIdle만큼 미리 생성 → 초기 레이턴시 스파이크 방지

---

## 출처

| 주제 | 출처 | URL |
|------|------|-----|
| Jedis vs Lettuce 성능 비교 | 향로(jojoldu) 블로그 | https://jojoldu.tistory.com/418 |
| SET NX 원자성, 분산 락 | Redis 공식 문서 | https://redis.io/docs/latest/develop/clients/patterns/distributed-locks/ |
| KEYS 위험성 | Redis 공식 문서 | https://redis.io/docs/latest/commands/keys/ |
| SCAN 커서 방식 | Redis 공식 문서 | https://redis.io/docs/latest/commands/scan/ |
| GETDEL 명령 | Redis 공식 문서 | https://redis.io/docs/latest/commands/getdel/ |
| GETDEL 도입 배경 | Redis GitHub Issue #6460 | https://github.com/redis/redis/issues/6460 |
| TTL 내부 만료 알고리즘 | Redis 공식 FAQ | https://redis.io/faq/ |
| JedisPool Best Practices | Alibaba Cloud 공식 문서 | https://www.alibabacloud.com/help/en/redis/use-cases/jedispool-optimization |
| JedisPool Best Practices | Huawei Cloud 공식 문서 | https://support.huaweicloud.com/intl/en-us/bestpractice-dcs/dcs_05_0009.html |