package com.backend.gjejpune.demo.payload.response;

import com.backend.gjejpune.demo.model.User;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private boolean privateProfile;
    private String fullName;
    private String bio;
    private String location;
    private String website;
    private String profileImageUrl;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.avatarUrl = user.getAvatarUrl();
        this.privateProfile = user.isPrivateProfile();
        
        // These fields might not exist in the current User model
        // Uncomment them if they are available
        /*
        this.fullName = user.getFullName();
        this.bio = user.getBio();
        this.location = user.getLocation();
        this.website = user.getWebsite();
        this.profileImageUrl = user.getProfileImageUrl();
        */
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(boolean privateProfile) {
        this.privateProfile = privateProfile;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
} 