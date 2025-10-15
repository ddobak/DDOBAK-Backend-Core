#!/bin/bash

# 프로덕션 환경 실행 스크립트 (메모리 제한)
set -e

echo "🚀 프로덕션 환경을 시작합니다..."

# 최신 코드 가져오기 (현재 브랜치 유지)
echo "📥 최신 코드를 가져옵니다..."
git pull origin dev

# 현재 실행중인 프로세스 종료
echo "🛑 기존 프로세스를 종료합니다..."
pkill -f java || true

# 현재 실행중인 자바 프로세스 확인
echo "🔍 실행중인 자바 프로세스를 확인합니다..."
ps aux | grep java | grep -v grep || echo "실행중인 자바 프로세스가 없습니다."

# 환경 변수 로드
# set -a를 사용하면 이후 모든 변수가 자동으로 export됨

if [ -f ".env.prod" ]; then
    echo "📋 환경 변수를 로드합니다..."
    set -a  # 모든 변수를 자동으로 export
    source .env.prod
    set +a  # export 자동화 해제
else
    echo "⚠️  .env.prod 파일이 없습니다. 기본 설정으로 실행합니다."
fi

# JAR 생성 (테스트 없이)
echo "📦 JAR 파일을 생성합니다..."
./gradlew clean bootJar

# 메모리 제한으로 실행
echo "☕ 애플리케이션을 실행합니다 (메모리 제한: 512MB, Profile: prod)..."
nohup java -Xmx512m -Xms256m -Dspring.profiles.active=prod -Dlogging.level.com.sbpb.ddobak=DEBUG -jar build/libs/main-server-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 프로세스 ID 저장
echo $! > app.pid

echo "✅ 프로덕션 환경이 시작되었습니다!"
echo "📄 로그 확인: tail -f app.log"
echo "🛑 종료: kill \$(cat app.pid)"
