<template>
    <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:#f5f5f5">
      <el-card style="width:400px">
        <h2 style="text-align:center;margin-bottom:24px">TeamHub 登录</h2>
        <el-form :model="form" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width:100%" @click="handleLogin">登录</el-button>
          </el-form-item>
          <el-form-item>
            <el-button link type="primary" style="width:100%" @click="goRegister">还没有账号？去注册</el-button>
          </el-form-item>
          <el-form-item>
            <el-button link type="info" style="width:100%" @click="goReset">忘记密码？</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup lang="ts">
  import { reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import request from '../utils/request'
  import { ElMessage } from 'element-plus'
  
  const router = useRouter()
  const form = reactive({ username: '', password: '' })
  
  async function handleLogin() {
    try {
      const res = await request.post('/api/user/login', form)
      const token = res.data?.token
      const user = res.data?.user
      
      if (!token || !user || !user.id) {
        ElMessage.error('登录响应异常，请稍后重试')
        return
      }
      
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      ElMessage.success('登录成功')
      router.push('/')
    } catch (e: any) {
      const msg = e.response?.data?.message || '登录失败，请稍后重试'
      ElMessage.error(msg)
    }
  }

  function goRegister() {
    router.push('/register')
  }

  function goReset() {
    router.push('/reset-password')
  }
  </script>
