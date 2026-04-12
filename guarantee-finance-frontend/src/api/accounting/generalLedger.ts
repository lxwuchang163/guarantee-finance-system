import request from '@/utils/request'

export function getGeneralLedgerPage(params: {
  period: string
  subjectCode?: string
  current?: number
  size?: number
}) {
  return request.get('/accounting/general-ledger/page', { params })
}

export function generateGeneralLedger(period: string) {
  return request.post('/accounting/general-ledger/generate', null, { params: { period } })
}
