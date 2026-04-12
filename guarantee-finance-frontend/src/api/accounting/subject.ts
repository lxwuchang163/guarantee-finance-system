import request from '@/utils/request'

export interface SubjectVO {
  id: number
  subjectCode: string
  subjectName: string
  subjectLevel: number
  parentCode: string
  subjectType: number
  balanceDirection: number
  status: number
  auxiliaryDimension: string
  description: string
  sortOrder: number
  category: string
  systemType: string
  createTime: string
  updateTime: string
  children?: SubjectVO[]
}

export interface SubjectDTO {
  subjectCode: string
  subjectName: string
  subjectLevel: number
  parentCode: string
  subjectType: number
  balanceDirection: number
  auxiliaryDimension: string
  description: string
  sortOrder: number
  category: string
  systemType: string
}

export interface SubjectBalanceInitDTO {
  subjectCode: string
  subjectName: string
  beginDebit: number
  beginCredit: number
  period: string
  year: number
  month: number
}

export function getSubjectPage(params: {
  subjectCode?: string
  subjectName?: string
  status?: number
  page: number
  size: number
}) {
  return request.get('/accounting/subject/page', { params })
}

export function getSubjectDetail(id: number) {
  return request.get(`/accounting/subject/detail/${id}`)
}

export function createSubject(data: SubjectDTO) {
  return request.post('/accounting/subject', data)
}

export function updateSubject(id: number, data: SubjectDTO) {
  return request.put(`/accounting/subject/${id}`, data)
}

export function deleteSubject(id: number) {
  return request.delete(`/accounting/subject/${id}`)
}

export function changeSubjectStatus(id: number, status: number) {
  return request.put(`/accounting/subject/${id}/status`, { status })
}

export function getSubjectTree() {
  return request.get('/accounting/subject/tree')
}

export function getEnabledSubjects() {
  return request.get('/accounting/subject/enabled')
}

export function checkSubjectCode(subjectCode: string, id?: number) {
  return request.get('/accounting/subject/check-code', { params: { subjectCode, id } })
}

export function importSubjects(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/accounting/subject/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function validateSubjects() {
  return request.post('/accounting/subject/validate')
}

export function getSubjectBalances(period?: string) {
  return request.get('/accounting/subject/balance/list', { params: { period } })
}

export function initSubjectBalances(balanceList: SubjectBalanceInitDTO[]) {
  return request.post('/accounting/subject/balance/init', balanceList)
}

export function validateBalances(period?: string) {
  return request.post('/accounting/subject/balance/validate', null, { params: { period } })
}
