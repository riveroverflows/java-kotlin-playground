package com.rofs.cache.local;

@FunctionalInterface
public interface Weigher<K, V> {

    int weigh(K key, V value);
}
