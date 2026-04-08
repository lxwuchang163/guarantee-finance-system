<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <div class="header-btns">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
            <el-dropdown @command="handleCommand">
              <el-button type="primary" icon="MoreFilled">更多操作</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="initPreset">初始化预设角色</el-dropdown-item>
                  <el-dropdown-item command="compare">角色对比</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="角色名称">
            <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="角色编码">
            <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="禁用" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 角色列表 -->
      <el-table :data="roleList" stripe border v-loading="loading">
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="140" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序号" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handleCopy(row)">复制</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
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
    <el-dialog
      v-model="dialogVisible"
      :title="formData.id ? '编辑角色' : '新增角色'"
      width="700px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="formData.roleName" placeholder="请输入角色名称" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色编码" prop="roleCode">
              <el-input
                v-model="formData.roleCode"
                placeholder="请输入角色编码"
                maxlength="50"
                :disabled="!!formData.id && isBuiltIn(formData.roleCode)"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="formData.description" type="textarea" placeholder="请输入角色描述" :rows="2" maxlength="255" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序号" prop="sortOrder">
              <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 权限配置 -->
        <el-divider content-position="left">菜单权限配置</el-divider>
        <el-form-item label="菜单权限" prop="menuIds">
          <div class="menu-tree-container">
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              :props="{ label: 'menuName', children: 'children' }"
              node-key="id"
              show-checkbox
              default-expand-all
              check-strictly
              :default-checked-keys="formData.menuIds"
              @check="handleMenuCheck"
            >
              <template #default="{ node, data }">
                <span class="custom-menu-node">
                  <el-icon v-if="data.menuType === 1"><FolderOpened /></el-icon>
                  <el-icon v-else-if="data.menuType === 2"><Document /></el-icon>
                  <el-icon v-else><Grid /></el-icon>
                  <span>{{ data.menuName }}</span>
                  <el-tag v-if="data.menuType === 1" size="small" type="info" effect="plain" style="margin-left: 4px">目录</el-tag>
                  <el-tag v-else-if="data.menuType === 2" size="small" type="success" effect="plain" style="margin-left: 4px">菜单</el-tag>
                  <el-tag v-else size="small" type="warning" effect="plain" style="margin-left: 4px">按钮</el-tag>
                </span>
              </template>
            </el-tree>
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ formData.id ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 复制角色对话框 -->
    <el-dialog v-model="copyDialogVisible" title="复制角色" width="500px">
      <el-form ref="copyFormRef" :model="copyFormData" :rules="copyFormRules" label-width="100px">
        <el-alert title="复制角色将包含原角色的所有菜单权限" type="info" :closable="false" style="margin-bottom: 16px" />

        <el-form-item label="源角色" disabled>
          <el-input :value="sourceRole?.roleName" disabled />
        </el-form-item>

        <el-form-item label="新角色名称" prop="newRoleName">
          <el-input v-model="copyFormData.newRoleName" placeholder="请输入新角色名称" maxlength="50" />
        </el-form-item>

        <el-form-item label="新角色编码" prop="newRoleCode">
          <el-input v-model="copyFormData.newRoleCode" placeholder="请输入新角色编码" maxlength="50" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="copyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCopySubmit" :loading="copying">确认复制</el-button>
      </template>
    </el-dialog>

    <!-- 角色对比对话框 -->
    <el-dialog v-model="compareDialogVisible" title="角色对比" width="800px">
      <el-form :inline="true" style="margin-bottom: 16px">
        <el-form-item label="选择角色A">
          <el-select v-model="compareForm.sourceRoleId" placeholder="请选择" filterable style="width: 200px">
            <el-option
              v-for="role in allRolesForCompare"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择角色B">
          <el-select v-model="compareForm.targetRoleId" placeholder="请选择" filterable style="width: 200px">
            <el-option
              v-for="role in allRolesForCompare"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCompare">开始对比</el-button>
        </el-form-item>
      </el-form>

      <div v-if="compareResult" class="compare-result">
        <el-descriptions :column="2" border title="基本信息对比">
          <el-descriptions-item
            v-for="diff in compareResult.differences.filter(d => d.field !== 'menuIds')"
            :key="diff.field"
            :label="diff.fieldName"
          >
            <div class="diff-cell">
              <span :class="{ 'text-danger': diff.diffType === 'different' }">{{ diff.sourceValue }}</span>
              <el-icon><Right /></el-icon>
              <span :class="{ 'text-success': diff.diffType === 'different' }">{{ diff.targetValue }}</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="menuDiff" style="margin-top: 16px">
          <h4>菜单权限差异</h4>
          <el-tag type="danger" v-if="menuDiff.sourceValue && menuDiff.sourceValue !== '-'">
            仅在{{ compareResult.sourceRole?.roleName }}中存在: {{ menuDiff.sourceValue }}
          </el-tag>
          <el-tag type="success" v-if="menuDiff.targetValue && menuDiff.targetValue !== '-'">
            仅在{{ compareResult.targetRole?.roleName }}中存在: {{ menuDiff.targetValue }}
          </el-tag>
          <el-tag v-if="(!menuDiff.sourceValue || menuDiff.sourceValue === '-') && (!menuDiff.targetValue || menuDiff.targetValue === '-')">
            菜单权限完全一致
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened, Document, Grid, Right } from '@element-plus/icons-vue'
import {
  getRolePage,
  addRole,
  updateRole,
  deleteRole,
  copyRole as copyRoleApi,
  compareRoles as compareRolesApi,
  updateRoleStatus,
  initPresetRoles as initPresetRolesApi,
  getAllRoles,
  getMenuTree as getMenuTreeApi
} from '@/api/role'
import type { RoleVO, RoleDTO, RoleSimpleVO, RoleCompareVO, MenuTreeVO } from '@/api/role'

const formRef = ref()
const copyFormRef = ref()
const menuTreeRef = ref()
const loading = ref(false)
const submitting = ref(false)
const copying = ref(false)

// 列表数据
const roleList = ref<RoleVO[]>([])
const total = ref(0)
const queryParams = reactive({
  roleName: '',
  roleCode: '',
  status: null as number | null,
  current: 1,
  size: 10
})

// 对话框
const dialogVisible = ref(false)
const copyDialogVisible = ref(false)
const compareDialogVisible = ref(false)

// 表单数据
const formData = ref<RoleDTO>({
  roleName: '',
  roleCode: '',
  description: '',
  status: 1,
  sortOrder: 0,
  menuIds: [],
  remark: ''
})

const formRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 50, message: '角色名称不能超过50个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { max: 50, message: '角色编码不能超过50个字符', trigger: 'blur' }
  ]
}

// 复制表单
const sourceRole = ref<RoleVO | null>(null)
const copyFormData = reactive({
  newRoleName: '',
  newRoleCode: ''
})

const copyFormRules = {
  newRoleName: [
    { required: true, message: '请输入新角色名称', trigger: 'blur' }
  ],
  newRoleCode: [
    { required: true, message: '请输入新角色编码', trigger: 'blur' }
  ]
}

// 对比相关
const allRolesForCompare = ref<RoleSimpleVO[]>([])
const compareForm = reactive({
  sourceRoleId: null as number | null,
  targetRoleId: null as number | null
})
const compareResult = ref<RoleCompareVO | null>(null)
const menuDiff = computed(() => {
  if (!compareResult.value) return null
  return compareResult.value.differences.find(d => d.field === 'menuIds')
})

// 菜单树
const menuTree = ref<MenuTreeVO[]>([])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    roleList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  loadData()
}

// 重置搜索
const resetQuery = () => {
  queryParams.roleName = ''
  queryParams.roleCode = ''
  queryParams.status = null
  queryParams.current = 1
  loadData()
}

// 判断是否内置角色
const isBuiltIn = (code: string) => {
  return ['ADMIN', 'USER'].includes(code)
}

// 新增
const handleAdd = () => {
  dialogVisible.value = true
  formData.value = {
    roleName: '',
    roleCode: '',
    description: '',
    status: 1,
    sortOrder: 0,
    menuIds: [],
    remark: ''
  }

  // 延迟设置默认选中，等待tree渲染完成
  setTimeout(() => {
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys([])
    }
  }, 100)
}

// 编辑
const handleEdit = async (row: RoleVO) => {
  try {
    const res = await getRoleDetail(row.id)
    const role = res.data as RoleVO
    dialogVisible.value = true
    formData.value = {
      id: role.id,
      roleName: role.roleName,
      roleCode: role.roleCode,
      description: role.description || '',
      status: role.status,
      sortOrder: role.sortOrder || 0,
      menuIds: role.menuIds || [],
      remark: role.remark || ''
    }

    setTimeout(() => {
      if (menuTreeRef.value) {
        menuTreeRef.value.setCheckedKeys(role.menuIds || [])
      }
    }, 100)
  } catch (error) {
    console.error('获取角色详情失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 获取选中的菜单ID
  if (menuTreeRef.value) {
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    formData.value.menuIds = [...checkedKeys, ...halfCheckedKeys] as number[]
  }

  submitting.value = true
  try {
    if (formData.value.id) {
      await updateRole(formData.value as RoleDTO)
      ElMessage.success('更新成功')
    } else {
      await addRole(formData.value as RoleDTO)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？如果该角色为内置角色或已分配给用户，将无法删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteRole(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row: RoleVO, val: number) => {
  try {
    await updateRoleStatus(row.id, val)
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
    row.status = val
  } catch (error) {
    row.status = val === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 复制角色
const handleCopy = (row: RoleVO) => {
  sourceRole.value = row
  copyFormData.newRoleName = `${row.roleName}(副本)`
  copyFormData.newRoleCode = `${row.roleCode}_COPY`
  copyDialogVisible.value = true
}

const handleCopySubmit = async () => {
  const valid = await copyFormRef.value?.validate().catch(() => false)
  if (!valid) return

  copying.value = true
  try {
    await copyRoleApi({
      sourceRoleId: sourceRole.value!.id,
      newRoleName: copyFormData.newRoleName,
      newRoleCode: copyFormData.newRoleCode
    })
    ElMessage.success('复制成功')
    copyDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '复制失败')
  } finally {
    copying.value = false
  }
}

// 更多操作
const handleCommand = async (command: string) => {
  if (command === 'initPreset') {
    try {
      await ElMessageBox.confirm(
        '确定要初始化预设角色吗？这将创建7种预设角色（系统管理员、财务主管、会计、出纳、业务员、审批人、普通用户）。',
        '初始化预设角色',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      await initPresetRolesApi()
      ElMessage.success('预设角色初始化成功')
      loadData()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '初始化失败')
      }
    }
  } else if (command === 'compare') {
    compareDialogVisible.value = true
    compareResult.value = null

    // 加载所有角色用于对比选择
    const res = await getAllRoles()
    allRolesForCompare.value = res.data as RoleSimpleVO[]
  }
}

// 角色对比
const handleCompare = async () => {
  if (!compareForm.sourceRoleId || !compareForm.targetRoleId) {
    ElMessage.warning('请选择两个角色进行对比')
    return
  }

  if (compareForm.sourceRoleId === compareForm.targetRoleId) {
    ElMessage.warning('请选择不同的角色进行对比')
    return
  }

  try {
    const res = await compareRolesApi(compareForm.sourceRoleId, compareForm.targetRoleId)
    compareResult.value = res.data as RoleCompareVO
  } catch (error) {
    console.error('角色对比失败:', error)
  }
}

// 菜单树勾选事件
const handleMenuCheck = () => {
  // 可以在这里添加逻辑
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

// 加载菜单树
const loadMenuTree = async () => {
  try {
    const res = await getMenuTreeApi()
    menuTree.value = res.data as MenuTreeVO[]
  } catch (error) {
    console.error('加载菜单树失败:', error)
  }
}

onMounted(() => {
  loadData()
  loadMenuTree()
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

  .header-btns {
    display: flex;
    gap: 8px;
  }
}

.search-area {
  margin-bottom: 16px;
}

.search-form {
  .el-form-item {
    margin-bottom: 12px;
  }
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.menu-tree-container {
  width: 100%;
  height: 350px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 8px;

  .custom-menu-node {
    display: flex;
    align-items: center;
    gap: 6px;

    .el-icon {
      font-size: 14px;
    }
  }
}

.compare-result {
  .diff-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .text-danger {
      color: #f56c6c;
    }

    .text-success {
      color: #67c23a;
    }
  }
}
</style>
