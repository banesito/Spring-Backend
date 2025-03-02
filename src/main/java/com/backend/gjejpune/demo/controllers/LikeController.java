package com.backend.gjejpune.demo.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.backend.gjejpune.demo.model.Like;
import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.repository.LikeRepository;
import com.backend.gjejpune.demo.repository.PostRepository;
import com.backend.gjejpune.demo.repository.UserRepository;
import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.FriendshipService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/likes")
public class LikeController {
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendshipService friendshipService;
    
    /**
     * Like a post
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Get current user
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Get post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to like this post."));
        }
        
        // Check if user already liked the post
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        
        if (existingLike.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: You have already liked this post."));
        }
        
        // Create new like
        Like like = new Like(user, post);
        likeRepository.save(like);
        
        // Return updated like count
        long likeCount = likeRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse("Post liked successfully. Total likes: " + likeCount));
    }
    
    /**
     * Unlike a post
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Get current user
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Get post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has liked the post
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        
        if (!existingLike.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: You have not liked this post."));
        }
        
        // Delete like
        likeRepository.delete(existingLike.get());
        
        // Return updated like count
        long likeCount = likeRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse("Post unliked successfully. Total likes: " + likeCount));
    }
    
    /**
     * Get all users who liked a post
     */
    @GetMapping("/posts/{postId}/users")
    public ResponseEntity<?> getUsersWhoLikedPost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Get post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view this post's likes."));
        }
        
        // Get all likes for the post
        List<Like> likes = likeRepository.findByPost(post);
        
        return ResponseEntity.ok(likes);
    }
    
    /**
     * Check if current user has liked a post
     */
    @GetMapping("/posts/{postId}/check")
    public ResponseEntity<?> checkIfUserLikedPost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Get current user
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Get post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view this post."));
        }
        
        // Check if user has liked the post
        boolean hasLiked = likeRepository.existsByUserAndPost(user, post);
        
        return ResponseEntity.ok(new MessageResponse(String.valueOf(hasLiked)));
    }
    
    /**
     * Get like count for a post
     */
    @GetMapping("/posts/{postId}/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Get post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view this post."));
        }
        
        // Get like count
        long likeCount = likeRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse(String.valueOf(likeCount)));
    }
} 