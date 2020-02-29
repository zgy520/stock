package com.zgy.project.stockServer.core.model.detail.SYL;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Entity
@Table(name = "us_stock_earning")
public class SYL {
    @Id
    @GeneratedValue(generator = "uuid_short")
    @GenericGenerator(name = "uuid_short",
            strategy = "com.zgy.project.stockServer.core.model.ShortUUIDIncrementGenerator")
    @Column(updatable = false,nullable = false)
    private Long id;
    private String code;
    private Date reportDate;
    private Double syl;

    public SYL(){

    }

    public SYL(String code,Date reportDate,Double syl){
        this.code = code;
        this.reportDate = reportDate;
        this.syl = syl;
    }

    @Override
    public String toString(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return String.format("%s在%s的收益率为:%s",this.code,sdf.format(this.reportDate),String.valueOf(this.syl));
    }
}
