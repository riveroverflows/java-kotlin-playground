package com.rofs.concurrency.repository;

import com.rofs.concurrency.domain.JStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface JStockRepository extends JpaRepository<JStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM JStock s WHERE s.id = :id")
    JStock findByIdWithPessimisticLock(Long id);


    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM JStock s WHERE s.id = :id")
    JStock findByIdWithOptimisticLock(Long id);
}
