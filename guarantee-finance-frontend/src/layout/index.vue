<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><Wallet /></el-icon>
        <span v-show="!isCollapse">担保财务系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#1890ff"
        router
      >
        <template v-for="item in menuList" :key="item.path">
          <el-sub-menu v-if="item.children && item.children.length > 0" :index="'/' + item.path">
            <template #title>
              <el-icon><component :is="item.meta?.icon || 'Document'" /></el-icon>
              <span>{{ item.meta?.title }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="'/' + child.path">
              <el-icon><component :is="child.meta?.icon || 'Document'" /></el-icon>
              <template #title>{{ child.meta?.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="'/' + item.path">
            <el-icon><component :is="item.meta?.icon || 'Document'" /></el-icon>
            <template #title>{{ item.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ currentRoute?.meta?.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Wallet, Fold, Expand } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

const menuList = [
  { path: 'dashboard', meta: { title: '仪表盘', icon: 'Odometer' } },
  { path: 'sync', meta: { title: '基础信息同步', icon: 'Refresh' } },
  { path: 'receipt', meta: { title: '收款单管理', icon: 'Wallet' } },
  { path: 'payment', meta: { title: '付款单管理', icon: 'CreditCard' } },
  { path: 'reconciliation', meta: { title: '银行对账', icon: 'Document' } },
  { path: 'bank', meta: { title: '银企直连', icon: 'Link' } },
  { path: 'accounting', meta: { title: '会计平台', icon: 'Notebook' } },
  {
    path: 'system/base',
    meta: { title: '基础管理', icon: 'Setting' },
    children: [
      { path: 'system/base/org', meta: { title: '机构管理', icon: 'OfficeBuilding' } },
      { path: 'system/base/user', meta: { title: '用户管理', icon: 'User' } },
      { path: 'system/base/role', meta: { title: '角色管理', icon: 'UserFilled' } },
      { path: 'system/base/menu', meta: { title: '菜单权限', icon: 'Menu' } }
    ]
  },
  { path: 'system/process', meta: { title: '审批流程', icon: 'SetUp' } }
]

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

async function handleCommand(command: string) {
  if (command === 'logout') {
    await userStore.userLogout()
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #fff;
      font-size: 16px;
      font-weight: bold;
      border-bottom: 1px solid #ffffff1a;

      .logo-icon {
        font-size: 32px;
        color: #1890ff;
      }
    }

  .el-menu {
    border-right: none;
  }
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #666;

      &:hover {
        color: #1890ff;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        color: #333;
        font-size: 14px;
      }
    }
  }
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
