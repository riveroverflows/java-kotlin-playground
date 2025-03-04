CREATE TABLE `cache_store`
(
    `cache_store_id` bigint                                   NOT NULL AUTO_INCREMENT COMMENT '캐시 ID',
    `cache_key`      varchar(255) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '캐시 키',
    `cache_value`    varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '캐시 값',
    `expires_at`     datetime                                 NOT NULL COMMENT '만료 시각 (TTL 적용)',
    `created_at`     datetime                                 NOT NULL COMMENT '데이터 생성 시각',
    `updated_at`     datetime                                 NOT NULL COMMENT '데이터 수정 시각',
    PRIMARY KEY (`cache_store_id`),
    UNIQUE KEY `uk_cache_store_cache_key` (`cache_key`),
    KEY `idx_cache_store_cache_key` (`cache_key`) COMMENT '빠른 조회를 위한 키 인덱스',
    KEY `idx_cache_store_expires_at` (`expires_at`) COMMENT '만료 데이터 정리를 위한 인덱스'
) ENGINE = InnoDB
  AUTO_INCREMENT = 17
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='로컬 캐시 영속화 용 테이블'
;


CREATE TABLE `user`
(
    `user_id`      int unsigned                            NOT NULL AUTO_INCREMENT,
    `identifier`   varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '로그인 식별 id',
    `phone`        varchar(25) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '국가번호가 포함된 휴대폰 번호',
    `email`        varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '이메일 주소',
    `signup_type`  varchar(10) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '가입 유형(EMAIL 또는 PHONE)',
    `country_code` varchar(2) COLLATE utf8mb4_unicode_ci   DEFAULT NULL COMMENT '국가 코드',
    `password`     varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '비밀번호(해시된 값)',
    `created_at`   datetime                                NOT NULL COMMENT '가입일시',
    `updated_at`   datetime                                NOT NULL COMMENT '수정일시',
    `deleted_at`   datetime                                DEFAULT NULL COMMENT '탈퇴일시',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_user_identifier_deleted_at` (`identifier`, `deleted_at`),
    KEY `idx_user_identifier` (`identifier`) COMMENT 'user 로그인 식별 id 검색용 인덱스'
) ENGINE = InnoDB
  AUTO_INCREMENT = 18
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='유저 테이블'
;