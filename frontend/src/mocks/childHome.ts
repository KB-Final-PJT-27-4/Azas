import { reactive } from 'vue'

export interface ChildTransaction {
  id: string
  title: string
  time: string
  amount: number
  type: 'income' | 'expense'
}

interface TransferRecordPayload {
  amount: number
  bankName: string
}

export const childAccountSummary = reactive({
  childName: '깨비',
  accountName: '깨비 돈',
  balance: 96_000,
  monthlySpent: 14_000,
  dailyLimit: 20_000,
  usageProgress: 70,
})

export const childTransactions = reactive<ChildTransaction[]>([
  {
    id: 'allowance-from-mom',
    title: '엄마가 보내준 용돈',
    time: '오늘 오후 2:30',
    amount: 30_000,
    type: 'income',
  },
  {
    id: 'stationery-1',
    title: '문구점',
    time: '어제 오후 4:12',
    amount: -4_000,
    type: 'expense',
  },
  {
    id: 'stationery-2',
    title: '문구점',
    time: '어제 오후 4:12',
    amount: -4_000,
    type: 'expense',
  },
  {
    id: 'snack-store',
    title: '분식집',
    time: '그제 오후 3:20',
    amount: -3_500,
    type: 'expense',
  },
  {
    id: 'bookstore',
    title: '서점',
    time: '지난주 금요일',
    amount: -8_500,
    type: 'expense',
  },
])

export const transferDefaults = {
  bankName: '국민은행',
  get balance() {
    return childAccountSummary.balance
  },
  quickAmounts: [5_000, 10_000, 20_000],
}

export const allowanceOptions = [5_000, 10_000, 20_000]

export const recordChildTransfer = ({ amount, bankName }: TransferRecordPayload) => {
  const transferAmount = Math.abs(amount)

  childAccountSummary.balance -= transferAmount
  childAccountSummary.monthlySpent += transferAmount
  childAccountSummary.usageProgress = Math.min(
    100,
    Math.round((childAccountSummary.monthlySpent / childAccountSummary.dailyLimit) * 100),
  )

  childTransactions.unshift({
    id: `transfer-${Date.now()}`,
    title: `${bankName} 이체`,
    time: '방금 전',
    amount: -transferAmount,
    type: 'expense',
  })
}
