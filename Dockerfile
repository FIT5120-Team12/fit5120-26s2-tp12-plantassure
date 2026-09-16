FROM eclipse-temurin:17-jre
WORKDIR /app
COPY backend/plantky-backend-iteration1-commented/plantky-backend-iteration1-commented/plantky-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-XX:+UseSerialGC", "-Djava.awt.headless=true", "-jar", "app.jar"]
