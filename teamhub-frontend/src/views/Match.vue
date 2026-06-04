<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="margin:0">比赛中心</h2>
      <el-button v-if="user.role === 'CAPTAIN'" type="primary" @click="showCreate = true">创建比赛</el-button>
    </div>

    <!-- 筛选 -->
    <el-radio-group v-model="filterType" style="margin-bottom:16px;display:flex;flex-wrap:nowrap;overflow-x:auto" @change="loadList">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="训练赛">训练赛</el-radio-button>
      <el-radio-button value="新生杯">新生杯</el-radio-button>
      <el-radio-button value="华工杯">华工杯</el-radio-button>
      <el-radio-button value="其他">其他</el-radio-button>
    </el-radio-group>

    <!-- 比赛列表 -->
    <el-card v-for="m in matches" :key="m.id" style="margin-bottom:12px;cursor:pointer" @click="goDetail(m.id)">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <div style="font-size:16px;font-weight:bold">vs {{ m.opponent }}</div>
          <div style="color:#888;margin-top:4px">{{ m.location }} · {{ formatTime(m.gameTime) }}</div>
        </div>
        <div style="display:flex;gap:8px;align-items:center">
          <el-tag>{{ m.gameType }}</el-tag>
          <el-tag :type="statusType(m.status)">{{ statusLabel(m.status) }}</el-tag>
        </div>
      </div>
    </el-card>

    <el-empty v-if="matches.length === 0" description="暂无比赛" />

    <!-- 创建比赛弹窗 -->
    <el-dialog v-model="showCreate" title="创建比赛" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="对手">
          <el-input v-model="form.opponent" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="form.gameTime" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="赛事类型">
          <el-select v-model="form.gameType" style="width:100%">
            <el-option label="训练赛" value="训练赛" />
            <el-option label="新生杯" value="新生杯" />
            <el-option label="华工杯" value="华工杯" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="首发名单">
          <el-input v-model="form.lineup" type="textarea" :rows="3" placeholder="每行一人，如：1号 张三 主攻" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="赛后总结、反思等" />
        </el-form-item>
        <el-form-item label="视频链接">
          <el-input v-model="form.videoUrl" placeholder="网盘/B站/抖音等分享链接" />
        </el-form-item>

        <!-- 动态比分 -->
        <el-form-item label="比分">
          <div v-for="(s, i) in form.sets" :key="i" style="display:flex;gap:8px;margin-bottom:8px;align-items:center">
            <span>第{{ i + 1 }}局</span>
            <el-input-number v-model="s.ourScore" :min="0" style="width:100px" />
            <span>:</span>
            <el-input-number v-model="s.oppScore" :min="0" style="width:100px" />
            <el-button type="danger" circle size="small" @click="removeSet(i)">-</el-button>
          </div>
          <el-button @click="addSet">+ 增加一局</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const matches = ref<any[]>([])
const showCreate = ref(false)
const filterType = ref('')
const user = JSON.parse(localStorage.getItem('user') || '{}')

const form = reactive({
  opponent: '',
  gameTime: '',
  location: '',
  gameType: '训练赛',
  lineup: '',
  notes: '',
  videoUrl: '',
  createdBy: null,
  sets: [{ ourScore: 0, oppScore: 0 }]
})

async function loadList() {
  const res = await request.get('/api/match/list', {
    params: { gameType: filterType.value }
  })
  matches.value = res.data
}

async function handleCreate() {
  const payload: any = { ...form }
  if (payload.gameTime instanceof Date) {
    const d = payload.gameTime as Date
    const pad = (n: number) => n.toString().padStart(2, '0')
    payload.gameTime = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
  await request.post('/api/match/create', payload)
  ElMessage.success('创建成功')
  showCreate.value = false
  loadList()
}

function addSet() {
  form.sets.push({ ourScore: 0, oppScore: 0 })
}

function removeSet(i: number) {
  form.sets.splice(i, 1)
}

function goDetail(id: number) {
  router.push(`/match/${id}`)
}

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

function statusLabel(s: string) {
  return { UPCOMING: '未开始', ONGOING: '进行中', FINISHED: '已结束' }[s] || s
}

function statusType(s: string) {
  return { UPCOMING: 'info', ONGOING: 'warning', FINISHED: 'success' }[s] || ''
}

onMounted(loadList)
</script>