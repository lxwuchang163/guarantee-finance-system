<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header"><span>付款单管理</span><el-button type="primary" icon="Plus" @click="handleAdd">新增付款单</el-button></div>
      </template>

      <!-- 搜索 -->
      <div class="search-area">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="关键词"><el-input v-model="queryParams.keyword" placeholder="单号/客户/合同" clearable style="width: 180px" /></el-form-item>
          <el-form-item label="业务类型">
            <el-select v-model="queryParams.businessType" clearable placeholder="请选择" style="width: 140px">
              <el-option :value="1" label="退费支出" /><el-option :value="2" label="代偿支出" /><el-option :value="3" label="追回资金分配" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" clearable placeholder="请选择" style="width: 110px">
              <el-option :value="0" label="草稿" /><el-option :value="1" label="已提交" /><el-option :value="2" label="已审核" />
              <el-option :value="3" label="已付款" /><el-option :value="4" label="已记账" /><el-option :value="5" label="已作废" />
            </el-select>
          </el-form-item>
          <el-form-item label="付款日期">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 240px" />
          </el-form-item>
          <el-form-item><el-button type="primary" icon="Search" @click="loadData">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="paymentNo" label="付款单号" width="160" fixed="left">
          <template #default="{ row }"><el-link type="primary" @click="handleView(row)">{{ row.paymentNo }}</el-link></template>
        </el-table-column>
        <el-table-column prop="businessTypeName" label="业务类型" width="110" align="center">
          <template #default="{ row }"><el-tag :type="getBusinessTypeTag(row.businessType)" size="small">{{ row.businessTypeName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }"><span class="amount-text">¥{{ formatAmount(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="付款日期" width="115" />
        <el-table-column prop="status" label="状态" width="85" align="center">
          <template #default="{ row }"><el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="makerName" label="制单人" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="165"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleAudit(row, true)">审核通过</el-button>
            <el-button v-if="row.status === 2" type="primary" link size="small" @click="handlePay(row)">执行付款</el-button>
            <el-button v-if="row.status === 3" type="success" link size="small" @click="handlePost(row)">记账</el-button>
            <el-button v-if="row.status >= 4" type="info" link size="small" @click="handleReverse(row)">冲销</el-button>
            <el-button v-if="row.status <= 0" type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="queryPage.current" v-model:page-size="queryPage.size" :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑付款单' : '新增付款单'" width="750px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务类型" prop="businessType">
              <el-select v-model="formData.businessType" placeholder="请选择" style="width: 100%">
                <el-option :value="1" label="退费支出" /><el-option :value="2" label="代偿支出" /><el-option :value="3" label="追回资金分配" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户编码" prop="customerCode"><el-input v-model="formData.customerCode" maxlength="30" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName"><el-input v-model="formData.customerName" maxlength="100" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同编号"><el-input v-model="formData.contractNo" maxlength="50" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="付款金额" prop="amount">
              <el-input-number v-model="formData.amount" :precision="2" :min="0.01" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款日期" prop="paymentDate">
              <el-date-picker v-model="formData.paymentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收款方名称" prop="payeeName"><el-input v-model="formData.payeeName" maxlength="100" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收款方账号"><el-input v-model="formData.payeeAccountNo" maxlength="30" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="用途/摘要"><el-input v-model="formData.usage" type="textarea" :rows="2" maxlength="200" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmitForm" :loading="submitting">保存</el-button></template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="付款单详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="付款单号" :span="2">{{ detailData.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="业务类型"><el-tag :type="getBusinessTypeTag(detailData.businessType)" size="small">{{ detailData.businessTypeName }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="getStatusTagType(detailData.status)" size="small">{{ getStatusText(detailData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="合同编号">{{ detailData.contractNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款金额" :span="2"><span class="amount-text large">¥{{ formatAmount(detailData.amount) }}</span></el-descriptions-item>
        <el-descriptions-item label="收款方">{{ detailData.payeeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用途">{{ detailData.usage || '-' }}</el-descriptions-item>
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
  getPaymentPage, createPayment, updatePayment, deletePayment,
  submitPayment, auditPayment, executePay, postPayment, reversePayment
} from '@/api/payment'
import type { PaymentVO, PaymentDTO } from '@/api/payment'

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)

const queryParams = reactive({ keyword: '', businessType: null as number | null, status: null as number | null })
const dateRange = ref<string[]>([])
const queryPage = reactive({ current: 1, size: 10 })
const tableData = ref<PaymentVO[]>([])
const total = ref(0)

const formData = ref<PaymentDTO>({ businessType: 1, customerCode: '', customerName: '', currency: 'CNY', amount: undefined as any, paymentDate: '' })
const formRules = {
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  customerCode: [{ required: true, message: '请输入客户编码', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }],
  paymentDate: [{ required: true, message: '请选择付款日期', trigger: 'change' }]
}

const detailData = ref<PaymentVO | null>(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPaymentPage({ ...queryParams, startDate: dateRange.value?.[0], endDate: dateRange.value?.[1], current: queryPage.current, size: queryPage.size })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const resetQuery = () => { Object.assign(queryParams, { keyword: '', businessType: null, status: null }); dateRange.value = []; queryPage.current = 1; loadData() }

const handleAdd = () => { formData.value = { businessType: 1, customerCode: '', customerName: '', currency: 'CNY', amount: undefined as any, paymentDate: '' }; dialogVisible.value = true }
const handleEdit = (row: PaymentVO) => { formData.value = { id: row.id, businessType: row.businessType, customerCode: row.customerCode, customerName: row.customerName, contractNo: row.contractNo, originalReceiptNo: row.originalReceiptNo, currency: row.currency || 'CNY', amount: row.amount, paymentDate: row.paymentDate || '', actualPaymentDate: row.actualPaymentDate || '', paymentMethod: row.paymentMethod, payeeName: row.payeeName, payeeAccountNo: row.payeeAccountNo, payeeBankName: row.payeeBankName, payerAccountNo: row.payerAccountNo, payerBankName: row.payerBankName, usage: row.usage }; dialogVisible.value = true }

const handleSubmitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (formData.value.id) { await updatePayment(formData.value as PaymentDTO); ElMessage.success('修改成功') }
    else { await createPayment(formData.value as PaymentDTO); ElMessage.success('创建成功') }
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { submitting.value = false }
}

const handleSubmit = async (row: PaymentVO) => {
  try { await ElMessageBox.confirm(`确定提交付款单 ${row.paymentNo} 进行审核吗？`, '提示', { type: 'info' }); await submitPayment(row.id); ElMessage.success('已提交审核'); loadData() }
  catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '提交失败') }
}
const handleAudit = async (row: PaymentVO, pass: boolean) => {
  const action = pass ? '通过' : '驳回'
  try { await ElMessageBox.confirm(`确定${action}付款单 ${row.paymentNo} 吗？`, '确认', { confirmButtonText: `确定${action}`, type: pass ? 'success' : 'warning' }); await auditPayment(row.id, pass); ElMessage.success(`已${action}`); loadData() }
  catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') }
}
const handlePay = async (row: PaymentVO) => {
  try { await ElMessageBox.confirm(`确定对付款单 ${row.paymentNo} 执行付款吗？`, '确认付款', { type: 'warning' }); await executePay(row.id); ElMessage.success('付款指令已发送'); loadData() }
  catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '付款失败') }
}
const handlePost = async (row: PaymentVO) => {
  try { await ElMessageBox.confirm(`确定对付款单 ${row.paymentNo} 执行记账吗？`, '确认记账', { type: 'success' }); await postPayment(row.id); ElMessage.success('记账成功'); loadData() }
  catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '记账失败') }
}
const handleReverse = async (row: PaymentVO) => {
  try { await ElMessageBox.confirm(`确定冲销付款单 ${row.paymentNo} 吗？冲销后不可恢复！`, '警告', { type: 'error' }); await reversePayment(row.id); ElMessage.success('冲销成功'); loadData() }
  catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '冲销失败') }
}
const handleDelete = async (id: number) => {
  try { await ElMessageBox.confirm('确定要删除该付款单吗？', '警告', { type: 'error' }); await deletePayment(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '删除失败') }
}
const handleView = (row: PaymentVO) => { detailData.value = row; detailVisible.value = true }

const getBusinessTypeTag = (t?: number): string => ({ 1: 'danger', 2: 'warning', 3: '' }[t!] || '')
const getStatusText = (s: number): string => ({ 0: '草稿', 1: '已提交', 2: '已审核', 3: '已付款', 4: '已记账', 5: '已作废' }[s] || '未知')
const getStatusTagType = (s: number): string => ({ 0: 'info', 1: 'warning', 2: '', 3: 'success', 4: '', 5: 'danger' }[s] || '')

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
.amount-text { color: #f56c6c; font-weight: bold; &.large { font-size: 18px; } }
</style>
