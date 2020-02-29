package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.detail.StockFinance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockFinanceDao extends JpaRepository<StockFinance,Long> {
    List<StockFinance> findByEarningPerShareGreaterThan(Double earningShare);
}
