<template>
  <div class="page-container">
    <!-- 同步概览 -->
    <el-row :gutter="16" class="sync-overview">
      <el-col :span="6" v-for="item in syncOverview" :key="item.type">
        <el-card shadow="hover" class="overview-card" :body-style="{ padding: '20px' }">
          <div class="overview-icon" :style="{ background: item.color }">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </div>
          <div class="overview-info">
            <div class="overview-label">{{ item.label }}</div>
            <div class="overview-value">{{ item.value }}</div>
            <div class="overview-time">上次: {{ item.lastTime || '-' }}</div>
          </div>
          <el-button
            type="primary"
            size="small"
            :loading="item.syncing"
            @click="handleSync(item.type)"
          >立即同步</el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容区 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <span>基础信息同步管理</span>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 客户信息 -->
        <el-tab-pane label="客户信息" name="customer">
          <div class="tab-toolbar">
            <el-button type="primary" icon="Refresh" @click="handleSync('customer')" :loading="syncingMap.customer">
              全量同步
            </el-button>
            <el-button type="success" @click="handleIncrementalSync('customer')" :loading="syncingMap.incrementalCustomer">
              增量同步
            </el-button>
          </div>

          <el-table :data="customerList" stripe border v-loading="loading" style="margin-top: 12px">
            <el-table-column prop="customerCode" label="客户编码" width="130" />
            <el-table-column prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="customerType" label="类型" width="90" align="center">
              <template #default="{ row }">
                {{ row.customerType === 1 ? '个人' : row.customerType === 2 ? '企业' : '机构' }}
              </template>
            </el-table-column>
            <el-table-column prop="contactPhone" label="联系电话" width="130" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastSyncTime" label="最后同步时间" width="170">
              <template #default="{ row }">{{ formatTime(row.lastSyncTime) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="queryPage.current"
              v-model:page-size="queryPage.size"
              :page-sizes="[10, 20, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="loadCustomers"
              @current-change="loadCustomers"
            />
          </div>
        </el-tab-pane>

        <!-- 业务品种 -->
        <el-tab-pane label="业务品种" name="product">
          <div class="tab-toolbar">
            <el-button type="primary" icon="Refresh" @click="handleSync('product')" :loading="syncingMap.product">
              同步业务品种
            </el-button>
          </div>
          <el-empty description="业务品种同步功能开发中..." />
        </el-tab-pane>

        <!-- 银行账号 -->
        <el-tab-pane label="银行账号" name="account">
          <div class="tab-toolbar">
            <el-button type="primary" icon="Refresh" @click="handleSync('account')" :loading="syncingMap.account">
              同步银行账号
            </el-button>
          </div>
          <el-empty description="银行账号同步功能开发中..." />
        </el-tab-pane>

        <!-- 同步日志 -->
        <el-tab-pane label="同步日志" name="log">
          <el-empty description="同步日志功能开发中..." />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, User, Goods, CreditCard, Document } from '@element-plus/icons-vue'
import { getCustomerPage, syncCustomerFull, syncCustomerIncremental } from '@/api/sync'
import type { CustomerVO } from '@/api/sync'

const loading = ref(false)
const activeTab = ref('customer')
const customerList = ref<CustomerVO[]>([])
const total = ref(0)
const queryPage = reactive({ current: 1, size: 10 })

// 同步概览数据
const syncOverview = ref([
  { type: 'customer', label: '客户信息', value: '0', lastTime: '', color: '#409eff', icon: User, syncing: false },
  { type: 'product', label: '业务品种', value: '0', lastTime: '', color: '#67c23a', icon: Goods, syncing: false },
  { type: 'account', label: '银行账号', value: '0', lastTime: '', color: '#e6a23c', icon: CreditCard, syncing: false },
  { type: 'org', label: '机构信息', value: '0', lastTime: '', color: '#f56c6c', icon: Document, syncing: false }
])

const syncingMap = reactive<Record<string, boolean>>({
  customer: false,
  incrementalCustomer: false,
  product: false,
  account: false,
  org: false
})

// 加载客户列表
const loadCustomers = async () => {
  loading.value = true
  try {
    const res = await getCustomerPage({
      keyword: '',
      customerType: null,
      status: null,
      current: queryPage.current,
      size: queryPage.size
    })
    customerList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载客户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 执行同步
const handleSync = async (type: string) => {
  const item = syncOverview.value.find(i => i.type === type)
  if (item) item.syncing = true
  syncingMap[type] = true

  try {
    if (type === 'customer') {
      await syncCustomerFull()
      ElMessage.success('客户信息全量同步任务已提交')
    }
    // 其他类型同步...
    loadCustomers()
  } catch (error: any) {
    ElMessage.error(error.message || '同步失败')
  } finally {
    if (item) item.syncing = false
    syncingMap[type] = false
  }
}

// 增量同步
const handleIncrementalSync = async (type: string) => {
  syncingMap['incremental' + type.charAt(0).toUpperCase() + type.slice(1)] = true

  try {
    if (type === 'customer') {
      await syncCustomerIncremental()
      ElMessage.success('客户信息增量同步任务已提交')
    }
    loadCustomers()
  } catch (error: any) {
    ElMessage.error(error.message || '增量同步失败')
  } finally {
    syncingMap['incrementalCustomer'] = false
  }
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

onMounted(() => {
  loadCustomers()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 16px;
}

.sync-overview {
  .overview-card {
    display: flex;
    align-items: center;
    gap: 16px;

    .overview-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .overview-info {
      flex: 1;
      min-width: 0;

      .overview-label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 4px;
      }

      .overview-value {
        font-size: 22px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 4px;
      }

      .overview-time {
        font-size: 12px;
        color: #b0b0b0;
      }
    }
  }
}

.tab-toolbar {
  margin-bottom: 8px;

  .el-button + .el-button {
    margin-left: 8px;
  }
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
