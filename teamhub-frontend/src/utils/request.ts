import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/',
  timeout: 10000
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message

    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    } else if (status === 403) {
      ElMessage.error('无权操作')
    } else if (status === 400) {
      // 400 业务错误由调用方自己处理，不弹全局提示
    } else {
      ElMessage.error(message || '请求失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default request
