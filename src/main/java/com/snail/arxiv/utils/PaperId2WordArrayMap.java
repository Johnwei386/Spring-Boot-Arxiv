package com.snail.arxiv.utils;

import com.snail.arxiv.entity.PaperRecord;
import com.snail.arxiv.mapper.PaperRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Configuration
public class PaperId2WordArrayMap {

    @Autowired
    PaperRecordMapper paperRecordMapper;

    @Bean
    public Map<String, String []> paperId2WordArray(){
        Map<String, String []> paperMap = new HashMap<String, String []>();

        // 得到所有论文记录
        List<PaperRecord> allRecords = paperRecordMapper.getAllPaperRecords();
        for (Iterator it = allRecords.iterator(); it.hasNext();){
            PaperRecord record = (PaperRecord) it.next();
            String coreWords = record.getTitle() + " " + record.getSummary(); //核心词从论文标题和摘要中获得
            coreWords = coreWords.replaceAll("\\pP", ""); //去除所有的标点符号
            coreWords = coreWords.toLowerCase(); //转换为小写
            String [] temparr = coreWords.split("\\s+"); //按空格切分单词,返回一个字符串数组
            paperMap.put(record.getId(), temparr);
        }

        return paperMap;
    }
}
