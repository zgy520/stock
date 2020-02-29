package com.zgy.project.stockServer.core.service.analysis;

import com.zgy.project.stockServer.core.dao.FundHoldingDao;
import com.zgy.project.stockServer.core.dao.HistoryDataDao;
import com.zgy.project.stockServer.core.dao.SYLDao;
import com.zgy.project.stockServer.core.dao.StockFinanceDao;
import com.zgy.project.stockServer.core.model.FundHolding;
import com.zgy.project.stockServer.core.model.Stock;
import com.zgy.project.stockServer.core.model.detail.SYL.SYL;
import com.zgy.project.stockServer.core.model.detail.StockFinance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockAnalysisService {
    private static final String[] selfCodeArray = {"601336","600585","001979","600801","000581","000401","600459",
            "600587","600668","000672","600136","002302","000723","600188","601555","002600",
            "601519","601319","601225","002367","600804","600354","000413","000166",
            "600368","600369","600020","002447"};
    @Autowired
    private StockFinanceDao stockFinanceDao;
    @Autowired
    private FundHoldingDao fundHoldingDao;
    @Autowired
    private HistoryDataDao historyDataDao;
    @Autowired
    private SYLDao sylDao;

    /**
     * 获取持股基金最低数量的所有股票
     * @param minCount
     * @return
     */
    public List<FundHolding> getStockByHolding(int minCount){
        List<FundHolding> stockList = new ArrayList<>();
        stockList = fundHoldingDao.findByStockCodeIn(selfCodeArray);
        for (FundHolding fundHolding : stockList){
            log.info("股票:" + fundHolding.getStockCode() + ":的基金数为:"+fundHolding.getFundCount());
        }
        return stockList;
    }

    /**
     * 获取2017年至今每股收益在enrningShare以上的且数量在7以上的股票
     * @param earningShare
     */
    public void analysisEarning(Double earningShare){
        if (earningShare == null)
            earningShare = 1.0;
        List<StockFinance> stockFinanceList = stockFinanceDao.findByEarningPerShareGreaterThan(earningShare);
        Map<String,Integer> codeCountMap = new HashMap<>();
        log.info("获取到的收益在" + earningShare.toString() + "以上的数量为:" + stockFinanceList.size());
        // 根据日期分组
        Map<String,List<StockFinance>> stockFinanceByStockCode = stockFinanceList.stream().collect(Collectors.groupingBy(finance->finance.getStockCode()));
        log.info("分组后的数量为:" + stockFinanceByStockCode.keySet().size()+"个");
        for (String code : stockFinanceByStockCode.keySet()){
            List<StockFinance> financeList = stockFinanceByStockCode.get(code);
            if (financeList.size() > 6){
                log.info("代码:" + code + ",对应的分组后的数量为:" + stockFinanceByStockCode.get(code).size() + "个");
                codeCountMap.put(code,financeList.size());
            }
        }
        Map<String,Integer> sortedMap = new LinkedHashMap<>();
        codeCountMap.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .forEachOrdered(x->sortedMap.put(x.getKey(),x.getValue()));
        log.info("获取到数量超过6个股票有:" + sortedMap.keySet().size()+"个");
        System.out.println(sortedMap);
    }


    public void getEearningAddPercent(){
        // 获取每股基于上一个季度的增长率
        List<StockFinance> stockFinanceList = stockFinanceDao.findAll().stream()
                .filter(stockFinance->StringUtils.indexOfAny(stockFinance.getStockName(),new String[]{"ST","银行"}) == -1)
                .collect(Collectors.toList());

        // 根据股票代码进行分组
        Map<String,List<StockFinance>> groupByCode = stockFinanceList.stream()
                    .collect(Collectors.groupingBy(stockFinance->stockFinance.getStockCode()));
        // 输出每个股票所对应的每个收益的数量
        for (String code : groupByCode.keySet()){
            List<StockFinance> codeFinanceList = groupByCode.get(code);
            // 根据季度日期进行排序
            codeFinanceList.sort(Comparator.comparing(f1->f1.getStockDate()));
            // 针对排序后的列表进行计算季度收益率
            calJDSYL(code,codeFinanceList);
        }
    }

    /**
     * 计算收益率
     * @param code
     * @param stockFinanceList
     */
    private void calJDSYL(String code,List<StockFinance> stockFinanceList){
        int count = stockFinanceList.size();
        SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd");
        List<SYL> sylList = new ArrayList<>();
        for(int i = 0; i < count - 1; i++){
            StockFinance stockFinance = stockFinanceList.get(i);
            StockFinance nextStockFinane = stockFinanceList.get(i + 1);
            Double syl = 0d;
            if (stockFinance.getEarningPerShare() != 0)
                syl = calCYL(code,stockFinance.getEarningPerShare(),nextStockFinane.getEarningPerShare(),sdf.format(nextStockFinane.getStockDate()));

            sylList.add(new SYL(code,nextStockFinane.getStockDate(),syl));
        }

        sylDao.saveAll(sylList);

    }


    public void getLastYearSYL(Double sylRate,String strStartDate,String strEndDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String,List<SYL>> filterSylMap = new HashMap<>();
        try {
            Date startDate = sdf.parse("2018-03-01");
            Date endDate = sdf.parse("2019-07-01");
            if (StringUtils.isNotBlank(strStartDate))
                startDate = sdf.parse(strStartDate);
            if (StringUtils.isNotBlank(strEndDate))
                endDate = sdf.parse(strEndDate);

            List<SYL> stockSYLList = sylDao.findByReportDateBetween(startDate,endDate);
            Map<String,List<SYL>> stlMap = stockSYLList.stream().collect(Collectors.groupingBy(syl->syl.getCode()));
            for (String code : stlMap.keySet()){
                List<SYL> sylList = stlMap.get(code);
                boolean flag = true;
                for (SYL sy : sylList){
                    if (sy.getSyl() < sylRate){
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    filterSylMap.put(code,sylList);
            }
            log.info("共有大于"+String.valueOf(sylRate)+"的股票:" + filterSylMap.keySet().size() + "个");
            for (String code : filterSylMap.keySet()){
                log.info("最近一年每季度大于"+String.valueOf(sylRate)+"的股票为:" + code);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * 计算收益率
     * @param code
     * @param start
     * @param next
     * @param date
     * @return
     */
    private Double calCYL(String code,Double start,Double next,String date){
        Double ylv = (next - start) / start;
        //log.info(code + "在" + date + "的收益率为:" + ylv.toString());
        return ylv;
    }
}
