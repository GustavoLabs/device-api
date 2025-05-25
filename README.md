
# 📘 Device Manager API

## 📋 Descrição

Este projeto é uma API REST desenvolvida com **Java 21** e **Spring Boot 3.5**, com foco no gerenciamento de *devices*. Ele utiliza práticas modernas de desenvolvimento como:

- Persistência com **Spring Data JPA**
- Banco de dados **MariaDB**
- Cache com **Redis**
- Migrações com **Flyway**
- Documentação com **Swagger (OpenAPI)**
- Observabilidade com **Micrometer + Prometheus**
- Testes com **JUnit + Jacoco**

---

## 🚀 Tecnologias Utilizadas

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
- Jacoco (cobertura de testes)

---

## ⚙️ Pré-requisitos

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- Java 21 (caso deseje rodar localmente fora do Docker)
- Maven 3.9+

---

## 📂 Como Rodar o Projeto

### ✅ 1. Usando Docker Compose

```bash
git clone <seu-repo>
cd DeviceManager
docker-compose up --build
```

> A aplicação estará disponível em: `http://localhost:8080`  
> A documentação Swagger: `http://localhost:8080/swagger-ui.html`

---

### ✅ 2. Rodando Localmente com MySQL e Redis via Docker

```bash
docker-compose up -d mariadb redis

# Exportar variáveis de ambiente
export SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/device-api
export SPRING_DATASOURCE_USERNAME=user
export SPRING_DATASOURCE_PASSWORD=rootpass
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379

# Rodar com Maven
./mvnw spring-boot:run
```

---

## 🔍 Endpoints Principais

Acesse `http://localhost:8080/swagger-ui.html` para visualizar todos os endpoints disponíveis.

---

## 📦 Build da Imagem Docker

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
