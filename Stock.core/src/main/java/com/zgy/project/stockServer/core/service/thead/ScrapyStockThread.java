package com.zgy.project.stockServer.core.service.thead;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.zgy.project.stockServer.core.dao.HistoryDataDao;
import com.zgy.project.stockServer.core.model.HistoryData;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import com.zgy.project.stockServer.core.model.Stock;
import com.zgy.project.stockServer.core.service.StockParser;
import com.zgy.project.stockServer.core.service.StockService;
import com.zgy.project.stockServer.core.utils.DateUtil.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.security.cert.CertificateExpiredException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class ScrapyStockThread implements Runnable{
    private final static Logger log = LoggerFactory.getLogger(ScrapyStockThread.class);
    private List<Stock> stockList = new ArrayList<>();
    private final String LAST_WORK_DAY = "2019-08-29";
    private final String START_WORK_DAY = "2018-01-01";
    public static List<HistoryDataT> historyDataList = new CopyOnWriteArrayList<>();
    public static LinkedBlockingQueue<HistoryDataT> historyDataTLinkedBlockingQueue = new LinkedBlockingQueue<HistoryDataT>();
    private StockService stockService;


    private ThreadLocal<WebClient> threadLocal = new ThreadLocal<>();

    public ScrapyStockThread(List<Stock> stockList,StockService stockService){
        this.stockList = stockList;
        this.stockService = stockService;

    }

    @Override
    public void run() {

      //log.info("当前线程： " + Thread.currentThread().getName()+" 正在运行!");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        threadLocal.set(new WebClient(BrowserVersion.CHROME));

        try {
            WebClient webClient = threadLocal.get();
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setPopupBlockerEnabled(false);
            webClient.getOptions().setPrintContentOnFailingStatusCode(false);
            webClient.getOptions().setRedirectEnabled(true);
            webClient.getOptions().setUseInsecureSSL(true);
            CookieManager cm = new CookieManager();
            webClient.setCookieManager(cm);
            //webClient.setJavaScriptTimeout(3600);
            //webClient.getOptions().setTimeout(9000);
            Instant start = Instant.now();
            for (Stock urlCode : stockList) {
                //log.info("code:"+urlCode.getCode());
                List<HistoryDataT> stockHistoryList = new ArrayList<>();
                Instant codeStart = Instant.now();
                String url = "http://q.stock.sohu.com/cn/" + urlCode.getCode() + "/lshq.shtml";
                //String url = "http://q.stock.sohu.com/zs/000001/lshq.shtml";
                Instant pageStart = Instant.now();
                HtmlPage myPage = null;
                try{
                    myPage = webClient.getPage(url);
                }catch (IllegalStateException ex){
                    ex.printStackTrace();
                    log.error("thow exception on url: " + url);
                    continue;
                }
                Instant pageEnd = Instant.now();
                //log.info("获取页面的时间为:"+Duration.between(pageStart,pageEnd));
                final HtmlForm form = myPage.getFormByName("historyHqForm");

                final HtmlTextInput endField = form.getInputByName("ed");

                HtmlPage queryPage = myPage;
                if (DateUtils.isSkipCurStock(LAST_WORK_DAY, endField.getText())) {
                    //continue;
                    //queryPage = myPage;
                    log.info("使用默认的时间段进行查询");
                }else {
                    HtmlButtonInput button = (HtmlButtonInput) myPage
                            .getByXPath("//input[@value='查询']").get(0);
                    final HtmlTextInput textField = form.getInputByName("sd");

                    textField.setValueAttribute(START_WORK_DAY);
                    queryPage = button.click();
                }

                Document doc = Jsoup.parse(queryPage.asXml());
                //log.info("标题为:" + doc.title());
                Element table = doc.select("#BIZ_hq_historySearch").get(0);
                Elements rows = table.select("tbody tr");

                int count = rows.size();
                //log.info("工获取到的数据量为:" + count + "个");
                for (int i = 1; i < count; i++) {
                    Element row = rows.get(i);
                    Elements cols = row.select("td");

                    Date curDate = sdf.parse(cols.get(0).text()); // 日期
                    BigDecimal statPrice = BigDecimal.valueOf(Double.parseDouble(cols.get(1).text())); // 开盘价
                    BigDecimal endPrice = BigDecimal.valueOf(Double.parseDouble(cols.get(2).text())); // 收盘价
                    BigDecimal range = BigDecimal.valueOf(Double.parseDouble(cols.get(3).text())); // 涨跌额
                    String rangeRate = cols.get(4).text(); // 涨跌率
                    BigDecimal minPrice = BigDecimal.valueOf(Double.parseDouble(cols.get(5).text()));
                    BigDecimal maxPrice = BigDecimal.valueOf(Double.parseDouble(cols.get(6).text()));
                    Integer volumn = Integer.parseInt(cols.get(7).text());
                    BigDecimal turnOver = BigDecimal.valueOf(Double.parseDouble(cols.get(8).text())); // 成交额
                    String turanRate = cols.get(9).text(); // 换手率

                    HistoryDataT historyData = new HistoryDataT(urlCode.getName(), urlCode.getCode(), curDate, statPrice, endPrice, range, rangeRate, minPrice, maxPrice, volumn, turnOver, turanRate);
                    stockHistoryList.add(historyData);
                    //historyDataTLinkedBlockingQueue.add(historyData);

                }
                stockService.save(stockHistoryList);

            }
            Instant end = Instant.now();
            log.info("已完成的线程:"+Thread.currentThread().getName()+"，工进行了数据："+historyDataTLinkedBlockingQueue.size()+"条数据的获取,共用时:"+Duration.between(start,end));

        } catch (ParseException e) {
            //e.printStackTrace();
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }finally {
            threadLocal.get().close();
        }
    }

}
