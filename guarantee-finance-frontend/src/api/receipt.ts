import request from '@/utils/request'

export interface ReceiptVO {
  id: number
  receiptNo: string
  businessType: number
  businessTypeName: string
  customerCode: string
  customerName: string
  contractNo?: string
  projectName?: string
  productCode?: string
  productName?: string
  currency: string
  amount: number
  amountInWords?: string
  receiptDate: string
  actualArrivalDate?: string
  paymentMethod?: number
  payerName?: string
  payerAccountNo?: string
  payerBankName?: string
  payeeAccountNo?: string
  payeeBankName?: string
  usage?: string
  voucherId?: number
  voucherNo?: string
  status: number // 0-草稿 1-已提交 2-已审核 3-已记账 4-已作废
  makerId?: number
  makerName?: string
  makerTime?: string
  auditorId?: number
  auditorName?: string
  auditorTime?: string
  posterId?: number
  posterName?: string
  posterTime?: string
  createTime?: string
}

export interface ReceiptDTO {
  id?: number
  businessType: number
  customerCode: string
  customerName: string
  contractNo?: string
  projectName?: string
  productCode?: string
  productName?: string
  currency?: string
  amount: number
  receiptDate: string
  actualArrivalDate?: string
  paymentMethod?: number
  payerName?: string
  payerAccountNo?: string
  payerBankName?: string
  payeeAccountNo?: string
  payeeBankName?: string
  usage?: string
  sharerCode?: string
  sharerName?: string
  shareRatio?: number
  originalAmount?: number
  compensationNo?: string
  recoveryTargetCode?: string
  recoveryTargetName?: string
  recoveryMethod?: number
}

export function getReceiptPage(params: {
  keyword?: string; businessType?: number | null; customerCode?: string;
  status?: number | null; startDate?: string; endDate?: string;
  current: number; size: number
}) {
  return request.get('/api/receipt/page', { params })
}

export function getReceiptDetail(id: number) {
  return request.get<ReceiptVO>(`/api/receipt/${id}`)
}

export function createReceipt(data: ReceiptDTO) {
  return request.post<number>('/api/receipt', data)
}

export function updateReceipt(data: ReceiptDTO) {
  return request.put('/api/receipt', data)
}

export function deleteReceipt(id: number) {
  return request.delete(`/api/receipt/${id}`)
}

export function submitReceipt(id: number) {
  return request.put(`/api/receipt/${id}/submit`)
}

export function auditReceipt(id: number, pass: boolean) {
  return request.put(`/api/receipt/${id}/audit`, null, { params: { pass } })
}

export function postReceipt(id: number) {
  return request.put(`/api/receipt/${id}/post`)
}

export function reverseReceipt(id: number) {
  return request.put(`/api/receipt/${id}/reverse`)
}
