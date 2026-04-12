import request from '@/utils/request'

export function getVoucherRulePage(params: {
  keyword?: string
  businessType?: string
  status?: number
  current?: number
  size?: number
}) {
  return request.get('/accounting/voucher-rule/page', { params })
}

export function getVoucherRuleDetail(id: number) {
  return request.get(`/accounting/voucher-rule/${id}`)
}

export function createVoucherRule(data: any) {
  return request.post('/accounting/voucher-rule', data)
}

export function updateVoucherRule(id: number, data: any) {
  return request.put(`/accounting/voucher-rule/${id}`, data)
}

export function deleteVoucherRule(id: number) {
  return request.delete(`/accounting/voucher-rule/${id}`)
}

export function enableVoucherRule(id: number) {
  return request.put(`/accounting/voucher-rule/${id}/enable`)
}

export function disableVoucherRule(id: number) {
  return request.put(`/accounting/voucher-rule/${id}/disable`)
}

export function getBusinessTypes() {
  return request.get('/accounting/voucher-rule/business-types')
}
