package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.FundHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundHoldingDao extends JpaRepository<FundHolding,Long> {
    List<FundHolding> findByFundCountGreaterThan(int fundCount);

    List<FundHolding> findByStockCodeIn(String[] codeArray);
}
