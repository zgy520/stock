package com.zgy.project.stockServer.core.service.comment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zgy.project.stockServer.core.dao.StockCommentDao;
import com.zgy.project.stockServer.core.model.comment.StockComment;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockCommentService {
    @Autowired
    private StockCommentDao stockCommentDao;

    public void getStockComments() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (int i = 0; i <= 16600; i++){
            log.info("开始第：" + i + "页的抓取");
            String url = "http://guba.eastmoney.com/default,0_"+i+".html";

            Document document = Jsoup.connect(url).get();
            Element commentDiv = document.select("div.balist").get(0);
            Elements liList = commentDiv.select("ul.newlist").get(0).select("li");
            List<StockComment> stockCommentList = new ArrayList<>();
            log.info("共获取到的元数数量为:" + liList.size() + "个");
            for (Element li : liList){
                StockComment stockComment = new StockComment();
                int readCountI = 0;
                String readCount = li.selectFirst("cite").text(); // 阅读数
                if (readCount.contains("万"))
                    readCountI = Integer.parseInt(readCount.substring(0,readCount.length() - 1)) * 10000;
                else
                    readCountI = Integer.parseInt(readCount);
                String commentCount = li.select("cite").get(1).text(); // 评论数
                int commentCountI = Integer.parseInt(commentCount);
                Element titleElement = li.select("span.sub").select("a").get(1);
                String stockCode = titleElement.attr("href").substring(6,12);
                String title = titleElement.attr("title"); // 标题
                String author = li.select("cite.aut").text(); // 作者
                String time = Calendar.getInstance().get(Calendar.YEAR) + "-" + li.select("cite.last").text();
                /*log.info(String.format("阅读数量为%s,评论数为%s,标题为:%s,作者为%s,时间为%s,href为:%s",
                        readCount,commentCount,title,author,time,stockCode));*/
                stockComment.setReadCount(readCountI);
                stockComment.setCommentCount(commentCountI);
                stockComment.setTitle(title);
                stockComment.setStockCode(stockCode);
                stockComment.setAuthor(author);
                stockComment.setLastUpdateTime(sdf.parse(time));
                stockCommentList.add(stockComment);
                if (stockCommentList.size() == 10000){
                    stockCommentDao.saveAll(stockCommentList);
                }else {
                    stockCommentList = new ArrayList<>();
                }
            }


        }

    }

    /**
     * 获取股票评论
     * @param pageable
     * @return
     */
    public JSONObject fetData(Pageable pageable){
        //Pageable pageable = PageRequest.of(1,500);
        JSONObject finalJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Page<StockComment> page = stockCommentDao.findAll(pageable);
        List<StockComment> stockComments = page.getContent();
        stockComments.forEach(stockComment -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",stockComment.getId().toString());
            jsonObject.put("stockCode",stockComment.getStockCode());
            jsonObject.put("comment",stockComment.getTitle());
            jsonArray.add(jsonObject);
        });
        finalJson.put("data",jsonArray);
        finalJson.put("last_page",page.getTotalPages());
        return finalJson;
    }

    public void generateDataSet(String idList){
        Set<Long> ids = Arrays.asList(idList.split(",")).stream()
                    .map(id->Long.parseLong(id.trim())).collect(Collectors.toSet());
        log.info("需要产生数据的数量为:" + ids.size() + "个");
        List<StockComment>stockComments = stockCommentDao.findByIdIn(ids);
        String path = "E:\\stockComment\\0\\";
        for (StockComment stockComment : stockComments){
            try(Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path + stockComment.getId() + ".txt"),"utf-8"))){
                writer.write(stockComment.getTitle());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateWord() throws IOException {
        XWPFDocument document = new XWPFDocument();

        FileOutputStream out = new FileOutputStream(new File("E:\\stockComment\\comment1.docx"));
        List<StockComment> stockComments = stockCommentDao.findAll();
        int i = 0;
        for (StockComment stockComment : stockComments){
            i++;
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(stockComment.getTitle());
            if (i > 10000)
                break;
        }
        document.write(out);
        out.close();

    }
}
