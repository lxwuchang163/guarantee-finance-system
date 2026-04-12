import request from '@/utils/request'

export function getPeriodList(year?: number) {
  return request.get('/accounting/period/list', { params: { year } })
}

export function getCurrentPeriod() {
  return request.get('/accounting/period/current')
}

export function initPeriods(year: number) {
  return request.post('/accounting/period/init', null, { params: { year } })
}

export function closePeriod(periodCode: string) {
  return request.put(`/accounting/period/${periodCode}/close`)
}

export function reopenPeriod(periodCode: string) {
  return request.put(`/accounting/period/${periodCode}/reopen`)
}

export function checkPeriodCanClose(periodCode: string) {
  return request.get(`/accounting/period/${periodCode}/check`)
}
