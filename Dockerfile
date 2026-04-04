FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/SagaOrchestrator-0.0.1-SNAPSHOT.jar /app/saga-orchestrator.jar
EXPOSE 7018
ENTRYPOINT ["java", "-jar", "/app/saga-orchestrator.jar"]
