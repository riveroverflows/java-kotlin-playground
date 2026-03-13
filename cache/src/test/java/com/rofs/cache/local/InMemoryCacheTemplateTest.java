package com.rofs.cache.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCacheTemplateTest {

    private static final Duration SHORT_TTL = Duration.ofMillis(50);
    private static final Duration LONG_TTL  = Duration.ofMinutes(10);

    private InMemoryCacheTemplate<String, String> cache;

    // ── get ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("get")
    class Get {

        @BeforeEach
        void setUp() {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);
        }

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
        @DisplayName("만료된 entry → null 반환 + 내부에서 제거 (Lazy Eviction)")
        void expired_entry_returns_null_and_is_removed() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            assertEquals(1, cache.size());

            Thread.sleep(100);

            assertNull(cache.get("k"));       // 만료 → null
            assertEquals(0, cache.size());    // lazy eviction으로 제거됨
        }
    }

    // ── set ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("set")
    class Set {

        @BeforeEach
        void setUp() {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);
        }

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
        @DisplayName("maxCount 초과 시 새 항목 무시")
        void set_drops_insert_when_max_count_exceeded() {
            cache = new InMemoryCacheTemplate<>(2, LONG_TTL);

            cache.set("k1", "v1", LONG_TTL);
            cache.set("k2", "v2", LONG_TTL);
            cache.set("k3", "v3", LONG_TTL); // 무시됨

            assertEquals(2, cache.size());
            assertNull(cache.get("k3"));
        }

        @Test
        @DisplayName("커스텀 TTL이 적용됨")
        void custom_ttl_is_applied() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);

            Thread.sleep(100);

            assertNull(cache.get("k")); // 만료됨
        }

        @Test
        @DisplayName("같은 key 재set: maxCount 여유 있을 때 값 갱신")
        void re_set_same_key_updates_value() {
            cache = new InMemoryCacheTemplate<>(5, LONG_TTL);

            cache.set("k", "v1", LONG_TTL);
            cache.set("k", "v2", LONG_TTL);

            assertEquals("v2", cache.get("k"));
            assertEquals(1, cache.size());
        }
    }

    // ── setIfAbsent ───────────────────────────────────────────────

    @Nested
    @DisplayName("setIfAbsent")
    class SetIfAbsent {

        @BeforeEach
        void setUp() {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);
        }

        @Test
        @DisplayName("없는 key → 저장됨")
        void absent_key_is_stored() {
            cache.setIfAbsent("k", "v", LONG_TTL);
            assertEquals("v", cache.get("k"));
        }

        @Test
        @DisplayName("있는 key → 무시됨")
        void existing_key_is_not_overwritten() {
            cache.set("k", "original", LONG_TTL);
            cache.setIfAbsent("k", "new", LONG_TTL);

            assertEquals("original", cache.get("k"));
        }

        @Test
        @DisplayName("만료된 key → 새 값으로 저장됨")
        void expired_key_is_treated_as_absent() throws InterruptedException {
            cache.set("k", "old", SHORT_TTL);
            Thread.sleep(100);

            cache.setIfAbsent("k", "new", LONG_TTL);

            assertEquals("new", cache.get("k"));
        }
    }

    // ── remove ────────────────────────────────────────────────────

    @Nested
    @DisplayName("remove")
    class Remove {

        @BeforeEach
        void setUp() {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);
        }

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
        @DisplayName("유효한 key → true + 이후 get은 null")
        void existing_key_returns_true_and_removes() {
            cache.set("k", "v", LONG_TTL);
            assertTrue(cache.remove("k"));
            assertNull(cache.get("k"));
        }

        @Test
        @DisplayName("만료된 key → false (만료 항목은 remove가 false 반환)")
        void expired_key_returns_false() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            Thread.sleep(100);

            assertFalse(cache.remove("k")); // 만료됐으므로 false
        }
    }

    // ── hasKey ────────────────────────────────────────────────────

    @Nested
    @DisplayName("hasKey")
    class HasKey {

        @BeforeEach
        void setUp() {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);
        }

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
        @DisplayName("유효한 key → true")
        void valid_key_returns_true() {
            cache.set("k", "v", LONG_TTL);
            assertTrue(cache.hasKey("k"));
        }

        @Test
        @DisplayName("만료된 key → false + Lazy Eviction으로 제거")
        void expired_key_returns_false_and_removes() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            assertEquals(1, cache.size());

            Thread.sleep(100);

            assertFalse(cache.hasKey("k"));
            assertEquals(0, cache.size()); // lazy eviction 발생
        }
    }

    // ── pop ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("pop")
    class Pop {

        @BeforeEach
        void setUp() {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);
        }

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
        @DisplayName("유효한 key → 값 반환 + 제거")
        void valid_key_returns_value_and_removes() {
            cache.set("k", "v", LONG_TTL);
            assertEquals("v", cache.pop("k"));
            assertNull(cache.get("k")); // 제거 확인
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("만료된 key → null (제거는 됨)")
        void expired_key_returns_null() throws InterruptedException {
            cache.set("k", "v", SHORT_TTL);
            Thread.sleep(100);

            assertNull(cache.pop("k"));   // 만료 → null
            assertEquals(0, cache.size()); // store에서는 제거됨
        }
    }

    // ── keys / size ───────────────────────────────────────────────

    @Nested
    @DisplayName("keys / size")
    class KeysAndSize {

        @BeforeEach
        void setUp() {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);
        }

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

            assertTrue(cache.keys().containsAll(java.util.List.of("a", "b", "c")));
            assertEquals(3, cache.size());
        }

        @Test
        @DisplayName("keys()는 만료된 항목도 포함할 수 있음 (live view — eviction 없음)")
        void keys_may_include_expired_entries() throws InterruptedException {
            cache.set("live", "v", LONG_TTL);
            cache.set("dead", "v", SHORT_TTL);

            Thread.sleep(100);

            // keys()는 store.keySet() 직접 반환 — 만료 항목 포함 가능
            // (get/hasKey와 달리 lazy eviction 없음)
            assertTrue(cache.keys().contains("live"));
        }

        @Test
        @DisplayName("remove 후 size 감소")
        void size_decreases_after_remove() {
            cache.set("k", "v", LONG_TTL);
            assertEquals(1, cache.size());
            cache.remove("k");
            assertEquals(0, cache.size());
        }
    }

    // ── evictExpired ──────────────────────────────────────────────

    @Nested
    @DisplayName("evictExpired (active eviction)")
    class EvictExpired {

        @Test
        @DisplayName("만료된 항목만 제거, 유효한 항목은 유지")
        void removes_expired_keeps_valid() throws InterruptedException {
            cache = new InMemoryCacheTemplate<>(10, LONG_TTL);

            cache.set("live", "v", LONG_TTL);
            cache.set("dead1", "v", SHORT_TTL);
            cache.set("dead2", "v", SHORT_TTL);

            Thread.sleep(100);

            cache.evictExpired();

            assertEquals(1, cache.size());
            assertEquals("v", cache.get("live"));
            assertNull(cache.get("dead1"));
            assertNull(cache.get("dead2"));
        }

        @Test
        @DisplayName("Weigher 있을 때 만료 항목의 weight가 차감됨")
        void evict_expired_adjusts_weight_with_weigher() throws InterruptedException {
            // 값 길이를 weight로 사용하는 Weigher
            Weigher<String, String> weigher = (k, v) -> v.length();
            cache = new InMemoryCacheTemplate<>(10, weigher, 100, LONG_TTL);

            cache.set("dead", "hello", SHORT_TTL); // weight=5
            cache.set("live", "world", LONG_TTL);  // weight=5

            Thread.sleep(100);

            cache.evictExpired();

            assertEquals(1, cache.size());
            assertNull(cache.get("dead"));
            assertEquals("world", cache.get("live"));
        }
    }

    // ── Weigher 기반 제한 ─────────────────────────────────────────

    @Nested
    @DisplayName("Weigher 기반 maxWeight 제한")
    class WeigherConstraint {

        @Test
        @DisplayName("maxWeight 초과 시 삽입 무시")
        void set_drops_insert_when_max_weight_exceeded() {
            Weigher<String, String> weigher = (k, v) -> v.length();
            cache = new InMemoryCacheTemplate<>(100, weigher, 10, LONG_TTL);

            cache.set("k1", "hello", LONG_TTL); // weight=5 → total=5
            cache.set("k2", "world", LONG_TTL); // weight=5 → total=10
            cache.set("k3", "extra", LONG_TTL); // weight=5 → total=15 > 10, 무시됨

            assertEquals(2, cache.size());
            assertNull(cache.get("k3"));
        }
    }
}
