<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>凭证管理</span>
          <div class="action-bar">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增凭证
            </el-button>
            <el-button @click="handleImport">
              <el-icon><Upload /></el-icon> 导入凭证
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="mb-4">
          <el-form-item label="凭证编号">
            <el-input v-model="searchForm.voucherNo" placeholder="请输入凭证编号" clearable />
          </el-form-item>
          <el-form-item label="会计期间">
            <el-input v-model="searchForm.period" placeholder="如：2024-01" clearable />
          </el-form-item>
          <el-form-item label="凭证日期">
            <el-date-picker
              v-model="searchForm.voucherDate"
              type="date"
              placeholder="选择日期"
              clearable
              format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="草稿" value="0" />
              <el-option label="已提交" value="1" />
              <el-option label="已审核" value="2" />
              <el-option label="已记账" value="3" />
              <el-option label="已作废" value="4" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 凭证表格 -->
      <el-table
        v-loading="loading"
        :data="vouchers"
        style="width: 100%"
        border
      >
        <el-table-column prop="voucherNo" label="凭证编号" width="180" />
        <el-table-column prop="voucherDate" label="凭证日期" width="120" />
        <el-table-column prop="period" label="会计期间" width="100" />
        <el-table-column prop="summary" label="摘要" min-width="200" />
        <el-table-column prop="voucherTypeText" label="凭证类型" width="120" />
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row.id)">
              查看
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              @click="handleEdit(scope.row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="primary"
              @click="handleSubmit(scope.row.id)"
            >
              提交
            </el-button>
            <el-button
              v-if="scope.row.status !== 4"
              size="small"
              type="danger"
              @click="handleVoid(scope.row.id)"
            >
              作废
            </el-button>
            <el-button
              v-if="scope.row.status === 4"
              size="small"
              type="success"
              @click="handleRestore(scope.row.id)"
            >
              恢复
            </el-button>
            <el-button
              v-if="scope.row.status !== 3 && scope.row.status !== 4"
              size="small"
              type="danger"
              @click="handleDelete(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageInfo.current"
          v-model:page-size="pageInfo.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增凭证' : '编辑凭证'"
      width="800px"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        label-width="100px"
      >
        <el-form-item label="凭证日期" prop="voucherDate">
          <el-date-picker
            v-model="formData.voucherDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会计期间" prop="period">
          <el-input v-model="formData.period" placeholder="如：2024-01" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="formData.summary" placeholder="请输入摘要" />
        </el-form-item>
        <el-form-item label="凭证类型" prop="voucherType">
          <el-select v-model="formData.voucherType" placeholder="请选择凭证类型">
            <el-option label="记账凭证" value="1" />
            <el-option label="收款凭证" value="2" />
            <el-option label="付款凭证" value="3" />
            <el-option label="转账凭证" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型" prop="sourceType">
          <el-select v-model="formData.sourceType" placeholder="请选择来源类型">
            <el-option label="手工" value="0" />
            <el-option label="自动" value="1" />
            <el-option label="导入" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea" />
        </el-form-item>

        <!-- 凭证明细 -->
        <el-form-item label="凭证分录">
          <el-button type="primary" size="small" @click="addDetail">
            <el-icon><Plus /></el-icon> 新增分录
          </el-button>
          <div class="detail-list">
            <el-table
              :data="formData.details"
              style="width: 100%"
              border
              class="mt-2"
            >
              <el-table-column label="行号" width="60">
                <template #default="scope">
                  {{ scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column label="科目" min-width="200">
                <template #default="scope">
                  <el-select
                    v-model="scope.row.subjectCode"
                    placeholder="选择科目"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="subject in subjectOptions"
                      :key="subject.subjectCode"
                      :label="`${subject.subjectCode} - ${subject.subjectName}`"
                      :value="subject.subjectCode"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="摘要" width="150">
                <template #default="scope">
                  <el-input v-model="scope.row.summary" placeholder="摘要" />
                </template>
              </el-table-column>
              <el-table-column label="借方金额" width="120">
                <template #default="scope">
                  <el-input-number
                    v-model="scope.row.debitAmount"
                    :min="0"
                    :step="0.01"
                    :precision="2"
                  />
                </template>
              </el-table-column>
              <el-table-column label="贷方金额" width="120">
                <template #default="scope">
                  <el-input-number
                    v-model="scope.row.creditAmount"
                    :min="0"
                    :step="0.01"
                    :precision="2"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="danger"
                    @click="removeDetail(scope.$index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload } from '@element-plus/icons-vue'
import * as voucherApi from '@/api/accounting/voucher'
import * as subjectApi from '@/api/accounting/subject'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const currentId = ref(0)

const searchForm = reactive<Record<string, any>>({
  voucherNo: '',
  period: '',
  voucherDate: '',
  status: undefined
})

const pageInfo = reactive({
  current: 1,
  size: 20
})

const total = ref(0)
const vouchers = ref<any[]>([])
const subjectOptions = ref<any[]>([])

const formData = reactive({
  voucherNo: '',
  voucherDate: '',
  period: '',
  summary: '',
  voucherType: 1,
  sourceType: '0',
  sourceId: '',
  remark: '',
  details: [
    {
      lineNo: 1,
      subjectCode: '',
      subjectName: '',
      summary: '',
      debitAmount: 0,
      creditAmount: 0,
      auxiliaryInfo: '',
      departmentCode: '',
      projectCode: '',
      customerCode: '',
      supplierCode: '',
      businessCode: '',
      bankCode: '',
      remark: ''
    }
  ]
})

const rules = {
  voucherDate: [{ required: true, message: '请选择凭证日期', trigger: 'blur' }],
  period: [{ required: true, message: '请输入会计期间', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  voucherType: [{ required: true, message: '请选择凭证类型', trigger: 'change' }],
  sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }]
}

onMounted(() => {
  loadVouchers()
  loadSubjectOptions()
})

const loadVouchers = async () => {
  loading.value = true
  try {
    const response = await voucherApi.getVoucherPage({
      ...searchForm,
      page: pageInfo.current,
      size: pageInfo.size
    })
    vouchers.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('加载凭证失败')
  } finally {
    loading.value = false
  }
}

const loadSubjectOptions = async () => {
  try {
    const response = await subjectApi.getEnabledSubjects()
    subjectOptions.value = response.data
  } catch (error) {
    console.error('加载科目选项失败', error)
  }
}

const handleSearch = () => {
  pageInfo.current = 1
  loadVouchers()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.status = undefined
  pageInfo.current = 1
  loadVouchers()
}

const handleSizeChange = (size: number) => {
  pageInfo.size = size
  loadVouchers()
}

const handleCurrentChange = (current: number) => {
  pageInfo.current = current
  loadVouchers()
}

const handleAdd = () => {
  dialogType.value = 'add'
  currentId.value = 0
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (id: number) => {
  dialogType.value = 'edit'
  currentId.value = id
  try {
    const response = await voucherApi.getVoucherDetail(id)
    Object.assign(formData, response.data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载凭证详情失败')
  }
}

const handleView = async (id: number) => {
  try {
    const response = await voucherApi.getVoucherDetail(id)
    // TODO: 显示详情对话框
    console.log('凭证详情', response.data)
  } catch (error) {
    ElMessage.error('加载凭证详情失败')
  }
}

const handleSubmitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await voucherApi.createVoucher(formData)
          ElMessage.success('新增凭证成功')
        } else {
          await voucherApi.updateVoucher(currentId.value, formData)
          ElMessage.success('编辑凭证成功')
        }
        dialogVisible.value = false
        loadVouchers()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleSubmit = async (id: number) => {
  try {
    await voucherApi.submitVoucher(id)
    ElMessage.success('提交凭证成功')
    loadVouchers()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

const handleVoid = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要作废该凭证吗？', '作废确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await voucherApi.voidVoucher(id)
    ElMessage.success('作废凭证成功')
    loadVouchers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '作废失败')
    }
  }
}

const handleRestore = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要恢复该凭证吗？', '恢复确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await voucherApi.restoreVoucher(id)
    ElMessage.success('恢复凭证成功')
    loadVouchers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '恢复失败')
    }
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该凭证吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await voucherApi.deleteVoucher(id)
    ElMessage.success('删除凭证成功')
    loadVouchers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleImport = () => {
  ElMessage.info('导入功能开发中')
}

const addDetail = () => {
  formData.details.push({
    lineNo: formData.details.length + 1,
    subjectCode: '',
    subjectName: '',
    summary: '',
    debitAmount: 0,
    creditAmount: 0,
    auxiliaryInfo: '',
    departmentCode: '',
    projectCode: '',
    customerCode: '',
    supplierCode: '',
    businessCode: '',
    bankCode: '',
    remark: ''
  })
}

const removeDetail = (index: number) => {
  formData.details.splice(index, 1)
  // 更新行号
  formData.details.forEach((detail, idx) => {
    detail.lineNo = idx + 1
  })
}

const resetForm = () => {
  formData.voucherNo = ''
  formData.voucherDate = ''
  formData.period = ''
  formData.summary = ''
  formData.voucherType = 1
  formData.sourceType = '0'
  formData.sourceId = ''
  formData.remark = ''
  formData.details = [
    {
      lineNo: 1,
      subjectCode: '',
      subjectName: '',
      summary: '',
      debitAmount: 0,
      creditAmount: 0,
      auxiliaryInfo: '',
      departmentCode: '',
      projectCode: '',
      customerCode: '',
      supplierCode: '',
      businessCode: '',
      bankCode: '',
      remark: ''
    }
  ]
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-bar {
  display: flex;
  gap: 8px;
}

.search-bar {
  margin-bottom: 20px;
}

.detail-list {
  margin-top: 10px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
