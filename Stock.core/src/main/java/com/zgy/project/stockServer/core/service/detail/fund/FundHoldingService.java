package com.zgy.project.stockServer.core.service.detail.fund;

import com.zgy.project.stockServer.core.dao.FundHoldingDao;
import com.zgy.project.stockServer.core.model.FundHolding;
import com.zgy.project.stockServer.core.service.StockParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class FundHoldingService {
    private FundHoldingDao fundHoldingDao;
    @Autowired
    public FundHoldingService(FundHoldingDao fundHoldingDao){
        this.fundHoldingDao = fundHoldingDao;
    }
    @Autowired
    private StockParser stockParser;

    public void insert(List<FundHolding> fundHoldingList){
        Instant start = Instant.now();
        fundHoldingDao.saveAll(fundHoldingList);
        Instant end = Instant.now();
        log.info(fundHoldingList.size()+"条数据的保存的时间为:" + Duration.between(start,end));
    }

    public void getStockFundHoldingInfo(){
        stockParser.getStockFundHolding();
    }
}
