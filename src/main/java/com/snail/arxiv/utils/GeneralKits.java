package com.snail.arxiv.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.*;

@Configuration
public class GeneralKits {

    @Autowired
    Map<String, String []> paperId2WordArray;

    @Bean
    public Map<String, Set<String>> word2IndexDictionary(){
        Map<String, Set<String>> wordDictionary = new HashMap<String, Set<String>>();
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

        return wordDictionary;
    }

    @Bean
    @Lazy
    public String rawAtomXmlData() throws IOException {
        String rawData = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String maxResults = "10"; //指定从Arxiv服务器上一次性获取多少条记录
        // 从Arxiv服务器上获取maxResults条最新的记录
        String url = "http://export.arxiv.org/api/query?search_query=cat:cs.AI&sortBy=lastUpdatedDate&start=0&max_results=" + maxResults;
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            //System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            rawData = EntityUtils.toString(entity, "UTF-8");
            //System.out.println(EntityUtils.toString(entity, "UTF-8"));
        } finally {
            response.close();
        }

        return rawData;
    }
}
