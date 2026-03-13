package com.rofs.cache.local;

import java.time.Duration;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheTemplate<K, V> implements CacheOperations<K, V> {

    private final ConcurrentHashMap<K, CacheEntry<V>> store;
    private final int maxCount;
    private Weigher<K, V> weigher;
    private long maxWeight;
    private long currentWeight;
    private final Duration defaultTtl;

    public InMemoryCacheTemplate() {
        this(1000, Duration.ofMinutes(3));
    }

    public InMemoryCacheTemplate(int maxCount, Duration defaultTtl) {
        store = new ConcurrentHashMap<>((int) (maxCount / 0.75f) + 1);
        this.maxCount = maxCount;
        this.defaultTtl = defaultTtl;
    }

    public InMemoryCacheTemplate(int maxCount, Weigher<K, V> weigher, long maxWeight) {
        this(maxCount, weigher, maxWeight, Duration.ofMinutes(3));
    }

    public InMemoryCacheTemplate(int maxCount, Weigher<K, V> weigher, long maxWeight, Duration defaultTtl) {
        store = new ConcurrentHashMap<>((int) (maxCount / 0.75f) + 1);
        this.maxCount = maxCount;
        this.weigher = weigher;
        this.maxWeight = maxWeight;
        this.defaultTtl = defaultTtl;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        CacheEntry<V> entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            store.remove(key);
            if (weigher != null) {
                int weight = weigher.weigh(key, entry.value());
                currentWeight -= weight;
            }
            return null;
        }
        return entry.value();
    }

    public void set(K key, V value) {
        set(key, value, defaultTtl);
    }

    @Override
    public void set(K key, V value, Duration ttl) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key or value is null");
        }

        if (store.size() + 1 > maxCount) {
            return;
        }

        if (weigher != null) {
            int weight = weigher.weigh(key, value);
            currentWeight += weight;
            if (currentWeight > maxWeight) {
                currentWeight -= weight;
                return;
            }
        }

        CacheEntry<V> entry = new CacheEntry<>(value, System.currentTimeMillis() + ttl.toMillis());
        store.put(key, entry);
    }


    /**
     * TOCTOU(Time-of-Check-Time-of-Use) 경쟁 조건 존재:
     * containsKey()와 set() 사이에 다른 스레드가 같은 키를 삽입할 수 있음.
     * ConcurrentHashMap.putIfAbsent()는 원자적이지만, 이 구현에서는 maxCount/weight 체크와
     * expiresAt 계산이 포함되어 있어 단순 대체가 불가능하다.
     * 완벽한 원자성이 필요하다면 별도의 락(synchronized 블록 등)이 필요하다.
     */
    @Override
    public void setIfAbsent(K key, V value, Duration ttl) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key or value is null");
        }
        if (hasKey(key)) {
            return;
        }
        set(key, value, ttl);
    }

    @Override
    public boolean remove(K key) {
        if (key == null) {
            return false;
        }

        CacheEntry<V> entry = store.remove(key);
        if (entry == null) {
            return false;
        }

        if (weigher != null) {
            int weight = weigher.weigh(key, entry.value());
            currentWeight -= weight;
        }

        return !entry.isExpired();
    }

    @Override
    public boolean hasKey(K key) {
        if (key == null) {
            return false;
        }
        CacheEntry<V> entry = store.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            store.remove(key);
            if (weigher != null) {
                int weight = weigher.weigh(key, entry.value());
                currentWeight -= weight;
            }
            return false;
        }
        return true;
    }

    @Override
    public V pop(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        CacheEntry<V> entry = store.remove(key);
        if (entry == null) {
            return null;
        }

        if (weigher != null) {
            int weight = weigher.weigh(key, entry.value());
            currentWeight -= weight;
        }

        if (entry.isExpired()) {
            return null;
        }

        return entry.value();
    }

    @Override
    public Set<K> keys() {
        return store.keySet();
    }

    @Override
    public long size() {
        return store.size();
    }

    void evictExpired() {
        for (Entry<K, CacheEntry<V>> entry : store.entrySet()) {
            K key = entry.getKey();
            CacheEntry<V> value = entry.getValue();

            if (value.isExpired()) {
                store.remove(key);
                if (weigher != null) {
                    int weight = weigher.weigh(key, value.value());
                    currentWeight -= weight;
                }
            }
        }
    }
}
