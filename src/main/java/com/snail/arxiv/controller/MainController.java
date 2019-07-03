package com.snail.arxiv.controller;

import com.snail.arxiv.entity.PaperRecord;
import com.snail.arxiv.mapper.PaperRecordMapper;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.*;

@Controller
public class MainController {

    @Autowired
    String rawAtomXmlData;

    @Autowired
    PaperRecordMapper paperRecordMapper;

    @Autowired
    Map<String, String []> paperId2WordArray;

    @Autowired
    Map<String, Set<String>> word2IndexDictionary;

    private int maxOutputRecordsNum = 20; // 指定主页一次性最大可以输出的记录数

    public void generatePaperId2WordArray(Map<String, String []> paperMap){

        if (paperMap.size() != 0) { //若论文id-词组映射不为空，则清空该映射
            paperMap.clear();
        }

        // 得到所有论文记录，重新生成论文id-词组映射
        List<PaperRecord> allRecords = paperRecordMapper.getAllPaperRecords();
        for (Iterator it = allRecords.iterator(); it.hasNext();){
            PaperRecord record = (PaperRecord) it.next();
            String coreWords = record.getTitle() + " " + record.getSummary(); //核心词从论文标题和摘要中获得
            coreWords = coreWords.replaceAll("\\pP", ""); //去除所有的标点符号
            coreWords = coreWords.toLowerCase(); //转换为小写
            String [] temparr = coreWords.split("\\s+"); //按空格切分单词,返回一个字符串数组
            paperMap.put(record.getId(), temparr); //若key已存在则覆盖原来的value
        }
    }

    public void generateWord2IndexDictionary(Map<String, Set<String>> wordDictionary){

        if (wordDictionary.size() != 0){ //若词典-id索引映射不为空，则清空该映射
            wordDictionary.clear();
        }

        // 重新生成词典-id索引映射
        for (Iterator it = paperId2WordArray.keySet().iterator(); it.hasNext();){
            String id = (String) it.next(); //得到论文记录的id
            String [] words = paperId2WordArray.get(id); //得到论文记录下所有的单词
            for (String word:words){
                Set<String> tempSet = null;
                if (wordDictionary.containsKey(word)){ //字典中包含有这个词
                    tempSet = wordDictionary.get(word);
                    tempSet.add(id);
                } else{
                    tempSet = new HashSet<String>();
                    tempSet.add(id);
                }
                wordDictionary.put(word, tempSet);
            }
        }
    }

    private String getIdOfMaxTFIDFValue(Map<String, Double> paperTFIDF){
        // 得到拥有最大TF-IDF值的id
        String fitMaxValueKeyId = "";
        double maxValue = 0.0;
        for (Iterator it = paperTFIDF.keySet().iterator(); it.hasNext();){
            String key = (String) it.next();
            double value = paperTFIDF.get(key);
            if (value > maxValue){
                maxValue = value;
                fitMaxValueKeyId = key;
            }
        }

        return fitMaxValueKeyId;
    }

    @ResponseBody
    @GetMapping("/test")
    public String testContainer(){
        return "Hello, World!";
    }

    @GetMapping("/")
    public String homePage(Map<String, Object> map) {
        // 从数据库中取出最新的20条论文记录反馈给主页
        List<PaperRecord> allRecords = paperRecordMapper.getAllPaperRecords();
        int numOfRecord = allRecords.size();
        if (numOfRecord >= maxOutputRecordsNum)
            allRecords = allRecords.subList(0, maxOutputRecordsNum);
        map.put("records", allRecords);

        return "index";
    }

    @PostMapping("/search")
    public String search(@RequestParam("keywords") String keywords,
                         Map<String, Object> map){
        keywords = keywords.replaceAll("\\pP", ""); //去掉标点符号
        keywords = keywords.toLowerCase(); //转换为小写
        String [] keyWordsArray = keywords.split("\\s+"); //按空格分割单词

        int numOfAllPaper = paperId2WordArray.size(); //所有的论文数目

        Map<String, Double> IDF = new HashMap<String, Double>(); //IDF:逆文本频率指数
        Map<String, Double> paperTFIDF = new HashMap<String, Double>(); //保存每篇论文的TF-IDF值

        //获取所有关键字的id索引的并集，计算IDF
        Set unionSet = new HashSet<String>();
        for (String keyword:keyWordsArray){
            unionSet.addAll(word2IndexDictionary.get(keyword));
            IDF.put(keyword, Math.log((double)numOfAllPaper)/(double)word2IndexDictionary.get(keyword).size());
        }

        Map<String, Double> TF = new HashMap<String, Double>(); //TF:关键词频率
        for (Iterator it = unionSet.iterator(); it.hasNext();){
            String fitId = (String) it.next();
            String [] fieldWords = paperId2WordArray.get(fitId);
            TF.clear(); //计算每个论文的TF值前，先清空已有的TF项
            for (String keyword:keyWordsArray){
                int keywordCount = 0;
                for (String word:fieldWords){
                    if (keyword.equals(word)) keywordCount++;
                }
                TF.put(keyword, (double)keywordCount/(double)fieldWords.length);
            }

            // 计算每个论文的TF-IDF值
            double tfidf = 0.0;
            for (String keyword:keyWordsArray)
                tfidf += TF.get(keyword) * IDF.get(keyword);
            paperTFIDF.put(fitId, tfidf);
        }

        // 每次获取最大TF-IDF值对应的id，按id获取对应的论文记录，放在一个List中返回给index页面
        List<PaperRecord> nominateRecords = new ArrayList<PaperRecord>();
        // 查询得到的并集的论文记录数超过限制输出数目，则只输出限制数目的记录给页面
        int numOfLoop = unionSet.size() > maxOutputRecordsNum ? maxOutputRecordsNum : unionSet.size();
        for (int i = 0; i < numOfLoop; i++){
            String fitKeyId = getIdOfMaxTFIDFValue(paperTFIDF);
            nominateRecords.add(paperRecordMapper.getPaperRecordById(fitKeyId));
            paperTFIDF.remove(fitKeyId); //去除这个key
        }

        map.put("records", nominateRecords);

        return "index";
    }

    @GetMapping("/rebuild")
    public String rebuild(Map<String, Object> map) throws DocumentException {

        Document document = DocumentHelper.parseText(rawAtomXmlData);
        Element root = document.getRootElement(); // 得到根元素

        // 遍历每个entry
        int updateCount = 0;
        int insertCount = 0;
        for (Iterator en = root.elementIterator("entry"); en.hasNext();){
            PaperRecord record = new PaperRecord(); // 封装一条论文记录,一个entry对应一条论文记录
            Element entry = (Element) en.next();
            String authors = "";
            String category = "";
            int aucount = 1;
            int categoryCount = 1;
            for (Iterator it = entry.elementIterator(); it.hasNext();){
                Element element = (Element) it.next();

                if(element.getName().equals("id")){
                    record.setId(element.getText());
                    //System.out.println(element.getName() + ":" + element.getText());
                }

                if(element.getName().equals("published")){
                    String stringtime = element.getText();
                    stringtime = stringtime.replace("T", " ").replace("Z", " ").trim();
                    Timestamp sqltime = Timestamp.valueOf(stringtime);
                    record.setTime(sqltime);
                    //System.out.println(element.getName() + ":" + stringtime);
                }

                if(element.getName().equals("title")){
                    record.setTitle(element.getText());
                    //System.out.println(element.getName() + ":" + element.getText());
                }

                if(element.getName().equals("summary")){
                    //String summary = element.getText().replace("\n", " ");
                    //record.setSummary(summary);
                    record.setSummary(element.getText());
                    //System.out.println(element.getName() + ":" + element.getText());
                }

                if(element.getName().equals("author")){
                    for (Iterator au = element.elementIterator(); au.hasNext();){
                        Element name = (Element) au.next();
                        if (aucount == 1)
                            authors = name.getText();
                        else
                            authors = authors + "," + name.getText();
                    }
                    authors = authors.trim();
                    aucount++;
                }

                if(element.getName().equals("link") && element.attribute(0).getName().equals("title")){
                    Attribute href = element.attribute("href");
                    record.setLink(href.getValue());
                    //System.out.println("link: " + href.getValue());
                }

                if(element.getName().equals("category")){
                    Attribute term = element.attribute("term");
                    if (categoryCount == 1)
                        category = term.getValue();
                    else
                        category = category + "," + term.getValue();
                    category = category.trim();
                    categoryCount++;
                }
            }
            record.setAuthors(authors);
            record.setCategory(category);
            if (paperRecordMapper.verifyRecordIsExist(record.getId())){
                // 论文记录已存在,则更新这条记录
                paperRecordMapper.updateOnePaperRecord(record);
                updateCount++;
            } else{
                // 论文记录还未写入数据库,则插入这条记录
                paperRecordMapper.insertOnePaperRecord(record);
                insertCount++;
            }
            //System.out.println("authors: " + authors);
        }
        map.put("rebuildMsg", "Updated " + updateCount + " records. Inserted " + insertCount + " records.");
        System.out.println("Updated " + updateCount + " records. Inserted " + insertCount + " records.");

        return "admin";
    }

    @GetMapping("/purge")
    public String purge(Map<String, Object> map) {
        int linenum = paperRecordMapper.purgeAllRecords();
        map.put("purgeMsg", "Success delete " + linenum + " paper records.");
        System.out.println("Success delete " + linenum + " paper records");

        return "admin";
    }

    @GetMapping("/reindex")
    public String reindex(Map<String, Object> map) {
        // 重新生成论文id-单词数组映射Map
        generatePaperId2WordArray(paperId2WordArray);

        // 重新生成单词-id索引字典
        generateWord2IndexDictionary(word2IndexDictionary);

        map.put("reindexMsg", "Success generate " + word2IndexDictionary.size() + " word's id index");

        return "admin";
    }
}
