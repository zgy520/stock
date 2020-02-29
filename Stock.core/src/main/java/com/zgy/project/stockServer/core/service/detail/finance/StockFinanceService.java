package com.zgy.project.stockServer.core.service.detail.finance;

import com.zgy.project.stockServer.core.dao.StockFinanceDao;
import com.zgy.project.stockServer.core.model.detail.StockFinance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class StockFinanceService {
    @Autowired
    private StockFinanceDao stockFinanceDao;

    public void insert(List<StockFinance> stockFinanceList){
        Instant start = Instant.now();
        stockFinanceDao.saveAll(stockFinanceList);
        Instant end = Instant.now();
        log.info("保存数据:" + stockFinanceList.size() + "共使用的时间为:"+ Duration.between(start,end));
    }
}
