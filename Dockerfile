# ---------------------------------------------------------------------------
# Shared multi-stage Dockerfile for both Spring Boot modules.
#
# Build the main application:
#   docker build --build-arg MODULE=student-record-system -t scholaris-main .
#
# Build the grading microservice:
#   docker build --build-arg MODULE=grading-service -t scholaris-grading .
#
# Or simply use: docker compose up --build
# ---------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /workspace

# Resolve dependencies first so Docker can cache this layer.
COPY pom.xml ./
COPY grading-service/pom.xml grading-service/pom.xml
COPY student-record-system/pom.xml student-record-system/pom.xml
RUN mvn -B -q -pl "${MODULE}" -am dependency:go-offline

# Copy sources and package only the requested module (plus the parent POM).
COPY grading-service/src grading-service/src
COPY student-record-system/src student-record-system/src
RUN mvn -B -q -pl "${MODULE}" -am -DskipTests package

FROM eclipse-temurin:17-jre-jammy
ARG MODULE
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/data

COPY --from=build /workspace/${MODULE}/target/*.jar /app/app.jar

# The H2 file database lives in /app/data; mount a volume to keep it.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0" \
    SPRING_PROFILES_ACTIVE=""

EXPOSE 8080 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
