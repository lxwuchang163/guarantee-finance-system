<template>
  <div class="accounting-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>会计平台</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="科目管理" name="subject">
          <subject-page />
        </el-tab-pane>
        <el-tab-pane label="辅助核算" name="auxiliary">
          <auxiliary-page />
        </el-tab-pane>
        <el-tab-pane label="凭证管理" name="voucher">
          <voucher-page />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SubjectPage from './subject.vue'
import AuxiliaryPage from './auxiliary.vue'
import VoucherPage from './voucher.vue'

const activeTab = ref('subject')
</script>

<style scoped>
.accounting-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-bar {
  display: flex;
  gap: 8px;
}
</style>
