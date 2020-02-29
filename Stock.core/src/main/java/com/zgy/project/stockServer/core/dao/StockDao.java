package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDao extends JpaRepository<Stock,Long> {
}
