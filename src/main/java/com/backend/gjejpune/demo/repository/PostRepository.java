package com.backend.gjejpune.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserOrderByCreatedAtDesc(User user);
    List<Post> findAllByOrderByCreatedAtDesc();
    
    // Paginated queries
    Page<Post> findByUser(User user, Pageable pageable);
    Page<Post> findAll(Pageable pageable);
} 