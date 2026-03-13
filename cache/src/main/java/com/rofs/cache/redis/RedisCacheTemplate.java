package com.rofs.cache.redis;

import com.rofs.cache.local.CacheOperations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class RedisCacheTemplate<K, V> implements CacheOperations<K, V> {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(3);

    private final JedisPool pool;
    private final String namespace;
    private final Function<K, String> keySerializer;
    private final Function<String, K> keyDeserializer;
    private final Function<V, String> valueSerializer;
    private final Function<String, V> valueDeserializer;
    private final Duration defaultTtl;

    public RedisCacheTemplate(
            JedisPool pool,
            String namespace,
            Function<K, String> keySerializer,
            Function<String, K> keyDeserializer,
            Function<V, String> valueSerializer,
            Function<String, V> valueDeserializer,
            Duration defaultTtl) {
        this.pool = pool;
        this.namespace = namespace;
        this.keySerializer = keySerializer;
        this.keyDeserializer = keyDeserializer;
        this.valueSerializer = valueSerializer;
        this.valueDeserializer = valueDeserializer;
        this.defaultTtl = defaultTtl;
    }

    /**
     * String 타입 전용 편의 생성자.
     * 직렬화/역직렬화가 identity function이므로 별도 설정 불필요.
     */
    @SuppressWarnings("unchecked")
    public static <V> RedisCacheTemplate<String, V> ofString(
            JedisPool pool,
            String namespace,
            Function<V, String> valueSerializer,
            Function<String, V> valueDeserializer) {
        return new RedisCacheTemplate<>(
                pool, namespace,
                Function.identity(), Function.identity(),
                valueSerializer, valueDeserializer,
                DEFAULT_TTL);
    }

    private String redisKey(K key) {
        return namespace + ":" + keySerializer.apply(key);
    }

    @Override
    public V get(K key) {
        if (key == null) throw new IllegalArgumentException("key is null");
        try (Jedis jedis = pool.getResource()) {
            // Redis TTL은 Redis가 자동 처리 — isExpired() 체크 불필요
            String value = jedis.get(redisKey(key));
            return value != null ? valueDeserializer.apply(value) : null;
        }
    }

    public void set(K key, V value) {
        set(key, value, defaultTtl);
    }

    @Override
    public void set(K key, V value, Duration ttl) {
        if (key == null || value == null) throw new IllegalArgumentException("key or value is null");
        try (Jedis jedis = pool.getResource()) {
            // px: TTL을 밀리초 단위로 설정
            jedis.set(redisKey(key), valueSerializer.apply(value),
                    SetParams.setParams().px(ttl.toMillis()));
        }
    }

    @Override
    public void setIfAbsent(K key, V value, Duration ttl) {
        if (key == null || value == null) throw new IllegalArgumentException("key or value is null");
        try (Jedis jedis = pool.getResource()) {
            // nx: key가 없을 때만 SET — InMemoryCacheTemplate과 달리 완전히 원자적
            jedis.set(redisKey(key), valueSerializer.apply(value),
                    SetParams.setParams().nx().px(ttl.toMillis()));
        }
    }

    @Override
    public boolean remove(K key) {
        if (key == null) return false;
        try (Jedis jedis = pool.getResource()) {
            return jedis.del(redisKey(key)) > 0;
        }
    }

    @Override
    public boolean hasKey(K key) {
        if (key == null) return false;
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(redisKey(key));
        }
    }

    @Override
    public V pop(K key) {
        if (key == null) throw new IllegalArgumentException("key is null");
        try (Jedis jedis = pool.getResource()) {
            // GETDEL: get + delete 원자적 실행 (Redis 6.2+)
            String value = jedis.getDel(redisKey(key));
            return value != null ? valueDeserializer.apply(value) : null;
        }
    }

    @Override
    public Set<K> keys() {
        Set<K> result = new HashSet<>();
        String pattern = namespace + ":*";
        int prefixLength = namespace.length() + 1; // "namespace:" 길이

        try (Jedis jedis = pool.getResource()) {
            // KEYS는 O(N) 블로킹 — SCAN으로 커서 기반 순회
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams params = new ScanParams().match(pattern);
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, params);
                cursor = scanResult.getCursor();
                for (String redisKey : scanResult.getResult()) {
                    String rawKey = redisKey.substring(prefixLength);
                    result.add(keyDeserializer.apply(rawKey));
                }
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
        }
        return result;
    }

    @Override
    public long size() {
        return keys().size();
    }
}
