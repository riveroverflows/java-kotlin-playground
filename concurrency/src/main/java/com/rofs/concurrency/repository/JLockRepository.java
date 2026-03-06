package com.rofs.concurrency.repository;

import com.rofs.concurrency.domain.JStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JLockRepository extends JpaRepository<JStock, Long> {

    @Query(value = "SELECT get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "SELECT release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);
}
