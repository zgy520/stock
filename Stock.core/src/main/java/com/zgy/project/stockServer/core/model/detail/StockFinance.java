package com.zgy.project.stockServer.core.model.detail;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * 股票财务指标
 */
@Table(name = "us_stock_finace")
@Entity
@Slf4j
@Data
public class StockFinance {
    @Id
    @GeneratedValue(generator = "uuid_short")
    @GenericGenerator(name = "uuid_short",
            strategy = "com.zgy.project.stockServer.core.model.ShortUUIDIncrementGenerator")
    @Column(updatable = false,nullable = false)
    private Long id;
    private String stockName;
    private String stockCode;
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyyMMdd")
    private Date stockDate;
    private Long primaryIncome; // 主营收入
    private Double earningPerShare; // 每股收益
    private Long netMargin; // 净利润
    private Long totalAsset; // 总资产

    public StockFinance(){

    }

    public StockFinance(String stockName,String stockCode,Date stockDate,String primaryIncome,String earningPerShare,
                        String netMargin,String totalAsset){
        this.stockName = stockName;
        this.stockCode = stockCode;
        this.stockDate = stockDate;
        this.primaryIncome = Long.parseLong(primaryIncome);
        this.earningPerShare = Double.parseDouble(earningPerShare);
        this.netMargin = Long.parseLong(netMargin);
        this.totalAsset = Long.parseLong(totalAsset);
    }
}
