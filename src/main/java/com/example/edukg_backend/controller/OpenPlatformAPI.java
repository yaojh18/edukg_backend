package com.example.edukg_backend.controller;


import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;


@Controller
@PropertySource(value="classpath:config/OpenPlatformAPI.properties")
public class OpenPlatformAPI {
    @Value("${site.url}")
    private String siteUrl;
    @Value("${site.userid}")
    private String userId;

    /**
     * 使用post方法获取开放平台的结果，类型限定为x-www-form-urlencoded
     * @param param_map body的内容，key-value
     * @param url api的url
     * @return api的返回结果
     */
    private Map<String, Object> getPostResult(MultiValueMap<String, Object> param_map, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param_map, headers);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> result = restTemplate.postForObject(url, httpEntity, Map.class);
        return result;
    }

    /**
     * 检索实体，获取实体列表
     * method:Get
     * url: localhost:8080/API/instanceList
     * @param searchKey 需要搜索的关键词
     * @param course 搜索的学科，all/chinese/english/math/physics/chemistry/biology/history/geo/politics, default为all
     * @return undefined
     *
     */
    @ResponseBody
    @RequestMapping(value="/API/instanceList", method = RequestMethod.GET)
    public Map<String, Object> instanceList(
            @RequestParam(value="searchKey")String searchKey,
            @RequestParam(value="course", defaultValue = "chinese")String course){
        Map<String, String> param_map = new HashMap<>();
        param_map.put("id", userId);
        param_map.put("searchKey", searchKey);
        param_map.put("course", course);
        // Map<String, Object> result_map = new HashMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> result = restTemplate.getForObject(
                siteUrl + "/instanceList?id={id}&searchKey={searchKey}&course={course}",
                Map.class,
                param_map);
        return result;
    }

    /**
     * 实体链接，识别出一段输入文本中含有的基础教育知识点
     * url：localhost:8080/API/linkInstance
     * method:Get
     * @param context  需要识别的文本
     * @param course  所属的学科
     * @return undefined
     */
    @ResponseBody
    @RequestMapping(value="/API/linkInstance", method = RequestMethod.GET)
    public Map<String, Object> linkInstance(
            @RequestParam(value="context")String context,
            @RequestParam(value="course", required = false)String course){
        MultiValueMap<String, Object> param_map = new LinkedMultiValueMap<>();
        param_map.add("id", userId);
        param_map.add("context", context);
        String url = siteUrl + "/linkInstance";
        if(course != null){
            param_map.add("course", course);
        }
        return getPostResult(param_map, url);
    }


    /**
     * 问答接口
     * method:Get
     * url: localhost:8080/API/inputQuestion
     * @param inputQuestion 提出的问题
     * @param course 所属的学科，非必需，但经过测试对结果影响很大
     * @return undefined
     */
    @ResponseBody
    @RequestMapping(value="/API/inputQuestion", method=RequestMethod.GET)
    public Map<String, Object> inputQuestion(
            @RequestParam(value="inputQuestion") String inputQuestion,
            @RequestParam(value="course", required = false) String course){
        MultiValueMap<String, Object> param_map = new LinkedMultiValueMap<>();
        param_map.add("id", userId);
        param_map.add("inputQuestion", inputQuestion);
        if(course != null){
            param_map.add("course", course);
        }
        String url = siteUrl + "/inputQuestion";
        return getPostResult(param_map, url);
    }

}
