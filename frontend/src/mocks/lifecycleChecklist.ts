export type LifecycleCategory =
  | 'service'
  | 'asset'
  | 'education'
  | 'support'
  | 'allowance'

export type ChecklistActionType = 'info' | 'route'

export interface LifecycleStage {
  id: string
  ageRange: string
  title: string
  description: string
}

export interface ChecklistInfoItem {
  title: string
  description: string
  actionLabel: string
  externalUrl?: string
  detail?: string
}

export interface ChecklistItem {
  id: string
  stageId: string
  category: LifecycleCategory
  title: string
  description: string
  completed: boolean
  actionType: ChecklistActionType
  route?: string
  externalUrl?: string
  infoTitle?: string
  infoDescription?: string
  infoItems?: ChecklistInfoItem[]
  infoNotice?: string
}

export const lifecycleCategories: Array<{
  id: LifecycleCategory | 'all'
  label: string
}> = [
  { id: 'all', label: '전체' },
  { id: 'service', label: '서비스 가이드' },
  { id: 'asset', label: '자산관리' },
  { id: 'education', label: '금융교육' },
  { id: 'support', label: '지원정보' },
  { id: 'allowance', label: '용돈·미션' },
]

export const lifecycleStages: LifecycleStage[] = [
  {
    id: 'prenatal',
    ageRange: '임신 중~출산 전',
    title: '미래 준비',
    description: '출산 전에 아이의 첫 금융 준비를 시작해보세요.',
  },
  {
    id: 'baby',
    ageRange: '출생~1세',
    title: '첫 금융 시작',
    description: '아이 이름으로 시작하는 금융생활의 기초를 준비해요.',
  },
  {
    id: 'toddler',
    ageRange: '2~4세',
    title: '자산 기반 형성',
    description: '목표와 저축 습관의 씨앗을 만들어보세요.',
  },
  {
    id: 'preschool',
    ageRange: '5~7세',
    title: '금융 습관 형성',
    description: '소비와 저축의 차이를 함께 배워볼 차례예요.',
  },
  {
    id: 'childhood',
    ageRange: '8~10세',
    title: '금융 이해 확장',
    description: '돈의 흐름과 계획을 아이 눈높이에 맞춰 알려줘요.',
  },
  {
    id: 'earlyTeen',
    ageRange: '11~13세',
    title: '금융 경험 시작',
    description: '용돈과 미션으로 직접 관리하는 경험을 시작해요.',
  },
  {
    id: 'teen',
    ageRange: '14~16세',
    title: '자산 성장',
    description: '장기 목표와 금융상품을 스스로 이해하도록 도와요.',
  },
  {
    id: 'future',
    ageRange: '17~19세',
    title: '미래 자산 완성',
    description: '독립 전 필요한 자산 준비를 마무리해요.',
  },
]

export const currentChildLifecycle = {
  childName: '깨비',
  childStatus: 'prenatal',
}

const categoryInfoTitle: Record<LifecycleCategory, string> = {
  service: '서비스에서 바로 이어서 해볼 수 있어요.',
  asset: '아이 자산 흐름을 점검해보세요.',
  education: '부모와 아이가 함께 이해하면 좋아요.',
  support: '시기별 지원 정보를 먼저 확인해보세요.',
  allowance: '용돈과 미션은 분리해서 관리하는 것을 추천해요.',
}

const categoryNotice: Record<LifecycleCategory, string> = {
  service: '실제 화면과 연결되기 전까지는 체크리스트 안내용 더미 데이터입니다.',
  asset: '금액과 기준은 가정용 예시이며, API 연동 후 실제 데이터로 교체합니다.',
  education: '교육 콘텐츠는 이후 상세 콘텐츠 또는 외부 안내 화면과 연결할 수 있습니다.',
  support: '지원 대상, 금액, 신청 기간은 시기와 지역에 따라 달라질 수 있어요.',
  allowance: '기본 용돈은 정기 지급, 미션은 추가 보상으로 분리하는 것을 추천합니다.',
}

const infoItemsByCategory: Record<LifecycleCategory, ChecklistInfoItem[]> = {
  service: [
    {
      title: '연결 화면 확인',
      description: '관련 기능 화면으로 이동해 현재 준비 상태를 확인해요.',
      actionLabel: '화면 보기',
    },
    {
      title: '다음 행동 정하기',
      description: '목표 만들기, 계좌 연결, 기록 남기기 중 필요한 행동을 골라요.',
      actionLabel: '준비하기',
    },
  ],
  asset: [
    {
      title: '현재 금액 확인',
      description: '계좌 잔액, 저축 금액, 이번 달 사용 금액을 먼저 확인해요.',
      actionLabel: '자산 점검하기',
    },
    {
      title: '목표와 비교',
      description: '계획한 저축액과 실제 모인 금액을 비교해요.',
      actionLabel: '목표 확인하기',
    },
  ],
  education: [
    {
      title: '필요 서류와 준비물 확인',
      description: '아이 명의 계좌 개설, 증여 기록처럼 부모가 먼저 챙겨야 할 준비물을 정리해요.',
      actionLabel: '준비물 확인하기',
    },
    {
      title: '아이에게 설명할 금융 개념 정리',
      description: '저축, 소비, 이자, 계좌처럼 아이가 나중에 이해할 개념을 쉬운 말로 정리해요.',
      actionLabel: '개념 살펴보기',
      externalUrl: 'https://www.fss.or.kr/edu',
    },
    {
      title: '기록이 필요한 순간 확인',
      description: '축하금, 세뱃돈, 증여금처럼 나중에 근거가 필요할 수 있는 기록 기준을 확인해요.',
      actionLabel: '기록 기준 보기',
      detail: '아이 명의로 받은 돈은 언제, 누구에게, 어떤 목적으로 받았는지 메모해두면 나중에 자금 출처를 설명할 때 도움이 됩니다.',
    },
  ],
  support: [
    {
      title: '정부 지원 확인',
      description: '출산, 양육, 돌봄, 교육과 관련된 공통 지원을 확인해요.',
      actionLabel: '지원 확인하기',
      externalUrl: 'https://www.bokjiro.go.kr/ssis-tbu/index.do',
    },
    {
      title: '지역 지원 확인',
      description: '거주 지역별로 받을 수 있는 지원이 있는지 살펴봐요.',
      actionLabel: '지역별 보기',
      externalUrl: 'https://www.gov.kr/portal/main',
    },
  ],
  allowance: [
    {
      title: '기본 용돈 정하기',
      description: '아이가 예측할 수 있도록 정기 지급 금액과 주기를 정해요.',
      actionLabel: '용돈 계획하기',
    },
    {
      title: '미션 보상 분리',
      description: '기본 용돈과 별도로 추가 보상 미션을 설계해요.',
      actionLabel: '미션 보기',
    },
  ],
}

const createInfoChecklist = (
  item: Omit<
    ChecklistItem,
    'actionType' | 'infoTitle' | 'infoDescription' | 'infoItems' | 'infoNotice'
  > &
    Partial<Pick<ChecklistItem, 'infoTitle' | 'infoDescription' | 'infoItems' | 'infoNotice'>>,
): ChecklistItem => ({
  ...item,
  actionType: 'info',
  infoTitle: item.infoTitle ?? item.title,
  infoDescription: item.infoDescription ?? categoryInfoTitle[item.category],
  infoItems: item.infoItems ?? infoItemsByCategory[item.category],
  infoNotice: item.infoNotice ?? categoryNotice[item.category],
})

const createRouteChecklist = (
  item: Omit<ChecklistItem, 'actionType'> & { route: string },
): ChecklistItem => ({
  ...item,
  actionType: 'route',
})

export const checklistItems: ChecklistItem[] = [
  createInfoChecklist({
    id: 'prenatal-support-after-birth',
    stageId: 'prenatal',
    category: 'support',
    title: '출산 후 받을 수 있는 지원제도 확인하기',
    description: '출산·육아 관련 지원제도를 미리 확인해요.',
    completed: false,
    infoItems: [
      {
        title: '첫만남 이용권',
        description: '출생 초기 양육 부담을 덜기 위한 지원을 확인해요.',
        actionLabel: '대상·조건 확인하기',
        externalUrl: 'https://www.bokjiro.go.kr/ssis-tbu/index.do',
      },
      {
        title: '부모급여',
        description: '영아기 양육 가정을 위한 지원 조건을 확인해요.',
        actionLabel: '대상·조건 확인하기',
        externalUrl: 'https://www.bokjiro.go.kr/ssis-tbu/index.do',
      },
      {
        title: '아동수당',
        description: '아동 양육과 관련된 기본 지원 내용을 확인해요.',
        actionLabel: '대상·조건 확인하기',
        externalUrl: 'https://www.bokjiro.go.kr/ssis-tbu/index.do',
      },
      {
        title: '거주 지역 출산지원',
        description: '지자체별 출산·양육 지원이 있는지 확인해요.',
        actionLabel: '지역별 지원 확인하기',
        externalUrl: 'https://www.gov.kr/portal/main',
      },
    ],
  }),
  createRouteChecklist({
    id: 'prenatal-childcare-cost',
    stageId: 'prenatal',
    category: 'asset',
    title: '출산 후 예상 양육비 계산해보기',
    description: '출산 후 필요한 월 양육비를 미리 가늠해봐요.',
    completed: false,
    route: '/reports',
  }),
  createRouteChecklist({
    id: 'prenatal-future-goal',
    stageId: 'prenatal',
    category: 'service',
    title: '우리 아이 미래자산 목표 정하기',
    description: '대학·독립 등 장기 목표를 미리 정해봐요.',
    completed: true,
    route: '/goals',
  }),
  createRouteChecklist({
    id: 'prenatal-monthly-saving',
    stageId: 'prenatal',
    category: 'asset',
    title: '매달 아이를 위해 저축할 금액 정하기',
    description: '꾸준히 이어갈 수 있는 월 저축액을 정해요.',
    completed: false,
    route: '/goals',
  }),
  createInfoChecklist({
    id: 'prenatal-account-guide',
    stageId: 'prenatal',
    category: 'education',
    title: '아이 명의 계좌 개설 방법 알아보기',
    description: '필요 서류와 계좌 개설 흐름을 확인해요.',
    completed: false,
    infoItems: [
      {
        title: '보호자 신분증',
        description: '법정대리인 확인을 위해 보호자 신분증이 필요해요.',
        actionLabel: '준비물 확인하기',
        detail: '은행 방문 전 보호자 신분증과 아이 기준 서류가 필요한지 먼저 확인해두면 다시 방문하는 일을 줄일 수 있습니다.',
      },
      {
        title: '가족관계 확인 서류',
        description: '아이와 보호자 관계를 확인할 수 있는 서류를 준비해요.',
        actionLabel: '서류 확인하기',
        externalUrl: 'https://efamily.scourt.go.kr/',
      },
      {
        title: '아이 기본증명서',
        description: '금융기관 요청에 따라 기본증명서가 필요할 수 있어요.',
        actionLabel: '발급 방법 보기',
        externalUrl: 'https://efamily.scourt.go.kr/',
      },
    ],
  }),
  createInfoChecklist({
    id: 'prenatal-gift-record',
    stageId: 'prenatal',
    category: 'education',
    title: '자녀에게 재산을 증여할 때 기록해야 할 내용 알아보기',
    description: '축하금·세뱃돈·증여금 기록 기준을 미리 확인해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'prenatal-first-account',
    stageId: 'prenatal',
    category: 'service',
    title: '아이 첫 계좌 연결 준비하기',
    description: '자산 관리를 시작할 계좌 연결 흐름을 확인해요.',
    completed: false,
    route: '/accounts',
  }),
  createRouteChecklist({
    id: 'prenatal-time-capsule',
    stageId: 'prenatal',
    category: 'service',
    title: '출산 전 첫 타임캡슐 만들기',
    description: '아이에게 전할 첫 기록을 미리 남겨요.',
    completed: true,
    route: '/time-capsules/new',
  }),
  createRouteChecklist({
    id: 'prenatal-kb-saving',
    stageId: 'prenatal',
    category: 'asset',
    title: '아이에게 맞는 KB 적금 후보 확인하기',
    description: '목표와 기간에 맞는 대표 상품을 살펴봐요.',
    completed: false,
    route: '/products',
  }),

  createInfoChecklist({
    id: 'baby-child-profile',
    stageId: 'baby',
    category: 'service',
    title: '자녀 프로필 등록하기',
    description: '아이 이름과 성장 단계를 서비스에 등록해요.',
    completed: true,
  }),
  createInfoChecklist({
    id: 'baby-account-prepare',
    stageId: 'baby',
    category: 'education',
    title: '아이 명의 계좌 개설 준비하기',
    description: '은행 방문 전 필요한 서류와 순서를 확인해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'baby-connect-account',
    stageId: 'baby',
    category: 'service',
    title: '아이 계좌를 서비스에 연결하기',
    description: '연결된 계좌로 아이 자산을 한눈에 확인해요.',
    completed: true,
    route: '/accounts',
  }),
  createInfoChecklist({
    id: 'baby-main-account',
    stageId: 'baby',
    category: 'service',
    title: '아이의 대표 입출금계좌 정하기',
    description: '용돈과 지원금이 들어올 대표 계좌를 정해요.',
    completed: false,
  }),
  createInfoChecklist({
    id: 'baby-support-money',
    stageId: 'baby',
    category: 'support',
    title: '출산·양육 관련 지원금 확인하기',
    description: '입금 예정 지원금과 신청 상태를 확인해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'baby-first-goal',
    stageId: 'baby',
    category: 'service',
    title: '아이의 첫 미래자산 목표 만들기',
    description: '작은 목표부터 장기 목표까지 설정해요.',
    completed: false,
    route: '/goals',
  }),
  createRouteChecklist({
    id: 'baby-first-saving',
    stageId: 'baby',
    category: 'asset',
    title: '첫 10만 원 저축 시작하기',
    description: '첫 저축 기록을 목표 진행률에 반영해요.',
    completed: false,
    route: '/goals',
  }),
  createRouteChecklist({
    id: 'baby-memory-capsule',
    stageId: 'baby',
    category: 'service',
    title: '첫 기념 저축과 타임캡슐 남기기',
    description: '저축한 날의 사진과 편지를 함께 남겨요.',
    completed: false,
    route: '/time-capsules/new',
  }),

  createRouteChecklist({
    id: 'toddler-total-asset',
    stageId: 'toddler',
    category: 'asset',
    title: '현재 아이의 총자산 확인하기',
    description: '계좌와 목표를 합쳐 현재 자산을 확인해요.',
    completed: true,
    route: '/assets',
  }),
  createRouteChecklist({
    id: 'toddler-asset-growth',
    stageId: 'toddler',
    category: 'asset',
    title: '지난달보다 자산이 얼마나 늘었는지 확인하기',
    description: '월별 자산 변화를 리포트에서 확인해요.',
    completed: false,
    route: '/reports',
  }),
  createRouteChecklist({
    id: 'toddler-saving-plan',
    stageId: 'toddler',
    category: 'asset',
    title: '매월 저축 계획이 유지되고 있는지 확인하기',
    description: '계획한 저축액이 꾸준히 들어가고 있는지 봐요.',
    completed: false,
    route: '/goals',
  }),
  createInfoChecklist({
    id: 'toddler-pocket-money',
    stageId: 'toddler',
    category: 'asset',
    title: '받은 용돈·세뱃돈·축하금 관리하기',
    description: '아이에게 들어온 돈의 출처와 사용 목적을 기록해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'toddler-goal-rate',
    stageId: 'toddler',
    category: 'service',
    title: '미래자산 목표 달성률 확인하기',
    description: '목표 금액 대비 진행률을 점검해요.',
    completed: true,
    route: '/goals',
  }),
  createInfoChecklist({
    id: 'toddler-child-saving-difference',
    stageId: 'toddler',
    category: 'education',
    title: '자녀 명의 저축과 부모 명의 저축의 차이 알아보기',
    description: '명의에 따라 관리 방식과 기록 기준이 달라질 수 있어요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'toddler-checkpoint-capsule',
    stageId: 'toddler',
    category: 'service',
    title: '목표 체크포인트 기념 타임캡슐 만들기',
    description: '자산이 늘어난 순간을 사진과 편지로 남겨요.',
    completed: false,
    route: '/time-capsules/new',
  }),

  createInfoChecklist({
    id: 'preschool-want-need',
    stageId: 'preschool',
    category: 'education',
    title: '사고 싶은 것과 필요한 것 구분해보기',
    description: '아이와 소비 우선순위를 쉬운 말로 이야기해요.',
    completed: true,
  }),
  createInfoChecklist({
    id: 'preschool-price-check',
    stageId: 'preschool',
    category: 'education',
    title: '갖고 싶은 물건의 가격 알아보기',
    description: '가격표를 보며 돈의 크기를 이해해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'preschool-first-saving-goal',
    stageId: 'preschool',
    category: 'asset',
    title: '아이와 함께 첫 저축 목표 만들기',
    description: '작은 장난감이나 책을 목표로 정해봐요.',
    completed: false,
    route: '/goals',
  }),
  createInfoChecklist({
    id: 'preschool-save-part',
    stageId: 'preschool',
    category: 'education',
    title: '받은 돈 중 일부를 저축해보기',
    description: '쓰는 돈과 모으는 돈을 나눠보는 경험을 해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'preschool-first-allowance',
    stageId: 'preschool',
    category: 'allowance',
    title: '아이의 첫 용돈 금액 정하기',
    description: '아이에게 이해하기 쉬운 금액과 주기를 정해요.',
    completed: true,
    route: '/child/allowance',
  }),
  createInfoChecklist({
    id: 'preschool-small-mission',
    stageId: 'preschool',
    category: 'allowance',
    title: '작은 생활 미션을 완료해보기',
    description: '정리하기, 준비물 챙기기 같은 생활 미션을 시작해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'preschool-first-capsule',
    stageId: 'preschool',
    category: 'service',
    title: '아이의 첫 금융 경험을 타임캡슐로 남기기',
    description: '처음 돈을 모은 순간을 기록해요.',
    completed: false,
    route: '/time-capsules/new',
  }),

  createInfoChecklist({
    id: 'childhood-connect-child',
    stageId: 'childhood',
    category: 'service',
    title: '내 계정을 부모님과 연결하기',
    description: '아이 화면에서 용돈과 기록을 직접 확인할 수 있게 해요.',
    completed: true,
  }),
  createRouteChecklist({
    id: 'childhood-allowance-plan',
    stageId: 'childhood',
    category: 'allowance',
    title: '이번 주에 필요한 용돈 계획하기',
    description: '이번 주에 쓸 돈과 모을 돈을 나눠봐요.',
    completed: false,
    route: '/child/allowance',
  }),
  createRouteChecklist({
    id: 'childhood-request-allowance',
    stageId: 'childhood',
    category: 'allowance',
    title: '부모님에게 용돈 요청 보내기',
    description: '필요한 금액과 이유를 적어서 요청해요.',
    completed: false,
    route: '/child/allowance',
  }),
  createRouteChecklist({
    id: 'childhood-saving-from-allowance',
    stageId: 'childhood',
    category: 'asset',
    title: '받은 용돈 중 저축할 금액 정하기',
    description: '용돈 일부를 목표 저축으로 옮겨요.',
    completed: true,
    route: '/goals',
  }),
  createRouteChecklist({
    id: 'childhood-wish-goal',
    stageId: 'childhood',
    category: 'asset',
    title: '갖고 싶은 물건을 저축 목표로 만들기',
    description: '원하는 물건을 목표로 설정하고 진행률을 봐요.',
    completed: false,
    route: '/goals',
  }),
  createRouteChecklist({
    id: 'childhood-spending-check',
    stageId: 'childhood',
    category: 'asset',
    title: '이번 주에 사용한 돈 확인하기',
    description: '최근 돈 기록에서 소비 내역을 확인해요.',
    completed: false,
    route: '/child/assets',
  }),
  createInfoChecklist({
    id: 'childhood-safe-money',
    stageId: 'childhood',
    category: 'education',
    title: '돈을 안전하게 사용하는 방법 알아보기',
    description: '비밀번호, 계좌정보, 낯선 링크를 조심하는 방법을 배워요.',
    completed: false,
  }),

  createRouteChecklist({
    id: 'early-teen-month-budget',
    stageId: 'earlyTeen',
    category: 'allowance',
    title: '이번 달 용돈 예산 세우기',
    description: '한 달 동안 쓸 돈과 모을 돈을 계획해요.',
    completed: true,
    route: '/child/allowance',
  }),
  createInfoChecklist({
    id: 'early-teen-split-spend-save',
    stageId: 'earlyTeen',
    category: 'allowance',
    title: '소비·저축 금액을 나누어 계획하기',
    description: '필요한 소비와 목표 저축을 구분해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'early-teen-transaction-weekly',
    stageId: 'earlyTeen',
    category: 'asset',
    title: '일주일에 한 번 거래내역 확인하기',
    description: '계획하지 않았던 소비가 있었는지 확인해요.',
    completed: false,
    route: '/child/assets',
  }),
  createRouteChecklist({
    id: 'early-teen-saving-goal',
    stageId: 'earlyTeen',
    category: 'asset',
    title: '이번 달 저축 목표 달성하기',
    description: '목표 달성률을 확인하고 부족한 금액을 계산해요.',
    completed: true,
    route: '/goals',
  }),
  createInfoChecklist({
    id: 'early-teen-interest',
    stageId: 'earlyTeen',
    category: 'education',
    title: '이자와 금리가 무엇인지 알아보기',
    description: '돈을 맡겼을 때 왜 이자가 생기는지 배워요.',
    completed: false,
  }),
  createInfoChecklist({
    id: 'early-teen-card-safe',
    stageId: 'earlyTeen',
    category: 'education',
    title: '체크카드 사용 시 주의사항 알아보기',
    description: '잔액, 결제 문자, 분실 신고 방법을 확인해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'early-teen-report',
    stageId: 'earlyTeen',
    category: 'service',
    title: '아이의 소비·저축 리포트 함께 보기',
    description: '월간 사용 금액과 저축 흐름을 같이 확인해요.',
    completed: false,
    route: '/reports',
  }),

  createRouteChecklist({
    id: 'teen-budget',
    stageId: 'teen',
    category: 'asset',
    title: '이번 달 예산 작성하기',
    description: '고정지출과 변동지출을 나눠 월 예산을 세워요.',
    completed: true,
    route: '/reports',
  }),
  createInfoChecklist({
    id: 'teen-fixed-variable',
    stageId: 'teen',
    category: 'education',
    title: '고정지출과 변동지출 구분하기',
    description: '매달 반복되는 지출과 선택 지출을 구분해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'teen-short-long-goal',
    stageId: 'teen',
    category: 'asset',
    title: '단기 목표와 장기 목표 구분하기',
    description: '이번 달 목표와 몇 년 뒤 목표를 따로 관리해요.',
    completed: false,
    route: '/goals',
  }),
  createRouteChecklist({
    id: 'teen-compare-spending',
    stageId: 'teen',
    category: 'asset',
    title: '계획과 실제 소비 비교하기',
    description: '이번 달 예산과 실제 사용 금액 차이를 확인해요.',
    completed: true,
    route: '/reports',
  }),
  createInfoChecklist({
    id: 'teen-emergency-fund',
    stageId: 'teen',
    category: 'asset',
    title: '비상금 목표 만들기',
    description: '예상하지 못한 상황에 대비하는 목표를 세워요.',
    completed: false,
  }),
  createInfoChecklist({
    id: 'teen-privacy',
    stageId: 'teen',
    category: 'education',
    title: '개인정보와 계좌정보 보호 방법 알아보기',
    description: '금융사기와 명의도용을 예방하는 습관을 배워요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'teen-capsule',
    stageId: 'teen',
    category: 'service',
    title: '목표 달성 과정 타임캡슐 남기기',
    description: '목표를 향해 가는 과정을 사진과 편지로 기록해요.',
    completed: false,
    route: '/time-capsules/new',
  }),

  createRouteChecklist({
    id: 'future-next-goal',
    stageId: 'future',
    category: 'asset',
    title: '대학·취업·독립 중 다음 목표 선택하기',
    description: '성인이 된 뒤 필요한 첫 목표를 선택해요.',
    completed: true,
    route: '/goals',
  }),
  createRouteChecklist({
    id: 'future-living-cost',
    stageId: 'future',
    category: 'asset',
    title: '독립 생활비 예상하기',
    description: '월세, 식비, 교통비 등 필요한 생활비를 계산해요.',
    completed: false,
    route: '/reports',
  }),
  createInfoChecklist({
    id: 'future-account-purpose',
    stageId: 'future',
    category: 'asset',
    title: '계좌별 사용 목적 구분하기',
    description: '생활비, 저축, 비상금 계좌의 역할을 나눠요.',
    completed: false,
  }),
  createInfoChecklist({
    id: 'future-subscription-check',
    stageId: 'future',
    category: 'asset',
    title: '정기결제와 구독 서비스 점검하기',
    description: '매달 빠져나가는 돈을 확인하고 필요 없는 지출을 줄여요.',
    completed: false,
  }),
  createInfoChecklist({
    id: 'future-credit',
    stageId: 'future',
    category: 'education',
    title: '신용점수와 부채의 기본 개념 알아보기',
    description: '신용을 지키는 습관과 빚의 의미를 배워요.',
    completed: true,
  }),
  createInfoChecklist({
    id: 'future-youth-support',
    stageId: 'future',
    category: 'support',
    title: '대학·취업·청년 관련 지원제도 확인하기',
    description: '장학금, 취업, 청년 자산형성 지원을 확인해요.',
    completed: false,
  }),
  createRouteChecklist({
    id: 'future-final-capsule',
    stageId: 'future',
    category: 'service',
    title: '성인이 되는 날 공개할 타임캡슐 확인하기',
    description: '오랫동안 모은 성장 기록을 함께 돌아봐요.',
    completed: false,
    route: '/time-capsules',
  }),
]
