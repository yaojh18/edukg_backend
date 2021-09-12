package com.example.edukg_backend.Service;

import com.example.edukg_backend.ConfigHelper.DefaultEntityConfig;
import com.example.edukg_backend.Models.CourseInstance;
import com.example.edukg_backend.Models.Question;
import com.example.edukg_backend.Models.User;
import com.example.edukg_backend.Repositories.QuestionRepository;
import com.example.edukg_backend.Repositories.UserRepository;
import com.example.edukg_backend.Util.JwtUtil;
import com.example.edukg_backend.Util.RecommendUtil;
import com.example.edukg_backend.Util.UserInformationUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
// import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.beans.Transient;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// import java.util.function.UnaryOperator;


@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    InstanceService instanceService;
    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    DefaultEntityConfig defaultEntityConfig;
    @Autowired
    UserInformationUtil userInformationUtil;
    @Autowired
    RecommendUtil recommendUtil;

    public Optional<User> checkToken(String token){
        if(token==null){
            return Optional.empty();
        }
        try {
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            return userRepository.findById(id);
        }
        catch (Exception e){
            return Optional.empty();
        }
    }

    public ResponseEntity<Map<String, Object>> login(String userName, String password){
        User user = userRepository.findByUserName(userName);
        Map<String, Object>result = new HashMap<>();
        HttpStatus httpStatus;
        if(user == null) {
           result.put("msg", "用户不存在");
           result.put("token", "");
           httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else if(!(user.getPassword().equals(password))){
            result.put("msg", "密码错误");
            result.put("token", "");
           httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else{
            result.put("msg", "登录成功");
            result.put("token", jwtUtil.createToken(Long.toString(user.getId())));
            httpStatus = HttpStatus.OK;
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> changePassword(String token, String oldPassword, String newPassword){
        Map<String, Object>result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            User user = userOptional.get();
            if (user.getPassword().equals(oldPassword)) {
                user.setPassword(newPassword);
                userRepository.save(user);
                result.put("msg", "修改成功");
                httpStatus = HttpStatus.OK;
            }
            else{
                result.put("msg", "原密码错误");
                httpStatus = HttpStatus.UNAUTHORIZED;
            }
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public boolean isDuplicateUserName(String userName){
        User user = userRepository.findByUserName(userName);
        return user != null ;
    }

    public ResponseEntity<Map<String, Object>> register(String userName, String password){
        Map<String, Object>result = new HashMap<>();
        HttpStatus httpStatus;
        if(isDuplicateUserName(userName)) {
            result.put("msg", "用户名已被使用");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            User newUser = new User();
            newUser.setName(userName);
            newUser.setPassword(password);
            userRepository.save(newUser);
            result.put("msg", "注册成功");
            httpStatus = HttpStatus.OK;
            result.put("token", jwtUtil.createToken(Long.toString(newUser.getId())));
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> addHistories(String token, String instanceName, String course){
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            CourseInstance courseInstance = instanceService.findOrAddInstance(instanceName, course);
            User user = userOptional.get();
            user.addHistories(courseInstance);
            userRepository.save(user);
            result.put("msg", "历史添加成功");
            httpStatus = HttpStatus.OK;
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> addFavorites(String token, String instanceName, String course){
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            CourseInstance courseInstance = instanceService.findOrAddInstance(instanceName, course);
            User user = userOptional.get();
            user.addFavorites(courseInstance);
            userRepository.save(user);
            result.put("msg", "收藏添加成功");

            httpStatus = HttpStatus.OK;
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public boolean checkFavorites(String token, String instanceName, String course){
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getFavorites().contains(instanceService.findOrAddInstance(instanceName, course));
        }
        return false;

    }

    public ResponseEntity<Map<String, Object>> deleteFavorites(String token, String instanceName, String course) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            CourseInstance courseInstance = instanceService.findOrAddInstance(instanceName, course);
            User user = userOptional.get();
            user.deleteFavorites(courseInstance);
            userRepository.save(user);
            result.put("msg", "收藏删除成功");
            httpStatus = HttpStatus.OK;
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> getFavoritesList(String token) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            User user = userOptional.get();
            result.put("data", user.getFavorites());
            httpStatus = HttpStatus.OK;
        }

        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> getHistoriesList(String token) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            User user = userOptional.get();
            result.put("data", user.getHistories());
            httpStatus = HttpStatus.OK;
        }

        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }


    public Map<String, Object> recommendEntity(String token) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            User user = userOptional.get();
            List<Map<String, Object>> recommendList = new ArrayList<>();
            Set<CourseInstance> favList = user.getFavorites();
            Set<CourseInstance> hisList = user.getHistories();
            httpStatus = HttpStatus.OK;
            List<Map<String, Object>> result_data = new ArrayList<>();
            if(hisList.isEmpty() && favList.isEmpty()){
                result.put("data", defaultEntityConfig.getDefaultEntity());
            }
            else{

                addFavoritesAndHistories2Recommend(favList, result_data);
                addFavoritesAndHistories2Recommend(hisList, result_data);
                if(result_data.size() < 6){
                    result_data.addAll(defaultEntityConfig.getDefaultEntity());
                }
                result.put("data", result_data);
            }
        }

        result.put("code", httpStatus.value());
        return  result;
    }

    private void addFavoritesAndHistories2Recommend(Set<CourseInstance> userList, List<Map<String, Object>> result_data_list) {
        for(CourseInstance element: userList){
            Map<String, Object> temp = new HashMap<>();
            temp.put("name", element.getInstanceName());
            temp.put("course", element.getCourse());
            temp.put("needMore", "true");
            result_data_list.add(temp);
        }
    }

    public List<Question> getDefaultQuestion(){
        return questionRepository.findAllDefault();
    }

    public ResponseEntity<Map<String, Object>> getRecommendQuestionList(String token, int number) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            result.put("msg", "用户不存在");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else {
            User user = userOptional.get();
            /*
            List<Question> temp = user.getRecommendQuestion();
            if(temp.size() < 5)
                temp.addAll(questionRepository.findAllDefault());

             */
            List<Map<String, Object>>temp = recommendUtil.getRecommendTable(user.getId());
            System.out.println("temp size is " + temp.size());
            if(temp.size() < 5){
                List<Map<String ,Object>> temp2 = questionList2MapList(questionRepository.findAllDefault());
                temp2.addAll(temp);
                result.put("data", getRandomFromList2(temp2, number));

            }
            else{
                result.put("data", getRandomFromList2(temp, number));
            }
            httpStatus = HttpStatus.OK;
        }

        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    private List<Map<String, Object>> questionList2MapList(List<Question> questionList){
        List<Map<String, Object>>result = new Vector<>();
        for(Question q:questionList){
            Map<String, Object>temp = new ConcurrentHashMap<>();
            temp.put("qBody", q.getQuestionBody());
            temp.put("qAnswer", q.getAnswer());
            result.add(temp);
        }
        return result;
    }

    public List<Map<String, Object>> getRandomFromList2(List<Map<String, Object>> questionList, int number){
        Pattern p = Pattern.compile("(.*)A[.．](.*)B[.．](.*)C[.．](.*)D[.．](.*)");
        Pattern answerPattern = Pattern.compile("(.*)([ABCD])(.*)");
        Set<String>questionBodySet = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        int searchTime = 0;
        Random rand = new Random();
        while(count < number && searchTime < 20){
            Map<String, Object>question = new HashMap<>();
            searchTime++;
            Map<String, Object> q = questionList.get(rand.nextInt(questionList.size()));
            String questionBody = (String) q.get("qBody");
            String questionAnswer = (String) q.get("qAnswer");
            Matcher m = p.matcher(questionBody);
            Matcher answerMatcher = answerPattern.matcher(questionAnswer);
            if(questionBodySet.contains(questionBody))
                continue;
            if(m.find() && answerMatcher.find()){
                questionBodySet.add(questionBody);
                question.put("qBody", m.group(1));
                question.put("A", m.group(2));
                question.put("B", m.group(3));
                question.put("C", m.group(4));
                question.put("D", m.group(5));
                question.put("qAnswer", answerMatcher.group(2));
                result.add(question);
                count++;
            }
        }
        return result;
    }

    public List<Map<String, Object>> getRandomFromList(List<Question> questionList, int number){
        Pattern p = Pattern.compile("(.*)A[.．](.*)B[.．](.*)C[.．](.*)D[.．](.*)");
        Pattern answerPattern = Pattern.compile("(.*)([ABCD])(.*)");
        Set<String>questionBodySet = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        int searchTime = 0;
        Random rand = new Random();
        while(count < number && searchTime < 20){
            Map<String, Object>question = new HashMap<>();
            searchTime++;
            Question q = questionList.get(rand.nextInt(questionList.size()));
            String questionBody = q.getQuestionBody();
            String answer = q.getAnswer();
            Matcher m = p.matcher(questionBody);
            Matcher answerMatcher = answerPattern.matcher(q.getAnswer());
            if(questionBodySet.contains(questionBody))
                continue;
            if(m.find() && answerMatcher.find()){
                questionBodySet.add(questionBody);
                question.put("qBody", m.group(1));
                question.put("A", m.group(2));
                question.put("B", m.group(3));
                question.put("C", m.group(4));
                question.put("D", m.group(5));
                question.put("qAnswer", answerMatcher.group(2));
                result.add(question);
                count++;
            }
        }
        return result;
    }

    public Question addRecommendQuestion(String qBody,
                                     String answer,
                                     String instanceName,
                                     String course,
                                     boolean isDefault){
        Question question = new Question();
        question.setQuestionBody(qBody);
        question.setAnswer(answer);
        question.setInstanceName(instanceName);
        question.setCourse(course);
        question.setDefault(isDefault);
        questionRepository.save(question);
        return question;
    }

    public void addRecommendQuestion2User(String token,
                                          String qBody,
                                          String answer,
                                          String instanceName,
                                          String course,
                                          boolean isDefault){
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            return;
        }
        else {
            /*
            User user = userOptional.get();
            Question q = addRecommendQuestion(qBody,answer,instanceName,course,isDefault);
            questionRepository.flush();
            user.addRecommendQuestion(q);
            System.out.println("try to add question to" + user);
            userRepository.save(user);
            */
            User user = userOptional.get();
            recommendUtil.writeToRecommendTable(user.getId(), qBody, answer);
        }

    }

    @Async("getQuestionExecutor")
    public void addQuestionBy2Instance(String token, String instanceName1, String instanceName2, String course){
        addQuestionByInstance(token, instanceName1, course);
        if(!instanceName2.equals(""))addQuestionByInstance(token, instanceName2, course);
    }


    public void addQuestionByInstance(String token,
                                      String instanceName,
                                      String course) {

        try {
            if(checkOrAddRecommendInstance(token, instanceName, course)){
                Optional<User> userOptional = checkToken(token);
                RestTemplate restTemplate = new RestTemplate();
                String name_without_space = instanceName;//.replace(" ", "+");
                String url = "http://open.edukg.cn/opedukg/api/typeOpen/open" + "/questionListByUriName?";
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(url)
                        .queryParam("id", userInformationUtil.getUserId())
                        .queryParam("uriName", URLEncoder.encode(name_without_space, "UTF-8"));
                URI uri = builder.build().toUri();
                Map<String, Object> questionResult = restTemplate.getForObject(uri, Map.class);
                System.out.println("questionResult" + questionResult);
                List<Map<String, Object>>questionList = (List<Map<String, Object>>) questionResult.get("data");
                if(userOptional.isEmpty()){
                    return;
                }
                else {
                    User user = userOptional.get();
                    List<Map<String, Object>> sublist = questionList.size() > 3 ? questionList.subList(0, 3) : questionList;
                    System.out.println("question list is "+ questionList);
                    for(Map<String, Object> q: sublist){
                        try {
                            System.out.println("try adding question " + q.get("qBody") + " for " + instanceName);
                            addRecommendQuestion2User(token, (String) q.get("qBody"), (String) q.get("qAnswer"), instanceName, course, false);
                        }
                        catch(Exception e){
                            System.out.println("fail in" + q.get("qBody"));
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("finish add question of instance of" + instanceName);
    }

    @Async("getQuestionExecutor")
    public void addQuestionByQuestionList(String token, String instanceName, String course, List<Map<String, Object>> questionList){
        if(checkOrAddRecommendInstance(token, instanceName, course)){
            Optional<User> userOptional = checkToken(token);
            if(userOptional.isEmpty()){
                return;
            }
            else {
                User user = userOptional.get();
                for(Map<String, Object> q: questionList){
                    addRecommendQuestion2User(token, (String)q.get("qBody"), (String)q.get("qAnswer"), instanceName, course, false);
                    System.out.println("finish adding question " + q.get("qBody") + " for " + instanceName);
                }
            }
        }

    }


    // true的时候才需要更新
    public boolean checkOrAddRecommendInstance(String token, String name, String course){
        CourseInstance courseInstance = instanceService.findOrAddInstance(name,course);
        System.out.println(courseInstance);
        Long instanceId = courseInstance.getId();
        Optional<User> userOptional = checkToken(token);
        if(userOptional.isEmpty()){
            return false;
        }
        else {
            User user = userOptional.get();
            //Set<CourseInstance> c = user.getRecommendInstance();
            if(user.hasRecommendInstance(courseInstance)){
                System.out.println("has visit");
                return false;
            }
            else{
                user.addRecommendInstance(courseInstance);
                System.out.println("try to add instance to user" + user);
                userRepository.save(user);
                return true;
            }
        }
    }
}
