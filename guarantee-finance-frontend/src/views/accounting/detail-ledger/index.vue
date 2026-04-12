<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>明细分类账</span>
          <div>
            <el-date-picker v-model="period" type="month" placeholder="选择期间" value-format="YYYYMM" style="width:150px;margin-right:10px" />
            <el-input v-model="subjectCode" placeholder="科目编码" clearable style="width:150px;margin-right:10px" />
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button type="success" @click="handleGenerate">生成明细账</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe border>
        <el-table-column prop="subjectCode" label="科目编码" width="100" />
        <el-table-column prop="subjectName" label="科目名称" width="120" />
        <el-table-column prop="voucherDate" label="日期" width="100" />
        <el-table-column prop="voucherNo" label="凭证号" width="100" />
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="debitAmount" label="借方金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.debitAmount) }}</template>
        </el-table-column>
        <el-table-column prop="creditAmount" label="贷方金额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.creditAmount) }}</template>
        </el-table-column>
        <el-table-column prop="direction" label="方向" width="60" align="center" />
        <el-table-column prop="balance" label="余额" width="130" align="right">
          <template #default="{ row }">{{ formatAmount(row.balance) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="current" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDetailLedgerPage, generateDetailLedger } from '@/api/accounting/detailLedger'

const period = ref('')
const subjectCode = ref('')
const tableData = ref<any[]>([])
const total = ref(0)
const current = ref(1)
const size = ref(20)

const formatAmount = (val: number) => {
  if (!val && val !== 0) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const loadData = async () => {
  if (!period.value) { ElMessage.warning('请选择期间'); return }
  const res = await getDetailLedgerPage({ period: period.value, subjectCode: subjectCode.value, current: current.value, size: size.value })
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const handleGenerate = async () => {
  if (!period.value) { ElMessage.warning('请选择期间'); return }
  await generateDetailLedger(period.value)
  ElMessage.success('明细分类账生成成功')
  loadData()
}

onMounted(() => {
  const now = new Date()
  period.value = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.el-pagination { margin-top: 16px; justify-content: flex-end; }
</style>
