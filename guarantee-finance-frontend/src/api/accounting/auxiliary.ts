import request from '@/utils/request'

export interface AuxiliaryDimensionVO {
  id: number
  dimensionCode: string
  dimensionName: string
  dimensionType: string
  description: string
  status: number
  sortOrder: number
  parentCode: string
  dimensionLevel: number
  createTime: string
  updateTime: string
  children?: AuxiliaryDimensionVO[]
}

export interface AuxiliaryDimensionDTO {
  dimensionCode: string
  dimensionName: string
  dimensionType: string
  description: string
  sortOrder: number
  parentCode: string
  dimensionLevel: number
}

export function getDimensionPage(params: {
  dimensionCode?: string
  dimensionName?: string
  dimensionType?: string
  status?: number
  page: number
  size: number
}) {
  return request.get('/accounting/auxiliary/page', { params })
}

export function getDimensionDetail(id: number) {
  return request.get(`/accounting/auxiliary/detail/${id}`)
}

export function createDimension(data: AuxiliaryDimensionDTO) {
  return request.post('/accounting/auxiliary', data)
}

export function updateDimension(id: number, data: AuxiliaryDimensionDTO) {
  return request.put(`/accounting/auxiliary/${id}`, data)
}

export function deleteDimension(id: number) {
  return request.delete(`/accounting/auxiliary/${id}`)
}

export function changeDimensionStatus(id: number, status: number) {
  return request.put(`/accounting/auxiliary/${id}/status`, { status })
}

export function getDimensionTree(dimensionType?: string) {
  return request.get('/accounting/auxiliary/tree', { params: { dimensionType } })
}

export function getEnabledDimensions(dimensionType?: string) {
  return request.get('/accounting/auxiliary/enabled', { params: { dimensionType } })
}

export function checkDimensionCode(dimensionCode: string, id?: number) {
  return request.get('/accounting/auxiliary/check-code', { params: { dimensionCode, id } })
}
