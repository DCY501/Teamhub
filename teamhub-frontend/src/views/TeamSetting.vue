<template>
  <div>
    <h2 style="margin-top:0">球队设置</h2>

    <el-card style="max-width:600px;margin-bottom:16px">
      <template #header>
        <div style="font-weight:bold;font-size:16px">球队信息</div>
      </template>

      <div v-if="loading" style="text-align:center;padding:20px">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="team">
        <el-form :model="team" label-width="100px">
          <el-form-item label="球队名称">
            <el-input v-model="team.name" maxlength="100" show-word-limit />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="updateTeamName">保存队名</el-button>
      </div>

      <el-empty v-else description="暂无球队信息" />
    </el-card>

    <el-card style="max-width:600px;margin-bottom:16px">
      <template #header>
        <div style="font-weight:bold;font-size:16px">邀请码管理</div>
      </template>

      <div v-if="loading" style="text-align:center;padding:20px">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="team" v-loading="refreshing">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="球队名称">{{ team.name }}</el-descriptions-item>
          <el-descriptions-item label="当前邀请码">
            <span class="invite-code">{{ team.inviteCode }}</span>
            <el-button link type="primary" style="margin-left:12px" @click="copyCode">复制</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ formatTime(team.inviteCodeExpiresAt) }}</el-descriptions-item>
          <el-descriptions-item label="剩余时间">
            <el-tag :type="isExpiredSoon ? 'danger' : 'success'">{{ remainingTime }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div style="color:#666;font-size:14px;margin-bottom:12px">
          <el-icon><Info-Filled /></el-icon>
          将邀请码分享给新队员，队员在注册页填写即可加入球队。邀请码 7 天内有效。
        </div>

        <el-button type="primary" @click="refreshInviteCode">重新生成邀请码</el-button>
      </div>

      <el-empty v-else description="暂无球队信息" />
    </el-card>

    <!-- 三个功能按钮 -->
    <div style="display:flex;gap:16px;max-width:600px">
      <el-card style="flex:1;cursor:pointer;text-align:center" @click="showTransferDialog = true">
        <el-icon :size="40" color="#F56C6C"><User-Filled /></el-icon>
        <div style="font-weight:bold;font-size:16px;margin-top:8px">队长传承</div>
        <div style="color:#999;font-size:13px;margin-top:4px">将队长身份移交给其他成员</div>
      </el-card>
      <el-card style="flex:1;cursor:pointer;text-align:center" @click="openManageDialog">
        <el-icon :size="40" color="#409EFF"><Management /></el-icon>
        <div style="font-weight:bold;font-size:16px;margin-top:8px">成员管理</div>
        <div style="color:#999;font-size:13px;margin-top:4px">编辑或删除球队成员</div>
      </el-card>
    </div>

    <div style="max-width:600px;margin-top:16px">
      <el-card style="cursor:pointer;text-align:center;border-color:#F56C6C" @click="handleDisband">
        <el-icon :size="40" color="#F56C6C"><Delete-Filled /></el-icon>
        <div style="font-weight:bold;font-size:16px;margin-top:8px;color:#F56C6C">解散球队</div>
        <div style="color:#999;font-size:13px;margin-top:4px">删除球队及所有数据（仅限空球队）</div>
      </el-card>
    </div>

    <!-- 队长传承弹窗 -->
    <el-dialog v-model="showTransferDialog" title="队长传承" width="480px" :close-on-click-modal="false">
      <div style="color:#666;font-size:14px;margin-bottom:16px">
        <el-icon><Info-Filled /></el-icon>
        选择一名队员作为新队长。传承后，你的账号将变为普通成员，请谨慎操作。
      </div>

      <div v-if="members.length === 0" style="color:#999;text-align:center;padding:20px">暂无可传承的队员</div>
      <el-radio-group v-else v-model="selectedMemberId" style="display:flex;flex-direction:column;gap:12px;width:100%">
        <el-radio v-for="m in members" :key="m.id" :label="m.userId" style="margin-right:0;padding:8px;border:1px solid #e4e7ed;border-radius:4px">
          <span style="font-weight:bold">{{ m.name }}</span>
          <span v-if="m.jerseyNumber" style="color:#666;margin-left:8px">#{{ m.jerseyNumber }}</span>
          <span v-if="m.position" style="color:#666;margin-left:8px">{{ m.position }}</span>
        </el-radio>
      </el-radio-group>

      <template #footer>
        <el-button @click="showTransferDialog = false">取消</el-button>
        <el-button
          v-if="members.length > 0"
          type="danger"
          :disabled="!selectedMemberId"
          @click="handleTransfer"
        >确认传承</el-button>
      </template>
    </el-dialog>

    <!-- 成员管理弹窗 -->
    <el-dialog v-model="showManageDialog" title="成员管理" width="800px" :close-on-click-modal="false">
      <el-table :data="allMembers" style="width:100%" size="small">
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column label="年级" width="90">
          <template #default="{ row }">
            {{ calcGrade(row.entryYear, row.degree) }}
            <el-tag v-if="row.role === 'CAPTAIN'" type="danger" size="small" style="margin-left:4px">队长</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="jerseyNumber" label="号码" width="70">
          <template #default="{ row }">{{ row.jerseyNumber || '-' }}</template>
        </el-table-column>
        <el-table-column prop="position" label="位置" width="90">
          <template #default="{ row }">{{ row.position || '-' }}</template>
        </el-table-column>
        <el-table-column prop="degree" label="学历" width="70" />
        <el-table-column prop="entryYear" label="入学年份" width="90" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 编辑成员对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑成员信息" width="480px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" maxlength="20" />
        </el-form-item>
        <el-form-item label="学历">
          <el-select v-model="editForm.degree" style="width:100%">
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-input-number v-model="editForm.entryYear" :min="2000" :max="2099" style="width:100%" />
        </el-form-item>
        <el-form-item label="号码">
          <el-input v-model="editForm.jerseyNumber" type="number" placeholder="选填" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="editForm.position" placeholder="例如：主攻 / 守门员" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, InfoFilled, UserFilled, Management, DeleteFilled } from '@element-plus/icons-vue'

const loading = ref(true)
const refreshing = ref(false)
const team = ref<any>(null)
const members = ref<any[]>([])
const allMembers = ref<any[]>([])
const selectedMemberId = ref<number | null>(null)
const user = JSON.parse(localStorage.getItem('user') || '{}')

// 弹窗控制
const showTransferDialog = ref(false)
const showManageDialog = ref(false)
const showEditDialog = ref(false)

// 编辑表单
const editForm = reactive({
  id: null as number | null,
  name: '',
  jerseyNumber: null as number | null,
  position: '',
  degree: '本科',
  entryYear: new Date().getFullYear()
})

const isExpiredSoon = computed(() => {
  if (!team.value?.inviteCodeExpiresAt) return false
  const expires = new Date(team.value.inviteCodeExpiresAt)
  const now = new Date()
  const diff = expires.getTime() - now.getTime()
  return diff < 24 * 60 * 60 * 1000
})

const remainingTime = computed(() => {
  if (!team.value?.inviteCodeExpiresAt) return ''
  const expires = new Date(team.value.inviteCodeExpiresAt)
  const now = new Date()
  const diff = expires.getTime() - now.getTime()
  if (diff <= 0) return '已过期'
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  if (days > 0) return `${days}天 ${hours}小时`
  return `${hours}小时`
})

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 19) : ''
}

async function loadTeam() {
  if (!user.teamId) {
    loading.value = false
    return
  }
  try {
    const [teamRes, membersRes] = await Promise.all([
      request.get(`/api/team/${user.teamId}`),
      request.get('/api/player/list', { params: { status: 'ACTIVE' } })
    ])
    team.value = teamRes.data
    allMembers.value = membersRes.data
    members.value = membersRes.data.filter((m: any) => m.userId !== user.id)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '获取球队信息失败')
  } finally {
    loading.value = false
  }
}

function openManageDialog() {
  showManageDialog.value = true
}

async function handleTransfer() {
  if (!selectedMemberId.value) return
  const target = members.value.find((m: any) => m.userId === selectedMemberId.value)
  try {
    await ElMessageBox.confirm(
      `确定将队长身份传承给「${target?.name || ''}」吗？传承后你的账号将变为普通成员，需要重新登录。`,
      '确认传承',
      { confirmButtonText: '确定传承', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await request.post(`/api/team/transfer-captain?teamId=${user.teamId}&newCaptainUserId=${selectedMemberId.value}`)
    ElMessage.success(res.data.message)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setTimeout(() => window.location.href = '/login', 1500)
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '传承失败')
    }
  }
}

function openEditDialog(member: any) {
  editForm.id = member.id
  editForm.name = member.name
  editForm.jerseyNumber = member.jerseyNumber
  editForm.position = member.position || ''
  editForm.degree = member.degree || '本科'
  editForm.entryYear = member.entryYear
  showEditDialog.value = true
}

async function saveEdit() {
  if (!editForm.name?.trim()) {
    ElMessage.warning('姓名不能为空')
    return
  }
  try {
    await request.put(`/api/player/${editForm.id}`, {
      name: editForm.name.trim(),
      jerseyNumber: editForm.jerseyNumber,
      position: editForm.position || null,
      degree: editForm.degree,
      entryYear: editForm.entryYear
    })
    ElMessage.success('修改成功')
    showEditDialog.value = false
    loadTeam()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

async function handleDelete(member: any) {
  try {
    await ElMessageBox.confirm(
      `确定删除成员「${member.name}」吗？该操作不可恢复。`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'error' }
    )
    await request.delete(`/api/player/${member.id}`)
    ElMessage.success('删除成功')
    loadTeam()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
}

async function handleDisband() {
  try {
    await ElMessageBox.confirm(
      '解散球队将删除所有相关数据且不可恢复。仅当球队只剩你一人时可解散，是否继续？',
      '确认解散球队',
      { confirmButtonText: '解散', cancelButtonText: '取消', type: 'error' }
    )
    const res = await request.post(`/api/team/disband?teamId=${user.teamId}`)
    ElMessage.success(res.data.message)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setTimeout(() => window.location.href = '/login', 1500)
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '解散失败')
    }
  }
}

function calcGrade(entryYear: number, degree: string) {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  let grade = year - entryYear
  if (month >= 9) grade += 1
  if (grade <= 0) return '预科'
  const max = { '本科': 4, '硕士': 3, '博士': 3 }[degree] || 4
  if (grade > max) return `已毕业`
  const prefix = { '本科': '大', '硕士': '研', '博士': '博' }[degree] || '大'
  return `${prefix}${grade}`
}

function copyCode() {
  if (!team.value?.inviteCode) return
  navigator.clipboard.writeText(team.value.inviteCode).then(() => {
    ElMessage.success('邀请码已复制')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

async function updateTeamName() {
  if (!team.value?.name?.trim()) {
    ElMessage.warning('队名不能为空')
    return
  }
  try {
    const res = await request.put('/api/team/name', null, {
      params: { teamId: user.teamId, name: team.value.name.trim() }
    })
    ElMessage.success(res.data.message)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

async function refreshInviteCode() {
  try {
    await ElMessageBox.confirm(
      '重新生成后，旧邀请码将立即失效，是否继续？',
      '确认重新生成',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    refreshing.value = true
    const res = await request.post(`/api/team/refresh-invite?teamId=${user.teamId}`)
    if (team.value) {
      team.value.inviteCode = res.data.inviteCode
      team.value.inviteCodeExpiresAt = res.data.inviteCodeExpiresAt
    }
    ElMessage.success(res.data.message)
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '生成失败')
    }
  } finally {
    refreshing.value = false
  }
}

onMounted(loadTeam)
</script>

<style scoped>
.invite-code {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 4px;
  color: #409EFF;
  display: inline-block;
  transition: all 0.4s ease;
}
</style>
