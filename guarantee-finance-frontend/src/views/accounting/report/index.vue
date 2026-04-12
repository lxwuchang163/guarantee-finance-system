<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表管理</span>
          <div>
            <el-date-picker v-model="period" type="month" placeholder="选择期间" value-format="YYYYMM" style="width:150px;margin-right:10px" />
            <el-button type="primary" @click="generateReport('BALANCE_SHEET')">资产负债表</el-button>
            <el-button type="success" @click="generateReport('INCOME_STATEMENT')">利润表</el-button>
            <el-button type="warning" @click="generateReport('CASH_FLOW')">现金流量表</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe border>
        <el-table-column prop="reportCode" label="报表编码" width="180" />
        <el-table-column prop="reportName" label="报表名称" min-width="200" />
        <el-table-column prop="reportType" label="报表类型" width="120">
          <template #default="{ row }">{{ ({ BALANCE_SHEET: '资产负债表', INCOME_STATEMENT: '利润表', CASH_FLOW: '现金流量表', PROFIT_DISTRIBUTION: '利润分配表' } as any)[row.reportType as string] || row.reportType }}</template>
        </el-table-column>
        <el-table-column prop="period" label="期间" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="({ DRAFT: 'info', CONFIRMED: 'warning', APPROVED: 'success' } as any)[row.status as string]" size="small">
              {{ ({ DRAFT: '草稿', CONFIRMED: '已确认', APPROVED: '已审批' } as any)[row.status as string] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewReport(row)">查看</el-button>
            <el-button v-if="row.status === 'DRAFT'" type="warning" size="small" link @click="confirmReport(row)">确认</el-button>
            <el-button v-if="row.status === 'CONFIRMED'" type="success" size="small" link @click="approveReport(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="current" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="reportVisible" :title="reportTitle" width="800px">
      <div v-if="reportData" class="report-content">
        <div v-if="reportData.assetItems" class="report-section">
          <h3>资产</h3>
          <el-table :data="reportData.assetItems" stripe border size="small">
            <el-table-column prop="subjectCode" label="科目编码" width="100" />
            <el-table-column prop="subjectName" label="科目名称" min-width="150" />
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">{{ Number(row.amount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</template>
            </el-table-column>
          </el-table>
          <div class="report-total">资产合计：{{ Number(reportData.totalAsset || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</div>
        </div>
        <div v-if="reportData.liabilityItems" class="report-section">
          <h3>负债</h3>
          <el-table :data="reportData.liabilityItems" stripe border size="small">
            <el-table-column prop="subjectCode" label="科目编码" width="100" />
            <el-table-column prop="subjectName" label="科目名称" min-width="150" />
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">{{ Number(row.amount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</template>
            </el-table-column>
          </el-table>
          <div class="report-total">负债合计：{{ Number(reportData.totalLiability || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</div>
        </div>
        <div v-if="reportData.equityItems" class="report-section">
          <h3>所有者权益</h3>
          <el-table :data="reportData.equityItems" stripe border size="small">
            <el-table-column prop="subjectCode" label="科目编码" width="100" />
            <el-table-column prop="subjectName" label="科目名称" min-width="150" />
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">{{ Number(row.amount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</template>
            </el-table-column>
          </el-table>
          <div class="report-total">权益合计：{{ Number(reportData.totalEquity || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</div>
        </div>
        <div v-if="reportData.revenueItems" class="report-section">
          <h3>收入</h3>
          <el-table :data="reportData.revenueItems" stripe border size="small">
            <el-table-column prop="subjectCode" label="科目编码" width="100" />
            <el-table-column prop="subjectName" label="科目名称" min-width="150" />
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">{{ Number(row.amount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div v-if="reportData.expenseItems" class="report-section">
          <h3>支出</h3>
          <el-table :data="reportData.expenseItems" stripe border size="small">
            <el-table-column prop="subjectCode" label="科目编码" width="100" />
            <el-table-column prop="subjectName" label="科目名称" min-width="150" />
            <el-table-column prop="amount" label="金额" width="150" align="right">
              <template #default="{ row }">{{ Number(row.amount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div v-if="reportData.netProfit !== undefined" class="report-total">净利润：{{ Number(reportData.netProfit || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { generateBalanceSheet, generateIncomeStatement, generateCashFlow, getReportPage, getReportDetail, confirmReport, approveReport } from '@/api/accounting/financialReport'

const period = ref('')
const tableData = ref<any[]>([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const reportVisible = ref(false)
const reportTitle = ref('')
const reportData = ref<any>(null)

const loadData = async () => {
  const res = await getReportPage({ period: period.value, current: current.value, size: size.value })
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const generateReport = async (type: string) => {
  if (!period.value) { ElMessage.warning('请选择期间'); return }
  if (type === 'BALANCE_SHEET') await generateBalanceSheet(period.value)
  else if (type === 'INCOME_STATEMENT') await generateIncomeStatement(period.value)
  else if (type === 'CASH_FLOW') await generateCashFlow(period.value)
  ElMessage.success('报表生成成功')
  loadData()
}

const viewReport = async (row: any) => {
  const res = await getReportDetail(row.id)
  const report = res.data
  if (report && report.reportData) {
    reportData.value = JSON.parse(report.reportData)
    reportTitle.value = report.reportName
    reportVisible.value = true
  }
}

const confirmReportAction = async (row: any) => {
  await ElMessageBox.confirm('确认此报表？', '提示')
  await confirmReport(row.id)
  ElMessage.success('确认成功')
  loadData()
}

const approveReportAction = async (row: any) => {
  await ElMessageBox.confirm('审批此报表？', '提示')
  await approveReport(row.id)
  ElMessage.success('审批成功')
  loadData()
}

onMounted(() => {
  const now = new Date()
  period.value = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.el-pagination { margin-top: 16px; justify-content: flex-end; }
.report-section { margin-bottom: 20px; }
.report-section h3 { margin: 0 0 10px; color: #333; }
.report-total { margin-top: 10px; font-weight: bold; font-size: 14px; text-align: right; }
</style>
