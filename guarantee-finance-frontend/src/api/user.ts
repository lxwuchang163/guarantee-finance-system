import request from '@/utils/request'

export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  sex: number
  orgId: number | null
  orgName?: string
  status: number
  roles?: RoleSimpleVO[]
  createTime?: string
  updateTime?: string
  remark?: string
}

export interface RoleSimpleVO {
  id: number
  roleName: string
  roleCode: string
}

export interface OnlineUserVO {
  token: string
  userId: number
  username: string
  nickname: string
  ipaddr: string
  loginLocation: string
  browser: string
  os: string
  loginTime: string
}

export interface UserDTO {
  id?: number
  username: string
  password?: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  sex?: number
  orgId?: number | null
  roleIds?: number[]
  status?: number
  remark?: string
}

export interface UserQueryDTO {
  username?: string
  nickname?: string
  phone?: string
  orgId?: number | null
  status?: number | null
  sex?: number | null
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export function getUserPage(params: UserQueryDTO & { current: number; size: number }) {
  return request.get<PageResult<UserVO>>('/system/user/page', { params })
}

export function getUserDetail(id: number) {
  return request.get<UserVO>(`/system/user/${id}`)
}

export function addUser(data: UserDTO) {
  return request.post('/system/user', data)
}

export function updateUser(data: UserDTO) {
  return request.put('/system/user', data)
}

export function deleteUser(id: number) {
  return request.delete(`/system/user/${id}`)
}

export function resetUserPassword(id: number, password = '123456') {
  return request.put(`/system/user/resetPwd/${id}`, null, { params: { password } })
}

export function unlockUser(id: number) {
  return request.put(`/system/user/unlock/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return request.put(`/system/user/status/${id}`, null, { params: { status } })
}

export function importUsers(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>('/system/user/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadUserTemplate() {
  return request.get('/system/user/template', {
    responseType: 'blob'
  })
}

export function getOnlineUsers() {
  return request.get<OnlineUserVO[]>('/system/user/online')
}

export function forceLogout(token: string) {
  return request.delete(`/system/user/online/${token}`)
}

export function checkUsernameUnique(username: string, id?: number) {
  return request.get<boolean>('/system/user/checkUsername', {
    params: { username, id }
  })
}

export function checkPhoneUnique(phone: string, id?: number) {
  return request.get<boolean>('/system/user/checkPhone', {
    params: { phone, id }
  })
}
