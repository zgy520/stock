package com.zgy.project.stockServer;

import com.zgy.project.stockServer.core.model.EntityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.zgy.project")
@EntityScan(basePackageClasses = {EntityConfig.class})
public class StockServerApplication {
    public static void main(String[] args){
        SpringApplication.run(StockServerApplication.class,args);
    }

}

