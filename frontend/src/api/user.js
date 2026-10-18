import request from '@/utils/request'

export function register(data) {
  return request({ url: '/user/register', method: 'post', data })
}

export function login(data) {
  return request({ url: '/user/login', method: 'post', data })
}

export function resetPassword(data) {
  return request({ url: '/user/resetPassword', method: 'post', data })
}

export function getInfo() {
  return request({ url: '/user/info', method: 'get' })
}

export function updateInfo(data) {
  return request({ url: '/user/updateInfo', method: 'put', data })
}

export function updatePassword(data) {
  return request({ url: '/user/updatePassword', method: 'put', data })
}

export function listUsers(params) {
  return request({ url: '/user/list', method: 'get', params })
}

export function adminUpdateUser(userId, data) {
  return request({ url: `/user/admin/update/${userId}`, method: 'put', data })
}

export function adminDeleteUser(userId) {
  return request({ url: `/user/admin/delete/${userId}`, method: 'delete' })
}

export function logout() {
  return request({ url: '/user/logout', method: 'post' })
}
