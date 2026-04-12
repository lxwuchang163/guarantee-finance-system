import request from '@/utils/request'

export interface DashboardStats {
  todayIncome: number
  todayExpense: number
  pendingDocuments: number
  pendingReceipts: number
  pendingPayments: number
  pendingVouchers: number
  monthlyReconciliation: number
  completedReconciliation: number
  reconciliationRate: number
  bankAccounts: number
  monthIncome: number
  monthExpense: number
  totalReceipts: number
  totalPayments: number
  totalVouchers: number
  changeRates: {
    todayIncome: string
    todayExpense: string
    pendingDocuments: string
    monthlyReconciliation: string
  }
}

export interface TodoItem {
  id: number
  title: string
  type: string
  priority: string
  status: string
  createTime: string
  displayTime: string
  description: string
  businessId: number
  businessType: string
  actionUrl: string
}

export interface Notice {
  id: number
  title: string
  content: string
  createTime: string
  status: number
}

export interface IncomeExpenseData {
  month: string
  income: number
  expense: number
}

export interface BusinessTypeData {
  type: string
  value: number
  percentage: number
}

export function getDashboardStats() {
  return request.get<DashboardStats>('/dashboard/stats')
}

export function getTodoList() {
  return request.get<TodoItem[]>('/dashboard/todos')
}

export function processTodo(id: number) {
  return request.post(`/dashboard/todos/${id}/process`)
}

export function getTodoDetail(id: number) {
  return request.get<TodoItem>(`/dashboard/todos/${id}`)
}

export function getNoticeList() {
  return request.get<Notice[]>('/dashboard/notices')
}

export function getNoticeDetail(id: number) {
  return request.get<Notice>(`/dashboard/notices/${id}`)
}

export function getIncomeExpenseData() {
  return request.get<IncomeExpenseData[]>('/dashboard/business/income-expense')
}

export function getBusinessTypeData() {
  return request.get<BusinessTypeData[]>('/dashboard/business/type-distribution')
}
