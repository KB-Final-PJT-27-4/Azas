export type ChildcareCategory = {
  id: string
  label: string
  amount: number
  averageAmount: number
  color: string
}

export type MonthlyChildcareExpense = {
  month: string
  amount: number
  averageAmount: number
}

export const childcareReportSummary = {
  currentMonthAmount: 1_860_000,
  previousMonthDifference: 120_000,
  previousMonthRate: 6.9,
  peerAverageAmount: 1_520_000,
  annualAmount: 18_100_000,
  ageGroup: '초등학생 자녀 가구',
  comparisonLabel: '초등학생 자녀 가구 월평균 양육비',
  benchmarkSourceName: '시연용 양육비 비교 데이터',
  benchmarkSourceUrl: '',
  benchmarkSourceYear: 2026,
  benchmarkCalculationBasis: '시연을 위한 초등학생 자녀 가구 월평균 비교값',
  topCategoryLabel: '교육/학습',
  topCategoryAmount: 744_000,
  topCategoryRate: 40,
}

export const childcareCategories: ChildcareCategory[] = [
  {
    id: 'education',
    label: '교육/학습',
    amount: 744_000,
    averageAmount: 520_000,
    color: 'var(--color-accent-yellow)',
  },
  {
    id: 'food',
    label: '식비',
    amount: 372_000,
    averageAmount: 350_000,
    color: '#ffdf62',
  },
  {
    id: 'clothing',
    label: '의류/잡화',
    amount: 279_000,
    averageAmount: 240_000,
    color: '#ffea91',
  },
  {
    id: 'culture',
    label: '여가/문화',
    amount: 186_000,
    averageAmount: 180_000,
    color: '#fff1b2',
  },
  {
    id: 'transport',
    label: '교통',
    amount: 93_000,
    averageAmount: 80_000,
    color: '#ffd75a',
  },
  {
    id: 'etc',
    label: '기타',
    amount: 186_000,
    averageAmount: 150_000,
    color: '#f3cb4f',
  },
]

export const monthlyChildcareExpenses: MonthlyChildcareExpense[] = [
  { month: '9월', amount: 1_280_000, averageAmount: 1_210_000 },
  { month: '10월', amount: 1_350_000, averageAmount: 1_260_000 },
  { month: '11월', amount: 1_420_000, averageAmount: 1_300_000 },
  { month: '12월', amount: 1_560_000, averageAmount: 1_420_000 },
  { month: '1월', amount: 1_310_000, averageAmount: 1_250_000 },
  { month: '2월', amount: 1_240_000, averageAmount: 1_200_000 },
  { month: '3월', amount: 1_480_000, averageAmount: 1_350_000 },
  { month: '4월', amount: 1_540_000, averageAmount: 1_430_000 },
  { month: '5월', amount: 1_630_000, averageAmount: 1_460_000 },
  { month: '6월', amount: 1_690_000, averageAmount: 1_490_000 },
  { month: '7월', amount: 1_740_000, averageAmount: 1_510_000 },
  { month: '8월', amount: 1_860_000, averageAmount: 1_520_000 },
]

export const formatReportWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
export const formatReportManwon = (amount: number) => `${Math.round(amount / 10_000)}만원`
