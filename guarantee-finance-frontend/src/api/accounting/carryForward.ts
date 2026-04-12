import request from '@/utils/request'

export function previewCarryForward(period: string) {
  return request.post('/accounting/carry-forward/preview', null, { params: { period } })
}

export function carryForwardProfitLoss(period: string) {
  return request.post('/accounting/carry-forward/profit-loss', null, { params: { period } })
}

export function reverseCarryForward(id: number) {
  return request.post(`/accounting/carry-forward/${id}/reverse`)
}

export function getCarryForwardList(period?: string) {
  return request.get('/accounting/carry-forward/list', { params: { period } })
}
