-- RDS 수동 반영용 통합 마이그레이션입니다.
-- V20260821__allow_parent_savings_auto_transfers.sql 및
-- V20260822__add_child_feature_permissions.sql과 보호자 초대 대상 자녀 스냅샷 변경을
-- 아직 적용하지 않은 RDS에서만 실행합니다.
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

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'family_invitation_child'
    ) THEN
        CREATE TABLE family_invitation_child
        (
            family_invitation_id BIGINT UNSIGNED NOT NULL COMMENT '가족 초대 ID',
            child_id             BIGINT UNSIGNED NOT NULL COMMENT '초대 대상 자녀 ID',
            created_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '대상 스냅샷 생성 시각',
            PRIMARY KEY (family_invitation_id, child_id),
            KEY idx_family_invitation_child_child_id (child_id),
            CONSTRAINT fk_family_invitation_child_invitation
                FOREIGN KEY (family_invitation_id)
                    REFERENCES family_invitation (family_invitation_id),
            CONSTRAINT fk_family_invitation_child_target
                FOREIGN KEY (child_id)
                    REFERENCES child (child_id)
        ) ENGINE = InnoDB
          DEFAULT CHARSET = utf8mb4
          COLLATE = utf8mb4_unicode_ci COMMENT ='가족 초대 대상 자녀 스냅샷';
    END IF;

    INSERT INTO family_invitation_child (
        family_invitation_id,
        child_id,
        created_at
    )
    SELECT
        fi.family_invitation_id,
        fi.child_id,
        fi.created_at
    FROM family_invitation fi
             LEFT JOIN family_invitation_child fic
                       ON fic.family_invitation_id = fi.family_invitation_id
                           AND fic.child_id = fi.child_id
    WHERE fic.family_invitation_id IS NULL;

    -- A child account can be created before that child completes social sign-up.
    -- Keep the RDS constraint aligned with schema.sql, where owner_member_id is
    -- optional for CHILD accounts but always required for PARENT accounts.
    IF EXISTS (
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'financial_account'
          AND CONSTRAINT_NAME = 'ck_financial_account_owner'
          AND CONSTRAINT_TYPE = 'CHECK'
    ) THEN
        ALTER TABLE financial_account
            DROP CHECK ck_financial_account_owner;
    END IF;

    ALTER TABLE financial_account
        ADD CONSTRAINT ck_financial_account_owner
            CHECK (
                (owner_type = 'PARENT'
                    AND owner_member_id IS NOT NULL
                    AND child_id IS NULL)
                OR
                (owner_type = 'CHILD'
                    AND child_id IS NOT NULL)
            );

    -- Existing profiles in the same household may have been created before
    -- common guardian propagation was introduced. Expand the relation graph
    -- until every child sharing one guardian has the same active guardians.
    SET @inserted_child_parent_count = 1;

    WHILE @inserted_child_parent_count > 0 DO
        INSERT IGNORE INTO child_parent (
            child_id,
            member_id,
            relation_type,
            created_at,
            updated_at
        )
        SELECT DISTINCT
            sibling.child_id,
            guardian.member_id,
            guardian.relation_type,
            CURRENT_TIMESTAMP(6),
            CURRENT_TIMESTAMP(6)
        FROM child_parent shared_guardian
                 INNER JOIN child_parent guardian
                            ON guardian.child_id = shared_guardian.child_id
                 INNER JOIN child_parent sibling
                            ON sibling.member_id = shared_guardian.member_id
                 INNER JOIN child source_child
                            ON source_child.child_id = shared_guardian.child_id
                 INNER JOIN child target_child
                            ON target_child.child_id = sibling.child_id
                 INNER JOIN member guardian_member
                            ON guardian_member.member_id = guardian.member_id
        WHERE source_child.status = 'ACTIVE'
          AND target_child.status = 'ACTIVE'
          AND guardian_member.status = 'ACTIVE';

        SET @inserted_child_parent_count = ROW_COUNT();
    END WHILE;

    -- Account names are snapshots of the selected Mock financial product.
    -- Earlier application versions overwrote demand-deposit names by owner
    -- type, so align existing product-linked accounts before new deployments.
    UPDATE financial_account fa
             INNER JOIN financial_product fp
                        ON fp.financial_product_id = fa.financial_product_id
    SET fa.account_name = fp.name,
        fa.updated_at = CURRENT_TIMESTAMP(6)
    WHERE fp.name IS NOT NULL
      AND TRIM(fp.name) <> ''
      AND fa.account_name <> fp.name;
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
      OR (TABLE_NAME = 'family_invitation_child'
          AND COLUMN_NAME IN (
              'family_invitation_id',
              'child_id'
          ))
  )
ORDER BY TABLE_NAME, ORDINAL_POSITION;
