# --- 1단계: 빌드 ---
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# --- 2단계: 실행 ---
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# 환경 변수는 docker-compose에서 주입
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 