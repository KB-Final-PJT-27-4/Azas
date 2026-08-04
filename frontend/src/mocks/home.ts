export type HomeMemberType = 'new' | 'existing'

export interface HomeQuickMenu {
  title: string
  subtitle: string
  icon: 'checklist' | 'timeCapsule' | 'goal'
  to: string
}

export interface HomeProduct {
  id: string
  name: string
  type: string
  rate: string
  period: string
  to: string
}

export interface HomeGoal {
  id: string
  title: string
  tag: string
  currentAmount: number
  targetAmount: number
  progress: number
  targetDate: string
  checklistStatus: string
  timeCapsuleStatus: string
}

export interface HomeData {
  memberType: HomeMemberType
  childName: string
  heroTitle: string
  heroDescription: string
  checklistStatus: string
  timeCapsuleStatus: string
  goalStatus: string
  goalCtaTo: string
  productsMoreTo: string
  quickMenus: HomeQuickMenu[]
  products: HomeProduct[]
  goals?: HomeGoal[]
}

export const currentHomeMemberType: HomeMemberType = 'existing'

export const homeDataByMemberType: Record<HomeMemberType, HomeData> = {
  new: {
    memberType: 'new',
    childName: '깨비',
    heroTitle: '깨비의 밝은 미래를 함께 준비해요!',
    heroDescription: '오늘도 한 걸음 더 성장하고 있어요.',
    checklistStatus: '확인하기',
    timeCapsuleStatus: '만들러가기',
    goalStatus: '설정하기',
    goalCtaTo: '/goals',
    productsMoreTo: '/products',
    quickMenus: [
      { title: '체크리스트', subtitle: '확인하기', icon: 'checklist', to: '/checklists' },
      { title: '타임캡슐', subtitle: '만들러가기', icon: 'timeCapsule', to: '/time-capsules' },
      { title: '목표', subtitle: '설정하기', icon: 'goal', to: '/goals' },
    ],
    products: [
      {
        id: 'kb-child-love-saving-1',
        name: 'KB아이사랑적금',
        type: '적금',
        rate: '최고 연 3.80%',
        period: '12개월',
        to: '/products/kb-child-love-saving-1',
      },
      {
        id: 'kb-child-love-saving-2',
        name: 'KB아이사랑적금',
        type: '적금',
        rate: '최고 연 3.80%',
        period: '12개월',
        to: '/products/kb-child-love-saving-2',
      },
    ],
  },
  existing: {
    memberType: 'existing',
    childName: '깨비',
    heroTitle: '깨비의 밝은 미래를 함께 준비해요!',
    heroDescription: '오늘도 한 걸음 더 성장하고 있어요.',
    checklistStatus: '3 / 6 완료',
    timeCapsuleStatus: 'D - 23',
    goalStatus: '9,600,000원',
    goalCtaTo: '/goals',
    productsMoreTo: '/products',
    quickMenus: [
      { title: '체크리스트', subtitle: '3 / 6 완료', icon: 'checklist', to: '/checklists' },
      { title: '타임캡슐', subtitle: 'D - 23', icon: 'timeCapsule', to: '/time-capsules' },
      { title: '목표', subtitle: '9,600,000원', icon: 'goal', to: '/goals' },
    ],
    goals: [
      {
        id: 'college-fund',
        title: '목표',
        tag: '대학자금',
        currentAmount: 9_600_000,
        targetAmount: 30_000_000,
        progress: 32,
        targetDate: '2034년 6월',
        checklistStatus: '3 / 6 완료',
        timeCapsuleStatus: 'D - 23',
      },
      {
        id: 'housing-fund',
        title: '목표',
        tag: '주거자금',
        currentAmount: 4_200_000,
        targetAmount: 80_000_000,
        progress: 5,
        targetDate: '2042년 12월',
        checklistStatus: '1 / 5 완료',
        timeCapsuleStatus: 'D - 68',
      },
    ],
    products: [
      {
        id: 'kb-child-love-saving-1',
        name: 'KB아이사랑적금',
        type: '적금',
        rate: '최고 연 3.80%',
        period: '12개월',
        to: '/products/kb-child-love-saving-1',
      },
      {
        id: 'kb-child-love-saving-2',
        name: 'KB아이사랑적금',
        type: '적금',
        rate: '최고 연 3.80%',
        period: '12개월',
        to: '/products/kb-child-love-saving-2',
      },
    ],
  },
}
