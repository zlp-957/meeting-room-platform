<template>
  <div>
    <el-card>
      <div class="filter">
        <el-input v-model.number="query.capacity" placeholder="最小容纳人数" clearable style="width:180px" @clear="load" @keyup.enter.native="load" />
        <el-button type="primary" icon="el-icon-search" @click="load">查询</el-button>
      </div>
    </el-card>

    <el-card style="margin-top:16px">
      <div class="room-grid">
        <el-card v-for="room in list" :key="room.id" class="room-card" shadow="hover">
          <div class="room-name">{{ room.roomName }}</div>
          <div class="room-info">
            <p><i class="el-icon-location"></i> {{ room.location || '未填写' }}</p>
            <p><i class="el-icon-user"></i> 容纳 {{ room.capacity }} 人</p>
            <p><i class="el-icon-monitor"></i> {{ room.equipment || '无设备信息' }}</p>
          </div>
          <el-button type="primary" size="small" @click="goDetail(room)">查看时段预约</el-button>
        </el-card>
        <el-empty v-if="list.length === 0" description="暂无会议室" />
      </div>
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
  </div>
</template>

<script>
import { listRooms } from '@/api/room'

export default {
  name: 'RoomList',
  data() {
    return {
      list: [],
      total: 0,
      query: { page: 1, size: 9, capacity: null }
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
      listRooms(this.query).then(res => {
        this.list = res.data.records || []
        this.total = res.data.total || 0
      })
    },
    handlePage(page) {
      this.query.page = page
      this.fetch()
    },
    goDetail(room) {
      this.$router.push(`/room/${room.id}`)
    }
  }
}
</script>

<style scoped>
.filter {
  display: flex;
  gap: 12px;
}
.room-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.room-card {
  cursor: pointer;
}
.room-name {
  font-size: 17px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}
.room-info p {
  color: #606266;
  font-size: 13px;
  margin: 6px 0;
}
.room-info i {
  color: #909399;
  margin-right: 4px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
