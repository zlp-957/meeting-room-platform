import request from '@/utils/request'

export function addBook(data) {
  return request({ url: '/book/add', method: 'post', data })
}

export function auditBook(data) {
  return request({ url: '/book/audit', method: 'put', data })
}

export function cancelBook(bookId) {
  return request({ url: `/book/cancel/${bookId}`, method: 'put' })
}

export function listMine(params) {
  return request({ url: '/book/list', method: 'get', params })
}

export function listAll(params) {
  return request({ url: '/book/all', method: 'get', params })
}

export function deleteBook(bookId) {
  return request({ url: `/book/delete/${bookId}`, method: 'delete' })
}

export function occupiedSlots(params) {
  return request({ url: '/book/occupiedSlots', method: 'get', params })
}
