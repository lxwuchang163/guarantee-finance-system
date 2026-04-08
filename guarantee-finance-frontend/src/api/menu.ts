import request from '@/utils/request'

export interface MenuTreeVO {
  id: number
  menuName: string
  parentId: number
  sortOrder: number
  path?: string
  component?: string
  icon?: string
  permission?: string
  menuType: number // 1-目录 2-菜单 3-按钮
  status: number
  children?: MenuTreeVO[]
}

export interface DataScopeVO {
  roleId: number
  dataScope: string // ALL, ORG_AND_CHILD, ORG_ONLY, CUSTOM, SELF
  orgCodes?: string[]
}

export function getMenuTree() {
  return request.get<MenuTreeVO[]>('/system/menu/tree')
}

export function getRoleMenus(roleId: number) {
  return request.get<number[]>(`/system/menu/role/${roleId}`)
}

export function assignMenuPermissions(roleId: number, menuIds: number[]) {
  return request.post('/system/menu/assign', menuIds, { params: { roleId } })
}

export function getDataScope(roleId: number) {
  return request.get<DataScopeVO>(`/system/menu/dataScope/${roleId}`)
}

export function setDataScope(data: { roleId: number; dataScope: string; orgCodes?: string[] }) {
  return request.put('/system/menu/dataScope', data)
}
