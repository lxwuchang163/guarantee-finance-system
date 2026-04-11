import request from '@/utils/request'

export interface NcSyncLogVO {
  id: number
  syncType: string
  businessKey: string
  status: number
  statusText: string
  errorMessage?: string
  retryCount?: number
  durationMs?: number
  syncTime?: string
  createTime?: string
}

export interface NcCustomerSyncDTO {
  id?: number
  customerCode: string
  customerName: string
  customerShortName?: string
  customerType: number
  creditCode?: string
  industry?: string
  contactPhone?: string
  address?: string
  status?: number
}

export interface NcVoucherSyncDTO {
  id?: number
  voucherNo: string
  voucherDate: string
  voucherType: number
  accountingPeriod: string
  companyCode: string
  maker: string
  remark?: string
  totalDebit: number
  totalCredit: number
  sourceBillType?: string
  sourceBillNo?: string
  details?: VoucherDetailSyncDTO[]
}

export interface VoucherDetailSyncDTO {
  lineNo: number
  accountCode: string
  accountName: string
  direction: string
  amount: number
  summary: string
  customerCode?: string
  departmentCode?: string
  projectCode?: string
}

// 认证
export function ncLogin() { return request.post<boolean>('/nc/login') }
export function ncLogout() { return request.post('/nc/logout') }

// 客户同步
export function syncCustomerToNc(data: NcCustomerSyncDTO) { return request.post<boolean>('/nc/customer/sync', data) }
export function batchSyncCustomersToNc(data: NcCustomerSyncDTO[]) { return request.post<boolean>('/nc/customer/batchSync', data) }

// 凭证同步
export function syncVoucherToNc(data: NcVoucherSyncDTO) { return request.post<boolean>('/nc/voucher/sync', data) }
export function batchSyncVouchersToNc(data: NcVoucherSyncDTO[]) { return request.post<boolean>('/nc/voucher/batchSync', data) }

// 同步日志
export function getNcSyncLogList(params: { syncType?: string; status?: number | null; current: number; size: number }) {
  return request.get('/nc/syncLog/list', { params })
}
export function getNcSyncLogDetail(id: number) { return request.get<NcSyncLogVO>(`/nc/syncLog/${id}`) }
export function retryNcSync(id: number) { return request.put<boolean>(`/nc/syncLog/${id}/retry`) }

// 差异检查
export function checkCustomerDiff(customerCode: string) { return request.get('/nc/customer/checkDiff', { params: { customerCode } }) }
