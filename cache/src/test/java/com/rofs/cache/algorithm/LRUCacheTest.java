package com.rofs.cache.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    private LRUCache<Integer, String> cache;

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("capacity가 0이면 예외")
        void capacity_zero_throws() {
            assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(0));
        }

        @Test
        @DisplayName("capacity가 음수이면 예외")
        void capacity_negative_throws() {
            assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(-1));
        }

        @Test
        @DisplayName("생성 직후 size는 0")
        void initial_size_is_zero() {
            cache = new LRUCache<>(3);
            assertEquals(0, cache.size());
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @BeforeEach
        void setUp() {
            cache = new LRUCache<>(3);
        }

        @Test
        @DisplayName("존재하지 않는 key는 null 반환")
        void get_missing_key_returns_null() {
            assertNull(cache.get(999));
        }

        @Test
        @DisplayName("put 후 get은 값 반환")
        void get_after_put_returns_value() {
            cache.put(1, "a");
            assertEquals("a", cache.get(1));
        }

        @Test
        @DisplayName("get은 해당 항목을 MRU로 승격")
        void get_promotes_to_mru() {
            // put 순서: 1, 2, 3
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");

            // 1을 get → 1이 MRU로 이동, LRU는 2
            cache.get(1);

            // 4 삽입 시 LRU(2) 제거
            cache.put(4, "d");

            assertNull(cache.get(2));   // 제거됨
            assertEquals("a", cache.get(1));
            assertEquals("c", cache.get(3));
            assertEquals("d", cache.get(4));
        }

        @Test
        @DisplayName("연속 get은 매번 MRU로 승격")
        void repeated_get_keeps_item_alive() {
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");

            // 1을 계속 get
            for (int i = 0; i < 10; i++) {
                cache.get(1);
            }

            cache.put(4, "d"); // LRU(2) 제거
            cache.put(5, "e"); // LRU(3) 제거

            assertEquals("a", cache.get(1)); // 생존
            assertNull(cache.get(2));
            assertNull(cache.get(3));
        }
    }

    @Nested
    @DisplayName("put")
    class Put {

        @BeforeEach
        void setUp() {
            cache = new LRUCache<>(3);
        }

        @Test
        @DisplayName("기존 key에 put하면 value 갱신 + MRU 승격")
        void put_existing_key_updates_value_and_promotes() {
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");

            // 1을 업데이트 → MRU, LRU는 2
            cache.put(1, "updated");

            cache.put(4, "d"); // LRU(2) 제거

            assertEquals("updated", cache.get(1));
            assertNull(cache.get(2));
        }

        @Test
        @DisplayName("capacity 초과 시 LRU 항목 제거")
        void evicts_lru_when_full() {
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");
            cache.put(4, "d"); // 1이 LRU → 제거

            assertNull(cache.get(1));
            assertEquals("b", cache.get(2));
            assertEquals("c", cache.get(3));
            assertEquals("d", cache.get(4));
        }

        @Test
        @DisplayName("capacity=1: 새 항목이 기존 항목을 항상 대체")
        void capacity_one_always_evicts_previous() {
            cache = new LRUCache<>(1);

            cache.put(1, "a");
            cache.put(2, "b");

            assertNull(cache.get(1));
            assertEquals("b", cache.get(2));
            assertEquals(1, cache.size());
        }

        @Test
        @DisplayName("capacity=2 경계 케이스")
        void capacity_two_edge_case() {
            cache = new LRUCache<>(2);

            cache.put(1, "a");
            cache.put(2, "b");
            cache.get(1);       // 1이 MRU, 2가 LRU
            cache.put(3, "c");  // 2 제거

            assertEquals("a", cache.get(1));
            assertNull(cache.get(2));
            assertEquals("c", cache.get(3));
        }

        @Test
        @DisplayName("동일 key 반복 put은 size를 증가시키지 않음")
        void repeated_put_same_key_does_not_increase_size() {
            cache.put(1, "a");
            cache.put(1, "b");
            cache.put(1, "c");

            assertEquals(1, cache.size());
            assertEquals("c", cache.get(1));
        }
    }

    @Nested
    @DisplayName("eviction 순서")
    class EvictionOrder {

        @BeforeEach
        void setUp() {
            cache = new LRUCache<>(3);
        }

        @Test
        @DisplayName("접근 순서대로 LRU 결정")
        void eviction_follows_access_order() {
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");
            // 순서: 1(LRU) → 2 → 3(MRU)

            cache.get(2); // 순서: 1(LRU) → 3 → 2(MRU)
            cache.get(1); // 순서: 3(LRU) → 2 → 1(MRU)

            cache.put(4, "d"); // 3 제거

            assertNull(cache.get(3));
            assertEquals("a", cache.get(1));
            assertEquals("b", cache.get(2));
            assertEquals("d", cache.get(4));
        }

        @Test
        @DisplayName("순차 접근(thrashing) 시나리오: capacity+1개 순환")
        void sequential_scan_thrashing() {
            // capacity=3, 4개 항목을 순환하면 매번 캐시 미스
            cache = new LRUCache<>(3);

            for (int round = 0; round < 5; round++) {
                for (int i = 1; i <= 4; i++) {
                    cache.put(i, "v" + i);
                }
            }

            // 마지막 삽입 순서: 1, 2, 3, 4 → 1이 제거됨
            assertNull(cache.get(1));
            assertEquals("v2", cache.get(2));
            assertEquals("v3", cache.get(3));
            assertEquals("v4", cache.get(4));
        }
    }

    @Nested
    @DisplayName("대규모")
    class LargeScale {

        @Test
        @DisplayName("10만 건 put/get 정합성")
        void large_scale_correctness() {
            int cap = 1000;
            cache = new LRUCache<>(cap);

            for (int i = 0; i < 100_000; i++) {
                cache.put(i, "v" + i);
            }

            // size는 capacity를 초과하지 않음
            assertEquals(cap, cache.size());

            // 최근 1000개는 모두 존재
            for (int i = 99_000; i < 100_000; i++) {
                assertEquals("v" + i, cache.get(i));
            }

            // 오래된 항목은 evict됨
            assertNull(cache.get(0));
        }
    }
}
