package com.backend.gjejpune.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.LikeService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/likes")
public class LikeController {
    
    @Autowired
    private LikeService likeService;
    
    /**
     * Like a post
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return likeService.likePost(postId, currentUserId);
    }
    
    /**
     * Unlike a post
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return likeService.unlikePost(postId, currentUserId);
    }
    
    /**
     * Get all users who liked a post
     */
    @GetMapping("/posts/{postId}/users")
    public ResponseEntity<?> getUsersWhoLikedPost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return likeService.getUsersWhoLikedPost(postId, currentUserId);
    }
    
    /**
     * Check if current user has liked a post
     */
    @GetMapping("/posts/{postId}/check")
    public ResponseEntity<?> checkIfUserLikedPost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return likeService.checkLikeStatus(postId, currentUserId);
    }
    
    /**
     * Get posts liked by current user
     */
    @GetMapping("/my-liked-posts")
    public ResponseEntity<?> getPostsLikedByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return likeService.getPostsLikedByCurrentUser(currentUserId);
    }
    
    /**
     * Get the count of likes for a post
     */
    @GetMapping("/posts/{postId}/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return likeService.getLikeCount(postId, currentUserId);
    }
} 