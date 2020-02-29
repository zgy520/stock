package com.zgy.project.stockServer.core.service.thead;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.zgy.project.stockServer.core.model.FundHolding;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import com.zgy.project.stockServer.core.model.Stock;
import com.zgy.project.stockServer.core.service.StockService;
import com.zgy.project.stockServer.core.service.detail.fund.FundHoldingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 抓取股票的持仓基金
 */
@Slf4j
public class ScrapyStockFundHoldingThread implements Runnable {
    private List<Stock> stockList = new ArrayList<>();
    private FundHoldingService fundHoldingService;

    public ScrapyStockFundHoldingThread(List<Stock> stockList, FundHoldingService fundHoldingService) {
        this.stockList = stockList;
        this.fundHoldingService = fundHoldingService;

    }

    @Override
    public void run() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<FundHolding> fundHoldingList = new ArrayList<>();
        try {
            for (Stock stock : stockList){
                Document doc = Jsoup.connect("http://q.stock.sohu.com/cn/"+stock.getCode()+"/jjcc.shtml").get();
                //log.info("标题为:" + doc.title());

                Element stockElement = doc.select("div.innerStr2ColumnBL").get(0);
                String reportTitle[] = stockElement.select("h4").text().split("："); // 报告期
                String reportDate = "2010-01-01";
                if (reportTitle.length==2){
                    reportDate = reportTitle[1];
                }else {
                    log.info(stock.getCode()+",报告期为:" + stockElement.select("h4").text());
                }

                Elements rows = stockElement.select("table tr");
                String fundCount = rows.get(0).select("td").text(); // 基金家数
                if (StringUtils.isBlank(fundCount) || fundCount.equals("--")){
                    fundCount = "0";
                }
                String newFundCount = rows.get(1).select("td").text(); // 新进基金家数
                if (StringUtils.isBlank(newFundCount) || newFundCount.equals("--")){
                    newFundCount = "0";
                }
                String plusFundCount = rows.get(2).select("td").text(); // 加仓基金家数
                if (StringUtils.isBlank(plusFundCount) || plusFundCount.equals("--")){
                    plusFundCount = "0";
                }
                String minFundCount = rows.get(3).select("td").text(); //减仓基金家数
                if (StringUtils.isBlank(minFundCount) || minFundCount.equals("--")){
                    minFundCount = "0";
                }
                String exitFundCount = rows.get(4).select("td").text(); // 退出基金家数
                if (StringUtils.isBlank(exitFundCount) || exitFundCount.equals("--")){
                    exitFundCount = "0";
                }
                String holdCount = rows.get(5).select("td").text(); // 持股总数(万股)
                if (StringUtils.isBlank(holdCount) || holdCount.equals("--")){
                    holdCount = "0";
                }
                String holdChange = rows.get(6).select("td").text(); // 总持仓变化(万股)
                if (StringUtils.isBlank(holdChange) || holdChange.equals("--")){
                    holdChange = "0";
                }
                String pertant = rows.get(7).select("td").text(); // 总持仓占流通盘比例
                if (StringUtils.isBlank(pertant) || pertant.equals("--")){
                    pertant = "0";
                }
                String totalMonty = rows.get(8).select("td").text(); // 总持股市值(万元)
                if (StringUtils.isBlank(totalMonty) || totalMonty.equals("--")){
                    totalMonty = "0";
                }
                fundHoldingList.add(new FundHolding(stock.getName(),stock.getCode(),sdf.parse(reportDate),fundCount,newFundCount,plusFundCount,minFundCount,
                        exitFundCount,holdCount,holdChange,pertant,totalMonty));
                /*log.info(String.format("基金=%s,报告期=%s的总基金数为=%s,new=%s,plus=%s,min=%s,exit=%s,holdCount=%s,holdChange=%s,petant=%s,total=%s",
                            stock.getName(),reportDate,fundCount,newFundCount,plusFundCount,minFundCount,exitFundCount,holdCount,holdChange,pertant,totalMonty));*/
             }
             fundHoldingService.insert(fundHoldingList);
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
