import request from '@/utils/request'

// 仪表盘统计数据接口
export interface DashboardStats {
  todayIncome: number
  todayExpense: number
  pendingDocuments: number
  monthlyReconciliation: number
  reconciliationRate: number
  changeRates: {
    todayIncome: string
    todayExpense: string
    pendingDocuments: string
    monthlyReconciliation: string
  }
}

// 待办事项接口
export interface TodoItem {
  id: number
  title: string
  type: string
  priority: string
  status: string
  createTime: string
  description: string
}

// 系统公告接口
export interface Notice {
  id: number
  title: string
  content: string
  createTime: string
  status: number
}

// 收支趋势数据接口
export interface IncomeExpenseData {
  month: string
  income: number
  expense: number
}

// 业务类型分布数据接口
export interface BusinessTypeData {
  type: string
  value: number
  percentage: number
}

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request.get<DashboardStats>('/dashboard/stats')
}

// 获取待办事项列表
export function getTodoList() {
  return request.get<TodoItem[]>('/dashboard/todos')
}

// 处理待办事项
export function processTodo(id: number) {
  return request.post(`/dashboard/todos/${id}/process`)
}

// 获取待办事项详情
export function getTodoDetail(id: number) {
  return request.get<TodoItem>(`/dashboard/todos/${id}`)
}

// 获取系统公告列表
export function getNoticeList() {
  return request.get<Notice[]>('/dashboard/notices')
}

// 获取公告详情
export function getNoticeDetail(id: number) {
  return request.get<Notice>(`/dashboard/notices/${id}`)
}

// 获取收支趋势数据
export function getIncomeExpenseData() {
  return request.get<IncomeExpenseData[]>('/dashboard/business/income-expense')
}

// 获取业务类型分布数据
export function getBusinessTypeData() {
  return request.get<BusinessTypeData[]>('/dashboard/business/type-distribution')
}