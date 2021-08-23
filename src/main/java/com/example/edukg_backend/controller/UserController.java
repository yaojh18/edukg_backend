package com.example.edukg_backend.controller;

import com.example.edukg_backend.Service.UserService;
import com.example.edukg_backend.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @ResponseBody
    @RequestMapping(value = "user/login", method = RequestMethod.POST)
    public Map<String, Object> login(@RequestParam("userName") String userName, @RequestParam("password") String password){
        return userService.login(userName, password);
    }

    @ResponseBody
    @RequestMapping(value = "user/register", method = RequestMethod.POST)
    public Map<String, Object> register(@RequestParam("userName") String userName, @RequestParam("password") String password){
        return userService.register(userName, password);
    }

    @ResponseBody
    @RequestMapping(value = "user/changePassword", method = RequestMethod.POST)
    public Map<String, Object>changePassword(
        @RequestParam("token") String token,
        @RequestParam("oldPassword") String oldPassword,
        @RequestParam("newPassword") String newPassword
    ){
        return userService.changePassword(token, oldPassword, newPassword);
    }

}
