package com.example.edukg_backend.Service;

import com.example.edukg_backend.Models.CourseInstance;
import com.example.edukg_backend.Models.User;
import com.example.edukg_backend.Repositories.UserRepository;
import com.example.edukg_backend.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    InstanceService instanceService;

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
        if(StringUtils.isEmpty(token)){
            result.put("msg", "token为空");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else{
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            Optional<User> userOptional = userRepository.findById(id);
            if(!userOptional.isPresent()){
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
        if(StringUtils.isEmpty(token)){
            result.put("msg", "token为空");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else{
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            Optional<User> userOptional = userRepository.findById(id);
            if(!userOptional.isPresent()){
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
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> addFavorites(String token, String instanceName, String course){
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        if(StringUtils.isEmpty(token)){
            result.put("msg", "token为空");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else{
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            Optional<User> userOptional = userRepository.findById(id);
            if(!userOptional.isPresent()){
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
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public boolean checkFavorites(String token, String instanceName, String course){
        Claims claims = jwtUtil.getTokenClaim(token);
        Long id = Long.parseLong(claims.getSubject());
        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getFavorites().contains(instanceService.findOrAddInstance(instanceName, course));
        }
        return false;

    }

    public ResponseEntity<Map<String, Object>> deleteFavorites(String token, String instanceName, String course) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        if(StringUtils.isEmpty(token)){
            result.put("msg", "token为空");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else{
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            Optional<User> userOptional = userRepository.findById(id);
            if(!userOptional.isPresent()){
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
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> getFavoritesList(String token) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        if(StringUtils.isEmpty(token)){
            result.put("msg", "token为空");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else{
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            Optional<User> userOptional = userRepository.findById(id);
            if(!userOptional.isPresent()){
                result.put("msg", "用户不存在");
                httpStatus = HttpStatus.UNAUTHORIZED;
            }
            else {
                User user = userOptional.get();
                result.put("data", user.getFavorites());
                httpStatus = HttpStatus.OK;
            }
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }

    public ResponseEntity<Map<String, Object>> getHistoriesList(String token) {
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus;
        if(StringUtils.isEmpty(token)){
            result.put("msg", "token为空");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        else{
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            Optional<User> userOptional = userRepository.findById(id);
            if(!userOptional.isPresent()){
                result.put("msg", "用户不存在");
                httpStatus = HttpStatus.UNAUTHORIZED;
            }
            else {
                User user = userOptional.get();
                result.put("data", user.getHistories());
                httpStatus = HttpStatus.OK;
            }
        }
        result.put("code", httpStatus.value());
        return  ResponseEntity.status(httpStatus).body(result);
    }
}
