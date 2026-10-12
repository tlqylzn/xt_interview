<template>
  <div class="user-management">
    <div class="page-header">
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">
        添加用户
      </el-button>
      <el-button :icon="Refresh" @click="loadUsers">刷新</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.username"
        placeholder="请输入用户名"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="searchForm.status"
        placeholder="全部状态"
        clearable
        style="width: 140px"
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-select
        v-model="searchForm.roleLevel"
        placeholder="全部权限级别"
        clearable
        style="width: 180px"
      >
        <el-option label="用户权限" :value="1" />
        <el-option label="管理员权限" :value="2" />
        <el-option label="超级管理员权限" :value="3" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="users"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="phone" label="联系方式" width="140" />
      <el-table-column label="所属部门" width="140">
        <template #default="{ row }">
          {{ row.departmentName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="权限级别" width="120">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.roleLevel)">
            {{ roleText(row.roleLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link :icon="Edit" @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="query.page"
      :page-size="query.size"
      :total="query.total"
      :page-sizes="[5, 10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="formData.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系方式" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="所属部门" prop="departmentId">
          <el-select
            v-model="formData.departmentId"
            placeholder="请选择所属部门（可不选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            show-password
            :placeholder="isEdit ? '留空则不修改密码' : '留空则默认123456'"
          />
        </el-form-item>
        <el-form-item label="权限级别" prop="roleLevel">
          <el-select v-model="formData.roleLevel" placeholder="请选择权限级别" style="width: 100%">
            <el-option label="用户权限" :value="1" />
            <el-option label="管理员权限" :value="2" />
            <el-option label="超级管理员权限" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { accountApi, departmentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)

const users = ref<any[]>([])
const departments = ref<any[]>([])

const formRef = ref<FormInstance>()

// 搜索条件。用 undefined 而不是空字符串表示"未选择"，
// axios 会自动把 undefined 的参数从 query string 里丢掉，后端收到 null 即不筛选。
const searchForm = reactive({
  username: '',
  status: undefined as number | undefined,
  roleLevel: undefined as number | undefined
})

// 分页状态。page 是给 el-pagination 用的，从 1 开始；调接口时再减 1 转成后端的 0-based。
const query = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  username: '',
  name: '',
  gender: '男',
  phone: '',
  departmentId: null as number | null,
  password: '',
  roleLevel: 1,
  status: 1,
  isSuper: 0
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleLevel: [{ required: true, message: '请选择权限级别', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function roleText(level: number): string {
  const map: Record<number, string> = { 1: '用户权限', 2: '管理员权限', 3: '超级管理员' }
  return map[level] || '未知'
}

function roleTagType(level: number): 'info' | 'warning' | 'danger' {
  const map: Record<number, 'info' | 'warning' | 'danger'> = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return map[level] || 'info'
}

async function loadUsers() {
  loading.value = true
  try {
    const res: any = await accountApi.page({
      username: searchForm.username || undefined,
      status: searchForm.status,
      roleLevel: searchForm.roleLevel,
      page: query.page - 1,
      size: query.size
    })
    users.value = res.list
    query.total = res.totalElements
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索条件变了，回到第一页重新查，否则可能停在一个越界的页码上
function handleSearch() {
  query.page = 1
  loadUsers()
}

function handleReset() {
  searchForm.username = ''
  searchForm.status = undefined
  searchForm.roleLevel = undefined
  query.page = 1
  loadUsers()
}

function handlePageChange(page: number) {
  query.page = page
  loadUsers()
}

// 改每页条数后总页数会变，同样回到第一页
function handleSizeChange(size: number) {
  query.size = size
  query.page = 1
  loadUsers()
}

async function loadDepartments() {
  try {
    const res: any = await departmentApi.getAll()
    departments.value = res
  } catch (error: any) {
    ElMessage.error(error.message || '加载部门列表失败')
  }
}

function resetForm() {
  formData.username = ''
  formData.name = ''
  formData.gender = '男'
  formData.phone = ''
  formData.departmentId = null
  formData.password = ''
  formData.roleLevel = 1
  formData.status = 1
  formData.isSuper = 0
  isEdit.value = false
  editId.value = 0
  formRef.value?.resetFields()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  resetForm()
  isEdit.value = true
  editId.value = row.id
  formData.username = row.username
  formData.name = row.name
  formData.gender = row.gender || '男'
  formData.phone = row.phone || ''
  formData.departmentId = row.departmentId ?? null
  formData.roleLevel = row.roleLevel
  formData.status = row.status
  formData.isSuper = row.isSuper
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = { ...formData }
      if (data.password === '') {
        delete data.password
      }

      if (isEdit.value) {
        await accountApi.update(editId.value, data)
        ElMessage.success('用户更新成功')
      } else {
        await accountApi.create(data)
        ElMessage.success('用户创建成功')
        // 列表按 id 倒序，新建的用户在第一页
        query.page = 1
      }
      dialogVisible.value = false
      await loadUsers()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await accountApi.delete(row.id)
    ElMessage.success('用户删除成功')
    // 删的是当前页最后一条时往前退一页，否则会停在一个空页上
    if (users.value.length === 1 && query.page > 1) {
      query.page -= 1
    }
    await loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadUsers()
  loadDepartments()
})
</script>

<style scoped>
.user-management {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
