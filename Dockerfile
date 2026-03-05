# 빌드 스테이지
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# 실행 스테이지
FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]