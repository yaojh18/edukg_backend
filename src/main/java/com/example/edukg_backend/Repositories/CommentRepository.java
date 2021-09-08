package com.example.edukg_backend.Repositories;

import com.example.edukg_backend.Models.InstanceComment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<InstanceComment, Long> {
}
