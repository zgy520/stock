package com.zgy.project.stockServer.core.model;

import com.zgy.project.framework.core.model.common.BaseModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "us_stock")
@Data
@Slf4j
public class Stock extends BaseModel {
    private String code; // 股票代码
    private String name; // 股票名称

    @ManyToMany(mappedBy = "stocks")
    private Set<Plate> plateSet = new HashSet<>();

    public void addPlate(Plate plate){
        this.plateSet.add(plate);
        plate.getStocks().add(this);
    }
    public void removePlate(Plate plate){
        this.plateSet.remove(plate);
        plate.getStocks().remove(this);
    }

    public Stock(String code,String name){
        this.code = code;
        this.name = name;
    }

}
