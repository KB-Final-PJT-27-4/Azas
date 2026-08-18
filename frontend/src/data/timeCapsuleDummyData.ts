import memoryPhoto1 from '@/assets/images/timeCapsules/thumbnails/light.png'
import memoryPhoto2 from '@/assets/images/timeCapsules/thumbnails/v.png'

export type TimeCapsulePhoto = {
  src: string
  orientation: 'landscape' | 'portrait'
  type?: 'image' | 'video'
}

export type TimeCapsuleRecord = {
  id: number
  title: string
  date: string
  amount: number
  thumbnail: string
  letter: string
  photos: TimeCapsulePhoto[]
  transferName: string
  remainingEdits: number
  openDate?: string
}

export type TimeCapsuleAccount = {
  id: number
  name: string
  bankName: string
  accountNumber: string
  description: string
  records: TimeCapsuleRecord[]
}

const photosA: TimeCapsulePhoto[] = [
  { src: memoryPhoto1, orientation: 'portrait' },
  { src: memoryPhoto2, orientation: 'portrait' },
]

const photosB: TimeCapsulePhoto[] = [
  { src: memoryPhoto2, orientation: 'portrait' },
  { src: memoryPhoto1, orientation: 'portrait' },
]

const createRecord = (
  id: number,
  title: string,
  date: string,
  amount: number,
  letter: string,
  photos: TimeCapsulePhoto[],
  transferName = title,
): TimeCapsuleRecord => ({
  id,
  title,
  date,
  amount,
  letter,
  photos,
  thumbnail: photos[0]!.src,
  transferName,
  remainingEdits: 1,
})

export const timeCapsuleAccounts: Record<string, TimeCapsuleAccount> = {
  '1': {
    id: 1,
    name: '아이사랑적금',
    bankName: 'KB국민은행',
    accountNumber: '123-456-789',
    description: '깨비의 성장 순간과 금융 기록을 모아보세요.',
    records: [
      createRecord(101, '첫 생일 축하', '2026-05-04', 150000, '우리 깨비의 첫 생일을 진심으로 축하해!\n하루하루 건강하게 자라는 모습을 볼 수 있어서 정말 행복해.\n\n앞으로도 지금처럼 밝고 씩씩하게 자라렴. 언제나 사랑해!', photosA),
      createRecord(102, '가족 여행', '2026-05-09', 80000, '우리 가족이 함께한 소중한 여행을 기억해두려고 해.\n새로운 풍경을 보며 활짝 웃던 깨비의 얼굴이 아직도 눈앞에 선해.\n\n다음에도 다 같이 즐거운 추억을 많이 만들자.', photosB),
      createRecord(103, '첫 어린이집', '2026-05-23', 100000, '처음 어린이집에 가던 날, 씩씩하게 손을 흔들어줘서 정말 대견했어.\n새로운 친구들과 행복한 추억을 많이 만들길 바라.', photosA),
      createRecord(104, '첫 놀이터', '2026-05-30', 100000, '미끄럼틀을 타며 신나게 웃던 오늘을 오래 기억하고 싶어.\n깨비의 웃음 덕분에 엄마 아빠도 정말 행복했단다.', photosB),
      createRecord(105, '첫 걸음마', '2026-05-31', 150000, '작은 발로 한 걸음씩 다가오던 순간이 아직도 생생해.\n넘어져도 다시 일어나는 멋진 깨비가 되길 바라.', photosA),
      createRecord(106, '바닷가 나들이', '2026-06-07', 80000, '처음 만난 바다와 반짝이는 모래를 신기하게 바라보던 날이야.\n다음 여름에도 함께 바다를 보러 가자.', photosB),
      createRecord(107, '동물원에 간 날', '2026-06-11', 70000, '좋아하는 동물을 보며 눈을 반짝이던 깨비가 정말 귀여웠어.\n오늘의 호기심을 오래 간직하길 바라.', photosA),
      createRecord(108, '즐거운 물놀이', '2026-06-14', 50000, '물장구를 치며 깔깔 웃던 즐거운 하루였어.\n언제나 지금처럼 밝고 건강하게 자라렴.', photosB),
      createRecord(109, '할머니와의 하루', '2026-06-28', 90000, '할머니 손을 꼭 잡고 산책하던 따뜻한 오후였어.\n가족의 사랑을 듬뿍 받으며 자라길 바라.', photosA),
      createRecord(110, '크리스마스 선물', '2025-12-25', 100000, '깨비와 함께한 첫 크리스마스야.\n반짝이는 트리처럼 설레고 행복한 날들이 계속되길 바라.', photosB),
      createRecord(111, '새해 첫 저축', '2027-01-02', 120000, '새해에도 깨비의 꿈을 위해 차곡차곡 저축할게.\n하고 싶은 일을 마음껏 펼칠 수 있도록 늘 응원하마.', photosA),
    ],
  },
  '2': {
    id: 2,
    name: '우리사랑적금',
    bankName: '우리은행',
    accountNumber: '1002-345-678901',
    description: '우리 가족의 행복한 추억과 저축 기록이에요.',
    records: [
      createRecord(201, '가족 캠핑', '2026-05-03', 120000, '별이 가득한 하늘 아래에서 우리 가족이 함께한 첫 캠핑이야.\n오늘의 웃음과 이야기를 오래 기억하자.', photosB),
      createRecord(202, '엄마와 쿠키 만들기', '2026-05-17', 50000, '조그만 손으로 반죽을 꾹꾹 누르던 모습이 정말 사랑스러웠어.\n다음에는 더 맛있는 쿠키를 함께 만들자.', photosA),
      createRecord(203, '아빠와 자전거', '2026-05-24', 70000, '아빠와 처음 자전거를 타던 신나는 날이야.\n바람을 가르며 웃던 깨비의 모습을 오래 간직할게.', photosB),
      createRecord(204, '우리 가족 사진', '2026-06-06', 100000, '우리 가족의 환한 미소를 사진에 담았어.\n시간이 흘러도 서로 아끼고 사랑하는 가족이 되자.', photosA),
      createRecord(205, '첫 수영 수업', '2026-06-20', 60000, '처음에는 조금 무서워했지만 곧 씩씩하게 물속으로 들어갔지.\n새로운 도전을 즐기는 깨비가 자랑스러워.', photosB),
      createRecord(206, '겨울 가족 여행', '2025-12-14', 110000, '하얀 눈을 밟으며 온 가족이 함께 웃었던 겨울 여행이야.\n따뜻한 추억을 오래오래 간직하자.', photosA),
      createRecord(207, '새해 가족 저축', '2027-01-10', 150000, '새해에도 우리 가족의 꿈을 위해 함께 저축을 시작했어.\n모두 건강하고 행복한 한 해가 되길 바라.', photosB),
    ],
  },
  '3': {
    id: 3,
    name: '깨비 자유통장',
    bankName: 'KB국민은행',
    accountNumber: '987-654-321',
    description: '특별한 날을 자유롭게 골라 추억을 담는 캡슐이에요.',
    records: [],
  },
}

export const getTimeCapsuleAccount = (accountId: string) =>
  timeCapsuleAccounts[accountId] ?? timeCapsuleAccounts['1']!

export const getTimeCapsuleRecord = (accountId: string, recordId: string) => {
  const account = getTimeCapsuleAccount(accountId)
  return account.records.find(({ id }) => String(id) === recordId) ?? account.records[0]!
}

export const findTimeCapsuleRecord = (recordId: string) => {
  for (const account of Object.values(timeCapsuleAccounts)) {
    const record = account.records.find(({ id }) => String(id) === recordId)
    if (record) return { account, record }
  }

  const account = timeCapsuleAccounts['1']!
  return { account, record: account.records[0]! }
}
