# Etapa 1: Build Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copiem tot codul sursă
COPY . .

# Compilăm aplicația fără teste (mai rapid)
RUN mvn clean package -DskipTests

# Etapa 2: Runtime JDK
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiem JAR-ul compilat din etapa anterioară
COPY --from=build /app/target/*.jar app.jar

# Expunem portul standard Render
ENV PORT=8080
EXPOSE 8080

# Pornim aplicația
CMD ["java", "-jar", "app.jar"]