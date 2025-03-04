CREATE TABLE if not exists `product_variant`
(
    `product_variant_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '상품 품목 id',
    `product_id`         int unsigned NOT NULL COMMENT '상품 id',
    `remain_qty`         int          NOT NULL COMMENT '잔여 수량',
    `created_at`         datetime     NOT NULL COMMENT '생성일시',
    `updated_at`         datetime     NOT NULL COMMENT '수정일시',
    `deleted_at`         datetime DEFAULT NULL COMMENT '삭제일시',
    PRIMARY KEY (`product_variant_id`),
    KEY `fk_product_variant_product` (`product_id`),
    CONSTRAINT `fk_product_variant_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1308
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='상품 품목'
;