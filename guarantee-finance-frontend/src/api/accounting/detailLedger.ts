import request from '@/utils/request'

export function getDetailLedgerPage(params: {
  period: string
  subjectCode?: string
  current?: number
  size?: number
}) {
  return request.get('/accounting/detail-ledger/page', { params })
}

export function generateDetailLedger(period: string) {
  return request.post('/accounting/detail-ledger/generate', null, { params: { period } })
}
