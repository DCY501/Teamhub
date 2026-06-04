import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/reset-password',
      component: () => import('../views/ResetPassword.vue')
    },
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', component: () => import('../views/Home.vue') },
        { path: 'training', component: () => import('../views/Training.vue') },
        { path: 'match', component: () => import('../views/Match.vue') },
        { path: 'player', component: () => import('../views/Player.vue') },
        { path: 'player/:id', component: () => import('../views/PlayerDetail.vue') },
        { path: 'player/:id/edit', component: () => import('../views/PlayerEdit.vue') },
        { path: 'development', component: () => import('../views/Development.vue') },
        { path: 'timeline', component: () => import('../views/Timeline.vue') },
        { path: 'training/:id', component: () => import('../views/TrainingDetail.vue') },
        { path: 'match/:id', component: () => import('../views/MatchDetail.vue') },
        { path: 'team-setting', component: () => import('../views/TeamSetting.vue') },
        { path: 'admin', component: () => import('../views/Admin.vue') },
      ]
    }
  ]
})

// 未登录跳转到登录页（注册页和登录页除外）
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const publicPages = ['/login', '/register', '/reset-password']
  if (!token && !publicPages.includes(to.path)) {
    return '/login'
  }
})

export default router
