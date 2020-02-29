package com.zgy.project.stockServer.controller;

import com.zgy.project.stockServer.core.service.comment.StockCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

@RestController
@RequestMapping(value = "stockComment")
public class StockCommentController {
    @Autowired
    private StockCommentService stockCommentService;

    @RequestMapping(value = "index")
    public void stockComment() throws IOException, ParseException {
        stockCommentService.getStockComments();
    }
}
