package com.example.edukg_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
@PropertySource(value="classpath:config/OpenPlatformAPI.properties")
public class OpenPlatformAPI {
    @Value("${site.url}")
    private String siteUrl;
    @Value("${site.userid}")
    private String userId;

    @ResponseBody
    @RequestMapping(value="API/instanceSearch", method = RequestMethod.GET)
    public Map<String, Object> instanceSearch(
            @RequestParam(value="searchKey")String searchKey,
            @RequestParam(value="course", defaultValue = "chinese")String course){
        Map<String, String> param_map = new HashMap<String, String>();
        param_map.put("id", userId);
        param_map.put("searchKey", searchKey);
        param_map.put("course", course);
        Map<String, Object> result_map = new HashMap<String, Object>();
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> result = restTemplate.getForObject(
                siteUrl + "/instanceList?id={id}&searchKey={searchKey}&course={course}",
                Map.class,
                param_map);
        return result;
    }
}
