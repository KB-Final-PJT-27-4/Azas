import { http } from '@/api/http'

export type ChildcareTransactionTagResponse = {
  account_transaction_id: number
  childcare_included: boolean
  childcare_child_id: number | null
}

export const updateChildcareTransactionTag = (
  accountTransactionId: number,
  childId: number | null,
) => http.patch<ChildcareTransactionTagResponse>(
  `/v1/account-transactions/${accountTransactionId}/childcare`,
  { child_id: childId },
)
