package com.rofs.cache.redis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 실제 Redis(localhost:6379)에 연결하는 통합 테스트.
 * 각 테스트 전 "test:cache:" 네임스페이스의 키를 전부 삭제해 격리한다.
 */
class RedisCacheTemplateTest {

    private static final String NAMESPACE = "test:cache";
    private static final Duration SHORT_TTL = Duration.ofMillis(200);
    private static final Duration LONG_TTL  = Duration.ofMinutes(10);

    private static JedisPool pool;
    private RedisCacheTemplate<String, String> cache;

    @BeforeAll
    static void setUpPool() {
        pool = new JedisPool("localhost", 6379);
    }

    @AfterAll
    static void tearDownPool() {
        if (pool != null) pool.close();
    }

    @BeforeEach
    void setUp() {
        cache = RedisCacheTemplate.ofString(pool, NAMESPACE,
                Function.identity(), Function.identity());
        // 이전 테스트 잔여 키 정리
        cache.keys().forEach(cache::remove);
    }

    // ── get ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("null key → IllegalArgumentException")
        void null_key_throws() {
            assertThrows(IllegalArgumentException.class, () -> cache.get(null));
        }

        @Test
        @DisplayName("없는 key → null")
        void miss_returns_null() {
            assertNull(cache.get("missing"));
        }

        @Test
        @DisplayName("set 후 get → 값 반환")
        void hit_returns_value() {
            cache.set("k", "v", LONG_TTL);
            assertEquals("v", cache.get("k"));
        }

        @Test
        @DisplayName("TTL 만료 후 get → null (Redis가 자동 처리)")
        void expired_key_returns_null() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            assertEquals("v", cache.get("k")); // 만료 전 조회

            Thread.sleep(400);

            assertNull(cache.get("k")); // 만료 후 Redis가 자동 삭제
        }
    }

    // ── set ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("set")
    class Set {

        @Test
        @DisplayName("null key → IllegalArgumentException")
        void null_key_throws() {
            assertThrows(IllegalArgumentException.class, () -> cache.set(null, "v", LONG_TTL));
        }

        @Test
        @DisplayName("null value → IllegalArgumentException")
        void null_value_throws() {
            assertThrows(IllegalArgumentException.class, () -> cache.set("k", null, LONG_TTL));
        }

        @Test
        @DisplayName("기본 set/get 동작")
        void basic_set_and_get() {
            cache.set("k", "v", LONG_TTL);
            assertEquals("v", cache.get("k"));
        }

        @Test
        @DisplayName("같은 key 재set → 값 갱신")
        void re_set_updates_value() {
            cache.set("k", "v1", LONG_TTL);
            cache.set("k", "v2", LONG_TTL);
            assertEquals("v2", cache.get("k"));
        }

        @Test
        @DisplayName("TTL이 설정됨 — 만료 후 조회 불가")
        void ttl_is_applied() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            Thread.sleep(400);
            assertNull(cache.get("k"));
        }
    }

    // ── setIfAbsent ───────────────────────────────────────────────

    @Nested
    @DisplayName("setIfAbsent")
    class SetIfAbsent {

        @Test
        @DisplayName("없는 key → 저장됨")
        void absent_key_is_stored() {
            cache.setIfAbsent("k", "v", LONG_TTL);
            assertEquals("v", cache.get("k"));
        }

        @Test
        @DisplayName("있는 key → 무시됨 (NX 원자적)")
        void existing_key_is_not_overwritten() {
            cache.set("k", "original", LONG_TTL);
            cache.setIfAbsent("k", "new", LONG_TTL);
            assertEquals("original", cache.get("k"));
        }

        @Test
        @DisplayName("만료된 key → 새 값으로 저장됨")
        void expired_key_is_treated_as_absent() throws InterruptedException {
            cache.set("k", "old", SHORT_TTL);
            Thread.sleep(400);

            cache.setIfAbsent("k", "new", LONG_TTL);
            assertEquals("new", cache.get("k"));
        }
    }

    // ── remove ────────────────────────────────────────────────────

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        @DisplayName("null key → false")
        void null_key_returns_false() {
            assertFalse(cache.remove(null));
        }

        @Test
        @DisplayName("없는 key → false")
        void missing_key_returns_false() {
            assertFalse(cache.remove("missing"));
        }

        @Test
        @DisplayName("있는 key → true + 이후 get은 null")
        void existing_key_returns_true_and_removes() {
            cache.set("k", "v", LONG_TTL);
            assertTrue(cache.remove("k"));
            assertNull(cache.get("k"));
        }
    }

    // ── hasKey ────────────────────────────────────────────────────

    @Nested
    @DisplayName("hasKey")
    class HasKey {

        @Test
        @DisplayName("null → false")
        void null_returns_false() {
            assertFalse(cache.hasKey(null));
        }

        @Test
        @DisplayName("없는 key → false")
        void missing_returns_false() {
            assertFalse(cache.hasKey("missing"));
        }

        @Test
        @DisplayName("있는 key → true")
        void valid_key_returns_true() {
            cache.set("k", "v", LONG_TTL);
            assertTrue(cache.hasKey("k"));
        }

        @Test
        @DisplayName("만료된 key → false (Redis 자동 처리)")
        void expired_key_returns_false() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            Thread.sleep(400);
            assertFalse(cache.hasKey("k"));
        }
    }

    // ── pop ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("pop")
    class Pop {

        @Test
        @DisplayName("null key → IllegalArgumentException")
        void null_key_throws() {
            assertThrows(IllegalArgumentException.class, () -> cache.pop(null));
        }

        @Test
        @DisplayName("없는 key → null")
        void missing_returns_null() {
            assertNull(cache.pop("missing"));
        }

        @Test
        @DisplayName("있는 key → 값 반환 + 원자적 삭제 (GETDEL)")
        void valid_key_returns_value_and_removes() {
            cache.set("k", "v", LONG_TTL);
            assertEquals("v", cache.pop("k"));
            assertNull(cache.get("k")); // 삭제 확인
        }

        @Test
        @DisplayName("만료된 key → null")
        void expired_key_returns_null() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            Thread.sleep(400);
            assertNull(cache.pop("k"));
        }
    }

    // ── keys / size ───────────────────────────────────────────────

    @Nested
    @DisplayName("keys / size")
    class KeysAndSize {

        @Test
        @DisplayName("빈 캐시 → empty set, size=0")
        void empty_cache() {
            assertTrue(cache.keys().isEmpty());
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("삽입한 key가 keys()에 포함됨")
        void inserted_keys_appear_in_keys() {
            cache.set("a", "1", LONG_TTL);
            cache.set("b", "2", LONG_TTL);
            cache.set("c", "3", LONG_TTL);

            var keys = cache.keys();
            assertTrue(keys.containsAll(java.util.List.of("a", "b", "c")));
            assertEquals(3, cache.size());
        }

        @Test
        @DisplayName("keys()는 이 네임스페이스 키만 반환 (네임스페이스 격리)")
        void keys_returns_only_this_namespace() {
            // 다른 네임스페이스에 키 삽입
            RedisCacheTemplate<String, String> other = RedisCacheTemplate.ofString(
                    pool, "test:other-ns", Function.identity(), Function.identity());
            other.set("x", "1", LONG_TTL);

            cache.set("k", "v", LONG_TTL);

            var keys = cache.keys();
            assertTrue(keys.contains("k"));
            assertFalse(keys.contains("x")); // 다른 네임스페이스 키 포함 안 됨

            // 정리
            other.remove("x");
        }

        @Test
        @DisplayName("만료된 key는 keys()에서 제외됨 (Redis 자동 처리)")
        void expired_keys_are_excluded() throws InterruptedException {
            cache.set("live", "v", LONG_TTL);
            cache.set("dead", "v", SHORT_TTL);

            Thread.sleep(400);

            var keys = cache.keys();
            assertTrue(keys.contains("live"));
            assertFalse(keys.contains("dead")); // Redis가 자동 삭제 → SCAN에서 안 보임
        }
    }
}
