package com.rofs.lang.java.consumer;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class ConsumerTest {

    @Test
    void consumerTest() {
        // 필요한 객체들을 생성
        UserProcessor processor = new UserProcessor();
        User user = new User("John");

        // 첫 번째 Consumer - 사용자 정보 출력
        Consumer<User> userConsumer = u -> {
            System.out.println("사용자 처리: " + u.name());
        };

        // 두 번째 Consumer - 로깅 역할
        Consumer<User> logger = u -> {
            System.out.println("로깅: " + u.name());
        };

        System.out.println("=== 단일 Consumer 테스트 ===");
        processor.processUser(user, userConsumer);

        System.out.println("\n=== Consumer 체이닝 테스트 ===");
        // andThen을 사용하여 두 Consumer를 연결
        Consumer<User> combined = userConsumer.andThen(logger);
        processor.processUser(user, combined);

        System.out.println("\n=== 여러 Consumer 체이닝 테스트 ===");
        // 세 번째 Consumer 추가 - 알림 역할
        Consumer<User> notifier = u -> {
            System.out.println("알림 발송: " + u.name());
        };

        // 세 개의 Consumer를 모두 체이닝
        Consumer<User> fullChain = userConsumer
            .andThen(logger)
            .andThen(notifier);

        processor.processUser(user, fullChain);
    }

    // 간단한 사용자 정보를 담는 클래스
    record User(String name) {}

    // 사용자 정보를 처리하는 클래스
    static class UserProcessor {

        public void processUser(User user, Consumer<User> processor) {
            // 처리 전 메시지 출력
            System.out.println("처리 시작");

            // Consumer의 accept 메서드 호출하여 실제 처리 수행
            processor.accept(user);

            // 처리 후 메시지 출력
            System.out.println("처리 완료");
        }
    }
}
