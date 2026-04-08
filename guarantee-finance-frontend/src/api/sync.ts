import request from '@/utils/request'

export interface CustomerVO {
  id: number
  customerCode: string
  customerName: string
  customerShortName?: string
  customerType: number
  customerTypeName?: string
  creditCode?: string
  idCard?: string
  contactPhone?: string
  contactPerson?: string
  registerAddress?: string
  businessAddress?: string
  industry?: string
  customerLevel?: number
  status: number
  sourceSystem?: string
  lastSyncTime?: string
  createTime?: string
  updateTime?: string
}

export interface SyncResult {
  successCount: number
  failCount: number
  message: string
  executeTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// 客户相关
export function getCustomerPage(params: { keyword?: string; customerType?: number | null; status?: number | null; current: number; size: number }) {
  return request.get<PageResult<CustomerVO>>('/api/sync/customer/page', { params })
}

export function getCustomerDetail(id: number) {
  return request.get<CustomerVO>(`/api/sync/customer/${id}`)
}

export function syncCustomerFull() {
  return request.post<SyncResult>('/api/sync/customer/full')
}

export function syncCustomerIncremental(lastSyncTime = '') {
  return request.post<SyncResult>('/api/sync/customer/incremental', null, { params: { lastSyncTime } })
}

export function checkCustomerCode(code: string, id?: number) {
  return request.get<boolean>('/api/sync/customer/checkCode', { params: { code, id } })
}
