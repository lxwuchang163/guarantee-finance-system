import request from '@/utils/request'

export interface BankTransactionVO {
  id: number
  transactionDate: string
  transactionNo?: string
  bankAccountNo: string
  bankAccountName: string
  direction: number
  amount: number
  balanceAfter: number
  counterAccountName?: string
  counterAccountNo?: string
  summary?: string
  matchStatus: number
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
  status: number
  creatorName?: string
  createTime?: string
}

export function importTransactions(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/reconciliation/transaction/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export function getTransactionPage(params: {
  accountNo?: string; transactionType?: number | null; matchStatus?: number | null;
  startDate?: string; endDate?: string; current: number; size: number
}) {
  return request.get('/reconciliation/transaction/list', { params })
}

export function autoReconcile(accountNo: string, reconcileDate: string) {
  return request.post('/reconciliation/auto', null, { params: { accountNo, reconcileDate } })
}

export function getReconciliationResult(params: {
  accountNo?: string; startDate?: string; endDate?: string; current: number; size: number
}) {
  return request.get('/reconciliation/result', { params })
}

export function forceMatch(data: { transactionId: number; billId: number; billType: string }) {
  return request.put('/reconciliation/forceMatch', null, { params: data })
}

export function generateBalanceAdjustment(accountNo: string, date: string) {
  return request.get('/reconciliation/balanceAdjustment', { params: { accountNo, date } })
}
