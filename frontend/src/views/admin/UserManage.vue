<template>
  <el-card>
    <div class="filter">
      <el-input v-model="query.username" placeholder="按账号搜索" clearable style="width:200px" @clear="load" @keyup.enter.native="load" />
      <el-button type="primary" icon="el-icon-search" @click="load">查询</el-button>
    </div>
    <el-table :data="list" border style="width:100%;margin-top:16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="角色">
        <template slot-scope="scope">
          <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'info'">
            {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">
            {{ scope.row.status === 0 ? '已禁用' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button size="mini" :type="scope.row.status === 0 ? 'success' : 'warning'" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 0 ? '启用' : '禁用' }}
          </el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="handlePage"
      />
    </div>
  </el-card>
</template>

<script>
import { listUsers, adminUpdateUser, adminDeleteUser } from '@/api/user'

export default {
  name: 'UserManage',
  data() {
    return {
      list: [],
      total: 0,
      query: { page: 1, size: 10, username: '' }
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.query.page = 1
      this.fetch()
    },
    fetch() {
      listUsers(this.query).then(res => {
        this.list = res.data.records || []
        this.total = res.data.total || 0
      })
    },
    handlePage(page) {
      this.query.page = page
      this.fetch()
    },
    toggleStatus(row) {
      const target = row.status === 0 ? 1 : 0
      const action = target === 0 ? '禁用' : '启用'
      this.$confirm(`确定要${action}账号「${row.username}」吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        adminUpdateUser(row.id, { status: target }).then(() => {
          this.$message.success(`${action}成功`)
          this.fetch()
        })
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm(`确定要删除用户「${row.username}」吗？删除后不可恢复。`, '警告', {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger'
      }).then(() => {
        adminDeleteUser(row.id).then(() => {
          this.$message.success('删除成功')
          this.fetch()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.filter {
  display: flex;
  gap: 12px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
