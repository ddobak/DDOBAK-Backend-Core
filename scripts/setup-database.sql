-- 데이터베이스 스키마 생성 스크립트
-- 하나의 RDS 인스턴스에서 dev/prod 환경을 스키마로 분리

-- dev 스키마 생성
CREATE SCHEMA IF NOT EXISTS dev;
GRANT ALL PRIVILEGES ON SCHEMA dev TO ddobak;

-- prod 스키마 생성  
CREATE SCHEMA IF NOT EXISTS prod;
GRANT ALL PRIVILEGES ON SCHEMA prod TO ddobak;

-- 스키마별 테이블 생성 권한 부여
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA dev TO ddobak;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA prod TO ddobak;

-- 시퀀스 권한 부여
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA dev TO ddobak;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA prod TO ddobak;
