<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>科目管理</span>
          <div class="action-bar">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增科目
            </el-button>
            <el-button @click="handleImport">
              <el-icon><Upload /></el-icon> 导入科目
            </el-button>
            <el-button @click="handleValidate">
              <el-icon><Check /></el-icon> 校验科目
            </el-button>
            <el-button type="warning" @click="handleBalanceInit">
              <el-icon><Coin /></el-icon> 余额初始化
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="mb-4">
          <el-form-item label="科目编码">
            <el-input v-model="searchForm.subjectCode" placeholder="请输入科目编码" clearable />
          </el-form-item>
          <el-form-item label="科目名称">
            <el-input v-model="searchForm.subjectName" placeholder="请输入科目名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" value="1" />
              <el-option label="封存" value="0" />
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

      <el-table v-loading="loading" :data="subjects" style="width: 100%" border>
        <el-table-column prop="subjectCode" label="科目编码" width="150" />
        <el-table-column prop="subjectName" label="科目名称" width="200" />
        <el-table-column prop="subjectLevel" label="层级" width="80">
          <template #default="scope">
            {{ scope.row.subjectLevel }}级
          </template>
        </el-table-column>
        <el-table-column prop="parentCode" label="父科目编码" width="150" />
        <el-table-column prop="subjectType" label="科目类型" width="120">
          <template #default="scope">
            {{ getSubjectTypeText(scope.row.subjectType) }}
          </template>
        </el-table-column>
        <el-table-column prop="balanceDirection" label="余额方向" width="100">
          <template #default="scope">
            {{ scope.row.balanceDirection === 1 ? '借方' : '贷方' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '封存' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="handleChangeStatus(scope.row.id, scope.row.status === 1 ? 0 : 1)"
            >
              {{ scope.row.status === 1 ? '封存' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增科目' : '编辑科目'" width="700px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="科目编码" prop="subjectCode">
          <el-input v-model="formData.subjectCode" placeholder="请输入科目编码" />
        </el-form-item>
        <el-form-item label="科目名称" prop="subjectName">
          <el-input v-model="formData.subjectName" placeholder="请输入科目名称" />
        </el-form-item>
        <el-form-item label="科目层级" prop="subjectLevel">
          <el-input-number v-model="formData.subjectLevel" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="父科目编码" prop="parentCode">
          <el-select v-model="formData.parentCode" placeholder="请选择父科目" filterable>
            <el-option label="无" value="" />
            <el-option
              v-for="subject in subjectOptions"
              :key="subject.subjectCode"
              :label="`${subject.subjectCode} - ${subject.subjectName}`"
              :value="subject.subjectCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科目类型" prop="subjectType">
          <el-select v-model="formData.subjectType" placeholder="请选择科目类型">
            <el-option label="资产" value="1" />
            <el-option label="负债" value="2" />
            <el-option label="所有者权益" value="3" />
            <el-option label="成本" value="4" />
            <el-option label="损益" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="余额方向" prop="balanceDirection">
          <el-select v-model="formData.balanceDirection" placeholder="请选择余额方向">
            <el-option label="借方" value="1" />
            <el-option label="贷方" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="辅助核算" prop="auxiliaryDimension">
          <el-input v-model="formData.auxiliaryDimension" placeholder="JSON格式" type="textarea" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="系统类型" prop="systemType">
          <el-select v-model="formData.systemType" placeholder="请选择系统类型">
            <el-option label="标准" value="0" />
            <el-option label="自定义" value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="导入科目" width="500px">
      <div class="import-content">
        <el-alert type="info" :closable="false" class="mb-4">
          <template #title>
            <div>Excel文件格式要求：</div>
            <div>第1列：科目编码，第2列：科目名称，第3列：科目类型（资产/负债/所有者权益/成本/损益），第4列：余额方向（借/贷），第5列：父科目编码</div>
          </template>
        </el-alert>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :on-exceed="() => ElMessage.warning('只能上传一个文件')"
          drag
        >
          <el-icon size="60"><Upload /></el-icon>
          <div>将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">仅支持 .xlsx / .xls 格式</div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="doImport">确认导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="validateDialogVisible" title="科目校验结果" width="700px">
      <div v-if="validateResult">
        <el-result
          :icon="validateResult.passed ? 'success' : 'error'"
          :title="validateResult.passed ? '校验通过' : '校验未通过'"
          :sub-title="`共 ${validateResult.total} 个科目，${validateResult.errorCount} 个错误，${validateResult.warningCount} 个警告`"
        />
        <div v-if="validateResult.errors && validateResult.errors.length > 0" class="validate-section">
          <h4 style="color: #f56c6c">错误信息：</h4>
          <el-table :data="validateResult.errors.map((e: string, i: number) => ({ index: i + 1, message: e }))" border size="small">
            <el-table-column prop="index" label="序号" width="60" />
            <el-table-column prop="message" label="错误信息" />
          </el-table>
        </div>
        <div v-if="validateResult.warnings && validateResult.warnings.length > 0" class="validate-section">
          <h4 style="color: #e6a23c">警告信息：</h4>
          <el-table :data="validateResult.warnings.map((w: string, i: number) => ({ index: i + 1, message: w }))" border size="small">
            <el-table-column prop="index" label="序号" width="60" />
            <el-table-column prop="message" label="警告信息" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="balanceDialogVisible" title="科目余额初始化" width="900px" top="5vh">
      <div class="balance-toolbar">
        <el-form :inline="true">
          <el-form-item label="会计期间">
            <el-date-picker
              v-model="balancePeriod"
              type="month"
              placeholder="选择会计期间"
              format="YYYY-MM"
              value-format="YYYY-MM"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadBalanceData">查询</el-button>
            <el-button type="warning" @click="validateBalanceData">校验余额</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="balanceData" border style="width: 100%" max-height="500" size="small">
        <el-table-column prop="subjectCode" label="科目编码" width="130" />
        <el-table-column prop="subjectName" label="科目名称" width="180" />
        <el-table-column label="期初借方余额" width="180">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.beginDebit"
              :controls="false"
              :precision="2"
              :min="0"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="期初贷方余额" width="180">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.beginCredit"
              :controls="false"
              :precision="2"
              :min="0"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>
      </el-table>
      <div class="balance-summary">
        <span>借方合计：<strong>{{ totalDebit }}</strong></span>
        <span>贷方合计：<strong>{{ totalCredit }}</strong></span>
        <span :style="{ color: isBalanced ? '#67c23a' : '#f56c6c' }">
          差额：<strong>{{ balanceDifference }}</strong>
          {{ isBalanced ? '(已平衡)' : '(未平衡)' }}
        </span>
      </div>
      <template #footer>
        <el-button @click="balanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="balanceSaving" @click="saveBalanceData">保存初始化余额</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="balanceValidateDialogVisible" title="余额校验结果" width="700px">
      <div v-if="balanceValidateResult">
        <el-result
          :icon="balanceValidateResult.passed ? 'success' : 'error'"
          :title="balanceValidateResult.passed ? '余额校验通过' : '余额校验未通过'"
        >
          <template #sub-title>
            <div>
              <p>会计期间：{{ balanceValidateResult.period }}</p>
              <p>借方合计：{{ balanceValidateResult.totalDebit }} | 贷方合计：{{ balanceValidateResult.totalCredit }}</p>
              <p v-if="balanceValidateResult.difference">差额：{{ balanceValidateResult.difference }}</p>
              <p>已初始化科目数：{{ balanceValidateResult.balanceCount }}</p>
              <p v-if="balanceValidateResult.uninitializedCount > 0" style="color: #e6a23c">
                未初始化科目数：{{ balanceValidateResult.uninitializedCount }}
              </p>
            </div>
          </template>
        </el-result>
        <div v-if="balanceValidateResult.errors && balanceValidateResult.errors.length > 0" class="validate-section">
          <h4 style="color: #f56c6c">错误信息：</h4>
          <el-table :data="balanceValidateResult.errors.map((e: string, i: number) => ({ index: i + 1, message: e }))" border size="small">
            <el-table-column prop="index" label="序号" width="60" />
            <el-table-column prop="message" label="错误信息" />
          </el-table>
        </div>
        <div v-if="balanceValidateResult.uninitializedSubjects && balanceValidateResult.uninitializedSubjects.length > 0" class="validate-section">
          <h4 style="color: #e6a23c">未初始化科目（前20个）：</h4>
          <el-table :data="balanceValidateResult.uninitializedSubjects.map((s: string, i: number) => ({ index: i + 1, name: s }))" border size="small">
            <el-table-column prop="index" label="序号" width="60" />
            <el-table-column prop="name" label="科目" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload, Check, Coin } from '@element-plus/icons-vue'
import * as subjectApi from '@/api/accounting/subject'
import type { SubjectBalanceInitDTO } from '@/api/accounting/subject'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const currentId = ref(0)

const searchForm = reactive<Record<string, any>>({
  subjectCode: '',
  subjectName: '',
  status: undefined
})

const pageInfo = reactive({
  current: 1,
  size: 20
})

const total = ref(0)
const subjects = ref<any[]>([])
const subjectOptions = ref<any[]>([])

const formData = reactive<Record<string, any>>({
  subjectCode: '',
  subjectName: '',
  subjectLevel: 1,
  parentCode: '',
  subjectType: 1,
  balanceDirection: 1,
  auxiliaryDimension: '',
  description: '',
  sortOrder: 0,
  category: '',
  systemType: '0'
})

const rules = {
  subjectCode: [{ required: true, message: '请输入科目编码', trigger: 'blur' }],
  subjectName: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
  subjectLevel: [{ required: true, message: '请输入科目层级', trigger: 'blur' }],
  subjectType: [{ required: true, message: '请选择科目类型', trigger: 'change' }],
  balanceDirection: [{ required: true, message: '请选择余额方向', trigger: 'change' }]
}

const importDialogVisible = ref(false)
const importLoading = ref(false)
const uploadRef = ref()
const importFile = ref<File | null>(null)

const validateDialogVisible = ref(false)
const validateResult = ref<any>(null)

const balanceDialogVisible = ref(false)
const balanceSaving = ref(false)
const balancePeriod = ref('')
const balanceData = ref<SubjectBalanceInitDTO[]>([])

const balanceValidateDialogVisible = ref(false)
const balanceValidateResult = ref<any>(null)

const totalDebit = computed(() => {
  return balanceData.value.reduce((sum, item) => sum + (item.beginDebit || 0), 0).toFixed(2)
})

const totalCredit = computed(() => {
  return balanceData.value.reduce((sum, item) => sum + (item.beginCredit || 0), 0).toFixed(2)
})

const balanceDifference = computed(() => {
  const diff = parseFloat(totalDebit.value) - parseFloat(totalCredit.value)
  return diff.toFixed(2)
})

const isBalanced = computed(() => {
  return parseFloat(balanceDifference.value) === 0
})

onMounted(() => {
  loadSubjects()
  loadSubjectOptions()
})

const loadSubjects = async () => {
  loading.value = true
  try {
    const response = await subjectApi.getSubjectPage({
      ...searchForm,
      page: pageInfo.current,
      size: pageInfo.size
    })
    subjects.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('加载科目失败')
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
  loadSubjects()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.status = undefined
  pageInfo.current = 1
  loadSubjects()
}

const handleSizeChange = (size: number) => {
  pageInfo.size = size
  loadSubjects()
}

const handleCurrentChange = (current: number) => {
  pageInfo.current = current
  loadSubjects()
}

const handleAdd = () => {
  dialogType.value = 'add'
  currentId.value = 0
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  currentId.value = row.id
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const submitData = formData as any
        if (dialogType.value === 'add') {
          await subjectApi.createSubject(submitData)
          ElMessage.success('新增科目成功')
        } else {
          await subjectApi.updateSubject(currentId.value, submitData)
          ElMessage.success('编辑科目成功')
        }
        dialogVisible.value = false
        loadSubjects()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该科目吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await subjectApi.deleteSubject(id)
    ElMessage.success('删除科目成功')
    loadSubjects()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleChangeStatus = async (id: number, status: number) => {
  try {
    await subjectApi.changeSubjectStatus(id, status)
    ElMessage.success(`${status === 1 ? '启用' : '封存'}科目成功`)
    loadSubjects()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleImport = () => {
  importFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file: any) => {
  importFile.value = file.raw
}

const doImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  importLoading.value = true
  try {
    await subjectApi.importSubjects(importFile.value)
    ElMessage.success('科目导入成功')
    importDialogVisible.value = false
    loadSubjects()
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

const handleValidate = async () => {
  try {
    const response = await subjectApi.validateSubjects()
    validateResult.value = response.data
    validateDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '校验失败')
  }
}

const handleBalanceInit = () => {
  const now = new Date()
  balancePeriod.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  balanceData.value = []
  balanceDialogVisible.value = true
  loadBalanceData()
}

const loadBalanceData = async () => {
  try {
    const response = await subjectApi.getSubjectBalances(balancePeriod.value)
    balanceData.value = response.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载余额数据失败')
  }
}

const validateBalanceData = async () => {
  try {
    const response = await subjectApi.validateBalances(balancePeriod.value)
    balanceValidateResult.value = response.data
    balanceValidateDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '校验失败')
  }
}

const saveBalanceData = async () => {
  if (!isBalanced.value) {
    try {
      await ElMessageBox.confirm(
        '期初余额借贷不平衡，是否仍要保存？',
        '余额不平衡',
        { confirmButtonText: '仍然保存', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      return
    }
  }

  balanceSaving.value = true
  try {
    const saveData = balanceData.value.map(item => ({
      ...item,
      period: balancePeriod.value,
      year: parseInt(balancePeriod.value.split('-')[0]),
      month: parseInt(balancePeriod.value.split('-')[1])
    }))
    await subjectApi.initSubjectBalances(saveData)
    ElMessage.success('余额初始化保存成功')
    balanceDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    balanceSaving.value = false
  }
}

const resetForm = () => {
  Object.keys(formData).forEach(key => {
    formData[key] = ''
  })
  formData.subjectLevel = 1
  formData.subjectType = 1
  formData.balanceDirection = 1
  formData.sortOrder = 0
  formData.systemType = '0'
  formData.parentCode = ''
}

const getSubjectTypeText = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '资产',
    2: '负债',
    3: '所有者权益',
    4: '成本',
    5: '损益'
  }
  return typeMap[type] || '未知'
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

.import-content {
  padding: 10px 0;
}

.mb-4 {
  margin-bottom: 16px;
}

.validate-section {
  margin-top: 16px;
}

.balance-toolbar {
  margin-bottom: 16px;
}

.balance-summary {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  gap: 24px;
  justify-content: center;
  font-size: 14px;
}
</style>
