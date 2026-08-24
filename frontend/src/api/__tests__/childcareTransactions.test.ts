import { describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({ patch: vi.fn() }))

vi.mock('@/api/http', () => ({
  http: { patch: mocks.patch },
}))

import { updateChildcareTransactionTag } from '@/api/childcareTransactions'

describe('childcare transaction tag API', () => {
  it('sends the selected child ID to the external-debit tag endpoint', () => {
    updateChildcareTransactionTag(901, 6)

    expect(mocks.patch).toHaveBeenCalledWith(
      '/v1/account-transactions/901/childcare',
      { child_id: 6 },
    )
  })

  it('sends null to remove the childcare tag', () => {
    updateChildcareTransactionTag(901, null)

    expect(mocks.patch).toHaveBeenLastCalledWith(
      '/v1/account-transactions/901/childcare',
      { child_id: null },
    )
  })
})
