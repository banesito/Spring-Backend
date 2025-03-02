# Testing Profile Visibility with Friendship Status

This guide outlines how to test the updated profile visibility feature that allows friends to view private profiles.

## Prerequisites
- The application is running
- Postman or a similar API testing tool is available
- At least two test user accounts are created

## Test Scenario 1: Friend can view private profile

1. **Create two test users (if not already created)**
   - Register User A: `POST http://localhost:8080/api/auth/signup`
   ```json
   {
     "username": "userA",
     "email": "userA@example.com",
     "password": "password123"
   }
   ```
   - Register User B: `POST http://localhost:8080/api/auth/signup`
   ```json
   {
     "username": "userB",
     "email": "userB@example.com",
     "password": "password123"
   }
   ```

2. **Set User B's profile to private**
   - Login as User B: `POST http://localhost:8080/api/auth/signin`
   ```json
   {
     "username": "userB",
     "password": "password123"
   }
   ```
   - Update User B's profile to private: `PATCH http://localhost:8080/api/user/my-profile`
   ```json
   {
     "isPrivateProfile": true
   }
   ```

3. **Verify User A cannot view User B's profile (before friendship)**
   - Login as User A: `POST http://localhost:8080/api/auth/signin`
   ```json
   {
     "username": "userA",
     "password": "password123"
   }
   ```
   - Try to view User B's profile: `GET http://localhost:8080/api/user/{userB_id}`
   - Expected result: 403 Forbidden with message "Error: This user has a private profile."

4. **Create friendship between User A and User B**
   - As User A, send friend request to User B: `POST http://localhost:8080/api/friendships/{userB_id}`
   - Login as User B: `POST http://localhost:8080/api/auth/signin`
   ```json
   {
     "username": "userB",
     "password": "password123"
   }
   ```
   - Get friend requests: `GET http://localhost:8080/api/friendships/requests/received`
   - Accept friend request from User A: `PUT http://localhost:8080/api/friendships/{friendshipId}/accept`

5. **Verify User A can now view User B's profile (after friendship)**
   - Login as User A: `POST http://localhost:8080/api/auth/signin`
   ```json
   {
     "username": "userA",
     "password": "password123"
   }
   ```
   - Try to view User B's profile again: `GET http://localhost:8080/api/user/{userB_id}`
   - Expected result: 200 OK with User B's profile information

## Test Scenario 2: Unfriending revokes access to private profile

1. **Unfriend User B from User A**
   - As User A, unfriend User B: `DELETE http://localhost:8080/api/friendships/{userB_id}`

2. **Verify User A cannot view User B's profile again**
   - Try to view User B's profile: `GET http://localhost:8080/api/user/{userB_id}`
   - Expected result: 403 Forbidden with message "Error: This user has a private profile."

## Notes
- Replace `{userB_id}` with the actual user ID of User B
- Replace `{friendshipId}` with the actual friendship ID from the received friend requests
- Make sure to include the JWT token in the Authorization header for authenticated requests 