package com.example.edukg_backend.Models;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="tab_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    private String password;

    // private List<Map<String, Object>> history;
    // private List<Map<String, Object>> star;
    // backend
    // photo
    public Long getId(){
        return id;
    }

    public String getName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }

    public void setName(String name){
        userName = name;
    }

    public void setPassword(String p){
        password = p;
    }



}
