# Testing Error Handling

This guide outlines how to test the new error handling functionality that provides standardized error responses across the application.

## Method Not Allowed Error

Test that the application returns a proper error message when an HTTP method is not supported for an endpoint.

### Example 1: Using DELETE on a GET-only endpoint

```
DELETE http://localhost:8080/api/user/my-profile
Authorization: Bearer your_jwt_token_here
```

Expected response:
```json
{
  "status": 405,
  "message": "Method DELETE is not supported for this request. Supported methods are: GET, PATCH",
  "path": "/api/user/my-profile",
  "timestamp": "2023-07-01T12:00:00.000Z"
}
```

### Example 2: Using PUT on a POST-only endpoint

```
PUT http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

Expected response:
```json
{
  "status": 405,
  "message": "Method PUT is not supported for this request. Supported methods are: POST",
  "path": "/api/auth/signup",
  "timestamp": "2023-07-01T12:00:00.000Z"
}
```

## Validation Errors

Test that the application returns proper validation error messages.

### Example: Invalid signup request

```
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "username": "",
  "email": "invalid-email",
  "password": "123"
}
```

Expected response:
```json
{
  "status": 400,
  "message": "Validation failed",
  "path": "/api/auth/signup",
  "timestamp": "2023-07-01T12:00:00.000Z",
  "errors": {
    "username": "Username cannot be blank",
    "email": "Must be a valid email address",
    "password": "Password must be at least 6 characters"
  }
}
```

## Missing Parameters

Test that the application returns proper error messages for missing required parameters.

### Example: Missing page parameter

```
GET http://localhost:8080/api/posts?size=10&required_param=
Authorization: Bearer your_jwt_token_here
```

Expected response:
```json
{
  "status": 400,
  "message": "Required parameter 'required_param' of type 'String' is missing",
  "path": "/api/posts",
  "timestamp": "2023-07-01T12:00:00.000Z"
}
```

## Type Mismatch

Test that the application returns proper error messages for parameter type mismatches.

### Example: Invalid ID format

```
GET http://localhost:8080/api/posts/abc
Authorization: Bearer your_jwt_token_here
```

Expected response:
```json
{
  "status": 400,
  "message": "Parameter 'id' should be of type 'Long'",
  "path": "/api/posts/abc",
  "timestamp": "2023-07-01T12:00:00.000Z"
}
```

## Resource Not Found

Test that the application returns proper error messages for non-existent resources.

### Example: Non-existent endpoint

```
GET http://localhost:8080/api/non-existent-endpoint
Authorization: Bearer your_jwt_token_here
```

Expected response:
```json
{
  "status": 404,
  "message": "No handler found for GET /api/non-existent-endpoint",
  "path": "/api/non-existent-endpoint",
  "timestamp": "2023-07-01T12:00:00.000Z"
}
```

## Malformed JSON

Test that the application returns proper error messages for malformed JSON requests.

### Example: Invalid JSON format

```
POST http://localhost:8080/api/posts
Content-Type: application/json
Authorization: Bearer your_jwt_token_here

{
  "title": "Test Post",
  "content": "This is a test post"
  invalid-json
}
```

Expected response:
```json
{
  "status": 400,
  "message": "Malformed JSON request",
  "path": "/api/posts",
  "timestamp": "2023-07-01T12:00:00.000Z"
}
```

## Notes

- Replace `your_jwt_token_here` with a valid JWT token obtained from the login endpoint
- The timestamp in the responses will be the actual time of the request
- The exact error messages may vary slightly depending on the specific validation rules and endpoint configurations 