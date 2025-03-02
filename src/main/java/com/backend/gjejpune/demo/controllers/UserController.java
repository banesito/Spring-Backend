package com.backend.gjejpune.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.UpdateProfileRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.repository.UserRepository;
import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.FileStorageService;
import com.backend.gjejpune.demo.service.FriendshipService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    FileStorageService fileStorageService;
    
    @Autowired
    FriendshipService friendshipService;
    
    @GetMapping("/my-profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // If the user has a private profile and is not the current user, check friendship status
        if (user.isPrivateProfile() && !userId.equals(currentUserId)) {
            // Check if users are friends
            boolean isFriend = friendshipService.areFriends(currentUserId, userId);
            
            // If not friends, return a forbidden response
            if (!isFriend) {
                return ResponseEntity
                        .status(403)
                        .body(new MessageResponse("Error: This user has a private profile."));
            }
        }
        
        return ResponseEntity.ok(user);
    }
    
    @PatchMapping("/my-profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
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
            logger.info("User {} updated their avatar", user.getUsername());
        } else if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            // Auto-generate avatar if not set
            String encodedName = URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8);
            String avatarUrl = "https://ui-avatars.com/api/?name=" + encodedName + "&background=random";
            user.setAvatarUrl(avatarUrl);
            avatarChanged = true;
            logger.info("Auto-generated avatar for user {}", user.getUsername());
        }
        
        // Update privacy setting only if explicitly provided in the request
        if (updateProfileRequest.isPrivateProfileExplicitlySet()) {
            boolean oldValue = user.isPrivateProfile();
            boolean newValue = updateProfileRequest.getIsPrivateProfile();
            
            if (oldValue != newValue) {
                privacyChanged = true;
                newPrivacyValue = newValue;
                user.setPrivateProfile(newValue);
                
                logger.info("User {} changed profile privacy from {} to {}", 
                        user.getUsername(), 
                        oldValue ? "private" : "public", 
                        newValue ? "private" : "public");
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
        
        return ResponseEntity.ok(new MessageResponse(message));
    }
    
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Check if file is an image
        if (!file.getContentType().startsWith("image/")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Only image files are allowed for avatars."));
        }
        
        // Store the file and get the URL
        String fileUrl = fileStorageService.storeFile(file);
        
        // Update user's avatar URL
        user.setAvatarUrl(fileUrl);
        userRepository.save(user);
        
        logger.info("User {} uploaded a new avatar", user.getUsername());
        
        return ResponseEntity.ok(new MessageResponse("Avatar uploaded successfully! Your profile has been updated."));
    }
} 