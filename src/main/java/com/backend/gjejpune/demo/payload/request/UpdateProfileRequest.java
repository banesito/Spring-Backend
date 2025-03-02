package com.backend.gjejpune.demo.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @Size(max = 20)
    private String phoneNumber;
    
    private String avatarUrl;
    
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
} 