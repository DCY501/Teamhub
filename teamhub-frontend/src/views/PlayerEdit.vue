<template>
  <div v-if="player">
    <el-page-header @back="$router.back()" content="编辑资料" style="margin-bottom:16px" />

    <el-card style="max-width:500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" disabled />
        </el-form-item>
        <el-form-item label="号码">
          <el-input v-model="form.jerseyNumber" type="number" placeholder="球衣号码" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.position" placeholder="例如：主攻 / 守门员" />
        </el-form-item>
        <el-form-item label="学历">
          <el-select v-model="form.degree" style="width:100%">
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-input v-model="form.entryYear" type="number" placeholder="例如：2024" />
        </el-form-item>
        <el-form-item label="照片链接">
          <el-input v-model="form.photoUrl" placeholder="网盘/图床图片链接" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const player = ref<any>(null)

const form = reactive({
  name: '',
  jerseyNumber: '',
  position: '',
  degree: '本科',
  entryYear: '',
  photoUrl: ''
})

async function load() {
  const res = await request.get(`/api/player/${id}`)
  player.value = res.data
  form.name = res.data.name
  form.jerseyNumber = res.data.jerseyNumber || ''
  form.position = res.data.position || ''
  form.degree = res.data.degree || '本科'
  form.entryYear = res.data.entryYear || ''
  form.photoUrl = res.data.photoUrl || ''
}

async function handleSave() {
  try {
    await request.put(`/api/player/${id}`, {
      jerseyNumber: form.jerseyNumber ? Number(form.jerseyNumber) : null,
      position: form.position || null,
      degree: form.degree,
      entryYear: form.entryYear ? Number(form.entryYear) : null,
      photoUrl: form.photoUrl || null
    })
    ElMessage.success('保存成功')
    router.back()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  }
}

onMounted(load)
</script>
