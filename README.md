
# 📘 Device Manager API

## 📋 Description

This project is a REST API developed with **Java 21** and **Spring Boot 3.5**, focusing on *device* management. It utilizes modern development practices such as:

- Persistence with **Spring Data JPA**
- Database **MariaDB**
- Caching with **Redis**
- Migrations with **Flyway**
- Documentation with **Swagger (OpenAPI)**
- Observability with **Micrometer + Prometheus**
- Testing with **JUnit + Jacoco**

---

## 🚀 Technologies Used

- Java 21
- Spring Boot 3.5
- Spring Data JPA
- MariaDB
- Redis
- Flyway
- Lombok
- Bean Validation (Hibernate Validator)
- Micrometer + Prometheus
- Swagger UI (Springdoc OpenAPI)
- Spring Cache
- Spring Boot Actuator
- Docker + Docker Compose
- Jacoco (test coverage)

---

## ⚙️ Prerequisites

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- Java 21 (if you wish to run it locally outside of Docker)
- Maven 3.9+

---

## 📂 How to Run the Project

### ✅ 1. Using Docker Compose

```bash
git clone <your-repo>
cd DeviceManager
docker-compose up --build
```

> The application will be available at: `http://localhost:8080`  
> Swagger documentation:: `http://localhost:8080/swagger-ui.html`

---

### ✅ 2.Running Locally with MySQL and Redis via Docker

```bash
docker-compose up -d mariadb redis

# Export environment variables
export SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/device-api
export SPRING_DATASOURCE_USERNAME=user
export SPRING_DATASOURCE_PASSWORD=rootpass
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379

# Run with Maven
./mvnw spring-boot:run
```

---

## 🔍 Main Endpoints

Access `http://localhost:8080/swagger-ui.html` to view all available endpoints.

---

## 📦 Docker Image Build

```dockerfile
# Dockerfile
# Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Exec
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

---
