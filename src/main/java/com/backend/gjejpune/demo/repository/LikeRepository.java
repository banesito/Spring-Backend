package com.backend.gjejpune.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.gjejpune.demo.model.Like;
import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    // Find like by user and post
    Optional<Like> findByUserAndPost(User user, Post post);
    
    // Find all likes for a post
    List<Like> findByPost(Post post);
    
    // Count likes for a post
    long countByPost(Post post);
    
    // Find all likes by a user
    List<Like> findByUser(User user);
    
    // Check if a user has liked a post
    boolean existsByUserAndPost(User user, Post post);
} 