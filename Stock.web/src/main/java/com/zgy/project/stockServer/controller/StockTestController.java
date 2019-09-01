package com.zgy.project.stockServer.controller;

import com.alibaba.fastjson.JSONArray;
import com.zgy.project.stockServer.core.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "stock")
@CrossOrigin(value = "*")
public class StockTestController {
    @Autowired
    private StockService stockService;
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
}
