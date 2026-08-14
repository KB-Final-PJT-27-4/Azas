-- Azas initial database schema.
-- 기준: ERDCloud export `아자스.sql`
-- 실행 방법 예시: mysql -u root -p < backend/src/main/resources/db/schema.sql

CREATE DATABASE IF NOT EXISTS azas
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE azas;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS notification_preference;
DROP TABLE IF EXISTS asset_report;
DROP TABLE IF EXISTS time_capsule_export;
DROP TABLE IF EXISTS time_capsule_media;
DROP TABLE IF EXISTS time_capsule_entry;
DROP TABLE IF EXISTS time_capsule;
DROP TABLE IF EXISTS auto_transfer_schedule;
DROP TABLE IF EXISTS financial_transfer;
DROP TABLE IF EXISTS account_transaction;
DROP TABLE IF EXISTS account_balance_snapshot;
DROP TABLE IF EXISTS financial_sync_job;
DROP TABLE IF EXISTS financial_account;
DROP TABLE IF EXISTS financial_connection;
DROP TABLE IF EXISTS financial_product_bookmark;
DROP TABLE IF EXISTS financial_goal_checkpoint;
DROP TABLE IF EXISTS financial_goal;
DROP TABLE IF EXISTS financial_product;
DROP TABLE IF EXISTS child_checklist_item;
DROP TABLE IF EXISTS checklist_item_template;
DROP TABLE IF EXISTS allowance_request;
DROP TABLE IF EXISTS family_invitation;
DROP TABLE IF EXISTS child_parent;
DROP TABLE IF EXISTS child;
DROP TABLE IF EXISTS financial_goal_template;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS social_account;
DROP TABLE IF EXISTS phone_verification;
DROP TABLE IF EXISTS member;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE member (
  member_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
  email VARCHAR(100) NOT NULL COMMENT '소셜 로그인 이메일',
  name VARCHAR(50) NOT NULL COMMENT '소셜 로그인 이름',
  profile_image_url VARCHAR(1000) NULL COMMENT '프로필 이미지 URL',
  member_type VARCHAR(20) NOT NULL COMMENT 'PARENT, CHILD, ADMIN',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, WITHDRAWN',
  birth_date DATE NULL COMMENT '회원 생년월일',
  phone_number_ciphertext VARBINARY(500) NULL COMMENT '휴대폰번호 암호문',
  phone_number_hash CHAR(64) NULL COMMENT '검색·중복 확인용 해시',
  phone_verified_at DATETIME(6) NULL COMMENT 'SMS 인증 완료 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '가입일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (member_id),
  UNIQUE KEY uk_member_email (email),
  UNIQUE KEY uk_member_phone_number_hash (phone_number_hash),
  CONSTRAINT ck_member_type CHECK (member_type IN ('PARENT', 'CHILD', 'ADMIN')),
  CONSTRAINT ck_member_status CHECK (status IN ('ACTIVE', 'WITHDRAWN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='로그인 가능한 회원';

CREATE TABLE phone_verification (
  phone_verification_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '휴대폰 인증 ID',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '인증을 요청한 회원 ID',
  phone_number_ciphertext VARBINARY(500) NOT NULL COMMENT '인증 대상 휴대폰번호 암호문',
  phone_number_hash CHAR(64) NOT NULL COMMENT '휴대폰번호 검색·중복 확인용 HMAC-SHA-256 해시',
  verification_code_hash CHAR(64) NOT NULL COMMENT 'SMS 인증번호 검증용 HMAC-SHA-256 해시',
  attempt_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '인증번호 확인 실패 횟수',
  expires_at DATETIME(6) NOT NULL COMMENT '인증번호 만료 시각',
  verified_at DATETIME(6) NULL COMMENT '인증번호 확인 완료 시각',
  verification_token_hash CHAR(64) NULL COMMENT '회원정보 수정용 일회용 인증 토큰 해시',
  token_expires_at DATETIME(6) NULL COMMENT '일회용 인증 토큰 만료 시각',
  token_consumed_at DATETIME(6) NULL COMMENT '일회용 인증 토큰 사용 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'SMS 인증 요청 생성 시각',
  PRIMARY KEY (phone_verification_id),
  UNIQUE KEY uk_phone_verification_token_hash (verification_token_hash),
  KEY idx_phone_verification_member_created (member_id, created_at),
  KEY idx_phone_verification_phone_hash_created (phone_number_hash, created_at),
  KEY idx_phone_verification_expires_at (expires_at),
  CONSTRAINT fk_phone_verification_member
    FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 휴대폰 SMS 인증 요청';

CREATE TABLE social_account (
  social_account_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '소셜 계정 ID',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '회원 ID',
  provider VARCHAR(30) NOT NULL COMMENT 'KAKAO, GOOGLE',
  provider_subject VARCHAR(255) NOT NULL COMMENT 'OAuth 제공자 사용자 식별자',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '연결일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (social_account_id),
  UNIQUE KEY uk_social_account_provider_subject (provider, provider_subject),
  KEY idx_social_account_member_id (member_id),
  CONSTRAINT fk_social_account_member
    FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 소셜 계정';

CREATE TABLE refresh_token (
  refresh_token_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Refresh Token ID',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '회원 ID',
  token_hash CHAR(64) NOT NULL COMMENT 'Refresh Token SHA-256 해시',
  expires_at DATETIME(6) NOT NULL COMMENT '만료 시각',
  revoked_at DATETIME(6) NULL COMMENT '폐기 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
  PRIMARY KEY (refresh_token_id),
  UNIQUE KEY uk_refresh_token_hash (token_hash),
  KEY idx_refresh_token_member_id (member_id),
  KEY idx_refresh_token_expires_at (expires_at),
  CONSTRAINT fk_refresh_token_member
    FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token 저장소';

CREATE TABLE financial_goal_template (
  financial_goal_template_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '목표 템플릿 ID',
  goal_name VARCHAR(100) NOT NULL COMMENT '예: 대학자금, 독립자금, 주거자금',
  description VARCHAR(500) NULL COMMENT '목표 선택 화면 설명',
  icon_key VARCHAR(100) NULL COMMENT '프론트 아이콘 키',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
  is_default TINYINT(1) NOT NULL DEFAULT 1 COMMENT '서비스 기본 목표 여부',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '노출 여부',
  created_by_member_id BIGINT UNSIGNED NULL COMMENT '직접 추가한 목표의 생성자',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (financial_goal_template_id),
  KEY idx_goal_template_created_by_member_id (created_by_member_id),
  CONSTRAINT fk_goal_template_created_by_member
    FOREIGN KEY (created_by_member_id) REFERENCES member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='적금 목표 템플릿';

CREATE TABLE child (
  child_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '자녀 ID',
  member_id BIGINT UNSIGNED NULL COMMENT '연결된 자녀 회원 ID',
  name VARCHAR(50) NOT NULL COMMENT '자녀 이름 또는 태명',
  birth_status VARCHAR(20) NOT NULL COMMENT 'EXPECTED, BORN',
  expected_birth_date DATE NULL COMMENT '출산 예정일',
  birth_date DATE NULL COMMENT '실제 생년월일',
  gender VARCHAR(20) NULL COMMENT 'MALE, FEMALE, UNKNOWN',
  profile_image_url VARCHAR(1000) NULL COMMENT '자녀 프로필 이미지',
  last_allowance_request_month DATE NULL COMMENT '마지막 용돈 요청 기준 월 1일',
  last_allowance_requested_at DATETIME(6) NULL COMMENT '최근 용돈 요청 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, DELETED',
  deleted_at DATETIME(6) NULL COMMENT '삭제일',
  PRIMARY KEY (child_id),
  UNIQUE KEY uk_child_member_id (member_id),
  CONSTRAINT fk_child_member
    FOREIGN KEY (member_id) REFERENCES member (member_id),
  CONSTRAINT ck_child_birth_status CHECK (birth_status IN ('EXPECTED', 'BORN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부모가 등록하는 자녀 프로필';

CREATE TABLE child_parent (
  child_parent_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '부모-자녀 관계 ID',
  child_id BIGINT UNSIGNED NOT NULL COMMENT '자녀 ID',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '성인 회원 ID',
  relation_type VARCHAR(20) NOT NULL COMMENT 'MOTHER, FATHER, GUARDIAN',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '연결일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (child_parent_id),
  UNIQUE KEY uk_child_parent_child_member (child_id, member_id),
  KEY idx_child_parent_member_id (member_id),
  CONSTRAINT fk_child_parent_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_child_parent_member
    FOREIGN KEY (member_id) REFERENCES member (member_id),
  CONSTRAINT ck_child_parent_relation_type CHECK (relation_type IN ('MOTHER', 'FATHER', 'GUARDIAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부모와 자녀 연결';

CREATE TABLE allowance_request (
    allowance_request_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '용돈 요청 ID',
    child_id BIGINT UNSIGNED NOT NULL COMMENT '요청한 자녀 ID',
    requested_amount DECIMAL(18, 0) NOT NULL COMMENT '요청 금액',
    message VARCHAR(200) NOT NULL COMMENT '부모에게 전달할 메시지',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, CANCELED',
    requested_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '요청 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (allowance_request_id),

    KEY idx_allowance_request_child_status_requested_at (
        child_id,
        status,
        requested_at
    ),
    CONSTRAINT fk_allowance_request_child
        FOREIGN KEY (child_id)
         REFERENCES child (child_id),

    CONSTRAINT ck_allowance_request_amount
        CHECK (requested_amount > 0),

    CONSTRAINT ck_allowance_request_status
        CHECK (
            status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELED')
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='자녀 용돈 요청';

CREATE TABLE family_invitation (
  family_invitation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '가족 초대 ID',
  child_id BIGINT UNSIGNED NOT NULL COMMENT '자녀 ID',
  inviter_member_id BIGINT UNSIGNED NOT NULL COMMENT '초대한 회원 ID',
  invitee_type VARCHAR(20) NOT NULL COMMENT 'PARENT, CHILD',
  relation_type VARCHAR(20) NULL COMMENT '성인 초대 시 관계',
  invite_token_hash CHAR(64) NOT NULL COMMENT '초대 토큰 해시',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, ACCEPTED, EXPIRED, CANCELED',
  expires_at DATETIME(6) NOT NULL COMMENT '초대 만료 시각',
  accepted_member_id BIGINT UNSIGNED NULL COMMENT '수락 회원 ID',
  accepted_at DATETIME(6) NULL COMMENT '수락 완료 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '상태 변경일',
  PRIMARY KEY (family_invitation_id),
  UNIQUE KEY uk_family_invitation_token_hash (invite_token_hash),
  KEY idx_family_invitation_child_status (child_id, status),
  KEY idx_family_invitation_inviter_member_id (inviter_member_id),
  KEY idx_family_invitation_accepted_member_id (accepted_member_id),
  CONSTRAINT fk_family_invitation_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_family_invitation_inviter_member
    FOREIGN KEY (inviter_member_id) REFERENCES member (member_id),
  CONSTRAINT fk_family_invitation_accepted_member
    FOREIGN KEY (accepted_member_id) REFERENCES member (member_id),
  CONSTRAINT ck_family_invitation_invitee_type CHECK (invitee_type IN ('PARENT', 'CHILD')),
  CONSTRAINT ck_family_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가족 초대링크';

CREATE TABLE checklist_item_template (
  checklist_item_template_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '체크리스트 템플릿 ID',
  title VARCHAR(150) NOT NULL COMMENT '공통 항목명',
  description TEXT NULL COMMENT '항목 설명',
  item_order INT NOT NULL COMMENT '표시 순서',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '노출 여부',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (checklist_item_template_id),
  KEY idx_checklist_template_active_order (is_active, item_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='체크리스트 기본 항목';

CREATE TABLE child_checklist_item (
  child_checklist_item_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '자녀 체크리스트 ID',
  child_id BIGINT UNSIGNED NOT NULL COMMENT '자녀 ID',
  checklist_item_template_id BIGINT UNSIGNED NOT NULL COMMENT '체크리스트 템플릿 ID',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED',
  completed_by_member_id BIGINT UNSIGNED NULL COMMENT '완료 회원 ID',
  completed_at DATETIME(6) NULL COMMENT '완료 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (child_checklist_item_id),
  UNIQUE KEY uk_child_checklist_child_template (child_id, checklist_item_template_id),
  KEY idx_child_checklist_template_id (checklist_item_template_id),
  KEY idx_child_checklist_completed_by_member_id (completed_by_member_id),
  CONSTRAINT fk_child_checklist_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_child_checklist_template
    FOREIGN KEY (checklist_item_template_id) REFERENCES checklist_item_template (checklist_item_template_id),
  CONSTRAINT fk_child_checklist_completed_by_member
    FOREIGN KEY (completed_by_member_id) REFERENCES member (member_id),
  CONSTRAINT ck_child_checklist_status CHECK (status IN ('PENDING', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='자녀별 체크리스트 진행 상태';

CREATE TABLE financial_product (
  financial_product_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '금융상품 ID',
  bank_name VARCHAR(50) NOT NULL COMMENT '상품 제공 기관',
  external_product_id VARCHAR(255) NOT NULL COMMENT '제공기관 상품 고유번호',
  product_type VARCHAR(30) NOT NULL COMMENT 'SAVING, DEPOSIT, ACCOUNT, CARD, SUBSCRIPTION',
  product_subtype VARCHAR(50) NULL COMMENT '상품 세부유형',
  name VARCHAR(150) NOT NULL COMMENT '상품명',
  summary VARCHAR(2000) NULL COMMENT '주요 특징 요약',
  detail_url VARCHAR(1000) NULL COMMENT '상품 상세 URL',
  product_image_key VARCHAR(255) NULL COMMENT '프론트 상품 이미지 키',
  base_interest_rate DECIMAL(7, 4) NULL COMMENT '기본 금리',
  max_interest_rate DECIMAL(7, 4) NULL COMMENT '최고 금리',
  min_age INT NULL COMMENT '가입 가능 최소 나이',
  max_age INT NULL COMMENT '가입 가능 최대 나이',
  min_monthly_amount DECIMAL(19, 2) NULL COMMENT '월 최소 납입액',
  max_monthly_amount DECIMAL(19, 2) NULL COMMENT '월 최대 납입액',
  contract_period_months INT NULL COMMENT '계약 기간 개월 수',
  renewal_description VARCHAR(500) NULL COMMENT '만기·재예치 안내',
  interest_payment_method VARCHAR(50) NULL COMMENT '이자 지급 방식',
  eligibility_conditions_json JSON NOT NULL COMMENT '가입 조건',
  deposit_conditions_json JSON NOT NULL COMMENT '납입 조건',
  preferential_conditions_json JSON NOT NULL COMMENT '우대 조건',
  additional_benefits_json JSON NOT NULL COMMENT '부가 혜택',
  cautions_json JSON NOT NULL COMMENT '유의사항',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '노출 여부',
  source_base_date DATE NULL COMMENT '상품정보 기준일',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '상품 캐시 갱신일',
  PRIMARY KEY (financial_product_id),
  UNIQUE KEY uk_financial_product_external_product_id (external_product_id),
  KEY idx_financial_product_type_active (product_type, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='추천 금융상품';

CREATE TABLE financial_product_bookmark (
  financial_product_bookmark_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '관심상품 ID',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '회원 ID',
  child_id BIGINT UNSIGNED NOT NULL COMMENT '자녀 ID',
  financial_product_id BIGINT UNSIGNED NOT NULL COMMENT '금융상품 ID',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '저장일',
  PRIMARY KEY (financial_product_bookmark_id),
  UNIQUE KEY uk_product_bookmark_member_child_product (member_id, child_id, financial_product_id),
  KEY idx_product_bookmark_child_id (child_id),
  KEY idx_product_bookmark_product_id (financial_product_id),
  CONSTRAINT fk_product_bookmark_member
    FOREIGN KEY (member_id) REFERENCES member (member_id),
  CONSTRAINT fk_product_bookmark_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_product_bookmark_product
    FOREIGN KEY (financial_product_id) REFERENCES financial_product (financial_product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관심 금융상품';

CREATE TABLE financial_account (
  financial_account_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '금융 계좌 ID',
  owner_type VARCHAR(20) NOT NULL COMMENT 'PARENT, CHILD',
  owner_member_id BIGINT UNSIGNED NULL COMMENT '계좌 소유 회원 ID. 자녀 가입 전 자녀 계좌는 NULL 가능',
  child_id BIGINT UNSIGNED NULL COMMENT '자녀 계좌 소유 범위 및 조회용 자녀 ID',
  financial_product_id BIGINT UNSIGNED NULL COMMENT 'Mock 개설에 사용한 KB 금융상품 ID',
  financial_goal_template_id BIGINT UNSIGNED NULL COMMENT '선택 목표 템플릿 ID',
  organization_code VARCHAR(20) NOT NULL COMMENT '금융기관 코드',
  bank_name VARCHAR(50) NOT NULL COMMENT '은행명',
  account_number_ciphertext VARBINARY(1000) NOT NULL COMMENT '계좌번호 암호문',
  account_number_hash CHAR(64) NOT NULL COMMENT '계좌번호 해시',
  account_name VARCHAR(100) NOT NULL COMMENT '계좌명',
  account_product_type VARCHAR(30) NOT NULL COMMENT 'DEMAND_DEPOSIT, SAVINGS, SUBSCRIPTION',
  balance DECIMAL(19, 2) NOT NULL DEFAULT 0 COMMENT '최신 잔액 캐시',
  balance_updated_at DATETIME(6) NULL COMMENT '잔액 기준 시각',
  account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, MATURED, CLOSED',
  child_access_mode VARCHAR(30) NULL COMMENT '자녀 사용 관리 모드: CO_MANAGED, UNRESTRICTED',
  child_available_amount DECIMAL(19, 2) NULL COMMENT '월간 사용 관리 기준 금액. 실제 금융기관 제한 금액이 아님',
  access_updated_by_member_id BIGINT UNSIGNED NULL COMMENT '자녀 사용 관리 정책 변경 회원 ID',
  access_updated_at DATETIME(6) NULL COMMENT '자녀 사용 관리 정책 변경 시각',
  goal_name_snapshot VARCHAR(100) NULL COMMENT '목표명 스냅샷',
  goal_target_amount DECIMAL(19, 2) NULL COMMENT '목표 금액',
  goal_target_date DATE NULL COMMENT '목표 달성 예정일',
  is_primary TINYINT(1) NOT NULL DEFAULT 0 COMMENT '대표 계좌 여부',
  opened_at DATETIME(6) NULL COMMENT '계좌 개설 시각',
  maturity_date DATE NULL COMMENT '적금 만기일',
  closed_at DATETIME(6) NULL COMMENT '계좌 해지 시각',
  link_status VARCHAR(20) NOT NULL DEFAULT 'DISCOVERED' COMMENT 'DISCOVERED, ACTIVE, UNLINKED',
  linked_at DATETIME(6) NULL COMMENT '서비스 연결 시각',
  unlinked_at DATETIME(6) NULL COMMENT '연결 해제 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '최초 저장일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (financial_account_id),
  UNIQUE KEY uk_financial_account_number_hash (account_number_hash),
  KEY idx_financial_account_owner_member (owner_type, owner_member_id, link_status),
  KEY idx_financial_account_child_type (child_id, account_product_type),
  KEY idx_financial_account_product_id (financial_product_id),
  KEY idx_financial_account_access_updated_by (access_updated_by_member_id),
  KEY idx_financial_account_goal_template_id (financial_goal_template_id),
  CONSTRAINT fk_financial_account_owner_member
    FOREIGN KEY (owner_member_id) REFERENCES member (member_id),
  CONSTRAINT fk_financial_account_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_financial_account_product
    FOREIGN KEY (financial_product_id) REFERENCES financial_product (financial_product_id),
  CONSTRAINT fk_financial_account_access_updated_by
    FOREIGN KEY (access_updated_by_member_id) REFERENCES member (member_id),
  CONSTRAINT fk_financial_account_goal_template
    FOREIGN KEY (financial_goal_template_id) REFERENCES financial_goal_template (financial_goal_template_id),
  CONSTRAINT ck_financial_account_product_type
    CHECK (account_product_type IN ('DEMAND_DEPOSIT', 'SAVINGS', 'SUBSCRIPTION')),
  CONSTRAINT ck_financial_account_owner
    CHECK (
      (owner_type = 'PARENT' AND owner_member_id IS NOT NULL AND child_id IS NULL)
      OR
      (owner_type = 'CHILD' AND child_id IS NOT NULL)
    ),
  CONSTRAINT ck_financial_account_status
    CHECK (account_status IN ('ACTIVE', 'MATURED', 'CLOSED')),
  CONSTRAINT ck_financial_account_link_status
    CHECK (link_status IN ('DISCOVERED', 'ACTIVE', 'UNLINKED')),
  CONSTRAINT ck_financial_account_child_usage_mode
    CHECK (
      child_access_mode IS NULL
      OR child_access_mode IN ('CO_MANAGED', 'UNRESTRICTED')
    ),
  CONSTRAINT ck_financial_account_child_usage_target
    CHECK (
      child_access_mode IS NULL
      OR (
        child_id IS NOT NULL
        AND account_product_type = 'DEMAND_DEPOSIT'
      )
    ),
  CONSTRAINT ck_financial_account_child_usage_amount
    CHECK (
      (
        child_access_mode IS NULL
        AND child_available_amount IS NULL
      )
      OR (
        child_access_mode = 'CO_MANAGED'
        AND child_available_amount IS NOT NULL
        AND child_available_amount >= 0
      )
      OR (
        child_access_mode = 'UNRESTRICTED'
        AND child_available_amount IS NULL
      )
    ),
  CONSTRAINT ck_financial_account_maturity
    CHECK (
      (account_product_type = 'SAVINGS' AND maturity_date IS NOT NULL)
      OR
      (account_product_type <> 'SAVINGS' AND maturity_date IS NULL)
    ),
  CONSTRAINT ck_financial_account_linked_at
    CHECK (link_status <> 'ACTIVE' OR linked_at IS NOT NULL),
  CONSTRAINT ck_financial_account_primary
    CHECK (is_primary = 0 OR account_product_type = 'DEMAND_DEPOSIT')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mock 금융계좌';

CREATE TABLE financial_goal (
  financial_goal_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '금융 목표 ID',
  child_id BIGINT UNSIGNED NOT NULL COMMENT '자녀 ID',
  financial_account_id BIGINT UNSIGNED NOT NULL COMMENT '연결 자녀 적금계좌 ID',
  financial_goal_template_id BIGINT UNSIGNED NULL COMMENT '선택 목표 템플릿 ID',
  title VARCHAR(100) NOT NULL COMMENT '목표명 스냅샷',
  target_amount DECIMAL(19, 2) NOT NULL COMMENT '목표 금액',
  target_date DATE NOT NULL COMMENT '목표일',
  monthly_saving_amount DECIMAL(19, 2) NOT NULL COMMENT '월 저축 계획 금액',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, ACHIEVED, ARCHIVED',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (financial_goal_id),
  UNIQUE KEY uk_financial_goal_account (financial_account_id),
  KEY idx_financial_goal_child_status (child_id, status),
  CONSTRAINT fk_financial_goal_child FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_financial_goal_account FOREIGN KEY (financial_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT fk_financial_goal_template FOREIGN KEY (financial_goal_template_id) REFERENCES financial_goal_template (financial_goal_template_id),
  CONSTRAINT ck_financial_goal_amount CHECK (target_amount > 0 AND monthly_saving_amount > 0),
  CONSTRAINT ck_financial_goal_status CHECK (status IN ('ACTIVE', 'ACHIEVED', 'ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='자녀 적금 금융 목표';

CREATE TABLE financial_goal_checkpoint (
  financial_goal_checkpoint_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '목표 체크포인트 ID',
  financial_goal_id BIGINT UNSIGNED NOT NULL COMMENT '금융 목표 ID',
  percentage INT NOT NULL COMMENT '달성 비율',
  target_amount DECIMAL(19, 2) NOT NULL COMMENT '체크포인트 금액',
  reached_at DATETIME(6) NULL COMMENT '달성 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  PRIMARY KEY (financial_goal_checkpoint_id),
  UNIQUE KEY uk_goal_checkpoint_percentage (financial_goal_id, percentage),
  CONSTRAINT fk_goal_checkpoint_goal FOREIGN KEY (financial_goal_id) REFERENCES financial_goal (financial_goal_id),
  CONSTRAINT ck_goal_checkpoint_percentage CHECK (percentage IN (10, 25, 50, 75, 100)),
  CONSTRAINT ck_goal_checkpoint_amount CHECK (target_amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='금융 목표 달성 체크포인트';

CREATE TABLE account_balance_snapshot (
  account_balance_snapshot_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '잔액 스냅샷 ID',
  financial_account_id BIGINT UNSIGNED NOT NULL COMMENT '금융 계좌 ID',
  child_id BIGINT UNSIGNED NULL COMMENT '조회 성능용 중복 FK',
  balance DECIMAL(19, 2) NOT NULL COMMENT '잔액',
  observed_at DATETIME(6) NOT NULL COMMENT '관측 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  PRIMARY KEY (account_balance_snapshot_id),
  UNIQUE KEY uk_balance_snapshot_account_observed (financial_account_id, observed_at),
  KEY idx_balance_snapshot_child_observed (child_id, observed_at),
  CONSTRAINT fk_balance_snapshot_account
    FOREIGN KEY (financial_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT fk_balance_snapshot_child
    FOREIGN KEY (child_id) REFERENCES child (child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계좌 잔액 스냅샷';

CREATE TABLE account_transaction (
  account_transaction_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '계좌 거래 ID',
  financial_account_id BIGINT UNSIGNED NOT NULL COMMENT '거래 계좌 ID',
  counterparty_account_id BIGINT UNSIGNED NULL COMMENT '서비스 내부 상대 계좌 ID',
  child_id BIGINT UNSIGNED NULL COMMENT '자녀 기준 거래 조회용 중복 FK',
  transaction_fingerprint CHAR(64) NOT NULL COMMENT '중복 방지 해시',
  occurred_at DATETIME(6) NOT NULL COMMENT '거래 발생 시각',
  direction VARCHAR(10) NOT NULL COMMENT 'CREDIT, DEBIT',
  amount DECIMAL(19, 2) NOT NULL COMMENT '거래 금액',
  balance_after DECIMAL(19, 2) NULL COMMENT '거래 후 잔액',
  description VARCHAR(500) NULL COMMENT '거래 설명',
  counterparty_name VARCHAR(150) NULL COMMENT '상대방명',
  transaction_type VARCHAR(50) NULL COMMENT '거래 유형',
  source_type VARCHAR(30) NOT NULL DEFAULT 'IMPORTED' COMMENT 'IMPORTED, TRANSFER, AUTO_TRANSFER',
  synced_at DATETIME(6) NOT NULL COMMENT '동기화 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '최초 저장일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (account_transaction_id),
  UNIQUE KEY uk_account_transaction_fingerprint (transaction_fingerprint),
  KEY idx_account_transaction_account_occurred (financial_account_id, occurred_at, account_transaction_id),
  KEY idx_account_transaction_counterparty (counterparty_account_id),
  KEY idx_account_transaction_child_occurred (child_id, occurred_at),
  CONSTRAINT fk_account_transaction_account
    FOREIGN KEY (financial_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT fk_account_transaction_counterparty
    FOREIGN KEY (counterparty_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT fk_account_transaction_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT ck_account_transaction_direction CHECK (direction IN ('CREDIT', 'DEBIT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계좌 거래내역';

CREATE TABLE financial_transfer (
  financial_transfer_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '금융 이체 ID',
  child_id BIGINT UNSIGNED NULL COMMENT '관련 자녀 ID',
  requested_by_member_id BIGINT UNSIGNED NOT NULL COMMENT '요청 회원 ID',
  source_account_id BIGINT UNSIGNED NOT NULL COMMENT '출금 계좌 ID',
  destination_account_id BIGINT UNSIGNED NOT NULL COMMENT '입금 계좌 ID',
  amount DECIMAL(19, 2) NOT NULL COMMENT '이체 금액',
  memo VARCHAR(255) NULL COMMENT '이체 메모',
  transfer_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL, AUTO',
  status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT 'REQUESTED, SUCCEEDED, FAILED, CANCELED',
  failure_code VARCHAR(100) NULL COMMENT '실패 코드',
  failure_message VARCHAR(500) NULL COMMENT '실패 메시지',
  idempotency_key CHAR(36) NOT NULL COMMENT '중복 이체 방지',
  provider_transfer_id VARCHAR(255) NULL COMMENT '제공기관 이체 식별자',
  account_transaction_id BIGINT UNSIGNED NULL COMMENT '출금 거래 ID',
  requested_at DATETIME(6) NOT NULL COMMENT '요청 시각',
  completed_at DATETIME(6) NULL COMMENT '최종 완료 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (financial_transfer_id),
  UNIQUE KEY uk_financial_transfer_idempotency_key (idempotency_key),
  KEY idx_financial_transfer_child_id (child_id),
  KEY idx_financial_transfer_requested_by (requested_by_member_id),
  KEY idx_financial_transfer_source_account (source_account_id),
  KEY idx_financial_transfer_destination_account (destination_account_id),
  KEY idx_financial_transfer_transaction_id (account_transaction_id),
  CONSTRAINT fk_financial_transfer_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_financial_transfer_requested_by
    FOREIGN KEY (requested_by_member_id) REFERENCES member (member_id),
  CONSTRAINT fk_financial_transfer_source_account
    FOREIGN KEY (source_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT fk_financial_transfer_destination_account
    FOREIGN KEY (destination_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT fk_financial_transfer_transaction
    FOREIGN KEY (account_transaction_id) REFERENCES account_transaction (account_transaction_id),
  CONSTRAINT ck_financial_transfer_type CHECK (transfer_type IN ('MANUAL', 'AUTO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이체 요청/결과';

CREATE TABLE auto_transfer_schedule (
  auto_transfer_schedule_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '자동이체 일정 ID',
  child_id BIGINT UNSIGNED NULL COMMENT '자동이체 대상 자녀',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '일정 등록 회원 ID',
  source_account_id BIGINT UNSIGNED NOT NULL COMMENT '출금 계좌 ID',
  destination_account_id BIGINT UNSIGNED NOT NULL COMMENT '입금 계좌 ID',
  amount DECIMAL(19, 2) NOT NULL COMMENT '이체 금액',
  frequency VARCHAR(20) NOT NULL DEFAULT 'MONTHLY' COMMENT 'MONTHLY, WEEKLY, DAILY',
  transfer_day INT NOT NULL COMMENT '매월 실행일',
  start_date DATE NOT NULL COMMENT '자동이체 시작일',
  end_date DATE NULL COMMENT '자동이체 종료일',
  next_transfer_at DATETIME(6) NULL COMMENT '다음 이체 예정 시각',
  provider_schedule_id VARCHAR(255) NULL COMMENT '제공기관 일정 ID',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, PAUSED, ENDED, CANCELED',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (auto_transfer_schedule_id),
  KEY idx_auto_transfer_child_id (child_id),
  KEY idx_auto_transfer_member_id (member_id),
  KEY idx_auto_transfer_source_account (source_account_id),
  KEY idx_auto_transfer_destination_account (destination_account_id),
  KEY idx_auto_transfer_next_status (status, next_transfer_at),
  CONSTRAINT fk_auto_transfer_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_auto_transfer_member
    FOREIGN KEY (member_id) REFERENCES member (member_id),
  CONSTRAINT fk_auto_transfer_source_account
    FOREIGN KEY (source_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT fk_auto_transfer_destination_account
    FOREIGN KEY (destination_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT ck_auto_transfer_frequency CHECK (frequency IN ('MONTHLY', 'WEEKLY', 'DAILY')),
  CONSTRAINT ck_auto_transfer_status CHECK (status IN ('ACTIVE', 'PAUSED', 'ENDED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='자동이체 일정';

CREATE TABLE time_capsule (
  time_capsule_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '타임캡슐 ID',
  child_id BIGINT UNSIGNED NOT NULL COMMENT '자녀 ID',
  financial_account_id BIGINT UNSIGNED NOT NULL COMMENT '적금 계좌 ID',
  title VARCHAR(200) NOT NULL COMMENT '보관함 제목',
  status VARCHAR(20) NOT NULL DEFAULT 'COLLECTING' COMMENT 'COLLECTING, RELEASED, ARCHIVED',
  expected_release_at DATETIME(6) NULL COMMENT '예상 공개일',
  release_reason VARCHAR(20) NULL COMMENT 'MATURITY, TERMINATION',
  released_at DATETIME(6) NULL COMMENT '실제 공개 시각',
  entry_count INT NOT NULL DEFAULT 0 COMMENT '보관함 목록용 캐시',
  latest_entry_at DATETIME(6) NULL COMMENT '최근 기록 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (time_capsule_id),
  UNIQUE KEY uk_time_capsule_account_id (financial_account_id),
  KEY idx_time_capsule_child_status (child_id, status),
  CONSTRAINT fk_time_capsule_child
    FOREIGN KEY (child_id) REFERENCES child (child_id),
  CONSTRAINT fk_time_capsule_account
    FOREIGN KEY (financial_account_id) REFERENCES financial_account (financial_account_id),
  CONSTRAINT ck_time_capsule_status CHECK (status IN ('COLLECTING', 'RELEASED', 'ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='적금 계좌 타임캡슐 보관함';

CREATE TABLE time_capsule_entry (
  time_capsule_entry_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '타임캡슐 기록 ID',
  time_capsule_id BIGINT UNSIGNED NOT NULL COMMENT '타임캡슐 ID',
  author_member_id BIGINT UNSIGNED NOT NULL COMMENT '작성 회원 ID',
  account_transaction_id BIGINT UNSIGNED NOT NULL COMMENT '계좌 거래 ID',
  title VARCHAR(200) NOT NULL COMMENT '기록 제목',
  message TEXT NULL COMMENT '부모 메시지',
  contribution_amount DECIMAL(19, 2) NOT NULL COMMENT '기록 금액 스냅샷',
  contributed_at DATETIME(6) NOT NULL COMMENT '기록 발생 시각 스냅샷',
  media_mode VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT 'NONE, IMAGE, VIDEO',
  thumbnail_object_key VARCHAR(1000) NULL COMMENT '대표 썸네일 객체 키',
  blurred_thumbnail_object_key VARCHAR(1000) NULL COMMENT '블러 썸네일 객체 키',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT, SEALED, DELETED',
  edit_count TINYINT NOT NULL DEFAULT 0 COMMENT '수정 횟수',
  sealed_at DATETIME(6) NULL COMMENT '봉인 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (time_capsule_entry_id),
  UNIQUE KEY uk_capsule_entry_transaction (time_capsule_id, account_transaction_id),
  KEY idx_capsule_entry_author_id (author_member_id),
  KEY idx_capsule_entry_transaction_id (account_transaction_id),
  CONSTRAINT fk_capsule_entry_capsule
    FOREIGN KEY (time_capsule_id) REFERENCES time_capsule (time_capsule_id),
  CONSTRAINT fk_capsule_entry_author
    FOREIGN KEY (author_member_id) REFERENCES member (member_id),
  CONSTRAINT fk_capsule_entry_transaction
    FOREIGN KEY (account_transaction_id) REFERENCES account_transaction (account_transaction_id),
  CONSTRAINT ck_capsule_entry_media_mode CHECK (media_mode IN ('NONE', 'IMAGE', 'VIDEO')),
  CONSTRAINT ck_capsule_entry_status CHECK (status IN ('DRAFT', 'SEALED', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='타임캡슐 기록';

CREATE TABLE time_capsule_media (
  time_capsule_media_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '타임캡슐 미디어 ID',
  time_capsule_entry_id BIGINT UNSIGNED NOT NULL COMMENT '타임캡슐 기록 ID',
  media_type VARCHAR(20) NOT NULL COMMENT 'IMAGE, VIDEO',
  object_key VARCHAR(1000) NOT NULL COMMENT 'S3 객체 키',
  mime_type VARCHAR(100) NOT NULL COMMENT 'MIME 타입',
  file_size BIGINT UNSIGNED NOT NULL COMMENT '파일 크기',
  slot_no TINYINT NOT NULL COMMENT '사진 1~3, 영상 1',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING_UPLOAD' COMMENT 'PENDING_UPLOAD, ACTIVE, DELETED',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '업로드일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (time_capsule_media_id),
  UNIQUE KEY uk_capsule_media_entry_slot (time_capsule_entry_id, slot_no),
  CONSTRAINT fk_capsule_media_entry
    FOREIGN KEY (time_capsule_entry_id) REFERENCES time_capsule_entry (time_capsule_entry_id),
  CONSTRAINT ck_capsule_media_type CHECK (media_type IN ('IMAGE', 'VIDEO')),
  CONSTRAINT ck_capsule_media_status CHECK (status IN ('PENDING_UPLOAD', 'ACTIVE', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='타임캡슐 미디어';

CREATE TABLE time_capsule_export (
  time_capsule_export_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '결과물 생성 ID',
  time_capsule_id BIGINT UNSIGNED NOT NULL COMMENT '타임캡슐 ID',
  requested_by_member_id BIGINT UNSIGNED NOT NULL COMMENT '요청 회원 ID',
  export_type VARCHAR(30) NOT NULL COMMENT 'VIDEO, ARCHIVE',
  options_json JSON NULL COMMENT '생성 옵션',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSING, SUCCEEDED, FAILED, EXPIRED',
  output_object_key VARCHAR(1000) NULL COMMENT '결과 객체 키',
  output_mime_type VARCHAR(100) NULL COMMENT '결과 MIME 유형',
  output_file_size BIGINT UNSIGNED NULL COMMENT '결과 파일 크기',
  error_code VARCHAR(100) NULL COMMENT '실패 코드',
  error_message VARCHAR(500) NULL COMMENT '실패 메시지',
  started_at DATETIME(6) NULL COMMENT '작업 시작 시각',
  completed_at DATETIME(6) NULL COMMENT '작업 완료 시각',
  expires_at DATETIME(6) NULL COMMENT '결과 보관 만료 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '요청 시각',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '상태 변경일',
  PRIMARY KEY (time_capsule_export_id),
  KEY idx_capsule_export_capsule_id (time_capsule_id),
  KEY idx_capsule_export_requested_by (requested_by_member_id),
  CONSTRAINT fk_capsule_export_capsule
    FOREIGN KEY (time_capsule_id) REFERENCES time_capsule (time_capsule_id),
  CONSTRAINT fk_capsule_export_requested_by
    FOREIGN KEY (requested_by_member_id) REFERENCES member (member_id),
  CONSTRAINT ck_capsule_export_type CHECK (export_type IN ('VIDEO', 'ARCHIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='타임캡슐 결과물 생성 작업';

CREATE TABLE asset_report (
  asset_report_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '자산 리포트 ID',
  child_id BIGINT UNSIGNED NOT NULL COMMENT '자녀 ID',
  report_month DATE NOT NULL COMMENT '리포트 기준 월 1일',
  total_asset_amount DECIMAL(19, 2) NOT NULL DEFAULT 0 COMMENT '전체 자산 금액',
  total_asset_change_amount DECIMAL(19, 2) NOT NULL DEFAULT 0 COMMENT '이전 달 대비 증감액',
  monthly_saved_amount DECIMAL(19, 2) NOT NULL DEFAULT 0 COMMENT '이번 달 저축액',
  total_goal_target_amount DECIMAL(19, 2) NOT NULL DEFAULT 0 COMMENT '전체 목표 금액',
  total_goal_saved_amount DECIMAL(19, 2) NOT NULL DEFAULT 0 COMMENT '목표 적금 현재 금액',
  goal_achievement_rate DECIMAL(7, 4) NOT NULL DEFAULT 0 COMMENT '전체 목표 달성률',
  six_month_flow_json JSON NULL COMMENT '최근 6개월 저축 흐름',
  savings_goal_summary_json JSON NULL COMMENT '적금 목표별 요약',
  insight_items_json JSON NULL COMMENT '인사이트 목록',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (asset_report_id),
  UNIQUE KEY uk_asset_report_child_month (child_id, report_month),
  CONSTRAINT fk_asset_report_child
    FOREIGN KEY (child_id) REFERENCES child (child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='월간 자산 리포트';

CREATE TABLE notification_preference (
  notification_preference_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '알림 설정 ID',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '회원 ID',
  notification_type VARCHAR(50) NOT NULL COMMENT '알림 유형',
  in_app_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '인앱 수신 여부',
  push_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '푸시 수신 여부',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
  PRIMARY KEY (notification_preference_id),
  UNIQUE KEY uk_notification_preference_member_type (member_id, notification_type),
  CONSTRAINT fk_notification_preference_member
    FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 알림 설정';

CREATE TABLE notification (
  notification_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '알림 ID',
  member_id BIGINT UNSIGNED NOT NULL COMMENT '수신 회원 ID',
  child_id BIGINT UNSIGNED NULL COMMENT '관련 자녀 ID',
  notification_type VARCHAR(50) NOT NULL COMMENT '알림 유형',
  title VARCHAR(200) NOT NULL COMMENT '알림 제목',
  content VARCHAR(2000) NOT NULL COMMENT '알림 내용',
  reference_type VARCHAR(50) NULL COMMENT '이동 대상 유형',
  reference_id BIGINT UNSIGNED NULL COMMENT '이동 대상 식별자',
  is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '읽음 여부',
  sent_at DATETIME(6) NULL COMMENT '발송 시각',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
  PRIMARY KEY (notification_id),
  KEY idx_notification_member_read_created (member_id, is_read, created_at),
  KEY idx_notification_child_id (child_id),
  CONSTRAINT fk_notification_member
    FOREIGN KEY (member_id) REFERENCES member (member_id),
  CONSTRAINT fk_notification_child
    FOREIGN KEY (child_id) REFERENCES child (child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 알림';

