export type AllowanceRequestStatus = 'pending' | 'approved' | 'rejected'

export type AllowanceRequest = {
  id: string
  childName: string
  amount: number
  reason: string
  requestedAt: string
  targetAccountName: string
  targetAccountNumber: string
  status: AllowanceRequestStatus
}

export const allowanceRequests: AllowanceRequest[] = [
  {
    id: 'allowance-request-1',
    childName: '깨비',
    amount: 10000,
    reason: '친구 생일 선물을 사려고 해요!\n예쁜 선물을 고르고 싶어요 😊',
    requestedAt: '2026.08.07 (금) 오전 10:30',
    targetAccountName: 'KB 깨비 용돈통장',
    targetAccountNumber: '123-456-789',
    status: 'pending',
  },
]

export const getAllowanceRequest = (requestId: string) =>
  allowanceRequests.find(({ id }) => id === requestId)
