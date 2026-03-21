package com.rofs.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@DisplayName("기본 Aspect Order: Retry(바깥) → CB(안쪽)")
class DefaultAspectOrderTest {

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @BeforeEach
    fun setUp() {
        circuitBreakerRegistry.circuitBreaker("pg").reset()
    }

    @Test
    @DisplayName("1건 요청 + maxAttempts=3 → CB에 실패가 3건 기록된다 (부풀림)")
    fun retryInflatesCircuitBreakerFailureCount() {
        val cb = circuitBreakerRegistry.circuitBreaker("pg")

        println("===== 기본 순서 테스트 (Retry 바깥, CB 안쪽) =====")
        println("CB 실패 건수 (호출 전): ${cb.metrics.numberOfFailedCalls}")

        try {
            paymentService.pay()
        } catch (e: Exception) {
            println("최종 예외: ${e.message}")
        }

        val failedCalls = cb.metrics.numberOfFailedCalls
        println("CB 실패 건수 (호출 후): $failedCalls")
        println("→ 1건의 요청인데 CB에 ${failedCalls}건의 실패가 기록됨")

        assertThat(failedCalls).isEqualTo(3)
    }
}

@SpringBootTest(
    properties = [
        "resilience4j.circuitbreaker.circuitBreakerAspectOrder=1",
        "resilience4j.retry.retryAspectOrder=2",
    ]
)
@DisplayName("변경된 Aspect Order: CB(바깥) → Retry(안쪽)")
class FixedAspectOrderTest {

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @BeforeEach
    fun setUp() {
        circuitBreakerRegistry.circuitBreaker("pg").reset()
    }

    @Test
    @DisplayName("1건 요청 + maxAttempts=3 → CB에 실패가 1건만 기록된다")
    fun circuitBreakerSeesRetryAsOneCall() {
        val cb = circuitBreakerRegistry.circuitBreaker("pg")

        println("===== 변경된 순서 테스트 (CB 바깥, Retry 안쪽) =====")
        println("CB 실패 건수 (호출 전): ${cb.metrics.numberOfFailedCalls}")

        try {
            paymentService.pay()
        } catch (e: Exception) {
            println("최종 예외: ${e.message}")
        }

        val failedCalls = cb.metrics.numberOfFailedCalls
        println("CB 실패 건수 (호출 후): $failedCalls")
        println("→ 재시도 3회가 CB에 ${failedCalls}건으로 기록됨")

        assertThat(failedCalls).isEqualTo(1)
    }
}
