package com.example.edukg_backend.Service;

import com.example.edukg_backend.Models.CourseInstance;
import com.example.edukg_backend.Models.User;
import com.example.edukg_backend.Repositories.UserRepository;
import com.example.edukg_backend.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    InstanceService instanceService;

    public Map<String, Object> login(String userName, String password){
        User user = userRepository.findByUserName(userName);
        Map<String, Object>result = new HashMap<>();
        if(user == null) {
           result.put("msg", "用户不存在");
           result.put("token", "");
           result.put("code", 401);
        }
        else if(!(user.getPassword().equals(password))){
            result.put("msg", "密码错误");
            result.put("token", "");
            result.put("code", 401);
        }
        else{
            result.put("msg", "登录成功");
            result.put("token", jwtUtil.createToken(Long.toString(user.getId())));
            result.put("code", 200);
        }
        return  result;
    }

    public Map<String, Object> changePassword(String token, String oldPassword, String newPassword){
        Map<String, Object>result = new HashMap<>();
        if(StringUtils.isEmpty(token)){
            result.put("msg", "token为空");
            result.put("code", 401);
        }
        else{
            Claims claims = jwtUtil.getTokenClaim(token);
            Long id = Long.parseLong(claims.getSubject());
            Optional<User> userOptional = userRepository.findById(id);
            if(!userOptional.isPresent()){
                result.put("msg", "用户不存在");
                result.put("code", 401);
            }
            else {
                User user = userOptional.get();
                if (user.getPassword().equals(oldPassword)) {
                    user.setPassword(newPassword);
                    userRepository.save(user);
                    result.put("msg", "修改成功");
                    result.put("code", 200);
                }
                else{
                    result.put("msg", "原密码错误");
                    result.put("code", 401);
                }
            }
        }
        return result;
    }

    public boolean isDuplicateUserName(String userName){
        User user = userRepository.findByUserName(userName);
        return user != null ;
    }

    public Map<String, Object> register(String userName, String password){
        Map<String, Object>result = new HashMap<>();
        if(isDuplicateUserName(userName)) {
            result.put("msg", "用户名已被使用");
            result.put("code", 401);
        }
        else {
            User newUser = new User();
            newUser.setName(userName);
            newUser.setPassword(password);
            userRepository.save(newUser);
            result.put("msg", "注册成功");
            result.put("code", 200);
        }
        return result;
    }

    public void addHistories(String token, String instanceName, String course){
        CourseInstance courseInstance = instanceService.findOrAddInstance(instanceName, course);
        Claims claims = jwtUtil.getTokenClaim(token);
        Long id = Long.parseLong(claims.getSubject());
        Optional<User> userOptional = userRepository.findById(id);
        userOptional.ifPresent(user -> user.addHistories(courseInstance));
    }

    public void addFavorites(String token, String instanceName, String course){
        CourseInstance courseInstance = instanceService.findOrAddInstance(instanceName, course);
        Claims claims = jwtUtil.getTokenClaim(token);
        Long id = Long.parseLong(claims.getSubject());
        Optional<User> userOptional = userRepository.findById(id);
        userOptional.ifPresent(user -> user.addFavorites(courseInstance));
    }

    public boolean checkFavorites(){
        return false;
    }
}
