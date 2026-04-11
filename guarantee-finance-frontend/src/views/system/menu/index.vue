<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>菜单权限管理</span>
          <div>
            <el-button type="primary" size="small" @click="handleAddMenu">新增菜单</el-button>
            <el-button type="warning" size="small" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 菜单管理 -->
        <el-tab-pane label="菜单管理" name="menu">
          <div class="menu-management">
            <el-card shadow="hover" class="mb-4">
              <div class="menu-tree-wrapper">
                <el-tree
                  ref="menuTreeRef"
                  :data="menuTree"
                  :props="{ label: 'menuName', children: 'children' }"
                  node-key="id"
                  default-expand-all
                >
                  <template #default="{ node, data }">
                    <span class="custom-menu-node">
                      <el-icon v-if="data.menuType === 1"><FolderOpened /></el-icon>
                      <el-icon v-else-if="data.menuType === 2"><Document /></el-icon>
                      <el-icon v-else><Grid /></el-icon>
                      <span>{{ data.menuName }}</span>
                      <el-tag v-if="data.menuType === 1" size="small" type="info">目录</el-tag>
                      <el-tag v-else-if="data.menuType === 2" size="small" type="success">菜单</el-tag>
                      <el-tag v-else size="small" type="warning">按钮</el-tag>
                      <div class="menu-actions">
                        <el-button type="primary" size="small" @click="handleAddSubMenu(data)" :disabled="data.menuType === 3">
                          <el-icon><Plus /></el-icon>
                          新增子菜单
                        </el-button>
                        <el-button type="info" size="small" @click="handleEditMenu(data)">
                          <el-icon><Edit /></el-icon>
                          编辑
                        </el-button>
                        <el-button type="danger" size="small" @click="handleDeleteMenu(data.id)">
                          <el-icon><Delete /></el-icon>
                          删除
                        </el-button>
                      </div>
                    </span>
                  </template>
                </el-tree>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 角色权限配置 -->
        <el-tab-pane label="角色权限配置" name="permission">
          <!-- 角色选择 -->
          <div class="role-select-area">
            <el-form :inline="true">
              <el-form-item label="选择角色">
                <el-select v-model="selectedRoleId" placeholder="请选择角色" filterable style="width: 250px" @change="handleRoleChange">
                  <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <div v-if="selectedRoleId" class="permission-content">
            <el-row :gutter="20">
              <!-- 左侧：菜单权限树 -->
              <el-col :span="14">
                <el-card shadow="hover">
                  <template #header>
                    <div style="display: flex; justify-content: space-between; align-items: center">
                      <span>菜单权限配置</span>
                      <el-button type="primary" size="small" @click="handleSaveMenuPermissions" :loading="saving">保存权限</el-button>
                    </div>
                  </template>

                  <div class="menu-tree-wrapper">
                    <el-tree
                      ref="permissionTreeRef"
                      :data="menuTree"
                      :props="{ label: 'menuName', children: 'children' }"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      check-strictly
                      :default-checked-keys="checkedMenuKeys"
                    >
                      <template #default="{ node, data }">
                        <span class="custom-menu-node">
                          <el-icon v-if="data.menuType === 1"><FolderOpened /></el-icon>
                          <el-icon v-else-if="data.menuType === 2"><Document /></el-icon>
                          <el-icon v-else><Grid /></el-icon>
                          <span>{{ data.menuName }}</span>
                          <el-tag v-if="data.menuType === 1" size="small" type="info">目录</el-tag>
                          <el-tag v-else-if="data.menuType === 2" size="small" type="success">菜单</el-tag>
                          <el-tag v-else size="small" type="warning">按钮</el-tag>
                        </span>
                      </template>
                    </el-tree>
                  </div>
                </el-card>
              </el-col>

              <!-- 右侧：数据权限设置 -->
              <el-col :span="10">
                <el-card shadow="hover">
                  <template #header>
                    <div style="display: flex; justify-content: space-between; align-items: center">
                      <span>数据权限设置</span>
                      <el-button type="primary" size="small" @click="handleSaveDataScope" :loading="savingDataScope">保存</el-button>
                    </div>
                  </template>

                  <el-form label-width="100px">
                    <el-form-item label="数据范围">
                      <el-select v-model="dataScope.dataScope" placeholder="请选择数据范围" style="width: 100%" @change="handleDataScopeChange">
                        <el-option value="ALL" label="全部数据权限" />
                        <el-option value="ORG_AND_CHILD" label="本机构及以下数据" />
                        <el-option value="ORG_ONLY" label="本机构数据" />
                        <el-option value="CUSTOM" label="自定义数据" />
                        <el-option value="SELF" label="仅本人数据" />
                      </el-select>
                    </el-form-item>

                    <el-form-item v-if="dataScope.dataScope === 'CUSTOM'" label="选择机构">
                      <el-tree-select
                        v-model="selectedOrgCodes"
                        :data="orgTreeOptions"
                        :props="{ label: 'label', value: 'id', children: 'children' }"
                        multiple
                        collapse-tags
                        collapse-tags-tooltip
                        placeholder="请选择机构"
                        check-strictly
                        filterable
                        style="width: 100%"
                      />
                    </el-form-item>

                    <el-divider />

                    <div class="scope-description">
                      <h4>权限说明：</h4>
                      <ul>
                        <li><strong>全部数据权限</strong>：可查看所有机构的数据</li>
                        <li><strong>本机构及以下</strong>：可查看本机构及其子机构的数据</li>
                        <li><strong>本机构数据</strong>：仅查看本机构的数据</li>
                        <li><strong>自定义数据</strong>：指定可查看的机构范围</li>
                        <li><strong>仅本人数据</strong>：仅查看自己创建或负责的数据</li>
                      </ul>
                    </div>
                  </el-form>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <el-empty v-else description="请先选择一个角色进行权限配置" />
        </el-tab-pane>
      </el-tabs>

      <!-- 菜单编辑对话框 -->
      <el-dialog
        v-model="menuDialogVisible"
        :title="menuForm.id ? '编辑菜单' : '新增菜单'"
        width="500px"
      >
        <el-form :model="menuForm" label-width="100px">
          <el-form-item label="菜单名称" required>
            <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
          </el-form-item>
          <el-form-item label="父菜单">
            <el-tree-select
              v-model="menuForm.parentId"
              :data="menuTree"
              :props="{ label: 'menuName', value: 'id', children: 'children' }"
              placeholder="请选择父菜单"
              filterable
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="菜单类型" required>
            <el-select v-model="menuForm.menuType" placeholder="请选择菜单类型">
              <el-option value="1" label="目录" />
              <el-option value="2" label="菜单" />
              <el-option value="3" label="按钮" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序" required>
            <el-input-number v-model="menuForm.sortOrder" :min="0" />
          </el-form-item>
          <el-form-item label="路由路径" v-if="menuForm.menuType !== 3">
            <el-input v-model="menuForm.path" placeholder="请输入路由路径" />
          </el-form-item>
          <el-form-item label="组件路径" v-if="menuForm.menuType === 2">
            <el-input v-model="menuForm.component" placeholder="请输入组件路径" />
          </el-form-item>
          <el-form-item label="图标" v-if="menuForm.menuType !== 3">
            <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
          </el-form-item>
          <el-form-item label="权限标识" v-if="menuForm.menuType === 3">
            <el-input v-model="menuForm.permission" placeholder="请输入权限标识" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="menuForm.status" active-value="1" inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="menuDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveMenu" :loading="savingMenu">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened, Document, Grid, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMenuTree, getRoleMenus, assignMenuPermissions, getDataScope, setDataScope as setDataScopeApi, addMenu, updateMenu, deleteMenu, getMenu } from '@/api/menu'
import { getAllRoles } from '@/api/role'
import { getOrgTree as getOrgTreeApi } from '@/api/org'
import type { MenuTreeVO, DataScopeVO } from '@/api/menu'
import type { RoleSimpleVO } from '@/api/role'
import type { OrgTreeVO } from '@/api/org'

const menuTreeRef = ref()
const permissionTreeRef = ref()
const selectedRoleId = ref<number | null>(null)
const activeTab = ref('menu')
const saving = ref(false)
const savingDataScope = ref(false)
const savingMenu = ref(false)
const menuDialogVisible = ref(false)

// 数据
const roleList = ref<RoleSimpleVO[]>([])
const menuTree = ref<MenuTreeVO[]>([])
const checkedMenuKeys = ref<number[]>([])
const orgTreeOptions = ref<any[]>([])

// 菜单表单
const menuForm = ref({
  id: 0,
  menuName: '',
  parentId: 0,
  menuType: 1,
  sortOrder: 0,
  path: '',
  component: '',
  icon: '',
  permission: '',
  status: 1
})

// 数据权限
const dataScope = ref<DataScopeVO>({
  roleId: 0,
  dataScope: 'ORG_AND_CHILD',
  orgCodes: []
})
const selectedOrgCodes = ref<number[]>([])

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await getAllRoles()
    roleList.value = res.data as RoleSimpleVO[]
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 加载菜单树
const loadMenuTree = async () => {
  try {
    const res = await getMenuTree()
    menuTree.value = res.data as MenuTreeVO[]
  } catch (error) {
    console.error('加载菜单树失败:', error)
  }
}

// 加载机构树
const loadOrgTree = async () => {
  try {
    const res = await getOrgTreeApi()
    orgTreeOptions.value = res.data as OrgTreeVO[]
  } catch (error) {
    console.error('加载机构树失败:', error)
  }
}

// 角色变更
const handleRoleChange = async (roleId: number) => {
  if (!roleId) return

  selectedRoleId.value = roleId

  try {
    // 加载该角色的菜单权限
    const menuRes = await getRoleMenus(roleId)
    checkedMenuKeys.value = menuRes.data as number[]

    setTimeout(() => {
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(checkedMenuKeys.value)
      }
    }, 100)

    // 加载数据权限配置
    const scopeRes = await getDataScope(roleId)
    dataScope.value = scopeRes.data as DataScopeVO

    if (dataScope.value.orgCodes && Array.isArray(dataScope.value.orgCodes)) {
      // 根据编码转换为ID（简化处理）
      selectedOrgCodes.value = []
    }
  } catch (error) {
    console.error('加载角色权限失败:', error)
  }
}

// 数据范围变更
const handleDataScopeChange = () => {
  if (dataScope.value.dataScope !== 'CUSTOM') {
    selectedOrgCodes.value = []
  }
}

// 保存菜单权限
const handleSaveMenuPermissions = async () => {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择角色')
    return
  }

  saving.value = true
  try {
    const checkedKeys = permissionTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || []
    const allChecked = [...checkedKeys, ...halfCheckedKeys] as number[]

    await assignMenuPermissions(selectedRoleId.value, allChecked)
    ElMessage.success('菜单权限保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 保存数据权限
const handleSaveDataScope = async () => {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择角色')
    return
  }

  savingDataScope.value = true
  try {
    dataScope.value.roleId = selectedRoleId.value
    await setDataScopeApi({
      roleId: dataScope.value.roleId,
      dataScope: dataScope.value.dataScope,
      orgCodes: dataScope.value.orgCodes || []
    })
    ElMessage.success('数据权限保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    savingDataScope.value = false
  }
}

// 新增菜单
const handleAddMenu = () => {
  menuForm.value = {
    id: 0,
    menuName: '',
    parentId: 0,
    menuType: 1,
    sortOrder: 0,
    path: '',
    component: '',
    icon: '',
    permission: '',
    status: 1
  }
  menuDialogVisible.value = true
}

// 新增子菜单
const handleAddSubMenu = (parentMenu: MenuTreeVO) => {
  menuForm.value = {
    id: 0,
    menuName: '',
    parentId: parentMenu.id,
    menuType: parentMenu.menuType === 1 ? 2 : 3,
    sortOrder: 0,
    path: '',
    component: '',
    icon: '',
    permission: '',
    status: 1
  }
  menuDialogVisible.value = true
}

// 编辑菜单
const handleEditMenu = async (menu: MenuTreeVO) => {
  try {
    const res = await getMenu(menu.id)
    menuForm.value = res.data
    menuDialogVisible.value = true
  } catch (error) {
    console.error('获取菜单详情失败:', error)
    ElMessage.error('获取菜单详情失败')
  }
}

// 删除菜单
const handleDeleteMenu = async (menuId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteMenu(menuId)
    ElMessage.success('删除成功')
    await loadMenuTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 保存菜单
const handleSaveMenu = async () => {
  if (!menuForm.value.menuName) {
    ElMessage.warning('请输入菜单名称')
    return
  }

  savingMenu.value = true
  try {
    if (menuForm.value.id) {
      await updateMenu(menuForm.value)
      ElMessage.success('编辑成功')
    } else {
      await addMenu(menuForm.value)
      ElMessage.success('新增成功')
    }
    menuDialogVisible.value = false
    await loadMenuTree()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    savingMenu.value = false
  }
}

// 刷新
const handleRefresh = async () => {
  await loadMenuTree()
  ElMessage.success('刷新成功')
}

onMounted(() => {
  loadRoles()
  loadMenuTree()
  loadOrgTree()
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

.role-select-area {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.permission-content {
  .menu-tree-wrapper {
    height: 500px;
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

  .scope-description {
    h4 {
      margin-bottom: 8px;
      color: #303133;
    }

    ul {
      margin: 0;
      padding-left: 20px;

      li {
        line-height: 24px;
        color: #606266;
        font-size: 13px;
      }
    }
  }
}

.menu-management {
  .menu-tree-wrapper {
    height: 600px;
    overflow-y: auto;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 8px;

    .custom-menu-node {
      display: flex;
      align-items: center;
      gap: 6px;
      justify-content: space-between;
      width: 100%;

      .el-icon {
        font-size: 14px;
      }

      .menu-actions {
        display: flex;
        gap: 4px;

        .el-button {
          padding: 2px 8px;
          font-size: 12px;
        }
      }
    }
  }
}

.mb-4 {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
