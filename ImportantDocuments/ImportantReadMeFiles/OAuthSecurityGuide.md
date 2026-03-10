# OAuth & OAuth2 – Complete Guide (Beginner to Advanced)

## Table of Contents

1. Introduction
2. What is Authentication vs Authorization
3. What is OAuth
4. What is OAuth2
5. Why OAuth is Needed
6. OAuth Terminology
7. OAuth Roles
8. OAuth 2.0 Authorization Flow (Diagram)
9. OAuth 2.0 Grant Types
10. OAuth vs OAuth2 Comparison
11. OAuth2 Tokens Explained
12. OAuth2 Flow Step-by-Step
13. Example OAuth2 Request
14. OAuth2 in Real World Applications
15. Implementing OAuth2 in Applications
16. Testing OAuth2 APIs
17. Automating OAuth2 in Automation Frameworks
18. OAuth2 Security Best Practices
19. Common OAuth2 Errors
20. Tools to Test OAuth
21. Interview Questions
22. Official References

---

# 1. Introduction

OAuth is one of the most widely used **authorization frameworks** in modern applications.

Many applications today allow users to **login using Google, Facebook, GitHub, or Microsoft**.

Example:

```
Login with Google
Login with Facebook
Login with GitHub
```

Behind the scenes, these login systems work using **OAuth2 protocol**.

OAuth helps applications **access resources on behalf of users without exposing their passwords**.

---

# 2. Authentication vs Authorization

| Concept        | Meaning                    | Example                       |
| -------------- | -------------------------- | ----------------------------- |
| Authentication | Verifying identity of user | Login using username/password |
| Authorization  | Checking permissions       | Accessing admin dashboard     |

Example:

```
Authentication → Who are you?
Authorization → What are you allowed to do?
```

Example in banking app:

| Step           | Example                          |
| -------------- | -------------------------------- |
| Authentication | Login using username/password    |
| Authorization  | Check if user can transfer money |

OAuth mainly focuses on **Authorization**.

---

# 3. What is OAuth

OAuth stands for:

```
Open Authorization
```

OAuth allows an application to **access user resources from another application without sharing password**.

Example:

You use **Spotify** and click:

```
Login with Google
```

Spotify does **NOT ask for your Google password**.

Instead:

1. Spotify redirects you to Google
2. Google asks for permission
3. Google sends a token to Spotify
4. Spotify uses that token to access user data

---

# 4. What is OAuth2

OAuth2 is an **improved version of OAuth 1.0**.

OAuth 2.0 is:

* Simpler
* Faster
* Token based
* More flexible
* Used by modern APIs

Examples of APIs using OAuth2:

| Platform       | Uses OAuth2 |
| -------------- | ----------- |
| Google APIs    | Yes         |
| Facebook APIs  | Yes         |
| GitHub APIs    | Yes         |
| Microsoft APIs | Yes         |
| AWS APIs       | Yes         |

---

# 5. Why OAuth is Needed

Before OAuth:

```
User shares password with third party apps
```

Problems:

* Security risk
* Password leakage
* Hard to revoke access

OAuth solves this by:

```
Token based access
```

Benefits:

| Feature             | Benefit                       |
| ------------------- | ----------------------------- |
| No password sharing | Improved security             |
| Limited access      | Apps only access allowed data |
| Revocable           | Access can be revoked anytime |
| Scalable            | Works with APIs               |

---

# 6. OAuth Terminology

Important OAuth terms:

| Term                 | Meaning                                  |
| -------------------- | ---------------------------------------- |
| Resource Owner       | The user                                 |
| Client               | Application requesting access            |
| Authorization Server | Server issuing tokens                    |
| Resource Server      | API holding user data                    |
| Access Token         | Token used to access APIs                |
| Refresh Token        | Token used to generate new access tokens |

Example:

```
User → logs into Google
Spotify → Client
Google Auth Server → Authorization Server
Google APIs → Resource Server
```

---

# 7. OAuth Roles

OAuth defines **four roles**.

| Role                 | Description                   |
| -------------------- | ----------------------------- |
| Resource Owner       | User who owns the data        |
| Client               | Application requesting access |
| Authorization Server | Issues access tokens          |
| Resource Server      | API containing resources      |

---

# 8. OAuth2 Authorization Flow (Diagram)

```
+--------+                                +---------------+
|  User  |                                | Authorization |
|        |                                |    Server     |
+--------+                                +---------------+
     |                                             |
     | 1. Request Authorization                    |
     |-------------------------------------------->|
     |                                             |
     | 2. User Login + Permission                  |
     |                                             |
     |<--------------------------------------------|
     |                                             |
     | 3. Authorization Code                       |
     |-------------------------------------------->|
     |                                             |
     | 4. Access Token                             |
     |<--------------------------------------------|
     |
     |
+------------+                         +------------------+
|   Client   |                         |  Resource Server |
+------------+                         +------------------+
         |                                     |
         | 5. API Request with Access Token    |
         |------------------------------------>|
         |                                     |
         | 6. Resource Response                |
         |<------------------------------------|
```

---

# 9. OAuth2 Grant Types

OAuth2 provides different **authorization flows** called **Grant Types**.

| Grant Type              | Description              | Use Case         |
| ----------------------- | ------------------------ | ---------------- |
| Authorization Code      | Most secure flow         | Web applications |
| Implicit                | Token returned directly  | Browser apps     |
| Client Credentials      | App-to-App communication | Backend services |
| Resource Owner Password | User credentials used    | Legacy apps      |
| Refresh Token           | Generate new tokens      | Long sessions    |
| Device Code             | For TV / IoT devices     | Smart devices    |

---

# 10. OAuth vs OAuth2 Comparison

| Feature        | OAuth 1.0       | OAuth 2.0     |
| -------------- | --------------- | ------------- |
| Protocol       | Complex         | Simpler       |
| Token Type     | Signature based | Bearer tokens |
| Mobile Support | Limited         | Excellent     |
| Implementation | Difficult       | Easy          |
| Adoption       | Less            | Very high     |

OAuth2 is the **industry standard today**.

---

# 11. OAuth2 Tokens Explained

OAuth2 mainly uses **two types of tokens**.

## Access Token

Used to access APIs.

Example:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
```

Access tokens usually expire in:

```
15 minutes to 1 hour
```

---

## Refresh Token

Used to generate a new access token.

Example flow:

```
Access Token expired
↓
Use Refresh Token
↓
Get new Access Token
```

---

# 12. OAuth2 Flow Step-by-Step

Step 1

Client redirects user to authorization server.

```
https://auth.server.com/authorize
```

Step 2

User logs in.

Step 3

Authorization server returns **authorization code**.

Step 4

Client exchanges code for **access token**.

Step 5

Client calls API using token.

---

# 13. Example OAuth2 Request

### Authorization Request

```
GET /authorize
?client_id=123
&response_type=code
&redirect_uri=https://app.com/callback
&scope=profile
```

---

### Token Request

```
POST /token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
client_id=123
client_secret=xyz
code=abc123
```

---

### API Request with Token

```
GET /user/profile

Authorization: Bearer access_token_here
```

---

# 14. OAuth2 in Real World Applications

Examples:

| Application          | OAuth Use |
| -------------------- | --------- |
| Google Login         | OAuth2    |
| Facebook Login       | OAuth2    |
| GitHub Apps          | OAuth2    |
| Slack Apps           | OAuth2    |
| Microsoft Azure APIs | OAuth2    |

---

# 15. Implementing OAuth2 in Applications

Typical steps:

### Step 1 Register Application

Register your application with OAuth provider.

Example:

```
Google Developer Console
```

You will receive:

```
Client ID
Client Secret
Redirect URI
```

---

### Step 2 Configure OAuth Client

Example configuration:

```
client_id=abc123
client_secret=xyz456
redirect_uri=http://localhost:8080/callback
```

---

### Step 3 Implement Authorization Flow

Application redirects user to:

```
https://accounts.google.com/o/oauth2/v2/auth
```

User logs in → returns authorization code.

---

### Step 4 Exchange Code for Token

```
POST /token
```

Receive:

```
access_token
refresh_token
expires_in
```

---

# 16. Testing OAuth2 APIs

OAuth APIs can be tested using:

| Tool         | Usage               |
| ------------ | ------------------- |
| Postman      | Manual testing      |
| cURL         | Command line        |
| REST Assured | API automation      |
| Playwright   | UI + API testing    |
| JMeter       | Performance testing |
| Gatling      | Load testing        |

---

## Testing OAuth Token Generation

Example Postman request:

```
POST /oauth/token
```

Body:

```
grant_type=client_credentials
client_id=abc
client_secret=xyz
```

Response:

```
{
 "access_token": "abc123",
 "token_type": "Bearer",
 "expires_in": 3600
}
```

---

# 17. Automating OAuth2 in Automation Frameworks

In automation frameworks we usually:

```
1. Generate token
2. Store token
3. Use token in API requests
```

---

## Example using REST Assured (Java)

```
Response tokenResponse = given()
.formParam("grant_type", "client_credentials")
.formParam("client_id", "abc")
.formParam("client_secret", "xyz")
.post("/oauth/token");

String token = tokenResponse.jsonPath().getString("access_token");
```

---

## Use Token in API Request

```
given()
.header("Authorization", "Bearer " + token)
.get("/api/users")
.then()
.statusCode(200);
```

---

# 18. OAuth2 Security Best Practices

| Best Practice         | Description                |
| --------------------- | -------------------------- |
| Use HTTPS             | Prevent token interception |
| Short token expiry    | Reduce risk                |
| Use refresh tokens    | Improve security           |
| Store tokens securely | Avoid leaks                |
| Validate scopes       | Restrict permissions       |

---

# 19. Common OAuth Errors

| Error               | Meaning                    |
| ------------------- | -------------------------- |
| invalid_client      | Wrong client credentials   |
| invalid_token       | Token expired              |
| invalid_grant       | Authorization code invalid |
| unauthorized_client | Client not allowed         |
| access_denied       | User rejected request      |

Example response:

```
{
 "error": "invalid_token"
}
```

---

# 20. Tools to Test OAuth

| Tool             | Usage             |
| ---------------- | ----------------- |
| Postman          | API testing       |
| Swagger          | API documentation |
| OAuth Playground | Test OAuth flows  |
| cURL             | CLI testing       |
| Burp Suite       | Security testing  |

---

# 21. Interview Questions

### Basic

1. What is OAuth?
2. What is OAuth2?
3. What is an access token?
4. What is refresh token?

---

### Intermediate

1. Explain OAuth2 authorization flow.
2. What are OAuth grant types?
3. Difference between authentication and authorization?

---

### Advanced

1. Explain OAuth2 authorization code flow.
2. How to automate OAuth APIs?
3. How does refresh token work?

---

# 22. Official References

Official documentation:

OAuth Official Site

```
https://oauth.net/
```

OAuth2 RFC

```
https://datatracker.ietf.org/doc/html/rfc6749
```

OAuth2 Security Best Practices

```
https://oauth.net/2/
```

Google OAuth Docs

```
https://developers.google.com/identity/protocols/oauth2
```

---

# Conclusion

OAuth2 is a **modern authorization framework** used by almost all modern APIs.

Key points:

* Token based authorization
* No password sharing
* Highly secure
* Industry standard
* Easy to automate in testing frameworks

Understanding OAuth2 is **very important for API automation engineers, backend developers, and security engineers**.

---