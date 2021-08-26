package com.example.edukg_backend.Repositories;

import com.example.edukg_backend.Models.CourseInstance;
import org.springframework.data.repository.CrudRepository;

public interface CourseInstanceRepository extends CrudRepository<CourseInstance, Long> {
    CourseInstance findByInstanceNameAndCourse(String instanceName, String course);
}
