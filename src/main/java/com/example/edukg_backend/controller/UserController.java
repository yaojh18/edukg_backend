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

    /**
     * 登录接口
     * method: POST <br>
     * url: localhost:8080/user/login
     * @param userName 用户名
     * @param password 密码
     * @return JSON
     * {
     *     code:200表示登录成功，401表示登录失败
     *     msg:成功返回"登录成功“，失败返回"用户不存在"或”密码错误“
     *     token:成功返回token，失败为""
     * }
     *
     */
    @ResponseBody
    @RequestMapping(value = "user/login", method = RequestMethod.POST)
    public Map<String, Object> login(@RequestParam("userName") String userName, @RequestParam("password") String password){
        return userService.login(userName, password);
    }

    /**
     * 注册接口
     * method: POST <br>
     * url: localhost:8080/user/register
     * @param userName 用户名
     * @param password 密码
     * @return JSON
     * {
     *     code:200表示注册成功，401表示失败
     *     msg:成功返回"登录成功“，失败返回"用户名已被使用“
     * }
     *
     */
    @ResponseBody
    @RequestMapping(value = "user/register", method = RequestMethod.POST)
    public Map<String, Object> register(@RequestParam("userName") String userName, @RequestParam("password") String password){
        return userService.register(userName, password);
    }

    /**
     * 修改密码接口
     * method: POST <br>
     * url: localhost:8080/user/changePassword
     * @param token 登录时返回的token
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return JSON
     * {
     *     code:200表示修改成功，401表示修改失败
     *     msg:成功返回"修改录成功“，失败返回"用户不存在"或”原密码错误“或”token为空“
     * }
     *
     */
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
