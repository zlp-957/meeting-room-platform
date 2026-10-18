<template>
  <el-card>
    <div class="filter">
      <el-select v-model="query.auditStatus" placeholder="审核状态" clearable style="width:140px" @change="load">
        <el-option label="待审核" value="wait" />
        <el-option label="已通过" value="pass" />
        <el-option label="已驳回" value="reject" />
        <el-option label="已取消" value="cancel" />
      </el-select>
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
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <template v-if="scope.row.auditStatus === 'wait'">
            <el-button size="mini" type="success" @click="handlePass(scope.row)">通过</el-button>
            <el-button size="mini" type="danger" @click="handleReject(scope.row)">驳回</el-button>
          </template>
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
import { listAll, auditBook } from '@/api/book'

export default {
  name: 'BookAudit',
  data() {
    return {
      list: [],
      total: 0,
      query: { page: 1, size: 10, auditStatus: 'wait' }
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
    handlePass(row) {
      this.$confirm(`确定通过预约「${row.bookTitle}」吗？`, '提示', { type: 'success' }).then(() => {
        auditBook({ bookId: row.id, auditStatus: 'pass' }).then(() => {
          this.$message.success('已通过')
          this.fetch()
        })
      }).catch(() => {})
    },
    handleReject(row) {
      this.$prompt('请输入驳回原因', '驳回预约', {
        inputValidator: v => !!v || '请填写驳回原因'
      }).then(({ value }) => {
        auditBook({ bookId: row.id, auditStatus: 'reject', rejectReason: value }).then(() => {
          this.$message.success('已驳回')
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
