package com.example.edukg_backend.Models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
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
    private int accessCount;
    @ManyToMany(mappedBy = "histories")
    private Set<User> visitors = new HashSet<>();
    @ManyToMany(mappedBy = "favorites")
    private  Set<User> lovers = new HashSet<>();



    @Override
    public String toString(){
        return "Instance{" + " id: " + id + ", name: " + instanceName + " }";
    }

}
