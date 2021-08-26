package com.example.edukg_backend.Service;

import com.example.edukg_backend.Models.CourseInstance;
import com.example.edukg_backend.Repositories.CourseInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceService {
    @Autowired
    CourseInstanceRepository courseInstanceRepository;

    public CourseInstance findOrAddInstance(String instanceName, String course){
        CourseInstance result = courseInstanceRepository.findByInstanceNameAndCourse(instanceName, course);
        if(result != null){
            return result;

        }
        result = new CourseInstance();
        result.setInstanceName(instanceName);
        result.setCourse(course);
        return result;
    }
}
