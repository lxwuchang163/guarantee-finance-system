<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-btns">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
            <el-dropdown @command="handleCommand">
              <el-button type="primary" icon="MoreFilled">更多操作</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="import">批量导入</el-dropdown-item>
                  <el-dropdown-item command="online">在线用户</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 160px" />
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

      <!-- 用户列表 -->
      <el-table :data="userList" stripe border v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" fixed />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="orgName" label="所属机构" width="150" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sex" label="性别" width="80">
          <template #default="{ row }">
            {{ row.sex === 1 ? '男' : row.sex === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
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
        <el-table-column label="角色" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
              v-for="role in (row.roles || [])"
              :key="role.id"
              size="small"
              style="margin-right: 4px; margin-bottom: 4px"
            >
              {{ role.roleName }}
            </el-tag>
            <span v-if="!row.roles || row.roles.length === 0" style="color: #999">未分配角色</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
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
      :title="formData.id ? '编辑用户' : '新增用户'"
      width="650px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="formData.username"
                placeholder="请输入用户名"
                :disabled="!!formData.id"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" placeholder="请输入昵称" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="!formData.id">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码（默认123456）"
                show-password
                maxlength="100"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="formData.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="formData.sex">
                <el-radio :value="0">未知</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属机构" prop="orgId">
              <el-tree-select
                v-model="formData.orgId"
                :data="orgTreeOptions"
                :props="{ label: 'label', value: 'id', children: 'children' }"
                placeholder="选择所属机构"
                check-strictly
                clearable
                filterable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="分配角色" prop="roleIds">
              <el-checkbox-group v-model="formData.roleIds">
                <el-checkbox
                  v-for="role in allRoles"
                  :key="role.id"
                  :value="role.id"
                  :label="role.roleName"
                />
              </el-checkbox-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            placeholder="请输入备注"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ formData.id ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入用户" width="500px">
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
            <p>4. 默认密码为：123456</p>
          </template>
        </el-alert>

        <el-button type="primary" link @click="handleDownloadTemplate" style="margin-bottom: 16px">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>

        <el-upload
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

    <!-- 在线用户对话框 -->
    <el-dialog v-model="onlineDialogVisible" title="在线用户" width="900px">
      <el-table :data="onlineUsers" stripe border v-loading="onlineLoading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="ipaddr" label="登录IP" width="140" />
        <el-table-column prop="loginLocation" label="登录地点" width="150" show-overflow-tooltip />
        <el-table-column prop="browser" label="浏览器" width="120" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="loginTime" label="登录时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleForceLogout(row)">强制下线</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import {
  getUserPage,
  getUserDetail,
  addUser,
  updateUser,
  deleteUser,
  resetUserPassword,
  updateUserStatus,
  importUsers as importUsersApi,
  downloadUserTemplate,
  getOnlineUsers,
  forceLogout
} from '@/api/user'
import { getOrgTree as getOrgTreeApi } from '@/api/org'
import { getAllRoles } from '@/api/role'
import type { UserVO, UserDTO, OnlineUserVO, RoleSimpleVO, UserQueryDTO } from '@/api/user'

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const onlineLoading = ref(false)
const importing = ref(false)

// 列表数据
const userList = ref<UserVO[]>([])
const total = ref(0)
const queryParams = reactive<UserQueryDTO & { current: number; size: number }>({
  username: '',
  nickname: '',
  phone: '',
  status: null,
  current: 1,
  size: 10
})

// 对话框
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const onlineDialogVisible = ref(false)
const onlineUsers = ref<OnlineUserVO[]>([])

// 表单数据类型
interface UserFormData extends UserDTO {
  confirmPassword?: string
}

const formData = ref<UserFormData>({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: '',
  sex: 0,
  orgId: null,
  roleIds: [],
  status: 1,
  remark: ''
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在2-50个字符之间', trigger: 'blur' }
  ],
  password: [
    { min: 6, max: 100, message: '密码长度在6-100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value && value !== formData.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 导入相关
const importFile = ref<File | null>(null)
const importResult = ref('')
const importSuccess = ref(false)

// 角色和机构选项
const allRoles = ref<RoleSimpleVO[]>([])
const orgTreeOptions = ref<any[]>([])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
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
  queryParams.username = ''
  queryParams.nickname = ''
  queryParams.phone = ''
  queryParams.status = null
  queryParams.current = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogVisible.value = true
  formData.value = {
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    email: '',
    phone: '',
    sex: 0,
    orgId: null,
    roleIds: [],
    status: 1,
    remark: ''
  }
}

// 编辑
const handleEdit = async (row: UserVO) => {
  try {
    const res = await getUserDetail(row.id)
    const user = res.data as UserVO
    dialogVisible.value = true
    formData.value = {
      id: user.id,
      username: user.username,
      password: '',
      confirmPassword: '',
      nickname: user.nickname || '',
      email: user.email || '',
      phone: user.phone || '',
      sex: user.sex || 0,
      orgId: user.orgId,
      roleIds: (user.roles || []).map(r => r.id),
      status: user.status,
      remark: user.remark || ''
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (formData.value.id) {
      await updateUser(formData.value as UserDTO)
      ElMessage.success('更新成功')
    } else {
      await addUser(formData.value as UserDTO)
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
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteUser(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 重置密码
const handleResetPwd = async (row: UserVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户"${row.username}"的密码吗？重置后默认密码为：123456`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await resetUserPassword(row.id)
    ElMessage.success('密码已重置为：123456')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重置失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row: UserVO, val: number) => {
  try {
    await updateUserStatus(row.id, val)
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
    row.status = val
  } catch (error) {
    row.status = val === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 更多操作
const handleCommand = (command: string) => {
  if (command === 'import') {
    importDialogVisible.value = true
    importResult.value = ''
    importFile.value = null
  } else if (command === 'online') {
    loadOnlineUsers()
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
    const res = await importUsersApi(importFile.value)
    importResult.value = res.data as string
    importSuccess.value = true
    ElMessage.success('导入完成')
    loadData()
  } catch (error: any) {
    importResult.value = error.message || '导入失败'
    importSuccess.value = false
  } finally {
    importing.value = false
  }
}

const handleDownloadTemplate = async () => {
  try {
    const response = await downloadUserTemplate()
    const blob = new Blob([response as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '用户导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败')
  }
}

// 在线用户
const loadOnlineUsers = async () => {
  onlineDialogVisible.value = true
  onlineLoading.value = true
  try {
    const res = await getOnlineUsers()
    onlineUsers.value = res.data as OnlineUserVO[]
  } catch (error) {
    console.error('获取在线用户失败:', error)
  } finally {
    onlineLoading.value = false
  }
}

const handleForceLogout = async (row: OnlineUserVO) => {
  try {
    await ElMessageBox.confirm(`确定要强制下线用户"${row.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await forceLogout(row.token)
    ElMessage.success('已强制下线')
    loadOnlineUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

// 加载选项数据
const loadOptions = async () => {
  try {
    // 加载角色列表
    const roleRes = await getAllRoles()
    allRoles.value = roleRes.data as RoleSimpleVO[]

    // 加载机构树
    const orgRes = await getOrgTreeApi()
    orgTreeOptions.value = orgRes.data as any[]
  } catch (error) {
    console.error('加载选项数据失败:', error)
  }
}

onMounted(() => {
  loadData()
  loadOptions()
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

.import-content {
  .el-upload-dragger {
    width: 100%;
  }
}
</style>
