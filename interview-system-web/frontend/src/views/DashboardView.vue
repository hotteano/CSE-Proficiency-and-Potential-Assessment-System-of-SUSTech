<template>
  <div class="dashboard">
    <h1>欢迎使用面试系统</h1>
    <p>当前角色: {{ authStore.userRole }}</p>
    
    <el-row :gutter="20" class="cards">
      <el-col :span="8">
        <el-card>
          <h3>题目总数</h3>
          <p class="number">{{ stats.questionCount }}</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <h3>面试记录</h3>
          <p class="number">{{ stats.interviewCount }}</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <h3>用户数量</h3>
          <p class="number">{{ stats.userCount }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { questionApi } from '@/api'

const authStore = useAuthStore()
const stats = ref({
  questionCount: 0,
  interviewCount: 0,
  userCount: 0
})

onMounted(async () => {
  try {
    const questions = await questionApi.getAll()
    stats.value.questionCount = questions.length
  } catch (e) {
    console.error('加载数据失败', e)
  }
})
</script>

<style scoped>
.dashboard h1 {
  margin-bottom: 10px;
  color: #1e293b;
}

.cards {
  margin-top: 30px;
}

.number {
  font-size: 32px;
  font-weight: bold;
  color: #4f46e5;
  margin-top: 10px;
}
</style>
