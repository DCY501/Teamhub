<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="margin:0">时光轴</h2>
      <el-button type="primary" @click="openCreate">发布事件</el-button>
    </div>

    <!-- 筛选 -->
    <div style="display:flex;gap:12px;margin-bottom:16px">
      <el-select v-model="filterYear" placeholder="选择年份" clearable style="width:140px" @change="loadEvents">
        <el-option v-for="y in availableYears" :key="y" :label="y + '年'" :value="y" />
      </el-select>
      <el-select v-model="filterMonth" placeholder="选择月份" clearable style="width:140px" @change="loadEvents">
        <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
      </el-select>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <!-- 时间河流 -->
    <el-timeline>
      <el-timeline-item
        v-for="e in events"
        :key="e.id"
        :type="e.eventType === 'MANUAL' ? 'primary' : 'info'"
        :timestamp="formatTime(e.eventTime)"
      >
        <el-card :body-style="{ padding: '12px 16px' }" :shadow="e.eventType === 'MANUAL' ? 'always' : 'never'">
          <div style="display:flex;justify-content:space-between;align-items:flex-start">
            <div style="cursor:pointer" @click="openDetail(e)">
              <el-tag v-if="e.eventType === 'AUTO'" size="small" type="info">自动</el-tag>
              <el-tag v-else size="small" type="primary">人工</el-tag>
              <span style="font-weight:bold;margin-left:8px">{{ e.title }}</span>
            </div>
            <el-button v-if="e.eventType === 'MANUAL' && canEdit(e)"
                       link type="primary" size="small" @click="openEdit(e)">编辑</el-button>
          </div>

          <p v-if="e.description" style="color:#666;margin:8px 0;font-size:14px">{{ e.description }}</p>

          <div v-if="e.imageUrls" style="display:flex;gap:8px;margin:8px 0;flex-wrap:wrap">
            <img v-for="(url, idx) in parseImages(e.imageUrls)" :key="idx" :src="url"
                 style="width:100px;height:100px;object-fit:cover;border-radius:4px;cursor:pointer"
                 @click.stop="previewImage = url; previewVisible = true" />
          </div>

          <div style="display:flex;gap:16px;margin-top:8px;flex-wrap:wrap">
            <el-link v-if="e.albumUrl" :href="e.albumUrl" target="_blank" type="primary" size="small" @click.stop>📁 相册</el-link>
            <el-link v-if="e.refTrainingId" type="primary" size="small" @click.stop="$router.push('/training')">🏃 训练</el-link>
            <el-link v-if="e.refMatchId" type="primary" size="small" @click.stop="$router.push('/match')">🏆 比赛</el-link>
            <el-link v-if="e.refPlayerId" type="primary" size="small" @click.stop="$router.push('/player')">👤 球员</el-link>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>

    <el-empty v-if="events.length === 0" description="暂无事件" />

    <!-- 详情/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="90px" :disabled="isViewOnly">
        <el-form-item label="类型">
          <el-tag v-if="form.eventType === 'AUTO'" type="info">自动事件（不可编辑）</el-tag>
          <el-tag v-else type="primary">人工事件</el-tag>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="事件标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="事件详细描述" />
        </el-form-item>
        <el-form-item label="事件时间" required>
          <el-date-picker v-model="form.eventTime" type="datetime" placeholder="选择事件时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="关联类型">
          <el-select v-model="refType" placeholder="选填" clearable style="width:100%">
            <el-option label="训练" value="training" />
            <el-option label="比赛" value="match" />
            <el-option label="球员" value="player" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="refType === 'training'" label="关联训练">
          <el-input-number v-model="form.refTrainingId" :min="1" placeholder="训练ID" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="refType === 'match'" label="关联比赛">
          <el-input-number v-model="form.refMatchId" :min="1" placeholder="比赛ID" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="refType === 'player'" label="关联球员">
          <el-input-number v-model="form.refPlayerId" :min="1" placeholder="球员ID" style="width:100%" />
        </el-form-item>
        <el-form-item label="图片链接">
          <div v-for="(img, idx) in form.images" :key="idx" style="display:flex;gap:8px;margin-bottom:8px">
            <el-input v-model="form.images[idx]" placeholder="图片URL" />
            <el-button type="danger" size="small" @click="removeImage(idx)">删除</el-button>
          </div>
          <el-button v-if="form.images.length < 3" size="small" @click="form.images.push('')">+ 添加图片</el-button>
        </el-form-item>
        <el-form-item label="相册链接">
          <el-input v-model="form.albumUrl" placeholder="网盘相册分享链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!isViewOnly" type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览">
      <img :src="previewImage" style="width:100%;border-radius:4px" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const events = ref<any[]>([])
const filterYear = ref<number | null>(null)
const filterMonth = ref<number | null>(null)
const availableYears = ref<number[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const isViewOnly = ref(false)
const editingId = ref<number | null>(null)
const previewVisible = ref(false)
const previewImage = ref('')
const refType = ref('')

const form = reactive({
  eventType: 'MANUAL',
  title: '',
  description: '',
  eventTime: null as any,
  images: [] as string[],
  albumUrl: '',
  refTrainingId: null as any,
  refMatchId: null as any,
  refPlayerId: null as any
})

function canEdit(e: any) {
  return e.eventType === 'MANUAL' && (currentUser.id === e.createdBy || currentUser.role === 'CAPTAIN')
}

function parseImages(json: string) {
  try {
    return JSON.parse(json)
  } catch {
    return []
  }
}

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

function resetFilter() {
  filterYear.value = null
  filterMonth.value = null
  loadEvents()
}

function removeImage(idx: number) {
  form.images.splice(idx, 1)
}

function resetForm() {
  form.eventType = 'MANUAL'
  form.title = ''
  form.description = ''
  form.eventTime = null
  form.images = []
  form.albumUrl = ''
  form.refTrainingId = null
  form.refMatchId = null
  form.refPlayerId = null
  refType.value = ''
  editingId.value = null
}

function setRefTypeFromForm() {
  if (form.refTrainingId) refType.value = 'training'
  else if (form.refMatchId) refType.value = 'match'
  else if (form.refPlayerId) refType.value = 'player'
  else refType.value = ''
}

function fillForm(e: any) {
  form.eventType = e.eventType
  form.title = e.title || ''
  form.description = e.description || ''
  form.eventTime = e.eventTime ? e.eventTime.replace(' ', 'T') : null
  form.images = e.imageUrls ? parseImages(e.imageUrls) : []
  form.albumUrl = e.albumUrl || ''
  form.refTrainingId = e.refTrainingId || null
  form.refMatchId = e.refMatchId || null
  form.refPlayerId = e.refPlayerId || null
  setRefTypeFromForm()
}

function openCreate() {
  resetForm()
  isEdit.value = false
  isViewOnly.value = false
  dialogTitle.value = '发布事件'
  dialogVisible.value = true
}

function openDetail(e: any) {
  fillForm(e)
  isEdit.value = false
  isViewOnly.value = true
  dialogTitle.value = '事件详情'
  dialogVisible.value = true
}

function openEdit(e: any) {
  fillForm(e)
  isEdit.value = true
  isViewOnly.value = false
  dialogTitle.value = '编辑事件'
  editingId.value = e.id
  dialogVisible.value = true
}

async function loadEvents() {
  const params: any = {}
  if (filterYear.value) params.year = filterYear.value
  if (filterMonth.value) params.month = filterMonth.value
  const res = await request.get('/api/timeline/list', { params })
  events.value = res.data
  const years = new Set<number>()
  events.value.forEach((e: any) => {
    if (e.eventTime) years.add(parseInt(e.eventTime.substring(0, 4)))
  })
  availableYears.value = Array.from(years).sort((a, b) => b - a)
}

async function submit() {
  if (!form.title || !form.eventTime) {
    ElMessage.warning('标题和事件时间为必填项')
    return
  }
  const d = new Date(form.eventTime)
  const pad = (n: number) => n.toString().padStart(2, '0')
  const payload: any = {
    title: form.title,
    description: form.description,
    eventTime: `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`,
    albumUrl: form.albumUrl || null
  }
  const imgs = form.images.filter(u => u.trim())
  if (imgs.length > 0) payload.imageUrls = JSON.stringify(imgs)
  if (form.refTrainingId) payload.refTrainingId = form.refTrainingId
  if (form.refMatchId) payload.refMatchId = form.refMatchId
  if (form.refPlayerId) payload.refPlayerId = form.refPlayerId

  if (isEdit.value && editingId.value) {
    await request.put(`/api/timeline/${editingId.value}`, payload)
    ElMessage.success('事件更新成功')
  } else {
    await request.post('/api/timeline', payload)
    ElMessage.success('事件发布成功')
  }
  dialogVisible.value = false
  resetForm()
  loadEvents()
}

onMounted(() => {
  loadEvents()
})
</script>
