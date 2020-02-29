package com.zgy.project.stockServer.core.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * 基金持仓
 */
@Entity
@Table(name = "us_fund_hold")
@Data
public class FundHolding {
    @Id
    @GeneratedValue(generator = "uuid_short")
    @GenericGenerator(name = "uuid_short",
            strategy = "com.zgy.project.stockServer.core.model.ShortUUIDIncrementGenerator")
    @Column(updatable = false,nullable = false)
    private Long id;
    private String stockName;
    private String stockCode;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private Date reportDate;
    private Integer fundCount; // 基金总数
    private Integer newFundCount; // 新进数量
    private Integer plusHoldCount; // 加仓基金家数
    private Integer minHoldCount; // 减仓基金家数
    private Integer exitFundCount; // 退除基金家数
    private Integer holdCount; // 持股总数(万股）
    private Integer holdChange; // 总持仓变化(万股）
    private String percent; // 流通盘比例
    private Long totalMoney; // 总持股市值

    public FundHolding(){

    }

    public FundHolding(String stockName,String stockCode,Date reportDate,String fundCount,String newFundCount,String plusHoldCount,
                       String minHoldCount,String exitHoldCount,String holdCount,String holdChange,String percent,String totalMonety
                       ){
        this.stockName = stockName;
        this.stockCode = stockCode;
        this.reportDate = reportDate;
        this.fundCount = Integer.parseInt(fundCount);
        this.newFundCount = Integer.parseInt(newFundCount);
        this.plusHoldCount = Integer.parseInt(plusHoldCount);
        this.minHoldCount = Integer.parseInt(minHoldCount);
        this.exitFundCount = Integer.parseInt(exitHoldCount);
        this.holdCount = Integer.parseInt(holdCount);
        this.holdChange = Integer.parseInt(holdChange);
        this.percent = percent;
        this.totalMoney = Long.valueOf(totalMonety);
    }
}
