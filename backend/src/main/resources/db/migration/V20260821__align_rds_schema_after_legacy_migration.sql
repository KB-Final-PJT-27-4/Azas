-- RDS 스키마를 현재 backend/src/main/resources/db/schema.sql의 비파괴 변경분과 정렬합니다.
--
-- 적용 대상: 2026-08-21에 추출한 azas-rds-schema-20260821.sql과 같은 RDS 구조
-- 선행 조건: RDS 수동 스냅샷이 AVAILABLE 상태여야 합니다.
-- 주의:
--   * 이 파일은 현재 RDS 구조를 기준으로 한 "한 번만" 실행하는 수동 마이그레이션입니다.
--   * DROP TABLE, DELETE, 기존 업무 데이터 변경은 포함하지 않습니다.
--   * 아래 실행이 끝난 뒤 checklist-content-seed.sql 및
--     financial-goal-amount-recommendations.sql을 각각 실행해야 콘텐츠/추천 데이터가 채워집니다.
--   * 전체 schema.sql은 절대 RDS에서 실행하지 마세요. schema.sql에는 초기화용 DROP 구문이 있습니다.

USE azas;

-- 기존 데이터가 새 제약조건을 만족하는지 먼저 검증합니다.
-- 하나라도 위반하면 이후 DDL을 실행하지 않고 즉시 중단합니다.
DELIMITER //

DROP PROCEDURE IF EXISTS assert_rds_schema_alignment_preconditions //

CREATE PROCEDURE assert_rds_schema_alignment_preconditions()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM child_checklist_item
        WHERE (status = 'PENDING' AND (completed_by_member_id IS NOT NULL OR completed_at IS NOT NULL))
           OR (status = 'COMPLETED' AND (completed_by_member_id IS NULL OR completed_at IS NULL))
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'child_checklist_item completion data is inconsistent; review before migration.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM auto_transfer_schedule
        WHERE amount <= 0
           OR transfer_day NOT BETWEEN 1 AND 28
           OR (end_date IS NOT NULL AND end_date < start_date)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'auto_transfer_schedule data violates the current schema constraints; review before migration.';
    END IF;
END //

DELIMITER ;

CALL assert_rds_schema_alignment_preconditions();
DROP PROCEDURE assert_rds_schema_alignment_preconditions;

-- 1. 체크리스트: 코드 조회에 필요한 컬럼을 추가합니다.
-- 기존 행은 seed 실행 전까지 안전한 기본값을 사용하고, seed가 실제 콘텐츠 값으로 갱신합니다.
ALTER TABLE checklist_item_template
    ADD COLUMN template_key VARCHAR(100) NOT NULL DEFAULT '' AFTER is_active,
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'SERVICE' AFTER template_key,
    ADD COLUMN action_type VARCHAR(20) NOT NULL DEFAULT 'INFO' AFTER category,
    ADD COLUMN action_url VARCHAR(2048) NULL AFTER action_type,
    ADD COLUMN info_title VARCHAR(150) NULL AFTER action_url,
    ADD COLUMN info_notice VARCHAR(500) NULL AFTER info_title;

UPDATE checklist_item_template
SET template_key = CONCAT('legacy-template-', checklist_item_template_id)
WHERE template_key = '';

ALTER TABLE checklist_item_template
    DROP CHECK ck_checklist_template_lifecycle_stage,
    ADD CONSTRAINT ck_checklist_template_lifecycle_stage
        CHECK (
            lifecycle_stage IN (
                'PREGNANCY',
                'AGE_0_TO_1',
                'AGE_2_TO_4',
                'AGE_5_TO_7',
                'AGE_8_TO_10',
                'AGE_11_TO_13',
                'AGE_14_TO_16',
                'AGE_17_TO_19'
            )
        ),
    ADD CONSTRAINT ck_checklist_template_item_order
        CHECK (item_order > 0);

ALTER TABLE checklist_item_detail
    ADD COLUMN action_label VARCHAR(100) NULL AFTER description,
    ADD COLUMN external_url VARCHAR(2048) NULL AFTER action_label,
    ADD COLUMN detail_content TEXT NULL AFTER external_url;

ALTER TABLE child_checklist_item
    ADD KEY idx_child_checklist_child_status (child_id, status),
    ADD CONSTRAINT ck_child_checklist_completion
        CHECK (
            (status = 'PENDING' AND completed_by_member_id IS NULL AND completed_at IS NULL)
            OR (status = 'COMPLETED' AND completed_by_member_id IS NOT NULL AND completed_at IS NOT NULL)
        );

-- 2. 목표 금액 추천: 현재 API/시드가 기대하는 테이블을 생성합니다.
CREATE TABLE IF NOT EXISTS financial_goal_recommendation_basis
(
    financial_goal_recommendation_basis_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    financial_goal_template_id             BIGINT UNSIGNED NOT NULL,
    recommendation_method                  VARCHAR(30)     NOT NULL,
    organization                           VARCHAR(200)    NULL,
    dataset_name                           VARCHAR(300)    NULL,
    reference_year                         SMALLINT UNSIGNED NULL,
    metric_name                            VARCHAR(200)    NULL,
    metric_value                           DECIMAL(19, 2)  NULL,
    metric_unit                            VARCHAR(50)     NULL,
    source_url                             VARCHAR(1000)   NULL,
    description                            VARCHAR(1000)   NOT NULL,
    disclaimer                             VARCHAR(1000)   NOT NULL,
    created_at                             DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                             DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (financial_goal_recommendation_basis_id),
    UNIQUE KEY uk_goal_recommendation_basis_template (financial_goal_template_id),
    CONSTRAINT fk_goal_recommendation_basis_template
        FOREIGN KEY (financial_goal_template_id) REFERENCES financial_goal_template (financial_goal_template_id),
    CONSTRAINT ck_goal_recommendation_method
        CHECK (recommendation_method IN ('STATISTICS_REFERENCE', 'SERVICE_SCENARIO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS financial_goal_amount_recommendation
(
    financial_goal_amount_recommendation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    financial_goal_template_id              BIGINT UNSIGNED NOT NULL,
    recommendation_code                     VARCHAR(30)     NOT NULL,
    title                                   VARCHAR(100)    NOT NULL,
    target_amount                           DECIMAL(19, 2)  NOT NULL,
    coverage_items                          VARCHAR(1000)   NOT NULL,
    display_order                           INT             NOT NULL,
    is_active                               TINYINT(1)      NOT NULL DEFAULT 1,
    created_at                              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (financial_goal_amount_recommendation_id),
    UNIQUE KEY uk_goal_amount_recommendation_code (financial_goal_template_id, recommendation_code),
    KEY idx_goal_amount_recommendation_display (financial_goal_template_id, is_active, display_order),
    CONSTRAINT fk_goal_amount_recommendation_template
        FOREIGN KEY (financial_goal_template_id) REFERENCES financial_goal_template (financial_goal_template_id),
    CONSTRAINT ck_goal_amount_recommendation_amount CHECK (target_amount > 0),
    CONSTRAINT ck_goal_amount_recommendation_code
        CHECK (recommendation_code IN ('STARTER', 'BALANCED', 'SECURE', 'LIFECYCLE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 3. 푸시 알림: FCM 기기 토큰 저장 테이블을 생성합니다.
CREATE TABLE IF NOT EXISTS push_device
(
    push_device_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    member_id        BIGINT UNSIGNED NOT NULL,
    device_key       VARCHAR(100)    NOT NULL,
    platform         VARCHAR(20)     NOT NULL,
    provider         VARCHAR(20)     NOT NULL DEFAULT 'FCM',
    device_name      VARCHAR(100)    NULL,
    token_ciphertext VARBINARY(8192) NOT NULL,
    token_hash       CHAR(64)        NOT NULL,
    is_active        TINYINT(1)      NOT NULL DEFAULT 1,
    last_seen_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (push_device_id),
    UNIQUE KEY uk_push_device_member_device_key (member_id, device_key),
    KEY idx_push_device_token_hash (token_hash),
    KEY idx_push_device_member_active (member_id, is_active),
    CONSTRAINT fk_push_device_member
        FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE,
    CONSTRAINT ck_push_device_platform CHECK (platform IN ('WEB', 'ANDROID', 'IOS')),
    CONSTRAINT ck_push_device_provider CHECK (provider IN ('FCM'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 4. 자동이체·알림: 누락된 데이터 무결성/조회 인덱스를 보강합니다.
ALTER TABLE auto_transfer_schedule
    ADD CONSTRAINT ck_auto_transfer_amount CHECK (amount > 0),
    ADD CONSTRAINT ck_auto_transfer_day CHECK (transfer_day BETWEEN 1 AND 28),
    ADD CONSTRAINT ck_auto_transfer_date_range CHECK (end_date IS NULL OR end_date >= start_date);

ALTER TABLE notification
    ADD KEY idx_notification_member_id (member_id, notification_id),
    ADD KEY idx_notification_member_read_id (member_id, is_read, notification_id),
    ADD KEY idx_notification_member_category_id (member_id, notification_category, notification_id);

-- 5. 적용 후 검증. 모든 count가 0이어야 하며, 추천 테이블은 존재해야 합니다.
SELECT 'checklist_template_missing_values' AS check_name, COUNT(*) AS invalid_count
FROM checklist_item_template
WHERE template_key = '' OR category = '' OR action_type = ''
UNION ALL
SELECT 'invalid_child_checklist_completion', COUNT(*)
FROM child_checklist_item
WHERE (status = 'PENDING' AND (completed_by_member_id IS NOT NULL OR completed_at IS NOT NULL))
   OR (status = 'COMPLETED' AND (completed_by_member_id IS NULL OR completed_at IS NULL));

SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'financial_goal_recommendation_basis',
    'financial_goal_amount_recommendation',
    'push_device'
  )
ORDER BY table_name;

-- 다음 두 파일은 이 파일 실행 후 별도로 실행합니다.
-- 1) backend/src/main/resources/db/checklist-content-seed.sql
-- 2) backend/src/main/resources/db/financial-goal-amount-recommendations.sql
