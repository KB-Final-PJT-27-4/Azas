USE azas;

START TRANSACTION;

INSERT INTO checklist_item_template (
    template_key,
    lifecycle_stage,
    category,
    title,
    description,
    detail_content,
    action_type,
    action_url,
    info_title,
    info_notice,
    item_order,
    is_active
)
VALUES
(
        'prenatal-support-after-birth',
        'PREGNANCY',
        'SUPPORT',
        '출산 후 받을 수 있는 지원제도 확인하기',
        '출산·육아 관련 지원제도를 미리 확인해요.',
        '시기별 지원 정보를 먼저 확인해보세요.',
        'INFO',
        NULL,
        '출산 후 받을 수 있는 지원제도 확인하기',
        '지원 대상, 금액, 신청 기간은 시기와 지역에 따라 달라질 수 있어요.',
        1,
        1
    ),
(
        'prenatal-childcare-cost',
        'PREGNANCY',
        'ASSET',
        '출산 후 예상 양육비 계산해보기',
        '출산 후 필요한 월 양육비를 미리 가늠해봐요.',
        '출산 후 필요한 월 양육비를 미리 가늠해봐요.',
        'ROUTE',
        '/reports',
        NULL,
        NULL,
        2,
        1
    ),
(
        'prenatal-future-goal',
        'PREGNANCY',
        'SERVICE',
        '우리 아이 미래자산 목표 정하기',
        '대학·독립 등 장기 목표를 미리 정해봐요.',
        '대학·독립 등 장기 목표를 미리 정해봐요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        3,
        1
    ),
(
        'prenatal-monthly-saving',
        'PREGNANCY',
        'ASSET',
        '매달 아이를 위해 저축할 금액 정하기',
        '꾸준히 이어갈 수 있는 월 저축액을 정해요.',
        '꾸준히 이어갈 수 있는 월 저축액을 정해요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        4,
        1
    ),
(
        'prenatal-account-guide',
        'PREGNANCY',
        'EDUCATION',
        '아이 명의 계좌 개설 방법 알아보기',
        '필요 서류와 계좌 개설 흐름을 확인해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '아이 명의 계좌 개설 방법 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        5,
        1
    ),
(
        'prenatal-gift-record',
        'PREGNANCY',
        'EDUCATION',
        '자녀에게 재산을 증여할 때 기록해야 할 내용 알아보기',
        '축하금·세뱃돈·증여금 기록 기준을 미리 확인해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '자녀에게 재산을 증여할 때 기록해야 할 내용 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        6,
        1
    ),
(
        'prenatal-first-account',
        'PREGNANCY',
        'SERVICE',
        '아이 첫 계좌 연결 준비하기',
        '자산 관리를 시작할 계좌 연결 흐름을 확인해요.',
        '자산 관리를 시작할 계좌 연결 흐름을 확인해요.',
        'ROUTE',
        '/accounts',
        NULL,
        NULL,
        7,
        1
    ),
(
        'prenatal-time-capsule',
        'PREGNANCY',
        'SERVICE',
        '출산 전 첫 타임캡슐 만들기',
        '아이에게 전할 첫 기록을 미리 남겨요.',
        '아이에게 전할 첫 기록을 미리 남겨요.',
        'ROUTE',
        '/time-capsules/new',
        NULL,
        NULL,
        8,
        1
    ),
(
        'prenatal-kb-saving',
        'PREGNANCY',
        'ASSET',
        '아이에게 맞는 KB 적금 후보 확인하기',
        '목표와 기간에 맞는 대표 상품을 살펴봐요.',
        '목표와 기간에 맞는 대표 상품을 살펴봐요.',
        'ROUTE',
        '/products',
        NULL,
        NULL,
        9,
        1
    ),
(
        'baby-child-profile',
        'AGE_0_TO_1',
        'SERVICE',
        '자녀 프로필 등록하기',
        '아이 이름과 성장 단계를 서비스에 등록해요.',
        '서비스에서 바로 이어서 해볼 수 있어요.',
        'INFO',
        NULL,
        '자녀 프로필 등록하기',
        '실제 화면과 연결되기 전까지는 체크리스트 안내용 더미 데이터입니다.',
        1,
        1
    ),
(
        'baby-account-prepare',
        'AGE_0_TO_1',
        'EDUCATION',
        '아이 명의 계좌 개설 준비하기',
        '은행 방문 전 필요한 서류와 순서를 확인해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '아이 명의 계좌 개설 준비하기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        2,
        1
    ),
(
        'baby-connect-account',
        'AGE_0_TO_1',
        'SERVICE',
        '아이 계좌를 서비스에 연결하기',
        '연결된 계좌로 아이 자산을 한눈에 확인해요.',
        '연결된 계좌로 아이 자산을 한눈에 확인해요.',
        'ROUTE',
        '/accounts',
        NULL,
        NULL,
        3,
        1
    ),
(
        'baby-main-account',
        'AGE_0_TO_1',
        'SERVICE',
        '아이의 대표 입출금계좌 정하기',
        '용돈과 지원금이 들어올 대표 계좌를 정해요.',
        '서비스에서 바로 이어서 해볼 수 있어요.',
        'INFO',
        NULL,
        '아이의 대표 입출금계좌 정하기',
        '실제 화면과 연결되기 전까지는 체크리스트 안내용 더미 데이터입니다.',
        4,
        1
    ),
(
        'baby-support-money',
        'AGE_0_TO_1',
        'SUPPORT',
        '출산·양육 관련 지원금 확인하기',
        '입금 예정 지원금과 신청 상태를 확인해요.',
        '시기별 지원 정보를 먼저 확인해보세요.',
        'INFO',
        NULL,
        '출산·양육 관련 지원금 확인하기',
        '지원 대상, 금액, 신청 기간은 시기와 지역에 따라 달라질 수 있어요.',
        5,
        1
    ),
(
        'baby-first-goal',
        'AGE_0_TO_1',
        'SERVICE',
        '아이의 첫 미래자산 목표 만들기',
        '작은 목표부터 장기 목표까지 설정해요.',
        '작은 목표부터 장기 목표까지 설정해요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        6,
        1
    ),
(
        'baby-first-saving',
        'AGE_0_TO_1',
        'ASSET',
        '첫 10만 원 저축 시작하기',
        '첫 저축 기록을 목표 진행률에 반영해요.',
        '첫 저축 기록을 목표 진행률에 반영해요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        7,
        1
    ),
(
        'baby-memory-capsule',
        'AGE_0_TO_1',
        'SERVICE',
        '첫 기념 저축과 타임캡슐 남기기',
        '저축한 날의 사진과 편지를 함께 남겨요.',
        '저축한 날의 사진과 편지를 함께 남겨요.',
        'ROUTE',
        '/time-capsules/new',
        NULL,
        NULL,
        8,
        1
    ),
(
        'toddler-total-asset',
        'AGE_2_TO_4',
        'ASSET',
        '현재 아이의 총자산 확인하기',
        '계좌와 목표를 합쳐 현재 자산을 확인해요.',
        '계좌와 목표를 합쳐 현재 자산을 확인해요.',
        'ROUTE',
        '/assets',
        NULL,
        NULL,
        1,
        1
    ),
(
        'toddler-asset-growth',
        'AGE_2_TO_4',
        'ASSET',
        '지난달보다 자산이 얼마나 늘었는지 확인하기',
        '월별 자산 변화를 리포트에서 확인해요.',
        '월별 자산 변화를 리포트에서 확인해요.',
        'ROUTE',
        '/reports',
        NULL,
        NULL,
        2,
        1
    ),
(
        'toddler-saving-plan',
        'AGE_2_TO_4',
        'ASSET',
        '매월 저축 계획이 유지되고 있는지 확인하기',
        '계획한 저축액이 꾸준히 들어가고 있는지 봐요.',
        '계획한 저축액이 꾸준히 들어가고 있는지 봐요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        3,
        1
    ),
(
        'toddler-pocket-money',
        'AGE_2_TO_4',
        'ASSET',
        '받은 용돈·세뱃돈·축하금 관리하기',
        '아이에게 들어온 돈의 출처와 사용 목적을 기록해요.',
        '아이 자산 흐름을 점검해보세요.',
        'INFO',
        NULL,
        '받은 용돈·세뱃돈·축하금 관리하기',
        '금액과 기준은 가정용 예시이며, API 연동 후 실제 데이터로 교체합니다.',
        4,
        1
    ),
(
        'toddler-goal-rate',
        'AGE_2_TO_4',
        'SERVICE',
        '미래자산 목표 달성률 확인하기',
        '목표 금액 대비 진행률을 점검해요.',
        '목표 금액 대비 진행률을 점검해요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        5,
        1
    ),
(
        'toddler-child-saving-difference',
        'AGE_2_TO_4',
        'EDUCATION',
        '자녀 명의 저축과 부모 명의 저축의 차이 알아보기',
        '명의에 따라 관리 방식과 기록 기준이 달라질 수 있어요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '자녀 명의 저축과 부모 명의 저축의 차이 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        6,
        1
    ),
(
        'toddler-checkpoint-capsule',
        'AGE_2_TO_4',
        'SERVICE',
        '목표 체크포인트 기념 타임캡슐 만들기',
        '자산이 늘어난 순간을 사진과 편지로 남겨요.',
        '자산이 늘어난 순간을 사진과 편지로 남겨요.',
        'ROUTE',
        '/time-capsules/new',
        NULL,
        NULL,
        7,
        1
    ),
(
        'preschool-want-need',
        'AGE_5_TO_7',
        'EDUCATION',
        '사고 싶은 것과 필요한 것 구분해보기',
        '아이와 소비 우선순위를 쉬운 말로 이야기해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '사고 싶은 것과 필요한 것 구분해보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        1,
        1
    ),
(
        'preschool-price-check',
        'AGE_5_TO_7',
        'EDUCATION',
        '갖고 싶은 물건의 가격 알아보기',
        '가격표를 보며 돈의 크기를 이해해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '갖고 싶은 물건의 가격 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        2,
        1
    ),
(
        'preschool-first-saving-goal',
        'AGE_5_TO_7',
        'ASSET',
        '아이와 함께 첫 저축 목표 만들기',
        '작은 장난감이나 책을 목표로 정해봐요.',
        '작은 장난감이나 책을 목표로 정해봐요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        3,
        1
    ),
(
        'preschool-save-part',
        'AGE_5_TO_7',
        'EDUCATION',
        '받은 돈 중 일부를 저축해보기',
        '쓰는 돈과 모으는 돈을 나눠보는 경험을 해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '받은 돈 중 일부를 저축해보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        4,
        1
    ),
(
        'preschool-first-allowance',
        'AGE_5_TO_7',
        'ALLOWANCE',
        '아이의 첫 용돈 금액 정하기',
        '아이에게 이해하기 쉬운 금액과 주기를 정해요.',
        '아이에게 이해하기 쉬운 금액과 주기를 정해요.',
        'ROUTE',
        '/child/allowance',
        NULL,
        NULL,
        5,
        1
    ),
(
        'preschool-small-mission',
        'AGE_5_TO_7',
        'ALLOWANCE',
        '작은 생활 미션을 완료해보기',
        '정리하기, 준비물 챙기기 같은 생활 미션을 시작해요.',
        '용돈과 미션은 분리해서 관리하는 것을 추천해요.',
        'INFO',
        NULL,
        '작은 생활 미션을 완료해보기',
        '기본 용돈은 정기 지급, 미션은 추가 보상으로 분리하는 것을 추천합니다.',
        6,
        1
    ),
(
        'preschool-first-capsule',
        'AGE_5_TO_7',
        'SERVICE',
        '아이의 첫 금융 경험을 타임캡슐로 남기기',
        '처음 돈을 모은 순간을 기록해요.',
        '처음 돈을 모은 순간을 기록해요.',
        'ROUTE',
        '/time-capsules/new',
        NULL,
        NULL,
        7,
        1
    ),
(
        'childhood-connect-child',
        'AGE_8_TO_10',
        'SERVICE',
        '내 계정을 부모님과 연결하기',
        '아이 화면에서 용돈과 기록을 직접 확인할 수 있게 해요.',
        '서비스에서 바로 이어서 해볼 수 있어요.',
        'INFO',
        NULL,
        '내 계정을 부모님과 연결하기',
        '실제 화면과 연결되기 전까지는 체크리스트 안내용 더미 데이터입니다.',
        1,
        1
    ),
(
        'childhood-allowance-plan',
        'AGE_8_TO_10',
        'ALLOWANCE',
        '이번 주에 필요한 용돈 계획하기',
        '이번 주에 쓸 돈과 모을 돈을 나눠봐요.',
        '이번 주에 쓸 돈과 모을 돈을 나눠봐요.',
        'ROUTE',
        '/child/allowance',
        NULL,
        NULL,
        2,
        1
    ),
(
        'childhood-request-allowance',
        'AGE_8_TO_10',
        'ALLOWANCE',
        '부모님에게 용돈 요청 보내기',
        '필요한 금액과 이유를 적어서 요청해요.',
        '필요한 금액과 이유를 적어서 요청해요.',
        'ROUTE',
        '/child/allowance',
        NULL,
        NULL,
        3,
        1
    ),
(
        'childhood-saving-from-allowance',
        'AGE_8_TO_10',
        'ASSET',
        '받은 용돈 중 저축할 금액 정하기',
        '용돈 일부를 목표 저축으로 옮겨요.',
        '용돈 일부를 목표 저축으로 옮겨요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        4,
        1
    ),
(
        'childhood-wish-goal',
        'AGE_8_TO_10',
        'ASSET',
        '갖고 싶은 물건을 저축 목표로 만들기',
        '원하는 물건을 목표로 설정하고 진행률을 봐요.',
        '원하는 물건을 목표로 설정하고 진행률을 봐요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        5,
        1
    ),
(
        'childhood-spending-check',
        'AGE_8_TO_10',
        'ASSET',
        '이번 주에 사용한 돈 확인하기',
        '최근 돈 기록에서 소비 내역을 확인해요.',
        '최근 돈 기록에서 소비 내역을 확인해요.',
        'ROUTE',
        '/child/assets',
        NULL,
        NULL,
        6,
        1
    ),
(
        'childhood-safe-money',
        'AGE_8_TO_10',
        'EDUCATION',
        '돈을 안전하게 사용하는 방법 알아보기',
        '비밀번호, 계좌정보, 낯선 링크를 조심하는 방법을 배워요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '돈을 안전하게 사용하는 방법 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        7,
        1
    ),
(
        'early-teen-month-budget',
        'AGE_11_TO_13',
        'ALLOWANCE',
        '이번 달 용돈 예산 세우기',
        '한 달 동안 쓸 돈과 모을 돈을 계획해요.',
        '한 달 동안 쓸 돈과 모을 돈을 계획해요.',
        'ROUTE',
        '/child/allowance',
        NULL,
        NULL,
        1,
        1
    ),
(
        'early-teen-split-spend-save',
        'AGE_11_TO_13',
        'ALLOWANCE',
        '소비·저축 금액을 나누어 계획하기',
        '필요한 소비와 목표 저축을 구분해요.',
        '용돈과 미션은 분리해서 관리하는 것을 추천해요.',
        'INFO',
        NULL,
        '소비·저축 금액을 나누어 계획하기',
        '기본 용돈은 정기 지급, 미션은 추가 보상으로 분리하는 것을 추천합니다.',
        2,
        1
    ),
(
        'early-teen-transaction-weekly',
        'AGE_11_TO_13',
        'ASSET',
        '일주일에 한 번 거래내역 확인하기',
        '계획하지 않았던 소비가 있었는지 확인해요.',
        '계획하지 않았던 소비가 있었는지 확인해요.',
        'ROUTE',
        '/child/assets',
        NULL,
        NULL,
        3,
        1
    ),
(
        'early-teen-saving-goal',
        'AGE_11_TO_13',
        'ASSET',
        '이번 달 저축 목표 달성하기',
        '목표 달성률을 확인하고 부족한 금액을 계산해요.',
        '목표 달성률을 확인하고 부족한 금액을 계산해요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        4,
        1
    ),
(
        'early-teen-interest',
        'AGE_11_TO_13',
        'EDUCATION',
        '이자와 금리가 무엇인지 알아보기',
        '돈을 맡겼을 때 왜 이자가 생기는지 배워요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '이자와 금리가 무엇인지 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        5,
        1
    ),
(
        'early-teen-card-safe',
        'AGE_11_TO_13',
        'EDUCATION',
        '체크카드 사용 시 주의사항 알아보기',
        '잔액, 결제 문자, 분실 신고 방법을 확인해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '체크카드 사용 시 주의사항 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        6,
        1
    ),
(
        'early-teen-report',
        'AGE_11_TO_13',
        'SERVICE',
        '아이의 소비·저축 리포트 함께 보기',
        '월간 사용 금액과 저축 흐름을 같이 확인해요.',
        '월간 사용 금액과 저축 흐름을 같이 확인해요.',
        'ROUTE',
        '/reports',
        NULL,
        NULL,
        7,
        1
    ),
(
        'teen-budget',
        'AGE_14_TO_16',
        'ASSET',
        '이번 달 예산 작성하기',
        '고정지출과 변동지출을 나눠 월 예산을 세워요.',
        '고정지출과 변동지출을 나눠 월 예산을 세워요.',
        'ROUTE',
        '/reports',
        NULL,
        NULL,
        1,
        1
    ),
(
        'teen-fixed-variable',
        'AGE_14_TO_16',
        'EDUCATION',
        '고정지출과 변동지출 구분하기',
        '매달 반복되는 지출과 선택 지출을 구분해요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '고정지출과 변동지출 구분하기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        2,
        1
    ),
(
        'teen-short-long-goal',
        'AGE_14_TO_16',
        'ASSET',
        '단기 목표와 장기 목표 구분하기',
        '이번 달 목표와 몇 년 뒤 목표를 따로 관리해요.',
        '이번 달 목표와 몇 년 뒤 목표를 따로 관리해요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        3,
        1
    ),
(
        'teen-compare-spending',
        'AGE_14_TO_16',
        'ASSET',
        '계획과 실제 소비 비교하기',
        '이번 달 예산과 실제 사용 금액 차이를 확인해요.',
        '이번 달 예산과 실제 사용 금액 차이를 확인해요.',
        'ROUTE',
        '/reports',
        NULL,
        NULL,
        4,
        1
    ),
(
        'teen-emergency-fund',
        'AGE_14_TO_16',
        'ASSET',
        '비상금 목표 만들기',
        '예상하지 못한 상황에 대비하는 목표를 세워요.',
        '아이 자산 흐름을 점검해보세요.',
        'INFO',
        NULL,
        '비상금 목표 만들기',
        '금액과 기준은 가정용 예시이며, API 연동 후 실제 데이터로 교체합니다.',
        5,
        1
    ),
(
        'teen-privacy',
        'AGE_14_TO_16',
        'EDUCATION',
        '개인정보와 계좌정보 보호 방법 알아보기',
        '금융사기와 명의도용을 예방하는 습관을 배워요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '개인정보와 계좌정보 보호 방법 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        6,
        1
    ),
(
        'teen-capsule',
        'AGE_14_TO_16',
        'SERVICE',
        '목표 달성 과정 타임캡슐 남기기',
        '목표를 향해 가는 과정을 사진과 편지로 기록해요.',
        '목표를 향해 가는 과정을 사진과 편지로 기록해요.',
        'ROUTE',
        '/time-capsules/new',
        NULL,
        NULL,
        7,
        1
    ),
(
        'future-next-goal',
        'AGE_17_TO_19',
        'ASSET',
        '대학·취업·독립 중 다음 목표 선택하기',
        '성인이 된 뒤 필요한 첫 목표를 선택해요.',
        '성인이 된 뒤 필요한 첫 목표를 선택해요.',
        'ROUTE',
        '/goals',
        NULL,
        NULL,
        1,
        1
    ),
(
        'future-living-cost',
        'AGE_17_TO_19',
        'ASSET',
        '독립 생활비 예상하기',
        '월세, 식비, 교통비 등 필요한 생활비를 계산해요.',
        '월세, 식비, 교통비 등 필요한 생활비를 계산해요.',
        'ROUTE',
        '/reports',
        NULL,
        NULL,
        2,
        1
    ),
(
        'future-account-purpose',
        'AGE_17_TO_19',
        'ASSET',
        '계좌별 사용 목적 구분하기',
        '생활비, 저축, 비상금 계좌의 역할을 나눠요.',
        '아이 자산 흐름을 점검해보세요.',
        'INFO',
        NULL,
        '계좌별 사용 목적 구분하기',
        '금액과 기준은 가정용 예시이며, API 연동 후 실제 데이터로 교체합니다.',
        3,
        1
    ),
(
        'future-subscription-check',
        'AGE_17_TO_19',
        'ASSET',
        '정기결제와 구독 서비스 점검하기',
        '매달 빠져나가는 돈을 확인하고 필요 없는 지출을 줄여요.',
        '아이 자산 흐름을 점검해보세요.',
        'INFO',
        NULL,
        '정기결제와 구독 서비스 점검하기',
        '금액과 기준은 가정용 예시이며, API 연동 후 실제 데이터로 교체합니다.',
        4,
        1
    ),
(
        'future-credit',
        'AGE_17_TO_19',
        'EDUCATION',
        '신용점수와 부채의 기본 개념 알아보기',
        '신용을 지키는 습관과 빚의 의미를 배워요.',
        '부모와 아이가 함께 이해하면 좋아요.',
        'INFO',
        NULL,
        '신용점수와 부채의 기본 개념 알아보기',
        '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
        5,
        1
    ),
(
        'future-youth-support',
        'AGE_17_TO_19',
        'SUPPORT',
        '대학·취업·청년 관련 지원제도 확인하기',
        '장학금, 취업, 청년 자산형성 지원을 확인해요.',
        '시기별 지원 정보를 먼저 확인해보세요.',
        'INFO',
        NULL,
        '대학·취업·청년 관련 지원제도 확인하기',
        '지원 대상, 금액, 신청 기간은 시기와 지역에 따라 달라질 수 있어요.',
        6,
        1
    ),
(
        'future-final-capsule',
        'AGE_17_TO_19',
        'SERVICE',
        '성인이 되는 날 공개할 타임캡슐 확인하기',
        '오랫동안 모은 성장 기록을 함께 돌아봐요.',
        '오랫동안 모은 성장 기록을 함께 돌아봐요.',
        'ROUTE',
        '/time-capsules',
        NULL,
        NULL,
        7,
        1
    )
ON DUPLICATE KEY UPDATE
    template_key = VALUES(template_key),
    lifecycle_stage = VALUES(lifecycle_stage),
    category = VALUES(category),
    title = VALUES(title),
    description = VALUES(description),
    detail_content = VALUES(detail_content),
    action_type = VALUES(action_type),
    action_url = VALUES(action_url),
    info_title = VALUES(info_title),
    info_notice = VALUES(info_notice),
    item_order = VALUES(item_order),
    is_active = VALUES(is_active),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '첫만남 이용권',
    '출생 초기 양육 부담을 덜기 위한 지원을 확인해요.',
    '대상·조건 확인하기',
    'https://www.bokjiro.go.kr/ssis-tbu/index.do',
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'prenatal-support-after-birth'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '부모급여',
    '영아기 양육 가정을 위한 지원 조건을 확인해요.',
    '대상·조건 확인하기',
    'https://www.bokjiro.go.kr/ssis-tbu/index.do',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'prenatal-support-after-birth'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아동수당',
    '아동 양육과 관련된 기본 지원 내용을 확인해요.',
    '대상·조건 확인하기',
    'https://www.bokjiro.go.kr/ssis-tbu/index.do',
    NULL,
    3
FROM checklist_item_template
WHERE template_key = 'prenatal-support-after-birth'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '거주 지역 출산지원',
    '지자체별 출산·양육 지원이 있는지 확인해요.',
    '지역별 지원 확인하기',
    'https://www.gov.kr/portal/main',
    NULL,
    4
FROM checklist_item_template
WHERE template_key = 'prenatal-support-after-birth'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '보호자 신분증',
    '법정대리인 확인을 위해 보호자 신분증이 필요해요.',
    '준비물 확인하기',
    NULL,
    '은행 방문 전 보호자 신분증과 아이 기준 서류가 필요한지 먼저 확인해두면 다시 방문하는 일을 줄일 수 있습니다.',
    1
FROM checklist_item_template
WHERE template_key = 'prenatal-account-guide'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '가족관계 확인 서류',
    '아이와 보호자 관계를 확인할 수 있는 서류를 준비해요.',
    '서류 확인하기',
    'https://efamily.scourt.go.kr/',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'prenatal-account-guide'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이 기본증명서',
    '금융기관 요청에 따라 기본증명서가 필요할 수 있어요.',
    '발급 방법 보기',
    'https://efamily.scourt.go.kr/',
    NULL,
    3
FROM checklist_item_template
WHERE template_key = 'prenatal-account-guide'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'prenatal-gift-record'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'prenatal-gift-record'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'prenatal-gift-record'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '연결 화면 확인',
    '관련 기능 화면으로 이동해 현재 준비 상태를 확인해요.',
    '화면 보기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'baby-child-profile'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '다음 행동 정하기',
    '목표 만들기, 계좌 연결, 기록 남기기 중 필요한 행동을 골라요.',
    '준비하기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'baby-child-profile'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'baby-account-prepare'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'baby-account-prepare'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'baby-account-prepare'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '연결 화면 확인',
    '관련 기능 화면으로 이동해 현재 준비 상태를 확인해요.',
    '화면 보기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'baby-main-account'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '다음 행동 정하기',
    '목표 만들기, 계좌 연결, 기록 남기기 중 필요한 행동을 골라요.',
    '준비하기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'baby-main-account'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '정부 지원 확인',
    '출산, 양육, 돌봄, 교육과 관련된 공통 지원을 확인해요.',
    '지원 확인하기',
    'https://www.bokjiro.go.kr/ssis-tbu/index.do',
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'baby-support-money'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '지역 지원 확인',
    '거주 지역별로 받을 수 있는 지원이 있는지 살펴봐요.',
    '지역별 보기',
    'https://www.gov.kr/portal/main',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'baby-support-money'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '현재 금액 확인',
    '계좌 잔액, 저축 금액, 이번 달 사용 금액을 먼저 확인해요.',
    '자산 점검하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'toddler-pocket-money'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '목표와 비교',
    '계획한 저축액과 실제 모인 금액을 비교해요.',
    '목표 확인하기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'toddler-pocket-money'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'toddler-child-saving-difference'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'toddler-child-saving-difference'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'toddler-child-saving-difference'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'preschool-want-need'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'preschool-want-need'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'preschool-want-need'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'preschool-price-check'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'preschool-price-check'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'preschool-price-check'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'preschool-save-part'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'preschool-save-part'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'preschool-save-part'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기본 용돈 정하기',
    '아이가 예측할 수 있도록 정기 지급 금액과 주기를 정해요.',
    '용돈 계획하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'preschool-small-mission'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '미션 보상 분리',
    '기본 용돈과 별도로 추가 보상 미션을 설계해요.',
    '미션 보기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'preschool-small-mission'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '연결 화면 확인',
    '관련 기능 화면으로 이동해 현재 준비 상태를 확인해요.',
    '화면 보기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'childhood-connect-child'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '다음 행동 정하기',
    '목표 만들기, 계좌 연결, 기록 남기기 중 필요한 행동을 골라요.',
    '준비하기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'childhood-connect-child'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'childhood-safe-money'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'childhood-safe-money'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'childhood-safe-money'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기본 용돈 정하기',
    '아이가 예측할 수 있도록 정기 지급 금액과 주기를 정해요.',
    '용돈 계획하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'early-teen-split-spend-save'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '미션 보상 분리',
    '기본 용돈과 별도로 추가 보상 미션을 설계해요.',
    '미션 보기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'early-teen-split-spend-save'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'early-teen-interest'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'early-teen-interest'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'early-teen-interest'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'early-teen-card-safe'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'early-teen-card-safe'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'early-teen-card-safe'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'teen-fixed-variable'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'teen-fixed-variable'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'teen-fixed-variable'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '현재 금액 확인',
    '계좌 잔액, 저축 금액, 이번 달 사용 금액을 먼저 확인해요.',
    '자산 점검하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'teen-emergency-fund'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '목표와 비교',
    '계획한 저축액과 실제 모인 금액을 비교해요.',
    '목표 확인하기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'teen-emergency-fund'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'teen-privacy'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'teen-privacy'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'teen-privacy'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '현재 금액 확인',
    '계좌 잔액, 저축 금액, 이번 달 사용 금액을 먼저 확인해요.',
    '자산 점검하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'future-account-purpose'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '목표와 비교',
    '계획한 저축액과 실제 모인 금액을 비교해요.',
    '목표 확인하기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'future-account-purpose'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '현재 금액 확인',
    '계좌 잔액, 저축 금액, 이번 달 사용 금액을 먼저 확인해요.',
    '자산 점검하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'future-subscription-check'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '목표와 비교',
    '계획한 저축액과 실제 모인 금액을 비교해요.',
    '목표 확인하기',
    NULL,
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'future-subscription-check'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '필요 서류와 준비물 확인',
    '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
    '준비물 확인하기',
    NULL,
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'future-credit'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '아이에게 설명할 금융 개념 정리',
    '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
    '개념 살펴보기',
    'https://www.fss.or.kr/edu',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'future-credit'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '기록이 필요한 순간 확인',
    '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
    '기록 기준 보기',
    NULL,
    '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    3
FROM checklist_item_template
WHERE template_key = 'future-credit'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '정부 지원 확인',
    '출산, 양육, 돌봄, 교육과 관련된 공통 지원을 확인해요.',
    '지원 확인하기',
    'https://www.bokjiro.go.kr/ssis-tbu/index.do',
    NULL,
    1
FROM checklist_item_template
WHERE template_key = 'future-youth-support'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    '지역 지원 확인',
    '거주 지역별로 받을 수 있는 지원이 있는지 살펴봐요.',
    '지역별 보기',
    'https://www.gov.kr/portal/main',
    NULL,
    2
FROM checklist_item_template
WHERE template_key = 'future-youth-support'
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);


COMMIT;
