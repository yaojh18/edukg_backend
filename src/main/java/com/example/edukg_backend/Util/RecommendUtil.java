package com.example.edukg_backend.Util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecommendUtil {
    private Map<Long, List<Map<String, Object>>> recommend_table;
    public RecommendUtil(){
        recommend_table = new ConcurrentHashMap<>();
    }
    public List<Map<String, Object>> getRecommendTable(Long key){
        recommend_table.computeIfAbsent(key, k -> new Vector<>());
        return recommend_table.get(key);
    }
    public void writeToRecommendTable(Long key, List<Map<String, Object>> questionList){
        recommend_table.computeIfAbsent(key, k -> new Vector<>());
        recommend_table.get(key).addAll(questionList);
    }
    public void writeToRecommendTable(Long key, Map<String, Object> question){
        recommend_table.computeIfAbsent(key, k -> new Vector<>());
        recommend_table.get(key).add(question);
    }
    public void writeToRecommendTable(Long key, String qBody, String qAnswer){
        recommend_table.computeIfAbsent(key, k -> new Vector<>());
        Map<String, Object> temp = new ConcurrentHashMap<>();
        temp.put("qBody", qBody);
        temp.put("qAnswer", qAnswer);
        recommend_table.get(key).add(temp);
        System.out.println("try add" + qBody);
    }
}
