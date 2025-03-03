package com.backend.gjejpune.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Find users who liked a post
    @Query("SELECT l.user FROM Like l WHERE l.post = :post")
    List<User> findUsersByPost(@Param("post") Post post);
    
    // Find posts liked by a user
    @Query("SELECT l.post FROM Like l WHERE l.user = :user")
    List<Post> findPostsByUser(@Param("user") User user);
} 