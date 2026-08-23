const MONTH_PATTERN = /^\d{4}-(0[1-9]|1[0-2])$/
const DATE_PATTERN = /^\d{4}-(0[1-9]|1[0-2])-\d{2}$/

export const toFinancialGoalApiDate = (value: string) => {
  if (MONTH_PATTERN.test(value)) return `${value}-01`
  if (DATE_PATTERN.test(value)) return value
  return value
}

export const toFinancialGoalMonth = (value: string) => (
  DATE_PATTERN.test(value) ? value.slice(0, 7) : value
)
