# 구현 진행 상황

## local 패키지

- [x] `CacheEntry.java` — record, package-private, `long expiresAt`
- [x] `CacheOperations.java` — 인터페이스 완료
- [x] `InMemoryCacheTemplate.java` — get/set/setIfAbsent/remove/hasKey/pop/keys/size 기본 동작 완료
  - `Weigher.java` 완료 (같은 패키지)
- [x] `InMemoryCacheTemplate.java` — eviction 완료 (lazy: get/set/hasKey/remove/pop 시점, active: evictExpired())
- [x] `CacheRegistry.java` — 팩토리 메서드 + 스케줄러 완료
- [x] `RedisCacheTemplate.java` — Jedis 기반 구현 완료

## algorithm 패키지

- [x] `LRUCache.java`
- [x] `LFUCache.java`

## 테스트

- [x] `LRUCacheTest.java` — 생성자/get/put/eviction 순서/대규모 (15 tests)
- [x] `LFUCacheTest.java` — 생성자/get/put/빈도 기반 eviction/minFreq 추적/대규모 (18 tests)
- [x] `CacheComparisonSimulation.java` — LRU vs LFU 동작 차이 시나리오 로그 출력 (4 scenarios)
- [x] `InMemoryCacheTemplateTest.java` — get/set/setIfAbsent/remove/hasKey/pop/keys/size/evictExpired/Weigher (32 tests)
- [x] `RedisCacheTemplateTest.java` — get/set/setIfAbsent/remove/hasKey/pop/keys/size + TTL + 네임스페이스 격리 (27 tests)

## 문서

- [x] `docs/concurrenthashmap.md` — ConcurrentHashMap, OOM 방지, Eviction 전략 정리
- [x] `docs/lru-cache.md` — LRU 원리, 역사, 구현, 변형 알고리즘, 출처 포함
- [x] `docs/lfu-cache.md` — LFU 원리, O(1) 구현, 한계, Redis Morris Counter
- [x] `docs/cache-comparison.md` — LRU vs LFU 4가지 시나리오 실행 결과 및 분석
- [ ] Technical Writing 초안
