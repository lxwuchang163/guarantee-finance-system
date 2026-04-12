<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>凭证规则管理</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="规则编码/名称" clearable />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryForm.businessType" placeholder="全部" clearable>
            <el-option v-for="t in businessTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" stripe border>
        <el-table-column prop="ruleCode" label="规则编码" width="150" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getBusinessTypeLabel(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="voucherType" label="凭证类型" width="100">
          <template #default="{ row }">
            {{ ['', '记账凭证', '收款凭证', '付款凭证', '转账凭证'][row.voucherType] || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" link @click="handleToggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryForm.current" v-model:page-size="queryForm.size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" width="800px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="规则编码"><el-input v-model="formData.ruleCode" :disabled="isEdit" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规则名称"><el-input v-model="formData.ruleName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="业务类型"><el-select v-model="formData.businessType" style="width:100%"><el-option v-for="t in businessTypes" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="业务子类型"><el-input v-model="formData.businessSubtype" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="凭证类型"><el-select v-model="formData.voucherType" style="width:100%"><el-option label="记账凭证" :value="1" /><el-option label="收款凭证" :value="2" /><el-option label="付款凭证" :value="3" /><el-option label="转账凭证" :value="4" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="优先级"><el-input-number v-model="formData.priority" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="摘要模板"><el-input v-model="formData.summaryTemplate" placeholder="支持变量: {businessNo}, {customerName}" /></el-form-item>
        <el-divider>分录配置</el-divider>
        <div v-for="(entry, index) in formData.entries" :key="index" style="margin-bottom:12px;padding:12px;border:1px solid #eee;border-radius:4px;">
          <el-row :gutter="10">
            <el-col :span="4"><el-form-item label="方向" label-width="50px"><el-select v-model="entry.entrySide"><el-option label="借方" value="debit" /><el-option label="贷方" value="credit" /></el-select></el-form-item></el-col>
            <el-col :span="5"><el-form-item label="科目来源" label-width="70px"><el-select v-model="entry.subjectSource"><el-option label="固定" value="fixed" /><el-option label="动态" value="dynamic" /></el-select></el-form-item></el-col>
            <el-col :span="5"><el-form-item label="科目编码" label-width="70px"><el-input v-model="entry.subjectCode" /></el-form-item></el-col>
            <el-col :span="5"><el-form-item label="金额来源" label-width="70px"><el-select v-model="entry.amountSource"><el-option label="字段" value="field" /><el-option label="固定" value="fixed" /><el-option label="公式" value="formula" /></el-select></el-form-item></el-col>
            <el-col :span="4"><el-form-item label="金额字段" label-width="70px"><el-input v-model="entry.amountField" /></el-form-item></el-col>
            <el-col :span="1"><el-button type="danger" link @click="formData.entries.splice(index, 1)">删除</el-button></el-col>
          </el-row>
        </div>
        <el-button type="primary" link @click="addEntry">+ 添加分录</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVoucherRulePage, getVoucherRuleDetail, createVoucherRule, updateVoucherRule, deleteVoucherRule, enableVoucherRule, disableVoucherRule, getBusinessTypes } from '@/api/accounting/voucherRule'

const queryForm = ref({ keyword: '', businessType: '', status: undefined as number | undefined, current: 1, size: 10 })
const tableData = ref<any[]>([])
const total = ref(0)
const businessTypes = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = ref<any>({ entries: [] })

const loadData = async () => {
  const res = await getVoucherRulePage(queryForm.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const getBusinessTypeLabel = (val: string) => {
  const t = businessTypes.value.find((b: any) => b.value === val)
  return t ? t.label : val
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = { ruleCode: '', ruleName: '', businessType: 'receipt', voucherType: 2, priority: 0, status: 1, summaryTemplate: '', entries: [] }
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  isEdit.value = true
  const res = await getVoucherRuleDetail(row.id)
  formData.value = res.data || { entries: [] }
  if (!formData.value.entries) formData.value.entries = []
  dialogVisible.value = true
}

const handleToggleStatus = async (row: any) => {
  if (row.status === 1) { await disableVoucherRule(row.id); ElMessage.success('已禁用') }
  else { await enableVoucherRule(row.id); ElMessage.success('已启用') }
  loadData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确认删除此规则？', '提示', { type: 'warning' })
  await deleteVoucherRule(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const addEntry = () => {
  formData.value.entries.push({ lineNo: formData.value.entries.length + 1, entrySide: 'debit', subjectSource: 'fixed', subjectCode: '', amountSource: 'field', amountField: 'amount' })
}

const handleSubmit = async () => {
  if (isEdit.value) { await updateVoucherRule(formData.value.id, formData.value) }
  else { await createVoucherRule(formData.value) }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

onMounted(async () => {
  const res = await getBusinessTypes()
  businessTypes.value = res.data || []
  loadData()
})
</script>

<style scoped>
.page-container { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.el-pagination { margin-top: 16px; justify-content: flex-end; }
</style>
