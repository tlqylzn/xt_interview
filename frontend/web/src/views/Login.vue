<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="logo-title">欣铁 面试项目</h1>
      <h2 class="login-subtitle">用户登录</h2>

      <el-form ref="formRef" :model="loginForm" :rules="rules" class="login-form" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        <p>默认账号：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const result: any = await authApi.login({
        username: loginForm.username,
        password: loginForm.password
      })

      if (result.success) {
        userStore.setToken(result.token)
        userStore.setUserInfo(result.user)
        localStorage.setItem('currentUser', JSON.stringify(result.user))
        ElMessage.success('登录成功')

        const redirect = route.query.redirect as string
        router.push(redirect || '/users')
      } else {
        ElMessage.error(result.message || '登录失败')
      }
    } catch (error: any) {
      if (error && error.message) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error('网络异常，请稍后重试')
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #1e3c72 100%);
}

.login-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(30, 60, 114, 0.3);
  padding: 50px 40px;
  width: 100%;
  max-width: 420px;
}

.logo-title {
  text-align: center;
  color: #1e3c72;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
}

.login-subtitle {
  text-align: center;
  color: #64748b;
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 30px;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
}

.login-tip {
  text-align: center;
  margin-top: 20px;
  color: #94a3b8;
  font-size: 13px;
}
</style>
