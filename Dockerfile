# Stage 1: Build the target Spring Boot module (along with common-dto) using Azul Zulu 21
FROM azul/zulu-openjdk-alpine:21-latest AS builder
WORKDIR /workspace

RUN apk add --no-cache maven

ARG MODULE_NAME

# Copy POMs first so Maven dependencies stay cached across source edits
COPY pom.xml .
COPY common-dto/pom.xml common-dto/
COPY api-gateway/pom.xml api-gateway/
COPY transaction-gateway-service/pom.xml transaction-gateway-service/
COPY fraud-audit-service/pom.xml fraud-audit-service/
COPY fraud-detection-engine/pom.xml fraud-detection-engine/

RUN mvn dependency:go-offline -pl ${MODULE_NAME} -am -B

# Copy shared DTOs and the target module's source
COPY common-dto/src common-dto/src
COPY ${MODULE_NAME}/src ${MODULE_NAME}/src

RUN mvn package -pl ${MODULE_NAME} -am -DskipTests -Dspotless.check.skip=true -B

# Stage 2: Minimal Azul Zulu 21 JRE runtime image
FROM azul/zulu-openjdk-alpine:21-jre
WORKDIR /app

# Install wget (for healthchecks) and libstdc++ (required by RocksDB JNI in Kafka Streams)
RUN apk add --no-cache wget libstdc++ && addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG MODULE_NAME
COPY --from=builder /workspace/${MODULE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-jar", "/app/app.jar"]
