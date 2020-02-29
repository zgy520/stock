package com.zgy.project.stockServer.core.service.thead.call;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import com.zgy.project.stockServer.core.model.Stock;
import com.zgy.project.stockServer.core.service.StockService;
import com.zgy.project.stockServer.core.service.thead.ScrapyStockThread;
import com.zgy.project.stockServer.core.utils.DateUtil.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class StockHistoryThread implements Callable<LinkedBlockingQueue<HistoryDataT>> {
    private final static Logger log = LoggerFactory.getLogger(ScrapyStockThread.class);
    private final List<Stock> stockList;
    private final String LAST_WORK_DAY = "2019-08-29";
    private final String START_WORK_DAY = "2018-01-01";
    public static List<HistoryDataT> historyDataList = new CopyOnWriteArrayList<>();
    public static LinkedBlockingQueue<HistoryDataT> historyDataTLinkedBlockingQueue = new LinkedBlockingQueue<HistoryDataT>();


    private ThreadLocal<WebClient> threadLocal = new ThreadLocal<>();

    public StockHistoryThread(List<Stock> stockList){
        this.stockList = stockList;

    }

    @Override
    public LinkedBlockingQueue<HistoryDataT> call() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        threadLocal.set(new WebClient(BrowserVersion.CHROME));
        Instant start = Instant.now();
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


            for (Stock urlCode : stockList) {
                //log.info("code:"+urlCode.getCode());
                List<HistoryDataT> stockHistoryList = new ArrayList<>();
                Instant codeStart = Instant.now();
                String url = "http://q.stock.sohu.com/cn/" + urlCode.getCode() + "/lshq.shtml";
                //String url = "http://q.stock.sohu.com/zs/000001/lshq.shtml";
                HtmlPage myPage = null;
                try{
                    myPage = webClient.getPage(url);
                }catch (IllegalStateException ex){
                    //ex.printStackTrace();
                    log.error("thow exception on url: " + url);
                    continue;
                }
                final HtmlForm form = myPage.getFormByName("historyHqForm");

                final HtmlTextInput endField = form.getInputByName("ed");

                HtmlPage queryPage = myPage;
                if (DateUtils.isSkipCurStock(LAST_WORK_DAY, endField.getText())) {
                    log.info("使用默认的时间段进行sddd查询");
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
                    historyDataTLinkedBlockingQueue.add(historyData);

                }

            }

        } catch (ParseException e) {
            //e.printStackTrace();
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }finally {
            threadLocal.get().close();
        }
        Instant end = Instant.now();
        log.info("已完成的线程:"+Thread.currentThread().getName()+"，工进行了数据："+historyDataTLinkedBlockingQueue.size()+"条数据的获取,共用时:"+Duration.between(start,end));
        return historyDataTLinkedBlockingQueue;
    }
}
