package com.rofs.cache.local;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheRegistry {

    private static final Duration DEFAULT_EVICT_PERIOD = Duration.ofSeconds(30);

    private List<InMemoryCacheTemplate<?, ?>> caches;
    private final ScheduledExecutorService scheduler;

    public CacheRegistry(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
        caches = new ArrayList<>();
    }

    public InMemoryCacheTemplate<?, ?> create() {
        return create(new InMemoryCacheTemplate<>(), DEFAULT_EVICT_PERIOD);
    }

    public InMemoryCacheTemplate<?, ?> create(Duration evictPeriod) {
        return create(new InMemoryCacheTemplate<>(), evictPeriod);
    }

    public InMemoryCacheTemplate<?, ?> create(int maxCount) {
        return create(new InMemoryCacheTemplate<>(maxCount), DEFAULT_EVICT_PERIOD);
    }

    public InMemoryCacheTemplate<?, ?> create(int maxCount, Duration evictPeriod) {
        return create(new InMemoryCacheTemplate<>(maxCount), evictPeriod);
    }

    public InMemoryCacheTemplate<?, ?> create(int maxCount, Duration defaultTtl, Duration evictPeriod) {
        return create(new InMemoryCacheTemplate<>(maxCount, defaultTtl), evictPeriod);
    }

    public InMemoryCacheTemplate<?, ?> create(int maxCount, Weigher<?, ?> weigher, long maxWeight) {
        return create(new InMemoryCacheTemplate<>(maxCount, weigher, maxWeight), DEFAULT_EVICT_PERIOD);
    }

    public InMemoryCacheTemplate<?, ?> create(int maxCount, Weigher<?, ?> weigher, long maxWeight, Duration evictPeriod) {
        return create(new InMemoryCacheTemplate<>(maxCount, weigher, maxWeight), evictPeriod);
    }

    public InMemoryCacheTemplate<?, ?> create(int maxCount, Weigher<?, ?> weigher, long maxWeight, Duration defaultTtl, Duration evictPeriod) {
        return create(new InMemoryCacheTemplate<>(maxCount, weigher, maxWeight, defaultTtl), evictPeriod);
    }

    private <K, V> InMemoryCacheTemplate<K, V> create(InMemoryCacheTemplate<K, V> cache, Duration evictPeriod) {
        caches.add(cache);
        long periodMillis = evictPeriod.toMillis();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                cache.evictExpired();
            } catch (Exception e) {
                // 예외가 발생해도 스케줄 태스크가 중단되지 않도록 무시
            }
        }, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
        return cache;
    }
}
