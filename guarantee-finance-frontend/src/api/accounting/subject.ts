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

export function getSubjectPage(params: {
  subjectCode?: string
  subjectName?: string
  status?: number
  page: number
  size: number
}) {
  return request.get('/api/accounting/subject/page', { params })
}

export function getSubjectDetail(id: number) {
  return request.get(`/api/accounting/subject/detail/${id}`)
}

export function createSubject(data: SubjectDTO) {
  return request.post('/api/accounting/subject', data)
}

export function updateSubject(id: number, data: SubjectDTO) {
  return request.put(`/api/accounting/subject/${id}`, data)
}

export function deleteSubject(id: number) {
  return request.delete(`/api/accounting/subject/${id}`)
}

export function changeSubjectStatus(id: number, status: number) {
  return request.put(`/api/accounting/subject/${id}/status`, { status })
}

export function getSubjectTree() {
  return request.get('/api/accounting/subject/tree')
}

export function getEnabledSubjects() {
  return request.get('/api/accounting/subject/enabled')
}

export function checkSubjectCode(subjectCode: string, id?: number) {
  return request.get('/api/accounting/subject/check-code', { params: { subjectCode, id } })
}

export function importSubjects(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/accounting/subject/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function validateSubjects() {
  return request.post('/api/accounting/subject/validate')
}
