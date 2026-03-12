package com.rofs.cache.local;

record CacheEntry<V>(
    V value,
    long expiresAt
) {

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
