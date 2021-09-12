package com.example.edukg_backend.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="tab_instance")
@Getter
@Setter
public class CourseInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String instanceName;
    private String course;
    private String category;
    private int accessCount;
    @JsonIgnore
    @ManyToMany(mappedBy = "histories")
    private Set<User> visitors = new HashSet<>();
    @JsonIgnore
    @ManyToMany(mappedBy = "favorites")
    private  Set<User> lovers = new HashSet<>();
    @JsonIgnore
    @ManyToMany(mappedBy = "recommendInstance")
    private Set<User> recommendInstance = new HashSet<>();

    @OneToMany(targetEntity = CourseInstance.class)
    private List<InstanceComment> commentList = new ArrayList<>();


    public void addComment(InstanceComment c){
        commentList.add(c);
    }

    public List<InstanceComment> getCommentList(){
        return commentList;
    }

    @Override
    public String toString(){
        return "Instance{" + " id: " + id + ", name: " + instanceName + " }";
    }

}
