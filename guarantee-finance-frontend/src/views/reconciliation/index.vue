<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header"><span>银行对账管理</span></div>
      </template>

      <el-row :gutter="16" class="status-cards">
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff;"><el-icon :size="24"><List /></el-icon></div>
            <div><div class="stat-value">{{ stats.totalTransactions }}</div><div class="stat-label">银行流水总数</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a;"><el-icon :size="24"><CircleCheck /></el-icon></div>
            <div><div class="stat-value">{{ stats.matched }}</div><div class="stat-label">已匹配</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c;"><el-icon :size="24"><Warning /></el-icon></div>
            <div><div class="stat-value">{{ stats.unmatched }}</div><div class="stat-label">未匹配</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c;"><el-icon :size="24"><CircleClose /></el-icon></div>
            <div><div class="stat-value">{{ stats.errorCount }}</div><div class="stat-label">异常</div></div>
          </div>
        </el-card></el-col>
      </el-row>

      <!-- 搜索和操作区 -->
      <div class="search-area">
        <el-form :inline="true" :model="query" class="search-form">
          <el-form-item label="银行账号"><el-input v-model="query.bankAccountNo" placeholder="银行账号" clearable style="width: 180px;" /></el-form-item>
          <el-form-item label="收支方向">
            <el-select v-model="query.direction" clearable placeholder="请选择" style="width: 120px;">
              <el-option :value="1" label="收入" /><el-option :value="2" label="支出" />
            </el-select>
          </el-form-item>
          <el-form-item label="匹配状态">
            <el-select v-model="query.matchStatus" clearable placeholder="请选择" style="width: 120px;">
              <el-option :value="0" label="未匹配" /><el-option :value="1" label="精确匹配" /><el-option :value="2" label="模糊匹配" /><el-option :value="3" label="异常" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期范围">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 260px;" />
          </el-form-item>
          <el-form-item>
            <el-upload action="" :auto-upload="false" :show-file-list="false" accept=".xlsx,.xls,.csv" :on-change="handleFileChange">
              <el-button type="primary" icon="Upload">导入银行流水</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item>
            <el-button type="success" icon="MagicStick" @click="handleAutoReconcile" :loading="reconciling">自动对账</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 流水表格 -->
      <el-table :data="transactionList" stripe border v-loading="tableLoading">
        <el-table-column prop="transactionDate" label="交易日期" width="110" fixed="left" />
        <el-table-column prop="bankAccountNo" label="银行账号" width="150" />
        <el-table-column prop="direction" label="方向" width="70" align="center">
          <template #default="{ row }"><el-tag :type="row.direction === 1 ? 'success' : 'danger'" size="small">{{ row.direction === 1 ? '收' : '付' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="130" align="right">
          <template #default="{ row }"><span :style="{ color: row.direction === 1 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">{{ (row.direction === 1 ? '+' : '-') + formatMoney(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column prop="counterAccountName" label="对方户名" width="140" show-overflow-tooltip />
        <el-table-column prop="counterAccountNo" label="对方账号" width="150" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="140" show-overflow-tooltip />
        <el-table-column prop="matchStatusText" label="匹配状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="matchStatusTagType(row.matchStatus)" size="small">{{ row.matchStatusText || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchedBillNo" label="关联单号" width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.matchStatus === 0 || row.matchStatus === 2" type="primary" link size="small" @click="handleForceMatch(row)">手工匹配</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 手工匹配对话框 -->
    <el-dialog v-model="forceMatchVisible" title="手工对账匹配" width="500px" destroy-on-close>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="银行流水金额">{{ forceMatchRow ? (forceMatchRow.direction === 1 ? '+' : '-') + formatMoney(forceMatchRow.amount) : '' }}</el-descriptions-item>
        <el-descriptions-item label="对方户名">{{ forceMatchRow?.counterAccountName }}</el-descriptions-item>
        <el-descriptions-item label="摘要">{{ forceMatchRow?.summary }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="forceMatchForm" label-width="90px" style="margin-top: 16px;">
        <el-form-item label="业务单类型">
          <el-select v-model="forceMatchForm.billType" style="width: 100%;">
            <el-option value="RECEIPT" label="收款单" /><el-option value="PAYMENT" label="付款单" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单ID">
          <el-input-number v-model="forceMatchForm.billId" :min="1" controls-position="right" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forceMatchVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmForceMatch" :loading="matching">确认匹配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, CircleCheck, Warning, CircleClose, Upload, Search, MagicStick } from '@element-plus/icons-vue'
import {
  importTransactions, getTransactionPage, autoReconcile, forceMatch
} from '@/api/reconciliation'
import type { BankTransactionVO } from '@/api/reconciliation'

const tableLoading = ref(false)
const reconciling = ref(false)
const matching = ref(false)
const transactionList = ref<BankTransactionVO[]>([])
const total = ref(0)
const dateRange = ref<string[]>()
const forceMatchVisible = ref(false)
const forceMatchRow = ref<BankTransactionVO | null>(null)

const query = reactive({ bankAccountNo: '', direction: null as number | null, matchStatus: null as number | null, startDate: '', endDate: '', current: 1, size: 10 })
const forceMatchForm = reactive({ billId: 0, billType: 'RECEIPT' })
const stats = reactive({ totalTransactions: 0, matched: 0, unmatched: 0, errorCount: 0 })

const handleFileChange = async (uploadFile: any) => {
  if (!uploadFile.raw) return
  tableLoading.value = true
  try {
    await importTransactions(uploadFile.raw)
    ElMessage.success('银行流水导入成功')
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '导入失败') }
  finally { tableLoading.value = false }
}

const handleAutoReconcile = async () => {
  const accountNo = query.bankAccountNo
  if (!accountNo) { ElMessage.warning('请先选择银行账号'); return }
  const date = dateRange.value?.[0] || new Date().toISOString().slice(0, 10)
  reconciling.value = true
  try {
    await autoReconcile(accountNo, date)
    ElMessage.success('自动对账完成')
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '对账失败') }
  finally { reconciling.value = false }
}

const loadData = async () => {
  tableLoading.value = true
  try {
    query.startDate = dateRange.value?.[0] || ''
    query.endDate = dateRange.value?.[1] || ''
    const res = await getTransactionPage({ ...query })
    transactionList.value = res.data.records
    total.value = res.data.total
    updateStats()
  } catch (e) { console.error(e) }
  finally { tableLoading.value = false }
}

const updateStats = () => {
  const list = transactionList.value
  stats.totalTransactions = list.length
  stats.matched = list.filter(t => t.matchStatus === 1).length
  stats.unmatched = list.filter(t => t.matchStatus === 0).length
  stats.errorCount = list.filter(t => t.matchStatus === 3).length
}

const handleForceMatch = (row: BankTransactionVO) => {
  forceMatchRow.value = row
  forceMatchForm.billId = 0
  forceMatchForm.billType = 'RECEIPT'
  forceMatchVisible.value = true
}

const confirmForceMatch = async () => {
  if (!forceMatchForm.billId) { ElMessage.warning('请输入业务单ID'); return }
  matching.value = true
  try {
    await forceMatch({ transactionId: forceMatchRow.value!.id, billId: forceMatchForm.billId, billType: forceMatchForm.billType })
    ElMessage.success('手工匹配成功')
    forceMatchVisible.value = false
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '匹配失败') }
  finally { matching.value = false }
}

const matchStatusTagType = (s: number): string => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] || '')
const formatMoney = (n: number) => n?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.page-container { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.status-cards .stat-card { display: flex; align-items: center; gap: 14px;
  .stat-icon { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  .stat-value { font-size: 24px; font-weight: bold; color: #303133; }
  .stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
}
.search-area { margin-bottom: 16px; }
.search-form .el-form-item { margin-bottom: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
