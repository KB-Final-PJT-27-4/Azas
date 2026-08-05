export type AssetTransaction = {
  id: number
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

export const assetTransactions: AssetTransaction[] = [
  {
    id: 1,
    accountLabel: 'KB국민 5678',
    memo: '니 너한테 릴스 3개 보냈는데 아직도 안읽네 잘 지내',
    amount: 100000,
    depositName: '아이사랑적금1',
    depositAccountNumber: '952-17362605-43',
    withdrawalName: 'KB국민은행',
    withdrawalAccountNumber: '123-456-789',
    transactedAt: '2026.07.23 6:00',
    balanceAfterTransaction: 500000,
  },
  {
    id: 2,
    accountLabel: '우리은행 8901',
    memo: '깨비 첫 생일을 위한 저축',
    amount: 150000,
    depositName: '우리아이행복적금',
    depositAccountNumber: '1002-345-678901',
    withdrawalName: '우리은행',
    withdrawalAccountNumber: '1002-111-222222',
    transactedAt: '2026.07.20 14:30',
    balanceAfterTransaction: 1200000,
  },
  {
    id: 3,
    accountLabel: '신한은행 4321',
    memo: '',
    amount: 200000,
    depositName: '꿈나무적금',
    depositAccountNumber: '110-456-789012',
    withdrawalName: '신한은행',
    withdrawalAccountNumber: '110-123-456789',
    transactedAt: '2026.07.15 9:10',
    balanceAfterTransaction: 2350000,
  },
]

export const getAssetTransaction = (assetId: string) =>
  assetTransactions.find(({ id }) => String(id) === assetId) ?? assetTransactions[0]!
