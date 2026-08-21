ALTER TABLE child
    ADD COLUMN allowance_request_enabled TINYINT(1) NOT NULL DEFAULT 1
        COMMENT '자녀 용돈 요청 권한' AFTER profile_image_url,
    ADD COLUMN usage_limit_view_enabled TINYINT(1) NOT NULL DEFAULT 1
        COMMENT '자녀 사용 금액 한도 조회 권한' AFTER allowance_request_enabled;
