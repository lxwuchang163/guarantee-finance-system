<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>担保集团业务财务系统</h2>
        <p>Guarantee Finance Management System</p>
      </div>
      
      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
          <el-tab-pane label="账号密码登录" name="password"></el-tab-pane>
          <el-tab-pane label="短信验证码登录" name="sms"></el-tab-pane>
          <el-tab-pane label="微信登录" name="wechat"></el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 账号密码登录 -->
      <el-form v-if="activeTab === 'password'" ref="formRef" :model="loginForm" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 短信验证码登录 -->
      <el-form v-else-if="activeTab === 'sms'" ref="smsFormRef" :model="smsForm" :rules="smsRules" size="large">
        <el-form-item prop="phone">
          <el-input v-model="smsForm.phone" placeholder="请输入手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="code">
          <el-input
            v-model="smsForm.code"
            placeholder="请输入验证码"
            prefix-icon="Message"
            @keyup.enter="handleSmsLogin"
          >
            <template #append>
              <el-button
                :disabled="countdown > 0"
                @click="handleSendSmsCode"
                type="text"
                class="countdown-btn"
              >
                {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="smsLoading" class="login-btn" @click="handleSmsLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 微信登录 -->
      <div v-else-if="activeTab === 'wechat'" class="wechat-login">
        <div v-if="!qrCodeUrl" class="wechat-loading" :loading="wechatLoading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>生成二维码中...</span>
        </div>
        <div v-else class="wechat-qr-code">
          <img :src="qrCodeUrl" alt="微信登录二维码" class="qr-code-img" />
          <p class="qr-code-tip">请使用微信扫描二维码登录</p>
          <p v-if="wechatStatus" class="wechat-status">{{ wechatStatus }}</p>
        </div>
      </div>
      
      <div class="login-footer">
        <span>默认账号: admin / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, User, Phone, Message, Loading } from '@element-plus/icons-vue'
import { sendSmsCode, loginBySms, getWechatQrCode, checkWechatLoginStatus } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const smsFormRef = ref<FormInstance>()
const loading = ref(false)
const smsLoading = ref(false)
const wechatLoading = ref(false)
const activeTab = ref('password')
const qrCodeUrl = ref('')
const qrCodeTicket = ref('')
const wechatStatus = ref('')
const countdown = ref(0)
let countdownTimer: number | null = null
let statusCheckTimer: number | null = null

// 账号密码登录表单
const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

// 短信验证码登录表单
const smsForm = reactive({
  phone: '',
  code: ''
})

// 表单验证规则
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const smsRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 账号密码登录
async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.userLogin(loginForm)
    await userStore.fetchUserInfo()
    router.push('/dashboard')
    ElMessage.success('登录成功')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 发送短信验证码
async function handleSendSmsCode() {
  const valid = await smsFormRef.value?.validateField('phone').catch(() => false)
  if (!valid) return

  try {
    await sendSmsCode(smsForm.phone)
    ElMessage.success('验证码发送成功')
    startCountdown()
  } catch (error) {
    console.error(error)
    ElMessage.error('验证码发送失败')
  }
}

// 开始倒计时
function startCountdown() {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = window.setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      if (countdownTimer) clearInterval(countdownTimer)
    }
  }, 1000)
}

// 短信验证码登录
async function handleSmsLogin() {
  const valid = await smsFormRef.value?.validate().catch(() => false)
  if (!valid) return

  smsLoading.value = true
  try {
    const result = await loginBySms(smsForm.phone, smsForm.code) as any
    if (result.code === 200 && result.data) {
      const loginData = result.data
      userStore.token = loginData.token
      localStorage.setItem('token', loginData.token)
      await userStore.fetchUserInfo()
      router.push('/dashboard')
      ElMessage.success('登录成功')
    } else {
      ElMessage.error('登录失败，请检查验证码是否正确')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('登录失败，请检查验证码是否正确')
  } finally {
    smsLoading.value = false
  }
}

// 生成微信登录二维码
async function generateWechatQrCode() {
  wechatLoading.value = true
  wechatStatus.value = ''
  try {
    const result = await getWechatQrCode() as any
    if (result.code === 200 && result.data) {
      qrCodeUrl.value = result.data.qrCodeUrl
      qrCodeTicket.value = result.data.ticket
      startStatusCheck()
    } else {
      ElMessage.error('生成二维码失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('生成二维码失败')
  } finally {
    wechatLoading.value = false
  }
}

// 开始检查二维码状态
function startStatusCheck() {
  if (statusCheckTimer) clearInterval(statusCheckTimer)
  statusCheckTimer = window.setInterval(async () => {
    if (!qrCodeTicket.value) return
    
    try {
      const result = await checkWechatLoginStatus(qrCodeTicket.value) as any
      if (result.code === 200 && result.data) {
        const status = result.data
        wechatStatus.value = status.message
        
        if (status.status === 'scanned' && status.userId) {
          if (statusCheckTimer) clearInterval(statusCheckTimer)
          // 这里应该调用登录接口获取token
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else if (status.status === 'expired') {
          if (statusCheckTimer) clearInterval(statusCheckTimer)
          ElMessage.error('二维码已过期，请重新生成')
          qrCodeUrl.value = ''
          qrCodeTicket.value = ''
        }
      }
    } catch (error) {
      console.error(error)
    }
  }, 2000)
}

// 切换登录方式
function handleTabChange() {
  if (activeTab.value === 'wechat') {
    generateWechatQrCode()
  }
}

// 组件挂载时
onMounted(() => {
  // 可以在这里添加初始化逻辑
})

// 组件卸载时
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (statusCheckTimer) clearInterval(statusCheckTimer)
})
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 500px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);

  .login-header {
    text-align: center;
    margin-bottom: 30px;

    h2 {
      color: #333;
      font-size: 24px;
      margin-bottom: 8px;
    }

    p {
      color: #999;
      font-size: 13px;
    }
  }

  .login-tabs {
    margin-bottom: 30px;
  }

  .login-btn {
    width: 100%;
  }

  .countdown-btn {
    color: #667eea;
  }

  .wechat-login {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;

    .wechat-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px 0;

      span {
        margin-top: 16px;
        color: #666;
      }
    }

    .wechat-qr-code {
      text-align: center;

      .qr-code-img {
        width: 200px;
        height: 200px;
        margin-bottom: 16px;
      }

      .qr-code-tip {
        color: #666;
        margin-bottom: 12px;
      }

      .wechat-status {
        color: #667eea;
        font-size: 14px;
      }
    }
  }

  .login-footer {
    text-align: center;
    margin-top: 16px;
    color: #999;
    font-size: 12px;
  }
}
</style>
