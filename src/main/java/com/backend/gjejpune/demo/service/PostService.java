package com.backend.gjejpune.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.PostRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.payload.response.PagedResponse;
import com.backend.gjejpune.demo.repository.PostRepository;
import com.backend.gjejpune.demo.repository.UserRepository;

@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private FriendshipService friendshipService;
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private PostMetadataService postMetadataService;
    
    /**
     * Get all posts with pagination, respecting privacy settings
     */
    public PagedResponse<Post> getAllPosts(int page, int size, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Create pageable object for pagination
        Pageable pageable = PaginationUtils.createPageable(page, size);
        
        // Get paginated posts
        Page<Post> postPage = postRepository.findAll(pageable);
        
        // Filter posts based on privacy settings and friendship
        List<Post> filteredPosts = postPage.getContent().stream()
            .filter(post -> permissionService.canAccessPost(post, currentUserId))
            .collect(Collectors.toList());
        
        // Populate like and comment counts for each post
        postMetadataService.populatePostMetadata(filteredPosts, currentUser);
        
        return PaginationUtils.createPagedResponse(filteredPosts, postPage, "/api/posts");
    }
    
    /**
     * Get a single post by ID, respecting privacy settings
     */
    public ResponseEntity<?> getPostById(Long id, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        if (!permissionService.canAccessPost(post, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view this post."));
        }
        
        // Populate like and comment counts
        postMetadataService.populatePostMetadata(post, currentUser);
        
        return new ResponseEntity<>(post, HttpStatus.OK);
    }
    
    /**
     * Get current user's posts with pagination
     */
    public PagedResponse<Post> getCurrentUserPosts(int page, int size, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Create pageable object for pagination
        Pageable pageable = PaginationUtils.createPageable(page, size);
        
        // Get paginated posts for current user
        Page<Post> postPage = postRepository.findByUser(user, pageable);
        
        // Populate like and comment counts for each post
        List<Post> posts = postPage.getContent();
        postMetadataService.populatePostMetadata(posts, user);
        
        return PaginationUtils.createPagedResponse(posts, postPage, "/api/posts/my-posts");
    }
    
    /**
     * Get posts by user ID with pagination, respecting privacy settings
     */
    public ResponseEntity<?> getPostsByUserId(Long userId, int page, int size, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Check if the target user has a private profile and is not the current user or a friend
        if (!permissionService.canAccessUserProfile(targetUser, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: This user has a private profile."));
        }
        
        // Create pageable object for pagination
        Pageable pageable = PaginationUtils.createPageable(page, size);
        
        // Get paginated posts for target user
        Page<Post> postPage = postRepository.findByUser(targetUser, pageable);
        
        // Filter posts based on privacy settings if not the owner
        List<Post> filteredPosts = postPage.getContent();
        if (!userId.equals(currentUserId)) {
            filteredPosts = filteredPosts.stream()
                .filter(post -> !post.isPrivate() || permissionService.canAccessPost(post, currentUserId))
                .collect(Collectors.toList());
        }
        
        // Populate like and comment counts for each post
        postMetadataService.populatePostMetadata(filteredPosts, currentUser);
        
        return ResponseEntity.ok(PaginationUtils.createPagedResponse(
                filteredPosts, 
                postPage, 
                "/api/posts/user/" + userId
        ));
    }
    
    /**
     * Create a new post
     */
    @Transactional
    public ResponseEntity<?> createPost(PostRequest postRequest, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = new Post();
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setPrivate(postRequest.isPrivate());
        post.setUser(user);
        
        // Set a default title if none is provided
        String title = postRequest.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Untitled Post";
        }
        post.setTitle(title);
        
        post = postRepository.save(post);
        
        return new ResponseEntity<>(post, HttpStatus.CREATED);
    }
    
    /**
     * Create a post with image upload
     */
    @Transactional
    public ResponseEntity<?> createPostWithImage(String title, String content, Boolean isPrivate, MultipartFile image, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Create a new post
        Post post = new Post();
        post.setContent(content);
        post.setPrivate(isPrivate != null ? isPrivate : false);
        post.setUser(user);
        
        // Set title (use default if not provided)
        if (title != null && !title.trim().isEmpty()) {
            post.setTitle(title);
        } else {
            post.setTitle("Untitled Post");
        }
        
        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = fileStorageService.storeFile(image, "post_images");
                post.setImageUrl(imageUrl);
            } catch (Exception e) {
                logger.error("Error uploading image: {}", e.getMessage());
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error: Could not upload image."));
            }
        }
        
        post = postRepository.save(post);
        
        return new ResponseEntity<>(post, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing post
     */
    @Transactional
    public ResponseEntity<?> updatePost(Long id, PostRequest postRequest, Long currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if the current user is the owner of the post
        if (!permissionService.canModifyPost(post, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to update this post."));
        }
        
        // Update post fields
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setPrivate(postRequest.isPrivate());
        
        post = postRepository.save(post);
        
        return new ResponseEntity<>(post, HttpStatus.OK);
    }
    
    /**
     * Update a post with image upload
     */
    @Transactional
    public ResponseEntity<?> updatePostWithImage(Long id, String title, String content, Boolean isPrivate, MultipartFile image, Long currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if the current user is the owner of the post
        if (!permissionService.canModifyPost(post, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to update this post."));
        }
        
        // Update post fields if provided
        if (title != null && !title.trim().isEmpty()) {
            post.setTitle(title);
        }
        
        if (content != null && !content.trim().isEmpty()) {
            post.setContent(content);
        }
        
        if (isPrivate != null) {
            post.setPrivate(isPrivate);
        }
        
        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = fileStorageService.storeFile(image, "post_images");
                post.setImageUrl(imageUrl);
            } catch (Exception e) {
                logger.error("Error uploading image: {}", e.getMessage());
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error: Could not upload image."));
            }
        }
        
        post = postRepository.save(post);
        
        return new ResponseEntity<>(post, HttpStatus.OK);
    }
    
    /**
     * Delete a post
     */
    @Transactional
    public ResponseEntity<?> deletePost(Long id, Long currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if the current user is the owner of the post
        if (!permissionService.canModifyPost(post, currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to delete this post."));
        }
        
        // Delete the post
        postRepository.delete(post);
        
        return ResponseEntity.ok(new MessageResponse("Post deleted successfully!"));
    }
    
    /**
     * Get posts for the user's feed (posts from friends)
     */
    public ResponseEntity<?> getFeedPosts(int page, int size, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Get IDs of friends
        List<Long> friendIds = friendshipService.getFriendIds(currentUserId);
        
        // Add current user's ID to include their posts in the feed
        friendIds.add(currentUserId);
        
        // Create pageable object for pagination
        Pageable pageable = PaginationUtils.createPageable(page, size);
        
        // Get paginated posts from friends and current user
        Page<Post> postPage = postRepository.findByUserIdIn(friendIds, pageable);
        
        // Filter posts based on privacy settings
        List<Post> filteredPosts = postPage.getContent().stream()
            .filter(post -> {
                // Include post if:
                // 1. It's not private, OR
                // 2. Current user is the owner
                boolean isOwner = post.getUser().getId().equals(currentUserId);
                return !post.isPrivate() || isOwner;
            })
            .collect(Collectors.toList());
        
        // Populate like and comment counts for each post
        postMetadataService.populatePostMetadata(filteredPosts, currentUser);
        
        return ResponseEntity.ok(PaginationUtils.createPagedResponse(
                filteredPosts, 
                postPage, 
                "/api/posts/feed"
        ));
    }
} 