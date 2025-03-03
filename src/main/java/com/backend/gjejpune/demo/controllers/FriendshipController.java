package com.backend.gjejpune.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.gjejpune.demo.model.Friendship;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.FriendshipService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {
    
    @Autowired
    private FriendshipService friendshipService;
    
    /**
     * Send a friend request to another user
     */
    @PostMapping("/{userId}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        // Prevent sending friend request to self
        if (currentUserId.equals(userId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You cannot send a friend request to yourself."));
        }
        
        try {
            friendshipService.sendFriendRequest(currentUserId, userId);
            return ResponseEntity.ok(new MessageResponse("Friend request sent successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    /**
     * Accept a friend request
     */
    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long friendshipId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        try {
            friendshipService.acceptFriendRequest(friendshipId, currentUserId);
            return ResponseEntity.ok(new MessageResponse("Friend request accepted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    /**
     * Reject a friend request
     */
    @PutMapping("/{friendshipId}/reject")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long friendshipId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        try {
            friendshipService.rejectFriendRequest(friendshipId, currentUserId);
            return ResponseEntity.ok(new MessageResponse("Friend request rejected successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    /**
     * Cancel a friend request
     */
    @DeleteMapping("/{friendshipId}/cancel")
    public ResponseEntity<?> cancelFriendRequest(@PathVariable Long friendshipId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        try {
            friendshipService.cancelFriendRequest(friendshipId, currentUserId);
            return ResponseEntity.ok(new MessageResponse("Friend request cancelled successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    /**
     * Unfriend a user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfriend(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        try {
            friendshipService.unfriend(currentUserId, userId);
            return ResponseEntity.ok(new MessageResponse("Unfriended successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
    
    /**
     * Get all friend requests received by the current user
     */
    @GetMapping("/requests/received")
    public ResponseEntity<?> getFriendRequestsReceived() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        List<Friendship> friendRequests = friendshipService.getFriendRequestsReceived(currentUserId);
        return ResponseEntity.ok(friendRequests);
    }
    
    /**
     * Get all friend requests sent by the current user
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<?> getFriendRequestsSent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        List<Friendship> friendRequests = friendshipService.getFriendRequestsSent(currentUserId);
        return ResponseEntity.ok(friendRequests);
    }
    
    /**
     * Get all friends of the current user
     */
    @GetMapping("/friends")
    public ResponseEntity<?> getFriends() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        List<User> friends = friendshipService.getFriends(currentUserId);
        return ResponseEntity.ok(friends);
    }
    
    /**
     * Check friendship status with another user
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getFriendshipStatus(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        String status = friendshipService.getFriendshipStatus(currentUserId, userId);
        return ResponseEntity.ok(new MessageResponse(status));
    }
} 