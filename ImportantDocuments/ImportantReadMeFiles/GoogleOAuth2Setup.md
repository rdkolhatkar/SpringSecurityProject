# Google OAuth2 Client ID and Client Secret Setup Guide

## Table of Contents

1. Introduction
2. What is OAuth2
3. Why Client ID and Client Secret are Needed
4. Prerequisites
5. Step-by-Step Guide to Create OAuth2 Credentials
6. Step 1 – Create Google Cloud Account
7. Step 2 – Create a New Project
8. Step 3 – Enable Required APIs
9. Step 4 – Configure OAuth Consent Screen
10. Step 5 – Create OAuth2 Credentials
11. Step 6 – Choose Application Type
12. Step 7 – Add Redirect URI
13. Step 8 – Create Client ID and Client Secret
14. Example Spring Boot Configuration
15. Important Notes
16. Troubleshooting
17. Cleanup Guide (Delete OAuth Credentials and Project)
18. Official Documentation

---

# 1. Introduction

Many modern web applications allow users to **login using their Google account**.

Example:

```
Login with Google
```

This functionality works using **OAuth2 authentication protocol**.

To integrate Google login in your application, you must generate:

* **Client ID**
* **Client Secret**

These credentials are created from **Google Cloud Console**.

---

# 2. What is OAuth2

OAuth2 is a **secure authorization framework** that allows applications to access user information **without sharing the user's password**.

Example flow:

```
User clicks "Login with Google"
        ↓
Google authentication page opens
        ↓
User grants permission
        ↓
Application receives an access token
        ↓
Application retrieves user information
```

OAuth2 improves security because the application **never sees the user's password**.

---

# 3. Why Client ID and Client Secret Are Needed

| Credential    | Purpose                                  |
| ------------- | ---------------------------------------- |
| Client ID     | Identifies your application to Google    |
| Client Secret | Verifies your application's authenticity |

Example:

```
Client ID:
123456789012-abcdefg.apps.googleusercontent.com

Client Secret:
GOCSPX-abcdef123456
```

These credentials allow Google to **trust your application**.

---

# 4. Prerequisites

Before starting:

* A Google account
* Internet access
* Access to Google Cloud Console

Open:

```
https://console.cloud.google.com
```

---

# 5. Step-by-Step Guide to Create OAuth2 Credentials

Process overview:

```
1. Login to Google Cloud Console
2. Create a new project
3. Enable required APIs
4. Configure OAuth consent screen
5. Create OAuth Client ID
6. Copy Client ID and Client Secret
```

---

# 6. Step 1 – Create Google Cloud Account

1. Open your browser
2. Go to:

```
https://console.cloud.google.com
```

3. Login with your Google account.

You will see the **Google Cloud Dashboard**.

---

# 7. Step 2 – Create a New Project

A **project** is required to manage APIs and credentials.

### Steps

1. Click **Select Project** at the top of the page.
2. Click:

```
New Project
```

3. Enter project details.

Example:

| Field        | Value               |
| ------------ | ------------------- |
| Project Name | SpringSecurityOAuth |
| Organization | Leave default       |
| Location     | Leave default       |

4. Click:

```
Create
```

Your project will be created.

---

# 8. Step 3 – Enable Required APIs

To allow authentication, you must enable Google APIs.

### Steps

1. Open the left navigation menu.
2. Click:

```
APIs & Services
```

3. Click:

```
Library
```

4. Search for:

```
Google People API
```

or

```
Google Identity Services
```

5. Select the API.
6. Click:

```
Enable
```

The API is now enabled.

---

# 9. Step 4 – Configure OAuth Consent Screen

The **OAuth Consent Screen** is shown when users log in.

Example message:

```
This application wants to access your Google profile.
```

### Steps

1. Navigate to:

```
APIs & Services
```

2. Click:

```
OAuth Consent Screen
```

3. Select user type.

| Option   | Use Case            |
| -------- | ------------------- |
| External | Public applications |
| Internal | Organization apps   |

Choose:

```
External
```

4. Click **Create**.

---

## Fill Application Information

Enter:

| Field              | Example                   |
| ------------------ | ------------------------- |
| App Name           | Spring Security OAuth App |
| User Support Email | your email                |
| Developer Email    | your email                |

Click:

```
Save and Continue
```

---

## Configure Scopes

Scopes define which user data your application can access.

Example:

```
email
profile
openid
```

Click:

```
Save and Continue
```

---

## Add Test Users

Add your email address.

Example:

```
your_email@gmail.com
```

Click:

```
Save
```

OAuth consent screen configuration is complete.

---

# 10. Step 5 – Create OAuth2 Credentials

Now create the **Client ID and Client Secret**.

Steps:

1. Go to:

```
APIs & Services
```

2. Click:

```
Credentials
```

3. Click:

```
Create Credentials
```

4. Select:

```
OAuth Client ID
```

---

# 11. Step 6 – Choose Application Type

Select the application type.

| Type            | Use Case     |
| --------------- | ------------ |
| Web Application | Websites     |
| Desktop App     | Local apps   |
| Android         | Android apps |
| iOS             | iPhone apps  |

For Spring Boot applications choose:

```
Web Application
```

---

# 12. Step 7 – Add Redirect URI

Redirect URI is where Google sends the user after login.

Example for Spring Boot:

```
http://localhost:8080/login/oauth2/code/google
```

Steps:

1. Add the redirect URI.
2. Click:

```
Add URI
```

---

# 13. Step 8 – Create Client ID and Client Secret

Click:

```
Create
```

Google will generate credentials.

Example:

```
Client ID
123456789012-abcdef.apps.googleusercontent.com

Client Secret
GOCSPX-abcdef123456
```

Save these credentials securely.

---

# 14. Example Spring Boot Configuration

Example `application.yml`:

```
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
            scope:
              - profile
              - email
```

---

# 15. Important Notes

| Best Practice              | Explanation                         |
| -------------------------- | ----------------------------------- |
| Keep Client Secret private | Never expose in public repositories |
| Use HTTPS                  | Required for production             |
| Restrict Redirect URI      | Prevent unauthorized access         |
| Use environment variables  | Protect credentials                 |

---

# 16. Troubleshooting

## redirect_uri_mismatch

Occurs when redirect URI is incorrect.

Fix:

Ensure it matches exactly.

```
http://localhost:8080/login/oauth2/code/google
```

---

## invalid_client

Occurs when Client ID or Client Secret is incorrect.

Solution:

Verify credentials.

---

## access_denied

Occurs when user denies permission.

Solution:

User must grant access.

---

# 17. Cleanup Guide (Delete OAuth Credentials and Project)

After testing OAuth integration, you may want to delete credentials and project.

---

## Delete OAuth Client ID and Client Secret

Steps:

1. Open Google Cloud Console.

```
https://console.cloud.google.com
```

2. Go to:

```
APIs & Services
```

3. Click:

```
Credentials
```

4. Find your OAuth Client.

Example:

```
SpringSecurityOAuthClient
```

5. Click the **Delete icon**.

6. Confirm deletion.

This will delete:

```
Client ID
Client Secret
OAuth credentials
```

---

## Delete OAuth Consent Screen Configuration

Steps:

1. Open:

```
APIs & Services
```

2. Click:

```
OAuth Consent Screen
```

3. If the app is still in testing mode, you can simply delete the project (recommended).

---

## Delete Entire Google Cloud Project

If the project was created only for learning, you can delete it completely.

Steps:

1. Open:

```
IAM & Admin
```

2. Click:

```
Settings
```

3. Click:

```
Shut Down Project
```

4. Enter project ID.

5. Confirm deletion.

This removes:

```
OAuth credentials
Enabled APIs
Consent screen
Project configuration
```

---

# 18. Official Documentation

Google OAuth documentation:

```
https://developers.google.com/identity
```

OAuth2 protocol documentation:

```
https://oauth.net/2/
```

Google Cloud Console:

```
https://console.cloud.google.com
```

---

# Conclusion

In this guide you learned:

* What OAuth2 is
* Why Client ID and Client Secret are required
* How to configure OAuth consent screen
* How to create OAuth credentials
* How to integrate credentials with Spring Boot
* How to delete OAuth credentials after testing

This setup enables **secure Google login integration in applications**.

---