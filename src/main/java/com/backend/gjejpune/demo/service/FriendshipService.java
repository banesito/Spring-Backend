package com.backend.gjejpune.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.gjejpune.demo.model.Friendship;
import com.backend.gjejpune.demo.model.Friendship.FriendshipStatus;
import com.backend.gjejpune.demo.model.User;
import com.backend.gjejpune.demo.repository.FriendshipRepository;
import com.backend.gjejpune.demo.repository.UserRepository;

@Service
public class FriendshipService {
    
    @Autowired
    private FriendshipRepository friendshipRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Send a friend request from one user to another
     */
    @Transactional
    public Friendship sendFriendRequest(Long requesterId, Long addresseeId) {
        // Check if users exist
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Error: Requester user not found."));
        
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new RuntimeException("Error: Addressee user not found."));
        
        // Check if friendship already exists
        Optional<Friendship> existingFriendship = friendshipRepository.findFriendshipBetweenUsers(requester, addressee);
        
        if (existingFriendship.isPresent()) {
            Friendship friendship = existingFriendship.get();
            FriendshipStatus status = friendship.getStatus();
            
            if (status == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Error: Users are already friends.");
            } else if (status == FriendshipStatus.PENDING) {
                // If the request was from the other user, accept it
                if (friendship.getRequester().getId().equals(addresseeId)) {
                    friendship.setStatus(FriendshipStatus.ACCEPTED);
                    return friendshipRepository.save(friendship);
                } else {
                    throw new RuntimeException("Error: Friend request already sent.");
                }
            } else if (status == FriendshipStatus.REJECTED) {
                // If previously rejected, update to pending
                if (friendship.getRequester().getId().equals(requesterId)) {
                    friendship.setStatus(FriendshipStatus.PENDING);
                    return friendshipRepository.save(friendship);
                } else {
                    // Create new request in the opposite direction
                    Friendship newFriendship = new Friendship(requester, addressee);
                    return friendshipRepository.save(newFriendship);
                }
            }
        }
        
        // Create new friendship
        Friendship friendship = new Friendship(requester, addressee);
        return friendshipRepository.save(friendship);
    }
    
    /**
     * Accept a friend request
     */
    @Transactional
    public Friendship acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Error: Friendship not found."));
        
        // Check if the user is the addressee
        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Error: Only the addressee can accept a friend request.");
        }
        
        // Check if the request is pending
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Error: Friend request is not pending.");
        }
        
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }
    
    /**
     * Reject a friend request
     */
    @Transactional
    public Friendship rejectFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Error: Friendship not found."));
        
        // Check if the user is the addressee
        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Error: Only the addressee can reject a friend request.");
        }
        
        // Check if the request is pending
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Error: Friend request is not pending.");
        }
        
        friendship.setStatus(FriendshipStatus.REJECTED);
        return friendshipRepository.save(friendship);
    }
    
    /**
     * Cancel a friend request
     */
    @Transactional
    public void cancelFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Error: Friendship not found."));
        
        // Check if the user is the requester
        if (!friendship.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Error: Only the requester can cancel a friend request.");
        }
        
        // Check if the request is pending
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Error: Friend request is not pending.");
        }
        
        friendshipRepository.delete(friendship);
    }
    
    /**
     * Unfriend a user
     */
    @Transactional
    public void unfriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Error: Friend not found."));
        
        // Check if they are friends
        Optional<Friendship> existingFriendship = friendshipRepository.findFriendshipBetweenUsers(user, friend);
        
        if (existingFriendship.isPresent()) {
            Friendship friendship = existingFriendship.get();
            
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                friendshipRepository.delete(friendship);
            } else {
                throw new RuntimeException("Error: Users are not friends.");
            }
        } else {
            throw new RuntimeException("Error: Users are not friends.");
        }
    }
    
    /**
     * Get all friend requests received by a user
     */
    public List<Friendship> getFriendRequestsReceived(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        return friendshipRepository.findByAddresseeAndStatus(user, FriendshipStatus.PENDING);
    }
    
    /**
     * Get all friend requests sent by a user
     */
    public List<Friendship> getFriendRequestsSent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        return friendshipRepository.findByRequesterAndStatus(user, FriendshipStatus.PENDING);
    }
    
    /**
     * Get all friends of a user
     */
    public List<User> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        // Get friendships where user is requester and status is accepted
        List<User> friendsAsRequester = friendshipRepository.findByRequesterAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendship::getAddressee)
                .collect(Collectors.toList());
        
        // Get friendships where user is addressee and status is accepted
        List<User> friendsAsAddressee = friendshipRepository.findByAddresseeAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendship::getRequester)
                .collect(Collectors.toList());
        
        // Combine both lists
        friendsAsRequester.addAll(friendsAsAddressee);
        return friendsAsRequester;
    }
    
    /**
     * Get IDs of all friends of a user
     */
    public List<Long> getFriendIds(Long userId) {
        return getFriends(userId).stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if two users are friends
     */
    public boolean areFriends(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("Error: User 1 not found."));
        
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("Error: User 2 not found."));
        
        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetweenUsers(user1, user2);
        
        return friendship.isPresent() && friendship.get().getStatus() == FriendshipStatus.ACCEPTED;
    }
    
    /**
     * Get friendship status between two users
     */
    public String getFriendshipStatus(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("Error: User 1 not found."));
        
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("Error: User 2 not found."));
        
        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetweenUsers(user1, user2);
        
        if (friendship.isPresent()) {
            Friendship f = friendship.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                return "FRIENDS";
            } else if (f.getStatus() == FriendshipStatus.PENDING) {
                if (f.getRequester().getId().equals(userId1)) {
                    return "REQUEST_SENT";
                } else {
                    return "REQUEST_RECEIVED";
                }
            } else {
                return "NOT_FRIENDS";
            }
        } else {
            return "NOT_FRIENDS";
        }
    }
} 