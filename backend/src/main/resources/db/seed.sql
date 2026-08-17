-- Azas initial seed data.
-- 실행 전 schema.sql을 먼저 실행해야 한다.
-- 실행 방법 예시: mysql -u root -p azas < backend/src/main/resources/db/seed.sql

USE azas;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notification;
TRUNCATE TABLE notification_preference;
TRUNCATE TABLE asset_report;
TRUNCATE TABLE time_capsule_media;
TRUNCATE TABLE time_capsule_entry;
TRUNCATE TABLE time_capsule;
TRUNCATE TABLE auto_transfer_schedule;
TRUNCATE TABLE financial_transfer;
TRUNCATE TABLE account_transaction;
TRUNCATE TABLE account_balance_snapshot;
TRUNCATE TABLE financial_goal_checkpoint;
TRUNCATE TABLE financial_goal;
TRUNCATE TABLE financial_account;
TRUNCATE TABLE financial_product_bookmark;
TRUNCATE TABLE financial_product;
TRUNCATE TABLE child_checklist_item;
TRUNCATE TABLE checklist_item_template;
TRUNCATE TABLE allowance_request;
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
  (1, '대학자금', '대학 등록금과 교육비', 'graduation_cap', 1, 1, 1, NULL),
  (2, '주거자금', '내 집 마련을 위한 자금', 'house', 2, 1, 1, NULL),
  (4, '결혼자금', '미래 자녀의 결혼을 위한 자금', 'hearts', 3, 1, 1, NULL),
  (3, '목돈 마련', '아이의 미래를 위한 든든한 목돈', 'coins', 4, 1, 1, NULL);

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
  target_owner_type,
  product_subtype,
  name,
  summary,
  highlight_label,
  display_badges_json,
  curation_reason,
  detail_url,
  product_image_key,
  base_interest_rate,
  max_interest_rate,
  interest_rate_reference,
  min_age,
  max_age,
  min_monthly_amount,
  max_monthly_amount,
  contract_period_months,
  min_contract_period_months,
  max_contract_period_months,
  renewal_description,
  interest_payment_method,
  join_termination_method,
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
    'CHILD',
    '자유적립식 예금',
    'KB Young Youth 적금',
    '어린이·청소년을 위한 장기 저축 적금 상품입니다.',
    '자녀 추천',
    JSON_ARRAY('#만19세미만', '#자유적립', '#무료보험'),
    '자녀가 성년이 될 때까지 장기 거래가 가능하고 어린이·청소년을 위한 우대조건과 부가혜택을 제공하는 상품이에요.',
    'https://www.kbstar.com/',
    'pig_coin',
    2.1000,
    3.6500,
    '12개월 기준 · 세금공제 전',
    0,
    19,
    10000,
    3000000,
    12,
    12,
    12,
    '재예치 가능 여부는 상품 약관을 확인하세요.',
    'MATURITY_LUMP_SUM',
    '영업점 또는 KB스타뱅킹에서 가입·해지할 수 있습니다.',
    JSON_ARRAY(JSON_OBJECT('label', '가입 대상', 'content', '만 19세 미만 실명의 개인')),
    JSON_ARRAY(JSON_OBJECT('label', '저축 금액', 'content', '월 1만원 이상 300만원 이하')),
    JSON_ARRAY(JSON_OBJECT('label', '가족사랑 우대', 'rate', 0.2), JSON_OBJECT('label', '자동이체 우대', 'rate', 0.1)),
    JSON_ARRAY(JSON_OBJECT('label', '무료 보험가입 서비스', 'content', '상품 조건에 따라 제공')),
    JSON_ARRAY(JSON_OBJECT('label', '유의사항', 'content', '상품 가입 전 약관과 상품설명서를 확인하세요.')),
    1,
    '2026-07-01'
  ),
  (
    2,
    'KB국민은행',
    'kb-mock-demand-deposit',
    'ACCOUNT',
    'BOTH',
    '입출금통장',
    'KB국민 입출금통장',
    'Mock 금융 온보딩과 용돈 관리를 위한 입출금계좌입니다.',
    '첫 계좌',
    JSON_ARRAY('#입출금', '#용돈관리'),
    '부모와 자녀의 일상적인 입출금과 용돈 관리를 시작하기 위한 Mock 계좌 상품이에요.',
    'https://www.kbstar.com/',
    'wallet',
    0.1000,
    0.1000,
    '수시입출금 · 세금공제 전',
    0,
    NULL,
    0,
    NULL,
    NULL,
    'Azas의 Mock 계좌 개설 화면에서 가입·해지할 수 있습니다.',
    NULL,
    NULL,
    NULL,
    NULL,
    JSON_ARRAY(JSON_OBJECT('label', '가입 대상', 'content', 'Azas Mock 금융 사용자')),
    JSON_ARRAY(JSON_OBJECT('label', '초기 금액', 'content', '0원 이상')),
    JSON_ARRAY(),
    JSON_ARRAY(),
    JSON_ARRAY(JSON_OBJECT('label', '유의사항', 'content', '실제 금융기관 계좌가 아닌 Mock 계좌입니다.')),
    1,
    '2026-08-13'
  ),
  (
    3,
    'KB국민은행',
    'DP01001614',
    'SAVING',
    'PARENT',
    '자유적립식 예금',
    'KB스타적금Ⅲ',
    '부모의 자산 형성을 위한 KB국민은행 적금 기반 Mock 상품입니다.',
    '부모 추천',
    JSON_ARRAY('#자유적립', '#월30만원'),
    '부모가 자녀의 미래를 위해 매월 자유롭게 저축 계획을 실천할 수 있는 Mock 적금 상품이에요.',
    'https://obank.kbstar.com/quics?cc=b061761%3Ab061770&isNew=N&page=C020702&prcode=DP01001614',
    'pig_coin',
    NULL,
    NULL,
    '12개월 기준 · 세금공제 전',
    19,
    NULL,
    10000,
    300000,
    12,
    12,
    12,
    NULL,
    'MATURITY_LUMP_SUM',
    'Azas의 Mock 적금 개설 화면에서 가입·해지할 수 있습니다.',
    JSON_ARRAY(JSON_OBJECT('label', '가입 대상', 'content', '만 19세 이상 부모 회원')),
    JSON_ARRAY(JSON_OBJECT('label', '저축 금액', 'content', '월 1만원 이상 30만원 이하')),
    JSON_ARRAY(),
    JSON_ARRAY(),
    JSON_ARRAY(JSON_OBJECT('label', '유의사항', 'content', '실제 금융기관 계좌가 아닌 Mock 계좌입니다.')),
    1,
    '2026-08-14'
  ),
  (
    4,
    'KB국민은행',
    'kb-child-love-saving',
    'SAVING',
    'CHILD',
    '자유적립식 예금',
    'KB아이사랑적금',
    '아이 키우는 가정의 목돈 마련을 응원하는 가족 맞춤형 적금이에요.',
    '최고 금리',
    JSON_ARRAY('#아이사랑', '#육아응원', '#월30만원'),
    '아이를 키우는 가족이 우대금리와 함께 꾸준히 자녀 자산을 준비할 수 있는 Mock 적금 상품이에요.',
    'https://www.kbstar.com/',
    'pig_coin',
    2.0000,
    10.0000,
    '12개월 기준 · 세금공제 전',
    0,
    19,
    10000,
    300000,
    12,
    12,
    12,
    '만기 후 재가입 여부는 상품 약관을 확인하세요.',
    'MATURITY_LUMP_SUM',
    '영업점 또는 KB스타뱅킹에서 가입·해지할 수 있습니다.',
    JSON_ARRAY(JSON_OBJECT('label', '가입 대상', 'content', '자녀를 양육하는 가족 고객')),
    JSON_ARRAY(JSON_OBJECT('label', '저축 금액', 'content', '월 1만원 이상 30만원 이하')),
    JSON_ARRAY(JSON_OBJECT('label', '가족 우대', 'rate', 8.0)),
    JSON_ARRAY(JSON_OBJECT('label', '육아 응원', 'content', '가족 고객 우대 조건 제공')),
    JSON_ARRAY(JSON_OBJECT('label', '유의사항', 'content', 'Mock 상품의 금리와 조건은 시연용 데이터입니다.')),
    1,
    '2026-08-15'
  ),
  (
    5,
    'KB국민은행',
    'kb-280-day-saving',
    'SAVING',
    'PARENT',
    '자유적립식 예금',
    '내 아이를 위한 280일 적금',
    '아이를 기다리는 기간 동안 즐겁게 저축하는 출산 준비 통장이에요.',
    '출산 준비',
    JSON_ARRAY('#예비부모', '#출산준비'),
    '출산을 준비하는 기간 동안 아이의 첫 자산을 차근차근 마련할 수 있는 Mock 적금 상품이에요.',
    'https://www.kbstar.com/',
    'pig_coin',
    2.5000,
    3.5500,
    '6~12개월 기준 · 세금공제 전',
    19,
    NULL,
    10000,
    1000000,
    12,
    6,
    12,
    '가입 기간은 6개월부터 12개월까지 선택할 수 있습니다.',
    'MATURITY_LUMP_SUM',
    'Azas의 Mock 적금 개설 화면에서 가입·해지할 수 있습니다.',
    JSON_ARRAY(JSON_OBJECT('label', '가입 대상', 'content', '출산을 준비하는 부모 회원')),
    JSON_ARRAY(JSON_OBJECT('label', '저축 금액', 'content', '월 1만원 이상 100만원 이하')),
    JSON_ARRAY(),
    JSON_ARRAY(JSON_OBJECT('label', '출산 준비', 'content', '예비 부모의 저축 계획을 지원합니다.')),
    JSON_ARRAY(JSON_OBJECT('label', '유의사항', 'content', 'Mock 상품의 금리와 조건은 시연용 데이터입니다.')),
    1,
    '2026-08-15'
  );

INSERT INTO financial_product_bookmark (
  financial_product_bookmark_id,
  member_id,
  child_id,
  financial_product_id
) VALUES
  (1, 1, 1, 1);

INSERT INTO financial_account (
  financial_account_id,
  owner_type,
  owner_member_id,
  child_id,
  financial_product_id,
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
    'PARENT',
    1,
    NULL,
    NULL,
    NULL,
    '004',
    'KB국민은행',
    FROM_BASE64('AS+1XqOuK/oansezNOWbzLlkSFQzXa+pjB2IqT+tWq9Ibya/JE7ldK82'),
    SHA2('987-6543-5678', 256),
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
    'CHILD',
    2,
    1,
    NULL,
    NULL,
    '004',
    'KB국민은행',
    FROM_BASE64('AY+uroBtCF2CkEZpvz0aq8HxWzX+axeE89gB8rgwKmUP8B6J7Wi9vlH2'),
    SHA2('123-4567-1001', 256),
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
    'CHILD',
    2,
    1,
    1,
    1,
    '004',
    'KB국민은행',
    FROM_BASE64('AXczMbSjhE4uyFH2/NWa3d6+hbzPPTu3gwsbAaH6qGinioBVM5ppBpKz'),
    SHA2('123-4567-2001', 256),
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

INSERT INTO financial_goal (
  financial_goal_id,
  child_id,
  financial_account_id,
  financial_goal_template_id,
  title,
  target_amount,
  target_date,
  monthly_saving_amount,
  status
) VALUES
  (1, 1, 3, 1, '대학자금 마련', 30000000, '2038-01-12', 100000, 'ACTIVE');

INSERT INTO financial_goal_checkpoint (
  financial_goal_checkpoint_id,
  financial_goal_id,
  percentage,
  target_amount
) VALUES
  (1, 1, 10, 3000000),
  (2, 1, 25, 7500000),
  (3, 1, 50, 15000000),
  (4, 1, 75, 22500000),
  (5, 1, 100, 30000000);

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
    request_idempotency_key,
    financial_goal_id,
    source_account_id,
    destination_account_id,
    amount,
    frequency,
    transfer_day,
    start_date,
    end_date,
    next_transfer_at,
    last_transfer_status,
    last_transferred_at,
    status
) VALUES (
             1,
             1,
             1,
             '00000000-0000-0000-0000-000000000101',
             1,
             1,
             3,
             100000,
             'MONTHLY',
             25,
             '2026-08-25',
             NULL,
             '2026-08-25 00:00:00.000000',
             NULL,
             NULL,
             'ACTIVE'
         );

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
    notification_category,
    enabled
) VALUES
      (1, 1, 'SAVINGS', 1),
      (2, 1, 'TIME_CAPSULE', 1),
      (3, 1, 'ALLOWANCE', 1),
      (4, 1, 'PREGNANCY', 1),
      (5, 1, 'USAGE_LIMIT', 1),
      (6, 1, 'MISSION', 1);

INSERT INTO notification_preference (
    notification_preference_id,
    member_id,
    notification_category,
    enabled
) VALUES
      (7, 2, 'SAVINGS', 1),
      (8, 2, 'ALLOWANCE', 1),
      (9, 2, 'USAGE_LIMIT', 1),
      (10, 2, 'MISSION', 1);

INSERT INTO notification (
    notification_id,
    member_id,
    child_id,
    notification_category,
    notification_type,
    title,
    content,
    reference_type,
    reference_id,
    metadata_json,
    deduplication_key,
    is_read
) VALUES
      (
          1,
          1,
          1,
          'SAVINGS',
          'AUTO_TRANSFER_SUCCEEDED',
          '저축 이체가 완료됐어요',
          '깨비의 적금 계좌로 100,000원이 입금됐어요.',
          'TRANSFER',
          1,
          JSON_OBJECT('amount', 100000),
          'AUTO_TRANSFER_SUCCEEDED:1',
          0
      ),
      (
          2,
          2,
          1,
          'TIME_CAPSULE',
          'TIME_CAPSULE_RELEASE_SOON',
          '타임캡슐 공개일이 다가오고 있어요',
          '소중한 추억을 만나는 날까지 이제 3일 남았어요.',
          'TIME_CAPSULE',
          1,
          JSON_OBJECT('remaining_days', 3),
          'TIME_CAPSULE_RELEASE_SOON:1:3',
          0
      );

