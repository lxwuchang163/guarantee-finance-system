<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
            <el-icon :size="48" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>待办事项</span>
          </template>
          <el-table :data="todoList" stripe>
            <el-table-column prop="title" label="事项名称" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'pending' ? 'warning' : 'success'">
                  {{ row.status === 'pending' ? '待处理' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>系统公告</span>
          </template>
          <div class="notice-list">
            <div v-for="(notice, index) in noticeList" :key="index" class="notice-item">
              <el-tag size="small" type="info">公告</el-tag>
              <span class="notice-title">{{ notice.title }}</span>
              <span class="notice-time">{{ notice.time }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>业务概览</span>
          </template>
          <div class="chart-placeholder">
            <p>图表区域 - 可集成 ECharts 展示业务数据统计</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const statCards = ref([
  { title: '今日收款', value: '¥125,680', icon: 'Wallet', color: '#409eff' },
  { title: '今日付款', value: '¥89,320', icon: 'CreditCard', color: '#f56c6c' },
  { title: '待审核单据', value: '23', icon: 'Document', color: '#e6a23c' },
  { title: '本月对账', value: '156', icon: 'Finished', color: '#67c23a' }
])

const todoList = ref([
  { title: '收款单 SK20240315001 待审核', type: '收款单', createTime: '2024-03-15 09:30', status: 'pending' },
  { title: '付款单 FK20240314002 待审批', type: '付款单', createTime: '2024-03-14 15:20', status: 'pending' },
  { title: '银行流水导入完成', type: '对账', createTime: '2024-03-15 10:00', status: 'done' },
  { title: '客户信息同步任务执行完成', type: '同步', createTime: '2024-03-15 08:00', status: 'done' }
])

const noticeList = ref([
  { title: '系统升级维护通知', time: '2024-03-15' },
  { title: '新功能上线：银企直连模块', time: '2024-03-10' },
  { title: '月度财务报表模板更新', time: '2024-03-05' }
])
</script>

<style lang="scss" scoped>
.dashboard-container {
  .stat-cards {
    .stat-card {
      .stat-content {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #333;
          }

          .stat-title {
            font-size: 14px;
            color: #999;
            margin-top: 8px;
          }
        }
      }
    }
  }

  .notice-list {
    .notice-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .notice-title {
        flex: 1;
        color: #333;
        font-size: 14px;
      }

      .notice-time {
        color: #999;
        font-size: 12px;
      }
    }
  }

  .chart-placeholder {
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    border-radius: 4px;
    color: #999;
  }
}
</style>
