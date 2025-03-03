package com.backend.gjejpune.demo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.payload.request.UpdateProfileRequest;
import com.backend.gjejpune.demo.payload.response.MessageResponse;
import com.backend.gjejpune.demo.payload.response.UserProfileResponse;
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
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * Get current user profile
     */
    public ResponseEntity<?> getCurrentUserProfile(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        UserProfileResponse profileResponse = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.isPrivateProfile(),
                true // isCurrentUser
        );

        return ResponseEntity.ok(profileResponse);
    }
    
    /**
     * Get user profile by ID
     */
    public ResponseEntity<?> getUserProfileById(Long userId, Long currentUserId) {
        // Check if the requested user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        boolean isCurrentUser = userId.equals(currentUserId);

        // If not current user and profile is private, check if they are friends
        if (!isCurrentUser && user.isPrivateProfile()) {
            if (!permissionService.canAccessUserProfile(user, currentUserId)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("This profile is private."));
            }
        }

        UserProfileResponse profileResponse = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.isPrivateProfile(),
                isCurrentUser
        );

        return ResponseEntity.ok(profileResponse);
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public ResponseEntity<?> updateUserProfile(UpdateProfileRequest updateProfileRequest, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Update user profile fields
        if (updateProfileRequest.getFullName() != null) {
            user.setFullName(updateProfileRequest.getFullName());
        }

        if (updateProfileRequest.getBio() != null) {
            user.setBio(updateProfileRequest.getBio());
        }

        if (updateProfileRequest.getPrivateProfile() != null) {
            user.setPrivateProfile(updateProfileRequest.getPrivateProfile());
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Profile updated successfully!"));
    }
    
    /**
     * Update user profile with image
     */
    @Transactional
    public ResponseEntity<?> updateUserProfileWithImage(
            String fullName,
            String bio,
            Boolean privateProfile,
            MultipartFile profileImage,
            Long currentUserId) {

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Update user profile fields
        if (fullName != null) {
            user.setFullName(fullName);
        }

        if (bio != null) {
            user.setBio(bio);
        }

        if (privateProfile != null) {
            user.setPrivateProfile(privateProfile);
        }

        // Handle profile image upload if provided
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // Validate image type
                String contentType = profileImage.getContentType();
                if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Error: Only JPEG, JPG and PNG images are allowed."));
                }

                // Save the image and update the user's profile image URL
                String imageUrl = fileStorageService.storeFile(profileImage, "profile_images");
                user.setProfileImageUrl(imageUrl);
            } catch (RuntimeException e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error: Could not upload profile image."));
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Profile updated successfully!"));
    }
    
    /**
     * Change user password
     */
    @Transactional
    public ResponseEntity<?> changePassword(String currentPassword, String newPassword, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: Current password is incorrect."));
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
    }
    
    /**
     * Search users by username or full name
     */
    public ResponseEntity<?> searchUsers(String query, Long currentUserId) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: Search query cannot be empty."));
        }

        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query);
        List<UserProfileResponse> userProfiles = new ArrayList<>();

        for (User user : users) {
            boolean isCurrentUser = user.getId().equals(currentUserId);
            
            // Skip private profiles that the current user cannot access
            if (!isCurrentUser && user.isPrivateProfile() && !permissionService.canAccessUserProfile(user, currentUserId)) {
                continue;
            }

            UserProfileResponse profileResponse = new UserProfileResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getBio(),
                    user.getProfileImageUrl(),
                    user.isPrivateProfile(),
                    isCurrentUser
            );
            userProfiles.add(profileResponse);
        }

        return ResponseEntity.ok(userProfiles);
    }
} 