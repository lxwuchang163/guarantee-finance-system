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

export interface WechatQrCodeResult {
  ticket: string
  qrCodeUrl: string
}

export interface WechatLoginStatus {
  status: string
  message: string
  userId?: number
}

export interface WechatLoginResult {
  success: boolean
  userId: number
  nickname: string
  avatar: string
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

// 短信验证码相关
export function sendSmsCode(phone: string) {
  return request.post('/auth/sendSms', { phone })
}

export function loginBySms(phone: string, code: string) {
  return request.post<LoginResult>('/auth/loginBySms', { phone, code })
}

// 微信登录相关
export function getWechatQrCode() {
  return request.get<WechatQrCodeResult>('/auth/wechat/qrCode')
}

export function checkWechatLoginStatus(ticket: string) {
  return request.get<WechatLoginStatus>('/auth/wechat/checkStatus', { params: { ticket } })
}

export function wechatCallback(code: string) {
  return request.get<WechatLoginResult>('/auth/wechat/callback', { params: { code } })
}
