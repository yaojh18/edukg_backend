package com.example.edukg_backend.Util;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

//存储与开放平台交互的用户信息
@Component
@PropertySource(value="classpath:config/OpenPlatformAPI.properties")
public class UserInformationUtil {
    private String userId;

    public void setUserId(String s){
        userId = s;
    }

    public String getUserId(){
        return userId;
    }
}
