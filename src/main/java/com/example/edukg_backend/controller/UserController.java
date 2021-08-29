package com.example.edukg_backend.controller;

import com.example.edukg_backend.Service.UserService;
import com.example.edukg_backend.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
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
     * status code:200登录成功, 401失败（原因见msg）， 400请求参数有误，500后端出错<br>
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
     * status code:200修改成功, 401失败（原因见msg）， 400请求参数有误，500后端出错<br>
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

    /**
     *
     * <pre>
     * 添加收藏接口
     * method: POST
     * url: localhost:8080/user/addFavorites
     * </pre>
     * @param token 登录时返回的token
     * @param name 添加收藏的实体名称
     * @param course 实体学科
     * @return JSON<br>
     * <pre>
     * status code:200操作成功, 401token相关原因的失败（原因见msg）， 400请求参数有误，500后端出错<br>
     * {
     *     code:200
     *     msg:成功返回"收藏添加成功“，失败返回"用户不存在"或”token为空“
     * }
     * </pre>
     *
     */
    @ResponseBody
    @RequestMapping(value="user/addFavorites",method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addFavorites(
            @RequestParam(value="name") String name,
            @RequestParam(value="course") String course,
            @RequestParam(value="token") String token
    ) {
        return userService.addFavorites(token, name, course);
    }

    /**
     *
     * <pre>
     * 删除收藏接口
     * method: POST
     * url: localhost:8080/user/deleteFavorites
     * </pre>
     * @param token 登录时返回的token
     * @param name 添加收藏的实体名称
     * @param course 实体学科
     * @return JSON<br>
     * <pre>
     * status code:200操作成功, 401token相关原因的失败（原因见msg）， 400请求参数有误，500后端出错<br>
     * {
     *     code:200
     *     msg:成功返回"删除添加成功“，失败返回"用户不存在"或”token为空“
     * }
     * </pre>
     *
     */
    @ResponseBody
    @RequestMapping(value="user/deleteFavorites",method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> deleteFavorites(
            @RequestParam(value="name") String name,
            @RequestParam(value="course") String course,
            @RequestParam(value="token") String token
    ) {
        return userService.deleteFavorites(token, name, course);
    }

    /**
     *
     * <pre>
     * 获取收藏列表
     * method: GET
     * url: localhost:8080/user/getFavoritesList
     * </pre>
     * @param token 登录时返回的token
     * @return JSON<br>
     * <pre>
     * status code:200操作成功, 401token相关原因的失败（原因见msg）， 400请求参数有误，500后端出错<br>
     * {
     *     "code": 200,
     *     "data": [
     *         {
     *             "id": 1, //本地数据库内的实体id，可能没用，不需要的话就不用管
     *             "instanceName": "李白",
     *             "course": "chinese",
     *             "accessCount": 0 //该实体被访问的次数，不需要的话就不用管
     *         }
     *     ]
     * }
     * </pre>
     *
     */
    @ResponseBody
    @RequestMapping(value="user/getFavoritesList", method=RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getFavoritesList(
            @RequestParam(value="token") String token
    ){
        return userService.getFavoritesList(token);
    }

    /**
     *
     * <pre>
     * 获取历史记录
     * method: GET
     * url: localhost:8080/user/getHistoriesList
     * </pre>
     * @param token 登录时返回的token
     * @return JSON<br>
     * <pre>
     * status code:200操作成功, 401token相关原因的失败（原因见msg）， 400请求参数有误，500后端出错<br>
     * {
     *     "code": 200,
     *     "data": [
     *         {
     *             "id": 1, //本地数据库内的实体id，可能没用，不需要的话就不用管
     *             "instanceName": "李白",
     *             "course": "chinese",
     *             "accessCount": 0 //该实体被访问的次数，不需要的话就不用管
     *         }
     *     ]
     * }
     * </pre>
     *
     */
    @ResponseBody
    @RequestMapping(value="user/geHistoriesList", method=RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getHistoriesList(
            @RequestParam(value="token") String token
    ){
        return userService.getHistoriesList(token);
    }
}
