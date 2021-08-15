package com.example.edukg_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ConnectedTestController {
    @ResponseBody
    @RequestMapping("/test_access")
    public String testAccess(){
        return "Access success!";
    }

    /**
     * 用于给安卓端测试GET
     * method:Get
     * url: localhost:8080/test_get
     * @param name 想给的参数
     * @return {
     *     code: 200,
     *     data: ”Get success“ + name
     * }
     */
    @ResponseBody
    @RequestMapping(value = "/test_get", method = RequestMethod.GET)
    public Map<String, Object> testGet(@RequestParam(value = "name", defaultValue = "default name") String name){
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", "Get success " + name);
        return result;
    }
}
