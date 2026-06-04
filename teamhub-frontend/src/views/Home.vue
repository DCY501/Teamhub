<template>
  <div>
    <h2 style="margin-top:0">个人仪表盘</h2>

    <!-- 第一行：个人信息 + 本周训练 -->
    <el-row :gutter="16">
      <!-- 个人信息 -->
      <el-col :xs="24" :md="8" style="margin-bottom:16px">
        <el-card>
          <div v-if="playerInfo" style="display:flex;gap:16px;align-items:center">
            <el-avatar :size="64" :src="playerInfo.photoUrl || defaultAvatar" />
            <div>
              <div style="font-size:18px;font-weight:bold">{{ playerInfo.name }}</div>
              <div style="color:#666;font-size:13px;margin-top:4px">
                <span v-if="playerInfo.jerseyNumber">#{{ playerInfo.jerseyNumber }}</span>
                <span v-if="playerInfo.position"> · {{ playerInfo.position }}</span>
              </div>
              <div style="color:#999;font-size:12px;margin-top:4px">
                {{ yearDisplay }}
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无球员信息" />
        </el-card>

        <!-- 最新自评 -->
        <el-card style="margin-top:16px" header="我的进步方向">
          <div v-if="latestSelfReview">
            <el-tag size="small">{{ latestSelfReview.category }}</el-tag>
            <p style="margin-top:8px;color:#666;font-size:13px">
              <strong style="color:#409EFF">当前问题：</strong>{{ latestSelfReview.currentProblem }}
            </p>
            <p style="margin-top:4px;color:#666;font-size:13px">
              <strong style="color:#67C23A">改进目标：</strong>{{ latestSelfReview.improvementGoal }}
            </p>
            <p v-if="latestSelfReview.expectedTraining" style="margin-top:4px;color:#666;font-size:13px">
              <strong style="color:#E6A23C">期望训练：</strong>{{ latestSelfReview.expectedTraining }}
            </p>
          </div>
          <div v-else style="color:#999;font-size:13px;text-align:center;padding:12px 0">
            还没有提交过自评<br/>
            <el-link type="primary" size="small" @click="$router.push('/development')">去发展中心填写</el-link>
          </div>
        </el-card>

        <!-- 修改密码入口 -->
        <el-card style="margin-top:16px">
          <el-button type="primary" style="width:100%" @click="showPwdDialog = true">🔒 修改密码</el-button>
        </el-card>

        <!-- 修改密码弹窗 -->
        <el-dialog v-model="showPwdDialog" title="修改密码" width="400px">
          <el-form :model="pwdForm" label-width="90px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showPwdDialog = false">取消</el-button>
            <el-button type="primary" @click="changePassword">确认修改</el-button>
          </template>
        </el-dialog>
      </el-col>

      <!-- 本周训练 -->
      <el-col :xs="24" :md="16" style="margin-bottom:16px">
        <el-card header="本周训练">
          <div v-if="upcomingTrainings.length > 0">
            <div v-for="t in upcomingTrainings" :key="t.id"
                 style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #f0f0f0">
              <div>
                <div style="font-weight:bold">
                  {{ t.title }}
                  <el-tag v-if="t.isFinished" type="info" size="small" effect="plain" style="margin-left:6px">已结束</el-tag>
                </div>
                <div style="color:#666;font-size:13px">
                  {{ formatTime(t.trainTime) }} · {{ t.location || '地点待定' }}
                </div>
              </div>
              <div v-if="t.isFinished">
                <el-tag type="info" size="small">已结束</el-tag>
              </div>
              <div v-else>
                <el-tag v-if="t.signupStatus === 'ATTEND'" type="success" size="small">已报名</el-tag>
                <el-tag v-else-if="t.signupStatus === 'ABSENT'" type="danger" size="small">请假</el-tag>
                <el-tag v-else-if="t.signupStatus === 'PENDING'" type="warning" size="small">待定</el-tag>
                <el-tag v-else type="info" size="small">未报名</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="本周暂无训练安排" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：出勤率 + 比赛 + 建议 -->
    <el-row :gutter="16">
      <!-- 出勤率 -->
      <el-col :xs="24" :md="8" style="margin-bottom:16px">
        <el-card>
          <div style="text-align:center">
            <div style="color:#666;font-size:14px;margin-bottom:8px">我的出勤率</div>
            <div style="font-size:36px;font-weight:bold;color:#409EFF">{{ attendance.rate }}%</div>
            <div style="color:#999;font-size:13px;margin-top:4px">
              已参加 {{ attendance.attended }} 次 / 共 {{ attendance.total }} 次训练
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近比赛 -->
      <el-col :xs="24" :md="8" style="margin-bottom:16px">
        <el-card header="最近比赛">
          <div v-if="recentMatches.length > 0">
            <div v-for="m in recentMatches" :key="m.id"
                 style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #f0f0f0;cursor:pointer"
                 @click="$router.push('/match/' + m.id)">
              <div>
                <div style="font-weight:bold">{{ m.gameType }} vs {{ m.opponent }}</div>
                <div style="color:#666;font-size:13px">{{ formatTime(m.gameTime) }}</div>
              </div>
              <el-tag :type="matchStatusType(m.status)" size="small">{{ matchStatusLabel(m.status) }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无比赛记录" />
        </el-card>
      </el-col>

      <!-- 待处理建议（给我的） -->
      <el-col :xs="24" :md="8" style="margin-bottom:16px">
        <el-card header="队友建议">
          <div v-if="pendingPlayerSuggestions.length > 0">
            <div v-for="s in pendingPlayerSuggestions" :key="s.id"
                 style="padding:8px 0;border-bottom:1px solid #f0f0f0">
              <el-tag size="small">{{ s.category }}</el-tag>
              <span style="font-size:13px;margin-left:6px">{{ s.content }}</span>
            </div>
          </div>
          <div v-else style="color:#999;font-size:13px;text-align:center;padding:20px 0">
            暂无待处理建议 👍
          </div>
          <div style="text-align:center;margin-top:8px">
            <el-link type="primary" size="small" @click="$router.push('/development')">去发展中心处理</el-link>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 队长专属：待处理球队建议 -->
    <el-row v-if="isCaptain" :gutter="16">
      <el-col :xs="24" :md="8" style="margin-bottom:16px">
        <el-card header="待处理球队建议">
          <div v-if="pendingTeamSuggestions.length > 0">
            <div v-for="s in pendingTeamSuggestions" :key="s.id"
                 style="padding:8px 0;border-bottom:1px solid #f0f0f0">
              <el-tag size="small">{{ s.category }}</el-tag>
              <span style="font-size:13px;margin-left:6px">{{ s.content }}</span>
            </div>
          </div>
          <div v-else style="color:#999;font-size:13px;text-align:center;padding:20px 0">
            暂无待处理建议
          </div>
          <div style="text-align:center;margin-top:8px">
            <el-link type="primary" size="small" @click="$router.push('/development')">去发展中心回应</el-link>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const isCaptain = currentUser.role === 'CAPTAIN'
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const upcomingTrainings = ref<any[]>([])
const recentMatches = ref<any[]>([])
const attendance = ref({ total: 0, attended: 0, rate: 0 })
const pendingPlayerSuggestions = ref<any[]>([])
const pendingTeamSuggestions = ref<any[]>([])
const playerInfo = ref<any>(null)
const latestSelfReview = ref<any>(null)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const showPwdDialog = ref(false)

const yearDisplay = computed(() => {
  if (!playerInfo.value) return ''
  const p = playerInfo.value
  const now = new Date()
  let year = now.getFullYear() - p.entryYear
  if (now.getMonth() + 1 >= 9) year++
  if (year <= 0) return '预科'
  const prefix = p.degree === '硕士' ? '研' : p.degree === '博士' ? '博' : '大'
  const maxYear = p.degree === '硕士' || p.degree === '博士' ? 3 : 4
  if (year > maxYear) return '已毕业（' + p.degree + '）'
  const nums = ['一', '二', '三', '四']
  return prefix + nums[year - 1]
})

function formatTime(t: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

function matchStatusLabel(s: string) {
  return { UPCOMING: '未开始', ONGOING: '进行中', FINISHED: '已结束' }[s] || s
}

function matchStatusType(s: string) {
  return { UPCOMING: 'info', ONGOING: 'warning', FINISHED: 'success' }[s] || ''
}

async function loadDashboard() {
  const res = await request.get('/api/dashboard')
  const data = res.data
  upcomingTrainings.value = data.upcomingTrainings || []
  recentMatches.value = data.recentMatches || []
  attendance.value = data.attendance || { total: 0, attended: 0, rate: 0 }
  pendingPlayerSuggestions.value = data.pendingPlayerSuggestions || []
  pendingTeamSuggestions.value = data.pendingTeamSuggestions || []
  playerInfo.value = data.playerInfo || null
  latestSelfReview.value = data.latestSelfReview || null
}

async function changePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    const res = await request.put('/api/user/password', null, {
      params: { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword }
    })
    ElMessage.success(res.data.message)
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    showPwdDialog.value = false
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

onMounted(() => {
  loadDashboard()
})
</script>
