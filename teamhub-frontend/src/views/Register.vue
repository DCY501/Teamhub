<template>
  <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;background:#f5f5f5;padding:20px">
    <el-card style="width:480px">
      <h2 style="text-align:center;margin-bottom:24px">TeamHub 注册</h2>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="🏆 创建球队" name="captain">
          <el-form ref="captainFormRef" :model="captainForm" :rules="captainRules" label-width="100px">
            <el-form-item label="球队名称" prop="teamName">
              <el-input v-model="captainForm.teamName" placeholder="例如：计算机院排球队" />
            </el-form-item>
            <el-form-item label="用户名" prop="username">
              <el-input v-model="captainForm.username" placeholder="3-20位字母或数字" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="captainForm.password" type="password" placeholder="至少6位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="captainForm.confirmPassword" type="password" />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="captainForm.name" placeholder="真实姓名" />
            </el-form-item>
            <el-form-item label="学历" prop="degree">
              <el-select v-model="captainForm.degree" style="width:100%" @change="onDegreeChange(captainForm)">
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
            <el-form-item label="当前年级" prop="grade">
              <el-select v-model="captainForm.grade" style="width:100%">
                <el-option v-for="g in gradeOptions(captainForm.degree)" :key="g.value" :label="g.label" :value="g.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="号码" prop="jerseyNumber">
              <el-input v-model="captainForm.jerseyNumber" type="number" placeholder="球衣号码（选填）" />
            </el-form-item>
            <el-form-item label="位置" prop="position">
              <el-input v-model="captainForm.position" placeholder="例如：主攻 / 守门员（选填）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width:100%" @click="handleCaptainRegister">创建球队并注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="🙋 加入球队" name="member">
          <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="100px">
            <el-form-item label="邀请码" prop="inviteCode">
              <el-input v-model="memberForm.inviteCode" placeholder="6位字母数字" maxlength="6" />
            </el-form-item>
            <el-form-item label="用户名" prop="username">
              <el-input v-model="memberForm.username" placeholder="3-20位字母或数字" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="memberForm.password" type="password" placeholder="至少6位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="memberForm.confirmPassword" type="password" />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="memberForm.name" placeholder="真实姓名" />
            </el-form-item>
            <el-form-item label="学历" prop="degree">
              <el-select v-model="memberForm.degree" style="width:100%" @change="onDegreeChange(memberForm)">
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
            <el-form-item label="当前年级" prop="grade">
              <el-select v-model="memberForm.grade" style="width:100%">
                <el-option v-for="g in gradeOptions(memberForm.degree)" :key="g.value" :label="g.label" :value="g.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="号码" prop="jerseyNumber">
              <el-input v-model="memberForm.jerseyNumber" type="number" placeholder="球衣号码（选填）" />
            </el-form-item>
            <el-form-item label="位置" prop="position">
              <el-input v-model="memberForm.position" placeholder="例如：主攻 / 守门员（选填）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" style="width:100%" @click="handleMemberRegister">加入球队</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div style="text-align:center;margin-top:16px">
        <el-link type="primary" @click="goLogin">已有账号？去登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const activeTab = ref('captain')
const captainFormRef = ref<FormInstance>()
const memberFormRef = ref<FormInstance>()

const usernameRule = { required: true, pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '用户名需为3-20位字母、数字或下划线', trigger: 'blur' }
const passwordRule = { required: true, min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
const nameRule = { required: true, pattern: /^[\u4e00-\u9fa5a-zA-Z\s]{2,20}$/, message: '姓名需为2-20位中文或英文', trigger: 'blur' }
const jerseyRule = { pattern: /^[1-9]\d{0,2}$/, message: '号码需为1-999之间的正整数', trigger: 'blur' }

const captainForm = reactive({
  teamName: '',
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  degree: '本科',
  grade: 1,
  jerseyNumber: '',
  position: ''
})

const memberForm = reactive({
  inviteCode: '',
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  degree: '本科',
  grade: 1,
  jerseyNumber: '',
  position: ''
})

function gradeOptions(degree: string) {
  if (degree === '硕士') return [{ label: '研一', value: 1 }, { label: '研二', value: 2 }, { label: '研三', value: 3 }]
  if (degree === '博士') return [{ label: '博一', value: 1 }, { label: '博二', value: 2 }, { label: '博三', value: 3 }]
  return [{ label: '大一', value: 1 }, { label: '大二', value: 2 }, { label: '大三', value: 3 }, { label: '大四', value: 4 }]
}

function onDegreeChange(form: any) {
  const max = form.degree === '本科' ? 4 : 3
  if (form.grade > max) form.grade = 1
}

function calcEntryYear(gradeNum: number) {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  if (month >= 9) {
    return year - gradeNum + 1
  } else {
    return year - gradeNum
  }
}

function confirmPasswordRule(form: any) {
  return {
    required: true,
    validator: (_rule: any, value: string, callback: any) => {
      if (!value) callback(new Error('请再次输入密码'))
      else if (value !== form.password) callback(new Error('两次输入的密码不一致'))
      else callback()
    },
    trigger: 'blur'
  }
}

const captainRules: FormRules = {
  teamName: { required: true, min: 2, max: 50, message: '球队名称需为2-50个字符', trigger: 'blur' },
  username: usernameRule,
  password: passwordRule,
  confirmPassword: confirmPasswordRule(captainForm),
  name: nameRule,
  degree: { required: true, message: '请选择学历', trigger: 'change' },
  grade: { required: true, message: '请选择年级', trigger: 'change' },
  jerseyNumber: jerseyRule
}

const memberRules: FormRules = {
  inviteCode: { required: true, pattern: /^[A-Z0-9]{6}$/, message: '邀请码为6位大写字母或数字', trigger: 'blur' },
  username: usernameRule,
  password: passwordRule,
  confirmPassword: confirmPasswordRule(memberForm),
  name: nameRule,
  degree: { required: true, message: '请选择学历', trigger: 'change' },
  grade: { required: true, message: '请选择年级', trigger: 'change' },
  jerseyNumber: jerseyRule
}

function buildPayload(form: any) {
  return {
    username: form.username,
    password: form.password,
    name: form.name,
    degree: form.degree,
    entryYear: calcEntryYear(form.grade),
    jerseyNumber: form.jerseyNumber ? Number(form.jerseyNumber) : null,
    position: form.position || null
  }
}

async function handleCaptainRegister() {
  const valid = await captainFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    const res = await request.post('/api/user/register', {
      ...buildPayload(captainForm),
      teamName: captainForm.teamName.trim()
    })
    ElMessage.success(`注册成功！你的邀请码是 ${res.data.inviteCode}（7天内有效）`)
    setTimeout(() => router.push('/login'), 2500)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '注册失败')
  }
}

async function handleMemberRegister() {
  const valid = await memberFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await request.post('/api/user/register', {
      ...buildPayload(memberForm),
      inviteCode: memberForm.inviteCode.trim().toUpperCase()
    })
    ElMessage.success('注册成功！')
    router.push('/login')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '邀请码无效或已过期')
  }
}

function goLogin() {
  router.push('/login')
}
</script>
