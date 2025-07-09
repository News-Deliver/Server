# --- 1단계: 빌드 ver 1.0 ---
#FROM gradle:8.4.0-jdk17 AS build
#WORKDIR /app
#COPY . .
#RUN gradle clean build -x test

# --- 1단계: 빌드 ver 1.1 ---
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app

# 의존성 캐시를 위해 먼저 build.gradle만 복사
# 소스 변경이 없으면 이 단계가 캐시
COPY build.gradle settings.gradle ./
RUN gradle dependencies || true

# 이후 전체 소스를 복사
COPY . .
RUN gradle clean build -x test

# --- 2단계: 실행 ---
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# 환경 변수는 docker-compose에서 주입
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]