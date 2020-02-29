package com.zgy.project.stockServer.core.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import com.zgy.project.stockServer.core.model.Stock;
import com.zgy.project.stockServer.core.model.detail.StockFinance;
import com.zgy.project.stockServer.core.service.detail.finance.StockFinanceService;
import com.zgy.project.stockServer.core.service.detail.fund.FundHoldingService;
import com.zgy.project.stockServer.core.service.thead.ScrapyStockFundHoldingThread;
import com.zgy.project.stockServer.core.service.thead.ScrapyStockThread;
import com.zgy.project.stockServer.core.service.thead.call.StockHistoryThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SHStockImpl implements StockParser {
    @Autowired
    private StockService stockService;
    @Autowired
    private FundHoldingService fundHoldingService;
    @Autowired
    private StockFinanceService stockFinanceService;

    @Override
    public List<HistoryData> getStockList(String url) {
        List<Stock> stockList = getStockCodeList();
        final int chunkSize = 400;
        final AtomicInteger counter = new AtomicInteger();

        final Collection<List<Stock>> result = stockList.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                    .values();

        System.out.println(result);
        Iterator<List<Stock>> stockIterator = result.iterator();
        while (stockIterator.hasNext()){
            ScrapyStockThread scrapyStockThread = new ScrapyStockThread(stockIterator.next(),stockService);
            Thread scrapyThread = new Thread(scrapyStockThread);
            scrapyThread.start();
        }

        return null;
    }

    @Override
    public List<Stock> getStockCodeList() {
        List<Stock> stockList = new ArrayList<>();
        Instant start = Instant.now();
        //stockList.add(new Stock("000000","上证指数"));
        if (stockList.size() > 0) return stockList;
        // 获取股票代码,基于jsoup
        try {
            Document doc = Jsoup.connect("http://quote.eastmoney.com/stock_list.html").get();
            //log.info("标题为:" + doc.title());

            Elements stockElements = doc.select("#quotesearch ul li");
            int count = 0;
            for (Element stock : stockElements){
                String stockText = stock.text();
                String codeStr = stockText.substring(stockText.indexOf("(") + 1, stockText.indexOf(")"));
                String codeNameStr = stockText.substring(0,stockText.indexOf("("));
                if (codeStr.startsWith("60") || codeStr.startsWith("00") || codeStr.startsWith("300")){
                //if (codeStr.startsWith("60")){
                    count++;
                    //log.info("获取到的股票代码为:" + codeStr+",股票名为:" + codeNameStr);
                    Stock stock1 = new Stock(codeStr,codeNameStr);
                    stockList.add(stock1);
                }
            }
            log.info("获取到的元素数量为:" + count + "个");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Instant end = Instant.now();
        log.info("获取股票代码的时间为:" + Duration.between(start,end));
        return stockList;
    }

    @Override
    public void getStockFundHolding() {
        List<Stock> stockList = getStockCodeList();
        final int chunkSize = 200;
        final AtomicInteger counter = new AtomicInteger();

        final Collection<List<Stock>> result = stockList.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                .values();

        System.out.println(result);
        Iterator<List<Stock>> stockIterator = result.iterator();
        while (stockIterator.hasNext()){
            ScrapyStockFundHoldingThread scrapyStockThread = new ScrapyStockFundHoldingThread(stockIterator.next(),fundHoldingService);
            Thread scrapyThread = new Thread(scrapyStockThread);
            scrapyThread.start();
        }
    }

    @Override
    public void getStockFinanceInfo() {
        List<Stock> stockList = getStockCodeList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");


            for (Stock stock : stockList) {
                try {
                    List<StockFinance> stockFinanceList = new ArrayList<>();
                    Document doc = Jsoup.connect("http://q.stock.sohu.com/cn/" + stock.getCode() + "/cwzb.shtml").get();
                    Elements stockElement = doc.select("div.innerStr2Column"); // 获取四个table所在的层

                    Elements tableElemtns = stockElement.select("table");
                    int count = tableElemtns.size();

                    Map<String, List<String>> financeList = new HashMap<>();

                    for (int i = 0; i < count; i++) {
                        Element table = tableElemtns.get(i);  // 获取第一个table
                        // 获取所有的tr
                        Elements rows = table.select("tr");
                        for (int j = 1; j < rows.size(); j++) {
                            Elements cols = rows.get(j).select("td");
                            String dateStr = "20190201";
                            for (int n = 1; n < cols.size(); n++) {
                                dateStr = rows.get(0).select("th").get(n).text().replace("(", "").replace(")", "") + cols.get(0).text();
                                if (!financeList.containsKey(dateStr)) {
                                    List<String> list = new ArrayList<>();
                                    list.add(cols.get(n).text());
                                    financeList.put(dateStr, list);
                                } else {
                                    financeList.get(dateStr).add(cols.get(n).text());
                                }
                                //log.info(String.format("第%d个的%s的值为:%s",i,dateStr,cols.get(n).text()));
                            }
                        }
                    }
                    Set<String> keys = financeList.keySet();
                    for (String key : keys) {
                        List<String> dataList = financeList.get(key);
                        if (dataList.get(0).equals("--") || dataList.get(1).equals("--") || dataList.get(2).equals("--") || dataList.get(3).equals("--")){
                            continue;
                        }
                        StockFinance stockFinance = new StockFinance(stock.getName(), stock.getCode(), sdf.parse(key),
                                dataList.get(0), dataList.get(2), dataList.get(1), dataList.get(3));
                        stockFinanceList.add(stockFinance);
                    }
                    stockFinanceService.insert(stockFinanceList);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


    }

    @Override
    public void getStockHistoryData() {
        List<Stock> stockList = getStockCodeList();
        final int chunkSize = 400;
        final AtomicInteger counter = new AtomicInteger();

        final Collection<List<Stock>> result = stockList.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                .values();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(counter.get());

        List<Future<LinkedBlockingQueue<HistoryDataT>>> resultList = new ArrayList<>();
        Iterator<List<Stock>> stockIterator = result.iterator();
        while (stockIterator.hasNext()){
            StockHistoryThread stockHistoryThread = new StockHistoryThread(stockIterator.next());
            Future<LinkedBlockingQueue<HistoryDataT>> historyDataList = executor.submit(stockHistoryThread);
            resultList.add(historyDataList);
        }

        for (Future<LinkedBlockingQueue<HistoryDataT>> future : resultList){
            try {
                log.info("获取到的历史数据的数量为:" + future.get().size()+",任务状态为:"+future.isDone());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }
}
