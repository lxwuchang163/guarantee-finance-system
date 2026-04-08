import request from '@/utils/request'

export interface LoginData {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
}

export function login(data: LoginData) {
  return request.post<LoginResult>('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get<UserInfo>('/auth/userinfo')
}
