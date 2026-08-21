import { reactive } from 'vue'

export interface ChildTransaction {
  id: string
  title: string
  time: string
  amount: number
  type: 'income' | 'expense'
  icon: string
  accountLabel?: string
  memo?: string
  depositName?: string
  depositAccountNumber?: string
  withdrawalName?: string
  withdrawalAccountNumber?: string
  balanceAfterTransaction?: number
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
    accountLabel: '엄마 국민은행',
    memo: '이번 주 용돈이야. 계획해서 써봐',
    depositName: '깨비 돈',
    depositAccountNumber: '952-17362605-47',
    withdrawalName: '엄마 국민은행',
    withdrawalAccountNumber: '123-456-789',
    balanceAfterTransaction: 96_000,
  },
  {
    id: 'stationery-1',
    title: '문구점',
    time: '어제 오후 4:12',
    amount: -4_000,
    type: 'expense',
    icon: '🛍️',
    accountLabel: '깨비 돈',
    memo: '문구점에서 준비물 구매',
    depositName: '문구점',
    depositAccountNumber: '가맹점 결제',
    withdrawalName: '깨비 돈',
    withdrawalAccountNumber: '952-17362605-47',
    balanceAfterTransaction: 66_000,
  },
  {
    id: 'icecream',
    title: '아이스크림 가게',
    time: '어제 오후 3:15',
    amount: -2_500,
    type: 'expense',
    icon: '🍦',
    accountLabel: '깨비 돈',
    memo: '아이스크림 간식',
    depositName: '아이스크림 가게',
    depositAccountNumber: '가맹점 결제',
    withdrawalName: '깨비 돈',
    withdrawalAccountNumber: '952-17362605-47',
    balanceAfterTransaction: 70_000,
  },
  {
    id: 'convenience',
    title: '편의점',
    time: '어제 오후 1:20',
    amount: -2_000,
    type: 'expense',
    icon: '🧃',
    accountLabel: '깨비 돈',
    memo: '편의점 간식',
    depositName: '편의점',
    depositAccountNumber: '가맹점 결제',
    withdrawalName: '깨비 돈',
    withdrawalAccountNumber: '952-17362605-47',
    balanceAfterTransaction: 72_500,
  },
])

export const childFallbackTransactions: ChildTransaction[] = [
  {
    id: 'asset-stationery-1',
    title: '문구점',
    time: '2026.07.21 11:01',
    amount: -10_000,
    type: 'expense',
    icon: '₩',
    accountLabel: '깨비 돈',
    memo: '준비물 구매',
    depositName: '문구점',
    depositAccountNumber: '가맹점 결제',
    withdrawalName: '깨비 돈',
    withdrawalAccountNumber: '952-17362605-47',
    balanceAfterTransaction: 50_000,
  },
  {
    id: 'asset-icecream',
    title: '아이스크림 가게',
    time: '2026.07.21 11:01',
    amount: -1_000,
    type: 'expense',
    icon: '₩',
    accountLabel: '깨비 돈',
    memo: '아이스크림 구매',
    depositName: '아이스크림 가게',
    depositAccountNumber: '가맹점 결제',
    withdrawalName: '깨비 돈',
    withdrawalAccountNumber: '952-17362605-47',
    balanceAfterTransaction: 60_000,
  },
  {
    id: 'asset-convenience',
    title: '편의점',
    time: '2026.07.21 11:01',
    amount: -3_000,
    type: 'expense',
    icon: '₩',
    accountLabel: '깨비 돈',
    memo: '편의점 구매',
    depositName: '편의점',
    depositAccountNumber: '가맹점 결제',
    withdrawalName: '깨비 돈',
    withdrawalAccountNumber: '952-17362605-47',
    balanceAfterTransaction: 61_000,
  },
  {
    id: 'asset-stationery-2',
    title: '문구점',
    time: '2026.07.21 11:01',
    amount: -5_000,
    type: 'expense',
    icon: '₩',
    accountLabel: '깨비 돈',
    memo: '필기구 구매',
    depositName: '문구점',
    depositAccountNumber: '가맹점 결제',
    withdrawalName: '깨비 돈',
    withdrawalAccountNumber: '952-17362605-47',
    balanceAfterTransaction: 64_000,
  },
  {
    id: 'asset-allowance',
    title: '엄마 용돈',
    time: '2026.07.21 11:01',
    amount: 100_000,
    type: 'income',
    icon: '₩',
    accountLabel: '엄마 국민은행',
    memo: '필요한 곳에 잘 써',
    depositName: '깨비 돈',
    depositAccountNumber: '952-17362605-47',
    withdrawalName: '엄마 국민은행',
    withdrawalAccountNumber: '123-456-789',
    balanceAfterTransaction: 100_000,
  },
]

export const childAssetTransactions = [...childTransactions, ...childFallbackTransactions]

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

export const getChildTransaction = (transactionId: string) =>
  childAssetTransactions.find((transaction) => transaction.id === transactionId) ??
  childAssetTransactions[0]!
