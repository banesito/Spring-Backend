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
import com.backend.gjejpune.demo.repository.CommentRepository;
import com.backend.gjejpune.demo.repository.PostRepository;
import com.backend.gjejpune.demo.repository.UserRepository;
import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.FriendshipService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private static final int MAX_PAGE_SIZE = 30;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendshipService friendshipService;
    
    /**
     * Create a new comment on a post
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest commentRequest) {
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
                    .body(new MessageResponse("Error: You don't have permission to comment on this post."));
        }
        
        // Create new comment
        Comment comment = new Comment(commentRequest.getContent(), user, post);
        commentRepository.save(comment);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
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
        
        // Get post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view this post's comments."));
        }
        
        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Get paginated comments for post
        Page<Comment> commentPage = commentRepository.findByPost(post, pageable);
        
        // Create next page URL if not on the last page
        String nextPageUrl = null;
        if (!commentPage.isLast()) {
            nextPageUrl = "/api/comments/posts/" + postId + "?page=" + (page + 1) + "&size=" + size;
        }
        
        PagedResponse<Comment> response = new PagedResponse<>(
                commentPage.getContent(),
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast(),
                nextPageUrl
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Update a comment
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentRequest commentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Get comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Error: Comment not found."));
        
        // Check if user is the owner of the comment
        if (!comment.getUser().getId().equals(currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to update this comment."));
        }
        
        // Update comment
        comment.setContent(commentRequest.getContent());
        commentRepository.save(comment);
        
        return ResponseEntity.ok(comment);
    }
    
    /**
     * Delete a comment
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Get comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Error: Comment not found."));
        
        // Check if user is the owner of the comment or the post
        boolean isCommentOwner = comment.getUser().getId().equals(currentUserId);
        boolean isPostOwner = comment.getPost().getUser().getId().equals(currentUserId);
        
        if (!isCommentOwner && !isPostOwner) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to delete this comment."));
        }
        
        // Delete comment
        commentRepository.delete(comment);
        
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully."));
    }
    
    /**
     * Get comment count for a post
     */
    @GetMapping("/posts/{postId}/count")
    public ResponseEntity<?> getCommentCount(@PathVariable Long postId) {
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
        
        // Get comment count
        long commentCount = commentRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse(String.valueOf(commentCount)));
    }
} 