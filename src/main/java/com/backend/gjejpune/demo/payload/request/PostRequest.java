package com.backend.gjejpune.demo.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostRequest {
    
    @NotBlank
    @Size(max = 100)
    private String title;
    
    @NotBlank
    private String content;
    
    private String imageUrl;
    
    @JsonProperty("isPrivate")
    private boolean isPrivate = false;
    
    private boolean isPrivateExplicitlySet = false;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    @JsonProperty("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }
    
    @JsonProperty("isPrivate")
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        this.isPrivateExplicitlySet = true;
    }
    
    public boolean isPrivateExplicitlySet() {
        return isPrivateExplicitlySet;
    }
} 