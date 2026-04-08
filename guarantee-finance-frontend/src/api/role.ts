import request from '@/utils/request'

export interface RoleVO {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  sortOrder: number
  menuIds?: number[]
  createTime?: string
  updateTime?: string
  remark?: string
}

export interface RoleSimpleVO {
  id: number
  roleName: string
  roleCode: string
}

export interface RoleCompareVO {
  sourceRole: RoleVO
  targetRole: RoleVO
  differences: RoleDiffVO[]
}

export interface RoleDiffVO {
  field: string
  fieldName: string
  sourceValue: string
  targetValue: string
  diffType: string
}

export interface RoleDTO {
  id?: number
  roleName: string
  roleCode: string
  description?: string
  status?: number
  sortOrder?: number
  menuIds?: number[]
  remark?: string
}

export interface RoleCopyDTO {
  sourceRoleId: number
  newRoleName: string
  newRoleCode: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export function getAllRoles() {
  return request.get<RoleSimpleVO[]>('/system/role/list')
}

export function getRolePage(params: { current: number; size: number; roleName?: string; roleCode?: string; status?: number | null }) {
  return request.get<PageResult<RoleVO>>('/system/role/page', { params })
}

export function getRoleDetail(id: number) {
  return request.get<RoleVO>(`/system/role/${id}`)
}

export function addRole(data: RoleDTO) {
  return request.post('/system/role', data)
}

export function updateRole(data: RoleDTO) {
  return request.put('/system/role', data)
}

export function deleteRole(id: number) {
  return request.delete(`/system/role/${id}`)
}

export function copyRole(data: RoleCopyDTO) {
  return request.post<RoleVO>('/system/role/copy', data)
}

export function compareRoles(sourceRoleId: number, targetRoleId: number) {
  return request.get<RoleCompareVO>('/system/role/compare', {
    params: { sourceRoleId, targetRoleId }
  })
}

export function updateRoleStatus(id: number, status: number) {
  return request.put(`/system/role/status/${id}`, null, { params: { status } })
}

export function initPresetRoles() {
  return request.post('/system/role/initPreset')
}

export function checkRoleCode(code: string, id?: number) {
  return request.get<boolean>('/system/role/checkCode', {
    params: { code, id }
  })
}
