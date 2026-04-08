<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header"><span>CFCA数字证书管理</span></div>
      </template>

      <el-row :gutter="16" class="status-cards">
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff;"><el-icon :size="24"><Key /></el-icon></div>
            <div><div class="stat-value">{{ certList.length }}</div><div class="stat-label">证书总数</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a;"><el-icon :size="24"><CircleCheck /></el-icon></div>
            <div><div class="stat-value">{{ normalCount }}</div><div class="stat-label">正常使用</div></div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c;"><el-icon :size="24"><Warning /></el-icon></div>
            <div><div class="stat-value">{{ expiringSoonCount }}</div><div class="stat-label">即将过期</div>
          </div>
        </el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c;"><el-icon :size="24"><CircleClose /></el-icon></div>
            <div><div class="stat-value">{{ expiredCount }}</div><div class="stat-label">已过期/吊销</div></div>
          </div>
        </el-card></el-col>
      </el-row>

      <div class="action-bar" style="margin-top: 16px;">
        <el-button type="primary" icon="Refresh" @click="refreshCertsAction" :loading="refreshing">刷新证书</el-button>
        <el-button type="warning" icon="Clock" @click="checkExpiryAction" :loading="expiryLoading">到期检查</el-button>
        <el-button type="success" icon="EditPen" @click="showSignDialog = true">签名测试</el-button>
      </div>

      <!-- 证书列表 -->
      <el-table :data="certList" stripe border v-loading="tableLoading" size="small" style="margin-top: 12px;">
        <el-table-column prop="certSubject" label="证书主题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="certNo" label="证书编号" width="180" show-overflow-tooltip />
        <el-table-column prop="ownerName" label="持有人" width="100" />
        <el-table-column prop="validFrom" label="生效日期" width="110" />
        <el-table-column prop="validTo" label="到期日期" width="110" />
        <el-table-column prop="daysUntilExpiry" label="剩余天数" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: getExpiryColor(row.daysUntilExpiry), fontWeight: 'bold' }">{{ row.daysUntilExpiry != null ? row.daysUntilExpiry + '天' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="algorithm" label="算法" width="80" align="center" />
        <el-table-column prop="keyLength" label="密钥长度" width="80" align="center" />
        <el-table-column prop="signCount" label="签名次数" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small" effect="dark">{{ row.statusText || '-' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 签名测试对话框 -->
    <el-dialog v-model="showSignDialog" title="CFCA数字签名测试" width="500px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        根据金额自动选择签名人级别：≤5万单人签、5万~50万双人签、>50万三人签+审批
      </el-alert>
      <el-form ref="signFormRef" :model="signForm" :rules="signRules" label-width="100px">
        <el-form-item label="付款单号" prop="paymentNo"><el-input v-model="signForm.paymentNo" placeholder="输入模拟付款单号" /></el-form-item>
        <el-form-item label="签名金额" prop="amount">
          <el-input-number v-model="signForm.amount" :precision="2" :min="0" :controls="false" style="width: 100%;" />
          <span style="margin-left: 12px; color: #909399;">自动级别: {{ signLevelText }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSignDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSign" :loading="signing">执行签名</el-button>
      </template>
    </el-dialog>

    <!-- 签名结果 -->
    <el-dialog v-model="showResultDialog" title="签名结果" width="480px">
      <el-result :icon="signResult?.success ? 'success' : 'error'" :title="signResult?.success ? '签名成功' : '签名失败'" :sub-title="signResult?.message">
        <template #extra v-if="signResult?.signatureData">
          <el-input :model-value="signResult.signatureData" type="textarea" :rows="4" readonly />
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Key, CircleCheck, Warning, CircleClose, Refresh, Clock, EditPen } from '@element-plus/icons-vue'
import { getCertificateList, signPayment, refreshCerts, checkCertExpiry } from '@/api/cfca'
import type { CfcaCertificateVO, CfcaSignDTO, CfcaSignResult } from '@/api/cfca'

const tableLoading = ref(false)
const refreshing = ref(false)
const expiryLoading = ref(false)
const signing = ref(false)
const certList = ref<CfcaCertificateVO[]>([])
const showSignDialog = ref(false)
const showResultDialog = ref(false)
const signResult = ref<CfcaSignResult | null>(null)

const signFormRef = ref()
const signForm = reactive<CfcaSignDTO>({ paymentId: 0, paymentNo: '', amount: 0, signLevel: 1 })
const signRules = {
  paymentNo: [{ required: true, message: '请输入付款单号', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入签名金额', trigger: 'blur' }]
}

const normalCount = computed(() => certList.value.filter(c => c.status === 1).length)
const expiringSoonCount = computed(() => certList.value.filter(c => c.isExpiringSoon).length)
const expiredCount = computed(() => certList.value.filter(c => c.status === 0 || c.status === 2).length)
const signLevelText = computed(() => ({ 1: '单人签(≤5万)', 2: '双人签(5万~50万)', 3: '三人签+审批(>50万)' }[signForm.signLevel] || ''))

watch(() => signForm.amount, (val) => {
  if (val <= 50000) signForm.signLevel = 1
  else if (val <= 500000) signForm.signLevel = 2
  else signForm.signLevel = 3
})

const loadCerts = async () => {
  tableLoading.value = true
  try {
    const res = await getCertificateList()
    certList.value = res.data
  } catch (e) { console.error(e) }
  finally { tableLoading.value = false }
}

const handleSign = async () => {
  const valid = await signFormRef.value?.validate().catch(() => false)
  if (!valid) return
  signing.value = true
  try {
    const res = await signPayment(signForm)
    signResult.value = res.data
    showSignDialog.value = false
    showResultDialog.value = true
    if (res.data.success) ElMessage.success('CFCA数字签名成功')
    else ElMessage.error(res.data.message || '签名失败')
  } catch (e: any) { ElMessage.error(e.message || '签名失败') }
  finally { signing.value = false }
}

const refreshCertsAction = async () => {
  refreshing.value = true
  try { await refreshCerts(); ElMessage.success('证书列表已刷新'); loadCerts() }
  catch (e: any) { ElMessage.error(e.message || '刷新失败') }
  finally { refreshing.value = false }
}

const checkExpiryAction = async () => {
  expiryLoading.value = true
  try { await checkCertExpiry(); ElMessage.success('到期检查完成'); loadCerts() }
  catch (e: any) { ElMessage.error(e.message || '检查失败') }
  finally { expiryLoading.value = false }
}

const getStatusTagType = (s: number): string => ({ 0: 'danger', 1: 'success', 2: 'danger', 3: 'warning' }[s] || 'info')
const getExpiryColor = (days?: number): string => {
  if (days == null) return '#909399'
  if (days <= 0) return '#f56c6c'
  if (days <= 30) return '#e6a23c'
  return '#67c23a'
}

onMounted(() => loadCerts())
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
</style>
