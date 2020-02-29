package com.zgy.project.stockServer.core.service.analysis;

import com.zgy.project.stockServer.core.dao.HistoryDataDao;
import com.zgy.project.stockServer.core.dao.ZFCodeDao;
import com.zgy.project.stockServer.core.model.HistoryDataT;
import com.zgy.project.stockServer.core.model.detail.ZFCode.SYType;
import com.zgy.project.stockServer.core.model.detail.ZFCode.ZFCode;
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
public class StockIncrementChangeService {
    private final static String commonStockCode = "‘601318\t’600519\t‘600036\t’601166\t‘000651\t’000333\t‘000858\t’600276\t‘600016\t’600030\t‘600000\t’600900\t‘600887\t’000002\t‘601328\t’601288\t‘601398\t’000001\t‘600837\t’601601\t‘601688\t’002027\t‘601989\t’601088\t‘600383\t’002230\t‘600570\t’600050\t‘600340\t’600015\t‘002594\t’600588\t‘600547\t’601899\t‘601009\t’000166\t‘601186\t’600958\t‘000338\t’600019\t‘601628\t’601006\t‘002415\t’600048\t‘000725\t’601888\t‘600104\t’600585\t‘600009\t’600031\t‘601211\t’002475\t‘603288\t’601988\t‘000063\t’601766\t‘600028\t’601669\t‘002202\t’002352\t‘600741\t’601225\t‘600406\t’600352\t‘000876\t’601800\t‘000776\t’601336\t‘000538\t’601933\t‘000100\t’600999\t‘002024\t’000568\t‘600309\t’601012\t‘6010939\t’002714\t‘600919\t’601169\t‘002142\t’601818\t‘001979\t’601129\t‘600690\t’002304\t‘000069\t’600660\t‘600010\t’601377\t‘002044\t’600438\t‘000783\t’002736\t‘600196\t’601901\t‘600886\t’002241\t‘600795"
            .replace("\t",",")
            .replace("‘","")
            .replace("’","");
    private final static String SZ_CODE = "000000"; // 上涨代码
    private final static int MIN_COUNT = 5;

    private Map<String,Integer> codeCountMap = new HashMap<>();

    @Autowired
    private HistoryDataDao historyDataDao;
    @Autowired
    private ZFCodeDao zfCodeDao;


    /**
     * 分析股票的涨停信息
     */
    public void analysisChanged() throws ParseException {
        log.info("代码为:" + commonStockCode);
        List<String> commonCodeList = Arrays.asList(commonStockCode.split(","));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse("2019-01-01");
        List<HistoryDataT> stockList = historyDataDao.findByStockDateAfter(startDate)
                    .stream().filter(historyData-> StringUtils.indexOfAny(historyData.getStockName(),new String[]{"ST","银行"}) == -1)
                    .collect(Collectors.toList());
        log.info("数量1为:" + stockList.size());

        /*List<HistoryDataT> filterHd = new ArrayList<>();
        for (HistoryDataT hdt : stockList){
            if (hdt.getStockDate().after(startDate))
                filterHd.add(hdt);
        }*/
        //log.info("共获取到股票历史数据:"+filterHd.size()+"个，股票:"+filterHd.stream().map(HistoryDataT::getStockCode).collect(Collectors.toSet()).size()+"个");

        List<HistoryDataT> ztList = new ArrayList<>();
        List<HistoryDataT> dtList = new ArrayList<>();
        for (HistoryDataT hdt : stockList){
            String rateChange = hdt.getRangeRate().substring(0,hdt.getRangeRate().length() - 1);
            Double doubleRateRange = Double.parseDouble(rateChange);
            if (doubleRateRange > 9 && doubleRateRange < 11 && commonCodeList.contains(hdt.getStockCode()))
                ztList.add(hdt);
            else if (doubleRateRange < - 9 && doubleRateRange > -11 && commonCodeList.contains(hdt.getStockCode()))
                dtList.add(hdt);
        }
        handleZTList(dtList);
        //handleDTList(dtList);
    }

    public void handleZTList(List<HistoryDataT> ztList){
        log.info("涨停的数量为:" + ztList.size()+"，涨停的股票为:"+ztList.stream().map(HistoryDataT::getStockCode).collect(Collectors.toSet()).size() + "个");
        Map<String,List<HistoryDataT>> groupByCode = ztList.stream().collect(Collectors.groupingBy(HistoryDataT::getStockCode));

        for (String code : groupByCode.keySet()){
            int count = groupByCode.get(code).size();
            if (count >= MIN_COUNT)
                log.info("代码:" + code + "的涨停数量为:" + count + "次");
        }
    }

    public void handleDTList(List<HistoryDataT> dtList){
        log.info("跌停的数量为:" + dtList+"，跌停的股票为:"+dtList.stream().map(HistoryDataT::getStockCode).collect(Collectors.toSet()).size() + "个");
    }

    public void onlyStockAnalysis() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse("2019-01-01");
        //个股分析
        //List<HistoryDataT> szList = historyDataDao.findByStockCodeIn(Arrays.asList(commonStockCode.split(",")));
        List<HistoryDataT> szList = historyDataDao.findByStockDateGreaterThanAndStockCodeIn(fromDate,Arrays.asList(commonStockCode.split(",")));
        log.info("获取到历史数据的数量为:"+szList.size()+"条,股票:"+szList.stream().map(HistoryDataT::getStockCode).collect(Collectors.toSet()).size() + "个");

        szList = szList.stream().filter(historyData-> StringUtils.indexOfAny(historyData.getStockName(),new String[]{"ST","银行"}) == -1)
                .collect(Collectors.toList());

        Map<String,List<HistoryDataT>> groupByCode = szList.parallelStream().collect(Collectors.groupingBy(HistoryDataT::getStockCode));
        Set<String> caledStockCodeList = new HashSet<>();
        for (String code : groupByCode.keySet()){
            log.info("计算股票:" + code + "所对应的信息");
            caledStockCodeList.add(code);
            List<HistoryDataT> codeList = groupByCode.get(code);
            // 时间与增长率的关系
            Map<Date,Double> dateRateChagne = getRateChange(codeList);
            for (String sCode : groupByCode.keySet()){
                if (sCode.equals(code) || caledStockCodeList.contains(sCode)) continue;
                Map<Date,Double> otherRateChange = getRateChange(groupByCode.get(sCode));
                compareRateChange(dateRateChagne,otherRateChange,code,sCode);
            }
        }
        saveCountMap();
    }

    public void saveCountMap(){
        List<ZFCode> zfCodeList = new ArrayList<>();
        for (String countCode : codeCountMap.keySet()){
            log.info("代码:"+countCode+"对应的数量为:"+codeCountMap.get(countCode));
            String[] ZFCodes = countCode.split(":");
            ZFCode zfCode = new ZFCode(ZFCodes[0],ZFCodes[1],ZFCodes[2].equals("Z")? SYType.P:SYType.N,
                    codeCountMap.get(countCode));
            zfCodeList.add(zfCode);
            if (zfCodeList.size() >= 400){
                zfCodeDao.saveAll(zfCodeList);
                zfCodeList = new ArrayList<>();
            }

        }
        zfCodeDao.saveAll(zfCodeList);
    }

    public void compareRateChange(Map<Date,Double> rate1,Map<Date,Double> rate2,String code1,String code2){
        codeCountMap.put(code1+":"+code2+":Z",0);
        codeCountMap.put(code1+":"+code2+":F",0);
        for (Date date : rate1.keySet()){
            if (!rate2.containsKey(date)) continue;
            Double plusRate = rate1.get(date) + rate2.get(date);
            if (plusRate <= 8){
                Integer count = codeCountMap.get(code1+":"+code2+":F");
                codeCountMap.put(code1+":"+code2+":F",count + 1);
            }else {
                Integer count = codeCountMap.get(code1+":"+code2+":Z");
                codeCountMap.put(code1+":"+code2+":Z",count + 1);
            }
        }

    }

    public Map<Date,Double> getRateChange(List<HistoryDataT> historyDataTS){
        Map<Date,Double> dateRateChagne = new HashMap<>();
        for (HistoryDataT historyDataT : historyDataTS){
            String rateC = historyDataT.getRangeRate();
            String fianl = rateC.substring(0,rateC.length() - 1);
            Double rateChagen = Double.parseDouble(fianl);
            dateRateChagne.put(historyDataT.getStockDate(),rateChagen);
        }
        return dateRateChagne;
    }
}
