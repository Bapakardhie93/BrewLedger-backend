FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV SERVER_PORT=8081
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC"

COPY --from=build /app/target/brewledger.backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-$SERVER_PORT} -jar app.jar"]
