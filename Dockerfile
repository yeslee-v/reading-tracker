FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]