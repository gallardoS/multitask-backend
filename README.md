# multitask Backend

Service for **Multitask**, a browser-based game available at [https://multitask.swami.dev](https://multitask.swami.dev).  

**Multitask** is a browser game built with **Three.js**, an arcade game where players have to simultaneously control multiple games.
## ⚙️ Overview

This backend powers the global leaderboard system, exposes secure endpoints to submit and retrieve scores from the frontend.  
It is built using **Spring Boot** and exposes a stateless HTTP API secured with an API key.

## 🔐 Authentication

All requests must include a valid `x-api-key` header.  
Unauthorized requests will receive an appropriate error response.

## 💻 Running Locally

To start the backend in a local environment, use the following command:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
````
## 🔧 Environment Variables

Before running the application, make sure to configure the required environment variables.  
Using the `local` profile, you can define them creating `application-local.properties`.

Example:

```properties
# application-local.properties

spring.datasource.url=jdbc:postgresql://localhost:5432/multitaskdb
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
app.api-key=your_api_key

# Optional
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
