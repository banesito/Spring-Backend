package com.backend.gjejpune.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.gjejpune.demo.model.Comment;
import com.backend.gjejpune.demo.model.Post;
import com.backend.gjejpune.demo.model.User;

/**
 * Service for centralizing permission checking logic across the application.
 * This helps avoid duplicating the same permission checks in multiple services.
 */
@Service
public class PermissionService {
    
    @Autowired
    private FriendshipService friendshipService;
    
    /**
     * Check if a user can access a post
     * 
     * @param post The post to check access for
     * @param userId The ID of the user trying to access the post
     * @return true if the user can access the post, false otherwise
     */
    public boolean canAccessPost(Post post, Long userId) {
        if (post == null || userId == null) {
            return false;
        }
        
        boolean isOwner = post.getUser().getId().equals(userId);
        boolean isFriend = friendshipService.areFriends(userId, post.getUser().getId());
        
        // User can access if:
        // 1. The post is not private, or
        // 2. The user is the owner of the post, or
        // 3. The user is a friend of the post owner
        boolean isPostAccessible = !post.isPrivate() || isOwner || isFriend;
        
        // User can access if:
        // 1. The post owner's profile is not private, or
        // 2. The user is the owner of the post, or
        // 3. The user is a friend of the post owner
        boolean isUserAccessible = !post.getUser().isPrivateProfile() || isOwner || isFriend;
        
        return isPostAccessible && isUserAccessible;
    }
    
    /**
     * Check if a user can modify a post
     * 
     * @param post The post to check modification permission for
     * @param userId The ID of the user trying to modify the post
     * @return true if the user can modify the post, false otherwise
     */
    public boolean canModifyPost(Post post, Long userId) {
        if (post == null || userId == null) {
            return false;
        }
        
        // Only the post owner can modify it
        return post.getUser().getId().equals(userId);
    }
    
    /**
     * Check if a user can access a comment
     * 
     * @param comment The comment to check access for
     * @param userId The ID of the user trying to access the comment
     * @return true if the user can access the comment, false otherwise
     */
    public boolean canAccessComment(Comment comment, Long userId) {
        if (comment == null || userId == null) {
            return false;
        }
        
        // Comment access depends on post access
        return canAccessPost(comment.getPost(), userId);
    }
    
    /**
     * Check if a user can modify a comment
     * 
     * @param comment The comment to check modification permission for
     * @param userId The ID of the user trying to modify the comment
     * @return true if the user can modify the comment, false otherwise
     */
    public boolean canModifyComment(Comment comment, Long userId) {
        if (comment == null || userId == null) {
            return false;
        }
        
        // Only the comment owner can modify it
        return comment.getUser().getId().equals(userId);
    }
    
    /**
     * Check if a user can delete a comment
     * 
     * @param comment The comment to check deletion permission for
     * @param userId The ID of the user trying to delete the comment
     * @return true if the user can delete the comment, false otherwise
     */
    public boolean canDeleteComment(Comment comment, Long userId) {
        if (comment == null || userId == null) {
            return false;
        }
        
        // The comment owner or the post owner can delete the comment
        boolean isCommentOwner = comment.getUser().getId().equals(userId);
        boolean isPostOwner = comment.getPost().getUser().getId().equals(userId);
        
        return isCommentOwner || isPostOwner;
    }
    
    /**
     * Check if a user can access another user's profile
     * 
     * @param targetUser The user whose profile is being accessed
     * @param userId The ID of the user trying to access the profile
     * @return true if the user can access the profile, false otherwise
     */
    public boolean canAccessUserProfile(User targetUser, Long userId) {
        if (targetUser == null || userId == null) {
            return false;
        }
        
        // If the profile is not private, anyone can access it
        if (!targetUser.isPrivateProfile()) {
            return true;
        }
        
        // Users can always access their own profile
        if (targetUser.getId().equals(userId)) {
            return true;
        }
        
        // Friends can access private profiles
        return friendshipService.areFriends(userId, targetUser.getId());
    }
} 