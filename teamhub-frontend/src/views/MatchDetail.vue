<template>
    <div v-if="game">
      <el-page-header @back="$router.back()" :content="`vs ${game.opponent}`" style="margin-bottom:16px" />

      <el-card style="margin-bottom:16px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="对手">{{ game.opponent }}</el-descriptions-item>
          <el-descriptions-item label="赛事类型">{{ game.gameType }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ formatTime(game.gameTime) }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ game.location }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(game.status)">{{ statusLabel(game.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="视频">
            <a v-if="game.videoUrl" :href="game.videoUrl" target="_blank">点击查看</a>
            <span v-else>暂无</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 比分 -->
      <el-card style="margin-bottom:16px">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>比分</span>
            <el-button link type="primary" @click="editSets">
              {{ editingSets ? '取消' : '编辑' }}
            </el-button>
          </div>
        </template>

        <div v-if="!editingSets">
          <el-table :data="sets" border>
            <el-table-column label="局数" prop="setNumber" width="80" />
            <el-table-column label="我方" prop="ourScore" />
            <el-table-column label="对方" prop="oppScore" />
            <el-table-column label="结果">
              <template #default="{ row }">
                <el-tag :type="row.ourScore > row.oppScore ? 'success' : 'danger'">
                  {{ row.ourScore > row.oppScore ? '胜' : '负' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else>
          <div v-for="(s, i) in setsDraft" :key="i" style="display:flex;gap:12px;align-items:center;margin-bottom:12px">
            <span style="width:60px">第{{ i + 1 }}局</span>
            <el-input-number v-model="s.ourScore" :min="0" style="width:120px" />
            <span>:</span>
            <el-input-number v-model="s.oppScore" :min="0" style="width:120px" />
            <el-button type="danger" circle size="small" @click="removeSet(i)">-</el-button>
          </div>
          <el-button @click="addSet">+ 增加一局</el-button>
          <el-button type="primary" @click="saveSets">保存比分</el-button>
        </div>
      </el-card>

      <!-- 首发名单 -->
      <el-card style="margin-bottom:16px">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>首发名单</span>
            <el-button link type="primary" @click="editLineup">
              {{ editingLineup ? '取消' : '编辑' }}
            </el-button>
          </div>
        </template>

        <div v-if="!editingLineup">
          <pre style="margin:0;white-space:pre-wrap">{{ game.lineup || '暂无' }}</pre>
        </div>
        <div v-else>
          <el-input v-model="lineupDraft" type="textarea" :rows="4" placeholder="每行一人，如：1号 张三 主攻" />
          <el-button type="primary" style="margin-top:12px" @click="saveLineup">保存</el-button>
        </div>
      </el-card>

      <!-- 备注 -->
      <el-card style="margin-bottom:16px">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>赛后备注</span>
            <el-button link type="primary" @click="editNotes">
              {{ editingNotes ? '取消' : '编辑' }}
            </el-button>
          </div>
        </template>

        <div v-if="!editingNotes">
          <p style="margin:0">{{ game.notes || '暂无' }}</p>
        </div>
        <div v-else>
          <el-input v-model="notesDraft" type="textarea" :rows="4" placeholder="填写赛后总结、反思等" />
          <el-button type="primary" style="margin-top:12px" @click="saveNotes">保存</el-button>
        </div>
      </el-card>

      <!-- 视频链接 -->
      <el-card style="margin-bottom:16px">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>视频链接</span>
            <el-button link type="primary" @click="editVideo">
              {{ editingVideo ? '取消' : '编辑' }}
            </el-button>
          </div>
        </template>

        <div v-if="!editingVideo">
          <a v-if="game.videoUrl" :href="game.videoUrl" target="_blank">{{ game.videoUrl }}</a>
          <span v-else>暂无</span>
        </div>
        <div v-else>
          <el-input v-model="videoDraft" placeholder="网盘/B站/抖音等分享链接" />
          <el-button type="primary" style="margin-top:12px" @click="saveVideo">保存</el-button>
        </div>
      </el-card>

      <!-- 队长操作 -->
      <el-card v-if="user?.role === 'CAPTAIN'" header="状态管理">
        <el-button @click="updateStatus('ONGOING')" :disabled="game.status === 'FINISHED'">设为进行中</el-button>
        <el-button type="success" @click="updateStatus('FINISHED')" :disabled="game.status === 'FINISHED'">设为已结束</el-button>
      </el-card>
    </div>
  </template>

  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import request from '../utils/request'
  import { ElMessage } from 'element-plus'

  const route = useRoute()
  const id = route.params.id
  const game = ref<any>(null)
  const sets = ref<any[]>([])
  const editingNotes = ref(false)
  const notesDraft = ref('')
  const editingSets = ref(false)
  const setsDraft = ref<any[]>([])
  const editingLineup = ref(false)
  const lineupDraft = ref('')
  const editingVideo = ref(false)
  const videoDraft = ref('')
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  async function load() {
    const res = await request.get(`/api/match/${id}`)
    game.value = res.data.game
    sets.value = res.data.sets
  }

  async function updateStatus(status: string) {
    await request.put(`/api/match/${id}/status`, null, { params: { status } })
    ElMessage.success('更新成功')
    load()
  }

  function editNotes() {
    if (editingNotes.value) {
      editingNotes.value = false
      notesDraft.value = ''
    } else {
      notesDraft.value = game.value?.notes || ''
      editingNotes.value = true
    }
  }

  async function saveNotes() {
    await request.put(`/api/match/${id}/notes`, { notes: notesDraft.value })
    ElMessage.success('备注保存成功')
    editingNotes.value = false
    load()
  }

  function editSets() {
    if (editingSets.value) {
      editingSets.value = false
      setsDraft.value = []
    } else {
      setsDraft.value = sets.value.map(s => ({ ...s }))
      editingSets.value = true
    }
  }

  function addSet() {
    setsDraft.value.push({ ourScore: 0, oppScore: 0 })
  }

  function removeSet(i: number) {
    setsDraft.value.splice(i, 1)
  }

  async function saveSets() {
    await request.put(`/api/match/${id}/sets`, setsDraft.value)
    ElMessage.success('比分保存成功')
    editingSets.value = false
    load()
  }

  function editLineup() {
    if (editingLineup.value) {
      editingLineup.value = false
      lineupDraft.value = ''
    } else {
      lineupDraft.value = game.value?.lineup || ''
      editingLineup.value = true
    }
  }

  async function saveLineup() {
    await request.put(`/api/match/${id}/lineup`, { lineup: lineupDraft.value })
    ElMessage.success('首发名单保存成功')
    editingLineup.value = false
    load()
  }

  function editVideo() {
    if (editingVideo.value) {
      editingVideo.value = false
      videoDraft.value = ''
    } else {
      videoDraft.value = game.value?.videoUrl || ''
      editingVideo.value = true
    }
  }

  async function saveVideo() {
    await request.put(`/api/match/${id}/video`, { videoUrl: videoDraft.value })
    ElMessage.success('视频链接保存成功')
    editingVideo.value = false
    load()
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

  onMounted(load)
  </script>
