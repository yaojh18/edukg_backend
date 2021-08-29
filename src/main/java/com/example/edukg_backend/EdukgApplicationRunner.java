package com.example.edukg_backend;

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

import java.util.Map;

@Component
@PropertySource(value="classpath:config/EdukgApplicationRunner.properties")
public class EdukgApplicationRunner implements ApplicationRunner {
    @Autowired
    OpenPlatformAPI openPlatformAPI;
    @Value("${phone}")
    private String phone;
    @Value("${password}")
    private String password;

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
        openPlatformAPI.setUserId(result.get("id"));
    }
}
