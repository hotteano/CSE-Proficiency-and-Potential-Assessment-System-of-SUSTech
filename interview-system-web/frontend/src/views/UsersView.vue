<template>
  <div class="users">
    <h2>用户管理</h2>
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名"
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <el-table :data="filteredUsers" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="active" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'danger'">
              {{ row.active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
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

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑用户"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="考官" value="EXAMINER" />
            <el-option label="考生" value="CANDIDATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.active"
            active-text="启用"
            inactive-text="禁用"
          />
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
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const users = ref([])
const loading = ref(false)
const searchQuery = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  id: null,
  username: '',
  realName: '',
  role: 'CANDIDATE',
  active: true
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleMap = {
  'ADMIN': '管理员',
  'EXAMINER': '考官',
  'CANDIDATE': '考生',
  'TEST_SETTER': '出题人'
}

const getRoleLabel = (role) => roleMap[role] || role

const getRoleType = (role) => {
  const map = {
    'ADMIN': 'danger',
    'EXAMINER': 'warning',
    'CANDIDATE': 'success',
    'TEST_SETTER': 'info'
  }
  return map[role] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter(user => 
    user.username?.toLowerCase().includes(query) ||
    user.realName?.toLowerCase().includes(query)
  )
})

const handleSearch = () => {
  // 搜索通过 computed 自动处理
}

const loadUsers = async () => {
  loading.value = true
  try {
    users.value = await userApi.getAll()
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const edit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await userApi.update(form.value.id, form.value)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadUsers()
  } catch (e) {
    ElMessage.error(e.response?.data || '更新失败')
  } finally {
    submitting.value = false
  }
}

const remove = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await userApi.delete(row.id)
      ElMessage.success('删除成功')
      loadUsers()
    } catch (e) {
      ElMessage.error(e.response?.data || '删除失败')
    }
  }).catch(() => {})
}

onMounted(loadUsers)
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
