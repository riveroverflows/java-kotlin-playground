package com.rofs.cache.local;

import java.time.Duration;
import java.util.Set;

public interface CacheOperations<K, V> {

    V get(K key);

    void set(K key, V value, Duration ttl);

    void setIfAbsent(K key, V value, Duration ttl);

    boolean remove(K key);

    boolean hasKey(K key);

    V pop(K key);

    Set<K> keys();

    long size();

}
