package com.rofs.concurrency.facade;

import com.rofs.concurrency.repository.JLockRepository;
import com.rofs.concurrency.service.JStockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JNamedLockStockFacade {

    private final JLockRepository lockRepository;
    private final JStockService stockService;

    public JNamedLockStockFacade(JLockRepository lockRepository, JStockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decrease(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
