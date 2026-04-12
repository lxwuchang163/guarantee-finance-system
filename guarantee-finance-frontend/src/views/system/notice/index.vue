<template>
  <div class="notice-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="queryParams.keyword" placeholder="搜索公告标题/内容" clearable style="width: 220px" @keyup.enter="handleSearch" />
          <el-select v-model="queryParams.noticeType" placeholder="公告类型" clearable style="width: 140px" @change="handleSearch">
            <el-option label="系统公告" value="system" />
            <el-option label="业务公告" value="business" />
            <el-option label="财务公告" value="finance" />
          </el-select>
          <el-select v-model="queryParams.status" placeholder="发布状态" clearable style="width: 120px" @change="handleSearch">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <el-button type="primary" @click="handleAdd">新增公告</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="noticeTypeTag(row.noticeType)">{{ noticeTypeLabel(row.noticeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="topFlag" label="置顶" width="70" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.topFlag === 1" color="#e6a23c" size="18"><Star /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1" link type="warning" size="small" @click="handleUnpublish(row)">取消发布</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公告标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-select v-model="formData.noticeType" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="系统公告" value="system" />
            <el-option label="业务公告" value="business" />
            <el-option label="财务公告" value="finance" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否置顶" prop="topFlag">
          <el-switch v-model="formData.topFlag" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import {
  getNoticePage,
  createNotice,
  updateNotice,
  deleteNotice,
  publishNotice,
  unpublishNotice,
  type NoticeDTO
} from '@/api/system/notice'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<NoticeDTO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增公告')
const isEdit = ref(false)
const formRef = ref()

const queryParams = reactive({
  keyword: '',
  noticeType: '',
  status: undefined as number | undefined,
  current: 1,
  size: 10
})

const formData = reactive<NoticeDTO>({
  id: undefined,
  title: '',
  content: '',
  noticeType: 'system',
  topFlag: 0,
  remark: ''
})

const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const noticeTypeLabel = (type: string) => {
  const map: Record<string, string> = { system: '系统', business: '业务', finance: '财务' }
  return map[type] || type
}

const noticeTypeTag = (type: string) => {
  const map: Record<string, string> = { system: '', business: 'warning', finance: 'success' }
  return map[type] || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNoticePage(queryParams)
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadData()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.noticeType = ''
  queryParams.status = undefined
  queryParams.current = 1
  loadData()
}

const resetForm = () => {
  formData.id = undefined
  formData.title = ''
  formData.content = ''
  formData.noticeType = 'system'
  formData.topFlag = 0
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '新增公告'
  dialogVisible.value = true
}

const handleEdit = (row: NoticeDTO) => {
  Object.assign(formData, { ...row })
  isEdit.value = true
  dialogTitle.value = '编辑公告'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateNotice(formData.id, formData)
      ElMessage.success('修改成功')
    } else {
      await createNotice(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

const handlePublish = async (row: NoticeDTO) => {
  try {
    await publishNotice(row.id!)
    ElMessage.success('发布成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleUnpublish = async (row: NoticeDTO) => {
  try {
    await unpublishNotice(row.id!)
    ElMessage.success('已取消发布')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row: NoticeDTO) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', { type: 'warning' })
    await deleteNotice(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // cancelled
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notice-container {
  padding: 16px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
