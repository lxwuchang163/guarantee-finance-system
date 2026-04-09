import request from '@/utils/request'

export interface VoucherDetailVO {
  id: number
  voucherId: number
  lineNo: number
  subjectCode: string
  subjectName: string
  summary: string
  debitAmount: number
  creditAmount: number
  auxiliaryInfo: string
  departmentCode: string
  projectCode: string
  customerCode: string
  supplierCode: string
  businessCode: string
  bankCode: string
  remark: string
  createTime: string
  updateTime: string
}

export interface VoucherVO {
  id: number
  voucherNo: string
  voucherDate: string
  period: string
  summary: string
  status: number
  statusText: string
  createUserId: number
  createUserName: string
  approveUserId: number
  approveUserName: string
  postUserId: number
  postUserName: string
  auditStatus: string
  auditOpinion: string
  voucherType: number
  voucherTypeText: string
  sourceType: string
  sourceId: string
  ncSyncStatus: string
  remark: string
  year: number
  month: number
  createTime: string
  updateTime: string
  details: VoucherDetailVO[]
}

export interface VoucherDetailDTO {
  lineNo: number
  subjectCode: string
  subjectName: string
  summary: string
  debitAmount: number
  creditAmount: number
  auxiliaryInfo: string
  departmentCode: string
  projectCode: string
  customerCode: string
  supplierCode: string
  businessCode: string
  bankCode: string
  remark: string
}

export interface VoucherDTO {
  voucherNo: string
  voucherDate: string
  period: string
  summary: string
  voucherType: number
  sourceType: string
  sourceId: string
  remark: string
  details: VoucherDetailDTO[]
}

export function getVoucherPage(params: {
  voucherNo?: string
  period?: string
  voucherDate?: string
  status?: number
  page: number
  size: number
}) {
  return request.get('/api/accounting/voucher/page', { params })
}

export function getVoucherDetail(id: number) {
  return request.get(`/api/accounting/voucher/detail/${id}`)
}

export function createVoucher(data: VoucherDTO) {
  return request.post('/api/accounting/voucher', data)
}

export function updateVoucher(id: number, data: VoucherDTO) {
  return request.put(`/api/accounting/voucher/${id}`, data)
}

export function deleteVoucher(id: number) {
  return request.delete(`/api/accounting/voucher/${id}`)
}

export function submitVoucher(id: number) {
  return request.put(`/api/accounting/voucher/${id}/submit`)
}

export function voidVoucher(id: number) {
  return request.put(`/api/accounting/voucher/${id}/void`)
}

export function restoreVoucher(id: number) {
  return request.put(`/api/accounting/voucher/${id}/restore`)
}

export function getVouchersByPeriod(period: string) {
  return request.get('/api/accounting/voucher/period', { params: { period } })
}

export function checkVoucherNo(voucherNo: string, id?: number) {
  return request.get('/api/accounting/voucher/check-no', { params: { voucherNo, id } })
}

export function postVoucher(id: number) {
  return request.put(`/api/accounting/voucher/${id}/post`)
}

export function unpostVoucher(id: number) {
  return request.put(`/api/accounting/voucher/${id}/unpost`)
}

export function importVouchers(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/accounting/voucher/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function exportVouchersToExcel(period: string) {
  return request.get('/api/accounting/voucher/export/excel', {
    params: { period },
    responseType: 'blob'
  })
}

export function exportVoucherToPdf(id: number) {
  return request.get(`/api/accounting/voucher/export/pdf/${id}`, {
    responseType: 'blob'
  })
}
