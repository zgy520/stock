package com.zgy.project.stockServer.core.model.comment;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "us_stock_comment")
@Slf4j
@Data
public class StockComment {
    @Id
    @GeneratedValue(generator = "uuid_short")
    @GenericGenerator(name = "uuid_short",
            strategy = "com.zgy.project.stockServer.core.model.ShortUUIDIncrementGenerator")
    @Column(updatable = false,nullable = false)
    private Long id;
    private String stockCode;
    private int readCount;
    private int commentCount;
    private String title;
    private String author;
    private Date lastUpdateTime;
}
