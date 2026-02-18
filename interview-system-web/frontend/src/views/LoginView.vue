<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <h2 class="title">面试系统 - Web版</h2>
      <p class="subtitle">计算机科学与工程能力与潜力测评系统</p>
      
      <el-form 
        :model="form" 
        :rules="rules"
        ref="formRef"
        @submit.prevent="handleLogin"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            native-type="submit" 
            :loading="authStore.loading"
            size="large"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <el-alert
        v-if="authStore.error"
        :title="authStore.error"
        type="error"
        :closable="false"
        style="margin-top: 15px"
      />
      
      <div class="tip">
        <el-tag type="info" size="small">提示</el-tag>
        <span>默认账号: admin / admin123</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  const success = await authStore.login(form.username, form.password)
  if (success) {
    router.push('/dashboard')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
}

.title {
  text-align: center;
  margin-bottom: 8px;
  color: #1e293b;
  font-size: 24px;
}

.subtitle {
  text-align: center;
  margin-bottom: 24px;
  color: #64748b;
  font-size: 14px;
}

.tip {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
}
</style>
