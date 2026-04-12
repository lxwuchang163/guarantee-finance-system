<template>
  <div class="dashboard-container">
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="8" :md="4" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-change">
                <span class="change-value" :class="item.trend">
                  {{ item.change }}
                  <el-icon size="12">
                    <CaretTop v-if="item.trend === 'up'" />
                    <CaretBottom v-else />
                  </el-icon>
                </span>
              </div>
            </div>
            <el-icon size="40" :style="{ color: item.color }">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="pending-section">
      <el-col :xs="12" :sm="12" :md="6" v-for="item in pendingCards" :key="item.title">
        <el-card shadow="hover" class="pending-card">
          <div class="pending-header">
            <el-icon size="20" :style="{ color: item.color }">
              <component :is="item.icon" />
            </el-icon>
            <span class="pending-title">{{ item.title }}</span>
          </div>
          <div class="pending-body">
            <div class="pending-count">{{ item.count }}</div>
            <div class="pending-desc">{{ item.desc }}</div>
          </div>
          <div class="pending-footer">
            <span class="pending-detail" v-for="(d, i) in item.details" :key="i">
              {{ d.label }}: <strong>{{ d.value }}</strong>
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :sm="24" :md="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-tag size="small" type="danger">{{ pendingTodoCount }} 项待处理</el-tag>
            </div>
          </template>
          <el-table :data="todoList" stripe size="small" max-height="360">
            <el-table-column prop="title" label="事项名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="getTodoTypeTag(row.type)">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="getPriorityType(row.priority)">
                  {{ getPriorityText(row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="displayTime" label="创建时间" width="160" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 'pending' ? 'warning' : 'success'">
                  {{ row.status === 'pending' ? '待处理' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'pending'" type="primary" size="small" link @click="handleProcess(row)">处理</el-button>
                <el-button type="info" size="small" link @click="handleViewTodo(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="hover">
          <template #header>
            <span>系统公告</span>
          </template>
          <div class="notice-list">
            <div v-for="(notice, index) in noticeList" :key="index" class="notice-item" @click="viewNoticeDetail(notice)">
              <el-tag size="small" type="info">公告</el-tag>
              <span class="notice-title">{{ notice.title }}</span>
              <span class="notice-time">{{ formatNoticeTime(notice.createTime) }}</span>
              <el-icon size="16" class="notice-arrow">
                <ArrowRight />
              </el-icon>
            </div>
            <el-empty v-if="noticeList.length === 0" description="暂无公告" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <span>收支趋势</span>
          </template>
          <div ref="incomeExpenseChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <span>业务类型分布</span>
          </template>
          <div ref="businessTypeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="noticeDialogVisible" title="公告详情" width="500px">
      <div v-if="selectedNotice" class="notice-detail">
        <h3>{{ selectedNotice.title }}</h3>
        <div class="notice-time">{{ formatNoticeTime(selectedNotice.createTime) }}</div>
        <div class="notice-content">{{ selectedNotice.content }}</div>
      </div>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="todoDialogVisible" title="待办详情" width="500px">
      <div v-if="selectedTodo" class="todo-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="事项名称">{{ selectedTodo.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag size="small" :type="getTodoTypeTag(selectedTodo.type)">{{ selectedTodo.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag size="small" :type="getPriorityType(selectedTodo.priority)">{{ getPriorityText(selectedTodo.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="selectedTodo.status === 'pending' ? 'warning' : 'success'">
              {{ selectedTodo.status === 'pending' ? '待处理' : '已完成' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述">{{ selectedTodo.description }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedTodo.displayTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="todoDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { CaretTop, CaretBottom, ArrowRight, Wallet, CreditCard, Document, Finished, Link, Tickets, DataBoard } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboardStats, getTodoList, getNoticeList, getIncomeExpenseData, getBusinessTypeData, processTodo } from '@/api/dashboard'
import type { DashboardStats, TodoItem, Notice } from '@/api/dashboard'

const loading = ref(false)

const statCards = ref([
  { title: '今日收款', value: '¥0', icon: Wallet, color: '#409eff', change: '0%', trend: 'up' },
  { title: '今日付款', value: '¥0', icon: CreditCard, color: '#f56c6c', change: '0%', trend: 'down' },
  { title: '本月收款', value: '¥0', icon: DataBoard, color: '#67c23a', change: '0%', trend: 'up' },
  { title: '本月付款', value: '¥0', icon: DataBoard, color: '#e6a23c', change: '0%', trend: 'down' },
  { title: '待审核单据', value: '0', icon: Document, color: '#909399', change: '0', trend: 'up' },
  { title: '银行账户', value: '0', icon: Link, color: '#409eff', change: '', trend: 'up' }
])

const pendingCards = ref([
  { title: '收款代办', count: 0, desc: '待处理收款单', icon: Wallet, color: '#409eff', details: [{ label: '待提交', value: 0 }, { label: '待审核', value: 0 }] },
  { title: '付款代办', count: 0, desc: '待处理付款单', icon: CreditCard, color: '#f56c6c', details: [{ label: '待提交', value: 0 }, { label: '待审核', value: 0 }] },
  { title: '对账代办', count: 0, desc: '待对账记录', icon: Finished, color: '#e6a23c', details: [{ label: '本月对账', value: 0 }, { label: '完成率', value: '0%' }] },
  { title: '银企直连代办', count: 0, desc: '待连接账户', icon: Link, color: '#67c23a', details: [{ label: '已连接', value: 0 }, { label: '凭证审核', value: 0 }] }
])

const todoList = ref<TodoItem[]>([])
const noticeList = ref<Notice[]>([])
const incomeExpenseData = ref([])
const businessTypeData = ref([])

const noticeDialogVisible = ref(false)
const selectedNotice = ref<Notice | null>(null)
const todoDialogVisible = ref(false)
const selectedTodo = ref<TodoItem | null>(null)

const incomeExpenseChart = ref(null)
const businessTypeChart = ref(null)
let incomeExpenseChartInstance: echarts.ECharts | null = null
let businessTypeChartInstance: echarts.ECharts | null = null

const pendingTodoCount = computed(() => todoList.value.filter(t => t.status === 'pending').length)

const formatNoticeTime = (time: string) => {
  if (!time) return ''
  return time.split(' ')[0]
}

const formatAmount = (val: number) => {
  if (val === undefined || val === null) return '¥0'
  return '¥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

const viewNoticeDetail = (notice: Notice) => {
  selectedNotice.value = notice
  noticeDialogVisible.value = true
}

const handleViewTodo = (row: TodoItem) => {
  selectedTodo.value = row
  todoDialogVisible.value = true
}

const handleProcess = async (row: TodoItem) => {
  try {
    await processTodo(row.id)
    ElMessage.success('处理成功')
    row.status = 'done'
  } catch (err) {
    console.error('处理待办失败:', err)
  }
}

const getTodoTypeTag = (type: string) => {
  switch (type) {
    case '收款': return 'primary'
    case '付款': return 'danger'
    case '对账': return 'warning'
    case '凭证': return 'success'
    case '银企直连': return 'info'
    default: return 'info'
  }
}

const getPriorityType = (priority: string) => {
  switch (priority) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    default: return 'info'
  }
}

const getPriorityText = (priority: string) => {
  switch (priority) {
    case 'high': return '高'
    case 'medium': return '中'
    default: return '低'
  }
}

const loadStats = async () => {
  try {
    const response = await getDashboardStats()
    const data = response.data as DashboardStats
    
    statCards.value = [
      { title: '今日收款', value: formatAmount(data.todayIncome), icon: Wallet, color: '#409eff', change: data.changeRates.todayIncome, trend: data.changeRates.todayIncome.startsWith('+') ? 'up' : 'down' },
      { title: '今日付款', value: formatAmount(data.todayExpense), icon: CreditCard, color: '#f56c6c', change: data.changeRates.todayExpense, trend: data.changeRates.todayExpense.startsWith('+') ? 'up' : 'down' },
      { title: '本月收款', value: formatAmount(data.monthIncome), icon: DataBoard, color: '#67c23a', change: '+0%', trend: 'up' },
      { title: '本月付款', value: formatAmount(data.monthExpense), icon: DataBoard, color: '#e6a23c', change: '+0%', trend: 'down' },
      { title: '待审核单据', value: String(data.pendingDocuments), icon: Document, color: '#909399', change: data.changeRates.pendingDocuments, trend: 'up' },
      { title: '银行账户', value: String(data.bankAccounts), icon: Link, color: '#409eff', change: '', trend: 'up' }
    ]

    pendingCards.value = [
      { title: '收款代办', count: data.pendingReceipts, desc: '待处理收款单', icon: Wallet, color: '#409eff', details: [{ label: '总收款单', value: data.totalReceipts }, { label: '待处理', value: data.pendingReceipts }] },
      { title: '付款代办', count: data.pendingPayments, desc: '待处理付款单', icon: CreditCard, color: '#f56c6c', details: [{ label: '总付款单', value: data.totalPayments }, { label: '待处理', value: data.pendingPayments }] },
      { title: '对账代办', count: data.monthlyReconciliation - data.completedReconciliation, desc: '待对账记录', icon: Finished, color: '#e6a23c', details: [{ label: '本月对账', value: data.monthlyReconciliation }, { label: '完成率', value: data.reconciliationRate + '%' }] },
      { title: '银企直连代办', count: data.pendingVouchers, desc: '待审核凭证', icon: Link, color: '#67c23a', details: [{ label: '已连接账户', value: data.bankAccounts }, { label: '凭证审核', value: data.pendingVouchers }] }
    ]
  } catch (err) {
    console.error('加载统计数据失败:', err)
  }
}

const loadTodos = async () => {
  try {
    const response = await getTodoList()
    todoList.value = response.data
  } catch (err) {
    console.error('加载待办事项失败:', err)
  }
}

const loadNotices = async () => {
  try {
    const response = await getNoticeList()
    noticeList.value = response.data
  } catch (err) {
    console.error('加载系统公告失败:', err)
  }
}

const initIncomeExpenseChart = () => {
  if (!incomeExpenseChart.value) return
  incomeExpenseChartInstance = echarts.init(incomeExpenseChart.value)
  const months = incomeExpenseData.value.map((item: any) => item.month)
  const incomes = incomeExpenseData.value.map((item: any) => item.income)
  const expenses = incomeExpenseData.value.map((item: any) => item.expense)
  const maxValue = Math.max(...incomes, ...expenses) * 1.2

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross', crossStyle: { color: '#999' } } },
    legend: { data: ['收入', '支出'] },
    xAxis: [{ type: 'category', data: months.length > 0 ? months : ['1月', '2月', '3月', '4月', '5月', '6月'], axisPointer: { type: 'shadow' } }],
    yAxis: [{ type: 'value', name: '金额', min: 0, max: maxValue > 0 ? maxValue : 500000, axisLabel: { formatter: '¥{value}' } }],
    series: [
      { name: '收入', type: 'bar', data: incomes.length > 0 ? incomes : [0, 0, 0, 0, 0, 0], itemStyle: { color: '#67c23a' } },
      { name: '支出', type: 'bar', data: expenses.length > 0 ? expenses : [0, 0, 0, 0, 0, 0], itemStyle: { color: '#f56c6c' } }
    ]
  }
  incomeExpenseChartInstance.setOption(option)
}

const initBusinessTypeChart = () => {
  if (!businessTypeChart.value) return
  businessTypeChartInstance = echarts.init(businessTypeChart.value)
  const chartData = businessTypeData.value.map((item: any) => ({ value: item.value, name: item.type }))
  const legendData = businessTypeData.value.map((item: any) => item.type)

  const option = {
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 10, data: legendData.length > 0 ? legendData : ['担保业务', '贷款业务', '理财业务', '其他业务'] },
    series: [{
      name: '业务类型', type: 'pie', radius: '70%', center: ['60%', '50%'],
      data: chartData.length > 0 ? chartData : [
        { value: 45, name: '担保业务' }, { value: 30, name: '贷款业务' },
        { value: 20, name: '理财业务' }, { value: 5, name: '其他业务' }
      ],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  }
  businessTypeChartInstance.setOption(option)
}

const loadChartData = async () => {
  try {
    const incomeExpenseResponse = await getIncomeExpenseData()
    incomeExpenseData.value = incomeExpenseResponse.data
    const businessTypeResponse = await getBusinessTypeData()
    businessTypeData.value = businessTypeResponse.data
    initIncomeExpenseChart()
    initBusinessTypeChart()
  } catch (err) {
    console.error('加载图表数据失败:', err)
  }
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    await Promise.all([loadStats(), loadTodos(), loadNotices(), loadChartData()])
  } catch (err) {
    console.error('加载仪表盘数据失败:', err)
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  incomeExpenseChartInstance?.resize()
  businessTypeChartInstance?.resize()
}

let refreshTimer: number | null = null

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadDashboardData()
  refreshTimer = window.setInterval(() => loadDashboardData(), 30000)
})

onUnmounted(() => {
  incomeExpenseChartInstance?.dispose()
  businessTypeChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  .stat-cards {
    .stat-card {
      .stat-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        .stat-info {
          .stat-title {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }
          .stat-value {
            font-size: 22px;
            font-weight: bold;
            color: #303133;
          }
          .stat-change {
            margin-top: 4px;
            .change-value {
              font-size: 12px;
              &.up { color: #67c23a; }
              &.down { color: #f56c6c; }
            }
          }
        }
      }
    }
  }

  .pending-section {
    margin-top: 16px;
    .pending-card {
      .pending-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        .pending-title {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
      }
      .pending-body {
        text-align: center;
        padding: 8px 0;
        .pending-count {
          font-size: 36px;
          font-weight: bold;
          color: #303133;
        }
        .pending-desc {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
      .pending-footer {
        display: flex;
        justify-content: space-around;
        padding-top: 12px;
        border-top: 1px solid #f0f0f0;
        .pending-detail {
          font-size: 12px;
          color: #606266;
          strong {
            color: #303133;
            font-size: 14px;
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .notice-list {
    .notice-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.3s;
      &:hover { background-color: #f5f7fa; }
      &:last-child { border-bottom: none; }
      .notice-title {
        flex: 1;
        color: #333;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .notice-time { color: #999; font-size: 12px; }
      .notice-arrow { color: #999; transition: color 0.3s; }
      &:hover .notice-arrow { color: #409eff; }
    }
  }

  .notice-detail {
    h3 { margin: 0 0 16px 0; color: #333; }
    .notice-time { color: #999; font-size: 12px; margin-bottom: 16px; }
    .notice-content { color: #666; line-height: 1.6; }
  }

  .chart-container {
    height: 300px;
    width: 100%;
  }
}
</style>
