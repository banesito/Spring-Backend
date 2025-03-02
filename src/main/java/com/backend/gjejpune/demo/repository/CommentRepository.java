package com.backend.gjejpune.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.gjejpune.demo.model.Comment;
import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find all comments for a post
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    
    // Find all comments for a post with pagination
    Page<Comment> findByPost(Post post, Pageable pageable);
    
    // Find all comments by a user
    List<Comment> findByUserOrderByCreatedAtDesc(User user);
    
    // Find all comments by a user with pagination
    Page<Comment> findByUser(User user, Pageable pageable);
    
    // Count comments for a post
    long countByPost(Post post);
} 