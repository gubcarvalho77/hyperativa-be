# HyperAtiva Backend Test

## Overview

**HyperAtiva Backend** is a Spring Boot application designed to manage user accounts and securely store credit card information.  
The project provides REST endpoints for:

- User authentication (`/auth/login`)
- User management (`/users`)
- Credit card management (`/credit-cards`), including:
    - Adding a credit card
    - Checking if a credit card exists for a user
    - Uploading batch files with multiple credit cards

The application uses:

- **Spring Boot 3.x**
- **Java 21+**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with repositories for `User` and `UserCreditCard`
- **AES/GCM encryption** for storing credit card numbers securely
- **Swagger/OpenAPI** documentation
- **JUnit + MockMvc** for unit and integration testing

---

## Requirements

- Java 21+
- Gradle
- PostgreSQL (or your preferred relational database)
- Optional: Docker (for running the database locally)

---

## Project Structure
```
src/main/java/com/hyperativa/be
├── controllers # REST Controllers
├── dtos # DTOs for requests and responses
├── exceptions # Custom exceptions
├── model # JPA Entities (User, UserCreditCard)
├── repositories # Spring Data repositories
├── services # Business logic services
├── util # Utilities (e.g., LoggedUsernameSupplier)
└── config # Configuration (security, swagger, etc.)
```

## Setup and Run

### 1. Clone the repository

```
git clone https://github.com/gubcarvalho77/hyperativa-be.git
cd hyperativa-be
```

### 2. 🐳 Running with Docker

This project includes a convenience script to start and/or stop all required services using Docker Compose. Instead of manually running docker-compose, you can simply execute:

- To build and start all containers:

```
./up.sh
```

- To build and stop all containers:

```
./down.sh
```

### To start manually the application

- run this to start only the database:
```
./docker-compose --env-file .env up -d hyperativa-db
```

- use gradle bootrun to start the application:
 ```
./gradlew bootRun
```

### 3. Testing

1. The application will automatically add two users in the database, one with ADMIN role and the other one with USER role. These are the curl to login and get the JWT token for each user:

- ADMIN user
```
curl --request POST \
  --url http://localhost:9020/auth/login \
  --header 'Content-Type: application/json' \
  --data '{
	"email": "admin-user@hyperativa.com",
	"password": "s4kRle@t"
}'
```

- DEV user
```
curl --request POST \
  --url http://localhost:9020/auth/login \
  --header 'Content-Type: application/json' \
  --data '{
	"email": "dev-user@hyperativa.com",
	"password": "s4kRle@t"
}'
```
#
2. Testing the ADMIN role

The endpoint below was created just to test the ADMIN role

```
curl --request GET \
  --url http://localhost:9020/users \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJoeXBlcmF0aXZhLWFwaSIsImp0aSI6ImViOGUzZDQxLTJlOGItNGFiOC1hZWE1LWU0Njg2YjUxMDk1MiIsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NjQ1NTgyNTMsImV4cCI6MTc2NDU2MTg1Mywic3ViIjoiQURNX2VhOGZiYjdhIn0.VbsuzNOA21sgGBe505SOy8Oe30b0xMWArRPG8tVeecQ' \
```

#
3. Testing the endpoint to register credit cards individually

```
curl --request POST \
  --url http://localhost:9020/credit-cards \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJoeXBlcmF0aXZhLWFwaSIsImp0aSI6ImQyZWRlNWExLTBlZmItNDBmZi1iOGNiLWFhYmZiNzM3NjdhMCIsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NjQ1NTgyODAsImV4cCI6MTc2NDU2MTg4MCwic3ViIjoiVVNSXzc5YTViODhkIn0.fgHoo_J4gGGzgqaf3dDKQ4B4JZnaF25LK3h59mTjyJY' \
  --header 'Content-Type: application/json' \
  --data '{
	"cardNumber": "5555555555554444"
}'
```

#
4. Testing the endpoint to check if the credit card is registered

```
curl --request POST \
  --url http://localhost:9020/credit-cards/check \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJoeXBlcmF0aXZhLWFwaSIsImp0aSI6ImQyZWRlNWExLTBlZmItNDBmZi1iOGNiLWFhYmZiNzM3NjdhMCIsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NjQ1NTgyODAsImV4cCI6MTc2NDU2MTg4MCwic3ViIjoiVVNSXzc5YTViODhkIn0.fgHoo_J4gGGzgqaf3dDKQ4B4JZnaF25LK3h59mTjyJY' \
  --header 'Content-Type: application/json' \
  --data '{
	"cardNumber": "5555555555554444"
}'
```

#
5. Testing the endpoint to upload the file

```
curl --request POST \
  --url http://localhost:9020/credit-cards/upload \
  --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJoeXBlcmF0aXZhLWFwaSIsImp0aSI6ImQyZWRlNWExLTBlZmItNDBmZi1iOGNiLWFhYmZiNzM3NjdhMCIsInR5cGUiOiJhY2Nlc3MiLCJpYXQiOjE3NjQ1NTgyODAsImV4cCI6MTc2NDU2MTg4MCwic3ViIjoiVVNSXzc5YTViODhkIn0.fgHoo_J4gGGzgqaf3dDKQ4B4JZnaF25LK3h59mTjyJY' \
  --header 'content-type: multipart/form-data' \
  --form 'file=@C:\Users\gustavo\wrks\hyperativa.txt'
```