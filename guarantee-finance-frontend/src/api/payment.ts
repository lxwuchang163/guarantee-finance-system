import request from '@/utils/request'

export interface PaymentVO {
  id: number
  paymentNo: string
  businessType: number
  businessTypeName: string
  customerCode: string
  customerName: string
  contractNo?: string
  originalReceiptNo?: string
  currency: string
  amount: number
  amountInWords?: string
  paymentDate: string
  actualPaymentDate?: string
  paymentMethod?: number
  payeeName?: string
  payeeAccountNo?: string
  payeeBankName?: string
  payerAccountNo?: string
  payerBankName?: string
  usage?: string
  voucherId?: number
  voucherNo?: string
  status: number // 0-草稿 1-已提交 2-已审核 3-已付款 4-已记账 5-已作废
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

export interface PaymentDTO {
  id?: number
  businessType: number
  customerCode: string
  customerName: string
  contractNo?: string
  originalReceiptNo?: string
  currency?: string
  amount: number
  paymentDate: string
  actualPaymentDate?: string
  paymentMethod?: number
  payeeName?: string
  payeeAccountNo?: string
  payeeBankName?: string
  payerAccountNo?: string
  payerBankName?: string
  usage?: string
  refundReason?: number
  refundRatio?: number
  originalAmount?: number
  creditorName?: string
  principalAmount?: number
  interestAmount?: number
  penaltyAmount?: number
  otherFeeAmount?: number
  totalCompensationAmount?: number
}

export function getPaymentPage(params: {
  keyword?: string; businessType?: number | null; customerCode?: string;
  status?: number | null; startDate?: string; endDate?: string;
  current: number; size: number
}) {
  return request.get('/api/payment/page', { params })
}

export function getPaymentDetail(id: number) {
  return request.get<PaymentVO>(`/api/payment/${id}`)
}

export function createPayment(data: PaymentDTO) {
  return request.post<number>('/api/payment', data)
}

export function updatePayment(data: PaymentDTO) {
  return request.put('/api/payment', data)
}

export function deletePayment(id: number) {
  return request.delete(`/api/payment/${id}`)
}

export function submitPayment(id: number) {
  return request.put(`/api/payment/${id}/submit`)
}

export function auditPayment(id: number, pass: boolean) {
  return request.put(`/api/payment/${id}/audit`, null, { params: { pass } })
}

export function executePay(id: number) {
  return request.put(`/api/payment/${id}/pay`)
}

export function postPayment(id: number) {
  return request.put(`/api/payment/${id}/post`)
}

export function reversePayment(id: number) {
  return request.put(`/api/payment/${id}/reverse`)
}
