CREATE TABLE if not exists `product`
(
    `product_id`          int unsigned                            NOT NULL AUTO_INCREMENT COMMENT '상품 id',
    `product_name`        varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '상품명',
    `price`               int                                     NOT NULL COMMENT '상품 기본판매가',
    `summary`             varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '상품 요약 설명',
    `thumbnail_image_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '썸네일(대표) 이미지의 s3 cf url',
    `created_at`          datetime                                NOT NULL COMMENT '생성일시',
    `updated_at`          datetime                                NOT NULL COMMENT '수정일시',
    `deleted_at`          datetime                                DEFAULT NULL COMMENT '삭제일시',
    PRIMARY KEY (`product_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 15
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='상품 정보'
;