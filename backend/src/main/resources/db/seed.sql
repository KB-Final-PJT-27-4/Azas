-- Azas initial seed data.
-- 실행 전 schema.sql을 먼저 실행해야 한다.
-- 실행 방법 예시: mysql -u root -p azas < backend/src/main/resources/db/seed.sql

USE azas;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notification;
TRUNCATE TABLE notification_preference;
TRUNCATE TABLE asset_report;
TRUNCATE TABLE time_capsule_export;
TRUNCATE TABLE time_capsule_media;
TRUNCATE TABLE time_capsule_entry;
TRUNCATE TABLE time_capsule;
TRUNCATE TABLE auto_transfer_schedule;
TRUNCATE TABLE financial_transfer;
TRUNCATE TABLE account_transaction;
TRUNCATE TABLE account_balance_snapshot;
TRUNCATE TABLE financial_sync_job;
TRUNCATE TABLE financial_account;
TRUNCATE TABLE financial_connection;
TRUNCATE TABLE financial_product_bookmark;
TRUNCATE TABLE financial_product;
TRUNCATE TABLE child_checklist_item;
TRUNCATE TABLE checklist_item_template;
TRUNCATE TABLE family_invitation;
TRUNCATE TABLE child_parent;
TRUNCATE TABLE child;
TRUNCATE TABLE financial_goal_template;
TRUNCATE TABLE refresh_token;
TRUNCATE TABLE social_account;
TRUNCATE TABLE member;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO member (
  member_id,
  email,
  name,
  profile_image_url,
  member_type,
  status,
  birth_date,
  phone_number_hash,
  phone_verified_at
) VALUES
  (1, 'parent@test.com', '김하나', NULL, 'PARENT', 'ACTIVE', '1994-04-12', SHA2('01012345678', 256), NOW(6)),
  (2, 'child@test.com', '깨비', NULL, 'CHILD', 'ACTIVE', '2016-01-12', NULL, NULL),
  (3, 'admin@test.com', '관리자', NULL, 'ADMIN', 'ACTIVE', NULL, NULL, NULL);

INSERT INTO social_account (
  social_account_id,
  member_id,
  provider,
  provider_subject
) VALUES
  (1, 1, 'KAKAO', 'kakao-parent-test-subject'),
  (2, 2, 'KAKAO', 'kakao-child-test-subject');

INSERT INTO financial_goal_template (
  financial_goal_template_id,
  goal_name,
  description,
  icon_key,
  sort_order,
  is_default,
  is_active,
  created_by_member_id
) VALUES
  (1, '대학자금 마련', '아이의 대학 입학 시점에 필요한 자금을 준비해요.', 'graduation_cap', 1, 1, 1, NULL),
  (2, '독립자금 마련', '성인이 되었을 때 첫 독립을 위한 종잣돈을 준비해요.', 'home', 2, 1, 1, NULL),
  (3, '첫 종잣돈 마련', '아이에게 경제적 출발선을 만들어주는 목표예요.', 'seed_money', 3, 1, 1, NULL);

INSERT INTO child (
  child_id,
  member_id,
  name,
  birth_status,
  birth_date,
  gender,
  profile_image_url
) VALUES
  (1, 2, '깨비', 'BORN', '2016-01-12', 'UNKNOWN', NULL);

INSERT INTO child_parent (
  child_parent_id,
  child_id,
  member_id,
  relation_type
) VALUES
  (1, 1, 1, 'MOTHER');

INSERT INTO family_invitation (
  family_invitation_id,
  child_id,
  inviter_member_id,
  invitee_type,
  relation_type,
  invite_token_hash,
  status,
  expires_at,
  accepted_member_id,
  accepted_at
) VALUES
  (1, 1, 1, 'CHILD', NULL, SHA2('accepted-child-invite-token', 256), 'ACCEPTED', DATE_ADD(NOW(6), INTERVAL 7 DAY), 2, NOW(6)),
  (2, 1, 1, 'PARENT', 'FATHER', SHA2('pending-parent-invite-token', 256), 'PENDING', DATE_ADD(NOW(6), INTERVAL 7 DAY), NULL, NULL);

INSERT INTO checklist_item_template (
  checklist_item_template_id,
  title,
  description,
  item_order,
  is_active
) VALUES
  (1, '아이 입출금 계좌 연결하기', '아이 명의 입출금 계좌를 서비스에 연결해요.', 1, 1),
  (2, '아이 적금 계좌 연결하기', '아이 명의 적금 계좌를 연결하고 목표를 설정해요.', 2, 1),
  (3, '첫 자동이체 설정하기', '매월 정해진 금액을 자동으로 저축하도록 설정해요.', 3, 1);

INSERT INTO child_checklist_item (
  child_checklist_item_id,
  child_id,
  checklist_item_template_id,
  status,
  completed_by_member_id,
  completed_at
) VALUES
  (1, 1, 1, 'COMPLETED', 1, NOW(6)),
  (2, 1, 2, 'COMPLETED', 1, NOW(6)),
  (3, 1, 3, 'PENDING', NULL, NULL);

INSERT INTO financial_product (
  financial_product_id,
  bank_name,
  external_product_id,
  product_type,
  product_subtype,
  name,
  summary,
  detail_url,
  product_image_key,
  base_interest_rate,
  max_interest_rate,
  min_age,
  max_age,
  min_monthly_amount,
  max_monthly_amount,
  contract_period_months,
  renewal_description,
  interest_payment_method,
  eligibility_conditions_json,
  deposit_conditions_json,
  preferential_conditions_json,
  additional_benefits_json,
  cautions_json,
  is_active,
  source_base_date
) VALUES
  (
    1,
    'KB국민은행',
    'kb-young-youth-saving',
    'SAVING',
    '자유적립식 예금',
    'KB Young Youth 적금',
    '어린이·청소년을 위한 장기 저축 적금 상품입니다.',
    'https://www.kbstar.com/',
    'pig_coin',
    2.1000,
    3.4000,
    0,
    19,
    10000,
    3000000,
    12,
    '재예치 가능 여부는 상품 약관을 확인하세요.',
    '만기일시지급식',
    JSON_ARRAY(JSON_OBJECT('label', '가입 대상', 'content', '만 19세 미만 실명의 개인')),
    JSON_ARRAY(JSON_OBJECT('label', '저축 금액', 'content', '월 1만원 이상 300만원 이하')),
    JSON_ARRAY(JSON_OBJECT('label', '가족사랑 우대', 'rate', 0.2), JSON_OBJECT('label', '자동이체 우대', 'rate', 0.1)),
    JSON_ARRAY(JSON_OBJECT('label', '무료 보험가입 서비스', 'content', '상품 조건에 따라 제공')),
    JSON_ARRAY(JSON_OBJECT('label', '유의사항', 'content', '상품 가입 전 약관과 상품설명서를 확인하세요.')),
    1,
    '2026-07-01'
  );

INSERT INTO financial_product_bookmark (
  financial_product_bookmark_id,
  member_id,
  child_id,
  financial_product_id
) VALUES
  (1, 1, 1, 1);

INSERT INTO financial_connection (
  financial_connection_id,
  connected_by_member_id,
  child_id,
  owner_type,
  provider,
  external_connection_ciphertext,
  external_connection_hash,
  consent_status,
  consented_at,
  consent_expires_at,
  last_synced_at
) VALUES
  (1, 1, NULL, 'PARENT', 'CODEF', UNHEX(SHA2('parent-connected-id', 256)), SHA2('parent-connected-id', 256), 'ACTIVE', NOW(6), DATE_ADD(NOW(6), INTERVAL 1 YEAR), NOW(6)),
  (2, 1, 1, 'CHILD', 'CODEF', UNHEX(SHA2('child-connected-id', 256)), SHA2('child-connected-id', 256), 'ACTIVE', NOW(6), DATE_ADD(NOW(6), INTERVAL 1 YEAR), NOW(6));

INSERT INTO financial_account (
  financial_account_id,
  financial_connection_id,
  child_id,
  financial_goal_template_id,
  organization_code,
  bank_name,
  account_number_ciphertext,
  account_number_hash,
  account_name,
  account_product_type,
  balance,
  balance_updated_at,
  account_status,
  child_access_mode,
  child_available_amount,
  access_updated_by_member_id,
  access_updated_at,
  goal_name_snapshot,
  goal_target_amount,
  goal_target_date,
  is_primary,
  opened_at,
  maturity_date,
  link_status,
  linked_at
) VALUES
  (
    1,
    1,
    NULL,
    NULL,
    '004',
    'KB국민은행',
    UNHEX(SHA2('parent-account-5678', 256)),
    SHA2('parent-account-5678', 256),
    'KB국민 5678',
    'DEMAND_DEPOSIT',
    2000000,
    NOW(6),
    'ACTIVE',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    '2024-01-01 09:00:00.000000',
    NULL,
    'ACTIVE',
    NOW(6)
  ),
  (
    2,
    2,
    1,
    NULL,
    '004',
    'KB국민은행',
    UNHEX(SHA2('child-demand-account-1001', 256)),
    SHA2('child-demand-account-1001', 256),
    'KB Young Youth 입출금통장',
    'DEMAND_DEPOSIT',
    120000,
    NOW(6),
    'ACTIVE',
    'CO_MANAGED',
    50000,
    1,
    NOW(6),
    NULL,
    NULL,
    NULL,
    1,
    '2024-01-12 09:00:00.000000',
    NULL,
    'ACTIVE',
    NOW(6)
  ),
  (
    3,
    2,
    1,
    1,
    '004',
    'KB국민은행',
    UNHEX(SHA2('child-saving-account-2001', 256)),
    SHA2('child-saving-account-2001', 256),
    'KB Young Youth 적금',
    'SAVINGS',
    14600000,
    NOW(6),
    'ACTIVE',
    NULL,
    NULL,
    NULL,
    NULL,
    '대학자금 마련',
    30000000,
    '2038-01-12',
    0,
    '2024-01-12 09:00:00.000000',
    '2038-01-12',
    'ACTIVE',
    NOW(6)
  );

INSERT INTO account_balance_snapshot (
  account_balance_snapshot_id,
  financial_account_id,
  child_id,
  balance,
  observed_at
) VALUES
  (1, 2, 1, 120000, NOW(6)),
  (2, 3, 1, 14600000, NOW(6));

INSERT INTO account_transaction (
  account_transaction_id,
  financial_account_id,
  child_id,
  transaction_fingerprint,
  occurred_at,
  direction,
  amount,
  balance_after,
  description,
  counterparty_name,
  transaction_type,
  source_type,
  synced_at
) VALUES
  (1, 3, 1, SHA2('saving-2026-07-20-100000', 256), '2026-07-20 09:00:00.000000', 'CREDIT', 100000, 14600000, '7월 저축', '김하나', '이체', 'TRANSFER', NOW(6)),
  (2, 2, 1, SHA2('demand-2026-07-25-15000', 256), '2026-07-25 12:00:00.000000', 'DEBIT', 15000, 105000, '편의점', 'CU', '카드출금', 'IMPORTED', NOW(6));

INSERT INTO financial_transfer (
  financial_transfer_id,
  child_id,
  requested_by_member_id,
  source_account_id,
  destination_account_id,
  amount,
  memo,
  transfer_type,
  status,
  idempotency_key,
  provider_transfer_id,
  account_transaction_id,
  requested_at,
  completed_at
) VALUES
  (1, 1, 1, 1, 3, 100000, '7월 저축', 'MANUAL', 'SUCCEEDED', '00000000-0000-0000-0000-000000000001', 'mock-transfer-1', 1, '2026-07-20 08:59:00.000000', '2026-07-20 09:00:00.000000');

INSERT INTO auto_transfer_schedule (
  auto_transfer_schedule_id,
  child_id,
  member_id,
  source_account_id,
  destination_account_id,
  amount,
  frequency,
  transfer_day,
  start_date,
  end_date,
  next_transfer_at,
  provider_schedule_id,
  status
) VALUES
  (1, 1, 1, 1, 3, 100000, 'MONTHLY', 25, '2026-08-25', NULL, '2026-08-25 09:00:00.000000', NULL, 'ACTIVE');

INSERT INTO time_capsule (
  time_capsule_id,
  child_id,
  financial_account_id,
  title,
  status,
  expected_release_at,
  entry_count,
  latest_entry_at
) VALUES
  (1, 1, 3, '깨비의 KB Young Youth 적금 타임캡슐', 'COLLECTING', '2038-01-12 00:00:00.000000', 1, '2026-07-20 09:00:00.000000');

INSERT INTO time_capsule_entry (
  time_capsule_entry_id,
  time_capsule_id,
  author_member_id,
  account_transaction_id,
  title,
  message,
  contribution_amount,
  contributed_at,
  media_mode,
  status,
  sealed_at
) VALUES
  (1, 1, 1, 1, '7월 저축 기록', '이번 달에도 깨비를 위해 10만 원을 넣었어.', 100000, '2026-07-20 09:00:00.000000', 'NONE', 'SEALED', '2026-07-20 09:05:00.000000');

INSERT INTO asset_report (
  asset_report_id,
  child_id,
  report_month,
  total_asset_amount,
  total_asset_change_amount,
  monthly_saved_amount,
  total_goal_target_amount,
  total_goal_saved_amount,
  goal_achievement_rate,
  six_month_flow_json,
  savings_goal_summary_json,
  insight_items_json
) VALUES
  (
    1,
    1,
    '2026-07-01',
    16750000,
    250000,
    250000,
    30000000,
    14600000,
    48.6667,
    JSON_ARRAY(
      JSON_OBJECT('month', '2026-02', 'saved_amount', 310000),
      JSON_OBJECT('month', '2026-03', 'saved_amount', 420000),
      JSON_OBJECT('month', '2026-04', 'saved_amount', 560000),
      JSON_OBJECT('month', '2026-05', 'saved_amount', 730000),
      JSON_OBJECT('month', '2026-06', 'saved_amount', 810000),
      JSON_OBJECT('month', '2026-07', 'saved_amount', 900000)
    ),
    JSON_ARRAY(
      JSON_OBJECT(
        'account_id', 3,
        'goal_name', '대학자금 마련',
        'current_amount', 14600000,
        'target_amount', 30000000,
        'achievement_rate', 48.7,
        'monthly_saved_amount', 150000
      )
    ),
    JSON_ARRAY(
      JSON_OBJECT('type', 'SAVED_MORE_THAN_LAST_MONTH', 'title', '지난달보다 9만원 더 저축했어요.')
    )
  );

INSERT INTO notification_preference (
  notification_preference_id,
  member_id,
  notification_type,
  in_app_enabled,
  push_enabled
) VALUES
  (1, 1, 'TRANSFER', 1, 1),
  (2, 1, 'CAPSULE_RELEASE', 1, 1),
  (3, 2, 'CAPSULE_RELEASE', 1, 1);

INSERT INTO notification (
  notification_id,
  member_id,
  child_id,
  notification_type,
  title,
  content,
  reference_type,
  reference_id,
  is_read,
  sent_at
) VALUES
  (1, 1, 1, 'TRANSFER', '저축 이체가 완료됐어요', '깨비의 적금 계좌로 100,000원이 입금됐어요.', 'TRANSFER', 1, 0, NOW(6)),
  (2, 2, 1, 'CAPSULE_OPEN', '타임캡슐이 쌓이고 있어요', '아직 열 수는 없지만 소중한 기록이 보관되고 있어요.', 'TIME_CAPSULE', 1, 0, NOW(6));

