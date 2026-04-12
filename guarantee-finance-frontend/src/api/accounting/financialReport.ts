import request from '@/utils/request'

export function generateBalanceSheet(period: string) {
  return request.post('/accounting/report/balance-sheet', null, { params: { period } })
}

export function generateIncomeStatement(period: string) {
  return request.post('/accounting/report/income-statement', null, { params: { period } })
}

export function generateCashFlow(period: string) {
  return request.post('/accounting/report/cash-flow', null, { params: { period } })
}

export function getReportDetail(id: number) {
  return request.get(`/accounting/report/${id}`)
}

export function confirmReport(id: number) {
  return request.put(`/accounting/report/${id}/confirm`)
}

export function approveReport(id: number) {
  return request.put(`/accounting/report/${id}/approve`)
}

export function getReportPage(params: {
  reportType?: string
  period?: string
  current?: number
  size?: number
}) {
  return request.get('/accounting/report/page', { params })
}
