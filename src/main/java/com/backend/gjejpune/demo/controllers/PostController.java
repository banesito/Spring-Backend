package com.backend.gjejpune.demo.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.PostRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.payload.response.PagedResponse;
import com.backend.gjejpune.demo.payload.response.PostResponse;
import com.backend.gjejpune.demo.repository.PostRepository;
import com.backend.gjejpune.demo.repository.UserRepository;
import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.FileStorageService;
import com.backend.gjejpune.demo.service.FriendshipService;
import com.backend.gjejpune.demo.repository.LikeRepository;
import com.backend.gjejpune.demo.repository.CommentRepository;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 30;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private FriendshipService friendshipService;
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    // Get all posts (respecting privacy settings) with pagination
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Get paginated posts
        Page<Post> postPage = postRepository.findAll(pageable);
        
        // Filter posts based on privacy settings and friendship
        List<Post> filteredPosts = postPage.getContent().stream()
            .filter(post -> {
                // Include post if:
                // 1. It's not private, OR
                // 2. Current user is the owner
                // 3. User profile is not private OR current user is the owner OR current user is a friend
                boolean isOwner = post.getUser().getId().equals(currentUserId);
                boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
                boolean isPostVisible = !post.isPrivate() || isOwner || isFriend;
                boolean isUserVisible = !post.getUser().isPrivateProfile() || isOwner || isFriend;
                
                return isPostVisible && isUserVisible;
            })
            .collect(Collectors.toList());
        
        // Populate like and comment counts for each post
        for (Post post : filteredPosts) {
            post.setLikesCount(likeRepository.countByPost(post));
            post.setCommentsCount(commentRepository.countByPost(post));
            post.setLikedByCurrentUser(likeRepository.existsByUserAndPost(currentUser, post));
        }
        
        // Create next page URL if not on the last page
        String nextPageUrl = null;
        if (!postPage.isLast()) {
            nextPageUrl = "/api/posts?page=" + (page + 1) + "&size=" + size;
        }
        
        PagedResponse<Post> response = new PagedResponse<>(
                filteredPosts,
                postPage.getNumber(),
                filteredPosts.size(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast(),
                nextPageUrl
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // Get a single post by ID (respecting privacy settings)
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view this post."));
        }
        
        // Populate like and comment counts
        post.setLikesCount(likeRepository.countByPost(post));
        post.setCommentsCount(commentRepository.countByPost(post));
        post.setLikedByCurrentUser(likeRepository.existsByUserAndPost(currentUser, post));
        
        return new ResponseEntity<>(post, HttpStatus.OK);
    }
    
    // Get current user's posts with pagination
    @GetMapping("/my-posts")
    public ResponseEntity<?> getCurrentUserPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Get paginated posts for current user
        Page<Post> postPage = postRepository.findByUser(user, pageable);
        
        // Populate like and comment counts for each post
        List<Post> posts = postPage.getContent();
        for (Post post : posts) {
            post.setLikesCount(likeRepository.countByPost(post));
            post.setCommentsCount(commentRepository.countByPost(post));
            post.setLikedByCurrentUser(likeRepository.existsByUserAndPost(user, post));
        }
        
        // Create next page URL if not on the last page
        String nextPageUrl = null;
        if (!postPage.isLast()) {
            nextPageUrl = "/api/posts/my-posts?page=" + (page + 1) + "&size=" + size;
        }
        
        PagedResponse<Post> response = new PagedResponse<>(
                posts,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast(),
                nextPageUrl
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // Get posts by user ID (respecting privacy settings) with pagination
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        // Validate and limit page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Check if the target user has a private profile and is not the current user or a friend
        boolean isFriend = friendshipService.areFriends(currentUserId, userId);
        if (targetUser.isPrivateProfile() && !userId.equals(currentUserId) && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: This user has a private profile."));
        }
        
        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Get paginated posts for target user
        Page<Post> postPage = postRepository.findByUser(targetUser, pageable);
        
        List<Post> filteredPosts = postPage.getContent();
        
        // If viewing someone else's posts, filter out private posts unless you're a friend
        if (!userId.equals(currentUserId) && !isFriend) {
            filteredPosts = filteredPosts.stream()
                    .filter(post -> !post.isPrivate())
                    .collect(Collectors.toList());
        }
        
        // Populate like and comment counts for each post
        for (Post post : filteredPosts) {
            post.setLikesCount(likeRepository.countByPost(post));
            post.setCommentsCount(commentRepository.countByPost(post));
            post.setLikedByCurrentUser(likeRepository.existsByUserAndPost(currentUser, post));
        }
        
        // Create next page URL if not on the last page
        String nextPageUrl = null;
        if (!postPage.isLast()) {
            nextPageUrl = "/api/posts/user/" + userId + "?page=" + (page + 1) + "&size=" + size;
        }
        
        PagedResponse<Post> response = new PagedResponse<>(
                filteredPosts,
                postPage.getNumber(),
                filteredPosts.size(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast(),
                nextPageUrl
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // Create a new post
    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequest postRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = new Post(
                postRequest.getTitle(),
                postRequest.getContent(),
                user,
                postRequest.isPrivate()
        );
        
        // Set image URL if provided
        if (postRequest.getImageUrl() != null && !postRequest.getImageUrl().isEmpty()) {
            post.setImageUrl(postRequest.getImageUrl());
            logger.info("User {} created a new post with ID: {}, privacy: {}, with image", 
                user.getUsername(), post.getId(), post.isPrivate() ? "private" : "public");
        } else {
            logger.info("User {} created a new post with ID: {}, privacy: {}, without image", 
                user.getUsername(), post.getId(), post.isPrivate() ? "private" : "public");
        }
        
        post = postRepository.save(post);
        
        String privacyMessage = post.isPrivate() 
                ? "Post created successfully and set to private. Only you can view it." 
                : "Post created successfully and set to public. Anyone can view it (unless your profile is private).";
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponse(privacyMessage, post));
    }
    
    // Create a new post with image upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "isPrivate", defaultValue = "false") boolean isPrivate,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = new Post(
                title,
                content,
                user,
                isPrivate
        );
        
        // Process and store the image if provided
        if (image != null && !image.isEmpty()) {
            // Check if file is an image
            if (!image.getContentType().startsWith("image/")) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Only image files are allowed for post images."));
            }
            
            // Store the file and get the URL
            String fileUrl = fileStorageService.storeFile(image);
            post.setImageUrl(fileUrl);
            
            logger.info("User {} created a new post with image, privacy: {}", 
                    user.getUsername(), post.isPrivate() ? "private" : "public");
        } else {
            logger.info("User {} created a new post without image, privacy: {}", 
                    user.getUsername(), post.isPrivate() ? "private" : "public");
        }
        
        post = postRepository.save(post);
        
        String privacyMessage = post.isPrivate() 
                ? "Post created successfully and set to private. Only you can view it." 
                : "Post created successfully and set to public. Anyone can view it (unless your profile is private).";
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponse(privacyMessage, post));
    }
    
    // Update a post
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest postRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if the current user is the owner of the post
        if (!post.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to update this post."));
        }
        
        // Store the original privacy setting
        boolean wasPrivate = post.isPrivate();
        boolean privacyChanged = false;
        boolean imageChanged = false;
        
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        
        // Update image URL if provided
        if (postRequest.getImageUrl() != null) {
            String oldImageUrl = post.getImageUrl();
            post.setImageUrl(postRequest.getImageUrl());
            imageChanged = !postRequest.getImageUrl().equals(oldImageUrl);
            
            if (imageChanged) {
                logger.info("User {} updated image for post {}", 
                    userDetails.getUsername(), post.getId());
            }
        }
        
        // Only update privacy if it was explicitly included in the request
        // This is determined by checking if the field was set in the request
        if (postRequest.isPrivateExplicitlySet()) {
            post.setPrivate(postRequest.isPrivate());
            privacyChanged = wasPrivate != post.isPrivate();
        }
        
        post = postRepository.save(post);
        
        // Log the privacy change if it occurred
        if (privacyChanged) {
            logger.info("User {} changed post {} privacy from {} to {}", 
                    userDetails.getUsername(), post.getId(), 
                    wasPrivate ? "private" : "public", 
                    post.isPrivate() ? "private" : "public");
        }
        
        String message = "Post updated successfully!";
        if (imageChanged) {
            message += " Post image has been updated.";
        }
        if (privacyChanged) {
            message = post.isPrivate() 
                    ? "Post updated successfully and set to private. Only you can view it." 
                    : "Post updated successfully and set to public. Anyone can view it (unless your profile is private).";
        }
        
        return ResponseEntity.ok(new PostResponse(message, post));
    }
    
    // Update a post with image upload
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePostWithImage(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "isPrivate", required = false) Boolean isPrivate,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if the current user is the owner of the post
        if (!post.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to update this post."));
        }
        
        // Store the original privacy setting
        boolean wasPrivate = post.isPrivate();
        boolean privacyChanged = false;
        boolean imageChanged = false;
        boolean contentChanged = false;
        
        // Only update title if provided
        if (title != null) {
            post.setTitle(title);
            contentChanged = true;
        }
        
        // Only update content if provided
        if (content != null) {
            post.setContent(content);
            contentChanged = true;
        }
        
        // Update privacy setting if provided
        if (isPrivate != null) {
            post.setPrivate(isPrivate);
            privacyChanged = wasPrivate != post.isPrivate();
        }
        
        // Process and store the image if provided
        if (image != null && !image.isEmpty()) {
            // Check if file is an image
            if (!image.getContentType().startsWith("image/")) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Only image files are allowed for post images."));
            }
            
            // Store the file and get the URL
            String fileUrl = fileStorageService.storeFile(image);
            String oldImageUrl = post.getImageUrl();
            post.setImageUrl(fileUrl);
            imageChanged = true;
            
            logger.info("User {} updated image for post {}", 
                    userDetails.getUsername(), post.getId());
        }
        
        post = postRepository.save(post);
        
        // Log the privacy change if it occurred
        if (privacyChanged) {
            logger.info("User {} changed post {} privacy from {} to {}", 
                    userDetails.getUsername(), post.getId(), 
                    wasPrivate ? "private" : "public", 
                    post.isPrivate() ? "private" : "public");
        }
        
        String message = "Post updated successfully!";
        if (contentChanged) {
            message += " Post content has been updated.";
        }
        if (imageChanged) {
            message += " Post image has been updated.";
        }
        if (privacyChanged) {
            message += post.isPrivate() 
                    ? " Post is now private. Only you can view it." 
                    : " Post is now public. Anyone can view it (unless your profile is private).";
        }
        
        return ResponseEntity.ok(new PostResponse(message, post));
    }
    
    // Delete a post
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if the current user is the owner of the post
        if (!post.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to delete this post."));
        }
        
        postRepository.delete(post);
        
        return ResponseEntity.ok(new MessageResponse("Post deleted successfully!"));
    }
} 