<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="margin:0">训练中心</h2>
      <el-button v-if="user.role === 'CAPTAIN'" type="primary" @click="openCreate">发布训练</el-button>
    </div>

    <!-- 训练列表 -->
    <el-card v-for="t in trainings" :key="t.id" style="margin-bottom:12px;cursor:pointer" @click="goDetail(t.id)">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <div style="font-size:16px;font-weight:bold">
            {{ t.title }}
            <el-tag v-if="t.isFinished" type="info" size="small" effect="plain" style="margin-left:6px">已结束</el-tag>
          </div>
          <div style="color:#888;margin-top:4px">{{ t.location }} · {{ formatTime(t.trainTime) }}</div>
        </div>
        <div style="display:flex;gap:8px;align-items:center">
          <el-tag v-if="t.signupStatus === 'ATTEND'" type="success" size="small">已报名</el-tag>
          <el-tag v-else-if="t.signupStatus === 'ABSENT'" type="danger" size="small">请假</el-tag>
          <el-tag v-else-if="t.signupStatus === 'PENDING'" type="warning" size="small">待定</el-tag>
          <el-tag v-else type="info" size="small">未报名</el-tag>
          <template v-if="user.role === 'CAPTAIN'">
            <el-button type="primary" size="small" @click.stop="openEdit(t)">编辑</el-button>
            <el-button type="danger" size="small" @click.stop="handleDelete(t.id)">删除</el-button>
          </template>
          <el-tag>查看详情</el-tag>
        </div>
      </div>
    </el-card>

    <el-empty v-if="trainings.length === 0" description="暂无训练" />

    <!-- 发布/编辑训练弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑训练' : '发布训练'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="form.trainTime" type="datetime" style="width:100%" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="视频链接">
          <el-input v-model="form.videoUrl" placeholder="网盘/B站链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ isEdit ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const trainings = ref<any[]>([])
const showDialog = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const user = JSON.parse(localStorage.getItem('user') || '{}')

const form = reactive({
  title: '',
  location: '',
  trainTime: '' as string | Date,
  content: '',
  videoUrl: '',
  createdBy: null
})

function resetForm() {
  form.title = ''
  form.location = ''
  form.trainTime = ''
  form.content = ''
  form.videoUrl = ''
  form.createdBy = null
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  showDialog.value = true
}

function openEdit(t: any) {
  isEdit.value = true
  editingId.value = t.id
  form.title = t.title
  form.location = t.location
  form.trainTime = t.trainTime ? new Date(t.trainTime.replace(' ', 'T')) : ''
  form.content = t.content
  form.videoUrl = t.videoUrl
  showDialog.value = true
}

function formatPayload() {
  const payload: any = { ...form }
  if (payload.trainTime instanceof Date) {
    const d = payload.trainTime as Date
    const pad = (n: number) => n.toString().padStart(2, '0')
    payload.trainTime = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
  return payload
}

async function handleSubmit() {
  const payload = formatPayload()
  if (isEdit.value && editingId.value !== null) {
    await request.put(`/api/training/${editingId.value}`, payload)
    ElMessage.success('更新成功')
  } else {
    payload.createdBy = user.id
    await request.post('/api/training/create', payload)
    ElMessage.success('发布成功')
  }
  showDialog.value = false
  loadList()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该训练吗？相关报名记录也将被清除', '确认删除', { type: 'warning' })
    await request.delete(`/api/training/${id}`)
    ElMessage.success('删除成功')
    loadList()
  } catch {
    // 取消删除
  }
}

function goDetail(id: number) {
  router.push(`/training/${id}`)
}

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

async function loadList() {
  const res = await request.get('/api/training/list')
  trainings.value = res.data
}

onMounted(loadList)
</script>
