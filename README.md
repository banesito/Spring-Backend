# Spring Boot Authentication REST API

This project is a RESTful API built with Spring Boot that provides authentication functionality including user registration, login, and profile retrieval.

## Features

- User registration
- User authentication using JWT (JSON Web Tokens)
- User profile retrieval and updates
- Post management (create, read, update, delete)
- Privacy settings for users and posts
- Pagination for post endpoints (30 posts per page)
- Comprehensive API responses with both status messages and data
- User avatars (auto-generated or custom) with file upload support
- Image support for posts with file upload support
- Hot reloading for faster development

## Technologies Used

- Spring Boot 3.4.3
- Spring Security
- Spring Data JPA
- MySQL Database
- JWT Authentication
- Hibernate Validation
- Spring Boot DevTools

## API Endpoints

### Authentication

- **POST /api/auth/sign-up**: Register a new user
  - Request Body:
    ```json
    {
      "username": "username",
      "email": "email@example.com",
      "password": "password"
    }
    ```
  - Response:
    ```json
    {
      "message": "User registered successfully!"
    }
    ```

- **POST /api/auth/sign-in**: Authenticate a user
  - Request Body:
    ```json
    {
      "username": "username",
      "password": "password"
    }
    ```
  - Response:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "type": "Bearer",
      "id": 1,
      "username": "username",
      "email": "email@example.com",
      "roles": ["USER"]
    }
    ```

### User Profile

- **GET /api/user/my-profile**: Get the authenticated user's profile
  - Headers:
    ```
    Authorization: Bearer {jwt_token}
    ```
  - Response:
    ```json
    {
      "id": 1,
      "username": "username",
      "email": "email@example.com",
      "phoneNumber": null,
      "avatarUrl": "https://ui-avatars.com/api/?name=username&background=random",
      "privateProfile": false
    }
    ```

- **GET /api/user/{userId}**: Get another user's profile (respects privacy settings)
  - Headers:
    ```
    Authorization: Bearer {jwt_token}
    ```
  - Response:
    ```json
    {
      "id": 2,
      "username": "another_user",
      "email": "another@example.com",
      "phoneNumber": null,
      "avatarUrl": "https://ui-avatars.com/api/?name=another_user&background=random",
      "privateProfile": false
    }
    ```
  - If the user has a private profile, you'll receive a 403 Forbidden response.

- **PATCH /api/user/my-profile**: Update the authenticated user's profile
  - Headers:
    ```
    Authorization: Bearer {jwt_token}
    ```
  - Request Body:
    ```json
    {
      "phoneNumber": "+1234567890",
      "avatarUrl": "https://example.com/my-avatar.jpg",
      "isPrivateProfile": true
    }
    ```
  - Response:
    ```json
    {
      "message": "Profile updated successfully! Your avatar has been updated. Your profile is now private. Only you can see your posts."
    }
    ```
  - Note: If you don't provide an `avatarUrl`, a default avatar will be auto-generated based on your username.

- **POST /api/user/avatar**: Upload a new avatar image
  - Headers:
    ```
    Authorization: Bearer {jwt_token}
    ```
  - Request Body (multipart/form-data):
    ```
    file: [image file]
    ```
  - Response:
    ```json
    {
      "message": "Avatar uploaded successfully! Your profile has been updated."
    }
    ```
  - Note: Only image files are accepted. The uploaded image will replace any existing avatar.

### Posts

#### Get all posts
```
GET http://localhost:8080/api/posts?page=0&size=30
Authorization: Bearer your_jwt_token_here
```
Note: 
- This endpoint respects privacy settings. It will only return posts that are:
  - Not private, AND
  - From users who don't have a private profile OR are the current user
- Pagination parameters:
  - `page`: Page number (zero-based, default: 0)
  - `size`: Number of posts per page (default and max: 30)
- Response includes pagination metadata:
  ```json
  {
    "content": [
      {
        "id": 1,
        "title": "Post Title",
        "content": "Post content...",
        "createdAt": "2023-07-01T12:00:00",
        "updatedAt": "2023-07-01T12:00:00",
        "isPrivate": false,
        "user": {
          "id": 1,
          "username": "username",
          "email": "email@example.com"
        }
      }
    ],
    "page": 0,
    "size": 30,
    "totalElements": 100,
    "totalPages": 4,
    "last": false
  }
  ```

#### Get a specific post
```
GET http://localhost:8080/api/posts/{id}
Authorization: Bearer your_jwt_token_here
```
Note: This endpoint respects privacy settings. You can only view a post if:
- The post is not private OR you are the owner
- The post's owner doesn't have a private profile OR you are the owner

#### Get current user's posts
```
GET http://localhost:8080/api/posts/my-posts?page=0&size=30
Authorization: Bearer your_jwt_token_here
```
Note: 
- Pagination parameters:
  - `page`: Page number (zero-based, default: 0)
  - `size`: Number of posts per page (default and max: 30)

#### Get posts by user ID
```
GET http://localhost:8080/api/posts/user/{userId}?page=0&size=30
Authorization: Bearer your_jwt_token_here
```
Note: 
- This endpoint respects privacy settings. You can only view another user's posts if:
  - The user doesn't have a private profile
  - The posts are not private
- Pagination parameters:
  - `page`: Page number (zero-based, default: 0)
  - `size`: Number of posts per page (default and max: 30)

#### Create a new post
```
POST http://localhost:8080/api/posts
Content-Type: application/json
Authorization: Bearer your_jwt_token_here

{
  "title": "My First Post",
  "content": "This is the content of my first post.",
  "imageUrl": "https://example.com/my-image.jpg",
  "isPrivate": false
}
```
Response:
```json
{
  "message": "Post created successfully and set to public. Anyone can view it (unless your profile is private).",
  "post": {
    "id": 1,
    "title": "My First Post",
    "content": "This is the content of my first post.",
    "imageUrl": "https://example.com/my-image.jpg",
    "createdAt": "2023-07-01T12:00:00",
    "updatedAt": "2023-07-01T12:00:00",
    "isPrivate": false,
    "user": {
      "id": 1,
      "username": "username",
      "email": "email@example.com",
      "avatarUrl": "https://ui-avatars.com/api/?name=username&background=random"
    }
  }
}
```
Note: The `imageUrl` field is optional. If not provided, the post will be created without an image.

#### Create a new post with image upload
```
POST http://localhost:8080/api/posts
Content-Type: multipart/form-data
Authorization: Bearer your_jwt_token_here

title: My First Post
content: This is the content of my first post.
isPrivate: false
image: [image file]
```
Response:
```json
{
  "message": "Post created successfully and set to public. Anyone can view it (unless your profile is private).",
  "post": {
    "id": 1,
    "title": "My First Post",
    "content": "This is the content of my first post.",
    "imageUrl": "http://localhost:8080/api/files/f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg",
    "createdAt": "2023-07-01T12:00:00",
    "updatedAt": "2023-07-01T12:00:00",
    "isPrivate": false,
    "user": {
      "id": 1,
      "username": "username",
      "email": "email@example.com",
      "avatarUrl": "https://ui-avatars.com/api/?name=username&background=random"
    }
  }
}
```
Note: The `image` field is optional. If not provided, the post will be created without an image.

#### Update a post
```
PUT http://localhost:8080/api/posts/{id}
Content-Type: application/json
Authorization: Bearer your_jwt_token_here

{
  "title": "Updated Post Title",
  "content": "This is the updated content of my post.",
  "imageUrl": "https://example.com/updated-image.jpg",
  "isPrivate": true
}
```
Response:
```json
{
  "message": "Post updated successfully and set to private. Only you can view it. Post image has been updated.",
  "post": {
    "id": 1,
    "title": "Updated Post Title",
    "content": "This is the updated content of my post.",
    "imageUrl": "https://example.com/updated-image.jpg",
    "createdAt": "2023-07-01T12:00:00",
    "updatedAt": "2023-07-01T12:30:00",
    "isPrivate": true,
    "user": {
      "id": 1,
      "username": "username",
      "email": "email@example.com",
      "avatarUrl": "https://ui-avatars.com/api/?name=username&background=random"
    }
  }
}
```
Note: You can update just the title, content, and/or image without changing the privacy setting. The privacy setting will only be updated if you explicitly include the `isPrivate` field in your request.

#### Update a post with image upload
```
PUT http://localhost:8080/api/posts/{id}
Content-Type: multipart/form-data
Authorization: Bearer your_jwt_token_here

title: Updated Post Title
content: This is the updated content of my post.
isPrivate: true
image: [image file]
```
Response:
```json
{
  "message": "Post updated successfully and set to private. Only you can view it. Post image has been updated.",
  "post": {
    "id": 1,
    "title": "Updated Post Title",
    "content": "This is the updated content of my post.",
    "imageUrl": "http://localhost:8080/api/files/a1b2c3d4-e5f6-7890-a1b2-c3d4e5f67890.jpg",
    "createdAt": "2023-07-01T12:00:00",
    "updatedAt": "2023-07-01T12:30:00",
    "isPrivate": true,
    "user": {
      "id": 1,
      "username": "username",
      "email": "email@example.com",
      "avatarUrl": "https://ui-avatars.com/api/?name=username&background=random"
    }
  }
}
```
Note: All fields are optional when updating a post. You can update just the title, just the content, just the image, or just the privacy setting. Only the fields you include in your request will be updated, and the rest will remain unchanged.

#### Delete a post
```
DELETE http://localhost:8080/api/posts/{id}
Authorization: Bearer your_jwt_token_here
```
Response:
```json
{
  "message": "Post deleted successfully!"
}
```

## Privacy Features

### User Privacy
- Users can set their profile to private using the `isPrivateProfile` flag
- When a user's profile is private:
  - Other users cannot view their profile details
  - Other users cannot view their posts (even public ones)

### Post Privacy
- Posts can be set to private using the `isPrivate` flag
- Private posts are only visible to their owner
- Even if a user's profile is public, their private posts remain hidden from other users

## Pagination

All post listing endpoints support pagination with the following parameters:
- `page`: The page number (zero-based, default: 0)
- `size`: Number of items per page (default and max: 30)

The response includes pagination metadata:
- `content`: Array of posts
- `page`: Current page number
- `size`: Number of items in the current page
- `totalElements`: Total number of items across all pages
- `totalPages`: Total number of pages
- `last`: Whether this is the last page

## Response Format

The API provides consistent and informative responses:

### Success Responses
- **Create/Update Operations**: Return both a success message and the created/updated resource
- **List Operations**: Return paginated data with metadata
- **Delete Operations**: Return a success message

### Error Responses
- **Validation Errors**: Return field-specific error messages
- **Authentication Errors**: Return appropriate 401/403 status codes with messages
- **Not Found Errors**: Return 404 status code with descriptive message

## Setup and Installation

1. Clone the repository
2. Configure MySQL database in `application.properties`
3. Run the application using Gradle:
   ```
   ./gradlew bootRun
   ```
   or on Windows:
   ```
   .\gradlew.bat bootRun
   ```
4. The application will start on port 8080

## Development with Hot Reloading

This project uses Spring Boot DevTools for hot reloading, which means you don't need to restart the application after making changes to the code.

1. Start the application with:
   ```
   ./gradlew bootRun
   ```
   or on Windows:
   ```
   .\gradlew.bat bootRun
   ```

2. Make changes to your Java files or resources
3. DevTools will automatically detect changes and restart the application

Note: Some changes like adding new dependencies or modifying configuration files may still require a manual restart.

## Database Configuration

The application uses MySQL. You can configure the database connection in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
```

## Security

- JWT token expiration is set to 24 hours by default
- Passwords are encrypted using BCrypt
- Sensitive information like passwords are excluded from API responses 

## Avatar Features

### User Avatars
- Users automatically receive a generated avatar based on their username when they register
- The default avatar is created using the UI Avatars API (https://ui-avatars.com)
- Users can update their avatar by:
  - Providing a custom URL in their profile update
  - Uploading an image file via the dedicated avatar upload endpoint
- If a user doesn't provide an avatar URL during profile update, the system will keep their existing avatar or generate a default one if none exists

### Post Images
- Posts can include an optional image
- Images can be added when creating a post or updated later
- Images can be provided as:
  - A URL in the JSON request
  - An uploaded file using multipart/form-data
- The API returns the image URL as part of the post data in all responses

## File Storage

The application stores uploaded files in the `./uploads` directory by default. This can be configured in the `application.properties` file:

```properties
file.upload-dir=./uploads
```

Uploaded files are:
- Renamed with a UUID to prevent filename conflicts
- Accessible via the `/api/files/{filename}` endpoint
- Limited to 10MB in size by default

File upload limits can be configured in the `application.properties` file:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB
``` 