import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  getReportUsingGET: vi.fn(),
  resolveCurrentChildId: vi.fn(),
}))

vi.mock('@/api', () => ({
  api: { getReportUsingGET: mocks.getReportUsingGET },
}))
vi.mock('@/api/context', () => ({
  resolveCurrentChildId: mocks.resolveCurrentChildId,
}))

let useChildcareReport: typeof import('../useChildcareReport').useChildcareReport

describe('useChildcareReport', () => {
  beforeEach(async () => {
    vi.resetModules()
    useChildcareReport = (await import('../useChildcareReport')).useChildcareReport
  })

  it('maps the backend comparison benchmark and monthly fixed average', async () => {
    mocks.resolveCurrentChildId.mockResolvedValue(6)
    mocks.getReportUsingGET.mockResolvedValue({
      data: {
        summary: {
          total_expense_amount: 30_000,
          same_age_monthly_average_amount: 1_407_000,
          same_age_difference_amount: -1_377_000,
          same_age_difference_rate: -98,
        },
        comparison_benchmark: {
          label: '30대 부모 가구 월평균 양육비',
          age_group: '30대',
          source_name: '육아정책연구소 소비실태조사 2024',
          source_url: 'https://example.com/source',
          source_year: 2024,
          calculation_basis: '부모 30대 응답 평균',
        },
        monthly_flow: [
          { year: 2026, month: 8, expense_amount: 30_000, same_age_average_amount: 1_407_000 },
        ],
      },
    })

    const report = useChildcareReport()
    await report.load()

    expect(report.childcareReportSummary).toMatchObject({
      peerAverageAmount: 1_407_000,
      comparisonDifferenceAmount: -1_377_000,
      comparisonLabel: '30대 부모 가구 월평균 양육비',
      ageGroup: '30대',
    })
    expect(report.monthlyChildcareExpenses).toEqual([
      { month: '8월', amount: 30_000, averageAmount: 1_407_000 },
    ])
  })

  it('uses consistent demo data when the backend aggregate is empty', async () => {
    mocks.resolveCurrentChildId.mockResolvedValue(6)
    mocks.getReportUsingGET.mockResolvedValue({
      data: {
        summary: { total_expense_amount: 0 },
        monthly_flow: [],
      },
    })

    const report = useChildcareReport()
    await report.load()

    expect(report.isUsingDemoData.value).toBe(true)
    expect(report.childcareReportSummary).toMatchObject({
      currentMonthAmount: 1_860_000,
      previousMonthDifference: 120_000,
      annualAmount: 18_100_000,
      peerAverageAmount: 1_520_000,
      topCategoryLabel: '교육/학습',
      topCategoryAmount: 600_000,
      topCategoryRate: 32,
    })
    expect(report.childcareCategories).toEqual([
      expect.objectContaining({ label: '교육/학습', amount: 600_000, averageAmount: 500_000 }),
      expect.objectContaining({ label: '돌봄/보육', amount: 340_000, averageAmount: 280_000 }),
      expect.objectContaining({ label: '식비', amount: 270_000, averageAmount: 240_000 }),
      expect.objectContaining({ label: '의류/생활', amount: 210_000, averageAmount: 160_000 }),
      expect.objectContaining({ label: '의료/건강', amount: 180_000, averageAmount: 120_000 }),
      expect.objectContaining({ label: '문화/여가·교통', amount: 260_000, averageAmount: 220_000 }),
    ])
    expect(report.childcareCategories.reduce((total, category) => total + category.amount, 0)).toBe(
      1_860_000,
    )
    expect(report.monthlyChildcareExpenses).toHaveLength(12)
    expect(report.monthlyChildcareExpenses.at(-1)).toEqual({
      month: '8월',
      amount: 1_860_000,
      averageAmount: 1_520_000,
    })
  })
})
