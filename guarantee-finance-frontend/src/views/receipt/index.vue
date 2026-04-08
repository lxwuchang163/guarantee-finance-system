<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>收款单管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增收款单</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="关键词">
            <el-input v-model="queryParams.keyword" placeholder="单号/客户/合同" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="业务类型">
            <el-select v-model="queryParams.businessType" clearable placeholder="请选择" style="width: 140px">
              <el-option :value="1" label="保费收入" />
              <el-option :value="2" label="分担收入" />
              <el-option :value="3" label="追偿到款" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" clearable placeholder="请选择" style="width: 110px">
              <el-option :value="0" label="草稿" />
              <el-option :value="1" label="已提交" />
              <el-option :value="2" label="已审核" />
              <el-option :value="3" label="已记账" />
              <el-option :value="4" label="已作废" />
            </el-select>
          </el-form-item>
          <el-form-item label="收款日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="receiptNo" label="收款单号" width="160" fixed="left">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.receiptNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="businessTypeName" label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTag(row.businessType)" size="small">{{ row.businessTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contractNo" label="合同编号" width="140" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="receiptDate" label="收款日期" width="115" />
        <el-table-column prop="status" label="状态" width="85" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="makerName" label="制单人" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="165">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleAudit(row, true)">审核通过</el-button>
            <el-button v-if="row.status === 1" type="danger" link size="small" @click="handleAudit(row, false)">驳回</el-button>
            <el-button v-if="row.status === 2" type="success" link size="small" @click="handlePost(row)">记账</el-button>
            <el-button v-if="row.status === 3" type="info" link size="small" @click="handleReverse(row)">冲销</el-button>
            <el-button v-if="row.status <= 0" type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryPage.current"
          v-model:page-size="queryPage.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑收款单' : '新增收款单'" width="750px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务类型" prop="businessType">
              <el-select v-model="formData.businessType" placeholder="请选择" @change="onBusinessTypeChange" style="width: 100%">
                <el-option :value="1" label="保费收入" />
                <el-option :value="2" label="分担收入" />
                <el-option :value="3" label="追偿到款" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户编码" prop="customerCode">
              <el-input v-model="formData.customerCode" placeholder="请输入客户编码" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="formData.customerName" placeholder="请输入客户名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同编号">
              <el-input v-model="formData.contractNo" placeholder="请输入合同编号" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收款金额" prop="amount">
              <el-input-number v-model="formData.amount" :precision="2" :min="0.01" :controls="false" style="width: 100%" placeholder="请输入金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收款日期" prop="receiptDate">
              <el-date-picker v-model="formData.receiptDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收款方式">
              <el-select v-model="formData.paymentMethod" placeholder="请选择" style="width: 100%">
                <el-option :value="1" label="转账" />
                <el-option :value="2" label="现金" />
                <el-option :value="3" label="支票" />
                <el-option :value="4" label="汇票" />
                <el-option :value="5" label="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款方名称">
              <el-input v-model="formData.payerName" placeholder="付款方名称" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分担收入特有字段 -->
        <template v-if="formData.businessType === 2">
          <el-divider content-position="left">分担信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="分担方编码">
                <el-input v-model="formData.sharerCode" placeholder="分担方编码" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="分担方名称">
                <el-input v-model="formData.sharerName" placeholder="分担方名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="分担比例(%)">
                <el-input-number v-model="formData.shareRatio" :precision="2" :min="0" :max="100" :controls="false" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 追偿到款特有字段 -->
        <template v-if="formData.businessType === 3">
          <el-divider content-position="left">追偿信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="代偿单号">
                <el-input v-model="formData.compensationNo" placeholder="关联代偿单号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="追偿对象">
                <el-input v-model="formData.recoveryTargetName" placeholder="追偿对象名称" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="追偿方式">
                <el-select v-model="formData.recoveryMethod" placeholder="请选择" style="width: 100%">
                  <el-option :value="1" label="协商还款" />
                  <el-option :value="2" label="诉讼执行" />
                  <el-option :value="3" label="资产处置" />
                  <el-option :value="4" label="其他" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-form-item label="用途/摘要">
          <el-input v-model="formData.usage" type="textarea" :rows="2" placeholder="请输入用途或摘要" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="收款单详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="收款单号" :span="2">{{ detailData.receiptNo }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">
          <el-tag :type="getBusinessTypeTag(detailData.businessType)" size="small">{{ detailData.businessTypeName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detailData.status)" size="small">{{ getStatusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="合同编号">{{ detailData.contractNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收款金额" :span="2">
          <span class="amount-text large">¥{{ formatAmount(detailData.amount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="收款日期">{{ detailData.receiptDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际到账日期">{{ detailData.actualArrivalDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收款方式">{{ getPaymentMethodText(detailData.paymentMethod) }}</el-descriptions-item>
        <el-descriptions-item label="付款方">{{ detailData.payerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用途/摘要" :span="2">{{ detailData.usage || '-' }}</el-descriptions-item>
        <el-descriptions-item label="制单人">{{ detailData.makerName || '-' }} ({{ formatTime(detailData.makerTime) }})</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.auditorName || '-' }} ({{ formatTime(detailData.auditorTime) }})</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getReceiptPage, createReceipt, updateReceipt, deleteReceipt,
  submitReceipt, auditReceipt, postReceipt, reverseReceipt
} from '@/api/receipt'
import type { ReceiptVO, ReceiptDTO } from '@/api/receipt'

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)

// 搜索
const queryParams = reactive({ keyword: '', businessType: null as number | null, status: null as number | null, customerCode: '' })
const dateRange = ref<string[]>([])
const queryPage = reactive({ current: 1, size: 10 })
const tableData = ref<ReceiptVO[]>([])
const total = ref(0)

// 表单
const formData = ref<ReceiptDTO>({
  businessType: 1, customerCode: '', customerName: '', currency: 'CNY',
  amount: undefined as any, receiptDate: '', paymentMethod: 1
})
const formRules = {
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  customerCode: [{ required: true, message: '请输入客户编码', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  receiptDate: [{ required: true, message: '请选择收款日期', trigger: 'change' }]
}

// 详情
const detailData = ref<ReceiptVO | null>(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getReceiptPage({
      ...queryParams,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      current: queryPage.current,
      size: queryPage.size
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.businessType = null
  queryParams.status = null
  dateRange.value = []
  queryPage.current = 1
  loadData()
}

const handleAdd = () => {
  formData.value = { businessType: 1, customerCode: '', customerName: '', currency: 'CNY', amount: undefined as any, receiptDate: '', paymentMethod: 1 }
  dialogVisible.value = true
}

const handleEdit = (row: ReceiptVO) => {
  formData.value = { id: row.id, businessType: row.businessType, customerCode: row.customerCode, customerName: row.customerName, contractNo: row.contractNo, projectName: row.projectName, productCode: row.productCode, productName: row.productName, currency: row.currency || 'CNY', amount: row.amount, receiptDate: row.receiptDate || '', actualArrivalDate: row.actualArrivalDate || '', paymentMethod: row.paymentMethod, payerName: row.payerName, payerAccountNo: row.payerAccountNo, payerBankName: row.payerBankName, payeeAccountNo: row.payeeAccountNo, payeeBankName: row.payeeBankName, usage: row.usage }
  dialogVisible.value = true
}

const onBusinessTypeChange = () => {}

const handleSubmitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (formData.value.id) {
      await updateReceipt(formData.value as ReceiptDTO)
      ElMessage.success('修改成功')
    } else {
      await createReceipt(formData.value as ReceiptDTO)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) { ElMessage.error(error.message || '操作失败') }
  finally { submitting.value = false }
}

const handleSubmit = async (row: ReceiptVO) => {
  try {
    await ElMessageBox.confirm(`确定提交收款单 ${row.receiptNo} 进行审核吗？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' })
    await submitReceipt(row.id)
    ElMessage.success('已提交审核')
    loadData()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '提交失败') }
}

const handleAudit = async (row: ReceiptVO, pass: boolean) => {
  const action = pass ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确定${action}收款单 ${row.receiptNo} 吗？`, '确认操作', { confirmButtonText: `确定${action}`, cancelButtonText: '取消', type: pass ? 'success' : 'warning' })
    await auditReceipt(row.id, pass)
    ElMessage.success(`已${action}`)
    loadData()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') }
}

const handlePost = async (row: ReceiptVO) => {
  try {
    await ElMessageBox.confirm(`确定对收款单 ${row.receiptNo} 执行记账操作吗？`, '确认记账', { type: 'success' })
    await postReceipt(row.id)
    ElMessage.success('记账成功')
    loadData()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '记账失败') }
}

const handleReverse = async (row: ReceiptVO) => {
  try {
    await ElMessageBox.confirm(`确定冲销收款单 ${row.receiptNo} 吗？冲销后不可恢复！`, '警告', { confirmButtonText: '确定冲销', cancelButtonText: '取消', type: 'error' })
    await reverseReceipt(row.id)
    ElMessage.success('冲销成功')
    loadData()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '冲销失败') }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该收款单吗？删除后不可恢复！', '警告', { type: 'error' })
    await deleteReceipt(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '删除失败') }
}

const handleView = (row: ReceiptVO) => {
  detailData.value = row
  detailVisible.value = true
}

const getBusinessTypeTag = (t?: number): string => ({ 1: '', 2: 'success', 3: 'warning' }[t!] || '')
const getStatusText = (s: number): string => ({ 0: '草稿', 1: '已提交', 2: '已审核', 3: '已记账', 4: '已作废' }[s] || '未知')
const getStatusTagType = (s: number): string => ({ 0: 'info', 1: 'warning', 2: 'success', 3: '', 4: 'danger' }[s] || '')
const getPaymentMethodText = (m?: number): string => ({ 1: '转账', 2: '现金', 3: '支票', 4: '汇票', 5: '其他' }[m!] || '-')

const formatAmount = (v?: number) => v != null ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '-'
const formatTime = (t?: string) => t ? t.replace('T', ' ') : '-'

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.page-container { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-area { margin-bottom: 16px; }
.search-form .el-form-item { margin-bottom: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

.amount-text {
  color: #f56c6c;
  font-weight: bold;
  &.large { font-size: 18px; }
}
</style>
