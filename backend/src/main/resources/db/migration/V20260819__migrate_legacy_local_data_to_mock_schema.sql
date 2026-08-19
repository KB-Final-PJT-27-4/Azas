-- Legacy local-data migration for the 2026-08-19 Azas backup.
--
-- IMPORTANT
--   * Run this script ONCE against the copied working database only:
--       azas_migrated_20260819
--   * Do NOT run it against `azas` (the original local database) or
--       `azas_legacy_20260819` (the untouched restored backup).
--   * This is intentionally not an application-start migration. Execute it in
--       MySQL Workbench after selecting the copied database and take a backup
--       of that database first.
--
-- The script preserves existing business rows while moving the legacy external
-- financial-connection model to the current Mock-account schema.

DELIMITER //

DROP PROCEDURE IF EXISTS assert_local_migration_target //
CREATE PROCEDURE assert_local_migration_target()
BEGIN
    IF DATABASE() IS NULL
        OR DATABASE() IN ('azas', 'azas_legacy_20260819')
        OR DATABASE() NOT LIKE 'azas_migrated_%' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Run only on a copied azas_migrated_* database, never azas or azas_legacy_20260819.';
    END IF;
END //

CALL assert_local_migration_target() //
DROP PROCEDURE assert_local_migration_target //

DELIMITER ;

-- MySQL DDL statements commit implicitly. This is why the script must run on
-- the disposable copied database rather than the original local database.

-- 1. Preserve the legacy savings-goal snapshot for account 3.  The supplied
--    backup already has the other snapshot goals normalized; account 3 is the
--    sole snapshot that may need a financial_goal/financial_goal_account row.
DROP TEMPORARY TABLE IF EXISTS legacy_snapshot_goal_account;
CREATE TEMPORARY TABLE legacy_snapshot_goal_account AS
SELECT
    fa.financial_account_id,
    fa.child_id,
    fa.financial_goal_template_id,
    fa.goal_name_snapshot,
    fa.goal_target_amount,
    fa.goal_target_date,
    COALESCE(
        (
            SELECT ats.amount
            FROM auto_transfer_schedule ats
            WHERE ats.destination_account_id = fa.financial_account_id
              AND ats.status IN ('ACTIVE', 'PAUSED')
            ORDER BY ats.auto_transfer_schedule_id
            LIMIT 1
        ),
        CEILING(
            fa.goal_target_amount / GREATEST(
                TIMESTAMPDIFF(MONTH, CURRENT_DATE, fa.goal_target_date),
                1
            )
        )
    ) AS monthly_saving_amount
FROM financial_account fa
WHERE fa.financial_account_id = 3
  AND fa.child_id = 1
  AND fa.account_product_type = 'SAVINGS'
  AND fa.goal_name_snapshot = '대학자금 마련'
  AND fa.goal_target_amount = 30000000.00
  AND fa.goal_target_date = '2038-01-12';

DROP TEMPORARY TABLE IF EXISTS missing_legacy_snapshot_goal_account;
CREATE TEMPORARY TABLE missing_legacy_snapshot_goal_account AS
SELECT lsg.*
FROM legacy_snapshot_goal_account lsg
LEFT JOIN financial_goal_account fga
    ON fga.financial_account_id = lsg.financial_account_id
WHERE fga.financial_account_id IS NULL;

-- A fresh copy has one missing link. A partially executed copy may already
-- have it. Both states are safe; anything else is not this known backup.
DELIMITER //
DROP PROCEDURE IF EXISTS assert_snapshot_goal_state //
CREATE PROCEDURE assert_snapshot_goal_state()
BEGIN
    DECLARE reference_count INT;
    DECLARE missing_count INT;
    DECLARE linked_count INT;

    SELECT COUNT(*) INTO reference_count FROM legacy_snapshot_goal_account;
    SELECT COUNT(*) INTO missing_count FROM missing_legacy_snapshot_goal_account;
    SELECT COUNT(*) INTO linked_count
    FROM financial_goal_account fga
    JOIN legacy_snapshot_goal_account lsg
        ON lsg.financial_account_id = fga.financial_account_id;

    IF reference_count <> 1 OR missing_count > 1 OR linked_count > 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Unexpected legacy account-3 goal state. Restore the copied DB and review the migration baseline.';
    END IF;
END //
CALL assert_snapshot_goal_state() //
DROP PROCEDURE assert_snapshot_goal_state //
DELIMITER ;

INSERT INTO financial_goal (
    child_id,
    financial_goal_template_id,
    title,
    target_amount,
    target_date,
    monthly_saving_amount,
    status,
    created_at,
    updated_at
)
SELECT
    child_id,
    financial_goal_template_id,
    goal_name_snapshot,
    goal_target_amount,
    goal_target_date,
    monthly_saving_amount,
    'ACTIVE',
    UTC_TIMESTAMP(6),
    UTC_TIMESTAMP(6)
FROM missing_legacy_snapshot_goal_account;

SET @migrated_snapshot_goal_id = LAST_INSERT_ID();

INSERT INTO financial_goal_account (
    financial_goal_id,
    financial_account_id,
    linked_at
)
SELECT
    @migrated_snapshot_goal_id,
    financial_account_id,
    UTC_TIMESTAMP(6)
FROM missing_legacy_snapshot_goal_account;

SELECT fga.financial_goal_id INTO @migrated_snapshot_goal_id
FROM financial_goal_account fga
JOIN legacy_snapshot_goal_account lsg
    ON lsg.financial_account_id = fga.financial_account_id;

INSERT IGNORE INTO financial_goal_checkpoint (
    financial_goal_id,
    percentage,
    target_amount,
    reached_at
)
SELECT
    @migrated_snapshot_goal_id,
    checkpoint_percent,
    ROUND(lsg.goal_target_amount * checkpoint_percent / 100, 2),
    NULL
FROM (
    SELECT 10 AS checkpoint_percent
    UNION ALL SELECT 25
    UNION ALL SELECT 50
    UNION ALL SELECT 75
    UNION ALL SELECT 100
) checkpoints
CROSS JOIN legacy_snapshot_goal_account lsg;

-- 2. Convert the old transaction source/time fields before their constraints
--    are replaced.
UPDATE account_transaction
SET source_type = CASE source_type
    WHEN 'IMPORTED' THEN 'MOCK'
    ELSE source_type
END;

ALTER TABLE account_transaction
    CHANGE COLUMN synced_at recorded_at DATETIME(6) NOT NULL;

ALTER TABLE account_transaction
    ADD CONSTRAINT ck_account_transaction_source_type
        CHECK (source_type IN ('MOCK', 'TRANSFER', 'AUTO_TRANSFER'));

-- 3. Fill owner information from the legacy connection once, before that
--    model is removed. Existing owner values always take precedence.
UPDATE financial_account fa
LEFT JOIN financial_connection fc
    ON fc.financial_connection_id = fa.financial_connection_id
SET fa.owner_member_id = COALESCE(fa.owner_member_id, fc.connected_by_member_id),
    fa.child_id = CASE
        WHEN fa.owner_type = 'PARENT' THEN NULL
        ELSE COALESCE(fa.child_id, fc.child_id)
    END
WHERE fa.owner_member_id IS NULL
   OR (fa.owner_type = 'PARENT' AND fa.child_id IS NOT NULL);

DELIMITER //
DROP PROCEDURE IF EXISTS assert_financial_account_owners //
CREATE PROCEDURE assert_financial_account_owners()
BEGIN
    DECLARE invalid_count INT;
    SELECT COUNT(*) INTO invalid_count
    FROM financial_account
    WHERE owner_member_id IS NULL
       OR (owner_type = 'PARENT' AND child_id IS NOT NULL)
       OR (owner_type = 'CHILD' AND child_id IS NULL);
    IF invalid_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot infer all financial-account owners. Fix owner_member_id/child_id in the copied DB and rerun from a fresh copy.';
    END IF;
END //
CALL assert_financial_account_owners() //
DROP PROCEDURE assert_financial_account_owners //
DELIMITER ;

-- 4. Existing transfers may remain unrelated to a goal. The newer goal/origin and
-- credit-transaction fields are nullable, so no transfer history is discarded.
ALTER TABLE financial_transfer
    ADD COLUMN financial_goal_id BIGINT UNSIGNED NULL AFTER updated_at,
    ADD COLUMN origin_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL' AFTER financial_goal_id,
    ADD COLUMN origin_id BIGINT UNSIGNED NULL AFTER origin_type,
    ADD COLUMN retry_of_transfer_id BIGINT UNSIGNED NULL AFTER origin_id,
    ADD COLUMN credit_transaction_id BIGINT UNSIGNED NULL AFTER retry_of_transfer_id,
    ADD KEY idx_financial_transfer_origin (origin_type, origin_id),
    ADD KEY idx_financial_transfer_retry_of (retry_of_transfer_id),
    ADD CONSTRAINT fk_financial_transfer_goal
        FOREIGN KEY (financial_goal_id) REFERENCES financial_goal (financial_goal_id),
    ADD CONSTRAINT fk_financial_transfer_retry_of
        FOREIGN KEY (retry_of_transfer_id) REFERENCES financial_transfer (financial_transfer_id),
    ADD CONSTRAINT fk_financial_transfer_credit_transaction
        FOREIGN KEY (credit_transaction_id) REFERENCES account_transaction (account_transaction_id),
    ADD CONSTRAINT ck_financial_transfer_origin_type
        CHECK (origin_type IN ('MANUAL', 'ALLOWANCE_REQUEST', 'MISSION', 'AUTO_TRANSFER_SCHEDULE'));

UPDATE financial_transfer ft
JOIN legacy_snapshot_goal_account lsg
    ON lsg.financial_account_id = ft.destination_account_id
SET ft.financial_goal_id = @migrated_snapshot_goal_id
WHERE ft.financial_goal_id IS NULL;

-- 5. Bring the saved automatic-transfer structure to the current Mock model.
-- The 2026-08-19 backup does not have financial_goal_id. Check first so this
-- script also explains the state clearly if it is inspected after a failed run.
SET @auto_transfer_has_goal_id = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'auto_transfer_schedule'
      AND column_name = 'financial_goal_id'
);
SET @add_auto_transfer_goal_id_sql = IF(
    @auto_transfer_has_goal_id = 0,
    'ALTER TABLE auto_transfer_schedule ADD COLUMN financial_goal_id BIGINT UNSIGNED NULL AFTER member_id',
    'SELECT 1'
);
PREPARE add_auto_transfer_goal_id_statement FROM @add_auto_transfer_goal_id_sql;
EXECUTE add_auto_transfer_goal_id_statement;
DEALLOCATE PREPARE add_auto_transfer_goal_id_statement;

ALTER TABLE auto_transfer_schedule
    ADD COLUMN request_idempotency_key CHAR(36) NULL AFTER member_id,
    ADD COLUMN last_transfer_status VARCHAR(20) NULL AFTER next_transfer_at,
    ADD COLUMN last_transferred_at DATETIME(6) NULL AFTER last_transfer_status;

UPDATE auto_transfer_schedule ats
JOIN legacy_snapshot_goal_account lsg
    ON lsg.financial_account_id = ats.destination_account_id
SET ats.request_idempotency_key = COALESCE(ats.request_idempotency_key, UUID()),
    ats.financial_goal_id = COALESCE(ats.financial_goal_id, @migrated_snapshot_goal_id);

ALTER TABLE auto_transfer_schedule
    DROP FOREIGN KEY fk_auto_transfer_child;

ALTER TABLE auto_transfer_schedule
    MODIFY COLUMN child_id BIGINT UNSIGNED NOT NULL,
    MODIFY COLUMN request_idempotency_key CHAR(36) NOT NULL,
    MODIFY COLUMN financial_goal_id BIGINT UNSIGNED NOT NULL,
    ADD UNIQUE KEY uk_auto_transfer_request_key (request_idempotency_key),
    ADD KEY idx_auto_transfer_goal_id (financial_goal_id),
    ADD CONSTRAINT fk_auto_transfer_goal
        FOREIGN KEY (financial_goal_id) REFERENCES financial_goal (financial_goal_id),
    DROP CHECK ck_auto_transfer_frequency,
    ADD CONSTRAINT ck_auto_transfer_frequency CHECK (frequency = 'MONTHLY'),
    ADD CONSTRAINT ck_auto_transfer_last_status
        CHECK (last_transfer_status IS NULL OR last_transfer_status IN ('SUCCEEDED', 'FAILED')),
    ADD CONSTRAINT fk_auto_transfer_child
        FOREIGN KEY (child_id) REFERENCES child (child_id);

ALTER TABLE auto_transfer_schedule
    DROP COLUMN provider_schedule_id;

-- 6. Keep legacy checklist completion rows. The old templates have no
--    lifecycle metadata. AGE_5_TO_7 is an explicit conservative migration
--    mapping for their existing account/transfer guidance; review this mapping
--    before execution if those test rows are meaningful to your demo.
ALTER TABLE checklist_item_template
    ADD COLUMN lifecycle_stage VARCHAR(30) NULL AFTER checklist_item_template_id,
    ADD COLUMN detail_content TEXT NULL AFTER description;

UPDATE checklist_item_template
SET lifecycle_stage = 'AGE_5_TO_7',
    detail_content = COALESCE(detail_content, description);

ALTER TABLE checklist_item_template
    MODIFY COLUMN lifecycle_stage VARCHAR(30) NOT NULL,
    ADD UNIQUE KEY uk_checklist_template_stage_order (lifecycle_stage, item_order),
    ADD KEY idx_checklist_template_stage_active_order (lifecycle_stage, is_active, item_order),
    ADD CONSTRAINT ck_checklist_template_lifecycle_stage
        CHECK (lifecycle_stage IN ('PREGNANCY', 'AGE_0_TO_1', 'AGE_2_TO_4', 'AGE_5_TO_7'));

CREATE TABLE checklist_item_detail (
    checklist_item_detail_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    checklist_item_template_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    item_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (checklist_item_detail_id),
    UNIQUE KEY uk_checklist_detail_template_order (checklist_item_template_id, item_order),
    CONSTRAINT fk_checklist_detail_template
        FOREIGN KEY (checklist_item_template_id)
        REFERENCES checklist_item_template (checklist_item_template_id)
        ON DELETE CASCADE,
    CONSTRAINT ck_checklist_detail_item_order CHECK (item_order > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Normalize single-image time-capsule media without generating or deleting
--    S3 objects. Existing backup media is already one IMAGE in slot 1.
ALTER TABLE time_capsule_entry
    DROP COLUMN blurred_thumbnail_object_key,
    DROP COLUMN edit_count,
    DROP CHECK ck_capsule_entry_media_mode,
    ADD CONSTRAINT ck_capsule_entry_media_mode CHECK (media_mode IN ('NONE', 'IMAGE'));

ALTER TABLE time_capsule_media
    DROP CHECK ck_capsule_media_type,
    ADD CONSTRAINT ck_capsule_media_type CHECK (media_type = 'IMAGE'),
    ADD CONSTRAINT ck_capsule_media_slot CHECK (slot_no = 1);

-- 8. Convert notification preference/type names while retaining read history.
-- The legacy composite unique key is also the only usable index for the
-- member foreign key. Add a temporary supporting index before replacing it.
ALTER TABLE notification_preference
    ADD KEY idx_notification_preference_member_id (member_id);

ALTER TABLE notification_preference
    DROP INDEX uk_notification_preference_member_type,
    CHANGE COLUMN notification_type notification_category VARCHAR(50) NOT NULL,
    ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1 AFTER notification_category;

UPDATE notification_preference
SET notification_category = CASE notification_category
    WHEN 'TRANSFER' THEN 'SAVINGS'
    WHEN 'CAPSULE_RELEASE' THEN 'TIME_CAPSULE'
    ELSE notification_category
END,
enabled = CASE WHEN in_app_enabled = 1 OR push_enabled = 1 THEN 1 ELSE 0 END;

ALTER TABLE notification_preference
    DROP COLUMN in_app_enabled,
    DROP COLUMN push_enabled,
    ADD UNIQUE KEY uk_notification_preference_member_category (member_id, notification_category);

ALTER TABLE notification_preference
    DROP INDEX idx_notification_preference_member_id;

ALTER TABLE notification
    ADD COLUMN notification_category VARCHAR(50) NULL AFTER child_id,
    ADD COLUMN metadata_json JSON NULL AFTER reference_id,
    ADD COLUMN deduplication_key VARCHAR(200) NULL AFTER metadata_json;

UPDATE notification
SET notification_category = CASE notification_type
        WHEN 'TRANSFER' THEN 'SAVINGS'
        WHEN 'CAPSULE_OPEN' THEN 'TIME_CAPSULE'
        ELSE notification_type
    END,
    deduplication_key = CONCAT('legacy-notification-', notification_id);

ALTER TABLE notification
    MODIFY COLUMN notification_category VARCHAR(50) NOT NULL,
    DROP COLUMN sent_at,
    ADD UNIQUE KEY uk_notification_deduplication_key (deduplication_key),
    ADD KEY idx_notification_member_category_id (member_id, notification_category, notification_id);

-- 9. Add the current tables that did not exist in the legacy backup. They have
--    no legacy rows to copy.
CREATE TABLE allowance_request (
    allowance_request_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    child_id BIGINT UNSIGNED NOT NULL,
    requested_amount DECIMAL(18, 0) NOT NULL,
    message VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (allowance_request_id),
    KEY idx_allowance_request_child_status_requested_at (child_id, status, requested_at),
    CONSTRAINT fk_allowance_request_child FOREIGN KEY (child_id) REFERENCES child (child_id),
    CONSTRAINT ck_allowance_request_amount CHECK (requested_amount > 0),
    CONSTRAINT ck_allowance_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mission (
    mission_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    child_id BIGINT UNSIGNED NOT NULL,
    created_by_member_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    reward_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (mission_id),
    KEY idx_mission_child_status_id (child_id, status, mission_id),
    KEY idx_mission_creator_id (created_by_member_id, mission_id),
    CONSTRAINT fk_mission_child FOREIGN KEY (child_id) REFERENCES child (child_id),
    CONSTRAINT fk_mission_creator FOREIGN KEY (created_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_mission_reward_amount CHECK (reward_amount > 0),
    CONSTRAINT ck_mission_status CHECK (status IN ('ASSIGNED', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Remove the legacy external connection model after all preservation work.
ALTER TABLE financial_account
    DROP FOREIGN KEY fk_financial_account_connection,
    DROP FOREIGN KEY fk_financial_account_goal_template,
    DROP INDEX idx_financial_account_connection_id,
    DROP INDEX idx_financial_account_goal_template_id,
    DROP COLUMN financial_connection_id,
    DROP COLUMN financial_goal_template_id,
    DROP COLUMN goal_name_snapshot,
    DROP COLUMN goal_target_amount,
    DROP COLUMN goal_target_date;

DELIMITER //
DROP PROCEDURE IF EXISTS assert_financial_account_constraints //
CREATE PROCEDURE assert_financial_account_constraints()
BEGIN
    DECLARE invalid_count INT;
    SELECT COUNT(*) INTO invalid_count
    FROM financial_account
    WHERE (account_product_type = 'SAVINGS' AND maturity_date IS NULL)
       OR (account_product_type <> 'SAVINGS' AND maturity_date IS NOT NULL)
       OR (link_status = 'ACTIVE' AND linked_at IS NULL)
       OR (is_primary = 1 AND account_product_type <> 'DEMAND_DEPOSIT');
    IF invalid_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Existing financial_account rows violate the target maturity, linked-at, or primary-account rules.';
    END IF;
END //
CALL assert_financial_account_constraints() //
DROP PROCEDURE assert_financial_account_constraints //
DELIMITER ;

ALTER TABLE financial_account
    ADD CONSTRAINT ck_financial_account_owner
        CHECK (
            (owner_type = 'PARENT' AND owner_member_id IS NOT NULL AND child_id IS NULL)
            OR (owner_type = 'CHILD' AND owner_member_id IS NOT NULL AND child_id IS NOT NULL)
        ),
    ADD CONSTRAINT ck_financial_account_maturity
        CHECK (
            (account_product_type = 'SAVINGS' AND maturity_date IS NOT NULL)
            OR (account_product_type <> 'SAVINGS' AND maturity_date IS NULL)
        ),
    ADD CONSTRAINT ck_financial_account_linked_at
        CHECK (link_status <> 'ACTIVE' OR linked_at IS NOT NULL),
    ADD CONSTRAINT ck_financial_account_primary
        CHECK (is_primary = 0 OR account_product_type = 'DEMAND_DEPOSIT');

ALTER TABLE child
    DROP COLUMN last_allowance_requested_at,
    DROP COLUMN last_allowance_requested_month;

DROP TABLE IF EXISTS financial_sync_job;
DROP TABLE IF EXISTS financial_connection;
DROP TABLE IF EXISTS financial_account_before_235;
DROP TABLE IF EXISTS financial_connection_before_235;

-- Run V20260819__validate_legacy_local_data_migration.sql immediately after
-- this script. If any validation query reports a non-zero count, restore the
-- copied database from backup and fix the migration instead of editing `azas`.
