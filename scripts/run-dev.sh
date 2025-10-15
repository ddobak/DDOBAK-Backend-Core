#!/bin/bash

# 개발 환경 실행 스크립트 (메모리 제한)
set -e

echo "🚀 개발 환경을 시작합니다..."

# dev 브랜치로 체크아웃 및 최신 코드 가져오기
echo "📥 최신 코드를 가져옵니다..."
git checkout dev
git pull origin dev

# 현재 실행중인 프로세스 종료
echo "🛑 기존 프로세스를 종료합니다..."
pkill -f java || true

# 현재 실행중인 자바 프로세스 확인
echo "🔍 실행중인 자바 프로세스를 확인합니다..."
ps aux | grep java | grep -v grep || echo "실행중인 자바 프로세스가 없습니다."

# 환경 변수 로드
# export 방식은 한 줄에 여러 환경변수 처리할 때 공백 처리에 문제가 있음 -> source로 변경!
if [ -f ".env.dev" ]; then
    echo "📋 환경 변수를 로드합니다..."
    source .env.dev
else
    echo "⚠️  .env.dev 파일이 없습니다. 기본 설정으로 실행합니다."
fi

# JAR 생성 (테스트 없이)
echo "📦 JAR 파일을 생성합니다..."
./gradlew clean bootJar

# 메모리 제한으로 실행
# Spring Boot가 시작할 때 Profile을 찾지 못해서 멈추는 문제 해결!
echo "☕ 애플리케이션을 실행합니다 (메모리 제한: 512MB, Profile: dev)..."
nohup java -Xmx512m -Xms256m -Dspring.profiles.active=dev -Dlogging.level.com.sbpb.ddobak=DEBUG -jar build/libs/main-server-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 프로세스 ID 저장
echo $! > app.pid

echo "✅ 개발 환경이 시작되었습니다!"
echo "📄 로그 확인: tail -f app.log"
echo "🛑 종료: kill \$(cat app.pid)"
