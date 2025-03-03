package com.backend.gjejpune.demo.payload.response;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String fullName;
    private String bio;
    private String profileImageUrl;
    private boolean privateProfile;
    private boolean isCurrentUser;

    public UserProfileResponse(Long id, String username, String fullName, String bio, 
                              String profileImageUrl, boolean privateProfile, boolean isCurrentUser) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.privateProfile = privateProfile;
        this.isCurrentUser = isCurrentUser;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(boolean privateProfile) {
        this.privateProfile = privateProfile;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean isCurrentUser) {
        this.isCurrentUser = isCurrentUser;
    }
} 