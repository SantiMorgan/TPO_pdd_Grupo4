FROM maven:3.9-eclipse-temurin-21 AS base
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -ntp dependency:resolve
COPY src ./src

FROM base AS development
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY scripts/dev.sh ./scripts/dev.sh
RUN chmod +x ./scripts/dev.sh
EXPOSE 8080
CMD ["./scripts/dev.sh"]

FROM base AS test
CMD ["mvn", "-B", "-ntp", "verify"]

FROM base AS build
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && groupadd --system barstock && useradd --system --gid barstock barstock
COPY --from=build --chown=barstock:barstock /app/target/barstock.jar ./app.jar
USER barstock
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
