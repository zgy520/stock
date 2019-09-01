package com.zgy.project.stockServer.core.model;

import com.zgy.project.framework.core.model.common.BaseModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 板块
 */
@Entity
@Table(name = "us_plate")
@Data
@Slf4j
public class Plate extends BaseModel {
    private String name; // 板块名称
    private String stockCount; // 股票数量
    private Double avgPrice; // 平均价格
    @ManyToMany
    @JoinTable(name = "uc_plate_stock",joinColumns = {@JoinColumn(name = "plate_id")},
            inverseJoinColumns = {@JoinColumn(name = "stock_id")})
    private Set<Stock> stocks = new HashSet<>();

    public void addStock(Stock stock){
        this.stocks.add(stock);
        stock.getPlateSet().add(this);
    }
    public void removeStock(Stock stock){
        this.stocks.remove(stock);
        stock.getPlateSet().remove(this);
    }


}
