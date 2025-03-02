package com.backend.gjejpune.demo.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    
    @NotBlank
    @Size(max = 500)
    private String content;
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
} 