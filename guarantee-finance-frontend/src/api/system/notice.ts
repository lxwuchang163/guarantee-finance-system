import request from '@/utils/request'

export interface NoticeDTO {
  id?: number
  title: string
  content: string
  noticeType: string
  topFlag: number
  publishTime?: string
  remark?: string
}

export interface NoticePageResult {
  records: NoticeDTO[]
  total: number
  size: number
  current: number
  pages: number
}

export function getNoticePage(params: {
  keyword?: string
  noticeType?: string
  status?: number
  current?: number
  size?: number
}) {
  return request.get<NoticePageResult>('/notice/page', { params })
}

export function getNoticeDetail(id: number) {
  return request.get<NoticeDTO>(`/notice/${id}`)
}

export function createNotice(data: NoticeDTO) {
  return request.post('/notice', data)
}

export function updateNotice(id: number, data: NoticeDTO) {
  return request.put(`/notice/${id}`, data)
}

export function deleteNotice(id: number) {
  return request.delete(`/notice/${id}`)
}

export function publishNotice(id: number) {
  return request.put(`/notice/${id}/publish`)
}

export function unpublishNotice(id: number) {
  return request.put(`/notice/${id}/unpublish`)
}
