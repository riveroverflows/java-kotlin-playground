package com.rofs.concurrency.facade;


import com.rofs.concurrency.service.JOptimisticLockStockService;
import org.springframework.stereotype.Component;

@Component
public class JOptimisticLockStockFacade {

    private final JOptimisticLockStockService optimisticLockStockService;

    public JOptimisticLockStockFacade(JOptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                optimisticLockStockService.decrease(id, quantity);
                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
