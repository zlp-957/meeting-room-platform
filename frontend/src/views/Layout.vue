<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">会议室预约平台</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/rooms">
          <i class="el-icon-office-building"></i>
          <span>会议室列表</span>
        </el-menu-item>
        <el-menu-item index="/my-bookings">
          <i class="el-icon-date"></i>
          <span>我的预约</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <i class="el-icon-user"></i>
          <span>个人信息</span>
        </el-menu-item>

        <template v-if="isAdmin">
          <el-submenu index="admin">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>管理后台</span>
            </template>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/rooms">会议室管理</el-menu-item>
            <el-menu-item index="/admin/audit">预约审核</el-menu-item>
            <el-menu-item index="/admin/records">预约记录</el-menu-item>
          </el-submenu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="welcome">{{ user.nickname || user.username }}，欢迎使用</span>
        <el-button type="text" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { logout } from '@/api/user'

export default {
  name: 'Layout',
  computed: {
    user() {
      return JSON.parse(localStorage.getItem('user') || '{}')
    },
    isAdmin() {
      return this.user.role === 'admin'
    }
  },
  methods: {
    handleLogout() {
      // 调用后端登出接口使 Token 失效（B-10），失败也继续清理本地状态
      logout().catch(() => {}).finally(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background-color: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}
.aside .el-menu {
  border-right: none;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
}
.welcome {
  color: #303133;
}
.main {
  background: #f5f7fa;
}
</style>
