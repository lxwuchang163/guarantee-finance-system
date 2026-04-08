<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>菜单权限管理</span>
        </div>
      </template>

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
                  ref="menuTreeRef"
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { FolderOpened, Document, Grid } from '@element-plus/icons-vue'
import { getMenuTree, getRoleMenus, assignMenuPermissions, getDataScope, setDataScope as setDataScopeApi } from '@/api/menu'
import { getAllRoles } from '@/api/role'
import { getOrgTree as getOrgTreeApi } from '@/api/org'
import type { MenuTreeVO, DataScopeVO, RoleSimpleVO, OrgTreeVO } from '@/api/menu'

const menuTreeRef = ref()
const selectedRoleId = ref<number | null>(null)
const saving = ref(false)
const savingDataScope = ref(false)

// 数据
const roleList = ref<RoleSimpleVO[]>([])
const menuTree = ref<MenuTreeVO[]>([])
const checkedMenuKeys = ref<number[]>([])
const orgTreeOptions = ref<any[]>([])

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
      if (menuTreeRef.value) {
        menuTreeRef.value.setCheckedKeys(checkedMenuKeys.value)
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
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
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
</style>
