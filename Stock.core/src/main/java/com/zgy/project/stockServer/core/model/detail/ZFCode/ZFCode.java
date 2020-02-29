package com.zgy.project.stockServer.core.model.detail.ZFCode;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "us_stock_zf")
@Data
public class ZFCode {
    @Id
    @GeneratedValue(generator = "uuid_short")
    @GenericGenerator(name = "uuid_short",
            strategy = "com.zgy.project.stockServer.core.model.ShortUUIDIncrementGenerator")
    @Column(updatable = false,nullable = false)
    private Long id;
    private String stockCode;
    private String otherCode;
    @Enumerated(EnumType.ORDINAL)
    private SYType syType;
    private Integer count;

    public ZFCode(){

    }
    public ZFCode(String stockCode,String otherCode,SYType syType,Integer count){
        this.stockCode = stockCode;
        this.otherCode = otherCode;
        this.syType = syType;
        this.count = count;
    }
}
