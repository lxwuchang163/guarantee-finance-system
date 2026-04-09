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
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
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

      <!-- 科目表格 -->
      <el-table
        v-loading="loading"
        :data="subjects"
        style="width: 100%"
        border
      >
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
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="handleChangeStatus(scope.row.id, scope.row.status === 1 ? 0 : 1)"
            >
              {{ scope.row.status === 1 ? '封存' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">
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
      :title="dialogType === 'add' ? '新增科目' : '编辑科目'"
      width="700px"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        label-width="120px"
      >
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
          <el-select v-model="formData.parentCode" placeholder="请选择父科目">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload, Check } from '@element-plus/icons-vue'
import * as subjectApi from '@/api/accounting/subject'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const currentId = ref(0)

const searchForm = reactive({
  subjectCode: '',
  subjectName: '',
  status: undefined
})

const pageInfo = reactive({
  current: 1,
  size: 20
})

const total = ref(0)
const subjects = ref([])
const subjectOptions = ref([])

const formData = reactive({
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
        if (dialogType.value === 'add') {
          await subjectApi.createSubject(formData)
          ElMessage.success('新增科目成功')
        } else {
          await subjectApi.updateSubject(currentId.value, formData)
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
  ElMessage.info('导入功能开发中')
}

const handleValidate = async () => {
  try {
    const response = await subjectApi.validateSubjects()
    ElMessage.success('科目校验成功')
  } catch (error: any) {
    ElMessage.error(error.message || '校验失败')
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
</style>
