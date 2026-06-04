<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="margin:0">球员档案</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card" @tab-change="loadList">
      <el-tab-pane label="当打之年" name="ACTIVE">
        <el-row :gutter="16">
          <el-col v-for="p in players" :key="p.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom:16px">
            <el-card style="cursor:pointer" @click="goDetail(p.id)">
              <div style="text-align:center">
                <el-avatar :size="64" :src="p.photoUrl || defaultAvatar" />
                <div style="margin-top:8px;font-weight:bold;font-size:16px">{{ p.name }}</div>
                <div style="color:#666;font-size:14px">
                  <span v-if="p.jerseyNumber">#{{ p.jerseyNumber }}</span>
                  <span v-if="p.position"> · {{ p.position }}</span>
                </div>
                <el-tag size="small" style="margin-top:8px">{{ calcGrade(p.entryYear, p.degree) }}</el-tag>
                <el-tag v-if="p.role === 'CAPTAIN'" type="danger" size="small" style="margin-top:8px;margin-left:4px">队长</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="players.length === 0" description="暂无球员" />
      </el-tab-pane>

      <el-tab-pane label="功勋人物" name="ALUMNI">
        <el-row :gutter="16">
          <el-col v-for="p in alumni" :key="p.id" :xs="24" :sm="12" :md="8" :lg="6" style="margin-bottom:16px">
            <el-card style="cursor:pointer" @click="goDetail(p.id)">
              <div style="text-align:center">
                <el-avatar :size="64" :src="p.photoUrl || defaultAvatar" />
                <div style="margin-top:8px;font-weight:bold;font-size:16px">{{ p.name }}</div>
                <div style="color:#666;font-size:14px">
                  <span v-if="p.jerseyNumber">#{{ p.jerseyNumber }}</span>
                  <span v-if="p.position"> · {{ p.position }}</span>
                </div>
                <el-tag type="info" size="small" style="margin-top:8px">{{ calcGrade(p.entryYear, p.degree) }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="alumni.length === 0" description="暂无功勋人物" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const activeTab = ref('ACTIVE')
const players = ref<any[]>([])
const alumni = ref<any[]>([])
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

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

async function loadList() {
  try {
    const [activeRes, alumniRes] = await Promise.all([
      request.get('/api/player/list', { params: { status: 'ACTIVE' } }),
      request.get('/api/player/list', { params: { status: 'ALUMNI' } })
    ])
    players.value = activeRes.data
    alumni.value = alumniRes.data
  } catch (e: any) {
    // ignore
  }
}

function goDetail(id: number) {
  router.push(`/player/${id}`)
}

onMounted(loadList)
</script>
