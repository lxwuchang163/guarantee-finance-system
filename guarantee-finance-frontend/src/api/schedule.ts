import request from '@/utils/request'

export interface ScheduleJobVO {
  id: number
  jobName: string
  jobGroup: string
  beanName: string
  methodName: string
  methodParams?: string
  cronExpression: string
  misfirePolicy: number
  concurrent: number
  status: string // 0-暂停 1-正常
  nextExecuteTime?: string
  prevExecuteTime?: string
  remark?: string
  createTime?: string
}

export interface ScheduleJobLogVO {
  id: number
  jobId: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  jobMessage: string
  status: number // 0-成功 1-失败
  exceptionInfo?: string
  startTime: number
  endTime: number
  duration?: number
  createTime?: string
}

export interface ScheduleTaskConfigVO {
  id: number
  taskCode: string
  taskName: string
  description: string
  cronExpression: string
  executeOrder: number
  status: string
  lastExecuteTime?: string
  nextExecuteTime?: string
  successCount: number
  failCount: number
  remark?: string
}

export function getScheduleDashboard() {
  return request.get<{ totalJobs: number; runningJobs: number; pausedJobs: number; todaySuccess: number; todayFail: number; recentLogs: any[] }>('/api/schedule/dashboard')
}

export function getJobPage(params: {
  jobName?: string; status?: string; current: number; size: number
}) {
  return request.get('/api/schedule/job/list', { params })
}

export function getJobDetail(id: number) {
  return request.get<ScheduleJobVO>(`/api/schedule/job/${id}`)
}

export function createJob(data: Partial<ScheduleJobVO>) {
  return request.post<number>('/api/schedule/job', data)
}

export function updateJob(data: Partial<ScheduleJobVO>) {
  return request.put('/api/schedule/job', data)
}

export function deleteJob(id: number) {
  return request.delete(`/api/schedule/job/${id}`)
}

export function changeJobStatus(id: number, status: string) {
  return request.put(`/api/schedule/job/${id}/status`, null, { params: { status } })
}

export function executeOnce(id: number) {
  return request.post(`/api/schedule/job/${id}/execute`)
}

export function getLogPage(params: {
  jobId?: number; status?: number | null; current: number; size: number
}) {
  return request.get('/api/schedule/log/list', { params })
}

export function getBuiltInTasks() {
  return request.get<ScheduleTaskConfigVO[]>('/api/schedule/built-in/tasks')
}

export function updateBuiltInTask(taskCode: string, cronExpression?: string, status?: string) {
  return request.put(`/api/schedule/built-in/task/${taskCode}/config`, null, { params: { cronExpression, status } })
}

export function initBuiltInTasks() {
  return request.post('/api/schedule/built-in/init')
}
