# Cache 모듈 컨텍스트

> 행동 지침: `.claude/rules/guide.md` 참고
> 진행 상황: `docs/CHECKLIST.md` 참고

## 왜 이 모듈을 만드는가

Loopers e-commerce 스터디 **round5** 과제 진행 중.
과제 문서 경로: `/Users/river/Developer/loopers/e-commerce/docs/loopers/round5/`
- `00-*.md` — 발표 자료
- `01-*.md` — Implementation Quest (Must-Have: DB Index, 비정규화, Redis 캐시)
- `02-*.md` — Technical Writing Quest

이 `cache` 모듈의 목적:
- in-memory local cache + LRU/LFU 알고리즘을 처음부터 직접 구현
- 구현 과정에서 고민한 내용을 **Technical Writing** 글감으로 활용
- Redis 구현체와 비교해서 trade-off 분석

> Redis 캐시 구현은 e-commerce 프로젝트에서 별도로 진행한다 (round5 Must-Have).
> 이 모듈의 Redis 구현체는 학습/비교 목적이다.

---

## 제약 조건

- **언어**: 순수 Java (Spring 없음) — 캐시 동작 원리 자체에 집중
- **빌드**: Gradle, JDK 25 (Amazon Corretto, mise로 관리)
- **IDE**: IntelliJ

---

## 전체 구현 범위

```
cache/src/main/java/com/rofs/cache/
├── local/
│   ├── CacheOperations<K, V>        인터페이스 — 캐시 연산 계약
│   ├── CacheEntry<V>                record, package-private — 값 + 만료시간 홀더
│   ├── InMemoryCacheTemplate<K, V>  ConcurrentHashMap 기반 in-memory 구현체
│   ├── RedisCacheTemplate<K, V>     Jedis 기반 Redis 구현체
│   └── CacheRegistry                팩토리 + 단일 스케줄러로 eviction 관리
└── algorithm/
    ├── LRUCache<K, V>
    └── LFUCache<K, V>
```

---

## 설계 결정사항

### CacheEntry
- `record`로 구현 (불변, 간결)
- `package-private` — 외부에 노출하지 않음
- `long expiresAt` — `System.currentTimeMillis()` 기반

### CacheOperations 인터페이스
- 메서드: `get`, `set`, `setIfAbsent`, `remove`, `hasKey`, `pop`, `keys`, `size`
- `keys()` 반환 타입: `Set<K>`

### InMemoryCacheTemplate
- `ConcurrentHashMap` — 버킷 단위 락으로 동시성 처리
- 초기 용량: `(int) (maxCount / 0.75) + 1` — rehashing 방지
- 크기 제한: `maxCount`(항목 수) + `maxWeight`(Weigher 기반 가중치)
- Eviction: Lazy(get/set 시점) + Active(주기적 스캔) 조합
- template은 `evictExpired()` 제공, 스케줄링은 CacheRegistry 담당

### CacheRegistry
- 팩토리 메서드로만 캐시 생성 가능 → 생성과 동시에 자동 등록
- 단일 `ScheduledExecutorService`로 모든 캐시의 `evictExpired()` 주기적 호출
- 스케줄 태스크는 반드시 try-catch로 감쌀 것 (예외 발생 시 태스크 silently 중단)

### RedisCacheTemplate
- Jedis 사용 (Spring 없이 순수 Java)
- `CacheOperations` 동일 인터페이스 구현 → InMemory와 비교 가능

### LRU / LFU
- 별도 `algorithm` 패키지, TTL 개념 없음
- `CacheEntry` 사용 안 함
