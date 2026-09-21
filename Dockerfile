FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY backend-common backend-common
COPY EurekaServer EurekaServer
COPY DiscoveryClient DiscoveryClient
COPY UserService UserService
COPY ShopService ShopService
COPY ShopApprovalService ShopApprovalService
COPY RatingService RatingService
COPY FavoriteService FavoriteService
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:25-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
ARG SERVICE
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar /app/app.jar
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["java","-jar","/app/app.jar"]
