package com.zgy.project.stockServer.core.dao;

import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryDataDao extends JpaRepository<HistoryDataT, UUID> {

    @Query(value = "select hd from HistoryDataT hd order by hd.endPrice desc")
    Page<HistoryDataT> findRandomOne(Pageable pageable);

    List<HistoryDataT> findByStockDateAfter(Date fromDate);

    /**
     * 根据代码前缀获取数据
     * @param codePrex
     * @return
     */
    List<HistoryDataT> findByStockCodeStartingWith(String codePrex);

    /**
     * 根据股票代码获取历史数据
     * @param stockCode
     * @return
     */
    List<HistoryDataT> findByStockCode(String stockCode);

    List<HistoryDataT> findByStockCodeIn(List<String> codeList);

    List<HistoryDataT> findByStockDateGreaterThanAndStockCodeIn(Date stockDate,List<String> codeList);
}
