import request from '@/utils/request'

export interface AccVoucherVO {
  id: number
  voucherNo: string
  voucherDate: string
  voucherType: number
  voucherTypeName: string
  accountingPeriod: string
  companyCode: string
  maker: string
  auditor?: string
  totalDebit: number
  totalCredit: number
  status: number // 0-未审核 1-已审核 2-已记账 3-已作废
  statusText?: string
  sourceBillType?: string
  sourceBillNo?: string
  ncSyncStatus?: number
  createTime?: string
}

export interface VoucherGenerateDTO {
  sourceBillId: number
  sourceBillType: 'RECEIPT' | 'PAYMENT'
  sourceBillNo: string
  voucherType: string
  customerCode: string
  customerName: string
  amount: number
  summaryTemplate?: string
}

export function getVoucherPage(params: {
  voucherNo?: string; voucherType?: string; status?: string;
  startDate?: string; endDate?: string; current: number; size: number
}) {
  return request.get('/accounting/voucher/list', { params })
}

export function getVoucherDetail(id: number) {
  return request.get<AccVoucherVO>(`/accounting/voucher/${id}`)
}

export function generateVoucher(data: VoucherGenerateDTO) {
  return request.post<number>('/accounting/voucher/generate', data)
}

export function batchGenerateVouchers(billIds: number[]) {
  return request.post<boolean>('/accounting/voucher/batch-generate', billIds)
}

export function auditVoucher(id: number) {
  return request.put(`/accounting/voucher/${id}/audit`)
}

export function reverseVoucher(id: number) {
  return request.put(`/accounting/voucher/${id}/reverse`)
}
