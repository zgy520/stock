package com.zgy.project.stockServer.controller;

import com.alibaba.fastjson.JSONArray;
import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import com.zgy.project.stockServer.core.service.StockService;
import com.zgy.project.stockServer.core.service.detail.fund.FundHoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping(value = "stock")
@CrossOrigin(value = "*")
public class StockTestController {
    @Autowired
    private StockService stockService;
    @Autowired
    private FundHoldingService fundHoldingService;
    @RequestMapping(value = "index")
    public String index(){
        return "page/index";
    }

    @RequestMapping(value = "fetchData")
    @ResponseBody
    public void fetchData(){
        stockService.fetchUrlInfo();
    }

    @GetMapping(value = "getCodeList")
    @ResponseBody
    public String getCodeList(){
        return stockService.getStockCodeList();
    }
    @RequestMapping(value = "getHistoryData/{stockCode}")
    @ResponseBody
    public JSONArray getStockHistoryData(String stockCode){
        return stockService.getXCKJStock(stockCode);
    }
    @RequestMapping(value = "randomOne")
    @ResponseBody
    public HistoryDataT randomOne(){
        return stockService.getOne().getContent().get(0);
    }
    @GetMapping(value = "findOne")
    @ResponseBody
    public HistoryDataT findOne(@RequestParam UUID uuid){
        return stockService.findOne(uuid);
    }
    @GetMapping(value = "getFundHoldingInfo")
    @ResponseBody
    public void getFundHoldingInfo(){
        fundHoldingService.getStockFundHoldingInfo();
    }
    @GetMapping(value = "getStockFinance")
    @ResponseBody
    public void getStockinace(){
        stockService.getStockinace();
    }
    @GetMapping(value = "updateStock")
    @ResponseBody
    public void updateStock(){
        stockService.saveStockList();
    }
}
