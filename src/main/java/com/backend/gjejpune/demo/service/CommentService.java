package com.backend.gjejpune.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.gjejpune.demo.model.Comment;
import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.CommentRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.payload.response.PagedResponse;
import com.backend.gjejpune.demo.repository.CommentRepository;
import com.backend.gjejpune.demo.repository.PostRepository;
import com.backend.gjejpune.demo.repository.UserRepository;

@Service
public class CommentService {
    
    private static final int MAX_PAGE_SIZE = 30;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendshipService friendshipService;
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * Get comments for a post with pagination
     */
    public ResponseEntity<?> getCommentsByPostId(Long postId, int page, int size, Long currentUserId) {
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post's comments
        if (!permissionService.canAccessPost(post, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view comments for this post."));
        }
        
        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Get paginated comments for the post
        Page<Comment> commentPage = commentRepository.findByPost(post, pageable);
        List<Comment> comments = commentPage.getContent();
        
        // Create next page URL if not on the last page
        String nextPageUrl = null;
        if (!commentPage.isLast()) {
            nextPageUrl = "/api/comments/post/" + postId + "?page=" + (page + 1) + "&size=" + size;
        }
        
        PagedResponse<Comment> response = new PagedResponse<>(
                comments,
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
     * Create a new comment on a post
     */
    @Transactional
    public ResponseEntity<?> createComment(Long postId, CommentRequest commentRequest, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to comment on this post
        if (!permissionService.canAccessPost(post, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to comment on this post."));
        }
        
        Comment comment = new Comment(
                commentRequest.getContent(),
                user,
                post
        );
        
        comment = commentRepository.save(comment);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    
    /**
     * Update a comment
     */
    @Transactional
    public ResponseEntity<?> updateComment(Long id, CommentRequest commentRequest, Long currentUserId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Comment not found."));
        
        // Check if the current user is the owner of the comment
        if (!permissionService.canModifyComment(comment, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to update this comment."));
        }
        
        comment.setContent(commentRequest.getContent());
        comment = commentRepository.save(comment);
        
        return ResponseEntity.ok(comment);
    }
    
    /**
     * Delete a comment
     */
    @Transactional
    public ResponseEntity<?> deleteComment(Long id, Long currentUserId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Comment not found."));
        
        // Check if the current user is the owner of the comment or the post
        if (!permissionService.canDeleteComment(comment, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to delete this comment."));
        }
        
        commentRepository.delete(comment);
        
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully!"));
    }
    
    /**
     * Get comments by current user
     */
    public ResponseEntity<?> getCommentsByCurrentUser(int page, int size, Long currentUserId) {
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Get paginated comments for the current user
        Page<Comment> commentPage = commentRepository.findByUser(user, pageable);
        List<Comment> comments = commentPage.getContent();
        
        // Create next page URL if not on the last page
        String nextPageUrl = null;
        if (!commentPage.isLast()) {
            nextPageUrl = "/api/comments/my-comments?page=" + (page + 1) + "&size=" + size;
        }
        
        PagedResponse<Comment> response = new PagedResponse<>(
                comments,
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
     * Get comment count for a post
     */
    public ResponseEntity<?> getCommentCount(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        if (!permissionService.canAccessPost(post, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view this post."));
        }
        
        // Get comment count
        long commentCount = commentRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse(String.valueOf(commentCount)));
    }
} 