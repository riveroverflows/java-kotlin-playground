package com.rofs.resilience

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.springframework.stereotype.Service

@Service
class PaymentService {

    @CircuitBreaker(name = "pg")
    @Retry(name = "pg")
    fun pay(): String {
        println("[PG 호출] 시도")
        throw RuntimeException("PG 서버 장애")
    }
}
