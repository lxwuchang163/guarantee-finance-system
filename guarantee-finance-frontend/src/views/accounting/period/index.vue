<template>
  <div class="page-container">
    <el-card>
      <template #header><span>期末处理</span></template>
      <el-row :gutter="16">
        <el-col :span="16">
          <el-table :data="periodList" stripe border>
            <el-table-column prop="periodCode" label="期间编码" width="100" />
            <el-table-column prop="periodName" label="期间名称" width="120" />
            <el-table-column prop="startDate" label="开始日期" width="110" />
            <el-table-column prop="endDate" label="结束日期" width="110" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'OPEN' ? 'success' : row.status === 'CLOSED' ? 'danger' : 'warning'" size="small">
                  {{ row.status === 'OPEN' ? '开启' : row.status === 'CLOSED' ? '已结账' : '暂封' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isCurrent" label="当前" width="60">
              <template #default="{ row }"><el-tag v-if="row.isCurrent === 1" type="success" size="small">是</el-tag></template>
            </el-table-column>
            <el-table-column prop="closingType" label="结账类型" width="80">
              <template #default="{ row }">{{ ({ NONE: '未结账', TEMP: '暂结', FINAL: '最终结账' } as any)[row.closingType as string] || '' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'OPEN'" type="primary" size="small" link @click="handleClose(row)">结账</el-button>
                <el-button v-if="row.status === 'CLOSED'" type="warning" size="small" link @click="handleReopen(row)">反结账</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header><span>操作</span></template>
            <el-form label-width="80px">
              <el-form-item label="年度">
                <el-input-number v-model="initYear" :min="2020" :max="2030" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleInit">初始化年度期间</el-button>
              </el-form-item>
            </el-form>
            <el-divider />
            <h4>损益结转</h4>
            <el-form label-width="80px">
              <el-form-item label="期间">
                <el-input v-model="carryPeriod" placeholder="如202604" />
              </el-form-item>
              <el-form-item>
                <el-button type="warning" @click="handlePreview">预览</el-button>
                <el-button type="danger" @click="handleCarryForward">执行结转</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="previewVisible" title="损益结转预览" width="600px">
      <el-table :data="previewData" stripe border>
        <el-table-column prop="subjectCode" label="科目编码" width="100" />
        <el-table-column prop="subjectName" label="科目名称" min-width="150" />
        <el-table-column prop="direction" label="方向" width="60" />
        <el-table-column prop="netAmount" label="净额" width="130" align="right">
          <template #default="{ row }">{{ Number(row.netAmount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPeriodList, initPeriods, closePeriod, reopenPeriod } from '@/api/accounting/period'
import { previewCarryForward, carryForwardProfitLoss } from '@/api/accounting/carryForward'

const periodList = ref<any[]>([])
const initYear = ref(new Date().getFullYear())
const carryPeriod = ref('')
const previewVisible = ref(false)
const previewData = ref<any[]>([])

const loadData = async () => {
  const res = await getPeriodList(initYear.value)
  periodList.value = res.data || []
}

const handleInit = async () => {
  await ElMessageBox.confirm(`确认初始化${initYear.value}年会计期间？`, '提示', { type: 'warning' })
  await initPeriods(initYear.value)
  ElMessage.success('初始化成功')
  loadData()
}

const handleClose = async (row: any) => {
  await ElMessageBox.confirm(`确认对${row.periodName}进行结账？`, '提示', { type: 'warning' })
  await closePeriod(row.periodCode)
  ElMessage.success('结账成功')
  loadData()
}

const handleReopen = async (row: any) => {
  await ElMessageBox.confirm(`确认对${row.periodName}进行反结账？`, '提示', { type: 'warning' })
  await reopenPeriod(row.periodCode)
  ElMessage.success('反结账成功')
  loadData()
}

const handlePreview = async () => {
  if (!carryPeriod.value) { ElMessage.warning('请输入期间'); return }
  const res = await previewCarryForward(carryPeriod.value)
  previewData.value = res.data || []
  previewVisible.value = true
}

const handleCarryForward = async () => {
  if (!carryPeriod.value) { ElMessage.warning('请输入期间'); return }
  await ElMessageBox.confirm('确认执行损益结转？', '提示', { type: 'warning' })
  await carryForwardProfitLoss(carryPeriod.value)
  ElMessage.success('损益结转成功')
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
