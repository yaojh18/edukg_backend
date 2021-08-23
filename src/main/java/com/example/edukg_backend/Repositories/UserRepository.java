package com.example.edukg_backend.Repositories;

import com.example.edukg_backend.Models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUserName(String name);
    // User findById(Long id);
}
