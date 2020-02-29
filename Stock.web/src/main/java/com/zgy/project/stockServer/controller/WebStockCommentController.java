package com.zgy.project.stockServer.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zgy.project.stockServer.core.service.comment.StockCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping(value = "stock/comment")
@Slf4j
public class WebStockCommentController {
    @Autowired
    private StockCommentService stockCommentService;

    @RequestMapping(value = "index")
    public String commentIndex(){
        return "page/comment/index";
    }
    @GetMapping(value = "fetData")
    @ResponseBody
    public JSONObject fetData(@PageableDefault(page = 1, size = 200) Pageable pageable){
        JSONObject jsonArray = stockCommentService.fetData(pageable);
        return jsonArray;
    }
    @PostMapping("generateDataSet")
    @ResponseBody
    public void generateDataSet(String idList){
        stockCommentService.generateDataSet(idList);
    }

    @GetMapping("generateStockList")
    @ResponseBody
    public void generateStockList() throws IOException {
        try {
            stockCommentService.generateWord();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
