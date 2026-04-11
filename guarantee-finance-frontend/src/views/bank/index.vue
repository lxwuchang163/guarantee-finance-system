<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header"><span>银企直连管理</span></div>
      </template>

      <el-row :gutter="16" class="status-cards">
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff;"><el-icon :size="24"><CreditCard /></el-icon></div>
            <div><div class="stat-value">{{ accountList.length }}</div><div class="stat-label">已配置账户</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a;"><el-icon :size="24"><CircleCheck /></el-icon></div>
            <div><div class="stat-value">{{ onlineCount }}</div><div class="stat-label">在线账户</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c;"><el-icon :size="24"><Warning /></el-icon></div>
            <div><div class="stat-value">{{ warningCount }}</div><div class="stat-label">预警账户</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c;"><el-icon :size="24"><CircleClose /></el-icon></div>
            <div><div class="stat-value">{{ offlineCount }}</div><div class="stat-label">离线/异常</div></div>
          </div>
        </el-card></el-col>
      </el-row>

      <!-- 操作按钮和搜索区域 -->
      <div class="action-search-container">
        <div class="action-bar">
          <el-button type="primary" icon="Refresh" @click="handleBatchBalance" :loading="batchLoading">批量查询余额</el-button>
          <el-button type="success" icon="Download" @click="showDownloadDialog = true">下载流水</el-button>
          <el-button type="warning" icon="Search" @click="showPaymentDialog = true">查询付款状态</el-button>
          <el-button type="info" icon="Plus" @click="showAccountDialog = true">新增账户</el-button>
        </div>

        <!-- 搜索 -->
        <div class="search-area">
          <el-form :inline="true" :model="queryParams" class="search-form">
            <el-form-item label="银行账号"><el-input v-model="queryParams.accountNo" placeholder="请输入银行账号" clearable style="width: 180px" /></el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" clearable placeholder="请选择" style="width: 110px">
                <el-option :value="1" label="正常" /><el-option :value="0" label="异常" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" icon="Search" @click="loadAccounts">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 账户列表 -->
      <el-table :data="accountList" stripe border v-loading="tableLoading">
        <el-table-column prop="accountNo" label="银行账号" width="180" fixed="left" />
        <el-table-column prop="accountName" label="账户名称" width="160" />
        <el-table-column prop="bankName" label="开户行" width="160" />
        <el-table-column prop="balance" label="账户余额" width="140" align="right">
          <template #default="{ row }"><span style="font-weight: bold; color: #303133;">{{ formatMoney(row.balance) }}</span></template>
        </el-table-column>
        <el-table-column prop="availableBalance" label="可用余额" width="140" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.availableBalance < (row.thresholdWarning || 0) ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">{{ formatMoney(row.availableBalance) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="thresholdWarning" label="预警阈值" width="110" align="right" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.apiStatus === 1 ? 'success' : 'danger'" size="small">{{ row.apiStatus === 1 ? '正常' : '异常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleQueryBalance(row.accountNo)" :loading="row._loading">查询余额</el-button>
            <el-button type="warning" link size="small" @click="handleEditAccount(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleRefresh()">刷新</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="queryPage.current" v-model:page-size="queryPage.size" :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadAccounts" @current-change="loadAccounts" />
      </div>
    </el-card>

    <!-- 新增/编辑账户对话框 -->
    <el-dialog v-model="showAccountDialog" :title="editMode ? '编辑银行账户' : '新增银行账户'" width="600px" destroy-on-close>
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="银行账号" prop="accountNo"><el-input v-model="accountForm.accountNo" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户名称" prop="accountName"><el-input v-model="accountForm.accountName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开户行" prop="bankName"><el-input v-model="accountForm.bankName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行代码"><el-input v-model="accountForm.bankCode" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="预警阈值"><el-input-number v-model="accountForm.thresholdWarning" :precision="2" :controls="false" style="width: 100%;" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="停用阈值"><el-input-number v-model="accountForm.thresholdStop" :precision="2" :controls="false" style="width: 100%;" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="日限额"><el-input-number v-model="accountForm.dailyLimit" :precision="2" :controls="false" style="width: 100%;" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="accountForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showAccountDialog = false">取消</el-button><el-button type="primary" @click="handleSaveAccount">保存</el-button></template>
    </el-dialog>

    <!-- 下载流水对话框 -->
    <el-dialog v-model="showDownloadDialog" title="下载银行流水" width="450px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="银行账号">
          <el-select v-model="downloadForm.accountNo" filterable style="width: 100%;">
            <el-option v-for="a in accountList" :key="a.accountNo" :label="a.accountNo + ' - ' + a.accountName" :value="a.accountNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="downloadForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showDownloadDialog = false">取消</el-button><el-button type="primary" @click="handleDownload" :loading="downloading">确认下载</el-button></template>
    </el-dialog>

    <!-- 查询付款状态 -->
    <el-dialog v-model="showPaymentDialog" title="查询付款状态" width="400px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="付款单号">
          <el-input v-model="paymentNoInput" placeholder="请输入付款单号" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showPaymentDialog = false">取消</el-button><el-button type="primary" @click="handleCheckPayment" :loading="checking">查询</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CreditCard, CircleCheck, Warning, CircleClose, Refresh, Download, Plus, Search } from '@element-plus/icons-vue'
import { getBankAccountList, queryBalance, batchQueryBalances, downloadTransactions, checkPaymentStatus, saveBankAccount } from '@/api/bank'
import type { BankAccountConfigVO } from '@/api/bank'

const tableLoading = ref(false)
const batchLoading = ref(false)
const downloading = ref(false)
const checking = ref(false)
const accountList = ref<(BankAccountConfigVO & { _loading?: boolean })[]>([])
const showAccountDialog = ref(false)
const showDownloadDialog = ref(false)
const showPaymentDialog = ref(false)
const editMode = ref(false)
const paymentNoInput = ref('')

const queryParams = reactive({ accountNo: '', status: null as number | null })
const queryPage = reactive({ current: 1, size: 10 })
const total = ref(0)

const accountFormRef = ref()
const accountForm = reactive({
  id: undefined as number | undefined,
  accountNo: '', accountName: '', bankName: '', bankCode: '',
  thresholdWarning: 0, thresholdStop: 0, dailyLimit: 0, singleLimit: 0, remark: ''
})
const downloadForm = reactive({ accountNo: '', dateRange: [] as string[] })

const accountRules = {
  accountNo: [{ required: true, message: '请输入银行账号', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  bankName: [{ required: true, message: '请输入开户行', trigger: 'blur' }]
}

const onlineCount = computed(() => accountList.value.filter(a => a.apiStatus === 1).length)
const warningCount = computed(() => accountList.value.filter(a => a.availableBalance < (a.thresholdWarning || Infinity)).length)
const offlineCount = computed(() => accountList.value.filter(a => a.apiStatus !== 1).length)

const loadAccounts = async () => {
  tableLoading.value = true
  try {
    const res = await getBankAccountList()
    let accounts = res.data.map((a: BankAccountConfigVO) => ({ ...a, _loading: false }))
    
    // 应用搜索过滤
    if (queryParams.accountNo) {
      accounts = accounts.filter(a => a.accountNo.includes(queryParams.accountNo))
    }
    if (queryParams.status !== null) {
      accounts = accounts.filter(a => a.apiStatus === queryParams.status)
    }
    
    // 应用分页
    const start = (queryPage.current - 1) * queryPage.size
    const end = start + queryPage.size
    accountList.value = accounts.slice(start, end)
    total.value = accounts.length
  } catch (e) { console.error(e) }
  finally { tableLoading.value = false }
}

const handleQueryBalance = async (accountNo: string) => {
  const acc = accountList.value.find(a => a.accountNo === accountNo)
  if (acc) acc._loading = true
  try {
    const res = await queryBalance(accountNo)
    if (acc) {
      acc.balance = res.data.balance
      acc.availableBalance = res.data.availableBalance
      if (res.data.isBelowThreshold) ElMessage.warning(`账户 ${accountNo} 余额低于预警阈值!`)
      else ElMessage.success(`查询成功，余额: ${res.data.balance}`)
    }
  } catch (e: any) { ElMessage.error(e.message || '查询失败') }
  finally { if (acc) acc._loading = false }
}

const handleBatchBalance = async () => {
  batchLoading.value = true
  try {
    const accountNos = accountList.value.map(a => a.accountNo)
    const res = await batchQueryBalances(accountNos)
    res.data.forEach((b: any) => {
      const acc = accountList.value.find(a => a.accountNo === b.accountNo)
      if (acc) { acc.balance = b.balance; acc.availableBalance = b.availableBalance }
    })
    ElMessage.success('批量查询余额成功')
  } catch (e: any) { ElMessage.error(e.message || '批量查询失败') }
  finally { batchLoading.value = false }
}

const handleEditAccount = (row: BankAccountConfigVO) => {
  editMode.value = true
  Object.assign(accountForm, { id: row.id, accountNo: row.accountNo, accountName: row.accountName, bankName: row.bankName, bankCode: row.bankCode, thresholdWarning: row.thresholdWarning, thresholdStop: row.thresholdStop, dailyLimit: row.dailyLimit, singleLimit: row.singleLimit, remark: row.remark })
  showAccountDialog.value = true
}

const handleSaveAccount = async () => {
  const valid = await accountFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await saveBankAccount(accountForm)
    ElMessage.success(editMode.value ? '修改成功' : '新增成功')
    showAccountDialog.value = false
    loadAccounts()
  } catch (e: any) { ElMessage.error(e.message || '保存失败') }
}

const handleDownload = async () => {
  if (!downloadForm.accountNo) { ElMessage.warning('请选择银行账号'); return }
  downloading.value = true
  try {
    await downloadTransactions(downloadForm.accountNo, downloadForm.dateRange[0], downloadForm.dateRange[1])
    ElMessage.success('流水下载成功')
    showDownloadDialog.value = false
  } catch (e: any) { ElMessage.error(e.message || '下载失败') }
  finally { downloading.value = false }
}

const handleCheckPayment = async () => {
  if (!paymentNoInput.value) { ElMessage.warning('请输入付款单号'); return }
  checking.value = true
  try {
    const bankCode = accountList.value[0]?.bankCode || ''
    const res = await checkPaymentStatus(bankCode, paymentNoInput.value)
    ElMessage.success(res.data ? '付款已到账' : '付款未到账或处理中')
    showPaymentDialog.value = false
  } catch (e: any) { ElMessage.error(e.message || '查询失败') }
  finally { checking.value = false }
}

const handleRefresh = () => loadAccounts()

const resetQuery = () => { Object.assign(queryParams, { accountNo: '', status: null }); queryPage.current = 1; loadAccounts() }

const formatMoney = (n: number) => n?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'

onMounted(() => loadAccounts())
</script>

<style lang="scss" scoped>
.page-container { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.status-cards .stat-card { display: flex; align-items: center; gap: 14px;
  .stat-icon { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  .stat-value { font-size: 24px; font-weight: bold; color: #303133; }
  .stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
}
.action-search-container { margin-top: 16px; }
.action-bar { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.search-area { margin-bottom: 16px; }
.search-form .el-form-item { margin-bottom: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
