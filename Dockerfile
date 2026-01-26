FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
COPY assets ./assets
COPY config ./config

# build runnable spring boot jar
RUN mvn -B -ntp -DskipTests package \
  && JAR="$(ls -1 target/*.jar | grep -v '\.original$' | head -n 1)" \
  && test -n "$JAR" \
  && cp "$JAR" /app/app.jar


FROM eclipse-temurin:21-jre
WORKDIR /app

# runtime assets/config
COPY --from=build /app/app.jar ./app.jar
COPY --from=build /app/assets ./assets
COPY --from=build /app/config ./config

# env vars
ENV SERVER_PORT=8080 \
    HTE_CONFIG_PATH=assets/config.infinite.json \
    HTE_DB_PATH=assets/data.json

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
