FROM gradle:8-jdk21 AS builder

WORKDIR /app

COPY . .

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-alpine

RUN apk add --no-cache postgresql-client bash

VOLUME /tmp

EXPOSE 9020

WORKDIR /hyperativa-be

COPY --from=builder /app/build/libs/*.jar hyperativa-be.jar

COPY wait-for-postgres.sh /wait-for-postgres.sh
RUN chmod +x /wait-for-postgres.sh

ENTRYPOINT ["/wait-for-postgres.sh", "hyperativa-db", "java", "-jar", "hyperativa-be.jar"]