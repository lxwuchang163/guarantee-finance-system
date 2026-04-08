import request from '@/utils/request'

export interface OrgVO {
  id: number
  orgName: string
  orgCode: string
  parentId: number | null
  parentName?: string
  level?: number
  leader?: string
  phone?: string
  address?: string
  status: number
  sortOrder: number
  children?: OrgVO[]
  createTime?: string
  updateTime?: string
  remark?: string
}

export interface OrgTreeVO {
  id: number
  label: string
  orgCode: string
  level?: number
  status: number
  children?: OrgTreeVO[]
}

export interface OrgQueryDTO {
  orgName?: string
  orgCode?: string
  parentId?: number | null
  status?: number | null
  level?: number | null
}

export interface OrgDTO {
  id?: number
  orgName: string
  orgCode: string
  parentId?: number | null
  level?: number
  leader?: string
  phone?: string
  address?: string
  status?: number
  sortOrder?: number
  remark?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export function getOrgTree() {
  return request.get<OrgTreeVO[]>('/system/org/tree')
}

export function getOrgPage(params: OrgQueryDTO & { current: number; size: number }) {
  return request.get<PageResult<OrgVO>>('/system/org/page', { params })
}

export function getOrgDetail(id: number) {
  return request.get<OrgVO>(`/system/org/${id}`)
}

export function addOrg(data: OrgDTO) {
  return request.post('/system/org', data)
}

export function updateOrg(data: OrgDTO) {
  return request.put('/system/org', data)
}

export function deleteOrg(id: number) {
  return request.delete(`/system/org/${id}`)
}

export function updateOrgStatus(id: number, status: number) {
  return request.put(`/system/org/status/${id}`, null, { params: { status } })
}

export function moveOrgNode(id: number, targetParentId: number, sortOrder = 0) {
  return request.put('/system/org/move', null, {
    params: { id, targetParentId, sortOrder }
  })
}

export function getOrgChildren(parentId: number) {
  return request.get<OrgVO[]>(`/system/org/children/${parentId}`)
}

export function importOrgs(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>('/system/org/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportOrgs(params: OrgQueryDTO = {}) {
  return request.get('/system/org/export', {
    params,
    responseType: 'blob'
  })
}

export function downloadTemplate() {
  return request.get('/system/org/template', {
    responseType: 'blob'
  })
}

export function checkOrgCode(code: string, id?: number) {
  return request.get<boolean>('/system/org/checkCode', {
    params: { code, id }
  })
}
