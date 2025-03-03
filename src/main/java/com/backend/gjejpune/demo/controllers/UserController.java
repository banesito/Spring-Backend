package com.backend.gjejpune.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.ChangePasswordRequest;
import com.backend.gjejpune.demo.payload.request.UpdateProfileRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.security.services.UserDetailsImpl;
import com.backend.gjejpune.demo.service.UserService;
import com.backend.gjejpune.demo.exception.ForbiddenProfileException;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/my-profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        User user = userService.getCurrentUserProfile(currentUserId);
        
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        User user = userService.getUserProfileById(userId, currentUserId);
        return ResponseEntity.ok(user);
    }
    
    @PatchMapping("/my-profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        String message = userService.updateUserProfile(updateProfileRequest, currentUserId);
        
        return ResponseEntity.ok(new MessageResponse(message));
    }
    
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        String message = userService.updateUserProfileWithImage(file, currentUserId);
        
        return ResponseEntity.ok(new MessageResponse(message));
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        return ResponseEntity.ok(userService.searchUsers(query, currentUserId));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        
        userService.changePassword(request.getCurrentPassword(), request.getNewPassword(), currentUserId);
        
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
} 