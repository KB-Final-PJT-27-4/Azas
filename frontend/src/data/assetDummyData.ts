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

export type LinkedAssetAccount = {
  id: string
  name: string
  accountNumber: string
  bankName: string
  ownerName: string
  type: '적금' | '입출금'
  balance: number
}

export type LinkedAssetTransfer = {
  id: string
  accountId: string
  transactionId: number
  transactedAt: string
  counterparty: string
  amount: number
  direction: '입금' | '출금'
}

export const linkedAssetAccounts: LinkedAssetAccount[] = [
  {
    id: 'parent-saving-1',
    name: '아이사랑적금1',
    accountNumber: '952-17362605-43',
    bankName: 'KB국민은행',
    ownerName: '황현진',
    type: '적금',
    balance: 4_800_000,
  },
  {
    id: 'parent-saving-2',
    name: '아이사랑적금2',
    accountNumber: '952-17362605-44',
    bankName: 'KB국민은행',
    ownerName: '황현진',
    type: '적금',
    balance: 4_800_000,
  },
  {
    id: 'parent-account-1',
    name: '아이사랑통장',
    accountNumber: '952-17362605-45',
    bankName: 'KB국민은행',
    ownerName: '황현진',
    type: '입출금',
    balance: 5_000_000,
  },
  {
    id: 'child-saving-1',
    name: '아이사랑적금1',
    accountNumber: '952-17362605-46',
    bankName: 'KB국민은행',
    ownerName: '깨비',
    type: '적금',
    balance: 9_600_000,
  },
  {
    id: 'child-account-1',
    name: '아이사랑통장',
    accountNumber: '952-17362605-47',
    bankName: 'KB국민은행',
    ownerName: '깨비',
    type: '입출금',
    balance: 5_000_000,
  },
]

export const deletedLinkedAssetAccountIds = new Set<string>()

export const removeLinkedAssetAccount = (accountId: string) => {
  deletedLinkedAssetAccountIds.add(accountId)

  const accountIndex = linkedAssetAccounts.findIndex(({ id }) => id === accountId)
  if (accountIndex >= 0) linkedAssetAccounts.splice(accountIndex, 1)

  for (let index = linkedAssetTransfers.length - 1; index >= 0; index -= 1) {
    if (linkedAssetTransfers[index]?.accountId === accountId) linkedAssetTransfers.splice(index, 1)
  }
}

export const linkedAssetTransfers: LinkedAssetTransfer[] = linkedAssetAccounts.flatMap(
  (account, accountIndex) =>
    Array.from({ length: accountIndex === 0 ? 5 : 3 }, (_, index) => ({
      id: `${account.id}-transfer-${index + 1}`,
      accountId: account.id,
      transactionId: (index % assetTransactions.length) + 1,
      transactedAt: `2026.07.${String(21 - index).padStart(2, '0')} 11:01`,
      counterparty: index % 2 === 0 ? 'KB국민 5678' : '아이사랑통장',
      amount: index % 3 === 0 ? 100_000 : 50_000,
      direction: index === 3 ? ('출금' as const) : ('입금' as const),
    })),
)

export const getLinkedAssetAccount = (accountId: string) =>
  linkedAssetAccounts.find(({ id }) => id === accountId) ?? linkedAssetAccounts[0]!

export const getLinkedAssetTransfers = (accountId: string) =>
  linkedAssetTransfers.filter((transfer) => transfer.accountId === accountId)
