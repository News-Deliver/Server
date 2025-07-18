-- 1. 사용자
CREATE TABLE user
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    user_id    VARCHAR(255) NOT NULL COMMENT '카카오에서 제공하는 id',
    created_at DATETIME     NOT NULL COMMENT '회원가입한 시간',
    PRIMARY KEY (id)
);

-- 2. 뉴스
CREATE TABLE news
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    sections     VARCHAR(100) NOT NULL COMMENT '섹션',
    title        VARCHAR(255) NOT NULL COMMENT '기사의 제목',
    publisher    VARCHAR(255) NOT NULL COMMENT '발행처',
    summary      MEDIUMTEXT NOT NULL COMMENT '기사의 한줄요약',
    content_url  VARCHAR(255) NOT NULL COMMENT '기사의 url',
    published_at DATETIME     NOT NULL COMMENT '기사가 작성된 날짜',
    send         BOOLEAN      NOT NULL COMMENT '발송 여부',
    PRIMARY KEY (id)
);

-- 3. 설정
CREATE TABLE setting
(
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    delivery_time DATETIME NOT NULL COMMENT '배송받고 싶은 시간',
    start_date    DATETIME NOT NULL COMMENT '배송받고 싶은 시작 기간',
    end_date      DATETIME NULL COMMENT '배송받고 싶은 종료 기간',
    is_deleted    BOOLEAN  NULL COMMENT '설정 삭제여부',
    user_id       BIGINT   NOT NULL COMMENT '설정을 등록한 유저의 고유번호',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- 4. 인증
CREATE TABLE auth
(
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    kakao_refresh_key VARCHAR(255) NOT NULL COMMENT '유효기간 2주',
    user_key          BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_key) REFERENCES user (id)
);

-- 5. 설정 제외 키워드
CREATE TABLE setting_block_keyword
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    block_keyword VARCHAR(255) NOT NULL COMMENT '설정에 대해서만 적용되는 제외하는 키워드',
    setting_id      BIGINT       NOT NULL COMMENT '제외 키워드를 등록한 설정의 고유번호',
    PRIMARY KEY (id),
    FOREIGN KEY (setting_id) REFERENCES setting (id)
);


-- 6. 설정 키워드
CREATE TABLE setting_keyword
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    setting_keyword VARCHAR(255) NOT NULL COMMENT '뉴스를 받아보고 싶은 키워드',
    setting_id      BIGINT       NOT NULL COMMENT '키워드를 등록한 설정의 고유번호',
    PRIMARY KEY (id),
    FOREIGN KEY (setting_id) REFERENCES setting (id)
);

-- 7. 기사 발송 히스토리
CREATE TABLE history
(
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    published_at DATETIME NOT NULL COMMENT '기사가 발송된 날짜',
    setting_id   BIGINT   NOT NULL COMMENT '기사가 발송된 설정의 고유번호',
    news_id      BIGINT   NOT NULL COMMENT '기사가 발송된 기사의 고유번호',
    setting_keyword VARCHAR(255) NOT NULL COMMENT '뉴스를 받아보고 싶은 키워드 히스토리',
    block_keyword VARCHAR(255) NULL COMMENT '설정에 대해서만 적용되는 제외하는 키워드히스토리',
    PRIMARY KEY (id),
    FOREIGN KEY (setting_id) REFERENCES setting (id),
    FOREIGN KEY (news_id) REFERENCES news (id)
);

-- 8. 피드백
CREATE TABLE feedback
(
    id                 BIGINT NOT NULL COMMENT '고유번호',
    keyword_reflection BIGINT NULL COMMENT '키워드 반영도',
    content_quality    BIGINT NULL COMMENT '컨텐츠 품질',
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES history (id)
);

-- 9. 발송 요일
CREATE TABLE days
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    delivery_day INT NOT NULL, -- 1(일요일) ~ 7(토요일)
    setting_id BIGINT NOT NULL,
    FOREIGN KEY (setting_id) REFERENCES setting(id)
);

-- 10. 핫토픽
CREATE TABLE hot_topic
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '고유번호',
    topic_rank BIGINT       NOT NULL COMMENT '핫토픽 순위',
    keyword    VARCHAR(255) NOT NULL COMMENT '핫토픽 키워드',
    keyword_count BIGINT    NOT NULL COMMENT '핫토픽 조회건수',
    topic_date DATETIME     NOT NULL COMMENT '해당날짜(계산일 기준 하루 전)',
    PRIMARY KEY (id)
);

CREATE INDEX idx_news_title ON news(title); -- news 테이블 DELETE 시 title 검색 시
CREATE INDEX idx_news_published_at ON news(published_at);
CREATE INDEX idx_news_publisher ON news(publisher);
-- SELECT에 사용되는 조건
CREATE INDEX idx_news_published_sections ON news(published_at, sections);
-- 중복 삭제에 효율적인 복합 인덱스
CREATE INDEX idx_news_dup ON news(published_at, title, publisher, id);