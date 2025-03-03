package com.backend.gjejpune.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.repository.CommentRepository;
import com.backend.gjejpune.demo.repository.LikeRepository;

/**
 * Service for handling post metadata operations like likes and comments counts
 */
@Service
public class PostMetadataService {
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    /**
     * Populate metadata (like and comment counts) for a list of posts
     * 
     * @param posts List of posts to populate metadata for
     * @param currentUser The current user
     */
    public void populatePostMetadata(List<Post> posts, User currentUser) {
        for (Post post : posts) {
            populatePostMetadata(post, currentUser);
        }
    }
    
    /**
     * Populate metadata (like and comment counts) for a single post
     * 
     * @param post Post to populate metadata for
     * @param currentUser The current user
     */
    public void populatePostMetadata(Post post, User currentUser) {
        // Set like count
        post.setLikeCount(likeRepository.countByPost(post));
        
        // Set comment count
        post.setCommentCount(commentRepository.countByPost(post));
        
        // Check if current user has liked the post
        post.setLikedByCurrentUser(likeRepository.existsByUserAndPost(currentUser, post));
    }
} 