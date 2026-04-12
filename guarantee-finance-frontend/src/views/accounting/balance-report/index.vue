<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>科目余额表</span>
          <div>
            <el-date-picker v-model="period" type="month" placeholder="选择期间" value-format="YYYYMM" style="width:150px;margin-right:10px" />
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe border show-summary>
        <el-table-column prop="subjectCode" label="科目编码" width="120" />
        <el-table-column prop="subjectName" label="科目名称" min-width="150" />
        <el-table-column prop="beginDebit" label="期初借方" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.beginDebit) }}</template>
        </el-table-column>
        <el-table-column prop="beginCredit" label="期初贷方" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.beginCredit) }}</template>
        </el-table-column>
        <el-table-column prop="currentDebit" label="本期借方" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.currentDebit) }}</template>
        </el-table-column>
        <el-table-column prop="currentCredit" label="本期贷方" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.currentCredit) }}</template>
        </el-table-column>
        <el-table-column prop="endDebit" label="期末借方" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.endDebit) }}</template>
        </el-table-column>
        <el-table-column prop="endCredit" label="期末贷方" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.endCredit) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const period = ref('')
const tableData = ref<any[]>([])

const formatAmount = (val: number) => {
  if (!val && val !== 0) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const loadData = async () => {
  if (!period.value) { ElMessage.warning('请选择期间'); return }
  const res = await request.get('/accounting/balance/page', { params: { period: period.value, current: 1, size: 500 } })
  tableData.value = res.data?.records || []
}

onMounted(() => {
  const now = new Date()
  period.value = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
