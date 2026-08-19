import { reactive } from 'vue'

export type AllowanceRequestStatus = 'pending' | 'approved' | 'rejected'

export const childAllowanceRequests = reactive([
  {
    id: 'request-1',
    amount: 10_000,
    purpose: '친구 생일 선물',
    requestedAt: '요청일 7/15 10:30',
    status: 'pending' as AllowanceRequestStatus,
  },
  {
    id: 'request-2',
    amount: 5_000,
    purpose: '학용품 구매',
    requestedAt: '요청일 7/10 15:20',
    status: 'approved' as AllowanceRequestStatus,
  },
  {
    id: 'request-3',
    amount: 8_000,
    purpose: '책 구매',
    requestedAt: '요청일 7/5 09:10',
    status: 'rejected' as AllowanceRequestStatus,
  },
])

export const childMissions = reactive([
  {
    id: 'mission-1',
    title: '용돈기입장 작성하기',
    description: '이번 주 용돈기입장 쓰기',
    reward: 1_000,
    status: 'completed',
  },
  {
    id: 'mission-2',
    title: '심부름하기',
    description: '집 청소기 돌리기',
    reward: 500,
    status: 'progress',
  },
  {
    id: 'mission-3',
    title: '소비 계획 지키기',
    description: '이번 주 계획한 소비 지키기',
    reward: 2_000,
    status: 'review',
  },
])

export const childQuizQuestions = [
  {
    id: 'quiz-1',
    question: '돈을 모으는 가장 좋은 방법은 무엇일까요?',
    options: [
      '필요와 불필요를 나누어 산다',
      '가지고 싶은 건 바로 산다',
      '돈을 주면 다 쓴다',
      '친구가 사면 나도 산다',
    ],
    answerIndex: 0,
    explanation: '필요한 물건을 사고 남은 돈을 저축하면 돈을 모을 수 있어요!',
  },
  {
    id: 'quiz-2',
    question: '용돈기입장에 적으면 좋은 내용은 무엇일까요?',
    options: ['쓴 돈과 받은 돈', '날씨', '게임 점수', '친구 이름만'],
    answerIndex: 0,
    explanation: '돈이 어디서 들어오고 어디에 쓰였는지 적으면 소비 습관을 알 수 있어요.',
  },
  {
    id: 'quiz-3',
    question: '갖고 싶은 물건이 생겼을 때 먼저 하면 좋은 일은?',
    options: ['목표 금액을 정한다', '바로 결제한다', '친구에게 빌린다', '모른 척한다'],
    answerIndex: 0,
    explanation: '목표 금액을 정하면 얼마를 모아야 하는지 알 수 있어요.',
  },
  {
    id: 'quiz-4',
    question: '월 한도는 왜 필요할까요?',
    options: [
      '돈을 계획적으로 쓰려고',
      '돈을 더 빨리 쓰려고',
      '알림을 없애려고',
      '기록을 안 하려고',
    ],
    answerIndex: 0,
    explanation: '한도는 내가 쓸 수 있는 돈을 지키도록 도와줘요.',
  },
  {
    id: 'quiz-5',
    question: '미션 보상은 언제 받을 수 있을까요?',
    options: [
      '부모님이 확인하고 승인하면',
      '미션을 안 해도',
      '버튼만 누르면 바로',
      '친구가 대신하면',
    ],
    answerIndex: 0,
    explanation: '미션을 완료하고 부모님이 확인하면 보상 용돈을 받을 수 있어요.',
  },
]
