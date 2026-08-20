-- Post-migration validation for V20260819__migrate_legacy_local_data_to_mock_schema.sql.
-- Execute this only after the migration on azas_migrated_20260819.

SELECT DATABASE() AS current_database;

-- All values below should be zero.
SELECT 'invalid_financial_account_owner' AS check_name, COUNT(*) AS invalid_count
FROM financial_account
WHERE owner_member_id IS NULL
   OR (owner_type = 'PARENT' AND child_id IS NOT NULL)
   OR (owner_type = 'CHILD' AND child_id IS NULL)
UNION ALL
SELECT 'unlinked_active_goal', COUNT(*)
FROM financial_goal fg
WHERE fg.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1 FROM financial_goal_account fga
      WHERE fga.financial_goal_id = fg.financial_goal_id
  )
UNION ALL
SELECT 'savings_goal_snapshot_columns_remaining', COUNT(*)
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'financial_account'
  AND column_name IN (
      'financial_connection_id', 'financial_goal_template_id',
      'goal_name_snapshot', 'goal_target_amount', 'goal_target_date'
  )
UNION ALL
SELECT 'legacy_connection_tables_remaining', COUNT(*)
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('financial_connection', 'financial_sync_job')
UNION ALL
SELECT 'legacy_transaction_source_remaining', COUNT(*)
FROM account_transaction
WHERE source_type = 'IMPORTED'
UNION ALL
SELECT 'invalid_schedule_goal_link', COUNT(*)
FROM auto_transfer_schedule ats
LEFT JOIN financial_goal fg ON fg.financial_goal_id = ats.financial_goal_id
WHERE fg.financial_goal_id IS NULL
UNION ALL
SELECT 'non_image_capsule_media', COUNT(*)
FROM time_capsule_media
WHERE media_type <> 'IMAGE' OR slot_no <> 1;

-- Confirm the existing business data survived. Compare with the legacy backup
-- counts recorded before migration; these counts must not unexpectedly shrink.
SELECT 'member' AS table_name, COUNT(*) AS row_count FROM member
UNION ALL SELECT 'child', COUNT(*) FROM child
UNION ALL SELECT 'financial_account', COUNT(*) FROM financial_account
UNION ALL SELECT 'financial_goal', COUNT(*) FROM financial_goal
UNION ALL SELECT 'financial_goal_account', COUNT(*) FROM financial_goal_account
UNION ALL SELECT 'account_transaction', COUNT(*) FROM account_transaction
UNION ALL SELECT 'auto_transfer_schedule', COUNT(*) FROM auto_transfer_schedule
UNION ALL SELECT 'time_capsule', COUNT(*) FROM time_capsule
UNION ALL SELECT 'time_capsule_entry', COUNT(*) FROM time_capsule_entry
UNION ALL SELECT 'time_capsule_media', COUNT(*) FROM time_capsule_media;

-- The backup's account 3 snapshot must now be normalized as a real goal and
-- linked account. This output is for manual review, not an error condition.
SELECT
    fg.financial_goal_id,
    fg.child_id,
    fg.title,
    fg.target_amount,
    fg.target_date,
    fg.monthly_saving_amount,
    GROUP_CONCAT(fga.financial_account_id ORDER BY fga.financial_account_id) AS linked_account_ids
FROM financial_goal fg
LEFT JOIN financial_goal_account fga
    ON fga.financial_goal_id = fg.financial_goal_id
WHERE fg.child_id = 1
  AND fg.title = '대학자금 마련'
GROUP BY fg.financial_goal_id, fg.child_id, fg.title,
         fg.target_amount, fg.target_date, fg.monthly_saving_amount;

SHOW TABLES LIKE 'allowance_request';
SHOW TABLES LIKE 'mission';
SHOW TABLES LIKE 'checklist_item_detail';
