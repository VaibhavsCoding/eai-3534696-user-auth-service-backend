# user-auth-service

# 🔐 User Authentication Service – Spring Boot Backend

A **secure Spring Boot backend** designed to provide user authentication and authorization APIs. It implements **JWT-based security**, **OTP verification**, and **login protection** mechanisms, making it suitable for modern full-stack applications and microservices.

---

## ✅ What This Backend Does

This project implements essential authentication and security features required for production-ready systems:

* **User Signup API**
    * `POST /api/signup`
    * Registers a new user and sends an OTP for verification.
    * Captures device, IP, and location details.

* **OTP Verification API**
    * `POST /api/verify-otp`
    * Verifies the OTP and activates the user account.

* **Login API**
    * `POST /api/login`
    * Authenticates user credentials and generates a **JWT access token**.
    * Tracks failed login attempts and locks accounts if needed.

* **JWT Token Verification**
    * `GET /api/verify-token`
    * Validates JWT tokens and checks expiration.

* **Resend OTP**
    * `POST /api/resend-otp`
    * Allows users to request a new OTP if not verified.

* **Security Features**
    * BCrypt password hashing.
    * JWT-based stateless authentication.
    * Login attempt tracking and account lock protection.
    * Global exception handling.

---

## ❌ What This Backend Does NOT Include (Intentional Scope)

These features are **intentionally excluded** to keep the backend focused strictly on the authentication layer:

* No **frontend UI**
* No **OAuth login** (Google, GitHub, etc.)
* No **refresh token** implementation
* No **password reset** email flow
* No **role-based authorization (RBAC)**
* No **admin dashboard**

---

## 📦 Prerequisites

Ensure you have the following installed to build and run the project:

* **Java 17+**
* **Maven 3.8+**
* **MySQL 8+**
* **Git**

---

## 🔧 Configuration

Configure your database and JWT settings in `src/main/resources/application.properties`:

| Property | Description |
| :--- | :--- |
| `spring.datasource.url` | MySQL database connection URL |
| `spring.datasource.username` | DB username |
| `spring.datasource.password` | DB password |
| `jwt.secret` | Secret key for JWT signing |
| `jwt.expiration` | Token expiration time in milliseconds |

---

## 🛠 Build & Run

### 1. Clone Repo
```bash
git clone [https://github.com/VaibhavsCoding/user-auth-service-backend.git](https://github.com/VaibhavsCoding/user-auth-service-backend.git)
cd user-auth-service-backend
```

### 2. Build
```bash
mvn clean package
```

### 3. Run (Recommended)
```bash
mvn spring-boot:run
```

### 4. Run JAR
```bash
java -jar target/user-auth-service-backend.jar
```
The server will be running and accessible at: `http://localhost:8080`

## 🔌 API Usage 🟦
### 1. Signup (User Registration)
#### Endpoint: 'POST /api/signup'

#### Request Body (JSON):
````
{
"email": "user@example.com",
"password": "StrongPassword123"
}
````

#### Response Body:
````
{
"success": true,
"message": "Signup successful! OTP sent to email.",
"token": "jwt-token"
}
````
### 🟩 2. OTP Verification
#### Endpoint: `POST /api/verify-otp`

#### Request Body (JSON):
````
{
"email": "user@example.com",
"otp": "123456"
}
````
### 🟨 3. Login
#### Endpoint: `POST /api/login`

#### Request Body (JSON):
````
{
"email": "user@example.com",
"password": "StrongPassword123"
}
````
### 🟪 4. Verify JWT Token
#### Endpoint: `GET /api/verify-token`

#### Header: `Authorization: Bearer <JWT_TOKEN>`

## ⚠️ Troubleshooting (Common Issues)
| Issue | Cause & Fix |
| :--- | :--- |
| **❌ 401 Unauthorized** | **Cause:** Invalid or expired JWT token.<br>**Fix:** Re-login and generate a new token. |
| **❌ Account Locked** | **Cause:** Too many failed login attempts.<br>**Fix:** Wait for lockout period or manually reset account state. |
| **❌ OTP Invalid / Expired** | **Cause:** OTP timeout or incorrect entry.<br>**Fix:** Use the resend OTP API. |
| **❌ Database Connection** | **Fix:** Verify MySQL credentials and ensure the service is running. |
| **❌ Port 8080 in use** | **Fix:** Change port using `server.port=9090` in `application.properties`. |

## 🔭 Recommended Future Enhancements

1. **Refresh Tokens:** Implementation of token rotation for better security and improved user session management.
2. **Password Recovery:** Development of Forgot Password and Reset Password email flows to enhance user self-service.
3. **RBAC (Role-Based Access Control):** Establishing distinct permissions for different user levels such as User, Admin, and Moderator.
4. **Social Login:** Integration of OAuth2 providers like Google and GitHub for a more seamless authentication experience.
5. **Admin Suite:** Specialized management APIs for user monitoring, manual account unlocks, and system oversight.

## 📄 License
````
MIT License
````

## AI chat model Backend application link:
````
https://github.com/VaibhavsCoding/react-spring-ai-backend
````
## AI chat model Frontend application link:
````
https://github.com/VaibhavsCoding/react-spring-ai-frontend
````