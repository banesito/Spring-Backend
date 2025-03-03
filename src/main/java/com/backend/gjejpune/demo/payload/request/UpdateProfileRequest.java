package com.backend.gjejpune.demo.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @Size(max = 20)
    private String phoneNumber;
    
    private String avatarUrl;
    
    @Size(max = 100)
    private String fullName;
    
    private String bio;
    
    @JsonProperty("isPrivateProfile")
    private Boolean isPrivateProfile;
    
    private boolean isPrivateProfileExplicitlySet = false;
    
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
    
    @JsonProperty("isPrivateProfile")
    public Boolean getIsPrivateProfile() {
        return isPrivateProfile;
    }
    
    @JsonProperty("isPrivateProfile")
    public void setIsPrivateProfile(Boolean isPrivateProfile) {
        this.isPrivateProfile = isPrivateProfile;
        this.isPrivateProfileExplicitlySet = true;
    }
    
    public boolean isPrivateProfileExplicitlySet() {
        return isPrivateProfileExplicitlySet;
    }
    
    // Alias for compatibility with refactored code
    public Boolean getPrivateProfile() {
        return isPrivateProfile;
    }
} 