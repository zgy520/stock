package com.zgy.project.stockServer.controller;

import com.zgy.project.stockServer.core.service.analysis.StockAnalysisService;
import com.zgy.project.stockServer.core.service.analysis.StockIncrementChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping(value = "stockAnalysis")
public class StockAnalysisController {
    @Autowired
    private StockAnalysisService stockAnalysisService;
    @Autowired
    private StockIncrementChangeService stockIncrementChangeService;


    @GetMapping(value = "index")
    public void analysisStock(Double earningShare){
        stockAnalysisService.analysisEarning(earningShare);
    }

    @GetMapping(value = "getSYL")
    public void getSYL(){
        stockAnalysisService.getEearningAddPercent();
    }
    @GetMapping(value = "getYearSYL")
    public void getYearSYL(Double sylRate,String strStartDate,String strEndDate){
        stockAnalysisService.getLastYearSYL(sylRate,strStartDate,strEndDate);
    }
    @GetMapping(value = "getStockChangeInfo")
    public void getStockChangeInfo() throws ParseException {
        stockIncrementChangeService.onlyStockAnalysis();
    }

    @GetMapping(value = "getStockFundInfo")
    public void getStockFundInfo() throws ParseException {
        stockAnalysisService.getStockByHolding(1);
    }
}
