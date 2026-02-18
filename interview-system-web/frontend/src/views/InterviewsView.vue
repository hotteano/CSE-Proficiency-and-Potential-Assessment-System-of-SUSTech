<template>
  <div class="interviews">
    <h2>面试记录</h2>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="showAddDialog">安排面试</el-button>
      </div>
      <el-table :data="records" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="candidateUsername" label="考生" />
        <el-table-column prop="examinerUsername" label="考官" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="100">
          <template #default="{ row }">
            {{ row.totalScore ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="scheduledAt" label="安排时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.scheduledAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'SCHEDULED'" 
              size="small" 
              type="primary"
              @click="startInterview(row)"
            >
              开始
            </el-button>
            <el-button 
              v-if="row.status === 'IN_PROGRESS'" 
              size="small" 
              type="success"
              @click="completeInterview(row)"
            >
              完成
            </el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 安排面试对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="安排面试"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="考生ID" prop="candidateId">
          <el-input-number v-model="form.candidateId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="考生用户名" prop="candidateUsername">
          <el-input v-model="form.candidateUsername" placeholder="请输入考生用户名" />
        </el-form-item>
        <el-form-item label="安排时间" prop="scheduledAt">
          <el-date-picker
            v-model="form.scheduledAt"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="面试详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)">
            {{ getStatusLabel(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="考生">{{ currentRecord.candidateUsername }}</el-descriptions-item>
        <el-descriptions-item label="考官">{{ currentRecord.examinerUsername }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ currentRecord.totalScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="安排时间">{{ formatDate(currentRecord.scheduledAt) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDate(currentRecord.startedAt) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatDate(currentRecord.completedAt) }}</el-descriptions-item>
        <el-descriptions-item label="评价" :span="2">{{ currentRecord.evaluation || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 完成面试对话框 -->
    <el-dialog
      v-model="completeVisible"
      title="完成面试"
      width="500px"
    >
      <el-form :model="completeForm" label-width="80px">
        <el-form-item label="总分">
          <el-input-number v-model="completeForm.totalScore" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input
            v-model="completeForm.evaluation"
            type="textarea"
            :rows="4"
            placeholder="请输入面试评价"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete" :loading="completing">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { interviewApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const records = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const completeVisible = ref(false)
const submitting = ref(false)
const completing = ref(false)
const formRef = ref(null)
const currentRecord = ref({})
const completeForm = ref({ totalScore: 0, evaluation: '' })

const form = ref({
  candidateId: null,
  candidateUsername: '',
  scheduledAt: null
})

const rules = {
  candidateId: [{ required: true, message: '请输入考生ID', trigger: 'blur' }],
  candidateUsername: [{ required: true, message: '请输入考生用户名', trigger: 'blur' }]
}

const statusMap = {
  'SCHEDULED': '已安排',
  'IN_PROGRESS': '进行中',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消'
}

const getStatusLabel = (status) => statusMap[status] || status

const getStatusType = (status) => {
  const map = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const loadRecords = async () => {
  loading.value = true
  try {
    records.value = await interviewApi.getMyRecords()
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  form.value = {
    candidateId: null,
    candidateUsername: '',
    scheduledAt: null
  }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      ...form.value,
      scheduledAt: form.value.scheduledAt?.toISOString()
    }
    await interviewApi.create(data)
    ElMessage.success('安排成功')
    dialogVisible.value = false
    loadRecords()
  } catch (e) {
    ElMessage.error(e.response?.data || '安排失败')
  } finally {
    submitting.value = false
  }
}

const startInterview = async (row) => {
  try {
    await interviewApi.start(row.id)
    ElMessage.success('面试已开始')
    loadRecords()
  } catch (e) {
    ElMessage.error(e.response?.data || '操作失败')
  }
}

const completeInterview = (row) => {
  currentRecord.value = row
  completeForm.value = { totalScore: 0, evaluation: '' }
  completeVisible.value = true
}

const submitComplete = async () => {
  completing.value = true
  try {
    await interviewApi.complete(currentRecord.value.id, completeForm.value)
    ElMessage.success('面试已完成')
    completeVisible.value = false
    loadRecords()
  } catch (e) {
    ElMessage.error(e.response?.data || '操作失败')
  } finally {
    completing.value = false
  }
}

const viewDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

const remove = (row) => {
  ElMessageBox.confirm(
    `确定要删除这条面试记录吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await interviewApi.delete(row.id)
      ElMessage.success('删除成功')
      loadRecords()
    } catch (e) {
      ElMessage.error(e.response?.data || '删除失败')
    }
  }).catch(() => {})
}

onMounted(loadRecords)
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
