package com.rofs.concurrency.service;

import com.rofs.concurrency.domain.JStock;
import com.rofs.concurrency.repository.JStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class JStockService {

    private final JStockRepository stockRepository;

    public JStockService(JStockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

//    @Transactional
    public synchronized void decrease(Long id, Long quantity) {
        // stock 조회
        // 재고 감소
        // 갱신된 값 저장

        JStock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }
}
