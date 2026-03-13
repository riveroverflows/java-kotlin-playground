# 구현 진행 상황

## local 패키지

- [x] `CacheEntry.java` — record, package-private, `long expiresAt`
- [x] `CacheOperations.java` — 인터페이스 완료
- [x] `InMemoryCacheTemplate.java` — get/set/setIfAbsent/remove/hasKey/pop/keys/size 기본 동작 완료
  - `Weigher.java` 완료 (같은 패키지)
- [x] `InMemoryCacheTemplate.java` — eviction 완료 (lazy: get/set/hasKey/remove/pop 시점, active: evictExpired())
- [x] `CacheRegistry.java` — 팩토리 메서드 + 스케줄러 완료
- [ ] `RedisCacheTemplate.java`

## algorithm 패키지

- [ ] `LRUCache.java`
- [ ] `LFUCache.java`

## 문서

- [x] `docs/concurrenthashmap.md` — ConcurrentHashMap, OOM 방지, Eviction 전략 정리
- [ ] Technical Writing 초안
