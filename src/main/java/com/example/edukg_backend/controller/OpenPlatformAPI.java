package com.example.edukg_backend.controller;


import com.example.edukg_backend.Models.User;
import com.example.edukg_backend.Service.InstanceService;
import com.example.edukg_backend.Service.UserService;
import com.example.edukg_backend.Util.PinyinUtil;
import com.example.edukg_backend.Util.UserInformationUtil;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import jdk.jfr.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Controller
@PropertySource(value="classpath:config/OpenPlatformAPI.properties")
public class OpenPlatformAPI {
    @Value("${site.url}")
    private String siteUrl;
    // @Value("${site.userid}")
    @Value("#{${maps}}")
    private Map<String, String> defaultSearchKey;
    @Autowired
    private UserService userService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private UserInformationUtil userInformationUtil;

    /**
     * ??????post????????????????????????????????????body???????????????x-www-form-urlencoded
     * ??????????????????
     * @param param_map body????????????key-value
     * @param url api???url
     * @return api???????????????
     */
    private Map<String, Object> getPostResult(MultiValueMap<String, Object> param_map, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param_map, headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(url, httpEntity, Map.class);
    }

    private String getInstanceImage(String name, String course){
        try {
            String url = siteUrl + "/infoByInstanceName?";
            String name_without_space = name.replace(" ", "+");
            RestTemplate restTemplate = new RestTemplate();
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(url)
                    .queryParam("id", userInformationUtil.getUserId())
                    .queryParam("name", URLEncoder.encode(name_without_space, "UTF-8"))
                    .queryParam("course", course);
            URI uri = builder.build(true).toUri();
            Map<String, Object> result = restTemplate.getForObject(uri, Map.class);
            Map<String, Object> result_data = (Map<String, Object>) result.get("data");
            List<Map<String, Object>> property = (List<Map<String, Object>>) result_data.get("property");
            for (Map<String, Object> element : property) {
                if (element.get("predicate").equals("http://edukg.org/knowledge/0.1/property/common#image")) {
                    return (String) element.get("object");
                }
            }
            return "";
        }
        catch (Exception e){
            return "";
        }
    }

    private String getInstanceCategory(String name, String course){
        Map<String, String> param_map = new HashMap<>();
        param_map.put("id", userInformationUtil.getUserId());
        param_map.put("searchKey", name);
        param_map.put("course", course);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> result = restTemplate.getForObject(
                siteUrl + "/instanceList?id={id}&searchKey={searchKey}&course={course}",
                Map.class,
                param_map);
        //??????????????????uri
        try {
            List<Map<String, String>> result_data = (List<Map<String, String>>) result.get("data");
            return result_data.get(0).get("category");
        }
        catch(Exception e){
            return "";
        }
    }

    /**
     * <pre>
     * ????????????????????????
     * method: GET
     * url: localhost:8080/API/homeList
     * </pre>
     * @param course ??????
     * @return JSON<br>
     * status code:200??????, 400?????????????????????500????????????<br>
     * <pre>
     * {
     *   "code": 200????????????
     *   "data": {
     *     "result": [
     *       {
     *         "label": ????????????1,
     *         "category": ???????????????1,
     *         "course": ????????????
     *       },
     *       {
     *         "label": ????????????????????????2,
     *         "category":???????????????????????????2,
     *         "course":????????????
     *       },
     *       ...
     *     ],
     *     "result_size": ????????????????????????
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="API/homeList")
    public ResponseEntity<Map<String, Object>> homeList(@RequestParam(value="course", defaultValue="chinese")String course) throws URISyntaxException, IOException {
        /*
        String searchKey = defaultSearchKey.get(course);
        return this.instanceList(searchKey, course, "default", "default", null);
         */
        //File file = new File(ResourceUtils.getURL("classpath:").getPath() + "/static/" + course + "-partial.csv");
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        //??????csv??????
        InputStream stream = resourceLoader.getResource("classpath:static/" + course + "-partial.csv").getInputStream();  //?????????
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        CsvReader csvReader = new CsvReader();
        Map<String, Object>data = new HashMap<>();
        List<Map<String, Object>>response_list = new ArrayList<>();
        Map<String, Map<String, Object>>uri2LabelAndType = new HashMap<>();
        Map<String, String>classUri2Label = new HashMap<>();
        int total = 0;
        try (CsvParser csvParser = csvReader.parse(reader)) {
            CsvRow row;
            while ((row = csvParser.nextRow()) != null && total < 100000) {
                String s = row.getField(0);
                String o = row.getField(1);
                String p = row.getField(2);
                if(s.startsWith("http://edukb.org/knowledge/0.1/instance/")){
                    if(o.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                        if (uri2LabelAndType.get(s) == null) {
                            total++;
                            Map<String, Object> temp_map = new HashMap<>();
                            temp_map.put("label", p);
                            temp_map.put("course", course);
                            uri2LabelAndType.put(s, temp_map);
                        } else {
                            uri2LabelAndType.get(s).put("label", p);
                        }
                    }
                    else if(o.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !p.endsWith("NamedIndividual")){
                        if (uri2LabelAndType.get(s) == null) {
                            total++;
                            Map<String, Object> temp_map = new HashMap<>();
                            temp_map.put("category", p);
                            temp_map.put("course", course);
                            uri2LabelAndType.put(s, temp_map);
                        } else {
                            uri2LabelAndType.get(s).put("category", p);
                        }
                    }
                }
                else if(s.startsWith("http://edukb.org/knowledge/0.1/class/") && o.equals("http://www.w3.org/2000/01/rdf-schema#label")){
                    classUri2Label.put(row.getField(0), row.getField(2));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        uri2LabelAndType.forEach((key, value) ->{
            if(value.get("category") !=null && classUri2Label.get(value.get("category")) != null){
                value.put("category", classUri2Label.get(value.get("category")));
                response_list.add(value);
            }
        });
        Map<String, Object>temp_map_result = new HashMap<>();
        List<Map<String, Object>> sublist;
        try {
            sublist = response_list.subList(0, 200);
        }
        catch (IndexOutOfBoundsException e){
            sublist = response_list;
        }
        temp_map_result.put("result",sublist);
        temp_map_result.put("result_size", sublist.size());
        data.put("data", temp_map_result);
        data.put("code", 200);
        return new ResponseEntity<>(data, HttpStatus.OK);

    }

    /**
     * <pre>
     * ?????????????????????????????????
     * method: Get
     * url: localhost:8080/API/instanceList
     * </pre>
     * @param searchKey ????????????????????????
     * @param course ??????????????????chinese/english/math/physics/chemistry/biology/history/geo/politics,
     *               default???chinese(??????????????????all???
     * @param sortMethod ???????????????default/pinyin/accessCount ??????????????????/??????/????????????
     * @param token ??????token???????????????????????????????????????????????????????????????????????????
     * @param filterMethod ???????????????default/popular/history/favorite????????????????????????/????????????????????????5/??????????????????/????????????????????????????????????????????????token
     * @return JSON<br>
     * status code:200??????, 400?????????????????????500????????????<br>
     * <pre>
     * {
     *   "code": 200????????????
     *   "data": {
     *     "result": [
     *       {
     *         "label": ????????????????????????1,
     *         "category": ???????????????????????????1,
     *         "course": ????????????,
     *         "img": url???""(???????????????)
     *       },
     *       {
     *         "label": ????????????????????????2,
     *         "category":???????????????????????????2,
     *         "course":????????????,
     *         "img": url???""(???????????????)
     *       },
     *       ...
     *     ],
     *     "result_size": ????????????????????????
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="/API/instanceList", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> instanceList(
            @RequestParam(value="searchKey")String searchKey,
            @RequestParam(value="course", defaultValue = "chinese")String course,
            @RequestParam(value="sortMethod", defaultValue = "default")String sortMethod,
            @RequestParam(value="filterMethod", defaultValue = "default")String filterMethod,
            @RequestParam(value="token", required = false)String token
    ){
        //?????????????????????get??????
            Map<String, String> param_map = new HashMap<>();

            param_map.put("id", userInformationUtil.getUserId());
            param_map.put("searchKey", searchKey);
            param_map.put("course", course);
            //System.out.println(param_map);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> result = restTemplate.getForObject(
                    siteUrl + "/instanceList?id={id}&searchKey={searchKey}&course={course}",
                    Map.class,
                    param_map);
            //System.out.println(result);
            //??????????????????uri
            List<Map<String, Object>> result_data = (List<Map<String, Object>>) result.get("data");

            Map<String, Integer> temp_set = new HashMap<>();
            List<Map<String, Object>> response_data = new ArrayList<>();
            for (Map<String, Object> element : result_data) {
                element.remove("uri");
                element.put("course", course);
                String label = (String)element.get("label");
                if(temp_set.containsKey(label)) {
                    Map<String, Object> temp = response_data.get(temp_set.get(label));
                    temp.put("category", (String)temp.get("category") + " " + (String)element.get("category"));
                }
                else{
                    response_data.add(element);
                    temp_set.put(label, response_data.size() - 1);
                }
            }
            if(filterMethod.equals("popular")){
                response_data = response_data.stream().filter(
                        (Map<String, Object> e) -> instanceService.findOrAddInstance((String) e.get("label"), course).getAccessCount() >= 5

                ).collect(Collectors.toList());
            }
            else if(filterMethod.equals("favorite")){
                Optional<User> userOptional = userService.checkToken(token);
                if(userOptional.isEmpty()){
                    Map<String, Object> err_msg = new HashMap<>();
                    err_msg.put("msg", "?????????");
                    err_msg.put("code", 401);
                    return new ResponseEntity<>(err_msg, HttpStatus.UNAUTHORIZED);
                }
                User user = userOptional.get();
                response_data = response_data.stream().filter(
                        (Map<String, Object> e) -> user.getFavorites().contains(instanceService.findOrAddInstance((String)e.get("label"), course))
                ).collect(Collectors.toList());
            }
            else if(filterMethod.equals("history")){
                Optional<User> userOptional = userService.checkToken(token);
                if(userOptional.isEmpty()){
                    Map<String, Object> err_msg = new HashMap<>();
                    err_msg.put("msg", "?????????");
                    err_msg.put("code", 401);
                    return new ResponseEntity<>(err_msg, HttpStatus.UNAUTHORIZED);
                }
                User user = userOptional.get();
                response_data = response_data.stream().filter(
                        (Map<String, Object> e) -> user.getHistories().contains(instanceService.findOrAddInstance((String)e.get("label"), course))
                ).collect(Collectors.toList());
            }
            if(sortMethod.equals("pinyin")){
                Collections.sort(response_data, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        String pinyin1 = PinyinUtil.getFullSpell((String) o1.get("label"));
                        String pinyin2 = PinyinUtil.getFullSpell((String) o2.get("label"));
                        return pinyin1.compareTo(pinyin2);
                    }
                });
            }
            else if(sortMethod.equals("accessCount")){
                Collections.sort(response_data, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Integer count1 = instanceService.findOrAddInstance((String)o1.get("label"), course).getAccessCount();
                        Integer count2 = instanceService.findOrAddInstance((String)o2.get("label"), course).getAccessCount();
                        return count2.compareTo(count1);
                    }
                });
            }

            if(response_data.size()<=13){
                for (Map<String, Object>element: response_data){
                    element.put("img", getInstanceImage((String) element.get("label"), course));
                }
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> response_data_and_size = new HashMap<>();
            response_data_and_size.put("result", response_data);
            response_data_and_size.put("result_size", response_data.size());
            response.put("code", 200);
            response.put("data", response_data_and_size);
            return new ResponseEntity<>(response, HttpStatus.OK);

    }

    /**
     *
     * <pre>???????????????????????????????????????????????????????????????????????????
     * method: Get
     * url: localhost:8080/API/linkInstance
     * </pre>
     * @param context  ?????????????????????
     * @param course  ???????????????
     * @return JSON<br>
     * status code:200??????, 400?????????????????????500????????????<br>
     * <pre>
     * {
     *   "code": 200????????????,
     *   "data": {
     *     "result": [
     *       {
     *         "entity_type": ????????????,
     *         "start_index": ????????????????????????????????????,
     *         "end_index": ????????????????????????????????????,
     *         "entity": ????????????,
     *         "course": ????????????
     *       },
     *       ...
     *     ],
     *     "result_size": ????????????
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
            param_map.add("id", userInformationUtil.getUserId());
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
     * ????????????
     * method: Get
     * url: localhost:8080/API/inputQuestion
     * </pre>
     * @param inputQuestion ???????????????
     * @param course ??????????????????????????????????????????????????????????????????
     * @return JSON<br>
     * status code:200??????, 400?????????????????????500????????????<br>
     * <pre>
     * {
     *   "code": 200????????????,
     *   "data": {
     *     "result": ????????????????????????????????????"??????????????????????????????"
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
        param_map.add("id", userInformationUtil.getUserId());
        param_map.add("inputQuestion", inputQuestion);
        if(course != null){
            param_map.add("course", course);
        }
        String url = siteUrl + "/inputQuestion";
        Map<String, Object> result = getPostResult(param_map, url);

        Map<String, String> result_data = ((List<Map<String, String>>)result.get("data")).get(0);
        String question_answer;
        if(result_data.get("value").isEmpty())
            question_answer = "??????????????????????????????";
        else
            question_answer = result_data.get("value");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> response_data = new HashMap<>();
        response_data.put("result", question_answer);
        response.put("code", 200);
        response.put("data", response_data);
        System.out.println(response);
        return response;
    }

    /**
     * <pre>
     * ?????????????????????
     * method: Get
     * url: localhost:8080/API/infoByInstanceName
     * </pre>
     * @param name ??????????????????????????????
     * @param course ???????????????
     * @param token ??????token(????????????
     * @return JSON<br>
     * status code:200??????, 401????????? 400?????????????????????500????????????<br>
     * <pre>
     *{
     *   "code": 200,
     *   "data": {
     *     "property": [
     *       {
     *         "predicateLabel": "????????????",
     *         "object": "???858"
     *       },
     *       {
     *         "predicateLabel": "???",
     *         "object": "??????"
     *       },
     *       ...
     *     ],
     *     "description": "??????813??????858??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *     ????????????????????????????????????????????????????????????????????????????????????????????????????????????...???",
     *     "img": "http://kb.cs.tsinghua.edu.cn/apihtml/getpng/495/CS090002T00620010201",
     *     "relationship": [
     *       {
     *         "predicate_label": "????????????",
     *         "object_label": "????????????",
     *         "course": "chinese"
     *       },
     *       {
     *         "subject_label": "?????????",
     *         "predicate_label": "??????",
     *         "course": "chinese"
     *       },
     *       ...
     *     ],
     *     "isFavorite": true/false,
     *     "hasQuestion": true/false
     *   }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value="/API/infoByInstanceName", method=RequestMethod.GET)
    public Map<String, Object> infoByInstanceName(
            @RequestParam(value="name") String name,
            @RequestParam(value="course") String course,
            @RequestParam(value="token", required = false) String token) throws URISyntaxException, UnsupportedEncodingException, InterruptedException {
        //?????????????????????get??????

        String url = siteUrl + "/infoByInstanceName?";
        String name_without_space = name.replace(" ", "+");
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(url)
                .queryParam("id", userInformationUtil.getUserId())
                .queryParam("name", URLEncoder.encode(name_without_space, "UTF-8"))
                .queryParam("course", course);
        URI uri = builder.build(true).toUri();
        Map<String, Object> result = restTemplate.getForObject(uri, Map.class);

        Map<String, Object> response_data = new HashMap<>();
        Map<String, Object> result_data = (Map<String, Object>) result.get("data");
        //System.out.println("result is" + result);
        response_data.put("description", "");
        response_data.put("img", "");
        List<Map<String, Object>> property = (List<Map<String, Object>>) result_data.get("property");
        Iterator<Map<String, Object>> it = property.iterator();
        while(it.hasNext()){
            Map<String, Object> element = it.next();
            if(element.get("predicateLabel").equals("??????")) {
                response_data.put("description", element.get("object"));
                it.remove();
            }
            else if(element.get("predicate").equals("http://edukg.org/knowledge/0.1/property/common#image")){
                response_data.put("img", element.get("object"));
                it.remove();
            }
            else if(element.get("predicateLabel").equals("individual_type")) {
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
        if(relationship.size() > 20)
            relationship = relationship.subList(0, 20);
        for(Map<String, Object> element: relationship){
            element.remove("predicate");
            element.remove("object");
            element.remove("subject");
            element.put("course", course);
        }

        //???????????????????????????
        String category = this.getInstanceCategory(name, course);
        instanceService.setInstanceCategory(name, course, category);
        instanceService.addAccessCount(name,course);
        //List<Map<String, Object>> q = (List<Map<String, Object>>)questionList(name, course).get("data");
        if(token == null){
            response_data.put("isFavorite", false);
        }
        else{
            response_data.put("isFavorite", userService.checkFavorites(token, name, course));
            userService.addHistories(token, name, course);
            String secondLevelName;
            try {
                System.out.println("start find question in " + name);
                Random rand = new Random();
                Map<String, Object> e = relationship.get(rand.nextInt(relationship.size()));
                secondLevelName = (String) (e.get("subject_label") == null ? e.get("object_label") : e.get("subject_label"));
                System.out.println("start find question in " + secondLevelName);
            }
            catch (Exception e){
                secondLevelName = "";
            }
            userService.addQuestionBy2Instance(token, name, secondLevelName, course);
            System.out.println("end find question");
        }
        response_data.put("property", property);
        response_data.put("relationship", relationship);
        Map<String, Object> response = new HashMap<>();
        //response_data.put("hasQuestion", q.size() > 0);
        response.put("code", 200);
        response.put("data", response_data);
        return response;
    }



    /**
     * <pre>
     * ????????????
     * method: Get
     * url: localhost:8080/API/questionList
     * </pre>
     * @param name ??????????????????????????????
     * @param course ??????????????????????????????????????????????????????????????????
     * @return JSON<br>
     * status code:200??????, 401????????? 400?????????????????????500????????????<br>
     * <pre>
     *{
     *     "code": 200,
     *     "data": [
     *       {
     *         "qAnswer": "B",
     *         "A": "??????",
     *         "B": "??????",
     *         "C": "???????????????
     *         "D": "?????????",
     *         "qBody": "?????????????????????????????????:????????????,????????????;????????????,????????????????????????????????????????????????()"
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
            @RequestParam(value="course") String course) throws UnsupportedEncodingException {
        //?????????????????????get??????

        RestTemplate restTemplate = new RestTemplate();
        String name_without_space = name.replace(" ", "+");
        String url = siteUrl + "/questionListByUriName?";
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(url)
                .queryParam("id", userInformationUtil.getUserId())
                .queryParam("uriName", URLEncoder.encode(name_without_space, "UTF-8"));
        URI uri = builder.build(true).toUri();
        System.out.println(uri);
        Map<String, Object> questionResult = restTemplate.getForObject(uri, Map.class);
        List<Map<String, Object>>questionList = (List<Map<String, Object>>) questionResult.get("data");
        Pattern p = Pattern.compile("(.*)A[.???](.*)B[.???](.*)C[.???](.*)D[.???](.*)");
        Pattern answerPattern = Pattern.compile("(.*)([ABCD])(.*)");

        Iterator<Map<String, Object>> question_iterator = questionList.iterator();
        while(question_iterator.hasNext()){
            Map<String, Object> element = question_iterator.next();
            element.remove("id");
            String questionBody = (String)element.get("qBody");
            String answer = (String)element.get("qAnswer");
            Matcher answerMatcher = answerPattern.matcher(answer);
            Matcher m = p.matcher(questionBody);
            if(m.find() && answerMatcher.find()) {
                element.put("qBody", m.group(1));
                element.put("A", m.group(2));
                element.put("B", m.group(3));
                element.put("C", m.group(4));
                element.put("D", m.group(5));
                element.put("qAnswer", answerMatcher.group(2));
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


    /**
     * <pre>
     * ????????????
     * method: Get
     * url: localhost:8080/API/getOutline
     * </pre>
     * @param searchKey ????????????????????????
     * @param course ???????????????
     * @return JSON<br>
     * status code:200??????, 401????????? 400?????????????????????500????????????<br>
     * <pre>
     *     {
     *     "code": 200,
     *     "data": {
     *         "list": [
     *             {
     *                 "label": "????????????",
     *                 "category": "?????????????????????",
     *                 "relationship_list": [
     *                     "??????",
     *                     "???????????????",
     *                     "????????????",
     *                     "????????????",
     *                     "?????????"
     *                 ]
     *             },
     *             ...
     *         ],
     *         "result_size": 64 // list.size()???????????????????????????
     *     }
     * }
     * </pre>
     */
    @ResponseBody
    @RequestMapping(value = "/API/getOutline", method = RequestMethod.GET)
    public Map<String, Object> getOutline(
            @RequestParam(value="searchKey") String searchKey,
            @RequestParam(value="course") String course
    ){
        Map<String, String> param_map = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> response_data = new HashMap<>();
        param_map.put("id", userInformationUtil.getUserId());
        param_map.put("searchKey", searchKey);
        param_map.put("course", course);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object>result = restTemplate.getForObject(
                siteUrl + "/instanceList?id={id}&searchKey={searchKey}&course={course}",
                Map.class,
                param_map);

        List<Map<String, Object>> result_data = (List<Map<String, Object>>)result.get("data");
        List<Map<String, Object>> response_main_list = new ArrayList<>();
        Map<String, Integer> temp_set = new HashMap<>();
        for(Map<String, Object> element: result_data){
            if(response_main_list.size() >= 10)
                break;
            String label = (String)element.get("label");
            if(temp_set.containsKey(label)) {
                Map<String, Object> temp = response_main_list.get(temp_set.get(label));
                temp.put("category", (String)temp.get("category") + " " + (String)element.get("category"));
            }
            else{
                element.remove("uri");
                Set<String> seclist = new HashSet<>();


                param_map.put("name", label);
               Map<String, Object>second_level_instance = restTemplate.getForObject(
                       siteUrl + "/infoByInstanceName?id={id}&name={name}&course={course}",
                        Map.class,
                        param_map);
                List<Map<String, Object>> second_level_instance_list = new ArrayList<>();
                try {
                    second_level_instance_list = (List<Map<String, Object>>) (((Map<String, Object>) second_level_instance.get("data")).get("content"));
                }
                catch(Exception e){
                    System.out.println(second_level_instance);

                }
                if(second_level_instance_list.isEmpty())
                    continue;

                // List<Map<String, Object>> relationship = (List<Map<String, Object>>) (Map<String, Object>)second_level_instance.get("data")result_data.get("content");
                for(Map<String, Object> e: second_level_instance_list){
                    e.remove("predicate");
                    e.remove("object");
                    e.remove("subject");
                    // element.put("course", course);
                    if(e.containsKey("object_label")){
                        seclist.add((String) e.get("object_label"));
                    }
                    else if(e.containsKey("subject_label")){
                        seclist.add((String) e.get("subject_label"));
                    }

                }
                element.put("relationship_list", seclist);
                response_main_list.add(element);
                temp_set.put(label, response_main_list.size() - 1);
            }
        }
        response_data.put("list", response_main_list);
        response_data.put("result_size", response_main_list.size());
        response.put("data", response_data);
        response.put("code", 200);

        return response;

    }



}
