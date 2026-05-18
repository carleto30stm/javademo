package com.productos.demo.exchange.domain.repository;

import com.productos.demo.exchange.domain.model.RateChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateChangeLogRepository extends JpaRepository<RateChangeLog, Long> {
}
