import { reactive } from 'vue'

export interface ChildTransaction {
  id: string
  title: string
  time: string
  amount: number
  type: 'income' | 'expense'
  icon: string
}

interface TransferRecordPayload {
  amount: number
  receiverName?: string
}

export const childAccountSummary = reactive({
  childName: '깨비',
  accountName: '깨비 돈',
  balance: 96_000,
  monthlySpent: 14_000,
  monthlyLimit: 20_000,
  usageProgress: 70,
})

export const childTransactions = reactive<ChildTransaction[]>([
  {
    id: 'allowance-from-mom',
    title: '엄마가 보내준 용돈',
    time: '오늘 오후 2:30',
    amount: 30_000,
    type: 'income',
    icon: '💰',
  },
  {
    id: 'stationery-1',
    title: '문구점',
    time: '어제 오후 4:12',
    amount: -4_000,
    type: 'expense',
    icon: '🛍️',
  },
  {
    id: 'icecream',
    title: '아이스크림 가게',
    time: '어제 오후 3:15',
    amount: -2_500,
    type: 'expense',
    icon: '🍦',
  },
  {
    id: 'convenience',
    title: '편의점',
    time: '어제 오후 1:20',
    amount: -2_000,
    type: 'expense',
    icon: '🧃',
  },
])

export const transferDefaults = {
  get balance() {
    return childAccountSummary.balance
  },
  contacts: [
    { id: 'mom', name: '엄마', bankName: '국민은행', accountNumber: '123-456-789' },
    { id: 'friend', name: '민준', bankName: '국민은행', accountNumber: '987-654-321' },
  ],
  quickAmounts: [1_000, 5_000, 10_000],
}

export const allowanceOptions = [5_000, 10_000, 20_000]

export const recordChildTransfer = ({ amount, receiverName = '국민은행' }: TransferRecordPayload) => {
  const transferAmount = Math.abs(amount)

  childAccountSummary.balance -= transferAmount
  childAccountSummary.monthlySpent += transferAmount
  childAccountSummary.usageProgress = Math.min(
    100,
    Math.round((childAccountSummary.monthlySpent / childAccountSummary.monthlyLimit) * 100),
  )

  childTransactions.unshift({
    id: `transfer-${Date.now()}`,
    title: `${receiverName} 이체`,
    time: '방금 전',
    amount: -transferAmount,
    type: 'expense',
    icon: '🎒',
  })
}
