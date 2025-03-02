package com.backend.gjejpune.demo.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "friendships", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
    })
public class Friendship {
    
    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id")
    private User requester;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "addressee_id")
    private User addressee;
    
    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    public Friendship() {
    }
    
    public Friendship(User requester, User addressee) {
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getRequester() {
        return requester;
    }
    
    public void setRequester(User requester) {
        this.requester = requester;
    }
    
    public User getAddressee() {
        return addressee;
    }
    
    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }
    
    public FriendshipStatus getStatus() {
        return status;
    }
    
    public void setStatus(FriendshipStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
} 