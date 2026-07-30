FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY logiclinter-backend/mvnw .
COPY logiclinter-backend/.mvn .mvn
COPY logiclinter-backend/pom.xml .
COPY logiclinter-backend/src src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]