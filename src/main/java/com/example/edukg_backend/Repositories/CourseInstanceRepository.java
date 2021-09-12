package com.example.edukg_backend.Repositories;

import com.example.edukg_backend.Models.CourseInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CourseInstanceRepository extends JpaRepository<CourseInstance, Long> {
    CourseInstance findByInstanceNameAndCourse(String instanceName, String course);
}
