package com.example.edukg_backend.controller;


import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
@PropertySource(value="classpath:config/OpenPlatformAPI.properties")
public class OpenPlatformAPI {
    @Value("${site.url}")
    private String siteUrl;
    @Value("${site.userid}")
    private String userId;
    @Value("#{${maps}}")
    private Map<String, String> defaultSearchKey;

    /**
     * 使用post方法获取开放平台的结果，body类型限定为x-www-form-urlencoded
     * 后端自己用的
     * @param param_map body的内容，key-value
     * @param url api的url
     * @return api的返回结果
     */
    private Map<String, Object> getPostResult(MultiValueMap<String, Object> param_map, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param_map, headers);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> result = restTemplate.postForObject(url, httpEntity, Map.class);
        return result;
    }

    /**
     * <pre>
     * 获取首页实体列表
     * method: GET
     * url: localhost:8080/API/homeList
     * </pre>
     * @param course 学科
     * @return JSON<br>
     * status code:200成功, 401失败， 400请求参数有误，500后端出错<br>
     * <pre>
     * {
     *   "code": 200表示成功
     *   "data": {
     *     "result": [
     *       {
     *         "label": 实体名称1,
     *         "category": 实体所属类1,
     *         "course": 所属学科
     *       },
     *       {
     *         "label": 搜索到的实体名称2,
     *         "category":搜索到的实体所属类2,
     *         "course":所属学科
     *       },
     *       ...
     *     ],
     *     "result_size": 搜索到的实体数量
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="API/homeList")
    public Map<String, Object> homeList(@RequestParam(value="course", defaultValue="chinese")String course){
        String searchKey = defaultSearchKey.get(course);
        return this.instanceList(searchKey, course);
    }

    /**
     * <pre>
     * 检索实体，获取实体列表
     * method: Get
     * url: localhost:8080/API/instanceList
     * </pre>
     * @param searchKey 需要搜索的关键词
     * @param course 搜索的学科，chinese/english/math/physics/chemistry/biology/history/geo/politics,
     *               default为chinese(之后可能加入all）
     * @return JSON<br>
     * status code:200成功, 400请求参数有误，500后端出错<br>
     * <pre>
     * {
     *   "code": 200表示成功
     *   "data": {
     *     "result": [
     *       {
     *         "label": 搜索到的实体名称1,
     *         "category": 搜索到的实体所属类1,
     *         "course": 所属学科
     *       },
     *       {
     *         "label": 搜索到的实体名称2,
     *         "category":搜索到的实体所属类2,
     *         "course":所属学科
     *       },
     *       ...
     *     ],
     *     "result_size": 搜索到的实体数量
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="/API/instanceList", method = RequestMethod.GET)
    public Map<String, Object> instanceList(
            @RequestParam(value="searchKey")String searchKey,
            @RequestParam(value="course", defaultValue = "chinese")String course){
        //传入参数并发出get请求
            Map<String, String> param_map = new HashMap<>();
            param_map.put("id", userId);
            param_map.put("searchKey", searchKey);
            param_map.put("course", course);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> result = restTemplate.getForObject(
                    siteUrl + "/instanceList?id={id}&searchKey={searchKey}&course={course}",
                    Map.class,
                    param_map);
            //清除结果中的uri
            List<Map<String, Object>> result_data = (List<Map<String, Object>>) result.get("data");
            for (Map<String, Object> element : result_data) {
                element.remove("uri");
                element.put("course", course);
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> response_data = new HashMap<>();
            response_data.put("result", result_data);
            response_data.put("result_size", result_data.size());
            response.put("code", 200);
            response.put("data", response_data);
            return response;

    }

    /**
     *
     * <pre>实体链接，识别出一段输入文本中含有的基础教育知识点
     * method: Get
     * url: localhost:8080/API/linkInstance
     * </pre>
     * @param context  需要识别的文本
     * @param course  所属的学科
     * @return JSON<br>
     * status code:200成功, 401失败， 400请求参数有误，500后端出错<br>
     * <pre>
     * {
     *   "code": 200表示成功,
     *   "data": {
     *     "result": [
     *       {
     *         "entity_type": 实体类型,
     *         "start_index": 该实体在段落中的开始位置,
     *         "end_index": 该实体在段落中的结束位置,
     *         "entity": 实体名称,
     *         "course": 所属学科
     *       },
     *       ...
     *     ],
     *     "result_size": 实体数量
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="/API/linkInstance", method = RequestMethod.GET)
    public Map<String, Object> linkInstance(
            @RequestParam(value="context")String context,
            @RequestParam(value="course", required = false)String course){
        //try {
            MultiValueMap<String, Object> param_map = new LinkedMultiValueMap<>();
            param_map.add("id", userId);
            param_map.add("context", context);
            String url = siteUrl + "/linkInstance";
            if (course != null) {
                param_map.add("course", course);
            }
            Map<String, Object> result = getPostResult(param_map, url);

            List<Map<String, Object>> result_data =
                    (List<Map<String, Object>>) (((Map<String, Object>) result.get("data")).get("results"));
            for (Map<String, Object> element : result_data) {
                element.remove("entity_url");
                element.put("course", course);
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> response_data = new HashMap<>();
            response_data.put("result", result_data);
            response_data.put("result_size", result_data.size());
            response.put("code", 200);
            response.put("data", response_data);
            return response;
        //}
    }


    /**
     * <pre>
     * 问答接口
     * method: Get
     * url: localhost:8080/API/inputQuestion
     * </pre>
     * @param inputQuestion 提出的问题
     * @param course 所属的学科，非必需，但经过测试对结果影响很大
     * @return JSON<br>
     * status code:200成功, 401失败， 400请求参数有误，500后端出错<br>
     * <pre>
     * {
     *   "code": 200表示成功,
     *   "data": {
     *     "result": 有答案返回答案，否则返回"此问题没有找到答案！"
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="/API/inputQuestion", method=RequestMethod.GET)
    public Map<String, Object> inputQuestion(
            @RequestParam(value="inputQuestion") String inputQuestion,
            @RequestParam(value="course", required = false) String course){
        MultiValueMap<String, Object> param_map = new LinkedMultiValueMap<>();
        param_map.add("id", userId);
        param_map.add("inputQuestion", inputQuestion);
        if(course != null){
            param_map.add("course", course);
        }
        String url = siteUrl + "/inputQuestion";
        Map<String, Object> result = getPostResult(param_map, url);

        Map<String, String> result_data = ((List<Map<String, String>>)result.get("data")).get(0);
        String question_answer;
        if(result_data.get("value").isEmpty())
            question_answer = "此问题没有找到答案！";
        else
            question_answer = result_data.get("value");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> response_data = new HashMap<>();
        response_data.put("result", question_answer);
        response.put("code", 200);
        response.put("data", response_data);
        return response;
    }

    /**
     * <pre>
     * 实体详情页接口
     * method: Get
     * url: localhost:8080/API/infoByInstanceName
     * </pre>
     * @param name 要获取详情的实体名称
     * @param course 所属的学科，非必需，但经过测试对结果影响很大
     * @return JSON<br>
     * status code:200成功, 401失败， 400请求参数有误，500后端出错<br>
     * <pre>
     *{
     *   "code": 200,
     *   "data": {
     *     "property": [
     *       {
     *         "predicateLabel": "死亡日期",
     *         "object": "约858"
     *       },
     *       {
     *         "predicateLabel": "字",
     *         "object": "义山"
     *       },
     *       ...
     *     ],
     *     "description": "（约813—约858）　唐代诗人。字义山，号玉溪生，怀州河内（现河南沁阳）人。其诗揭露和批判当时藩镇割据、宦官擅权和上层
     *     统治集团的腐朽糜烂，《行次西郊作一百韵》《有感二首》《重有感》等皆著名；...。",
     *     "relationship": [
     *       {
     *         "predicate_label": "主要作品",
     *         "object_label": "《锦瑟》",
     *         "course": "chinese"
     *       },
     *       {
     *         "subject_label": "无　题",
     *         "predicate_label": "作者",
     *         "course": "chinese"
     *       },
     *       ...
     *     ],
     *     "questionList": [
     *       {
     *         "qAnswer": "B",
     *         "A": "李白",
     *         "B": "杜甫",
     *         "C": "白居易“，
     *         "D": "李商隐",
     *         "qBody": "郭沫若赞誉一位唐代诗人:世上疮痍,诗中圣哲;民间疾苦,笔底波澜。这位被称为圣哲的诗人是()"
     *       },
     *       ...
     *     ]
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="/API/infoByInstanceName", method=RequestMethod.GET)
    public Map<String, Object> infoByInstanceName(
            @RequestParam(value="name") String name,
            @RequestParam(value="course") String course){
        //传入参数并发出get请求
        Map<String, String> param_map = new HashMap<>();
        param_map.put("id", userId);
        param_map.put("name", name);
        param_map.put("course", course);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object>result = restTemplate.getForObject(
                siteUrl + "/infoByInstanceName?id={id}&name={name}&course={course}",
                Map.class,
                param_map);

        Map<String, Object> response_data = new HashMap<>();
        Map<String, Object> result_data = (Map<String, Object>) result.get("data");
        response_data.put("description", "");
        List<Map<String, Object>> property = (List<Map<String, Object>>) result_data.get("property");
        Iterator<Map<String, Object>> it = property.iterator();
        while(it.hasNext()){
            Map<String, Object> element = it.next();
            if(element.get("predicateLabel").equals("内容")) {
                response_data.put("description", element.get("object"));
                it.remove();
            }
            else if(element.get("objectLabel") != null){
                element.put("object", element.get("objectLabel"));
                element.remove("objectLabel");
                element.remove("predicate");
            }
            else if( ((String)element.get("object")).startsWith("http")){
               it.remove();
            }
            else {
                element.remove("predicate");
            }
        }

        List<Map<String, Object>> relationship = (List<Map<String, Object>>) result_data.get("content");
        for(Map<String, Object> element: relationship){
            element.remove("predicate");
            element.remove("object");
            element.remove("subject");
            element.put("course", course);
        }

        response_data.put("property", property);
        response_data.put("relationship", relationship);
        Map<String, Object> response = new HashMap<>();
        response_data.put("questionList", questionList(name, course).get("data"));
        response.put("code", 200);
        response.put("data", response_data);
        return response;
    }


    /**
     * <pre>
     * 实体详情页接口
     * method: Get
     * url: localhost:8080/API/questionList
     * </pre>
     * @param name 要获取试题的实体名称
     * @param course 所属的学科，非必需，但经过测试对结果影响很大
     * @return JSON<br>
     * status code:200成功, 401失败， 400请求参数有误，500后端出错<br>
     * <pre>
     *{
     *     "code": 200,
     *     "data": [
     *       {
     *         "qAnswer": "B",
     *         "A": "李白",
     *         "B": "杜甫",
     *         "C": "白居易“，
     *         "D": "李商隐",
     *         "qBody": "郭沫若赞誉一位唐代诗人:世上疮痍,诗中圣哲;民间疾苦,笔底波澜。这位被称为圣哲的诗人是()"
     *       },
     *       ...
     *     ]
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="/API/questionList", method=RequestMethod.GET)
    public Map<String, Object> questionList(
            @RequestParam(value="name") String name,
            @RequestParam(value="course") String course){
        //传入参数并发出get请求
        Map<String, String> param_map = new HashMap<>();
        param_map.put("id", userId);
        param_map.put("name", name);
        param_map.put("course", course);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object>questionResult = restTemplate.getForObject(
                siteUrl + "/questionListByUriName?id={id}&uriName={name}",
                Map.class,
                param_map);
        List<Map<String, Object>>questionList = (List<Map<String, Object>>) questionResult.get("data");
        Pattern p = Pattern.compile("(.*)A\\.(.*)B\\.(.*)C\\.(.*)D\\.(.*)");

        Iterator<Map<String, Object>> question_iterator = questionList.iterator();
        while(question_iterator.hasNext()){
            Map<String, Object> element = question_iterator.next();
            element.remove("id");
            String questionBody = (String)element.get("qBody");
            Matcher m = p.matcher(questionBody);
            if(m.find()) {
                element.put("qBody", m.group(1));
                element.put("A", m.group(2));
                element.put("B", m.group(3));
                element.put("C", m.group(4));
                element.put("D", m.group(5));
            }
            else{
                question_iterator.remove();
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", questionList);
        return response;
    }
}
