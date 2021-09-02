package com.example.edukg_backend.controller;

import com.example.edukg_backend.Service.UserService;
import com.example.edukg_backend.Util.JwtUtil;
import com.example.edukg_backend.Util.UserInformationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    UserInformationUtil userInformationUtil;

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
     *             "category": "人物",
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
     *             "category": "人物",
     *             "accessCount": 0 //该实体被访问的次数，不需要的话就不用管
     *         }
     *     ]
     * }
     * </pre>
     *
     */
    @ResponseBody
    @RequestMapping(value="user/getHistoriesList", method=RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getHistoriesList(
            @RequestParam(value="token") String token
    ){
        return userService.getHistoriesList(token);
    }

    /**
     *
     * <pre>
     * 获取推荐试题
     * 该接口的机制如下：当用户未访问或收藏过任何实体时，返回默认推荐结果；访问和收藏实体总数小于5个时，返回用户个性推荐和默认推荐的结合；访问和收藏总数超过5个时，返回的试题全部为个性推荐
     * method: GET
     * url: localhost:8080/user/recommendQuestion
     * </pre>
     * @param token 登录时返回的token
     * @return JSON<br>
     * <pre>
     * status code:200操作成功, 401token相关原因的失败（原因见msg）， 400请求参数有误，500后端出错<br>
     *{
     *     "code": 200,
     *     "data": [
     *         {
     *             "qAnswer": "C",
     *             "qBody": "岳飞是著名的抗金英雄,却被奸臣秦桧等诬告而惨遭杀害。小明深有感触地说:岳飞比窦娥还冤!窦娥这一角色出自()",
     *             "A": "唐诗",
     *             "B": "宋词",
     *             "C": "元曲",
     *             "D": "明清小说"
     *         },
     *         ...
     *     ]
     * }
     * </pre>
     *
     */
    @ResponseBody
    @RequestMapping(value = "user/recommendQuestion", method=RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> recommendQuestion(
            @RequestParam(value="token") String token
    ){
        Map<String, Object> recommend_entity = userService.recommendEntity(token);
        if(recommend_entity.get("code").equals(400)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(recommend_entity);
        }
        Random rand = new Random();
        List<Map<String, Object>> recommend_entity_list = (List<Map<String, Object>>)recommend_entity.get("data");
        List<Map<String, Object>> recommend_question_list = new ArrayList<>();
        Set<String> questionBodySet = new HashSet<>();
        for(Map<String, Object> element: recommend_entity_list){
            if(recommend_question_list.size() >= 3)
                break;
            getRandomQuestionByEntity(element, rand, recommend_question_list, questionBodySet);
            if(element.get("needMore").equals("true")){
                Map<String, Object> new_element = getRelatedInstance(element, rand);
                if((boolean) new_element.get("success")){
                    getRandomQuestionByEntity(new_element, rand, recommend_question_list, questionBodySet);
                }
            }

        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", recommend_question_list);


        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private void getRandomQuestionByEntity(Map<String, Object> element,
                                           Random rand,
                                           List<Map<String, Object>> recommend_question_list,
                                           Set<String> questionBodySet){
        Map<String, String> param_map = new HashMap<>();
        param_map.put("id", userInformationUtil.getUserId());
        Pattern p = Pattern.compile("(.*)A[.．](.*)B[.．](.*)C[.．](.*)D[.．](.*)");
        param_map.put("name", (String)element.get("name"));
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object>questionResult = restTemplate.getForObject(
                "http://open.edukg.cn/opedukg/api/typeOpen/open" + "/questionListByUriName?id={id}&uriName={name}",
                Map.class,
                param_map);
        List<Map<String, Object>> questionList = (List<Map<String, Object>>) questionResult.get("data");
        if(questionList == null || questionList.isEmpty())
            return;

        Map<String, Object> question = questionList.get(rand.nextInt(questionList.size()));
        String questionBody = (String)question.get("qBody");
        Matcher m = p.matcher(questionBody);
        //重复问题
        if(questionBodySet.contains(questionBody))
            return;

        if(m.find()) {
            questionBodySet.add(questionBody);
            question.put("qBody", m.group(1));
            question.put("A", m.group(2));
            question.put("B", m.group(3));
            question.put("C", m.group(4));
            question.put("D", m.group(5));
            question.remove("id");
            recommend_question_list.add(question);
        }

    }

    private Map<String, Object>getRelatedInstance(Map<String, Object> element, Random rand){
        Map<String, Object> result = new HashMap<>();
        Map<String, String> param_map = new HashMap<>();
        param_map.put("id", userInformationUtil.getUserId());
        param_map.put("name", (String)element.get("name"));
        param_map.put("course", (String)element.get("course"));
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object>second_level_instance = restTemplate.getForObject(
                "http://open.edukg.cn/opedukg/api/typeOpen/open" + "/infoByInstanceName?id={id}&name={name}&course={course}",
                Map.class,
                param_map);
        List<Map<String, Object>> second_level_instance_list = new ArrayList<>();
        try {
            second_level_instance_list = (List<Map<String, Object>>) (((Map<String, Object>) second_level_instance.get("data")).get("content"));
        }
        catch(Exception e){
            System.out.println(second_level_instance);
        }
        if(second_level_instance_list.isEmpty()){
            result.put("success", false);
            return result;
        }


        Map<String, Object> e= second_level_instance_list.get(rand.nextInt(second_level_instance_list.size()));

        if(e.containsKey("object_label")){
            result.put("name", (String) e.get("object_label"));
        }
        else if(e.containsKey("subject_label")){
            result.put("name", (String) e.get("subject_label"));
        }
        else {
            result.put("success", false);
            return result;
        }
        result.put("course", element.get("course"));
        result.put("needMore", "false");
        result.put("success", true);
        return result;
    }
}
