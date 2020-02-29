package com.zgy.project.stockServer.core.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.zgy.project.stockServer.core.dao.HistoryDataDao;
import com.zgy.project.stockServer.core.dao.HistoryStockDao;
import com.zgy.project.stockServer.core.dao.StockDao;
import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import com.zgy.project.stockServer.core.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockService {

    private HistoryDataDao historyDataDao;
    @Autowired
    private StockDao stockDao;
    @Autowired
    private HistoryStockDao historyStockDao;

    public StockService(@Autowired HistoryDataDao historyDataDao){
        this.historyDataDao = historyDataDao;
    }

    @Autowired
    private StockParser stockParser;

    public String getStockCodeList(){
        List<Stock> codeList = stockParser.getStockCodeList();
        return codeList.stream().map(Stock::getCode).collect(Collectors.joining(","));
    }

    public String fetchData(String myurl){
        int i = 0;
        StringBuffer sb = new StringBuffer("");
        URL url;
        try {
            url = new URL(myurl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "gb2312"));
            String s = "";
            while ((s = br.readLine()) != null) {
                i++;

                sb.append(s + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(sb.toString());

        return sb.toString();
    }

    public void fetchByJsoup(String url){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            List<HistoryData> historyDataList = new ArrayList<>();
            WebClient webClient = new WebClient();
            HtmlPage myPage = webClient.getPage(url);
            final HtmlForm form = myPage.getFormByName("historyHqForm");

            HtmlButtonInput button = (HtmlButtonInput) myPage
                    .getByXPath("//input[@value='查询']").get(0);
            final HtmlTextInput textField = form.getInputByName("sd");

            textField.setValueAttribute("2018-1-01");
            HtmlPage queryPage = button.click();

            Document doc = Jsoup.parse(queryPage.asXml());
            log.info("标题为:" + doc.title());
            Element table = doc.select("#BIZ_hq_historySearch").get(0);
            Elements rows = table.select("tbody tr");

            int count = rows.size();
            log.info("工获取到的数据量为:"+count+"个");
            for (int i = 1; i < count; i++){
                Element row = rows.get(i);
                Elements cols = row.select("td");

                log.info(String.format("%s:%s:%s",cols.get(0).text(),cols.get(1).text(),cols.get(2).text()));
                /*for (int j  = 0; j < cols.size(); j++){
                    log.info("%s",cols.get(j).text());
                }*/
                Date curDate = sdf.parse(cols.get(0).text()); // 日期
                BigDecimal  statPrice =BigDecimal.valueOf(Double.parseDouble(cols.get(1).text())); // 开盘价
                BigDecimal  endPrice = BigDecimal.valueOf(Double.parseDouble(cols.get(2).text())); // 收盘价
                BigDecimal range = BigDecimal.valueOf(Double.parseDouble(cols.get(3).text())); // 涨跌额
                String rangeRate = cols.get(4).text(); // 涨跌率
                BigDecimal  minPrice = BigDecimal.valueOf(Double.parseDouble(cols.get(5).text()));
                BigDecimal  maxPrice = BigDecimal.valueOf(Double.parseDouble(cols.get(6).text()));
                Integer volumn = Integer.parseInt(cols.get(7).text());
                BigDecimal  turnOver = BigDecimal.valueOf(Double.parseDouble(cols.get(8).text())); // 成交额
                String turanRate = cols.get(9).text(); // 换手率

                HistoryData historyData = new HistoryData("晨鑫科技","002447",curDate,statPrice,endPrice,range,rangeRate,minPrice,maxPrice,volumn,turnOver,turanRate);
                historyDataList.add(historyData);
            }

            log.info("工需要存储的数据量wei:" + historyDataList.size());
            //historyDataDao.saveAll(historyDataList);

        } catch (IOException e) {

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void fetchUrlInfo(){
        //fetchByJsoup("http://q.stock.sohu.com/cn/002447/lshq.shtml");
        stockParser.getStockList("");
        stockParser.getStockHistoryData();
    }

    /**
     * 根据股票代码获取相应的历史数据
     * @param stockCode
     * @return
     */
    public JSONArray getXCKJStock(String stockCode){
        List<HistoryDataT> historyDataList = historyDataDao.findAll();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JSONArray jsonArray = new JSONArray();
        historyDataList.stream().forEach(data->{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("time",sdf.format(data.getStockDate()));
            jsonObject.put("start",data.getStartPrice());
            jsonObject.put("max",data.getMaxPrice());
            jsonObject.put("min",data.getMinPrice());
            jsonObject.put("end",data.getEndPrice());
            jsonObject.put("volumn",data.getVolumn());
            jsonObject.put("money",data.getTurnOverSum());
            jsonArray.add(jsonObject);
        });
        return jsonArray;
    }


    public void save(List<HistoryDataT> historyDataList){
        Instant start = Instant.now();
        historyDataDao.saveAll(historyDataList);
        Instant end = Instant.now();
        log.info("保存:"+historyDataList.size()+"条数据用时:"+Duration.between(start,end));
    }

    public Page<HistoryDataT> getOne(){
        Pageable pageable = PageRequest.of(1,2);
        return historyDataDao.findRandomOne(pageable);
    }

    public HistoryDataT findOne(UUID uuid){
        HistoryDataT hdt = historyDataDao.findById(uuid).get();
        return hdt;
    }
    public void getStockinace(){
        stockParser.getStockFinanceInfo();
    }

    public void saveStockList(){
        List<Stock> stockList = stockParser.getStockCodeList();
        Instant start = Instant.now();
        stockDao.saveAll(stockList);
        Instant end = Instant.now();
        log.info(stockList.size()+"条数据的保存时间为:" + Duration.between(start,end));
    }
}
