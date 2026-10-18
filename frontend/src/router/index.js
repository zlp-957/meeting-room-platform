import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  { path: '/reset-password', name: 'ResetPassword', component: () => import('@/views/ResetPassword.vue') },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/rooms',
    children: [
      { path: 'rooms', name: 'RoomList', component: () => import('@/views/RoomList.vue') },
      { path: 'room/:id', name: 'RoomDetail', component: () => import('@/views/RoomDetail.vue') },
      { path: 'my-bookings', name: 'MyBookings', component: () => import('@/views/MyBookings.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') },
      { path: 'admin/users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue'), meta: { role: 'admin' } },
      { path: 'admin/rooms', name: 'RoomManage', component: () => import('@/views/admin/RoomManage.vue'), meta: { role: 'admin' } },
      { path: 'admin/audit', name: 'BookAudit', component: () => import('@/views/admin/BookAudit.vue'), meta: { role: 'admin' } },
      { path: 'admin/records', name: 'BookRecords', component: () => import('@/views/admin/BookRecords.vue'), meta: { role: 'admin' } }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.path === '/register' || to.path === '/reset-password') {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  // 校验管理员路由
  if (to.meta && to.meta.role === 'admin') {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role !== 'admin') {
      next('/rooms')
      return
    }
  }
  next()
})

export default router
