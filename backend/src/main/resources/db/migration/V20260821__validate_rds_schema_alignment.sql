-- V20260821__align_rds_schema_after_legacy_migration.sql 실행 후,
-- 다음 두 시드 파일까지 실행한 뒤 사용하는 검증용 SQL입니다.
--   1) backend/src/main/resources/db/checklist-content-seed.sql
--   2) backend/src/main/resources/db/financial-goal-amount-recommendations.sql
-- 데이터는 변경하지 않습니다.

USE azas;

-- 0이어야 합니다. legacy-template-*가 남으면 기존 템플릿 행이 현재 시드와 매칭되지 않은 것입니다.
SELECT 'unresolved_checklist_template' AS check_name, COUNT(*) AS invalid_count
FROM checklist_item_template
WHERE template_key = ''
   OR template_key LIKE 'legacy-template-%'
   OR category = ''
   OR action_type = ''
UNION ALL
SELECT 'invalid_child_checklist_completion', COUNT(*)
FROM child_checklist_item
WHERE (status = 'PENDING' AND (completed_by_member_id IS NOT NULL OR completed_at IS NOT NULL))
   OR (status = 'COMPLETED' AND (completed_by_member_id IS NULL OR completed_at IS NULL))
UNION ALL
SELECT 'missing_goal_recommendation_basis', COUNT(*)
FROM financial_goal_template template
LEFT JOIN financial_goal_recommendation_basis basis
    ON basis.financial_goal_template_id = template.financial_goal_template_id
WHERE template.is_default = 1
  AND template.is_active = 1
  AND basis.financial_goal_recommendation_basis_id IS NULL
UNION ALL
SELECT 'missing_goal_amount_recommendation', COUNT(*)
FROM financial_goal_template template
LEFT JOIN financial_goal_amount_recommendation recommendation
    ON recommendation.financial_goal_template_id = template.financial_goal_template_id
   AND recommendation.is_active = 1
WHERE template.is_default = 1
  AND template.is_active = 1
  AND recommendation.financial_goal_amount_recommendation_id IS NULL;

-- 정확히 3개가 반환되어야 합니다.
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'financial_goal_recommendation_basis',
    'financial_goal_amount_recommendation',
    'push_device'
  )
ORDER BY table_name;

-- 체크리스트 API가 읽는 컬럼의 실제 값 샘플입니다.
SELECT
    template.checklist_item_template_id,
    template.template_key,
    template.lifecycle_stage,
    template.category,
    template.action_type,
    template.action_url,
    template.info_title,
    detail.action_label,
    detail.external_url
FROM checklist_item_template template
LEFT JOIN checklist_item_detail detail
    ON detail.checklist_item_template_id = template.checklist_item_template_id
WHERE template.is_active = 1
ORDER BY template.lifecycle_stage, template.item_order, detail.item_order;
