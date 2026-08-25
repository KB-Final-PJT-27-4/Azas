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
  topCategoryAmount: 600_000,
  topCategoryRate: 32,
}

export const childcareCategories: ChildcareCategory[] = [
  {
    id: 'education',
    label: '교육/학습',
    amount: 600_000,
    averageAmount: 500_000,
    color: 'var(--color-accent-yellow)',
  },
  {
    id: 'childcare',
    label: '돌봄/보육',
    amount: 340_000,
    averageAmount: 280_000,
    color: '#ffdf62',
  },
  {
    id: 'food',
    label: '식비',
    amount: 270_000,
    averageAmount: 240_000,
    color: '#ffea91',
  },
  {
    id: 'clothing-living',
    label: '의류/생활',
    amount: 210_000,
    averageAmount: 160_000,
    color: '#fff1b2',
  },
  {
    id: 'medical-health',
    label: '의료/건강',
    amount: 180_000,
    averageAmount: 120_000,
    color: '#ffd75a',
  },
  {
    id: 'culture-transport',
    label: '문화/여가·교통',
    amount: 260_000,
    averageAmount: 220_000,
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
