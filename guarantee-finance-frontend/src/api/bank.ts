import request from '@/utils/request'

export interface BankAccountConfigVO {
  id: number
  accountNo: string
  accountName: string
  bankName: string
  bankCode: string
  currency: string
  balance: number
  availableBalance: number
  thresholdWarning: number
  thresholdStop: number
  dailyLimit: number
  singleLimit: number
  apiEndpoint?: string
  apiStatus?: number // 0-未配置 1-正常 2-异常
  status: number // 0-停用 1-启用
  remark?: string
}

export interface BankBalanceInfo {
  accountNo: string
  accountName: string
  bankName: string
  balance: number
  availableBalance: number
  updateTime: string
  isBelowThreshold: boolean
}

export function queryBalance(accountNo: string) {
  return request.get<BankBalanceInfo>('/api/bank/balance', { params: { accountNo } })
}

export function batchQueryBalances() {
  return request.post<BankBalanceInfo[]>('/api/bank/batch-balance')
}

export function downloadTransactions(accountNo: string, startDate: string, endDate: string) {
  return request.get('/api/bank/transactions', { params: { accountNo, startDate, endDate } })
}

export function checkPaymentStatus(paymentNo: string) {
  return request.get('/api/bank/payment-status', { params: { paymentNo } })
}

export function getBankAccountList() {
  return request.get<BankAccountConfigVO[]>('/api/bank/account/list')
}

export function saveBankAccount(data: Partial<BankAccountConfigVO>) {
  return request.post('/api/bank/account/save', data)
}
