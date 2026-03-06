package com.rofs.concurrency.service;

import com.rofs.concurrency.JConcurrencyApplication;
import com.rofs.concurrency.domain.JStock;
import com.rofs.concurrency.repository.JStockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JConcurrencyApplication.class)
class JPessimisticLockStockServiceTest {

    @Autowired
    private JPessimisticLockStockService stockService;

    @Autowired
    private JStockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new JStock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void decrease() {
        stockService.decrease(1L, 1L);
        // 100 - 1 = 99
        JStock stock = stockRepository.findById(1L).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(99);
    }

    @Test
    public void 동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        JStock stock = stockRepository.findById(1L).orElseThrow();

        // 100 - (1 * 100) = 0
        assertThat(stock.getQuantity()).isEqualTo(0);
    }
}