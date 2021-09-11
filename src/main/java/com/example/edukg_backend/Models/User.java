package com.example.edukg_backend.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name="tab_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name ="user_name")
    private String userName;

    @Column(name="password")
    private String password;

    @JsonIgnore
    @ManyToMany(targetEntity = CourseInstance.class, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "tab_user_history",
            joinColumns = {@JoinColumn(name="user_id")},
            inverseJoinColumns = {@JoinColumn(name = "instance_id")}
    )
    private Set<CourseInstance> histories = new HashSet<>();

    @JsonIgnore
    @ManyToMany(targetEntity = CourseInstance.class, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "tab_user_favorite",
            joinColumns = {@JoinColumn(name="user_id")},
            inverseJoinColumns = {@JoinColumn(name = "instance_id")}
    )
    private Set<CourseInstance> favorites = new HashSet<>();



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

    public Set<CourseInstance> getHistories(){
        return histories;
    }

    public Set<CourseInstance> getFavorites(){
        return favorites;
    }

    public void addHistories(CourseInstance courseInstance){
        histories.add(courseInstance);
    }

    public void addFavorites(CourseInstance courseInstance){
        favorites.add(courseInstance);
    }

    @Override
    public String toString(){
        return "User{" + " id: " + id + ", name: " + userName + " }";
    }


    public void deleteFavorites(CourseInstance courseInstance) {
        favorites.remove(courseInstance);
    }
}
