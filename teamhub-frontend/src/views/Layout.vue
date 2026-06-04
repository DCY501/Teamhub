<template>
  <el-container style="height: 100vh">
    <!-- 桌面端侧边栏 -->
    <el-aside
      v-if="!isMobile"
      :width="isCollapse ? '64px' : '200px'"
      style="background:#001529;transition:width 0.3s"
    >
      <div style="color:white;text-align:center;padding:20px 0;font-size:18px;font-weight:bold;white-space:nowrap;overflow:hidden">
        <span v-if="!isCollapse">TeamHub</span>
        <span v-else>T</span>
      </div>
      <el-menu
        router
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/">
          <el-icon><Home-Filled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/training">
          <el-icon><Basketball /></el-icon>
          <template #title>训练中心</template>
        </el-menu-item>
        <el-menu-item index="/match">
          <el-icon><Trophy /></el-icon>
          <template #title>比赛中心</template>
        </el-menu-item>
        <el-menu-item index="/player">
          <el-icon><User-Filled /></el-icon>
          <template #title>球员档案</template>
        </el-menu-item>
        <el-menu-item index="/development">
          <el-icon><Trend-Charts /></el-icon>
          <template #title>发展中心</template>
        </el-menu-item>
        <el-menu-item index="/timeline">
          <el-icon><Clock /></el-icon>
          <template #title>时光轴</template>
        </el-menu-item>
        <el-menu-item v-if="user?.role === 'CAPTAIN'" index="/team-setting">
          <el-icon><Setting /></el-icon>
          <template #title>球队设置</template>
        </el-menu-item>
        <el-menu-item v-if="user?.role === 'CAPTAIN'" index="/admin">
          <el-icon><Management /></el-icon>
          <template #title>管理后台</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header style="background:#fff;border-bottom:1px solid #eee;line-height:60px;display:flex;justify-content:space-between;align-items:center;padding:0 16px">
        <div style="display:flex;align-items:center;gap:12px">
          <!-- 移动端汉堡按钮 -->
          <el-button v-if="isMobile" link @click="drawerVisible = true">
            <el-icon :size="24"><Expand /></el-icon>
          </el-button>
          <!-- 桌面端折叠按钮 -->
          <el-button v-else link @click="isCollapse = !isCollapse">
            <el-icon :size="20"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          <span>TeamHub</span>
        </div>
        <div style="display:flex;align-items:center;gap:12px">
          <el-tag v-if="user?.role === 'CAPTAIN'" type="danger">队长</el-tag>
          <el-tag v-else type="info">队员</el-tag>
          <span style="font-size:14px">{{ user?.username || '未知用户' }}</span>
          <el-button link type="danger" size="small" @click="logout">退出</el-button>
        </div>
      </el-header>

      <el-main style="background:#f5f5f5">
        <router-view />
      </el-main>
    </el-container>

    <!-- 移动端抽屉导航 -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="200px" :with-header="false" style="background:#001529">
      <div style="color:white;text-align:center;padding:20px;font-size:18px;font-weight:bold">
        TeamHub
      </div>
      <el-menu
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
        @select="drawerVisible = false"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/training">训练中心</el-menu-item>
        <el-menu-item index="/match">比赛中心</el-menu-item>
        <el-menu-item index="/player">球员档案</el-menu-item>
        <el-menu-item index="/development">发展中心</el-menu-item>
        <el-menu-item index="/timeline">时光轴</el-menu-item>
        <el-menu-item v-if="user?.role === 'CAPTAIN'" index="/team-setting">球队设置</el-menu-item>
        <el-menu-item v-if="user?.role === 'CAPTAIN'" index="/admin">管理后台</el-menu-item>
      </el-menu>
    </el-drawer>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  HomeFilled,
  Basketball,
  Trophy,
  UserFilled,
  TrendCharts,
  Clock,
  Setting,
  Management,
  Fold,
  Expand
} from '@element-plus/icons-vue'

const router = useRouter()
const isCollapse = ref(false)
const isMobile = ref(false)
const drawerVisible = ref(false)

function checkScreen() {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) drawerVisible.value = false
}

onMounted(() => {
  checkScreen()
  window.addEventListener('resize', checkScreen)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreen)
})

function safeParse(key: string) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw || raw === 'undefined' || raw === 'null') return null
    return JSON.parse(raw)
  } catch {
    return null
  }
}

const user = safeParse('user')

if (!user || !user.id) {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.replace('/login')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>
