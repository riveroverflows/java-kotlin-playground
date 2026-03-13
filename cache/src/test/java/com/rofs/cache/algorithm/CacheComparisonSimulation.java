package com.rofs.cache.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * LRU vs LFU 동작 차이를 시나리오별로 출력하는 시뮬레이션.
 * 테스트 성공/실패가 아닌 로그 출력이 목적이다.
 */
class CacheComparisonSimulation {

    private static final String DIVIDER = "─".repeat(60);
    private static final String THICK   = "━".repeat(60);

    // ── 헬퍼 ──────────────────────────────────────────────────

    private <K, V> void printState(String op,
                                   LRUCache<K, V> lru, String lruExtra,
                                   LFUCache<K, V> lfu) {
        List<K> lruKeys = lru.keysLruToMru();
        Map<K, Integer> lfuFreqs = lfu.keyFreqs();

        System.out.printf("  %-20s%n", op);
        System.out.printf("    LRU  캐시: %-30s  (LRU→MRU 순)%s%n",
                formatLruState(lruKeys), lruExtra.isEmpty() ? "" : "  " + lruExtra);
        System.out.printf("    LFU  캐시: %s%n", formatLfuState(lfuFreqs));
        System.out.println();
    }

    private <K> String formatLruState(List<K> keysLruToMru) {
        if (keysLruToMru.isEmpty()) return "[]";
        return "[" + String.join(", ", keysLruToMru.stream()
                .map(Object::toString).toList()) + "]";
    }

    private <K> String formatLfuState(Map<K, Integer> freqs) {
        if (freqs.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        freqs.forEach((k, f) -> sb.append(k).append("(f=").append(f).append("), "));
        sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    private <K, V> K lruEvictTarget(LRUCache<K, V> lru) {
        List<K> keys = lru.keysLruToMru();
        return keys.isEmpty() ? null : keys.getFirst(); // LRU = 첫 번째
    }

    private <K, V> K lfuEvictTarget(LFUCache<K, V> lfu) {
        Map<K, Integer> freqs = lfu.keyFreqs();
        if (freqs.isEmpty()) return null;
        // minFreq 키 중 첫 번째 (내부 getFirst()와 동일)
        int minF = freqs.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        return freqs.entrySet().stream()
                .filter(e -> e.getValue() == minF)
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    // ── 시나리오 ───────────────────────────────────────────────

    @Test
    void scenario1_basicEviction() {
        System.out.println("\n" + THICK);
        System.out.println("시나리오 1: 기본 Eviction — 삽입 순서만 있을 때 (capacity=3)");
        System.out.println(THICK);
        System.out.println("목적: 접근 이력이 없을 때 LRU와 LFU가 같은 항목을 evict함을 확인\n");

        LRUCache<Integer, String> lru = new LRUCache<>(3);
        LFUCache<Integer, String> lfu = new LFUCache<>(3);

        lru.put(1, "a"); lfu.put(1, "a");
        printState("put(1, \"a\")", lru, "", lfu);

        lru.put(2, "b"); lfu.put(2, "b");
        printState("put(2, \"b\")", lru, "", lfu);

        lru.put(3, "c"); lfu.put(3, "c");
        printState("put(3, \"c\")  ← 꽉 참", lru, "", lfu);

        System.out.printf("  [예상 eviction 대상]%n");
        System.out.printf("    LRU  → %s  (가장 오래전에 삽입)%n", lruEvictTarget(lru));
        System.out.printf("    LFU  → %s  (동점 중 LRU)%n\n", lfuEvictTarget(lfu));

        lru.put(4, "d"); lfu.put(4, "d");
        printState("put(4, \"d\")  ← eviction 발생", lru, "", lfu);

        System.out.println("  [결론] 삽입 순서만 있을 때 LRU = LFU. 둘 다 key=1 제거.");
        System.out.println(DIVIDER);
    }

    @Test
    void scenario2_hotItemProtection() {
        System.out.println("\n" + THICK);
        System.out.println("시나리오 2: 핫 아이템 보호 — 빈도 vs 최근성 (capacity=3)");
        System.out.println(THICK);
        System.out.println("목적: 자주 접근된 항목이 있을 때 두 캐시가 다른 항목을 evict함\n");

        LRUCache<Integer, String> lru = new LRUCache<>(3);
        LFUCache<Integer, String> lfu = new LFUCache<>(3);

        lru.put(1, "a"); lfu.put(1, "a");
        printState("put(1, \"a\")", lru, "", lfu);

        System.out.println("  get(1) × 10회 — key=1의 빈도를 높임");
        for (int i = 0; i < 10; i++) {
            lru.get(1);
            lfu.get(1);
        }
        printState("  → get(1) × 10 완료", lru, "", lfu);

        lru.put(2, "b"); lfu.put(2, "b");
        printState("put(2, \"b\")", lru, "", lfu);

        lru.put(3, "c"); lfu.put(3, "c");
        printState("put(3, \"c\")  ← 꽉 참", lru, "", lfu);

        System.out.printf("  [예상 eviction 대상]%n");
        System.out.printf("    LRU  → %s  (put 후 get이 없어 LRU 자리로 밀림)%n", lruEvictTarget(lru));
        System.out.printf("    LFU  → %s  (freq=1 중 가장 오래된 것)%n\n", lfuEvictTarget(lfu));

        lru.put(4, "d"); lfu.put(4, "d");
        printState("put(4, \"d\")  ← eviction 발생", lru, "", lfu);

        System.out.println("  [결론]");
        System.out.println("    LRU: key=1 제거 — get(1) 이후 put(2), put(3)이 더 최근이라 1이 LRU로 밀림");
        System.out.println("    LFU: key=2 제거 — 1의 freq=11 vs 2,3의 freq=1. 빈도 낮은 2 제거");
        System.out.println("    → LRU는 '자주 썼지만 최근 put이 없던' 항목을 희생시킴");
        System.out.println(DIVIDER);
    }

    @Test
    void scenario3_temporalShift() {
        System.out.println("\n" + THICK);
        System.out.println("시나리오 3: 접근 패턴 변화 — LFU의 빈도 편향 (capacity=3)");
        System.out.println(THICK);
        System.out.println("목적: 과거에 인기 있던 항목이 이제 안 쓰일 때 두 캐시의 반응 차이\n");

        LRUCache<Integer, String> lru = new LRUCache<>(3);
        LFUCache<Integer, String> lfu = new LFUCache<>(3);

        lru.put(1, "a"); lfu.put(1, "a");
        lru.put(2, "b"); lfu.put(2, "b");
        lru.put(3, "c"); lfu.put(3, "c");
        printState("put(1,2,3)  ← 초기 상태", lru, "", lfu);

        System.out.println("  [Phase 1] key=1을 집중 접근 — 과거의 '핫' 아이템");
        for (int i = 0; i < 10; i++) {
            lru.get(1);
            lfu.get(1);
        }
        printState("  get(1) × 10 완료", lru, "", lfu);

        System.out.println("  [Phase 2] 접근 패턴 변화 — 이제 key=2, 3이 핫해짐");
        for (int i = 0; i < 8; i++) {
            lru.get(2);
            lfu.get(2);
        }
        for (int i = 0; i < 8; i++) {
            lru.get(3);
            lfu.get(3);
        }
        printState("  get(2)×8, get(3)×8 완료", lru, "", lfu);

        System.out.printf("  [예상 eviction 대상]%n");
        System.out.printf("    LRU  → %s  (get(3)이 가장 최근, get(2) 그 다음, 1이 LRU)%n", lruEvictTarget(lru));
        System.out.printf("    LFU  → %s  (freq: 1=11, 2=9, 3=9. 동점 중 LRU=2)%n\n", lfuEvictTarget(lfu));

        lru.put(4, "d"); lfu.put(4, "d");
        printState("put(4, \"d\")  ← eviction 발생", lru, "", lfu);

        System.out.println("  [결론]");
        System.out.println("    LRU: key=1 제거 — 최근에 아무도 안 씀. 현재 패턴에 빠르게 적응");
        System.out.println("    LFU: key=2 제거 — 1의 과거 freq=11이 높아 보호됨. 빈도 편향!");
        System.out.println("    → LFU는 '이제 필요 없는' 항목을 메모리에 계속 붙잡음");
        System.out.println(DIVIDER);
    }

    @Test
    void scenario4_coldStart() {
        System.out.println("\n" + THICK);
        System.out.println("시나리오 4: Cold Start — 새 항목의 생존 가능성 (capacity=2)");
        System.out.println(THICK);
        System.out.println("목적: 기존 고빈도 항목들 사이에 새 항목이 들어올 때의 차이\n");

        LRUCache<Integer, String> lru = new LRUCache<>(2);
        LFUCache<Integer, String> lfu = new LFUCache<>(2);

        lru.put(1, "a"); lfu.put(1, "a");
        lru.put(2, "b"); lfu.put(2, "b");
        printState("put(1,2)  ← 초기", lru, "", lfu);

        System.out.println("  key=1, 2 모두 고빈도로 만들기 (각 5회)");
        for (int i = 0; i < 5; i++) { lru.get(1); lfu.get(1); }
        for (int i = 0; i < 5; i++) { lru.get(2); lfu.get(2); }
        printState("  get(1)×5, get(2)×5 완료", lru, "", lfu);

        System.out.printf("  [put(3) 예상 eviction 대상]%n");
        System.out.printf("    LRU  → %s  (get(2) 최근 → 2=MRU, 1=LRU)%n", lruEvictTarget(lru));
        System.out.printf("    LFU  → %s  (freq 동점, LRU 기준 1이 먼저)%n\n", lfuEvictTarget(lfu));

        lru.put(3, "c"); lfu.put(3, "c");
        printState("put(3, \"c\")  ← 새 항목 삽입, eviction 발생", lru, "", lfu);

        System.out.println("  새 항목 key=3에 3회 접근 — 빈도를 쌓아봄");
        for (int i = 0; i < 3; i++) { lru.get(3); lfu.get(3); }
        printState("  get(3)×3 완료", lru, "", lfu);

        System.out.printf("  [put(4) 예상 eviction 대상]%n");
        System.out.printf("    LRU  → %s%n", lruEvictTarget(lru));
        System.out.printf("    LFU  → %s%n\n", lfuEvictTarget(lfu));

        lru.put(4, "d"); lfu.put(4, "d");
        printState("put(4, \"d\")  ← 또 새 항목, eviction 발생", lru, "", lfu);

        System.out.println("  [결론]");
        System.out.println("    LRU: get(3)으로 3이 MRU → 2가 LRU → key=2 제거. 새 항목이 생존.");
        System.out.println("    LFU: 3의 freq=4 < 2의 freq=6 → key=3 제거. 새 항목이 희생됨!");
        System.out.println("    → LFU의 Cold Start 약점: 새 항목은 누적 freq가 낮아 바로 쫓겨남");
        System.out.println(DIVIDER);
    }
}
