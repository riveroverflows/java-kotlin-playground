package com.rofs.cache.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LFUCacheTest {

    private LFUCache<Integer, String> cache;

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("capacity가 0이면 예외")
        void capacity_zero_throws() {
            assertThrows(IllegalArgumentException.class, () -> new LFUCache<>(0));
        }

        @Test
        @DisplayName("capacity가 음수이면 예외")
        void capacity_negative_throws() {
            assertThrows(IllegalArgumentException.class, () -> new LFUCache<>(-1));
        }

        @Test
        @DisplayName("생성 직후 size는 0")
        void initial_size_is_zero() {
            cache = new LFUCache<>(3);
            assertEquals(0, cache.size());
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @BeforeEach
        void setUp() {
            cache = new LFUCache<>(3);
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
        @DisplayName("get은 해당 항목의 freq를 증가시킴")
        void get_increments_frequency() {
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");

            // 1을 여러 번 get → freq 높아짐
            cache.get(1);
            cache.get(1);
            cache.get(1);

            // 새 항목 삽입 시 freq가 가장 낮은 2나 3이 evict됨 (1은 보호됨)
            cache.put(4, "d");

            assertEquals("a", cache.get(1)); // 생존
        }
    }

    @Nested
    @DisplayName("put")
    class Put {

        @BeforeEach
        void setUp() {
            cache = new LFUCache<>(3);
        }

        @Test
        @DisplayName("기존 key에 put하면 value 갱신 + freq 증가")
        void put_existing_key_updates_value_and_increments_freq() {
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");

            // 1의 빈도를 높임
            cache.put(1, "updated"); // freq: 1→2

            // 새 항목 → freq=1인 2 또는 3 중 LRU인 2 제거
            cache.put(4, "d");

            assertEquals("updated", cache.get(1));
            assertNull(cache.get(2));
        }

        @Test
        @DisplayName("capacity=1: 새 항목은 즉시 기존 항목 대체")
        void capacity_one_always_evicts_existing() {
            cache = new LFUCache<>(1);

            cache.put(1, "a");
            cache.put(2, "b"); // 1 제거

            assertNull(cache.get(1));
            assertEquals("b", cache.get(2));
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

        @Test
        @DisplayName("새 항목 삽입 시 minFreq는 1로 리셋")
        void new_item_resets_min_freq_to_one() {
            cache = new LFUCache<>(2);

            cache.put(1, "a"); // freq=1
            cache.get(1);       // freq=2
            cache.put(2, "b"); // freq=1, minFreq=1

            // 3 삽입 → minFreq=1인 2가 evict (1은 freq=2로 보호)
            cache.put(3, "c");

            assertEquals("a", cache.get(1));
            assertNull(cache.get(2));
            assertEquals("c", cache.get(3));
        }
    }

    @Nested
    @DisplayName("eviction — 빈도 기반")
    class FrequencyEviction {

        @BeforeEach
        void setUp() {
            cache = new LFUCache<>(3);
        }

        @Test
        @DisplayName("가장 낮은 빈도의 항목이 evict됨")
        void evicts_least_frequent() {
            cache.put(1, "a");
            cache.put(2, "b");
            cache.put(3, "c");

            cache.get(1); // freq: 1→2
            cache.get(1); // freq: 2→3
            cache.get(2); // freq: 1→2

            // 3의 freq=1이 최소 → 3 evict
            cache.put(4, "d");

            assertNull(cache.get(3));
            assertEquals("a", cache.get(1));
            assertEquals("b", cache.get(2));
            assertEquals("d", cache.get(4));
        }

        @Test
        @DisplayName("빈도 동점 시 LRU 순서로 evict")
        void tie_broken_by_lru_order() {
            cache = new LFUCache<>(3);

            cache.put(1, "a"); // freq=1, 삽입 순서 1번
            cache.put(2, "b"); // freq=1, 삽입 순서 2번
            cache.put(3, "c"); // freq=1, 삽입 순서 3번

            // 모두 freq=1 → 가장 먼저 삽입된 1이 LRU → 1 evict
            cache.put(4, "d");

            assertNull(cache.get(1));
            assertEquals("b", cache.get(2));
            assertEquals("c", cache.get(3));
            assertEquals("d", cache.get(4));
        }

        @Test
        @DisplayName("get 후 빈도 동점 재배열: get된 항목은 동점 그룹 내 MRU로 이동")
        void get_moves_item_to_mru_within_same_freq() {
            cache = new LFUCache<>(3);

            cache.put(1, "a"); // freq=1
            cache.put(2, "b"); // freq=1
            cache.put(3, "c"); // freq=1

            cache.get(1); // 1: freq=2
            cache.get(1); // 1: freq=3
            cache.get(2); // 2: freq=2

            // freq=1은 3만 남음 → 4 삽입 시 3 evict
            cache.put(4, "d");

            assertNull(cache.get(3));
            assertEquals("a", cache.get(1)); // freq=3, 생존
            assertEquals("b", cache.get(2)); // freq=2, 생존
        }

        @Test
        @DisplayName("cold start: 새 항목은 높은 freq 항목들에게 즉시 evict됨")
        void cold_start_new_item_evicted_immediately() {
            cache = new LFUCache<>(2);

            cache.put(1, "a");
            cache.put(2, "b");

            // 1, 2 모두 freq 높임
            cache.get(1);
            cache.get(1);
            cache.get(2);
            cache.get(2);

            // 3 삽입 → freq=1(minFreq=3으로 갱신됨 이후 3 삽입 → minFreq=1)
            // → 3이 삽입되면 minFreq=1이 됨. 그러나 capacity=2이므로 먼저 eviction 발생
            // eviction 시점: 3 삽입 전, 1과 2 모두 freq=3. minFreq=3. 그 중 LRU인 1 evict
            cache.put(3, "c");

            // 4 삽입 → eviction: 2(freq=3)와 3(freq=1) 중 3이 evict
            cache.put(4, "d");

            assertNull(cache.get(3)); // cold start victim
            assertEquals("d", cache.get(4));
        }
    }

    @Nested
    @DisplayName("minFreq 추적")
    class MinFreqTracking {

        @Test
        @DisplayName("increments 후 이전 버킷이 비면 minFreq 갱신")
        void min_freq_updates_when_bucket_becomes_empty() {
            cache = new LFUCache<>(1);

            cache.put(1, "a"); // freq=1, minFreq=1
            cache.get(1);       // freq=2, 이전 버킷(freq=1) 비워짐 → minFreq=2

            // 2 삽입 → 용량 초과, eviction: minFreq=2인 1 evict
            cache.put(2, "b");

            assertNull(cache.get(1));
            assertEquals("b", cache.get(2));
        }

        @Test
        @DisplayName("새 항목 삽입은 항상 minFreq를 1로 리셋")
        void inserting_new_item_always_resets_min_freq_to_one() {
            cache = new LFUCache<>(3);

            cache.put(1, "a");
            cache.get(1); // freq=2
            cache.put(2, "b");
            cache.get(2); // freq=2
            cache.put(3, "c"); // freq=1, minFreq=1

            // 4 삽입 → eviction: minFreq=1인 3 evict
            cache.put(4, "d");

            assertNull(cache.get(3));
        }
    }

    @Nested
    @DisplayName("대규모")
    class LargeScale {

        @Test
        @DisplayName("10만 건 put/get 정합성 — 높은 빈도 항목은 생존")
        void large_scale_high_freq_items_survive() {
            int cap = 100;
            cache = new LFUCache<>(cap);

            // 0~9: 고빈도 항목 (1000회 접근)
            for (int i = 0; i < 10; i++) {
                cache.put(i, "hot" + i);
                for (int j = 0; j < 1000; j++) {
                    cache.get(i);
                }
            }

            // 10~10109: 저빈도 항목 (한 번씩 삽입)
            for (int i = 10; i < 10_100; i++) {
                cache.put(i, "cold" + i);
            }

            // size는 capacity 초과하지 않음
            assertEquals(cap, cache.size());

            // 고빈도 0~9는 모두 생존
            for (int i = 0; i < 10; i++) {
                assertEquals("hot" + i, cache.get(i));
            }
        }

        @Test
        @DisplayName("10만 건 순차 put: size는 capacity를 초과하지 않음")
        void size_never_exceeds_capacity() {
            int cap = 500;
            cache = new LFUCache<>(cap);

            for (int i = 0; i < 100_000; i++) {
                cache.put(i, "v" + i);
                assertTrue(cache.size() <= cap);
            }
        }
    }
}
