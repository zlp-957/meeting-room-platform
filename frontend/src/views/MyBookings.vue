<template>
  <el-card>
    <el-table :data="list" border style="width:100%">
      <el-table-column prop="roomName" label="会议室" />
      <el-table-column prop="bookTitle" label="预约主题" />
      <el-table-column prop="bookDate" label="预约日期" />
      <el-table-column prop="timeSlot" label="时段" />
      <el-table-column prop="peopleNum" label="人数" />
      <el-table-column label="状态">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.auditStatus)">{{ statusText(scope.row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回原因" />
      <el-table-column prop="createTime" label="提交时间" />
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.auditStatus === 'wait' || scope.row.auditStatus === 'pass'"
            type="danger"
            size="mini"
            @click="handleCancel(scope.row)"
          >取消预约</el-button>
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
import { listMine, cancelBook } from '@/api/book'

export default {
  name: 'MyBookings',
  data() {
    return {
      list: [],
      total: 0,
      query: { page: 1, size: 10 }
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      listMine(this.query).then(res => {
        this.list = res.data.records || []
        this.total = res.data.total || 0
      })
    },
    handlePage(page) {
      this.query.page = page
      this.load()
    },
    handleCancel(row) {
      this.$confirm(`确定取消预约「${row.bookTitle}」吗？`, '提示', { type: 'warning' }).then(() => {
        cancelBook(row.id).then(() => {
          this.$message.success('取消成功')
          this.load()
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
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
