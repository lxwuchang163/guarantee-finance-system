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
  apiStatus?: number
  status: number
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
  return request.get<BankBalanceInfo>('/bank/balance', { params: { accountNo } })
}

export function batchQueryBalances(accountNos: string[]) {
  return request.post<BankBalanceInfo[]>('/bank/balance/batch', accountNos)
}

export function downloadTransactions(accountNo: string, startDate: string, endDate: string) {
  return request.get('/bank/transactions/download', { params: { accountNo, startDate, endDate } })
}

export function checkPaymentStatus(bankCode: string, paymentOrderNo: string) {
  return request.get('/bank/payment/status', { params: { bankCode, paymentOrderNo } })
}

export function getBankAccountList() {
  return request.get<BankAccountConfigVO[]>('/bank/account/list')
}

export function saveBankAccount(data: Partial<BankAccountConfigVO>) {
  return request.post('/bank/account/save', data)
}
