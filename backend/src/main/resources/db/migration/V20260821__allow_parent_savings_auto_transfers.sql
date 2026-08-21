-- 부모 또는 자녀 적금으로 Mock 자동이체를 등록할 수 있도록 목표와 자녀 연결을 선택값으로 전환한다.
ALTER TABLE auto_transfer_schedule
    MODIFY COLUMN child_id BIGINT UNSIGNED NULL COMMENT '자동이체 대상 자녀(자녀 적금인 경우)',
    MODIFY COLUMN financial_goal_id BIGINT UNSIGNED NULL COMMENT '연결 금융 목표 ID(있는 경우)';
