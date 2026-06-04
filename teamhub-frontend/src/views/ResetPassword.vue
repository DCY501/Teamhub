<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:#f5f5f5">
    <el-card style="width:400px">
      <h2 style="text-align:center;margin-bottom:8px">找回密码</h2>
      <p style="text-align:center;color:#666;font-size:13px;margin-bottom:24px">联系队长获取当前邀请码</p>
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="你的登录用户名" />
        </el-form-item>
        <el-form-item label="邀请码">
          <el-input v-model="form.inviteCode" placeholder="队长提供的6位邀请码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" placeholder="设置新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" @click="handleReset">重置密码</el-button>
        </el-form-item>
        <el-form-item>
          <el-button link type="primary" style="width:100%" @click="goLogin">记得密码？去登录</el-button>
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
const form = reactive({ username: '', inviteCode: '', newPassword: '', confirmPassword: '' })

async function handleReset() {
  if (!form.username || !form.inviteCode || !form.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  try {
    const res = await request.post('/api/user/reset-password', null, {
      params: {
        username: form.username,
        inviteCode: form.inviteCode,
        newPassword: form.newPassword
      }
    })
    ElMessage.success(res.data.message)
    router.push('/login')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '重置失败')
  }
}

function goLogin() {
  router.push('/login')
}
</script>
