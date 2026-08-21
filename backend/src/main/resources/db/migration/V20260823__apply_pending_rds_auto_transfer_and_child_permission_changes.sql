-- RDS 수동 반영용 통합 마이그레이션입니다.
-- V20260821__allow_parent_savings_auto_transfers.sql 및
-- V20260822__add_child_feature_permissions.sql을 아직 적용하지 않은 RDS에서만 실행합니다.
-- 각 변경은 존재 여부를 확인하므로, 일부 변경이 이미 반영된 RDS에서도 안전하게 실행할 수 있습니다.

DROP PROCEDURE IF EXISTS apply_pending_rds_auto_transfer_and_child_permission_changes;

DELIMITER //

CREATE PROCEDURE apply_pending_rds_auto_transfer_and_child_permission_changes()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'auto_transfer_schedule'
          AND COLUMN_NAME = 'child_id'
          AND IS_NULLABLE = 'NO'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.TABLE_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = DATABASE()
              AND TABLE_NAME = 'auto_transfer_schedule'
              AND CONSTRAINT_NAME = 'fk_auto_transfer_child'
              AND CONSTRAINT_TYPE = 'FOREIGN KEY'
        ) THEN
            ALTER TABLE auto_transfer_schedule
                DROP FOREIGN KEY fk_auto_transfer_child;
        END IF;

        ALTER TABLE auto_transfer_schedule
            MODIFY COLUMN child_id BIGINT UNSIGNED NULL
                COMMENT '자동이체 대상 자녀 ID(자녀 적금인 경우)';

        ALTER TABLE auto_transfer_schedule
            ADD CONSTRAINT fk_auto_transfer_child
                FOREIGN KEY (child_id) REFERENCES child (child_id);
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'auto_transfer_schedule'
          AND COLUMN_NAME = 'financial_goal_id'
          AND IS_NULLABLE = 'NO'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.TABLE_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = DATABASE()
              AND TABLE_NAME = 'auto_transfer_schedule'
              AND CONSTRAINT_NAME = 'fk_auto_transfer_goal'
              AND CONSTRAINT_TYPE = 'FOREIGN KEY'
        ) THEN
            ALTER TABLE auto_transfer_schedule
                DROP FOREIGN KEY fk_auto_transfer_goal;
        END IF;

        ALTER TABLE auto_transfer_schedule
            MODIFY COLUMN financial_goal_id BIGINT UNSIGNED NULL
                COMMENT '연결 금융 목표 ID(있는 경우)';

        ALTER TABLE auto_transfer_schedule
            ADD CONSTRAINT fk_auto_transfer_goal
                FOREIGN KEY (financial_goal_id)
                    REFERENCES financial_goal (financial_goal_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'child'
          AND COLUMN_NAME = 'allowance_request_enabled'
    ) THEN
        ALTER TABLE child
            ADD COLUMN allowance_request_enabled TINYINT(1) NOT NULL DEFAULT 1
                COMMENT '자녀 용돈 요청 권한' AFTER profile_image_url;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'child'
          AND COLUMN_NAME = 'usage_limit_view_enabled'
    ) THEN
        ALTER TABLE child
            ADD COLUMN usage_limit_view_enabled TINYINT(1) NOT NULL DEFAULT 1
                COMMENT '자녀 사용 금액 한도 조회 권한' AFTER allowance_request_enabled;
    END IF;
END //

DELIMITER ;

CALL apply_pending_rds_auto_transfer_and_child_permission_changes();

DROP PROCEDURE apply_pending_rds_auto_transfer_and_child_permission_changes;

-- 실행 후 확인용 쿼리
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND (
      (TABLE_NAME = 'auto_transfer_schedule'
       AND COLUMN_NAME IN ('child_id', 'financial_goal_id'))
      OR (TABLE_NAME = 'child'
          AND COLUMN_NAME IN (
              'allowance_request_enabled',
              'usage_limit_view_enabled'
          ))
  )
ORDER BY TABLE_NAME, ORDINAL_POSITION;
