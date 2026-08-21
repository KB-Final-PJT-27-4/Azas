import { reactive, ref } from 'vue'

import { api } from '@/api'
import { resolveCurrentChildId } from '@/api/context'

export type ChildcareCategory = {
  id: string
  label: string
  amount: number
  averageAmount: number
  color: string
}

export type MonthlyChildcareExpense = { month: string; amount: number; averageAmount: number }

const childcareReportSummary = reactive({
  currentMonthAmount: 0,
  previousMonthDifference: 0,
  previousMonthRate: 0,
  peerAverageAmount: 0,
  annualAmount: 0,
  ageGroup: '자녀',
})
const childcareCategories = reactive<ChildcareCategory[]>([])
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
    monthlyChildcareExpenses.splice(0, monthlyChildcareExpenses.length, ...(data.monthly_flow ?? []).map((item) => ({
      month: `${item.month ?? 0}월`,
      amount: item.expense_amount ?? 0,
      averageAmount: 0,
    })))
    loadedPeriod.value = period
  })().finally(() => { loading = null })

  return loading
}

export const formatReportWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
export const formatReportManwon = (amount: number) => `${Math.round(amount / 10_000)}만원`

export const useChildcareReport = () => ({
  childcareReportSummary,
  childcareCategories,
  monthlyChildcareExpenses,
  load,
})
