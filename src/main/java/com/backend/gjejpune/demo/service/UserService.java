package com.backend.gjejpune.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.UpdateProfileRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.payload.response.UserResponse;
import com.backend.gjejpune.demo.repository.UserRepository;
import com.backend.gjejpune.demo.exception.ForbiddenProfileException;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendshipService friendshipService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Get current user profile
     */
    public User getCurrentUserProfile(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
    }
    
    /**
     * Get user profile by ID
     */
    public User getUserProfileById(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Check if the user has a private profile and is not the current user or a friend
        boolean isFriend = friendshipService.areFriends(currentUserId, userId);
        if (user.isPrivateProfile() && !userId.equals(currentUserId) && !isFriend) {
            throw new ForbiddenProfileException("Error: This user has a private profile.");
        }
        
        return user;
    }
    
    /**
     * Update user profile
     */
    public String updateUserProfile(UpdateProfileRequest updateProfileRequest, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        boolean privacyChanged = false;
        boolean newPrivacyValue = false;
        boolean avatarChanged = false;
        
        // Update phone number if provided
        if (updateProfileRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateProfileRequest.getPhoneNumber());
        }
        
        // Update avatar if provided
        if (updateProfileRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateProfileRequest.getAvatarUrl());
            avatarChanged = true;
        }
        
        // Update privacy setting only if explicitly provided in the request
        if (updateProfileRequest.isPrivateProfileExplicitlySet()) {
            boolean oldValue = user.isPrivateProfile();
            boolean newValue = updateProfileRequest.getIsPrivateProfile();
            
            if (oldValue != newValue) {
                privacyChanged = true;
                newPrivacyValue = newValue;
                user.setPrivateProfile(newValue);
            }
        }
        
        userRepository.save(user);
        
        String message = "Profile updated successfully!";
        if (avatarChanged) {
            message += " Your avatar has been updated.";
        }
        if (privacyChanged) {
            if (newPrivacyValue) {
                message += " Your profile is now private. Only you can see your posts.";
            } else {
                message += " Your profile is now public. Others can see your public posts.";
            }
        }
        
        return message;
    }
    
    /**
     * Update user profile with image upload
     */
    public String updateUserProfileWithImage(MultipartFile file, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Check if file is an image
        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Error: Only image files are allowed for avatars.");
        }
        
        // Store the file and get the URL
        String fileUrl = fileStorageService.storeFile(file);
        
        // Update user's avatar URL
        user.setAvatarUrl(fileUrl);
        userRepository.save(user);
        
        return "Avatar uploaded successfully! Your profile has been updated.";
    }
    
    /**
     * Change user password
     */
    public void changePassword(String currentPassword, String newPassword, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Error: Current password is incorrect.");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * Search users by username or full name
     */
    public List<User> searchUsers(String query, Long currentUserId) {
        // This is a simplified implementation - you'll need to adjust based on your actual UserRepository methods
        List<User> users = userRepository.findAll().stream()
                .filter(user -> 
                    user.getUsername().contains(query) || 
                    (user.getPhoneNumber() != null && user.getPhoneNumber().contains(query)))
                .collect(Collectors.toList());
        
        // Filter out users with private profiles who are not friends with the current user
        return users.stream()
                .filter(user -> {
                    if (!user.isPrivateProfile()) {
                        return true;
                    }
                    
                    if (user.getId().equals(currentUserId)) {
                        return true;
                    }
                    
                    return friendshipService.areFriends(currentUserId, user.getId());
                })
                .collect(Collectors.toList());
    }
} 