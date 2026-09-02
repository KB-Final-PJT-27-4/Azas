import { describe, expect, it } from 'vitest'

import { toFinancialGoalApiDate, toFinancialGoalMonth } from '@/utils/financialGoalDate'

describe('financial goal date conversion', () => {
  it('converts a selected month to the LocalDate expected by the API', () => {
    expect(toFinancialGoalApiDate('2030-07')).toBe('2030-07-01')
  })

  it('keeps an API date unchanged and converts it back for the month picker', () => {
    expect(toFinancialGoalApiDate('2030-07-31')).toBe('2030-07-31')
    expect(toFinancialGoalMonth('2030-07-31')).toBe('2030-07')
  })
})
