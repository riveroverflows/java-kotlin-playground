# 구현 진행 상황

## local 패키지

- [x] `CacheEntry.java` — record, package-private, `long expiresAt`
- [x] `CacheOperations.java` — 인터페이스 완료
- [ ] `InMemoryCacheTemplate.java` — **현재 진행 중** / 클래스 선언 + 필드 작성 단계
  - 필드: `ConcurrentHashMap<K, CacheEntry<V>> store`, `int maxCount`, `long maxWeight`, `Weigher<K, V> weigher`, `long currentWeight`
  - `Weigher.java` 완료 (같은 패키지)
- [ ] `CacheRegistry.java`
- [ ] `RedisCacheTemplate.java`

## algorithm 패키지

- [ ] `LRUCache.java`
- [ ] `LFUCache.java`

## 문서

- [x] `docs/concurrenthashmap.md` — ConcurrentHashMap, OOM 방지, Eviction 전략 정리
- [ ] Technical Writing 초안
