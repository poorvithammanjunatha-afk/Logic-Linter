FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy the build files and source code into the container workdir
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Grant execution permission and build
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]