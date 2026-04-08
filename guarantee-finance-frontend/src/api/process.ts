import request from '@/utils/request'

export interface ProcessNodeDTO {
  id?: number
  nodeName: string
  nodeType: string
  approverType: string
  approverValue: string
}

export interface ProcessDefinitionVO {
  id: number
  name: string
  businessType: string
  description?: string
  status: number
  version: number
  nodes?: ProcessNodeDTO[]
  createTime?: string
  updateTime?: string
}

export interface ProcessInstanceVO {
  id: number
  definitionId: number
  definitionName?: string
  businessKey: number
  businessType: string
  status: number // 0-进行中 1-待审批 2-已通过 3-已驳回 4-已撤回
  currentNodeIndex: number
  currentNodeId?: number
  currentNodeName?: string
  currentApprover?: string
  initiatorId: number
  initiatorName: string
  initiatorTime: string
  createTime?: string
}

export interface ApproveRecordVO {
  id: number
  instanceId: number
  nodeId: number
  nodeName: string
  approverId: number
  approverName: string
  opinion: string
  result: string
  approveTime: string
}

export interface ProcessDTO {
  id?: number
  name: string
  businessType: string
  description?: string
  status?: number
  nodes?: ProcessNodeDTO[]
}

export function getProcessDefinitionList(params?: { name?: string; status?: number | null }) {
  return request.get<ProcessDefinitionVO[]>('/system/process/definition/list', { params })
}

export function getProcessDefinition(id: number) {
  return request.get<ProcessDefinitionVO>(`/system/process/definition/${id}`)
}

export function addProcessDefinition(data: ProcessDTO) {
  return request.post('/system/process/definition', data)
}

export function updateProcessDefinition(data: ProcessDTO) {
  return request.put('/system/process/definition', data)
}

export function deleteProcessDefinition(id: number) {
  return request.delete(`/system/process/definition/${id}`)
}

export function updateProcessDefStatus(id: number, status: number) {
  return request.put(`/system/process/definition/status/${id}`, null, { params: { status } })
}

export function startProcess(data: { definitionId: number; businessKey: number; businessType: string }) {
  return request.post<ProcessInstanceVO>('/system/process/instance/start', data)
}

export function approveProcess(instanceId: number, opinion: string) {
  return request.put(`/system/process/instance/approve/${instanceId}`, null, { params: { opinion } })
}

export function rejectProcess(instanceId: number, opinion: string) {
  return request.put(`/system/process/instance/reject/${instanceId}`, null, { params: { opinion } })
}

export function withdrawProcess(instanceId: number) {
  return request.put(`/system/process/instance/withdraw/${instanceId}`)
}

export function getMyPending() {
  return request.get<ProcessInstanceVO[]>('/system/process/instance/myPending')
}

export function getMyInitiated() {
  return request.get<ProcessInstanceVO[]>('/system/process/instance/myInitiated')
}

export function getApproveHistory(instanceId: number) {
  return request.get<ApproveRecordVO[]>(`/system/process/instance/history/${instanceId}`)
}

export function getInstanceDetail(instanceId: number) {
  return request.get<ProcessInstanceVO>(`/system/process/instance/${instanceId}`)
}
