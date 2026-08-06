FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache maven
COPY pom.xml .
RUN mvn -B -e dependency:go-offline \
  -Dorg.apache.maven.wagon.http.retryHandler.count=2 \
  -Dmaven.wagon.http.timeout=60000 \
  -Dmaven.wagon.http.connectionTimeout=60000
COPY src ./src
RUN mvn -B -e -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
