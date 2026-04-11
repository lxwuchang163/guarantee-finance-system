<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>辅助核算配置</span>
          <div class="action-bar">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增维度
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="mb-4">
          <el-form-item label="维度编码">
            <el-input v-model="searchForm.dimensionCode" placeholder="请输入维度编码" clearable />
          </el-form-item>
          <el-form-item label="维度名称">
            <el-input v-model="searchForm.dimensionName" placeholder="请输入维度名称" clearable />
          </el-form-item>
          <el-form-item label="维度类型">
            <el-select v-model="searchForm.dimensionType" placeholder="请选择维度类型" clearable>
              <el-option label="部门" value="dept" />
              <el-option label="项目" value="project" />
              <el-option label="客户" value="customer" />
              <el-option label="供应商" value="supplier" />
              <el-option label="业务" value="business" />
              <el-option label="银行" value="bank" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" value="1" />
              <el-option label="停用" value="0" />
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

      <!-- 维度表格 -->
      <el-table
        v-loading="loading"
        :data="dimensions"
        style="width: 100%"
        border
      >
        <el-table-column prop="dimensionCode" label="维度编码" width="150" />
        <el-table-column prop="dimensionName" label="维度名称" width="180" />
        <el-table-column prop="dimensionType" label="维度类型" width="120">
          <template #default="scope">
            {{ getDimensionTypeText(scope.row.dimensionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="dimensionLevel" label="层级" width="80">
          <template #default="scope">
            {{ scope.row.dimensionLevel }}级
          </template>
        </el-table-column>
        <el-table-column prop="parentCode" label="父维度编码" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="handleChangeStatus(scope.row.id, scope.row.status === 1 ? 0 : 1)"
            >
              {{ scope.row.status === 1 ? '停用' : '启用' }}
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
      :title="dialogType === 'add' ? '新增维度' : '编辑维度'"
      width="600px"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        label-width="120px"
      >
        <el-form-item label="维度编码" prop="dimensionCode">
          <el-input v-model="formData.dimensionCode" placeholder="请输入维度编码" />
        </el-form-item>
        <el-form-item label="维度名称" prop="dimensionName">
          <el-input v-model="formData.dimensionName" placeholder="请输入维度名称" />
        </el-form-item>
        <el-form-item label="维度类型" prop="dimensionType">
          <el-select v-model="formData.dimensionType" placeholder="请选择维度类型">
            <el-option label="部门" value="dept" />
            <el-option label="项目" value="project" />
            <el-option label="客户" value="customer" />
            <el-option label="供应商" value="supplier" />
            <el-option label="业务" value="business" />
            <el-option label="银行" value="bank" />
          </el-select>
        </el-form-item>
        <el-form-item label="维度层级" prop="dimensionLevel">
          <el-input-number v-model="formData.dimensionLevel" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="父维度编码" prop="parentCode">
          <el-select v-model="formData.parentCode" placeholder="请选择父维度">
            <el-option label="无" value="" />
            <el-option
              v-for="dimension in dimensionOptions"
              :key="dimension.dimensionCode"
              :label="`${dimension.dimensionCode} - ${dimension.dimensionName}`"
              :value="dimension.dimensionCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
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
import { Plus, Search } from '@element-plus/icons-vue'
import * as auxiliaryApi from '@/api/accounting/auxiliary'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const currentId = ref(0)

const searchForm = reactive<Record<string, any>>({
  dimensionCode: '',
  dimensionName: '',
  dimensionType: undefined,
  status: undefined
})

const pageInfo = reactive({
  current: 1,
  size: 20
})

const total = ref(0)
const dimensions = ref<any[]>([])
const dimensionOptions = ref<any[]>([])

const formData = reactive<Record<string, any>>({
  dimensionCode: '',
  dimensionName: '',
  dimensionType: '',
  description: '',
  sortOrder: 0,
  parentCode: '',
  dimensionLevel: 1
})

const rules = {
  dimensionCode: [{ required: true, message: '请输入维度编码', trigger: 'blur' }],
  dimensionName: [{ required: true, message: '请输入维度名称', trigger: 'blur' }],
  dimensionType: [{ required: true, message: '请选择维度类型', trigger: 'change' }],
  dimensionLevel: [{ required: true, message: '请输入维度层级', trigger: 'blur' }]
}

onMounted(() => {
  loadDimensions()
  loadDimensionOptions()
})

const loadDimensions = async () => {
  loading.value = true
  try {
    const response = await auxiliaryApi.getDimensionPage({
      ...searchForm,
      page: pageInfo.current,
      size: pageInfo.size
    })
    dimensions.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('加载维度失败')
  } finally {
    loading.value = false
  }
}

const loadDimensionOptions = async () => {
  try {
    const response = await auxiliaryApi.getEnabledDimensions()
    dimensionOptions.value = response.data
  } catch (error) {
    console.error('加载维度选项失败', error)
  }
}

const handleSearch = () => {
  pageInfo.current = 1
  loadDimensions()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.dimensionType = undefined
  searchForm.status = undefined
  pageInfo.current = 1
  loadDimensions()
}

const handleSizeChange = (size: number) => {
  pageInfo.size = size
  loadDimensions()
}

const handleCurrentChange = (current: number) => {
  pageInfo.current = current
  loadDimensions()
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
          await auxiliaryApi.createDimension(submitData)
          ElMessage.success('新增维度成功')
        } else {
          await auxiliaryApi.updateDimension(currentId.value, submitData)
          ElMessage.success('编辑维度成功')
        }
        dialogVisible.value = false
        loadDimensions()
        loadDimensionOptions()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该维度吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await auxiliaryApi.deleteDimension(id)
    ElMessage.success('删除维度成功')
    loadDimensions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleChangeStatus = async (id: number, status: number) => {
  try {
    await auxiliaryApi.changeDimensionStatus(id, status)
    ElMessage.success(`${status === 1 ? '启用' : '停用'}维度成功`)
    loadDimensions()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const resetForm = () => {
  Object.keys(formData).forEach(key => {
    formData[key] = ''
  })
  formData.dimensionLevel = 1
  formData.sortOrder = 0
  formData.parentCode = ''
}

const getDimensionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    dept: '部门',
    project: '项目',
    customer: '客户',
    supplier: '供应商',
    business: '业务',
    bank: '银行'
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
