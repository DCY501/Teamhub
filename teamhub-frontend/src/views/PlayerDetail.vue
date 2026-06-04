<template>
  <div v-if="player">
    <el-page-header @back="$router.back()" :content="player.name" style="margin-bottom:16px" />

    <el-card style="max-width:600px">
      <div style="display:flex;gap:20px;align-items:flex-start">
        <el-avatar :size="80" :src="player.photoUrl || defaultAvatar" />
        <div>
          <div style="font-size:20px;font-weight:bold">{{ player.name }}</div>
          <div style="color:#666;margin-top:4px">
            <span v-if="player.jerseyNumber">#{{ player.jerseyNumber }}</span>
            <span v-if="player.position"> · {{ player.position }}</span>
          </div>
          <el-tag style="margin-top:8px">{{ calcGrade(player.entryYear, player.degree) }}</el-tag>
          <el-tag v-if="player.role === 'CAPTAIN'" type="danger" style="margin-top:8px;margin-left:8px">队长</el-tag>
          <el-tag v-if="player.status === 'ALUMNI'" type="info" style="margin-top:8px;margin-left:8px">功勋人物</el-tag>
        </div>
      </div>

      <el-descriptions :column="1" border style="margin-top:16px">
        <el-descriptions-item label="学历">{{ player.degree }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ player.entryYear }}</el-descriptions-item>
        <el-descriptions-item label="号码">{{ player.jerseyNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ player.position || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top:16px;display:flex;gap:12px">
        <el-button v-if="canEdit" type="primary" @click="goEdit">编辑资料</el-button>
        <el-button v-if="isCaptain && player.status === 'ACTIVE'" type="danger" @click="handleGraduate">毕业迁移</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const player = ref<any>(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const currentUser = JSON.parse(localStorage.getItem('user') || '{}')

const isCaptain = computed(() => currentUser.role === 'CAPTAIN')
const canEdit = computed(() => {
  if (!player.value) return false
  return isCaptain.value || player.value.userId === currentUser.id
})

function calcGrade(entryYear: number, degree: string) {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  let grade = year - entryYear
  if (month >= 9) grade += 1
  if (grade <= 0) return '预科'
  const max = { '本科': 4, '硕士': 3, '博士': 3 }[degree] || 4
  if (grade > max) return `已毕业（${degree}）`
  const prefix = { '本科': '大', '硕士': '研', '博士': '博' }[degree] || '大'
  return `${prefix}${grade}`
}

async function load() {
  const res = await request.get(`/api/player/${id}`)
  player.value = res.data
}

function goEdit() {
  router.push(`/player/${id}/edit`)
}

async function handleGraduate() {
  try {
    await ElMessageBox.confirm('确定将该球员迁移至功勋人物？', '确认', { type: 'warning' })
    await request.put(`/api/player/${id}/graduate`)
    ElMessage.success('迁移成功')
    load()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

onMounted(load)
</script>
