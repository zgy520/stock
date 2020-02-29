package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.HistoryDataT;

import java.util.List;

public interface HistoryStockDao {
    void insertList(List<HistoryDataT> historyDataList);
}
