# 🏦 My Banking Simulation Web Application

A **cloud-ready banking simulation backend** built with **Spring Boot** and **MySQL (AWS Aurora/RDS)** — designed to mimic real-world banking operations with **secure authentication**, **multi-account management**, **transaction history**, and **profile image uploads to AWS S3**.

The application runs inside a **Docker container on AWS EC2**, and is continuously integrated & deployed through **GitHub Actions CI/CD** for fully automated delivery.

---

[<--KNN-Banking-Simulation-->](http://knnbank-frontend-deploy.s3-website-ap-southeast-2.amazonaws.com)

---

## ✨ Key Features

### 💳 Banking Simulation

- Multiple account types per user (Savings, Current, etc.)
- Fund transfer simulation between accounts (no real money)
- Balance updates with validation & insufficient-fund checks
- Transaction history with pagination & timestamps

### 🔐 Authentication & Security

- JWT-based login and token flow
- BCrypt password hashing
- Role-based access control (`CUSTOMER`, `ADMIN`, `AUDITOR`)
- Custom exception handling with consistent error responses

### ☁️ Cloud & Storage

- **AWS S3**: Stores user profile images
- **AWS RDS / Aurora MySQL**: Main production database for persistent data
- **AWS EC2**: Hosts Dockerized Spring Boot container
- **GitHub Actions**: Automated build, test, and deployment to EC2 via SSH

---

## 🧱 Tech Stack

| Layer          | Technology                 |
| -------------- | -------------------------- |
| **Backend**    | Spring Boot 4.0.0          |
| **Database**   | AWS Aurora MySQL / RDS     |
| **Storage**    | AWS S3                     |
| **Deployment** | AWS EC2 (Docker container) |
| **CI/CD**      | GitHub Actions             |
| **Build Tool** | Maven Wrapper              |
| **Auth**       | JWT                        |
| **Language**   | Java 21                    |

---

## 🗂️ Project Structure

```
banking-simulation/
├─ src/main/java/com/knn/knnbank/
│  ├─ account/
│  ├─ audit_dashboard/
│  ├─ auth_users/
│  ├─ aws/
│  ├─ config/
│  ├─ enums/
│  ├─ exceptions/
│  ├─ notification/
│  ├─ response/
│  ├─ role/
│  ├─ security/
│  └─ transaction/
├─ src/main/resources/
│  ├─ application.properties
│  └─ templates
├─ Dockerfile
├─ compose.yml
├─ .github/workflows/deploy.yml
├─ ─ ─ ─ ─ ─ ─ ─ ─ ─/validate.yml
├─ pom.xml
└─ README.md

```

---

## API Overview

| Method   | Endpoint                              | Description                                           |
| -------- | ------------------------------------- | ----------------------------------------------------- |
| `POST`   | `/api/auth/register`                  | Register a new user                                   |
| `POST`   | `/api/auth/login`                     | Login and receive JWT tokens                          |
| `POST`   | `/api/auth/forgot-password`           | Enter an email and will recieve a reset password link |
| `POST`   | `/api/auth/reset-password`            | Enter your new password and change                    |
| `GET`    | `/api/users/me`                       | Get authenticated user profile                        |
| `PUT`    | `/api/users/update-password`          | Change authenticated user profile                     |
| `POST`   | `/api/users/profile-picture`          | Upload profile picture (AWS S3)                       |
| `GET`    | `/api/accounts/me`                    | Get authenticated user account                        |
| `POST`   | `/api/accounts/close/{accountNumber}` | Close account                                         |
| `GET`    | `/api/audit/totals`                   | Retrieve audit logs for every activity                |
| `GET`    | `/api/audit/users`                    | Find user details by email                            |
| `GET`    | `/api/audit/accounts`                 | Find account details by account number                |
| `GET`    | `/api/audit/transactions/by-account`  | Find transactions by account number                   |
| `GET`    | `/api/audit/transactions/by-id`       | Find transactions by transaction id                   |
| `POST`   | `/api/roles`                          | Create role                                           |
| `PUT`    | `/api/roles`                          | Update role                                           |
| `GET`    | `/api/roles`                          | Get all role                                          |
| `Delete` | `/api/roles/{id}`                     | Delete role                                           |
| `POST`   | `/api/transactions`                   | Create transaction                                    |
| `GET`    | `/api/transactions/{accountNumber}`   | Get transaction for authenticated user                |

---

## 🐳 Running the Application

### Local (without Docker)

```

./mvnw spring-boot:run

```

## With Docker Compose

```
# Build the JAR
./mvnw clean package -DskipTests

# Build and start containers
docker compose up -d --build

# View logs
docker compose logs -f app

```

App runs at: http://localhost:8090

---

## ☁️ AWS Deployment Overview

### Architecture Diagram

```
GitHub → GitHub Actions → EC2 (Docker container)
                      ↓
          AWS RDS / Aurora MySQL  ←→  Spring Boot App
                      ↓
                   AWS S3 (Profile Images)

```
