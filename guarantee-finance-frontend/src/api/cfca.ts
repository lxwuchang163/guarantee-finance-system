import request from '@/utils/request'

export interface CfcaCertificateVO {
  id: number
  certNo: string
  certSubject: string
  certIssuer: string
  validFrom: string
  validTo: string
  status: number // 0-已过期 1-正常 2-已吊销 3-待更新
  statusText?: string
  keyLength: number
  algorithm: string
  ownerName: string
  ownerDept?: string
  daysUntilExpiry?: number
  isExpiringSoon?: boolean
  lastSignTime?: string
  signCount: number
  remark?: string
  createTime?: string
}

export interface CfcaSignDTO {
  paymentId: number
  paymentNo: string
  amount: number
  signLevel: number // 1-单签 2-双签 3-三签+审批
}

export interface CfcaSignResult {
  success: boolean
  signatureData?: string
  signTime?: string
  message?: string
}

export function getCertificateList() {
  return request.get<CfcaCertificateVO[]>('/api/cfca/certificates')
}

export function signPayment(data: CfcaSignDTO) {
  return request.post<CfcaSignResult>('/api/cfca/sign', data)
}

export function verifySignature(signatureData: string) {
  return request.post<boolean>('/api/cfca/verify', { signatureData })
}

export function checkCertExpiry() {
  return request.get('/api/cfca/expiry-check')
}

export function refreshCerts() {
  return request.put('/api/cfca/refresh')
}
