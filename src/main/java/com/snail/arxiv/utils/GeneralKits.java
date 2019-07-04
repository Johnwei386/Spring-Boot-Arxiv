package com.snail.arxiv.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
