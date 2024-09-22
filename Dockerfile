FROM eclipse-temurin:17-jdk-jammy
ARG JAR_PATH=build/libs/uv-index-bot-0.0.1-SNAPSHOT.jar
COPY $JAR_PATH app.jar
EXPOSE 8443
ENTRYPOINT ["java","-jar","/app.jar"]
