package com.zgy.project.stockServer.core.service;

import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.Stock;

import java.util.List;

public interface StockParser {

    /**
     * 根据url获取股票数据
     * @param url
     * @return
     */
    List<HistoryData> getStockList(String url);

    /**
     * 获取股票代码
     * @return
     */
    List<Stock> getStockCodeList();

    /**
     * 获取股票的持仓基金
     */
    void getStockFundHolding();

    /**
     * 获取股票的财务信息
     */
    void getStockFinanceInfo();

    /**
     * 获取股票的历史数据
     */
    void getStockHistoryData();
}
