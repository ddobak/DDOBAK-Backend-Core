# 🚀 DDOBAK Server

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-316192?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![AWS](https://img.shields.io/badge/AWS-Lambda%20%7C%20S3-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/)

> **계약서 OCR 및 분석 API 서버**

## 🎯 프로젝트 소개

계약서 이미지를 업로드하면 OCR로 텍스트를 추출하고, AI로 독소조항을 분석하는 Spring Boot REST API 서버입니다.

### 🔧 기술 스택
- **백엔드**: Java 17, Spring Boot 3.5.0, Spring Data JPA
- **데이터베이스**: PostgreSQL
- **클라우드**: AWS Lambda (OCR/분석), S3 (파일 저장)
- **기타**: JWT, OAuth2
