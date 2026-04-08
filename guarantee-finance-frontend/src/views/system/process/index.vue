<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>审批流程管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增流程</button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 流程模板列表 -->
        <el-tab-pane label="流程模板" name="template">
          <!-- 搜索区域 -->
          <div class="search-area">
            <el-form :inline="true" :model="queryParams" class="search-form">
              <el-form-item label="流程名称">
                <el-input v-model="queryParams.name" placeholder="请输入流程名称" clearable style="width: 180px" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
                  <el-option :value="1" label="启用" />
                  <el-option :value="0" label="禁用" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="loadDefinitions">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="definitionList" stripe border v-loading="loading">
            <el-table-column prop="name" label="流程名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="businessType" label="业务类型" width="140" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="version" label="版本号" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="(val: number) => handleDefStatusChange(row, val)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="queryPage.current"
              v-model:page-size="queryPage.size"
              :page-sizes="[10, 20, 50]"
              :total="definitionTotal"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="loadDefinitions"
              @current-change="loadDefinitions"
            />
          </div>
        </el-tab-pane>

        <!-- 我的审批（待我审批） -->
        <el-tab-pane label="我的审批" name="pending">
          <el-table :data="pendingList" stripe border v-loading="pendingLoading">
            <el-table-column prop="businessType" label="业务类型" width="120" />
            <el-table-column prop="definitionName" label="流程名称" width="150" />
            <el-table-column prop="currentNodeName" label="当前节点" width="120" />
            <el-table-column prop="initiatorName" label="发起人" width="100" />
            <el-table-column prop="initiatorTime" label="发起时间" width="170">
              <template #default="{ row }">{{ formatTime(row.initiatorTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link size="small" @click="handleApprove(row)">同意</el-button>
                <el-button type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
                <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!pendingLoading && pendingList.length === 0" description="暂无待审批的流程" />
        </el-tab-pane>

        <!-- 我发起的 -->
        <el-tab-pane label="我发起的" name="initiated">
          <el-table :data="initiatedList" stripe border v-loading="initiatedLoading">
            <el-table-column prop="businessType" label="业务类型" width="120" />
            <el-table-column prop="definitionName" label="流程名称" width="150" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentNodeName" label="当前节点" width="120" />
            <el-table-column prop="initiatorTime" label="发起时间" width="170">
              <template #default="{ row }">{{ formatTime(row.initiatorTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
                <el-button
                  v-if="row.status === 1"
                  type="warning"
                  link
                  size="small"
                  @click="handleWithdraw(row)"
                >撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!initiatedLoading && initiatedList.length === 0" description="暂无已发起的流程" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑流程对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.id ? '编辑流程' : '新增流程'"
      width="800px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="流程名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入流程名称" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务类型" prop="businessType">
              <el-input v-model="formData.businessType" placeholder="如：收款单/付款单/凭证调整" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入流程描述" :rows="2" maxlength="255" show-word-limit />
        </el-form-item>

        <el-divider content-position="left">审批节点配置</el-divider>

        <div class="node-list">
          <div
            v-for="(node, index) in formData.nodes"
            :key="index"
            class="node-item"
          >
            <div class="node-header">
              <span class="node-index">节点 {{ index + 1 }}</span>
              <el-button
                type="danger"
                link
                size="small"
                icon="Delete"
                @click="removeNode(index)"
                :disabled="formData.nodes!.length <= 1"
              >删除</el-button>
            </div>
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="节点名称" :prop="'nodes.' + index + '.nodeName'" label-width="70px">
                  <el-input v-model="node.nodeName" placeholder="如：部门经理审批" maxlength="30" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="审批人类型" :prop="'nodes.' + index + '.approverType'" label-width="90px">
                  <el-select v-model="node.approverType" style="width: 100%">
                    <el-option value="role" label="按角色" />
                    <el-option value="user" label="指定用户" />
                    <el-option value="specific" label="特定人员" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="审批人值" :prop="'nodes.' + index + '.approverValue'" label-width="80px">
                  <el-input v-model="node.approverValue" :placeholder="getApproverPlaceholder(node.approverType)" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </div>

        <el-button type="primary" plain icon="Plus" @click="addNode" style="margin-top: 8px">添加审批节点</el-button>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ formData.id ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 审批意见对话框 -->
    <el-dialog v-model="approveDialogVisible" :title="approveAction === 'approve' ? '同意' : '驳回'" width="500px">
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="80px">
        <el-form-item label="审批意见" prop="opinion">
          <el-input
            v-model="approveForm.opinion"
            type="textarea"
            :placeholder="approveAction === 'approve' ? '请输入同意意见（可选）' : '请输入驳回原因'"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button :type="approveAction === 'approve' ? 'success' : 'danger'" @click="submitApprove" :loading="approving">
          确认{{ approveAction === 'approve' ? '同意' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="流程详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="流程名称">{{ detailData.definitionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detailData.businessType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusTagType(detailData.status)" size="small">{{ getStatusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ detailData.currentNodeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ detailData.initiatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ formatTime(detailData.initiatorTime) }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin-top: 16px; margin-bottom: 8px">审批历史</h4>
      <el-timeline v-if="historyList && historyList.length > 0">
        <el-timeline-item
          v-for="record in historyList"
          :key="record.id"
          :timestamp="formatTime(record.approveTime)"
          placement="top"
          :type="record.result === 'approve' ? 'success' : record.result === 'reject' ? 'danger' : 'primary'"
        >
          <div class="timeline-content">
            <strong>{{ record.nodeName }}</strong> - {{ record.approverName }}
            <el-tag :type="record.result === 'approve' ? 'success' : 'danger'" size="small" style="margin-left: 8px">
              {{ record.result === 'approve' ? '同意' : '驳回' }}
            </el-tag>
            <p v-if="record.opinion" style="color: #666; margin-top: 4px; margin-bottom: 0;">{{ record.opinion }}</p>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审批记录" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProcessDefinitionList,
  addProcessDefinition,
  updateProcessDefinition,
  deleteProcessDefinition as deleteDefApi,
  updateProcessDefStatus,
  getMyPending,
  getMyInitiated,
  approveProcess,
  rejectProcess,
  withdrawProcess,
  getApproveHistory,
  getInstanceDetail
} from '@/api/process'
import type { ProcessDefinitionVO, ProcessInstanceVO, ProcessDTO, ProcessNodeDTO, ApproveRecordVO } from '@/api/process'

const formRef = ref()
const approveFormRef = ref()
const loading = ref(false)
const submitting = ref(false)
const pendingLoading = ref(false)
const initiatedLoading = ref(false)
const approving = ref(false)

// Tab切换
const activeTab = ref('template')

// 流程模板列表
const definitionList = ref<ProcessDefinitionVO[]>([])
const definitionTotal = ref(0)
const queryParams = reactive({ name: '', status: null as number | null })
const queryPage = reactive({ current: 1, size: 10 })

// 待审批列表
const pendingList = ref<ProcessInstanceVO[]>([])

// 已发起列表
const initiatedList = ref<ProcessInstanceVO[]>([])

// 对话框
const dialogVisible = ref(false)
const approveDialogVisible = ref(false)
const detailDialogVisible = ref(false)

// 表单数据
const formData = ref<ProcessDTO>({
  name: '',
  businessType: '',
  description: '',
  status: 1,
  nodes: [createEmptyNode()]
})

const formRules = {
  name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  businessType: [{ required: true, message: '请输入业务类型', trigger: 'blur' }]
}

// 审批表单
const approveAction = ref<'approve' | 'reject'>('approve')
const currentInstance = ref<ProcessInstanceVO | null>(null)
const approveForm = reactive({ opinion: '' })
const approveRules = {}

// 详情
const detailData = ref<ProcessInstanceVO | null>(null)
const historyList = ref<ApproveRecordVO[]>([])

function createEmptyNode(): ProcessNodeDTO {
  return { nodeName: '', nodeType: 'approve', approverType: 'role', approverValue: '' }
}

function getApproverPlaceholder(type?: string) {
  switch (type) {
    case 'role': return '角色编码，如：FINANCE_MANAGER'
    case 'user': return '用户ID'
    case 'specific': return '指定人员标识'
    default: return '请选择审批人类型后填写'
  }
}

// 加载流程模板列表
const loadDefinitions = async () => {
  loading.value = true
  try {
    const res = await getProcessDefinitionList(queryParams)
    definitionList.value = res.data as ProcessDefinitionVO[]
    definitionTotal.value = (res.data as ProcessDefinitionVO[]).length
  } catch (error) {
    console.error('加载流程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载待审批列表
const loadPending = async () => {
  pendingLoading.value = true
  try {
    const res = await getMyPending()
    pendingList.value = res.data as ProcessInstanceVO[]
  } catch (error) {
    console.error('加载待审批列表失败:', error)
  } finally {
    pendingLoading.value = false
  }
}

// 加载已发起列表
const loadInitiated = async () => {
  initiatedLoading.value = true
  try {
    const res = await getMyInitiated()
    initiatedList.value = res.data as ProcessInstanceVO[]
  } catch (error) {
    console.error('加载已发起列表失败:', error)
  } finally {
    initiatedLoading.value = false
  }
}

// Tab切换处理
const handleTabChange = (tab: string) => {
  if (tab === 'pending') loadPending()
  else if (tab === 'initiated') loadInitiated()
}

// 搜索/重置
const resetQuery = () => {
  queryParams.name = ''
  queryParams.status = null
  queryPage.current = 1
  loadDefinitions()
}

// 新增
const handleAdd = () => {
  dialogVisible.value = true
  formData.value = { name: '', businessType: '', description: '', status: 1, nodes: [createEmptyNode()] }
}

// 编辑
const handleEdit = (row: ProcessDefinitionVO) => {
  dialogVisible.value = true
  formData.value = {
    id: row.id,
    name: row.name,
    businessType: row.businessType,
    description: row.description || '',
    status: row.status,
    nodes: row.nodes && row.nodes.length > 0 ? [...row.nodes] : [createEmptyNode()]
  }
}

// 添加节点
const addNode = () => {
  formData.value.nodes!.push(createEmptyNode())
}

// 删除节点
const removeNode = (index: number) => {
  formData.value.nodes!.splice(index, 1)
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 校验节点信息
  for (let i = 0; i < formData.value.nodes!.length; i++) {
    const node = formData.value.nodes![i]
    if (!node.nodeName) {
      ElMessage.warning(`第${i + 1}个节点的名称不能为空`)
      return
    }
    if (!node.approverValue) {
      ElMessage.warning(`第${i + 1}个节点的审批人不能为空`)
      return
    }
  }

  submitting.value = true
  try {
    if (formData.value.id) {
      await updateProcessDefinition(formData.value as ProcessDTO)
      ElMessage.success('更新成功')
    } else {
      await addProcessDefinition(formData.value as ProcessDTO)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadDefinitions()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该流程定义吗？如果存在进行中的实例则无法删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDefApi(id)
    ElMessage.success('删除成功')
    loadDefinitions()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

// 状态变更
const handleDefStatusChange = async (row: ProcessDefinitionVO, val: number) => {
  try {
    await updateProcessDefStatus(row.id, val)
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
    row.status = val
  } catch (error) {
    row.status = val === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 同意
const handleApprove = (row: ProcessInstanceVO) => {
  currentInstance.value = row
  approveAction.value = 'approve'
  approveForm.opinion = ''
  approveDialogVisible.value = true
}

// 驳回
const handleReject = (row: ProcessInstanceVO) => {
  currentInstance.value = row
  approveAction.value = 'reject'
  approveForm.opinion = ''
  approveDialogVisible.value = true
}

// 提交审批
const submitApprove = async () => {
  if (!currentInstance.value) return

  approving.value = true
  try {
    if (approveAction.value === 'approve') {
      await approveProcess(currentInstance.value.id, approveForm.opinion)
      ElMessage.success('已同意')
    } else {
      await rejectProcess(currentInstance.value.id, approveForm.opinion)
      ElMessage.success('已驳回')
    }
    approveDialogVisible.value = false
    loadPending()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    approving.value = false
  }
}

// 撤回
const handleWithdraw = async (row: ProcessInstanceVO) => {
  try {
    await ElMessageBox.confirm('确定要撤回该流程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await withdrawProcess(row.id)
    ElMessage.success('已撤回')
    loadInitiated()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '撤回失败')
  }
}

// 查看详情
const handleViewDetail = async (row: ProcessInstanceVO) => {
  try {
    const res = await getInstanceDetail(row.id)
    detailData.value = res.data as ProcessInstanceVO

    // 加载审批历史
    const historyRes = await getApproveHistory(row.id)
    historyList.value = historyRes.data as ApproveRecordVO[]

    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取流程详情失败:', error)
  }
}

// 状态相关工具函数
const getStatusText = (status: number): string => {
  const map: Record<number, string> = { 0: '进行中', 1: '待审批', 2: '已通过', 3: '已驳回', 4: '已撤回' }
  return map[status] || '未知'
}

const getStatusTagType = (status: number): string => {
  const map: Record<number, string> = { 0: '', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || ''
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

onMounted(() => {
  loadDefinitions()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 16px;
}

.search-form .el-form-item {
  margin-bottom: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.node-list {
  max-height: 400px;
  overflow-y: auto;

  .node-item {
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 8px;
    background-color: #fafafa;

    .node-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .node-index {
        font-weight: bold;
        color: #409eff;
        font-size: 14px;
      }
    }
  }
}

.timeline-content {
  p {
    font-size: 13px;
  }
}
</style>
