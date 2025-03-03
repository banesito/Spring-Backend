package com.backend.gjejpune.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.backend.gjejpune.demo.model.Like;
import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.repository.LikeRepository;
import com.backend.gjejpune.demo.repository.PostRepository;
import com.backend.gjejpune.demo.repository.UserRepository;

@Service
public class LikeService {
    
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
    public ResponseEntity<?> likePost(Long postId, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to like this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to like this post."));
        }
        
        // Check if user already liked this post
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        if (existingLike.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: You have already liked this post."));
        }
        
        // Create new like
        Like like = new Like(user, post);
        like = likeRepository.save(like);
        
        // Update like count in response
        long likesCount = likeRepository.countByPost(post);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Post liked successfully! Total likes: " + likesCount));
    }
    
    /**
     * Unlike a post
     */
    public ResponseEntity<?> unlikePost(Long postId, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to unlike this post
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to unlike this post."));
        }
        
        // Check if user has liked this post
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        if (!existingLike.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: You have not liked this post."));
        }
        
        // Remove like
        likeRepository.delete(existingLike.get());
        
        // Update like count in response
        long likesCount = likeRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse("Post unliked successfully! Total likes: " + likesCount));
    }
    
    /**
     * Check if user has liked a post
     */
    public ResponseEntity<?> checkLikeStatus(Long postId, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
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
        
        boolean hasLiked = likeRepository.existsByUserAndPost(user, post);
        long likesCount = likeRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse("Liked: " + hasLiked + ", Total likes: " + likesCount));
    }
    
    /**
     * Get users who liked a post
     */
    public ResponseEntity<?> getUsersWhoLikedPost(Long postId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post's likes
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view likes for this post."));
        }
        
        List<User> usersWhoLiked = likeRepository.findUsersByPost(post);
        
        return ResponseEntity.ok(usersWhoLiked);
    }
    
    /**
     * Get posts liked by current user
     */
    public ResponseEntity<?> getPostsLikedByCurrentUser(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        List<Post> likedPosts = likeRepository.findPostsByUser(user);
        
        // Filter out posts that the user doesn't have permission to view
        likedPosts = likedPosts.stream()
                .filter(post -> {
                    boolean isOwner = post.getUser().getId().equals(currentUserId);
                    boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
                    boolean isPostVisible = !post.isPrivate() || isOwner || isFriend;
                    boolean isUserVisible = !post.getUser().isPrivateProfile() || isOwner || isFriend;
                    
                    return isPostVisible && isUserVisible;
                })
                .toList();
        
        return ResponseEntity.ok(likedPosts);
    }
    
    /**
     * Get the count of likes for a post
     */
    public ResponseEntity<?> getLikeCount(Long postId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Error: Post not found."));
        
        // Check if user has permission to view this post's likes
        boolean isOwner = post.getUser().getId().equals(currentUserId);
        boolean isFriend = friendshipService.areFriends(currentUserId, post.getUser().getId());
        
        if ((post.isPrivate() || post.getUser().isPrivateProfile()) && !isOwner && !isFriend) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: You don't have permission to view likes for this post."));
        }
        
        long likesCount = likeRepository.countByPost(post);
        
        return ResponseEntity.ok(new MessageResponse(String.valueOf(likesCount)));
    }
} 