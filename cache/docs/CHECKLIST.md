# 구현 진행 상황

## local 패키지

- [x] `CacheEntry.java` — record, package-private, `long expiresAt`
- [x] `CacheOperations.java` — 인터페이스 완료
- [x] `InMemoryCacheTemplate.java` — get/set/setIfAbsent/remove/hasKey/pop/keys/size 기본 동작 완료
  - `Weigher.java` 완료 (같은 패키지)
- [ ] `InMemoryCacheTemplate.java` — **현재 진행 중** / eviction 구현 (lazy + active)
  - lazy: get/set 시점에 이미 구현됨, active: `evictExpired()` 메서드 추가 필요
- [ ] `CacheRegistry.java`
- [ ] `RedisCacheTemplate.java`

## algorithm 패키지

- [ ] `LRUCache.java`
- [ ] `LFUCache.java`

## 문서

- [x] `docs/concurrenthashmap.md` — ConcurrentHashMap, OOM 방지, Eviction 전략 정리
- [ ] Technical Writing 초안
