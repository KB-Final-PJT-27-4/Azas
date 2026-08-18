import educationGoalIcon from '@/assets/images/goals/goals_1.png'
import productMascot1 from '@/assets/images/products/product-1.png'
import productMascot2 from '@/assets/images/products/product-2.png'
import productMascot3 from '@/assets/images/products/product-3.png'
import productMascot4 from '@/assets/images/products/product-4.png'

export type RecommendedProduct = {
  id: string
  name: string
  bankName: string
  type: '적금' | '입출금계좌'
  rate: string
  period: string
  monthlyLimit: string
  mascot: string
}

export const productRecommendationGoal = {
  title: '대학자금',
  icon: educationGoalIcon,
  targetAmount: 30000000,
  targetDate: '2045년 3월',
  monthlySavingAmount: 300000,
}

export const recommendedProducts: RecommendedProduct[] = [
  {
    id: 'kb-child-love-saving-1',
    name: 'KB 아이사랑적금',
    bankName: 'KB국민은행',
    type: '적금',
    rate: '3.80%',
    period: '12개월',
    monthlyLimit: '30만원',
    mascot: productMascot1,
  },
  {
    id: 'kb-child-happy-saving',
    name: 'KB 아이행복 적금',
    bankName: 'KB국민은행',
    type: '적금',
    rate: '3.50%',
    period: '12개월',
    monthlyLimit: '30만원',
    mascot: productMascot2,
  },
  {
    id: 'kb-child-grow-saving',
    name: 'KB 아이키움 적금',
    bankName: 'KB국민은행',
    type: '적금',
    rate: '3.40%',
    period: '12개월',
    monthlyLimit: '30만원',
    mascot: productMascot3,
  },
  {
    id: 'kb-child-dream-saving',
    name: 'KB 아이꿈 적금',
    bankName: 'KB국민은행',
    type: '적금',
    rate: '3.20%',
    period: '12개월',
    monthlyLimit: '30만원',
    mascot: productMascot4,
  },
  {
    id: 'kb-young-youth-account',
    name: 'KB Young Youth 어린이통장',
    bankName: 'KB국민은행',
    type: '입출금계좌',
    rate: '0.10%',
    period: '제한없음',
    monthlyLimit: '제한없음',
    mascot: productMascot1,
  },
  {
    id: 'kb-child-dream-account',
    name: 'KB 꿈나무통장',
    bankName: 'KB국민은행',
    type: '입출금계좌',
    rate: '0.10%',
    period: '제한없음',
    monthlyLimit: '제한없음',
    mascot: productMascot2,
  },
]
