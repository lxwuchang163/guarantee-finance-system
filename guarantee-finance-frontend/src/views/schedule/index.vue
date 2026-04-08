<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header"><span>定时任务调度中心</span></div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 仪表盘 -->
        <el-tab-pane label="调度仪表盘" name="dashboard">
          <el-row :gutter="16" class="status-cards">
            <el-col :span="5"><el-card shadow="hover" :body-style="{ padding: '16px' }">
              <div class="stat-card">
                <div class="stat-icon" style="background: #409eff;"><el-icon :size="24"><List /></el-icon></div>
                <div><div class="stat-value">{{ dashboard.totalJobs ?? 0 }}</div><div class="stat-label">任务总数</div></div>
              </div>
            </el-card></el-col>
            <el-col :span="5"><el-card shadow="hover" :body-style="{ padding: '16px' }">
              <div class="stat-card">
                <div class="stat-icon" style="background: #67c23a;"><el-icon :size="24"><VideoPlay /></el-icon></div>
                <div><div class="stat-value">{{ dashboard.runningJobs ?? 0 }}</div><div class="stat-label">运行中</div></div>
              </div>
            </el-card></el-col>
            <el-col :span="5"><el-card shadow="hover" :body-style="{ padding: '16px' }">
              <div class="stat-card">
                <div class="stat-icon" style="background: #909399;"><el-icon :size="24"><VideoPause /></el-icon></div>
                <div><div class="stat-value">{{ dashboard.pausedJobs ?? 0 }}</div><div class="stat-label">已暂停</div></div>
              </div>
            </el-card></el-col>
            <el-col :span="4"><el-card shadow="hover" :body-style="{ padding: '16px' }">
              <div class="stat-card">
                <div class="stat-icon" style="background: #67c23a;"><el-icon :size="24"><CircleCheck /></el-icon></div>
                <div><div class="stat-value">{{ dashboard.todaySuccess ?? 0 }}</div><div class="stat-label">今日成功</div></div>
              </div>
            </el-card></el-col>
            <el-col :span="5"><el-card shadow="hover" :body-style="{ padding: '16px' }">
              <div class="stat-card">
                <div class="stat-icon" style="background: #f56c6c;"><el-icon :size="24"><CircleClose /></el-icon></div>
                <div><div class="stat-value">{{ dashboard.todayFail ?? 0 }}</div><div class="stat-label">今日失败</div></div>
              </div>
            </el-card></el-col>
          </el-row>

          <el-divider content-position="left">最近执行记录</el-divider>
          <el-table :data="(dashboard.recentLogs || [])" stripe border size="small" max-height="300">
            <el-table-column prop="jobName" label="任务名称" min-width="150" />
            <el-table-column prop="status" label="结果" width="80" align="center">
              <template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '成功' : '失败' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="jobMessage" label="消息" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="执行时间" width="165" />
          </el-table>
        </el-tab-pane>

        <!-- 任务管理 -->
        <el-tab-pane label="任务管理" name="jobs">
          <div class="action-bar" style="margin-bottom: 12px;">
            <el-button type="primary" icon="Plus" @click="showJobDialog = true">新增任务</el-button>
            <el-button type="info" icon="Setting" @click="initBuiltIn">初始化内置任务</el-button>
            <el-input v-model="jobQuery.jobName" placeholder="任务名称" clearable style="width: 180px;" />
            <el-select v-model="jobQuery.status" placeholder="状态" clearable style="width: 110px;">
              <el-option value="1" label="正常" /><el-option value="0" label="暂停" />
            </el-select>
            <el-button icon="Search" @click="loadJobs">查询</el-button>
          </div>

          <el-table :data="jobList" stripe border v-loading="jobLoading" size="small">
            <el-table-column prop="jobName" label="任务名称" min-width="140" />
            <el-table-column prop="jobGroup" label="分组" width="100" />
            <el-table-column prop="beanName" label="Bean名称" width="140" />
            <el-table-column prop="cronExpression" label="Cron表达式" width="130">
              <template #default="{ row }"><el-tag size="small">{{ row.cronExpression }}</el-tag></template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-switch v-model="row._statusBool" active-value="1" inactive-value="0"
                  @change="(val: string) => handleChangeStatus(row.id, val)"
                  :loading="row._switching" inline-prompt active-text="开" inactive-text="关" />
              </template>
            </el-table-column>
            <el-table-column prop="nextExecuteTime" label="下次执行" width="165" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="executeOnce(row.id)">执行一次</el-button>
                <el-button type="primary" link size="small" @click="editJob(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="deleteJobAction(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination v-model:current-page="jobQuery.current" v-model:page-size="jobQuery.size" :page-sizes="[10, 20, 50]" :total="jobTotal" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadJobs" @current-change="loadJobs" />
          </div>
        </el-tab-pane>

        <!-- 内置任务配置 -->
        <el-tab-pane label="内置任务" name="builtin">
          <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
            系统预置了10个核心定时任务，可在此调整Cron表达式和启用/禁用状态
          </el-alert>
          <el-table :data="builtInList" stripe border v-loading="builtinLoading" size="small">
            <el-table-column prop="taskCode" label="任务编码" width="220" />
            <el-table-column prop="taskName" label="任务名称" width="160" />
            <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
            <el-table-column prop="cronExpression" label="Cron表达式" width="140">
              <template #default="{ row }">
                <el-input v-model="row.cronExpression" size="small" style="width: 120px;" @change="handleBuiltinConfigChange(row)" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch v-model="row._statusBool" active-value="1" inactive-value="0"
                  @change="(val: string) => handleBuiltinToggle(row.taskCode, val)"
                  inline-prompt active-text="启" inactive-text="停" />
              </template>
            </el-table-column>
            <el-table-column prop="successCount" label="成功次数" width="80" align="center" />
            <el-table-column prop="failCount" label="失败次数" width="80" align="center" />
          </el-table>
        </el-tab-pane>

        <!-- 执行日志 -->
        <el-tab-pane label="执行日志" name="logs">
          <div class="action-bar" style="margin-bottom: 12px;">
            <el-select v-model="logQuery.jobId" placeholder="选择任务" clearable filterable style="width: 200px;">
              <el-option v-for="j in jobList" :key="j.id" :label="j.jobName" :value="j.id" />
            </el-select>
            <el-select v-model="logQuery.status" placeholder="执行结果" clearable style="width: 120px;">
              <el-option :value="0" label="成功" /><el-option :value="1" label="失败" />
            </el-select>
            <el-button icon="Search" @click="loadLogs">查询</el-button>
          </div>
          <el-table :data="logList" stripe border v-loading="logLoading" size="small">
            <el-table-column prop="jobName" label="任务名称" width="140" />
            <el-table-column prop="invokeTarget" label="调用目标" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="结果" width="80" align="center">
              <template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '成功' : '失败' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="jobMessage" label="消息" min-width="200" show-overflow-tooltip />
            <el-table-column label="耗时(ms)" width="100" align="right">
              <template #default="{ row }">{{ row.endTime && row.startTime ? (row.endTime - row.startTime) : '-' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="执行时间" width="165" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.exceptionInfo" type="danger" link size="small" @click="showException(row)">异常</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination v-model:current-page="logQuery.current" v-model:page-size="logQuery.size" :page-sizes="[10, 20, 50]" :total="logTotal" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadLogs" @current-change="loadLogs" />
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 新增/编辑任务对话框 -->
      <el-dialog v-model="showJobDialog" :title="editMode ? '编辑定时任务' : '新增定时任务'" width="550px" destroy-on-close>
        <el-form ref="jobFormRef" :model="jobForm" :rules="jobRules" label-width="100px">
          <el-form-item label="任务名称" prop="jobName"><el-input v-model="jobForm.jobName" /></el-form-item>
          <el-form-item label="任务分组" prop="jobGroup"><el-input v-model="jobForm.jobGroup" /></el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Bean名称" prop="beanName"><el-input v-model="jobForm.beanName" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="方法名" prop="methodName"><el-input v-model="jobForm.methodName" /></el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="Cron表达式" prop="cronExpression"><el-input v-model="jobForm.cronExpression" placeholder="如: 0 0 2 * * ?" /></el-form-item>
          <el-form-item label="方法参数"><el-input v-model="jobForm.methodParams" placeholder="JSON格式参数（可选）" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="jobForm.remark" type="textarea" :rows="2" /></el-form-item>
        </el-form>
        <template #footer><el-button @click="showJobDialog = false">取消</el-button><el-button type="primary" @click="handleSaveJob">保存</el-button></template>
      </el-dialog>

      <!-- 异常详情 -->
      <el-dialog v-model="exceptionVisible" title="执行异常详情" width="600px">
        <el-input :model-value="exceptionInfo" type="textarea" :rows="15" readonly style="font-family: monospace;" />
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, VideoPlay, VideoPause, CircleCheck, CircleClose, Plus, Setting, Search } from '@element-plus/icons-vue'
import {
  getScheduleDashboard, getJobPage, createJob, updateJob, deleteJob as delJob,
  changeJobStatus, executeOnce, getLogPage, getBuiltInTasks, updateBuiltInTask, initBuiltInTasks
} from '@/api/schedule'
import type { ScheduleJobVO, ScheduleJobLogVO, ScheduleTaskConfigVO } from '@/api/schedule'

const activeTab = ref('dashboard')
const dashboard = ref<any>({})
const jobList = ref<(ScheduleJobVO & { _statusBool?: string; _switching?: boolean })[]>([])
const builtInList = ref<(ScheduleTaskConfigVO & { _statusBool?: string })[]>([])
const logList = ref<ScheduleJobLogVO[]>([])
const jobTotal = ref(0)
const logTotal = ref(0)

const jobLoading = ref(false)
const builtinLoading = ref(false)
const logLoading = ref(false)
const showJobDialog = ref(false)
const editMode = ref(false)
const exceptionVisible = ref(false)
const exceptionInfo = ref('')

const jobFormRef = ref()
const jobForm = reactive({
  id: undefined as number | undefined,
  jobName: '', jobGroup: '', beanName: '', methodName: '',
  cronExpression: '', methodParams: '', remark: ''
})
const jobRules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  beanName: [{ required: true, message: '请输入Bean名称', trigger: 'blur' }],
  methodName: [{ required: true, message: '请输入方法名', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
}
const jobQuery = reactive({ jobName: '', status: '', current: 1, size: 10 })
const logQuery = reactive({ jobId: undefined as number | undefined, status: null as number | null, current: 1, size: 10 })

const loadDashboard = async () => {
  try {
    const res = await getScheduleDashboard()
    dashboard.value = res.data
  } catch (e) { console.error(e) }
}

const loadJobs = async () => {
  jobLoading.value = true
  try {
    const res = await getJobPage(jobQuery)
    jobList.value = res.data.records.map((r: ScheduleJobVO) => ({ ...r, _statusBool: r.status, _switching: false }))
    jobTotal.value = res.data.total
  } catch (e) { console.error(e) }
  finally { jobLoading.value = false }
}

const loadBuiltIn = async () => {
  builtinLoading.value = true
  try {
    const res = await getBuiltInTasks()
    builtInList.value = res.data.map((r: ScheduleTaskConfigVO) => ({ ...r, _statusBool: r.status }))
  } catch (e) { console.error(e) }
  finally { builtinLoading.value = false }
}

const loadLogs = async () => {
  logLoading.value = true
  try {
    const res = await getLogPage(logQuery)
    logList.value = res.data.records
    logTotal.value = res.data.total
  } catch (e) { console.error(e) }
  finally { logLoading.value = false }
}

const editJob = (row: ScheduleJobVO) => {
  editMode.value = true
  Object.assign(jobForm, { id: row.id, jobName: row.jobName, jobGroup: row.jobGroup, beanName: row.beanName, methodName: row.methodName, cronExpression: row.cronExpression, methodParams: row.methodParams, remark: row.remark })
  showJobDialog.value = true
}

const handleSaveJob = async () => {
  const valid = await jobFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editMode.value) { await updateJob(jobForm); ElMessage.success('修改成功') }
    else { await createJob(jobForm); ElMessage.success('创建成功') }
    showJobDialog.value = false
    loadJobs()
  } catch (e: any) { ElMessage.error(e.message || '保存失败') }
}

const deleteJobAction = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该定时任务？', '警告', { type: 'warning' })
    await delJob(id)
    ElMessage.success('删除成功')
    loadJobs()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message || '删除失败') }
}

const handleChangeStatus = async (id: number, status: string) => {
  const job = jobList.value.find(j => j.id === id)
  if (job) job._switching = true
  try {
    await changeJobStatus(id, status)
    ElMessage.success(status === '1' ? '已恢复运行' : '已暂停')
  } catch (e: any) {
    if (job) job._statusBool = status === '1' ? '0' : '1'
    ElMessage.error(e.message || '操作失败')
  } finally { if (job) job._switching = false }
}

const executeOnceAction = async (id: number) => {
  try {
    await executeOnce(id)
    ElMessage.success('已触发执行')
    loadLogs()
  } catch (e: any) { ElMessage.error(e.message || '执行失败') }
}

const initBuiltIn = async () => {
  try {
    await initBuiltInTasks()
    ElMessage.success('内置任务初始化成功')
    loadBuiltIn()
  } catch (e: any) { ElMessage.error(e.message || '初始化失败') }
}

const handleBuiltinToggle = async (taskCode: string, status: string) => {
  try {
    await updateBuiltInTask(taskCode, undefined, status)
    ElMessage.success(status === '1' ? '已启用' : '已禁用')
  } catch (e: any) {
    const item = builtInList.value.find(t => t.taskCode === taskCode)
    if (item) item._statusBool = status === '1' ? '0' : '1'
    ElMessage.error(e.message || '操作失败')
  }
}

const handleBuiltinConfigChange = async (row: ScheduleTaskConfigVO & { _statusBool?: string }) => {
  try {
    await updateBuiltInTask(row.taskCode, row.cronExpression, undefined)
    ElMessage.success('Cron表达式已更新')
  } catch (e: any) { ElMessage.error(e.message || '更新失败') }
}

const showException = (row: ScheduleJobLogVO) => { exceptionInfo.value = row.exceptionInfo || ''; exceptionVisible.value = true }

onMounted(() => { loadDashboard(); loadJobs(); loadBuiltIn(); loadLogs() })
</script>

<style lang="scss" scoped>
.page-container { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.status-cards .stat-card { display: flex; align-items: center; gap: 14px;
  .stat-icon { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  .stat-value { font-size: 24px; font-weight: bold; color: #303133; }
  .stat-label { font-size: 13px; color: #909399; margin-top: 2px; }
}
.action-bar { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
