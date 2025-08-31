### 교육 과정 : 멋쟁이 사자처럼 단기 심화 5기 
### 서비스 명 : 멋쟁이 뉴스 배달부 

**프로토타입** : https://merry-crepe-479d93.netlify.app/   
**화면 설계서** : https://www.figma.com/design/b742hXQtI8IqTM3iyirWzR/Untitled?node-id=0-1&p=f&t=BDVVaoSPTOQTkpMc-0



# 멋쟁이 뉴스 배달부 🦁📰

KakaoTalk으로 배달하는 나만의 맞춤형 최신 뉴스 배달 서비스

## 동영상
https://github.com/user-attachments/assets/45e5fe34-3ae1-4d19-98b8-a11fb84ffce4




## 이미지
<img width="2512" height="1316" alt="스크린샷 2025-07-28 152753" src="https://github.com/user-attachments/assets/eb6326e0-3b81-4b1c-9eec-af6eac7883d4" />
<img width="1897" height="1051" alt="스크린샷 2025-07-28 152815" src="https://github.com/user-attachments/assets/23fc7c9f-3ca1-4928-becf-831fee33a200" />
<img width="1876" height="1060" alt="스크린샷 2025-07-28 152825" src="https://github.com/user-attachments/assets/7d01d799-ebd4-45dc-98da-8a2c77871826" />
<img width="1888" height="1049" alt="스크린샷 2025-07-28 152834" src="https://github.com/user-attachments/assets/652d6753-8482-49bf-80d4-4a36b9b9e223" />
<img width="1884" height="1050" alt="스크린샷 2025-07-28 152842" src="https://github.com/user-attachments/assets/1b34df44-cd1b-45a9-a22f-4616c7b76a95" />
<img width="1881" height="1057" alt="스크린샷 2025-07-28 152851" src="https://github.com/user-attachments/assets/27a29b16-4b82-43a4-9b2f-e1b41f3279a1" />
<img width="1884" height="1058" alt="스크린샷 2025-07-28 152917" src="https://github.com/user-attachments/assets/3142ca43-2066-47bb-b055-4732c83f6e5a" />
<img width="1877" height="1028" alt="스크린샷 2025-07-28 152925" src="https://github.com/user-attachments/assets/b87faa38-6623-498b-99ea-d32e6ca73f08" />

---
## 시스템 아키텍쳐
<img width="1503" height="813" alt="image" src="https://github.com/user-attachments/assets/c808f894-5015-4f01-8d51-f401d8c47673" />


## 프로젝트 개요

유저의 관심 키워드와 설정한 시간에 맞춰 맞춤형 뉴스를 KakaoTalk으로 배달,  
국내 **Hot Topic Top 5** 을 제공하는 뉴스 추천 서비스입니다.

### 핵심 서비스

- 맞춤형 뉴스 배달 : 관심 키워드 기반 뉴스를 원하는 시간에 카카오톡으로 전송
- Hot Topic Top 5 : 어제 가장 많이 언급된 키워드와 관련 뉴스 제공
- AI 요약 : Spring AI를 활용한 뉴스 요약 서비스



##  팀원별 역할 요약

| 역할                             | 담당자   | 주요 업무 및 세부 기능 |
|----------------------------------|----------|--------------------------|
| **Backend - Auth**              | 문준원   | - 카카오 소셜 로그인 (OAuth2)<br>- JWT 토큰 발급 및 검증<br>- 사용자 정보/권한 관리 <br>- CICD 파이프라인 구축  <br>- AWS 환경구축 |
| **Backend - HotTopic**          | 김원중   | - ElasticSearch 기반 키워드 집계<br>- Nori 형태소 분석기 적용<br>- Top 키워드 API<br>- 키워드 기반 뉴스 검색 기능 <br>- 설정 관련 사용자 연동 처리<br>뉴스 수신 시간/요일/키워드 설정<br>- 설정 정보 저장 및 유효성 검증<br>- 뉴스 발송 스케줄러 구현(Spring Scheduler)|
| **Backend - Message**           | 정다음   | - 뉴스 발송 요청 처리<br>- KakaoTalk API 연동<br>- 뉴스 발송 이력 저장 (History 관리) |
| **Backend + Frontend - Sub & PM**          | 류성열   | - 더보기 기능: 키워드 기반 추가 뉴스 수집 (ElasticSearch)<br>- AI 요약: Top 5 기사 자동 요약<br>- 키워드 필터링 정책 수립<br>- 전체 기획/요구사항 정의 및 UX 설계<br>- 프로젝트 일정 관리  <br>- 뉴스 발송 요청 처리<br>- KakaoTalk API 연동<br>- 뉴스 발송 이력 저장 (History 관리) |


