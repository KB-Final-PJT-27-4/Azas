-- 기존 로컬/배포 DB에 목표 금액 추천 기능만 추가하는 비파괴 SQL입니다.
-- financial_goal_template 기본 데이터(ID 1, 2, 3, 4)가 먼저 존재해야 합니다.

CREATE TABLE IF NOT EXISTS financial_goal_recommendation_basis
(
    financial_goal_recommendation_basis_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '추천 기준 ID',
    financial_goal_template_id             BIGINT UNSIGNED NOT NULL COMMENT '목표 템플릿 ID',
    recommendation_method                  VARCHAR(30)     NOT NULL COMMENT 'STATISTICS_REFERENCE, SERVICE_SCENARIO',
    organization                           VARCHAR(200)    NULL COMMENT '통계 제공 기관',
    dataset_name                           VARCHAR(300)    NULL COMMENT '참고 데이터셋명',
    reference_year                         SMALLINT UNSIGNED NULL COMMENT '통계 기준연도',
    metric_name                            VARCHAR(200)    NULL COMMENT '참고 지표명',
    metric_value                           DECIMAL(19, 2)  NULL COMMENT '참고 지표값',
    metric_unit                            VARCHAR(50)     NULL COMMENT '참고 지표 단위',
    source_url                             VARCHAR(1000)   NULL COMMENT '공식 출처 URL',
    description                            VARCHAR(1000)   NOT NULL COMMENT '추천 산정 기준 설명',
    disclaimer                             VARCHAR(1000)   NOT NULL COMMENT '추천 금액 유의사항',
    created_at                             DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
    updated_at                             DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
    PRIMARY KEY (financial_goal_recommendation_basis_id),
    UNIQUE KEY uk_goal_recommendation_basis_template (financial_goal_template_id),
    CONSTRAINT fk_goal_recommendation_basis_template
        FOREIGN KEY (financial_goal_template_id)
            REFERENCES financial_goal_template (financial_goal_template_id),
    CONSTRAINT ck_goal_recommendation_method CHECK (
        recommendation_method IN ('STATISTICS_REFERENCE', 'SERVICE_SCENARIO')
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='목표별 추천 금액 통계·서비스 기준';

CREATE TABLE IF NOT EXISTS financial_goal_amount_recommendation
(
    financial_goal_amount_recommendation_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '추천 금액 ID',
    financial_goal_template_id              BIGINT UNSIGNED NOT NULL COMMENT '목표 템플릿 ID',
    recommendation_code                     VARCHAR(30)     NOT NULL COMMENT 'STARTER, BALANCED, SECURE, LIFECYCLE',
    title                                   VARCHAR(100)    NOT NULL COMMENT '추천안 표시명',
    target_amount                           DECIMAL(19, 2)  NOT NULL COMMENT '추천 목표 금액',
    coverage_items                          VARCHAR(1000)   NOT NULL COMMENT '포함 범위, | 구분',
    display_order                           INT             NOT NULL COMMENT '표시 순서',
    is_active                               TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '노출 여부',
    created_at                              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일',
    updated_at                              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일',
    PRIMARY KEY (financial_goal_amount_recommendation_id),
    UNIQUE KEY uk_goal_amount_recommendation_code (financial_goal_template_id, recommendation_code),
    KEY idx_goal_amount_recommendation_display (financial_goal_template_id, is_active, display_order),
    CONSTRAINT fk_goal_amount_recommendation_template
        FOREIGN KEY (financial_goal_template_id)
            REFERENCES financial_goal_template (financial_goal_template_id),
    CONSTRAINT ck_goal_amount_recommendation_amount CHECK (target_amount > 0),
    CONSTRAINT ck_goal_amount_recommendation_code CHECK (
        recommendation_code IN ('STARTER', 'BALANCED', 'SECURE', 'LIFECYCLE')
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='목표별 4단계 생애주기 추천 금액';

INSERT INTO financial_goal_recommendation_basis (
  financial_goal_template_id, recommendation_method, organization,
  dataset_name, reference_year, metric_name, metric_value, metric_unit,
  source_url, description, disclaimer
) VALUES
  (1, 'STATISTICS_REFERENCE', '교육부·한국대학교육협의회',
   '2025년 4월 대학정보공시 분석 결과', 2025,
   '4년제 일반·교육대학 1인당 연평균 등록금', 7106500, '원/년',
   'https://www.moe.go.kr/boardCnts/viewRenew.do?boardID=294&boardSeq=103257&lev=0&m=020402&opType=N',
   '연평균 등록금 7,106,500원의 4년치 28,426,000원을 기본 근거로 교육·생활·사회초년 비용을 단계별로 구성했습니다.',
   '공공 통계를 참고한 서비스 추천금액이며 실제 대학과 생활 방식에 따른 비용 또는 목표 달성을 보장하지 않습니다.'),
  (2, 'STATISTICS_REFERENCE', '국가데이터처', '2024년 주택소유통계', 2024,
   '주택 소유 가구 평균 주택 자산가액', 333000000, '원',
   'https://www.kostat.go.kr/board.es?act=view&bid=11471&list_no=439298&mid=a10301100400',
   '평균 주택 자산가액 333,000,000원은 지역 편차가 커 직접 목표로 쓰지 않고 계약금·보증금·주택 구입 종잣돈 단계의 참고값으로 사용했습니다.',
   '주택 전체 구매가격이 아닌 초기 주거 종잣돈 참고값이며 실제 지역과 주택 유형에 따라 크게 달라질 수 있습니다.'),
  (4, 'STATISTICS_REFERENCE', '한국소비자원', '2025년 4월 결혼서비스 가격조사', 2025,
   '전국 결혼서비스 평균 계약금액', 21010000, '원',
   'https://www.kca.go.kr/webzine/board/view?div=kca_2507&linkId=868&menuId=MENU00307',
   '결혼식장과 스드메 평균 계약금액 21,010,000원을 시작 근거로 신혼여행·가전·초기 생활비를 단계별로 더했습니다.',
   '주거비가 제외된 결혼서비스 조사에 서비스 시나리오를 더한 참고값이며 실제 결혼 비용을 보장하지 않습니다.'),
  (3, 'STATISTICS_REFERENCE', '통계청', '2024년 가계금융복지조사', 2024,
   '가구주 39세 이하 가구 평균 금융자산', 130790000, '원',
   'https://www.kostat.go.kr/board.es?act=view&bid=215&list_no=434107&mid=a10301010000',
   '39세 이하 가구 평균 금융자산 130,790,000원은 자녀 개인 평균이 아니므로 규모 검토에만 참고해 장기 종잣돈을 단계별로 구성했습니다.',
   '가구 단위 금융자산을 참고한 서비스 추천금액이며 자녀 개인의 미래 자산 또는 수익을 보장하지 않습니다.')
ON DUPLICATE KEY UPDATE
  recommendation_method = VALUES(recommendation_method),
  organization = VALUES(organization),
  dataset_name = VALUES(dataset_name),
  reference_year = VALUES(reference_year),
  metric_name = VALUES(metric_name),
  metric_value = VALUES(metric_value),
  metric_unit = VALUES(metric_unit),
  source_url = VALUES(source_url),
  description = VALUES(description),
  disclaimer = VALUES(disclaimer);

INSERT INTO financial_goal_amount_recommendation (
  financial_goal_template_id, recommendation_code, title, target_amount,
  coverage_items, display_order, is_active
) VALUES
  (1, 'STARTER', '시작 준비안', 30000000, '4년 등록금 중심|교재 및 학습비 일부', 1, 1),
  (1, 'BALANCED', '균형 준비안', 50000000, '등록금|생활비|취업 준비비', 2, 1),
  (1, 'SECURE', '든든 준비안', 70000000, '등록금|생활비|교환학생 또는 추가 교육비', 3, 1),
  (1, 'LIFECYCLE', '생애주기 준비안', 100000000, '등록금|생활비|주거비|사회초년 자금', 4, 1),
  (2, 'STARTER', '시작 준비안', 50000000, '청약 계약금|초기 보증금', 1, 1),
  (2, 'BALANCED', '균형 준비안', 100000000, '전월세 보증금|주택 구입 종잣돈', 2, 1),
  (2, 'SECURE', '든든 준비안', 200000000, '주택 구입 종잣돈|이사 및 정착 비용', 3, 1),
  (2, 'LIFECYCLE', '생애주기 준비안', 300000000, '주택 구입 종잣돈|장기 주거 안정 자금', 4, 1),
  (4, 'STARTER', '시작 준비안', 20000000, '예식|신혼여행', 1, 1),
  (4, 'BALANCED', '균형 준비안', 30000000, '예식|신혼여행|가전', 2, 1),
  (4, 'SECURE', '든든 준비안', 50000000, '예식|가전|신혼생활', 3, 1),
  (4, 'LIFECYCLE', '생애주기 준비안', 80000000, '예식|가전|신혼생활|주거 종잣돈', 4, 1),
  (3, 'STARTER', '시작 준비안', 30000000, '비상금|첫 종잣돈', 1, 1),
  (3, 'BALANCED', '균형 준비안', 50000000, '교육|취업 준비|독립 준비', 2, 1),
  (3, 'SECURE', '든든 준비안', 100000000, '교육|독립 준비|장기 자산 기반', 3, 1),
  (3, 'LIFECYCLE', '생애주기 준비안', 200000000, '교육|독립|주거 종잣돈|장기 자산', 4, 1)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  target_amount = VALUES(target_amount),
  coverage_items = VALUES(coverage_items),
  display_order = VALUES(display_order),
  is_active = VALUES(is_active);
