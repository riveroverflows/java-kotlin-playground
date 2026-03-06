package com.rofs.concurrency.service;

public class JStockProxyService {

    private JStockService stockService;

    public JStockProxyService(JStockService stockService) {
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        startTransaction();
        stockService.decrease(id, quantity);
        endTransaction();
    }

    private void startTransaction() {
        System.out.println("Transaction started");
    }

    private void endTransaction() {
        System.out.println("Transaction ended");
    }
}
