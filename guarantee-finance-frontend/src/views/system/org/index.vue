<template>
  <div class="page-container">
    <div class="org-container">
      <!-- 左侧机构树 -->
      <div class="left-panel">
        <el-card shadow="hover" class="tree-card">
          <template #header>
            <div class="card-header">
              <span>机构树</span>
              <div class="header-btns">
                <el-button type="primary" link icon="Plus" @click="handleAdd(null)">新增</el-button>
                <el-dropdown @command="handleCommand">
                  <el-button type="primary" link icon="MoreFilled">更多</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="import">导入</el-dropdown-item>
                      <el-dropdown-item command="export">导出</el-dropdown-item>
                      <el-dropdown-item command="expandAll">全部展开</el-dropdown-item>
                      <el-dropdown-item command="collapseAll">全部折叠</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </template>

          <el-input
            v-model="filterText"
            placeholder="搜索机构..."
            clearable
            :prefix-icon="Search"
            class="search-input"
          />

          <el-tree
            ref="treeRef"
            :data="orgTree"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            :filter-node-method="filterNode"
            draggable
            :allow-drop="allowDrop"
            :allow-drag="allowDrag"
            @node-click="handleNodeClick"
            @node-drag-end="handleDragEnd"
            @node-contextmenu="handleRightClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <el-tag
                  v-if="data.status === 0"
                  size="small"
                  type="danger"
                  effect="dark"
                  style="margin-left: 8px"
                >禁用</el-tag>
              </span>
            </template>
          </el-tree>
        </el-card>
      </div>

      <!-- 右侧详情/编辑区 -->
      <div class="right-panel">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ currentOrg?.orgName ? `编辑机构 - ${currentOrg.orgName}` : '请选择机构' }}</span>
              <div v-if="currentOrg" class="action-btns">
                <el-button type="primary" icon="Edit" @click="handleEdit(currentOrg)">编辑</el-button>
                <el-button type="danger" icon="Delete" @click="handleDelete(currentOrg.id)">删除</el-button>
                <el-switch
                  v-model="currentOrg.status"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="启用"
                  inactive-text="禁用"
                  @change="handleStatusChange(currentOrg)"
                />
              </div>
            </div>
          </template>

          <!-- 机构详情展示 -->
          <div v-if="currentOrg && !showForm" class="org-detail">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="机构名称">{{ currentOrg.orgName }}</el-descriptions-item>
              <el-descriptions-item label="机构编码">{{ currentOrg.orgCode }}</el-descriptions-item>
              <el-descriptions-item label="上级机构">{{ currentOrg.parentName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="层级">{{ currentOrg.level || '-' }}级</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ currentOrg.leader || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentOrg.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ currentOrg.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="currentOrg.status === 1 ? 'success' : 'danger'">
                  {{ currentOrg.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="排序号">{{ currentOrg.sortOrder || 0 }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ currentOrg.remark || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatTime(currentOrg.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatTime(currentOrg.updateTime) }}</el-descriptions-item>
            </el-descriptions>

            <!-- 子机构列表 -->
            <div v-if="currentOrg.children && currentOrg.children.length > 0" class="children-section">
              <h4>子机构 ({{ currentOrg.children.length }})</h4>
              <el-table :data="currentOrg.children" stripe border size="small">
                <el-table-column prop="orgName" label="机构名称" />
                <el-table-column prop="orgCode" label="机构编码" width="120" />
                <el-table-column prop="leader" label="负责人" width="100" />
                <el-table-column prop="phone" label="电话" width="120" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                      {{ row.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
                    <el-button type="primary" link size="small" @click="handleAdd(row)">新增子机构</el-button>
                    <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- 空状态提示 -->
          <el-empty v-if="!currentOrg && !showForm" description="请从左侧选择一个机构或点击新增按钮" />

          <!-- 编辑/新增表单 -->
          <div v-if="showForm" class="org-form">
            <el-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              label-width="100px"
              style="max-width: 600px"
            >
              <el-form-item label="上级机构" prop="parentId">
                <el-tree-select
                  v-model="formData.parentId"
                  :data="parentTreeOptions"
                  :props="{ label: 'label', value: 'id', children: 'children' }"
                  placeholder="选择上级机构（不选则为顶级）"
                  check-strictly
                  clearable
                  filterable
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item label="机构名称" prop="orgName">
                <el-input v-model="formData.orgName" placeholder="请输入机构名称" maxlength="50" show-word-limit />
              </el-form-item>

              <el-form-item label="机构编码" prop="orgCode">
                <el-input v-model="formData.orgCode" placeholder="请输入机构编码" maxlength="30" show-word-limit>
                  <template #append>
                    <el-button :icon="Check" @click="checkCodeUnique" :loading="checkingCode" />
                  </template>
                </el-input>
                <div v-if="codeUnique !== null" class="code-check-result">
                  <el-icon v-if="codeUnique" color="#67c23a"><CircleCheckFilled /></el-icon>
                  <el-icon v-else color="#f56c6c"><CircleCloseFilled /></el-icon>
                  <span :style="{ color: codeUnique ? '#67c23a' : '#f56c6c' }">
                    {{ codeUnique ? '编码可用' : '编码已存在' }}
                  </span>
                </div>
              </el-form-item>

              <el-form-item label="负责人" prop="leader">
                <el-input v-model="formData.leader" placeholder="请输入负责人姓名" maxlength="20" />
              </el-form-item>

              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="formData.phone" placeholder="请输入联系电话" maxlength="20" />
              </el-form-item>

              <el-form-item label="地址" prop="address">
                <el-input v-model="formData.address" type="textarea" placeholder="请输入地址" :rows="2" maxlength="200" show-word-limit />
              </el-form-item>

              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="formData.status">
                  <el-radio :value="1">启用</el-radio>
                  <el-radio :value="0">禁用</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="排序号" prop="sortOrder">
                <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" />
              </el-form-item>

              <el-form-item label="备注" prop="remark">
                <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" :rows="3" maxlength="500" show-word-limit />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="handleSubmit" :loading="submitting">
                  {{ formData.id ? '更新' : '创建' }}
                </el-button>
                <el-button @click="cancelForm">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入机构数据" width="500px">
      <div class="import-content">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          style="margin-bottom: 16px"
        >
          <template #default>
            <p>1. 请先下载模板文件</p>
            <p>2. 按照模板格式填写数据</p>
            <p>3. 上传Excel文件进行导入</p>
          </template>
        </el-alert>

        <el-button type="primary" link @click="handleDownloadTemplate" style="margin-bottom: 16px">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>

        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">只能上传 xlsx/xls 文件</div>
          </template>
        </el-upload>

        <el-alert
          v-if="importResult"
          :title="importResult"
          :type="importSuccess ? 'success' : 'error'"
          :closable="false"
          style="margin-top: 16px"
        />
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">确认导入</el-button>
      </template>
    </el-dialog>

    <!-- 右键菜单 -->
    <context-menu
      v-show="contextMenuVisible"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      @close="contextMenuVisible = false"
    >
      <context-menu-item @click="handleContextMenuAdd">新增子节点</context-menu-item>
      <context-menu-item @click="handleContextMenuEdit">编辑</context-menu-item>
      <context-menu-item divided @click="handleContextMenuDelete">删除</context-menu-item>
    </context-menu>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Check, Download, UploadFilled, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import {
  getOrgTree,
  getOrgDetail,
  addOrg,
  updateOrg,
  deleteOrg,
  updateOrgStatus,
  moveOrgNode,
  importOrgs,
  downloadTemplate,
  checkOrgCode
} from '@/api/org'
import type { OrgVO, OrgTreeVO, OrgDTO } from '@/api/org'

const treeRef = ref()
const formRef = ref()
const uploadRef = ref()

// 树形数据
const orgTree = ref<OrgTreeVO[]>([])
const filterText = ref('')

// 当前选中的机构
const currentOrg = ref<OrgVO | null>(null)

// 表单相关
const showForm = ref(false)
const submitting = ref(false)
const checkingCode = ref(false)
const codeUnique = ref<boolean | null>(null)
const formData = ref<OrgDTO>({
  orgName: '',
  orgCode: '',
  parentId: null,
  leader: '',
  phone: '',
  address: '',
  status: 1,
  sortOrder: 0,
  remark: ''
})

const formRules = {
  orgName: [
    { required: true, message: '请输入机构名称', trigger: 'blur' },
    { max: 100, message: '机构名称不能超过100个字符', trigger: 'blur' }
  ],
  orgCode: [
    { required: true, message: '请输入机构编码', trigger: 'blur' },
    { max: 50, message: '机构编码不能超过50个字符', trigger: 'blur' }
  ]
}

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuNode = ref<any>(null)

// 导入相关
const importDialogVisible = ref(false)
const importing = ref(false)
const importFile = ref<File | null>(null)
const importResult = ref('')
const importSuccess = ref(false)

// 监听搜索框
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 加载机构树
const loadTree = async () => {
  try {
    const res = await getOrgTree()
    orgTree.value = res.data as OrgTreeVO[]
  } catch (error) {
    console.error('加载机构树失败:', error)
  }
}

// 过滤节点
const filterNode = (value: string, data: any) => {
  if (!value) return true
  return data.label.includes(value)
}

// 节点点击事件
const handleNodeClick = async (data: any) => {
  try {
    const res = await getOrgDetail(data.id)
    currentOrg.value = res.data as OrgVO
    showForm.value = false
  } catch (error) {
    console.error('获取机构详情失败:', error)
  }
}

// 拖拽相关
const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  if (type === 'inner') {
    return true
  }
  if (dropNode.data.id === draggingNode.data.parentId) {
    return true
  }

  let dropParentId = dropNode.data.parentId
  while (dropParentId) {
    if (dropParentId === draggingNode.data.id) {
      return false
    }
    const dropParentNode = treeRef.value?.getNode(dropParentId)
    dropParentId = dropParentNode?.data?.parentId
  }

  return true
}

const allowDrag = (draggingNode: any) => {
  return true
}

const handleDragEnd = async (draggingNode: any, dropNode: any, dropType: string, ev: Event) => {
  try {
    const targetParentId = dropType === 'inner' ? dropNode.data.id : (dropNode.data.parentId || 0)
    await moveOrgNode(draggingNode.data.id, targetParentId, draggingNode.data.sortOrder || 0)
    ElMessage.success('移动成功')
    loadTree()
  } catch (error) {
    ElMessage.error('移动失败')
    loadTree()
  }
}

// 右键菜单
const handleRightClick = (event: MouseEvent, data: any, node: any, element: HTMLElement) => {
  event.preventDefault()
  contextMenuNode.value = node
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true

  document.addEventListener('click', () => {
    contextMenuVisible.value = false
  }, { once: true })
}

// 新增机构
const handleAdd = (parentData?: any) => {
  showForm.value = true
  currentOrg.value = null
  formData.value = {
    orgName: '',
    orgCode: '',
    parentId: parentData?.id || null,
    level: parentData ? (parentData.level || 0) + 1 : 1,
    leader: '',
    phone: '',
    address: '',
    status: 1,
    sortOrder: 0,
    remark: ''
  }
  codeUnique.value = null
  nextTick(() => formRef.value?.clearValidate())
}

// 编辑机构
const handleEdit = (org: OrgVO) => {
  showForm.value = true
  formData.value = {
    id: org.id,
    orgName: org.orgName,
    orgCode: org.orgCode,
    parentId: org.parentId,
    level: org.level,
    leader: org.leader,
    phone: org.phone,
    address: org.address,
    status: org.status,
    sortOrder: org.sortOrder,
    remark: org.remark
  }
  codeUnique.value = null
  nextTick(() => formRef.value?.clearValidate())
}

// 删除机构
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该机构吗？如果该机构下存在子机构或关联用户，将无法删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteOrg(id)
    ElMessage.success('删除成功')
    currentOrg.value = null
    loadTree()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 更新状态
const handleStatusChange = async (org: OrgVO) => {
  try {
    await updateOrgStatus(org.id, org.status)
    ElMessage.success(org.status === 1 ? '已启用' : '已禁用')
    loadTree()
  } catch (error) {
    org.status = org.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (codeUnique.value === false) {
    ElMessage.warning('机构编码已存在，请修改后重试')
    return
  }

  submitting.value = true
  try {
    if (formData.value.id) {
      await updateOrg(formData.value as OrgDTO)
      ElMessage.success('更新成功')
    } else {
      await addOrg(formData.value as OrgDTO)
      ElMessage.success('创建成功')
    }
    showForm.value = false
    loadTree()

    // 刷新当前选中机构的详情
    if (formData.value.id) {
      handleNodeClick({ id: formData.value.id })
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 取消表单
const cancelForm = () => {
  showForm.value = false
  if (currentOrg.value) {
    handleNodeClick({ id: currentOrg.value.id })
  }
}

// 检查编码唯一性
const checkCodeUnique = async () => {
  if (!formData.value.orgCode) {
    ElMessage.warning('请先输入机构编码')
    return
  }

  checkingCode.value = true
  try {
    const res = await checkOrgCode(formData.value.orgCode, formData.value.id)
    codeUnique.value = res.data as boolean
  } catch (error) {
    console.error('检查编码失败:', error)
  } finally {
    checkingCode.value = false
  }
}

// 更多操作
const handleCommand = (command: string) => {
  switch (command) {
    case 'import':
      importDialogVisible.value = true
      importResult.value = ''
      importFile.value = null
      break
    case 'export':
      handleExport()
      break
    case 'expandAll':
      expandAllNodes()
      break
    case 'collapseAll':
      collapseAllNodes()
      break
  }
}

// 导入功能
const handleFileChange = (file: any) => {
  importFile.value = file.raw
  importResult.value = ''
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  importing.value = true
  try {
    const res = await importOrgs(importFile.value)
    importResult.value = res.data as string
    importSuccess.value = true
    ElMessage.success('导入完成')
    loadTree()
  } catch (error: any) {
    importResult.value = error.message || '导入失败'
    importSuccess.value = false
  } finally {
    importing.value = false
  }
}

const handleDownloadTemplate = async () => {
  try {
    const response = await downloadTemplate()
    const blob = new Blob([response as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '机构导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败')
  }
}

// 导出功能
const handleExport = async () => {
  try {
    const response = await exportOrgs({})
    const blob = new Blob([response as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '机构数据.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 展开/折叠所有节点
const expandAllNodes = () => {
  const nodes = treeRef.value?.store._getAllNodes()
  nodes?.forEach((node: any) => {
    node.expanded = true
  })
}

const collapseAllNodes = () => {
  const nodes = treeRef.value?.store._getAllNodes()
  nodes?.forEach((node: any) => {
    node.expanded = false
  })
}

// 右键菜单操作
const handleContextMenuAdd = () => {
  if (contextMenuNode.value) {
    handleAdd(contextMenuNode.value.data)
  }
  contextMenuVisible.value = false
}

const handleContextMenuEdit = () => {
  if (contextMenuNode.value) {
    handleNodeClick(contextMenuNode.value.data)
    nextTick(() => handleEdit(contextMenuNode.value.data))
  }
  contextMenuVisible.value = false
}

const handleContextMenuDelete = () => {
  if (contextMenuNode.value) {
    handleDelete(contextMenuNode.value.data.id)
  }
  contextMenuVisible.value = false
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

// 计算属性 - 父级机构选项
const parentTreeOptions = computed(() => {
  return [{ id: 0, label: '顶级机构（无父级）', children: [] }, ...orgTree.value]
})

onMounted(() => {
  loadTree()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 16px;
  height: calc(100vh - 84px);
  overflow: hidden;
}

.org-container {
  display: flex;
  gap: 16px;
  height: 100%;
}

.left-panel {
  width: 320px;
  flex-shrink: 0;

  .tree-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      overflow: auto;
      display: flex;
      flex-direction: column;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-btns {
      display: flex;
      gap: 8px;
    }
  }

  .search-input {
    margin-bottom: 12px;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }
}

.right-panel {
  flex: 1;
  min-width: 0;

  .el-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      overflow: auto;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .action-btns {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
}

.org-detail {
  .children-section {
    margin-top: 24px;

    h4 {
      margin-bottom: 12px;
      color: #303133;
    }
  }
}

.org-form {
  padding: 20px 0;
}

.code-check-result {
  margin-top: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.import-content {
  .el-upload-dragger {
    width: 100%;
  }
}
</style>
