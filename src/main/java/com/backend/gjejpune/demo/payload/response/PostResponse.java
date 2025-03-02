package com.backend.gjejpune.demo.payload.response;

import com.backend.gjejpune.demo.model.Post;

public class PostResponse {
    private String message;
    private Post post;

    public PostResponse() {
    }

    public PostResponse(String message, Post post) {
        this.message = message;
        this.post = post;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
} 