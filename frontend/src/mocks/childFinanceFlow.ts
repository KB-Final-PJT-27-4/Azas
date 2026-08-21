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
    question: '새 장난감이 사고 싶지만 다음 주 준비물도 사야 해요. 먼저 할 일은 무엇일까요?',
    options: [
      '장난감을 바로 사요',
      '준비물과 장난감 중 무엇이 더 필요한지 비교해요',
      '친구가 사는 것을 따라 사요',
      '가격표를 보지 않고 골라요',
    ],
    answerIndex: 1,
    explanation: '돈을 쓰기 전에는 꼭 필요한 것과 갖고 싶은 것을 먼저 비교하면 좋아요.',
  },
  {
    id: 'quiz-2',
    question: '용돈기입장에 적으면 좋은 내용은 무엇일까요?',
    options: [
      '오늘 날씨와 기분만 적어요',
      '게임 점수와 친구 이름만 적어요',
      '받은 돈, 쓴 돈, 이유, 남은 돈을 적어요',
      '아무것도 적지 않아요',
    ],
    answerIndex: 2,
    explanation: '받은 돈과 쓴 돈, 이유, 남은 돈을 적으면 내 돈이 어떻게 움직였는지 알 수 있어요.',
  },
  {
    id: 'quiz-3',
    question: '갖고 싶은 물건이 너무 비쌀 때 가장 좋은 방법은 무엇일까요?',
    options: [
      '부족한 돈을 몰래 빌려요',
      '바로 포기하고 잊어요',
      '목표 금액을 정하고 조금씩 모아요',
      '가격을 확인하지 않아요',
    ],
    answerIndex: 2,
    explanation: '목표 금액을 정하면 얼마를 더 모아야 하는지 알고 계획을 세울 수 있어요.',
  },
  {
    id: 'quiz-4',
    question: '거래내역을 확인하면 어떤 점이 좋을까요?',
    options: [
      '내 돈이 어디에 쓰였는지 알 수 있어요',
      '돈을 더 빨리 쓰게 돼요',
      '기록을 모두 지울 수 있어요',
      '잔액을 몰라도 돼요',
    ],
    answerIndex: 0,
    explanation: '거래내역을 보면 내가 돈을 어디에 썼는지 알고 다음 계획을 세울 수 있어요.',
  },
  {
    id: 'quiz-5',
    question: '다음 중 먼저 사야 할 가능성이 가장 큰 것은 무엇일까요?',
    options: [
      '이미 집에 많은 스티커',
      '광고에서 본 장난감',
      '학교에서 내일 필요한 공책',
      '친구가 산 새 키링',
    ],
    answerIndex: 2,
    explanation: '학교에서 꼭 필요한 공책은 갖고 싶은 물건보다 먼저 준비하는 것이 좋아요.',
  },
  {
    id: 'quiz-6',
    question: '이번 달 용돈 한도를 정해두면 어떤 점이 좋을까요?',
    options: [
      '돈을 계획 없이 쓸 수 있어요',
      '얼마나 쓸 수 있는지 알고 조심할 수 있어요',
      '기록을 하지 않아도 돼요',
      '갖고 싶은 것을 모두 살 수 있어요',
    ],
    answerIndex: 1,
    explanation: '한도를 정하면 쓸 수 있는 돈을 알고 계획적으로 사용할 수 있어요.',
  },
  {
    id: 'quiz-7',
    question: '모르는 사람이 비밀번호나 계좌 정보를 물어보면 어떻게 해야 할까요?',
    options: [
      '절대 알려주지 않고 부모님께 말해요',
      '친구에게 먼저 알려줘요',
      '메시지로 보내줘요',
      '선물을 준다고 하면 알려줘요',
    ],
    answerIndex: 0,
    explanation: '비밀번호와 계좌 정보는 중요한 금융 정보라서 다른 사람에게 알려주면 안 돼요.',
  },
  {
    id: 'quiz-8',
    question: '미션 보상은 어떻게 생각하는 것이 좋을까요?',
    options: [
      '아무것도 하지 않아도 받는 돈이에요',
      '가족과 약속한 일을 해낸 뒤 받는 보상이에요',
      '친구가 대신 해도 되는 돈이에요',
      '무조건 바로 써야 하는 돈이에요',
    ],
    answerIndex: 1,
    explanation: '미션 보상은 약속한 일을 해내고 확인받았을 때 받는 돈이에요.',
  },
  {
    id: 'quiz-9',
    question: '10,000원을 받았고 4,000원을 썼어요. 용돈기입장에 남은 돈은 얼마라고 적을까요?',
    options: [
      '4,000원',
      '5,000원',
      '6,000원',
      '10,000원',
    ],
    answerIndex: 2,
    explanation: '10,000원에서 4,000원을 쓰면 6,000원이 남아요.',
  },
  {
    id: 'quiz-10',
    question: '문자나 링크로 “공짜 선물”을 준다며 누르라고 하면 어떻게 해야 할까요?',
    options: [
      '바로 눌러서 확인해요',
      '친구에게 먼저 보내요',
      '부모님께 보여주고 함부로 누르지 않아요',
      '계좌 정보를 입력해요',
    ],
    answerIndex: 2,
    explanation: '낯선 링크는 위험할 수 있어요. 부모님께 먼저 보여주고 확인하는 습관이 필요해요.',
  },
]
