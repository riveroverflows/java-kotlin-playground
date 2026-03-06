package com.rofs.concurrency.repository;

import com.rofs.concurrency.domain.JStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JStockRepository extends JpaRepository<JStock, Long> {
}
