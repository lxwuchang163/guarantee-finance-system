<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="12" :md="6" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-change">
                <span class="change-value" :class="item.trend">
                  {{ item.change }}
                  <el-icon size="14">
                    <CaretTop v-if="item.trend === 'up'" />
                    <CaretBottom v-else />
                  </el-icon>
                </span>
              </div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
            <el-icon size="48">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="16">
        <el-card shadow="hover">
          <template #header>
            <span>待办事项</span>
          </template>
          <el-table :data="todoList" stripe size="small">
            <el-table-column prop="title" label="事项名称" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="priority" label="优先级" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getPriorityType(row.priority)">
                  {{ getPriorityText(row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 'pending' ? 'warning' : 'success'">
                  {{ row.status === 'pending' ? '待处理' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button v-if="row.status === 'pending'" type="primary" size="small" link>处理</el-button>
                <el-button type="info" size="small" link>查看</el-button>
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
              <span class="notice-time">{{ notice.time }}</span>
              <el-icon size="16" class="notice-arrow">
                <ArrowRight />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
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

    <!-- 公告详情对话框 -->
    <el-dialog v-model="noticeDialogVisible" title="公告详情" width="500px">
      <div v-if="selectedNotice" class="notice-detail">
        <h3>{{ selectedNotice.title }}</h3>
        <div class="notice-time">{{ selectedNotice.time }}</div>
        <div class="notice-content">{{ selectedNotice.content }}</div>
      </div>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { CaretTop, CaretBottom, ArrowRight, Wallet, CreditCard, Document, Finished } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardStats, getTodoList, getNoticeList, getIncomeExpenseData, getBusinessTypeData } from '@/api/dashboard'
import type { DashboardStats, TodoItem, Notice, IncomeExpenseData as IncomeExpenseDataType, BusinessTypeData as BusinessTypeDataType } from '@/api/dashboard'

// 加载状态
const loading = ref(false)
const error = ref('')

// 统计卡片数据
const statCards = ref([
  { title: '今日收款', value: '¥0', icon: Wallet, color: '#409eff', change: '0%', trend: 'up' },
  { title: '今日付款', value: '¥0', icon: CreditCard, color: '#f56c6c', change: '0%', trend: 'down' },
  { title: '待审核单据', value: '0', icon: Document, color: '#e6a23c', change: '0', trend: 'up' },
  { title: '本月对账', value: '0', icon: Finished, color: '#67c23a', change: '0%', trend: 'up' }
])

// 待办事项数据
const todoList = ref([])

// 系统公告数据
const noticeList = ref([])

// 收支趋势数据
const incomeExpenseData = ref([])

// 业务类型分布数据
const businessTypeData = ref([])

// 公告详情相关
const noticeDialogVisible = ref(false)
const selectedNotice = ref(null)

// 图表引用
const incomeExpenseChart = ref(null)
const businessTypeChart = ref(null)

// 图表实例
let incomeExpenseChartInstance = null
let businessTypeChartInstance = null

// 查看公告详情
const viewNoticeDetail = (notice) => {
  selectedNotice.value = notice
  noticeDialogVisible.value = true
}

// 获取优先级类型
const getPriorityType = (priority) => {
  switch (priority) {
    case 'high':
      return 'danger'
    case 'medium':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取优先级文本
const getPriorityText = (priority) => {
  switch (priority) {
    case 'high':
      return '高'
    case 'medium':
      return '中'
    default:
      return '低'
  }
}

// 加载仪表盘统计数据
const loadStats = async () => {
  try {
    const response = await getDashboardStats()
    const data = response.data
    
    statCards.value = [
      {
        title: '今日收款',
        value: `¥${data.todayIncome.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`,
        icon: Wallet,
        color: '#409eff',
        change: data.changeRates.todayIncome,
        trend: data.changeRates.todayIncome.startsWith('+') ? 'up' : 'down'
      },
      {
        title: '今日付款',
        value: `¥${data.todayExpense.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`,
        icon: CreditCard,
        color: '#f56c6c',
        change: data.changeRates.todayExpense,
        trend: data.changeRates.todayExpense.startsWith('+') ? 'up' : 'down'
      },
      {
        title: '待审核单据',
        value: data.pendingDocuments.toString(),
        icon: Document,
        color: '#e6a23c',
        change: data.changeRates.pendingDocuments,
        trend: data.changeRates.pendingDocuments.startsWith('+') ? 'up' : 'down'
      },
      {
        title: '本月对账',
        value: data.monthlyReconciliation.toString(),
        icon: Finished,
        color: '#67c23a',
        change: data.changeRates.monthlyReconciliation,
        trend: 'up'
      }
    ]
  } catch (err) {
    console.error('加载统计数据失败:', err)
    error.value = '加载统计数据失败'
  }
}

// 加载待办事项数据
const loadTodos = async () => {
  try {
    const response = await getTodoList()
    todoList.value = response.data
  } catch (err) {
    console.error('加载待办事项失败:', err)
    error.value = '加载待办事项失败'
  }
}

// 加载系统公告数据
const loadNotices = async () => {
  try {
    const response = await getNoticeList()
    noticeList.value = response.data.map(notice => ({
      title: notice.title,
      time: notice.createTime.split(' ')[0],
      content: notice.content,
      id: notice.id
    }))
  } catch (err) {
    console.error('加载系统公告失败:', err)
    error.value = '加载系统公告失败'
  }
}

// 初始化收支趋势图表
const initIncomeExpenseChart = () => {
  if (!incomeExpenseChart.value) return
  
  incomeExpenseChartInstance = echarts.init(incomeExpenseChart.value)
  
  // 使用从API获取的数据
  const months = incomeExpenseData.value.map(item => item.month)
  const incomes = incomeExpenseData.value.map(item => item.income)
  const expenses = incomeExpenseData.value.map(item => item.expense)
  
  // 计算最大值，用于设置y轴范围
  const maxValue = Math.max(...incomes, ...expenses) * 1.2
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      data: ['收入', '支出']
    },
    xAxis: [
      {
        type: 'category',
        data: months.length > 0 ? months : ['1月', '2月', '3月', '4月', '5月', '6月'],
        axisPointer: {
          type: 'shadow'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '金额',
        min: 0,
        max: maxValue > 0 ? maxValue : 500000,
        axisLabel: {
          formatter: '¥{value}'
        }
      }
    ],
    series: [
      {
        name: '收入',
        type: 'bar',
        data: incomes.length > 0 ? incomes : [320000, 332000, 301000, 334000, 390000, 330000],
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '支出',
        type: 'bar',
        data: expenses.length > 0 ? expenses : [280000, 292000, 251000, 294000, 330000, 280000],
        itemStyle: {
          color: '#f56c6c'
        }
      }
    ]
  }
  
  incomeExpenseChartInstance.setOption(option)
}

// 初始化业务类型分布图表
const initBusinessTypeChart = () => {
  if (!businessTypeChart.value) return
  
  businessTypeChartInstance = echarts.init(businessTypeChart.value)
  
  // 使用从API获取的数据
  const chartData = businessTypeData.value.map(item => ({
    value: item.value,
    name: item.type
  }))
  const legendData = businessTypeData.value.map(item => item.type)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: legendData.length > 0 ? legendData : ['担保业务', '贷款业务', '理财业务', '其他业务']
    },
    series: [
      {
        name: '业务类型',
        type: 'pie',
        radius: '70%',
        center: ['60%', '50%'],
        data: chartData.length > 0 ? chartData : [
          { value: 45, name: '担保业务' },
          { value: 30, name: '贷款业务' },
          { value: 20, name: '理财业务' },
          { value: 5, name: '其他业务' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  businessTypeChartInstance.setOption(option)
}

// 加载图表数据
const loadChartData = async () => {
  try {
    // 加载收支趋势数据
    const incomeExpenseResponse = await getIncomeExpenseData()
    incomeExpenseData.value = incomeExpenseResponse.data
    
    // 加载业务类型分布数据
    const businessTypeResponse = await getBusinessTypeData()
    businessTypeData.value = businessTypeResponse.data
    
    // 重新初始化图表
    initIncomeExpenseChart()
    initBusinessTypeChart()
  } catch (err) {
    console.error('加载图表数据失败:', err)
    error.value = '加载图表数据失败'
  }
}

// 加载所有仪表盘数据
const loadDashboardData = async () => {
  loading.value = true
  error.value = ''
  
  try {
    await Promise.all([
      loadStats(),
      loadTodos(),
      loadNotices(),
      loadChartData()
    ])
  } catch (err) {
    console.error('加载仪表盘数据失败:', err)
    error.value = '加载仪表盘数据失败'
  } finally {
    loading.value = false
  }
}

// 响应式处理
const handleResize = () => {
  incomeExpenseChartInstance?.resize()
  businessTypeChartInstance?.resize()
}

// 实时数据更新
let refreshTimer = null

// 刷新数据
const refreshData = async () => {
  await loadDashboardData()
}

// 组件挂载时初始化图表和启动定时器
onMounted(() => {
  window.addEventListener('resize', handleResize)
  
  // 加载初始数据
  loadDashboardData()
  
  // 启动定时刷新（每30秒刷新一次）
  refreshTimer = window.setInterval(refreshData, 30000)
})

// 组件卸载时销毁图表和清除定时器
onUnmounted(() => {
  incomeExpenseChartInstance?.dispose()
  businessTypeChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
  
  // 清除定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
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
          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #333;
          }

          .stat-change {
            margin-top: 4px;
            .change-value {
              font-size: 12px;
              &.up {
                color: #67c23a;
              }
              &.down {
                color: #f56c6c;
              }
            }
          }

          .stat-title {
            font-size: 14px;
            color: #999;
            margin-top: 8px;
          }
        }
      }
    }
  }

  .notice-list {
    .notice-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      &:last-child {
        border-bottom: none;
      }

      .notice-title {
        flex: 1;
        color: #333;
        font-size: 14px;
      }

      .notice-time {
        color: #999;
        font-size: 12px;
      }

      .notice-arrow {
        color: #999;
        transition: color 0.3s;
      }

      &:hover .notice-arrow {
        color: #409eff;
      }
    }
  }

  .notice-detail {
    h3 {
      margin: 0 0 16px 0;
      color: #333;
    }

    .notice-time {
      color: #999;
      font-size: 12px;
      margin-bottom: 16px;
    }

    .notice-content {
      color: #666;
      line-height: 1.6;
    }
  }

  .chart-container {
    height: 300px;
    width: 100%;
  }
}
</style>