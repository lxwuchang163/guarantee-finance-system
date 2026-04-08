<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header"><span>会计平台-凭证管理</span></div>
      </template>

      <el-row :gutter="16" class="status-cards">
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff;"><el-icon :size="24"><Document /></el-icon></div>
            <div><div class="stat-value">{{ total }}</div><div class="stat-label">凭证总数</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c;"><el-icon :size="24"><Clock /></el-icon></div>
            <div><div class="stat-value">{{ pendingCount }}</div><div class="stat-label">待审核</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a;"><el-icon :size="24"><CircleCheck /></el-icon></div>
            <div><div class="stat-value">{{ auditedCount }}</div><div class="stat-label">已审核</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #909399;"><el-icon :size="24"><Connection /></el-icon></div>
            <div><div class="stat-value">{{ syncedCount }}</div><div class="stat-label">已同步NC</div></div>
          </div>
        </el-card></el-col>
      </el-row>

      <div class="action-bar" style="margin-top: 16px;">
        <el-button type="primary" icon="Plus" @click="showGenerateDialog = true">生成凭证</el-button>
        <el-button type="success" icon="Batch" @click="showBatchDialog = true">批量生成</el-button>
        <el-input v-model="query.voucherNo" placeholder="凭证编号" clearable style="width: 160px;" />
        <el-select v-model="query.voucherType" placeholder="凭证类型" clearable style="width: 130px;">
          <el-option value="1" label="收款凭证" /><el-option value="2" label="付款凭证" /><el-option value="3" label="转账凭证" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 110px;">
          <el-option value="0" label="未审核" /><el-option value="1" label="已审核" /><el-option value="2" label="已记账" /><el-option value="3" label="已作废" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 260px;" />
        <el-button icon="Search" @click="loadVouchers">查询</el-button>
      </div>

      <!-- 凭证表格 -->
      <el-table :data="voucherList" stripe border v-loading="tableLoading" size="small" style="margin-top: 12px;" @row-click="handleRowClick">
        <el-table-column prop="voucherNo" label="凭证编号" width="170" fixed />
        <el-table-column prop="voucherDate" label="凭证日期" width="110" />
        <el-table-column prop="voucherTypeName" label="凭证类型" width="100" align="center" />
        <el-table-column prop="accountingPeriod" label="会计期间" width="100" />
        <el-table-column prop="totalDebit" label="借方合计" width="130" align="right">
          <template #default="{ row }"><span style="color: #303133; font-weight: bold;">{{ formatMoney(row.totalDebit) }}</span></template>
        </el-table-column>
        <el-table-column prop="totalCredit" label="贷方合计" width="130" align="right">
          <template #default="{ row }"><span style="color: #303133; font-weight: bold;">{{ formatMoney(row.totalCredit) }}</span></template>
        </el-table-column>
        <el-table-column prop="maker" label="制单人" width="80" />
        <el-table-column prop="auditor" label="审核人" width="80" />
        <el-table-column prop="sourceBillType" label="来源类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sourceBillType === 'RECEIPT' ? '' : 'warning'" size="small">{{ row.sourceBillType === 'RECEIPT' ? '收款单' : row.sourceBillType === 'PAYMENT' ? '付款单' : '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceBillNo" label="来源单号" width="150" show-overflow-tooltip />
        <el-table-column prop="statusText" label="状态" width="85" align="center">
          <template #default="{ row }"><el-tag :type="getStatusTagType(row.status)" size="small">{{ row.statusText }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click.stop="handleAudit(row.id)">审核</el-button>
            <el-button v-if="row.status === 1 || row.status === 2" type="danger" link size="small" @click.stop="handleReverse(row.id)">冲销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadVouchers" @current-change="loadVouchers" />
      </div>
    </el-card>

    <!-- 生成凭证对话框 -->
    <el-dialog v-model="showGenerateDialog" title="生成会计凭证" width="550px" destroy-on-close>
      <el-form ref="genFormRef" :model="genForm" :rules="genRules" label-width="110px">
        <el-form-item label="来源单据类型" prop="sourceBillType">
          <el-select v-model="genForm.sourceBillType" style="width: 100%;">
            <el-option value="RECEIPT" label="收款单" /><el-option value="PAYMENT" label="付款单" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源单据ID" prop="sourceBillId">
          <el-input-number v-model="genForm.sourceBillId" :min="1" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="客户编码"><el-input v-model="genForm.customerCode" /></el-form-item>
        <el-form-item label="客户名称"><el-input v-model="genForm.customerName" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="金额" prop="amount"><el-input-number v-model="genForm.amount" :precision="2" :controls="false" style="width: 100%;" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="凭证类型"><el-select v-model="genForm.voucherType" style="width: 100%;"><el-option value="1" label="收款凭证" /><el-option value="2" label="付款凭证" /><el-option value="3" label="转账凭证" /></el-select></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要模板"><el-input v-model="genForm.summaryTemplate" placeholder="如：收到{customerName}保费收入" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showGenerateDialog = false">取消</el-button><el-button type="primary" @click="handleGenerate" :loading="generating">确认生成</el-button></template>
    </el-dialog>

    <!-- 批量生成对话框 -->
    <el-dialog v-model="showBatchDialog" title="批量生成凭证" width="450px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        选择已审核的收/付款单据ID，系统将自动批量生成对应会计凭证
      </el-alert>
      <el-form label-width="100px">
        <el-form-item label="单据ID列表">
          <el-input v-model="batchIdsInput" type="textarea" :rows="4" placeholder="多个ID用逗号分隔，如：1,2,3,4,5" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showBatchDialog = false">取消</el-button><el-button type="primary" @click="handleBatchGenerate" :loading="batchGenerating">批量生成</el-button></template>
    </el-dialog>

    <!-- 凭证详情 -->
    <el-dialog v-model="detailVisible" title="凭证详情" width="650px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="凭证编号">{{ detailData.voucherNo }}</el-descriptions-item>
        <el-descriptions-item label="凭证日期">{{ detailData.voucherDate }}</el-descriptions-item>
        <el-descriptions-item label="凭证类型">{{ detailData.voucherTypeName }}</el-descriptions-item>
        <el-descriptions-item label="会计期间">{{ detailData.accountingPeriod }}</el-descriptions-item>
        <el-descriptions-item label="借方合计">{{ formatMoney(detailData.totalDebit) }}</el-descriptions-item>
        <el-descriptions-item label="贷方合计">{{ formatMoney(detailData.totalCredit) }}</el-descriptions-item>
        <el-descriptions-item label="制单人">{{ detailData.maker }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.auditor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">{{ detailData.sourceBillType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源单号">{{ detailData.sourceBillNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="NC同步状态">
          <el-tag :type="detailData.ncSyncStatus === 1 ? 'success' : 'info'" size="small">{{ detailData.ncSyncStatus === 1 ? '已同步' : '未同步' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="getStatusTagType(detailData.status)" size="small">{{ detailData.statusText }}</el-tag></el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Clock, CircleCheck, Connection, Plus, Batch, Search } from '@element-plus/icons-vue'
import { getVoucherPage, getVoucherDetail, generateVoucher, batchGenerateVouchers, auditVoucher, reverseVoucher } from '@/api/accounting'
import type { AccVoucherVO, VoucherGenerateDTO } from '@/api/accounting'

const tableLoading = ref(false)
const generating = ref(false)
const batchGenerating = ref(false)
const voucherList = ref<AccVoucherVO[]>([])
const total = ref(0)
const dateRange = ref<string[]>()
const showGenerateDialog = ref(false)
const showBatchDialog = ref(false)
const detailVisible = ref(false)
const detailData = ref<AccVoucherVO | null>(null)
const batchIdsInput = ref('')

const genFormRef = ref()
const genForm = reactive<VoucherGenerateDTO>({
  sourceBillId: 0, sourceBillType: 'RECEIPT', sourceBillNo: '',
  voucherType: '', customerCode: '', customerName: '', amount: 0, summaryTemplate: ''
})
const genRules = {
  sourceBillType: [{ required: true, message: '请选择来源单据类型', trigger: 'change' }],
  sourceBillId: [{ required: true, message: '请输入来源单据ID', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const query = reactive({ voucherNo: '', voucherType: '', status: '', startDate: '', endDate: '', current: 1, size: 10 })

const pendingCount = computed(() => voucherList.value.filter(v => v.status === 0).length)
const auditedCount = computed(() => voucherList.value.filter(v => v.status === 1).length)
const syncedCount = computed(() => voucherList.value.filter(v => v.ncSyncStatus === 1).length)

const loadVouchers = async () => {
  tableLoading.value = true
  try {
    query.startDate = dateRange.value?.[0] || ''
    query.endDate = dateRange.value?.[1] || ''
    const res = await getVoucherPage(query)
    voucherList.value = res.data.records
    total.value = res.data.total
  } catch (e) { console.error(e) }
  finally { tableLoading.value = false }
}

const handleGenerate = async () => {
  const valid = await genFormRef.value?.validate().catch(() => false)
  if (!valid) return
  generating.value = true
  try {
    const id = await generateVoucher(genForm)
    ElMessage.success('凭证生成成功，ID: ' + id.data)
    showGenerateDialog.value = false
    loadVouchers()
  } catch (e: any) { ElMessage.error(e.message || '生成失败') }
  finally { generating.value = false }
}

const handleBatchGenerate = async () => {
  if (!batchIdsInput.value.trim()) { ElMessage.warning('请输入单据ID列表'); return }
  const ids = batchIdsInput.value.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n))
  if (ids.length === 0) { ElMessage.warning('请输入有效的单据ID'); return }
  batchGenerating.value = true
  try {
    await batchGenerateVouchers(ids)
    ElMessage.success(`成功批量生成${ids.length}张凭证`)
    showBatchDialog.value = false
    loadVouchers()
  } catch (e: any) { ElMessage.error(e.message || '批量生成失败') }
  finally { batchGenerating.value = false }
}

const handleAudit = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认审核该凭证？', '提示', { type: 'warning' })
    await auditVoucher(id)
    ElMessage.success('审核成功')
    loadVouchers()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '审核失败') }
}

const handleReverse = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认冲销该凭证？此操作不可撤销！', '警告', { type: 'error' })
    await reverseVoucher(id)
    ElMessage.success('冲销成功')
    loadVouchers()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '冲销失败') }
}

const viewDetail = async (row: AccVoucherVO) => {
  try {
    const res = await getVoucherDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (e) { console.error(e) }
}

const handleRowClick = (row: AccVoucherVO) => {}

const getStatusTagType = (s: number): string => ({ 0: 'warning', 1: 'success', 2: '', 3: 'danger' }[s] || 'info')
const formatMoney = (n: number) => n?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'

onMounted(() => loadVouchers())
</script>

<style lang="scss" scoped>
.page-container { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.status-cards .stat-card { display: flex; align-items: center; gap: 14px;
  .stat-icon { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  .stat-value { font-size: 24px; font-weight: bold; color: #303133; }
  .stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
}
.action-bar { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
