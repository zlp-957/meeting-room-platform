<template>
  <el-card>
    <div class="filter">
      <el-select v-model="query.auditStatus" placeholder="审核状态" clearable style="width:140px" @change="load">
        <el-option label="待审核" value="wait" />
        <el-option label="已通过" value="pass" />
        <el-option label="已驳回" value="reject" />
        <el-option label="已取消" value="cancel" />
      </el-select>
      <el-date-picker
        v-model="query.bookDate"
        type="date"
        value-format="yyyy-MM-dd"
        placeholder="预约日期"
        style="width:160px"
        @change="load"
      />
      <el-button type="primary" icon="el-icon-search" @click="load">查询</el-button>
    </div>
    <el-table :data="list" border style="width:100%;margin-top:16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="nickname" label="预约人" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="roomName" label="会议室" />
      <el-table-column prop="bookTitle" label="主题" />
      <el-table-column prop="bookDate" label="日期" />
      <el-table-column prop="timeSlot" label="时段" />
      <el-table-column prop="peopleNum" label="人数" />
      <el-table-column label="状态">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.auditStatus)">{{ statusText(scope.row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回原因" />
      <el-table-column prop="createTime" label="提交时间" />
      <el-table-column label="操作" width="100">
        <template slot-scope="scope">
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
import { listAll, deleteBook } from '@/api/book'

export default {
  name: 'BookRecords',
  data() {
    return {
      list: [],
      total: 0,
      query: { page: 1, size: 10, auditStatus: '', bookDate: '' }
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
      listAll(this.query).then(res => {
        this.list = res.data.records || []
        this.total = res.data.total || 0
      })
    },
    handlePage(page) {
      this.query.page = page
      this.fetch()
    },
    handleDelete(row) {
      this.$confirm(`确定删除预约「${row.bookTitle}」吗？此操作不可恢复`, '提示', { type: 'warning' }).then(() => {
        deleteBook(row.id).then(() => {
          this.$message.success('删除成功')
          this.fetch()
        })
      }).catch(() => {})
    },
    statusText(status) {
      const map = { wait: '待审核', pass: '已通过', reject: '已驳回', cancel: '已取消' }
      return map[status] || status
    },
    statusType(status) {
      const map = { wait: 'warning', pass: 'success', reject: 'danger', cancel: 'info' }
      return map[status] || 'info'
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
