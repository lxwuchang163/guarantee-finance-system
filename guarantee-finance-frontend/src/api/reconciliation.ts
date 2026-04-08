import request from '@/utils/request'

export interface BankTransactionVO {
  id: number
  transactionDate: string
  transactionNo?: string
  bankAccountNo: string
  bankAccountName: string
  direction: number // 1-收入 2-支出
  amount: number
  balanceAfter: number
  counterAccountName?: string
  counterAccountNo?: string
  summary?: string
  matchStatus: number // 0-未匹配 1-精确匹配 2-模糊匹配 3-异常
  matchStatusText?: string
  matchedBillType?: string
  matchedBillNo?: string
  matchedBillId?: number
  createTime?: string
}

export interface ReconciliationResultVO {
  id: number
  reconciliationDate: string
  bankAccountNo: string
  bankAccountName: string
  totalBankCount: number
  totalBankAmount: number
  totalBizCount: number
  totalBizAmount: number
  matchedCount: number
  matchedAmount: number
  unmatchedCount: number
  unmatchedAmount: number
  status: number // 0-对账中 1-已完成 2-有差异
  creatorName?: string
  createTime?: string
}

export function importTransactions(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/reconciliation/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export function getTransactionPage(params: {
  bankAccountNo?: string; direction?: number | null; matchStatus?: number | null;
  startDate?: string; endDate?: string; current: number; size: number
}) {
  return request.get('/api/reconciliation/transaction/list', { params })
}

export function autoReconcile(bankAccountNo: string, reconcileDate: string) {
  return request.post('/api/reconciliation/auto', null, { params: { bankAccountNo, reconcileDate } })
}

export function getReconciliationResult(params: {
  bankAccountNo?: string; startDate?: string; endDate?: string; current: number; size: number
}) {
  return request.get('/api/reconciliation/result/list', { params })
}

export function forceMatch(data: { transactionId: number; billId: number; billType: string }) {
  return request.post('/api/reconciliation/force-match', data)
}

export function generateBalanceAdjustment(reconciliationId: number) {
  return request.post(`/api/reconciliation/${reconciliationId}/balance-adjustment`)
}
