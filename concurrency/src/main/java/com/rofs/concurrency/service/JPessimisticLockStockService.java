package com.rofs.concurrency.service;

import com.rofs.concurrency.domain.JStock;
import com.rofs.concurrency.repository.JStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JPessimisticLockStockService {

    private final JStockRepository stockRepository;

    public JPessimisticLockStockService(JStockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        JStock stock = stockRepository.findByIdWithPessimisticLock(id);
        stock.decrease(quantity);
        stockRepository.save(stock);
    }
}
