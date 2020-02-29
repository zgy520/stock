package com.zgy.project.stockServer.core.model;

import com.zgy.project.framework.core.model.common.BaseModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.UUIDGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "us_history_data_new")
@Data
@Slf4j
public class HistoryDataT {
    @Id
    @GeneratedValue(generator = "uuid_short")
    @GenericGenerator(name = "uuid_short",
        strategy = "com.zgy.project.stockServer.core.model.ShortUUIDIncrementGenerator")
    @Column(updatable = false,nullable = false)
    private Long id;
    private String stockName;
    private String stockCode;
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date stockDate;
    @Column(name = "start_price",precision = 10,scale = 2)
    private BigDecimal startPrice; // 开盘价
    @Column(name = "end_price",precision = 10,scale = 2)
    private BigDecimal  endPrice; // 收盘价
    @Column(name = "price_range",precision = 10,scale = 2)
    private BigDecimal  priceRange; // 涨跌幅
    private String rangeRate; // 涨跌率
    @Column(name = "min_price",precision = 10,scale = 2)
    private BigDecimal  minPrice; // 最低价
    @Column(name = "max_price",precision = 10,scale = 2)
    private BigDecimal  maxPrice; // 最高价
    private Integer volumn; // 成交量
    @Column(name = "turan_over_sum",precision = 10,scale = 2)
    private BigDecimal  turnOverSum; // 成交额
    @Column(name = "turan_over_rate")
    private String turanOverRate; // 换手率

    public HistoryDataT(){

    }


    public HistoryDataT(String name, String code, Date stockDate, BigDecimal  startPrice, BigDecimal  endPrice, BigDecimal  range, String rangeRate, BigDecimal  minPrice, BigDecimal  maxPrice, Integer volumn, BigDecimal  turnOverSum, String turanOverRate) {
        this.stockName = name;
        this.stockCode = code;
        this.stockDate = stockDate;
        this.startPrice = startPrice;
        this.endPrice = endPrice;
        this.priceRange = range;
        this.rangeRate = rangeRate;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.volumn = volumn;
        this.turnOverSum = turnOverSum;
        this.turanOverRate = turanOverRate;
    }
}
