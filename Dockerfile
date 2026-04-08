# ===== build =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle

COPY build.gradle settings.gradle ./

COPY src src

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

# instala curl para o healthcheck do docker-compose
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","/app/app.jar"]

