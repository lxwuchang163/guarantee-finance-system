import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },

      {
        path: 'receipt',
        name: 'Receipt',
        component: () => import('@/views/receipt/index.vue'),
        meta: { title: '收款单管理', icon: 'Wallet' }
      },
      {
        path: 'payment',
        name: 'Payment',
        component: () => import('@/views/payment/index.vue'),
        meta: { title: '付款单管理', icon: 'CreditCard' }
      },
      {
        path: 'reconciliation',
        name: 'Reconciliation',
        component: () => import('@/views/reconciliation/index.vue'),
        meta: { title: '银行对账', icon: 'Document' }
      },
      {
        path: 'bank',
        name: 'Bank',
        component: () => import('@/views/bank/index.vue'),
        meta: { title: '银企直连', icon: 'Link' }
      },
      {
        path: 'accounting',
        redirect: '/accounting/subject'
      },
      {
        path: 'accounting/subject',
        name: 'SubjectManage',
        component: () => import('@/views/accounting/subject.vue'),
        meta: { title: '科目管理', icon: 'List' }
      },
      {
        path: 'accounting/auxiliary',
        name: 'AuxiliaryManage',
        component: () => import('@/views/accounting/auxiliary.vue'),
        meta: { title: '辅助核算', icon: 'Grid' }
      },
      {
        path: 'accounting/voucher',
        name: 'VoucherManage',
        component: () => import('@/views/accounting/voucher.vue'),
        meta: { title: '凭证管理', icon: 'Tickets' }
      },
      {
        path: 'accounting/voucher-rule',
        name: 'VoucherRule',
        component: () => import('@/views/accounting/voucher-rule/index.vue'),
        meta: { title: '凭证规则', icon: 'SetUp' }
      },
      {
        path: 'accounting/general-ledger',
        name: 'GeneralLedger',
        component: () => import('@/views/accounting/general-ledger/index.vue'),
        meta: { title: '总分类账', icon: 'Notebook' }
      },
      {
        path: 'accounting/detail-ledger',
        name: 'DetailLedger',
        component: () => import('@/views/accounting/detail-ledger/index.vue'),
        meta: { title: '明细分类账', icon: 'Document' }
      },
      {
        path: 'accounting/balance-report',
        name: 'BalanceReport',
        component: () => import('@/views/accounting/balance-report/index.vue'),
        meta: { title: '科目余额表', icon: 'DataBoard' }
      },
      {
        path: 'accounting/period',
        name: 'PeriodManage',
        component: () => import('@/views/accounting/period/index.vue'),
        meta: { title: '期末处理', icon: 'Calendar' }
      },
      {
        path: 'accounting/carry-forward',
        name: 'CarryForward',
        component: () => import('@/views/accounting/carry-forward/index.vue'),
        meta: { title: '自动结转', icon: 'Switch' }
      },
      {
        path: 'accounting/report',
        name: 'FinancialReport',
        component: () => import('@/views/accounting/report/index.vue'),
        meta: { title: '报表管理', icon: 'TrendCharts' }
      },
      {
        path: 'cfca',
        name: 'CfcaManage',
        component: () => import('@/views/cfca/index.vue'),
        meta: { title: 'CFCA认证', icon: 'Key' }
      },
      {
        path: 'schedule',
        name: 'ScheduleManage',
        component: () => import('@/views/schedule/index.vue'),
        meta: { title: '定时任务', icon: 'Timer' }
      },
      {
        path: 'system/base',
        name: 'BaseManagement',
        meta: { title: '基础管理', icon: 'Setting' },
        children: [
          {
            path: 'org',
            name: 'OrgManage',
            component: () => import('@/views/system/org/index.vue'),
            meta: { title: '机构管理', icon: 'OfficeBuilding' }
          },
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理', icon: 'User' }
          },
          {
            path: 'role',
            name: 'RoleManage',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理', icon: 'UserFilled' }
          },
          {
            path: 'menu',
            name: 'MenuManage',
            component: () => import('@/views/system/menu/index.vue'),
            meta: { title: '菜单权限', icon: 'Menu' }
          },
          {
            path: 'sync',
            name: 'Sync',
            component: () => import('@/views/sync/index.vue'),
            meta: { title: '基础信息同步', icon: 'Refresh' }
          },
          {
            path: 'notice',
            name: 'NoticeManage',
            component: () => import('@/views/system/notice/index.vue'),
            meta: { title: '公告管理', icon: 'Bell' }
          }
        ]
      },
      {
        path: 'system/process',
        name: 'ProcessManage',
        component: () => import('@/views/system/process/index.vue'),
        meta: { title: '审批流程', icon: 'SetUp' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || ''} - 担保集团业务财务系统`
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    if (userStore.isLoggedIn && userStore.menuList.length === 0) {
      try {
        await userStore.loadMenuAndPermissions()
      } catch (e) {
        console.error('加载菜单权限失败:', e)
      }
    }
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
