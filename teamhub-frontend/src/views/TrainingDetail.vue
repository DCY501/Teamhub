<template>
  <div v-if="training">
    <el-page-header @back="$router.back()" :content="training.title" style="margin-bottom:16px" />

    <el-card style="margin-bottom:16px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <h3 style="margin:0">训练详情</h3>
        <div v-if="user.role === 'CAPTAIN'" style="display:flex;gap:8px">
          <el-button type="primary" size="small" @click="openEdit">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete">删除</el-button>
        </div>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="地点">{{ training.location }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ formatTime(training.trainTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="training.isFinished" type="info">已结束</el-tag>
          <el-tag v-else type="success">进行中</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ training.content }}</el-descriptions-item>
        <el-descriptions-item label="视频链接" :span="2">
          <a v-if="training.videoUrl" :href="training.videoUrl" target="_blank">点击查看</a>
          <span v-else>暂无</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 报名统计 -->
    <el-card style="margin-bottom:16px">
      <div style="display:flex;gap:24px;margin-bottom:16px">
        <el-tag type="success">参加：{{ stats.attend }}</el-tag>
        <el-tag type="danger">请假：{{ stats.absent }}</el-tag>
        <el-tag type="warning">待定：{{ stats.pending }}</el-tag>
      </div>

      <div v-if="training.isFinished">
        <el-alert type="info" :closable="false">训练已结束，报名通道已关闭</el-alert>
      </div>
      <div v-else>
        <div style="margin-bottom:8px">我的状态：</div>
        <el-radio-group v-model="myStatus">
          <el-radio value="ATTEND">参加</el-radio>
          <el-radio value="ABSENT">请假</el-radio>
          <el-radio value="PENDING">待定</el-radio>
        </el-radio-group>
        <el-input v-if="myStatus === 'ABSENT'" v-model="reason" placeholder="请假原因（选填）" style="margin-top:8px" />
        <el-button type="primary" style="margin-top:8px;display:block" @click="handleSignup">提交</el-button>
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEdit" title="编辑训练" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="editForm.location" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="editForm.trainTime" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="视频链接">
          <el-input v-model="editForm.videoUrl" placeholder="网盘/B站链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const training = ref<any>(null)
const stats = ref({ attend: 0, absent: 0, pending: 0 })
const myStatus = ref('ATTEND')
const reason = ref('')
const user = JSON.parse(localStorage.getItem('user') || '{}')

const showEdit = ref(false)
const editForm = reactive({
  title: '',
  location: '',
  trainTime: '' as string | Date,
  content: '',
  videoUrl: ''
})

async function load() {
  const [t, s] = await Promise.all([
    request.get(`/api/training/${id}`),
    request.get(`/api/training/${id}/stats`)
  ])
  training.value = t.data
  stats.value = s.data
}

async function handleSignup() {
  await request.post(`/api/training/${id}/signup`, null, {
    params: { status: myStatus.value, reason: reason.value }
  })
  ElMessage.success('提交成功')
  load()
}

function openEdit() {
  const t = training.value
  editForm.title = t.title
  editForm.location = t.location
  editForm.trainTime = t.trainTime ? new Date(t.trainTime.replace(' ', 'T')) : ''
  editForm.content = t.content
  editForm.videoUrl = t.videoUrl
  showEdit.value = true
}

function formatPayload() {
  const payload: any = { ...editForm }
  if (payload.trainTime instanceof Date) {
    const d = payload.trainTime as Date
    const pad = (n: number) => n.toString().padStart(2, '0')
    payload.trainTime = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
  return payload
}

async function handleUpdate() {
  const payload = formatPayload()
  await request.put(`/api/training/${id}`, payload)
  ElMessage.success('更新成功')
  showEdit.value = false
  load()
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除该训练吗？相关报名记录也将被清除', '确认删除', { type: 'warning' })
    await request.delete(`/api/training/${id}`)
    ElMessage.success('删除成功')
    router.push('/training')
  } catch {
    // 取消删除
  }
}

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

onMounted(load)
</script>
