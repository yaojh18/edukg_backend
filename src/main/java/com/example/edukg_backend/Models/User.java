package com.example.edukg_backend.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.security.PrivilegedAction;
import java.util.*;

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

    @JsonIgnore
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_recommend_question",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "question_id", referencedColumnName = "id")}
    )
    private List<Question> recommendQuestion = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(targetEntity = CourseInstance.class, cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "tab_user_recommendInstance",
            joinColumns = {@JoinColumn(name="user_id")},
            inverseJoinColumns = {@JoinColumn(name = "instance_id")}
    )
    private Set<CourseInstance> recommendInstance = new HashSet<>();

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

    public void addRecommendQuestion(Question question){
        recommendQuestion.add(question);
    }

    public void addRecommendInstance(CourseInstance courseInstance){recommendInstance.add(courseInstance);}

    public Set<CourseInstance> getRecommendInstance(){
        return recommendInstance;
    }

    public boolean hasRecommendInstance(CourseInstance courseInstance){
        for(CourseInstance c: recommendInstance){
            // System.out.println(c.getId());
            if(Objects.equals(c.getId(), courseInstance.getId()))
                return true;
        }
        return false;
    }


    public List<Question> getRecommendQuestion(){
        return recommendQuestion;
    }

    @Override
    public String toString(){
        return "User{" + " id: " + id + ", name: " + userName + " }";
    }


    public void deleteFavorites(CourseInstance courseInstance) {
        favorites.remove(courseInstance);
    }
}
