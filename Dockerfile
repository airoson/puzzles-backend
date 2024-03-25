FROM bellsoft/liberica-openjdk-debian:17

WORKDIR /app

COPY build/libs/puzzles-backend-0.0.1-SNAPSHOT.jar ./application.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "./application.jar"]