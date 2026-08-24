import { reactive, ref } from 'vue'

import { api } from '@/api'
import { resolveCurrentChildId } from '@/api/context'

export type MonthlyChildcareExpense = { month: string; amount: number; averageAmount: number }

const childcareReportSummary = reactive({
  currentMonthAmount: 0,
  previousMonthDifference: 0,
  previousMonthRate: 0,
  peerAverageAmount: 0,
  comparisonDifferenceAmount: 0,
  comparisonDifferenceRate: 0,
  comparisonLabel: '30대 부모 가구 월평균 양육비',
  benchmarkSourceName: '',
  benchmarkSourceUrl: '',
  benchmarkSourceYear: 0,
  benchmarkCalculationBasis: '',
  annualAmount: 0,
  ageGroup: '30대',
})
const monthlyChildcareExpenses = reactive<MonthlyChildcareExpense[]>([])
const loadedPeriod = ref('')
let loading: Promise<void> | null = null

const load = async () => {
  const now = new Date()
  const period = `${now.getFullYear()}-${now.getMonth() + 1}`
  if (loadedPeriod.value === period) return
  if (loading) return loading

  loading = (async () => {
    const childId = await resolveCurrentChildId()
    const { data } = await api.getReportUsingGET(childId, now.getMonth() + 1, now.getFullYear())
    childcareReportSummary.currentMonthAmount = data.summary?.total_expense_amount ?? 0
    childcareReportSummary.previousMonthDifference = data.summary?.previous_month_change_amount ?? 0
    childcareReportSummary.previousMonthRate = data.summary?.previous_month_change_rate ?? 0
    childcareReportSummary.annualAmount = data.summary?.annual_expense_amount ?? 0
    childcareReportSummary.peerAverageAmount = data.summary?.same_age_monthly_average_amount
      ?? data.comparison_benchmark?.monthly_average_amount ?? 0
    childcareReportSummary.comparisonDifferenceAmount = data.summary?.same_age_difference_amount
      ?? childcareReportSummary.currentMonthAmount - childcareReportSummary.peerAverageAmount
    childcareReportSummary.comparisonDifferenceRate = data.summary?.same_age_difference_rate ?? 0
    childcareReportSummary.comparisonLabel = data.comparison_benchmark?.label
      ?? '30대 부모 가구 월평균 양육비'
    childcareReportSummary.ageGroup = data.comparison_benchmark?.age_group ?? '30대'
    childcareReportSummary.benchmarkSourceName = data.comparison_benchmark?.source_name ?? ''
    childcareReportSummary.benchmarkSourceUrl = data.comparison_benchmark?.source_url ?? ''
    childcareReportSummary.benchmarkSourceYear = data.comparison_benchmark?.source_year ?? 0
    childcareReportSummary.benchmarkCalculationBasis = data.comparison_benchmark?.calculation_basis ?? ''
    monthlyChildcareExpenses.splice(0, monthlyChildcareExpenses.length, ...(data.monthly_flow ?? []).map((item) => ({
      month: `${item.month ?? 0}월`,
      amount: item.expense_amount ?? 0,
      averageAmount: item.same_age_average_amount ?? childcareReportSummary.peerAverageAmount,
    })))
    loadedPeriod.value = period
  })().finally(() => { loading = null })

  return loading
}

export const formatReportWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
export const formatReportManwon = (amount: number) => `${Math.round(amount / 10_000)}만원`

export const useChildcareReport = () => ({
  childcareReportSummary,
  monthlyChildcareExpenses,
  load,
})
