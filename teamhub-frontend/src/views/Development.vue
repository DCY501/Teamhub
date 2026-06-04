<template>
  <div>
    <h2 style="margin-top:0">发展中心</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== 个人发展 ========== -->
      <el-tab-pane label="个人发展" name="personal">
        <!-- 球员自评 -->
        <el-card header="球员自评" style="margin-bottom:16px">
          <el-form :model="selfReviewForm" label-width="100px">
            <el-form-item label="分类" required>
              <el-select v-model="selfReviewForm.category" style="width:100%">
                <el-option label="技术" value="技术" />
                <el-option label="体能" value="体能" />
                <el-option label="意识" value="意识" />
                <el-option label="态度" value="态度" />
                <el-option label="配合" value="配合" />
              </el-select>
            </el-form-item>
            <el-form-item label="当前问题" required>
              <el-input v-model="selfReviewForm.currentProblem" type="textarea" :rows="2" placeholder="必填，描述你当前的问题" />
            </el-form-item>
            <el-form-item label="改进目标" required>
              <el-input v-model="selfReviewForm.improvementGoal" type="textarea" :rows="2" placeholder="必填，你计划如何改进" />
            </el-form-item>
            <el-form-item label="期望训练">
              <el-input v-model="selfReviewForm.expectedTraining" type="textarea" :rows="2" placeholder="选填，希望球队安排什么训练内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitSelfReview">提交自评</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 历史自评 -->
        <el-card header="历史自评" style="margin-bottom:16px">
          <el-timeline v-if="selfReviews.length > 0">
            <el-timeline-item v-for="r in selfReviews" :key="r.id" :timestamp="formatTime(r.createdAt)">
              <el-tag size="small">{{ r.category }}</el-tag>
              <p style="margin-top:4px"><strong style="color:#409EFF">当前问题：</strong>{{ r.currentProblem }}</p>
              <p style="margin-top:4px"><strong style="color:#67C23A">改进目标：</strong>{{ r.improvementGoal }}</p>
              <p v-if="r.expectedTraining" style="margin-top:4px"><strong style="color:#E6A23C">期望训练：</strong>{{ r.expectedTraining }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无自评记录" />
        </el-card>

        <!-- 队友给我的建议 -->
        <el-card header="队友给我的建议">
          <div v-if="playerSuggestions.length > 0">
            <div v-for="s in playerSuggestions" :key="s.id" style="margin-bottom:12px;padding-bottom:12px;border-bottom:1px solid #eee">
              <div style="display:flex;justify-content:space-between;align-items:center">
                <el-tag size="small">{{ s.category }}</el-tag>
                <span style="color:#999;font-size:12px">{{ s.isAnonymous ? '匿名队友' : '队友' }} · {{ formatTime(s.createdAt) }}</span>
              </div>
              <p style="margin:8px 0">{{ s.content }}</p>
              <div style="display:flex;gap:12px;align-items:center">
                <el-button :type="s.likedByMe ? 'primary' : 'link'" size="small" @click="likePlayerSuggestion(s)">
                  👍 {{ s.likes }}
                </el-button>
                <el-select v-model="s.status" size="small" style="width:120px" @change="updatePlayerSuggestionStatus(s.id, s.status)">
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="已采纳" value="ADOPTED" />
                  <el-option label="尝试中" value="TRYING" />
                  <el-option label="暂不适用" value="SKIP" />
                </el-select>
                <el-button link type="danger" size="small" @click="reportPlayerSuggestion(s.id)">举报</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无建议" />
        </el-card>
      </el-tab-pane>

      <!-- ========== 全队动态 ========== -->
      <el-tab-pane label="全队自评" name="teamOverview">
        <el-row :gutter="16">
          <el-col v-for="item in allSelfReviews" :key="item.player.id" :xs="24" :sm="12" :md="8" style="margin-bottom:16px">
            <el-card style="cursor:pointer;height:100%" @click="openPlayerDetail(item)">
              <div style="display:flex;gap:12px;align-items:center;margin-bottom:12px">
                <el-avatar :size="48" :src="item.player.photoUrl || defaultAvatar" />
                <div>
                  <div style="font-weight:bold">{{ item.player.name }}</div>
                  <div style="color:#666;font-size:12px">
                    <span v-if="item.player.jerseyNumber">#{{ item.player.jerseyNumber }}</span>
                    <span v-if="item.player.position"> · {{ item.player.position }}</span>
                  </div>
                </div>
              </div>
              <div v-if="item.latestReview">
                <el-tag size="small">{{ item.latestReview.category }}</el-tag>
                <p style="margin-top:8px;color:#666;font-size:14px;overflow:hidden;text-overflow:ellipsis;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">
                  <strong>当前问题：</strong>{{ item.latestReview.currentProblem }}
                </p>
              </div>
              <div v-else style="color:#999;font-size:14px">暂无自评</div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="allSelfReviews.length === 0" description="暂无数据" />
      </el-tab-pane>

      <!-- ========== 球队建议 ========== -->
      <el-tab-pane label="球队建议" name="team">
        <div style="display:flex;justify-content:space-between;margin-bottom:16px">
          <div>
            <el-select v-model="teamFilter.category" placeholder="分类" clearable style="width:140px;margin-right:8px" @change="loadTeamSuggestions">
              <el-option label="训练内容" value="训练内容" />
              <el-option label="时间安排" value="时间安排" />
              <el-option label="战术方向" value="战术方向" />
              <el-option label="装备后勤" value="装备后勤" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-select v-model="teamFilter.status" placeholder="状态" clearable style="width:140px" @change="loadTeamSuggestions">
              <el-option label="已阅待议" value="PENDING" />
              <el-option label="采纳" value="ADOPTED" />
              <el-option label="暂不采纳" value="REJECTED" />
              <el-option label="已落实" value="DONE" />
            </el-select>
          </div>
          <el-button type="primary" @click="showTeamSuggestionForm = true">提交建议</el-button>
        </div>

        <el-card v-for="s in teamSuggestions" :key="s.id" style="margin-bottom:12px">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <div>
              <el-tag>{{ s.category }}</el-tag>
              <el-tag v-if="s.urgency === 'URGENT'" type="danger" style="margin-left:8px">紧急</el-tag>
              <el-tag :type="teamStatusType(s.status)" style="margin-left:8px">{{ teamStatusLabel(s.status) }}</el-tag>
            </div>
            <span style="color:#999;font-size:12px">{{ formatTime(s.createdAt) }}</span>
          </div>
          <p style="margin:12px 0">{{ s.content }}</p>
          <div style="display:flex;gap:12px;align-items:center">
            <el-button :type="s.likedByMe ? 'primary' : 'link'" size="small" @click="likeTeamSuggestion(s)">
              👍 {{ s.likes }}
            </el-button>
          </div>
          <div v-if="s.captainReply" style="margin-top:8px;padding:10px;background:#f5f5f5;border-radius:4px">
            <strong>队长回应：</strong>{{ s.captainReply }}
          </div>
          <div v-if="isCaptain && s.status === 'PENDING'" style="margin-top:12px;display:flex;gap:8px;align-items:center">
            <el-input v-model="s.replyDraft" placeholder="回应理由" size="small" style="width:240px" />
            <el-select v-model="s.statusDraft" size="small" style="width:120px">
              <el-option label="采纳" value="ADOPTED" />
              <el-option label="暂不采纳" value="REJECTED" />
              <el-option label="已落实" value="DONE" />
            </el-select>
            <el-button size="small" type="primary" @click="replyTeamSuggestion(s)">回应</el-button>
          </div>
        </el-card>
        <el-empty v-if="teamSuggestions.length === 0" description="暂无球队建议" />
      </el-tab-pane>
    </el-tabs>

    <!-- 队员详情弹窗（自评 + 建议 + 给建议） -->
    <el-dialog v-model="showPlayerDetail" :title="selectedPlayer?.name" width="700px">
      <div v-if="selectedPlayer">
        <div style="display:flex;gap:12px;align-items:center;margin-bottom:16px">
          <el-avatar :size="64" :src="selectedPlayer.photoUrl || defaultAvatar" />
          <div>
            <div style="font-size:18px;font-weight:bold">{{ selectedPlayer.name }}</div>
            <div style="color:#666">
              <span v-if="selectedPlayer.jerseyNumber">#{{ selectedPlayer.jerseyNumber }}</span>
              <span v-if="selectedPlayer.position"> · {{ selectedPlayer.position }}</span>
            </div>
          </div>
        </div>

        <el-tabs v-model="detailTab">
          <el-tab-pane label="自评历史" name="reviews">
            <el-timeline v-if="selectedReviews.length > 0">
              <el-timeline-item v-for="r in selectedReviews" :key="r.id" :timestamp="formatTime(r.createdAt)">
                <el-tag size="small">{{ r.category }}</el-tag>
                <p style="margin-top:4px"><strong style="color:#409EFF">当前问题：</strong>{{ r.currentProblem }}</p>
                <p style="margin-top:4px"><strong style="color:#67C23A">改进目标：</strong>{{ r.improvementGoal }}</p>
                <p v-if="r.expectedTraining" style="margin-top:4px"><strong style="color:#E6A23C">期望训练：</strong>{{ r.expectedTraining }}</p>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无自评记录" />
          </el-tab-pane>

          <el-tab-pane label="队友建议" name="suggestions">
            <div v-if="selectedSuggestions.length > 0">
              <div v-for="s in selectedSuggestions" :key="s.id" style="margin-bottom:12px;padding-bottom:12px;border-bottom:1px solid #eee">
                <div style="display:flex;justify-content:space-between;align-items:center">
                  <el-tag size="small">{{ s.category }}</el-tag>
                  <span style="color:#999;font-size:12px">{{ s.isAnonymous ? '匿名队友' : '队友' }} · {{ formatTime(s.createdAt) }}</span>
                </div>
                <p style="margin:8px 0">{{ s.content }}</p>
                <div style="display:flex;gap:12px;align-items:center">
                  <el-button :type="s.likedByMe ? 'primary' : 'link'" size="small" @click="likePlayerSuggestion(s)">
                    👍 {{ s.likes }}
                  </el-button>
                  <el-tag v-if="s.status !== 'PENDING'" size="small" :type="s.status === 'ADOPTED' ? 'success' : s.status === 'TRYING' ? 'warning' : 'info'">
                    {{ { PENDING: '待处理', ADOPTED: '已采纳', TRYING: '尝试中', SKIP: '暂不适用' }[s.status as 'PENDING' | 'ADOPTED' | 'TRYING' | 'SKIP'] }}
                  </el-tag>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无建议" />
          </el-tab-pane>

          <el-tab-pane v-if="selectedPlayer.userId !== currentUser.id" label="给TA提建议" name="give">
            <el-form :model="playerSuggestionForm" label-width="80px">
              <el-form-item label="分类" required>
                <el-select v-model="playerSuggestionForm.category" style="width:100%">
                  <el-option label="技术" value="技术" />
                  <el-option label="体能" value="体能" />
                  <el-option label="意识" value="意识" />
                  <el-option label="态度" value="态度" />
                  <el-option label="配合" value="配合" />
                </el-select>
              </el-form-item>
              <el-form-item label="内容" required>
                <el-input v-model="playerSuggestionForm.content" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="限200字，具体指出问题" />
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="playerSuggestionForm.isAnonymous">匿名提交</el-checkbox>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitPlayerSuggestion">提交建议</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 提交球队建议弹窗 -->
    <el-dialog v-model="showTeamSuggestionForm" title="提交球队建议" width="500px">
      <el-form :model="teamSuggestionForm" label-width="90px">
        <el-form-item label="分类" required>
          <el-select v-model="teamSuggestionForm.category" style="width:100%">
            <el-option label="训练内容" value="训练内容" />
            <el-option label="时间安排" value="时间安排" />
            <el-option label="战术方向" value="战术方向" />
            <el-option label="装备后勤" value="装备后勤" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="teamSuggestionForm.urgency">
            <el-radio value="NORMAL">普通</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="teamSuggestionForm.content" type="textarea" :rows="4" placeholder="请详细描述你的建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTeamSuggestionForm = false">取消</el-button>
        <el-button type="primary" @click="submitTeamSuggestion">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('personal')
const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const isCaptain = currentUser.role === 'CAPTAIN'
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// ========== 自评 ==========
const selfReviewForm = reactive({ category: '技术', currentProblem: '', improvementGoal: '', expectedTraining: '' })
const selfReviews = ref<any[]>([])

async function loadSelfReviews() {
  const res = await request.get('/api/development/self-review/list')
  selfReviews.value = res.data
}

async function submitSelfReview() {
  if (!selfReviewForm.currentProblem || !selfReviewForm.improvementGoal) {
    ElMessage.warning('当前问题和改进目标为必填项')
    return
  }
  await request.post('/api/development/self-review', { ...selfReviewForm })
  ElMessage.success('自评提交成功')
  selfReviewForm.currentProblem = ''
  selfReviewForm.improvementGoal = ''
  selfReviewForm.expectedTraining = ''
  loadSelfReviews()
}

// ========== 队友给我的建议 ==========
const playerSuggestions = ref<any[]>([])

async function loadPlayerSuggestions() {
  const res = await request.get('/api/development/player-suggestion/list', { params: { targetUserId: currentUser.id } })
  playerSuggestions.value = res.data
}

async function likePlayerSuggestion(s: any) {
  await request.post(`/api/development/player-suggestion/${s.id}/like`)
  s.likedByMe = !s.likedByMe
  s.likes = s.likedByMe ? (s.likes || 0) + 1 : Math.max(0, (s.likes || 0) - 1)
  // 同时刷新另一侧列表保持同步
  loadPlayerSuggestions()
  if (showPlayerDetail.value && selectedPlayer.value) loadSelectedSuggestions()
}

async function updatePlayerSuggestionStatus(id: number, status: string) {
  await request.put(`/api/development/player-suggestion/${id}/status`, null, { params: { status } })
  ElMessage.success('状态更新成功')
}

async function reportPlayerSuggestion(id: number) {
  await request.post(`/api/development/player-suggestion/${id}/report`)
  ElMessage.success('已举报')
  loadPlayerSuggestions()
}

// ========== 全队动态 ==========
const allSelfReviews = ref<any[]>([])

async function loadAllSelfReviews() {
  const res = await request.get('/api/development/self-review/all')
  allSelfReviews.value = res.data
}

// ========== 队员详情弹窗 ==========
const showPlayerDetail = ref(false)
const selectedPlayer = ref<any>(null)
const selectedReviews = ref<any[]>([])
const selectedSuggestions = ref<any[]>([])
const detailTab = ref('reviews')
const playerSuggestionForm = reactive({ targetUserId: null as any, category: '技术', content: '', isAnonymous: false })

async function openPlayerDetail(item: any) {
  selectedPlayer.value = item.player
  playerSuggestionForm.targetUserId = item.player.userId
  detailTab.value = 'reviews'

  const [reviewsRes, suggestionsRes] = await Promise.all([
    request.get('/api/development/self-review/list', { params: { userId: item.player.userId } }),
    request.get('/api/development/player-suggestion/list', { params: { targetUserId: item.player.userId } })
  ])
  selectedReviews.value = reviewsRes.data
  selectedSuggestions.value = suggestionsRes.data
  showPlayerDetail.value = true
}

async function loadSelectedSuggestions() {
  if (!selectedPlayer.value) return
  const res = await request.get('/api/development/player-suggestion/list', { params: { targetUserId: selectedPlayer.value.userId } })
  selectedSuggestions.value = res.data
}

async function submitPlayerSuggestion() {
  if (!playerSuggestionForm.content) {
    ElMessage.warning('请填写建议内容')
    return
  }
  await request.post('/api/development/player-suggestion', { ...playerSuggestionForm })
  ElMessage.success('建议提交成功')
  playerSuggestionForm.content = ''
  playerSuggestionForm.isAnonymous = false
  loadSelectedSuggestions()
}

// ========== 球队建议 ==========
const teamFilter = reactive({ category: '', status: '' })
const teamSuggestions = ref<any[]>([])
const showTeamSuggestionForm = ref(false)
const teamSuggestionForm = reactive({ category: '训练内容', urgency: 'NORMAL', content: '' })

async function loadTeamSuggestions() {
  const res = await request.get('/api/development/team-suggestion/list', {
    params: { category: teamFilter.category, status: teamFilter.status }
  })
  teamSuggestions.value = res.data.map((s: any) => ({ ...s, replyDraft: '', statusDraft: 'ADOPTED' }))
}

async function submitTeamSuggestion() {
  if (!teamSuggestionForm.content) {
    ElMessage.warning('请填写建议内容')
    return
  }
  await request.post('/api/development/team-suggestion', { ...teamSuggestionForm })
  ElMessage.success('建议提交成功')
  showTeamSuggestionForm.value = false
  teamSuggestionForm.content = ''
  loadTeamSuggestions()
}

async function likeTeamSuggestion(s: any) {
  await request.post(`/api/development/team-suggestion/${s.id}/like`)
  s.likedByMe = !s.likedByMe
  s.likes = s.likedByMe ? (s.likes || 0) + 1 : Math.max(0, (s.likes || 0) - 1)
  loadTeamSuggestions()
}

async function replyTeamSuggestion(s: any) {
  if (!s.statusDraft) {
    ElMessage.warning('请选择回应状态')
    return
  }
  await request.put(`/api/development/team-suggestion/${s.id}/reply`, null, {
    params: { status: s.statusDraft, reply: s.replyDraft }
  })
  ElMessage.success('回应成功')
  loadTeamSuggestions()
}

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 19) : ''
}

function teamStatusLabel(s: string) {
  return { PENDING: '已阅待议', ADOPTED: '采纳', REJECTED: '暂不采纳', DONE: '已落实' }[s] || s
}

function teamStatusType(s: string) {
  return { PENDING: 'info', ADOPTED: 'success', REJECTED: 'danger', DONE: 'primary' }[s] || ''
}

onMounted(() => {
  loadSelfReviews()
  loadPlayerSuggestions()
  loadAllSelfReviews()
  loadTeamSuggestions()
})
</script>
