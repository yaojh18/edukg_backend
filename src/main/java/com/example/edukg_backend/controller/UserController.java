package com.example.edukg_backend.controller;

import com.example.edukg_backend.Service.UserService;
import com.example.edukg_backend.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
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
     * <pre>
     * 登录接口
     * method: POST <br>
     * url: localhost:8080/user/login
     * </pre>
     * @param userName 用户名
     * @param password 密码
     * @return JSON<br>
     * status code:200注册成功, 401失败（原因见msg）， 400请求参数有误，500后端出错<br>
     * <pre>
     * {
     *     code:200表示登录成功，401表示登录失败
     *     msg:成功返回"登录成功“，失败返回"用户不存在"或”密码错误“
     *     token:成功返回token，失败为""
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value = "user/login", method = RequestMethod.POST)
    public HttpEntity<Map<String, Object>> login(@RequestParam("userName") String userName, @RequestParam("password") String password){
        return userService.login(userName, password);
    }

    /**
     * <pre>
     * 注册接口
     * method: POST <br>
     * url: localhost:8080/user/register
     * </pre>
     * @param userName 用户名
     * @param password 密码
     * @return JSON<br>
     * status code:200注册成功, 401失败（原因见msg）， 400请求参数有误，500后端出错<br>
     * <pre>
     * {
     *     code:200表示注册成功，401表示失败
     *     msg:成功返回"登录成功“，失败返回"用户名已被使用“
     *     token: 仅登陆成功时有此项
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value = "user/register", method = RequestMethod.POST)
    public HttpEntity<Map<String, Object>> register(@RequestParam("userName") String userName, @RequestParam("password") String password){
        return userService.register(userName, password);
    }

    /**
     * 
     * <pre>
     * 修改密码接口
     * method: POST
     * url: localhost:8080/user/changePassword
     * </pre>
     * @param token 登录时返回的token
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return JSON<br>
     * <pre>
     * status code:200注册成功, 401失败（原因见msg）， 400请求参数有误，500后端出错<br>
     * {
     *     code:200表示修改成功，401表示修改失败
     *     msg:成功返回"修改录成功“，失败返回"用户不存在"或”原密码错误“或”token为空“
     * }
     * </pre>
     *
     */
    @ResponseBody
    @RequestMapping(value = "user/changePassword", method = RequestMethod.POST)
    public HttpEntity<Map<String, Object>> changePassword(
        @RequestParam("token") String token,
        @RequestParam("oldPassword") String oldPassword,
        @RequestParam("newPassword") String newPassword
    ){
        return userService.changePassword(token, oldPassword, newPassword);
    }

}
