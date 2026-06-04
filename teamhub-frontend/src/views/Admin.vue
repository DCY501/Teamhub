<template>
  <div>
    <h2 style="margin-top:0">管理后台</h2>

    <el-card header="被举报的个人建议">
      <div v-if="reportedSuggestions.length > 0">
        <el-table :data="reportedSuggestions" style="width:100%">
          <el-table-column prop="category" label="分类" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.category }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="200" />
          <el-table-column label="时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="ignoreReport(row.id)">忽略</el-button>
              <el-button link type="danger" size="small" @click="deleteSuggestion(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else description="暂无被举报的建议" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const reportedSuggestions = ref<any[]>([])

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

async function loadReported() {
  const res = await request.get('/api/development/player-suggestion/reported')
  reportedSuggestions.value = res.data
}

async function ignoreReport(id: number) {
  try {
    await ElMessageBox.confirm('确定忽略这条举报吗？', '确认', { type: 'warning' })
    await request.post(`/api/development/player-suggestion/${id}/ignore`)
    ElMessage.success('已忽略举报')
    loadReported()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function deleteSuggestion(id: number) {
  try {
    await ElMessageBox.confirm('删除后不可恢复，确定删除吗？', '确认删除', { type: 'error' })
    await request.delete(`/api/development/player-suggestion/${id}`)
    ElMessage.success('建议已删除')
    loadReported()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadReported()
})
</script>
