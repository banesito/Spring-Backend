package com.backend.gjejpune.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.gjejpune.demo.model.Friendship;
import com.backend.gjejpune.demo.model.Friendship.FriendshipStatus;
import com.backend.gjejpune.demo.model.User;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    // Find friendship between two users (in either direction)
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :user1 AND f.addressee = :user2) OR " +
           "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(
            @Param("user1") User user1, 
            @Param("user2") User user2);
    
    // Find all friendships where user is either requester or addressee
    @Query("SELECT f FROM Friendship f WHERE " +
           "f.requester = :user OR f.addressee = :user")
    List<Friendship> findAllFriendshipsForUser(@Param("user") User user);
    
    // Find all accepted friendships for a user
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :user OR f.addressee = :user) AND " +
           "f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendshipsForUser(@Param("user") User user);
    
    // Find all pending friend requests sent by user
    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);
    
    // Find all pending friend requests received by user
    List<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status);
} 