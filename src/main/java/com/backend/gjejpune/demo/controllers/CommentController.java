package com.backend.gjejpune.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.gjejpune.demo.model.Comment;
import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.CommentRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.payload.response.PagedResponse;
import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.CommentService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private static final int MAX_PAGE_SIZE = 30;
    
    @Autowired
    private CommentService commentService;
    
    /**
     * Create a new comment on a post
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest commentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return commentService.createComment(postId, commentRequest, currentUserId);
    }
    
    /**
     * Get all comments for a post with pagination
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getCommentsForPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        return commentService.getCommentsByPostId(postId, page, size, currentUserId);
    }
    
    /**
     * Update a comment
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentRequest commentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return commentService.updateComment(commentId, commentRequest, currentUserId);
    }
    
    /**
     * Delete a comment
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return commentService.deleteComment(commentId, currentUserId);
    }
    
    /**
     * Get comment count for a post
     */
    @GetMapping("/posts/{postId}/count")
    public ResponseEntity<?> getCommentCount(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return commentService.getCommentCount(postId, currentUserId);
    }
    
    /**
     * Get comments by current user
     */
    @GetMapping("/my-comments")
    public ResponseEntity<?> getMyComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        return commentService.getCommentsByCurrentUser(page, size, currentUserId);
    }
} 