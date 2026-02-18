<template>
  <div class="questions">
    <h2>题目管理</h2>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="showAddDialog">新增题目</el-button>
      </div>
      <el-table :data="questions" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            {{ getTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelLabel(row.level) }}
          </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="active" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'">
              {{ row.active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题目' : '新增题目'"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题目标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
          />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input
            v-model="form.answer"
            type="textarea"
            :rows="4"
            placeholder="请输入参考答案"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="技术题" value="TECHNICAL" />
                <el-option label="行为题" value="BEHAVIORAL" />
                <el-option label="场景题" value="SCENARIO" />
                <el-option label="算法题" value="ALGORITHM" />
                <el-option label="系统设计题" value="SYSTEM_DESIGN" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度" prop="level">
              <el-select v-model="form.level" placeholder="请选择难度" style="width: 100%">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
                <el-option label="专家" value="EXPERT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类，如：Java基础" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { questionApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const questions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  id: null,
  title: '',
  content: '',
  answer: '',
  type: 'TECHNICAL',
  level: 'EASY',
  category: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

const typeMap = {
  'TECHNICAL': '技术题',
  'BEHAVIORAL': '行为题',
  'SCENARIO': '场景题',
  'ALGORITHM': '算法题',
  'SYSTEM_DESIGN': '系统设计题',
  'OTHER': '其他'
}

const levelMap = {
  'EASY': '简单',
  'MEDIUM': '中等',
  'HARD': '困难',
  'EXPERT': '专家'
}

const getTypeLabel = (type) => typeMap[type] || type
const getLevelLabel = (level) => levelMap[level] || level

const getLevelType = (level) => {
  const map = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger',
    'EXPERT': 'danger'
  }
  return map[level] || 'info'
}

const loadQuestions = async () => {
  loading.value = true
  try {
    questions.value = await questionApi.getAll()
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    title: '',
    content: '',
    answer: '',
    type: 'TECHNICAL',
    level: 'EASY',
    category: ''
  }
}

const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const edit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await questionApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await questionApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadQuestions()
  } catch (e) {
    ElMessage.error(e.response?.data || '操作失败')
  } finally {
    submitting.value = false
  }
}

const remove = (row) => {
  ElMessageBox.confirm(
    `确定要删除题目 "${row.title}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await questionApi.delete(row.id)
      ElMessage.success('删除成功')
      loadQuestions()
    } catch (e) {
      ElMessage.error(e.response?.data || '删除失败')
    }
  }).catch(() => {})
}

onMounted(loadQuestions)
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
