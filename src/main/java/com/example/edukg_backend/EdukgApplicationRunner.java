package com.example.edukg_backend;

import com.example.edukg_backend.ConfigHelper.DefaultEntityConfig;
import com.example.edukg_backend.Service.UserService;
import com.example.edukg_backend.Util.UserInformationUtil;
import com.example.edukg_backend.controller.OpenPlatformAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@PropertySource(value="classpath:config/EdukgApplicationRunner.properties")
public class EdukgApplicationRunner implements ApplicationRunner {
    @Autowired
    UserInformationUtil userInformationUtil;
    @Autowired
    UserService userService;
    @Value("${phone}")
    private String phone;
    @Value("${password}")
    private String password;
    @Autowired
    DefaultEntityConfig defaultEntityConfig;

    @Override
    public void run(ApplicationArguments args) throws  Exception{
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> param_map = new LinkedMultiValueMap<>();
        param_map.add("phone", phone);
        param_map.add("password", password);
        String url = "http://open.edukg.cn/opedukg/api/typeAuth/user/login";
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param_map, headers);
        Map<String, String> result = new RestTemplate().postForObject(url, httpEntity, Map.class);
        userInformationUtil.setUserId(result.get("id"));
        if(userService.getDefaultQuestion().isEmpty()){
            List<Map<String, Object>> defaultEntity = defaultEntityConfig.getDefaultEntity();
            for(Map<String, Object> element: defaultEntity){
                Map<String, String> param_map2 = new HashMap<>();
                param_map2.put("id", userInformationUtil.getUserId());
                param_map2.put("name", (String)element.get("name"));
                RestTemplate restTemplate = new RestTemplate();
                Map<String, Object>questionResult = restTemplate.getForObject(
                        "http://open.edukg.cn/opedukg/api/typeOpen/open" + "/questionListByUriName?id={id}&uriName={name}",
                        Map.class,
                        param_map2);
                List<Map<String, Object>> questionList = (List<Map<String, Object>>) questionResult.get("data");
                for(Map<String, Object> q: questionList){
                    userService.addRecommendQuestion((String)q.get("qBody"), (String)q.get("qAnswer"), (String)element.get("name"), (String)element.get("course"), true);
                    System.out.println("finish adding question " + q.get("qBody") + " for " + element.get("name"));
                }
            }
        }
    }
}
