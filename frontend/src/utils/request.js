import axios from 'axios'
import { Message } from 'element-ui'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：携带 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

// 响应拦截器：统一处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    // 未登录 / 无权限
    if (res.code === 401) {
      Message.error('登录已失效，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
      return Promise.reject(new Error(res.msg))
    }
    if (res.code === 403) {
      Message.error('无权限访问')
      return Promise.reject(new Error(res.msg))
    }
    Message.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg))
  },
  error => {
    // 网络层错误统一转成友好中文提示，不暴露英文技术信息
    let msg = '网络异常，请稍后重试'
    if (error.code === 'ECONNABORTED') {
      msg = '请求超时，请稍后重试'
    } else if (error.response) {
      const status = error.response.status
      const map = {
        400: '请求参数有误',
        401: '登录已失效，请重新登录',
        403: '无权限访问',
        404: '请求的资源不存在',
        500: '服务器繁忙，请稍后重试'
      }
      msg = map[status] || '请求失败，请稍后重试'
    }
    Message.error(msg)
    return Promise.reject(new Error(msg))
  }
)

export default request
