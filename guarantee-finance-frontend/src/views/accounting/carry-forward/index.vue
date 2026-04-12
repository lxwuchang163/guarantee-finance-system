<template>
  <div class="page-container">
    <el-card>
      <template #header><span>自动结转</span></template>
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="期间"><el-input v-model="queryForm.period" placeholder="如202604" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>
      <el-table :data="tableData" stripe border>
        <el-table-column prop="period" label="期间" width="100" />
        <el-table-column prop="carryType" label="结转类型" width="120">
          <template #default="{ row }">{{ ({ PROFIT_LOSS: '损益结转', COST: '成本结转', PERIOD_END: '期末结转' } as any)[row.carryType as string] || row.carryType }}</template>
        </el-table-column>
        <el-table-column prop="sourceSubjectCode" label="源科目" width="120" />
        <el-table-column prop="targetSubjectCode" label="目标科目" width="120" />
        <el-table-column prop="amount" label="金额" width="130" align="right">
          <template #default="{ row }">{{ Number(row.amount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</template>
        </el-table-column>
        <el-table-column prop="voucherNo" label="凭证号" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="({ PENDING: 'warning', COMPLETED: 'success', REVERSED: 'info' } as Record<string,string>)[row.status as string]" size="small">
              {{ ({ PENDING: '待处理', COMPLETED: '已完成', REVERSED: '已冲回' } as Record<string,string>)[row.status as string] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'COMPLETED'" type="warning" size="small" link @click="handleReverse(row)">冲回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCarryForwardList, reverseCarryForward } from '@/api/accounting/carryForward'

const queryForm = ref({ period: '' })
const tableData = ref<any[]>([])

const loadData = async () => {
  const res = await getCarryForwardList(queryForm.value.period)
  tableData.value = res.data || []
}

const handleReverse = async (row: any) => {
  await ElMessageBox.confirm('确认冲回此结转记录？', '提示', { type: 'warning' })
  await reverseCarryForward(row.id)
  ElMessage.success('冲回成功')
  loadData()
}

onMounted(() => {
  const now = new Date()
  queryForm.value.period = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}`
  loadData()
})
</script>
