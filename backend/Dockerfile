FROM ubuntu:latest
LABEL authors="zarem"

# --- Этап 1: Сборка проекта ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --- Этап 2: Запуск приложения ---
FROM eclipse-temurin:17-jdk-jammy

# Устанавливаем Python и нужные библиотеки
RUN apt-get update && apt-get install -y python3 python3-pip
RUN pip3 install textblob && python3 -m textblob.download_corpora

WORKDIR /app

# Копируем СОБРАННЫЙ jar файл
COPY --from=builder /build/target/*.jar app.jar

# КОПИРУЕМ ПАПКУ СО СКРИПТОМ ВНУТРЬ КОНТЕЙНЕРА
COPY python-sentiment /app/python-sentiment

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]