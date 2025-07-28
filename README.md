### 교육 과정 : 멋쟁이 사자처럼 단기 심화 5기 
### 서비스 명 : 멋쟁이 뉴스 배달부 

**프로토타입** : https://merry-crepe-479d93.netlify.app/   
**화면 설계서** : https://www.figma.com/design/b742hXQtI8IqTM3iyirWzR/Untitled?node-id=0-1&p=f&t=BDVVaoSPTOQTkpMc-0


# 멋쟁이 뉴스 배달부 🦁📰

KakaoTalk으로 배달하는 나만의 맞춤형 최신 뉴스 배달 서비스

---

##  Quick Links

- [ 서비스 기획](url) - 핵심 기능, 역할분담, ERD
- [ 개발 가이드](url) - 코드 컨벤션, API 명세, 브랜치 전략
- [ 기술 스택 & 배포](url) - 인프라 구성, Docker, CI/CD
- [ 협업 룰](url) - 팀 규칙, 일정 관리, 품질 기준
- [ 진행 로그](url)
---

## 프로젝트 개요

유저의 관심 키워드와 설정한 시간에 맞춰 맞춤형 뉴스를 KakaoTalk으로 배달,  
국내 **Hot Topic Top 5** 을 제공하는 뉴스 추천 서비스입니다.

### 핵심 서비스

- 맞춤형 뉴스 배달 : 관심 키워드 기반 뉴스를 원하는 시간에 카카오톡으로 전송
- Hot Topic Top 5 : 어제 가장 많이 언급된 키워드와 관련 뉴스 제공
- AI 요약 : Spring AI를 활용한 뉴스 요약 서비스

---

##  주요 기술 스택

### Frontend
- React 18 + TypeScript

### Backend
- Spring Boot 3.x + Spring Security (OAuth2.0)
- Spring AI (뉴스 요약) + Spring Batch (데이터 수집)
- JPA + MyBatis (데이터 접근)

### Database & Search
- MySQL 8.0 (메인 DB) + ElasticSearch 8.x (검색 엔진)
- Redis 7.x (캐싱) + Nori Analyzer (한국어 형태소 분석)

### Infrastructure
- AWS (EC2, RDS, ElasticSearch Service, ElastiCache)
- Docker + GitHub Actions (CI/CD)

---

##  프로토타입

 배포 링크 : [Korean News Service MVP](https://merry-crepe-479d93.netlify.app/)

---

##  프로젝트 목표

### 개발 전략

빠른 MVP 구현 → 알파 테스팅 → 베타 테스팅 → 서비스 고도화

---

##  팀원별 역할 요약

| 역할                             | 담당자   | 주요 업무 및 세부 기능 |
|----------------------------------|----------|--------------------------|
| **Backend - Auth**              | 문준원   | - 카카오 소셜 로그인 (OAuth2)<br>- JWT 토큰 발급 및 검증<br>- 사용자 정보/권한 관리<br>- 설정 관련 사용자 연동 처리<br>뉴스 수신 시간/요일/키워드 설정<br>- 설정 정보 저장 및 유효성 검증<br>- 뉴스 발송 스케줄러 구현(Spring Scheduler) |
| **Backend - HotTopic**          | 김원중   | - ElasticSearch 기반 키워드 집계<br>- Nori 형태소 분석기 적용<br>- Top 키워드 API<br>- 키워드 기반 뉴스 검색 기능 |
| **Backend - Message**           | 정다음   | - 뉴스 발송 요청 처리<br>- KakaoTalk API 연동<br>- 뉴스 발송 이력 저장 (History 관리) |
| **Backend + Frontend - Sub & PM**          | 류성열   | - 더보기 기능: 키워드 기반 추가 뉴스 수집 (ElasticSearch)<br>- AI 요약: Top 5 기사 자동 요약<br>- 키워드 필터링 정책 수립<br>- 전체 기획/요구사항 정의 및 UX 설계<br>- 프로젝트 일정 관리 |

##  핵심 참고 사항

- 사용자 설정은 최대 3개까지 등록 가능
- Hot Topic 키워드 5개 선정 - 각 topic 별 20개 기사 추출
..

---

## 주요 링크

### 외부 API
- Kakao Developers: [[카카오 개발자 문서](https://developers.kakao.com/)](https://developers.kakao.com/)
---

## 최근 업데이트

- 2025.07.05 :
  - 🔹 초기 프로젝트 생성 및 GitHub 업로드
  - 🔹 `application.properties` 프로필(dev/prod) 분리
  - 🔹 예외 처리 시스템 구축 (GlobalExceptionHandler 등)
  - 🔹 공통 응답 객체 클래스(Global Response) 구현
  - 🔹 DB 연결 임시 해제로 초기 버그 대응
  - 🔹 `.env` 파일 구현 (보안상 비공개, 공유 예정)
- 2025.07.03 : 팀 구성 및 역할 분담 회의 진행  
- 2025.07.01 : 프로젝트 기획 시작

---

