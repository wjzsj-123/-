# 阶段一：Maven 构建 Spring Boot 可执行 jar（仅后端，不含前端）
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -Pdocker -DskipTests

# 阶段二：运行时镜像
FROM eclipse-temurin:8-jre-jammy
WORKDIR /app
COPY --from=build /app/target/demo-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
