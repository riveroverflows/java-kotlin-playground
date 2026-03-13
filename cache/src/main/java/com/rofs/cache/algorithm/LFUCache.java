package com.rofs.cache.algorithm;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class LFUCache<K, V> {

    private final int capacity;
    private int minFreq;
    private final Map<K, Node<K, V>> keyMap;              // key → node
    private final Map<Integer, LinkedHashSet<K>> freqMap; // freq → 해당 빈도의 key 집합 (삽입 순서 유지)

    public LFUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.minFreq = 0;
        this.keyMap = new HashMap<>();
        this.freqMap = new HashMap<>();
    }

    public V get(K key) {
        Node<K, V> node = keyMap.get(key);
        if (node == null) {
            return null;
        }
        incrementFreq(node);
        return node.value;
    }

    public void put(K key, V value) {
        Node<K, V> node = keyMap.get(key);
        if (node != null) {
            node.value = value;
            incrementFreq(node);
        } else {
            if (keyMap.size() >= capacity) {
                evict();
            }
            Node<K, V> newNode = new Node<>(key, value, 1);
            keyMap.put(key, newNode);
            freqMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
            minFreq = 1;
        }
    }

    public int size() {
        return keyMap.size();
    }

    private void incrementFreq(Node<K, V> node) {
        int oldFreq = node.freq;
        int newFreq = oldFreq + 1;

        LinkedHashSet<K> oldBucket = freqMap.get(oldFreq);
        oldBucket.remove(node.key);
        if (oldBucket.isEmpty()) {
            freqMap.remove(oldFreq);
            if (minFreq == oldFreq) {
                minFreq = newFreq;
            }
        }

        freqMap.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(node.key);
        node.freq = newFreq;
    }

    private void evict() {
        LinkedHashSet<K> minBucket = freqMap.get(minFreq);
        K evictKey = minBucket.iterator().next(); // 가장 먼저 삽입된 key = LRU 순서
        minBucket.remove(evictKey);
        if (minBucket.isEmpty()) {
            freqMap.remove(minFreq);
        }
        keyMap.remove(evictKey);
    }

    private static class Node<K, V> {
        K key;
        V value;
        int freq;

        Node(K key, V value, int freq) {
            this.key = key;
            this.value = value;
            this.freq = freq;
        }
    }
}
