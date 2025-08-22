# ========================================
# 빌드 단계 (Build Stage)
# ========================================
# Gradle을 사용하여 애플리케이션을 빌드하는 단계
FROM gradle:8.4-jdk17 AS build

# 빌드 시점에 APP_NAME 전달 받기
ARG APP_NAME=alim-service

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 설정 파일 복사 (의존성 캐싱을 위해 먼저 복사)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Gradle 의존성 다운로드 (캐싱을 위해 별도 단계로 분리)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN gradle build -x test --no-daemon

# ========================================
# 런타임 단계 (Runtime Stage)
# ========================================
# JRE만 포함된 가벼운 런타임 이미지
FROM eclipse-temurin:17-jre-alpine

# 메타데이터 설정
LABEL maintainer="DeeFacto Noti"
LABEL description="Noti Service for Deefacto Platform"

# 작업 디렉토리 설정
WORKDIR /app

# 보안을 위한 비root 사용자 생성
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 빌드된 JAR 파일 복사
# 'your-app-name' 부분을 프로젝트의 실제 JAR 파일 이름으로 바꿔주세요.
# [setting.gradle rootProjectname]-[build.gradle version]
COPY --from=build /app/build/libs/alim-service-0.0.1-SNAPSHOT.jar app.jar

# 파일 소유권 변경
RUN chown -R appuser:appgroup /app

# 비root 사용자로 전환
USER appuser

# 헬스체크 설정
# '8081' 부분을 여러분의 애플리케이션 포트로 바꿔주세요.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# 포트 노출
# '8081' 부분을 여러분의 애플리케이션 포트로 바꿔주세요.
EXPOSE 8082

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]