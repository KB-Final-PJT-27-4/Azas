export type AssetTransaction = {
  id: number
  goalId: number
  accountId: number
  accountLabel: string
  memo: string
  amount: number
  depositName: string
  depositAccountNumber: string
  withdrawalName: string
  withdrawalAccountNumber: string
  transactedAt: string
  balanceAfterTransaction: number
}

export type AssetAccount = {
  id: number
  name: string
  accountNumber: string
  balance: number
}

export type AssetGoal = {
  id: number
  title: string
  status: string
  targetAmount: number
  currentAmount: number
  accounts: AssetAccount[]
}

export const assetSummary = {
  totalTargetAmount: 40000000,
  totalCurrentAmount: 16750000,
}

export const assetGoals: AssetGoal[] = [
  {
    id: 1,
    title: '대학자금 마련',
    status: '진행중',
    targetAmount: 30000000,
    currentAmount: 14600000,
    accounts: [
      {
        id: 1,
        name: '아이사랑적금 1',
        accountNumber: '952-17362605-43',
        balance: 9600000,
      },
      {
        id: 2,
        name: '아이사랑적금 2',
        accountNumber: '952-17362605-43',
        balance: 9600000,
      },
    ],
  },
  {
    id: 2,
    title: '첫 독립 준비',
    status: '진행중',
    targetAmount: 10000000,
    currentAmount: 2150000,
    accounts: [],
  },
]

export const assetTransactions: AssetTransaction[] = [
  {
    id: 1,
    goalId: 1,
    accountId: 1,
    accountLabel: 'KB국민 5678',
    memo: '니 너한테 릴스 3개 보냈는데 아직도 안읽네 잘 지내',
    amount: 100000,
    depositName: '아이사랑적금1',
    depositAccountNumber: '952-17362605-43',
    withdrawalName: 'KB국민은행',
    withdrawalAccountNumber: '123-456-789',
    transactedAt: '2026.07.21 11:01',
    balanceAfterTransaction: 500000,
  },
  {
    id: 2,
    goalId: 1,
    accountId: 1,
    accountLabel: 'KB국민 5678',
    memo: '깨비 첫 생일을 위한 저축',
    amount: 100000,
    depositName: '아이사랑적금1',
    depositAccountNumber: '952-17362605-43',
    withdrawalName: '우리은행',
    withdrawalAccountNumber: '1002-111-222222',
    transactedAt: '2026.07.21 11:01',
    balanceAfterTransaction: 1200000,
  },
  {
    id: 3,
    goalId: 1,
    accountId: 2,
    accountLabel: 'KB국민 5678',
    memo: '',
    amount: 100000,
    depositName: '아이사랑적금2',
    depositAccountNumber: '952-17362605-43',
    withdrawalName: '신한은행',
    withdrawalAccountNumber: '110-123-456789',
    transactedAt: '2026.07.21 11:01',
    balanceAfterTransaction: 2350000,
  },
]

export const getAssetTransaction = (assetId: string) =>
  assetTransactions.find(({ id }) => String(id) === assetId) ?? assetTransactions[0]!
