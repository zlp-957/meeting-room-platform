import request from '@/utils/request'

export function listRooms(params) {
  return request({ url: '/room/list', method: 'get', params })
}

export function getRoom(roomId) {
  return request({ url: `/room/${roomId}`, method: 'get' })
}

export function getTimeSlot(roomId) {
  return request({ url: `/room/getTimeSlot/${roomId}`, method: 'get' })
}

export function addRoom(data) {
  return request({ url: '/room/add', method: 'post', data })
}

export function updateRoom(data) {
  return request({ url: '/room/update', method: 'put', data })
}

export function deleteRoom(id) {
  return request({ url: `/room/delete/${id}`, method: 'delete' })
}
