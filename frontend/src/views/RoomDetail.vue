<template>
  <div>
    <el-card>
      <div class="detail-header">
        <div>
          <h2>{{ room.roomName }}</h2>
          <p class="meta">
            <span><i class="el-icon-location"></i> {{ room.location || '未填写' }}</span>
            <span><i class="el-icon-user"></i> 容纳 {{ room.capacity }} 人</span>
            <span><i class="el-icon-monitor"></i> {{ room.equipment || '无设备信息' }}</span>
          </p>
          <p v-if="room.remark" class="remark">备注：{{ room.remark }}</p>
        </div>
        <el-button @click="$router.push('/rooms')">返回列表</el-button>
      </div>
    </el-card>

    <el-card style="margin-top:16px">
      <h3>预约会议室</h3>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px" style="max-width:480px">
        <el-form-item label="预约日期" prop="bookDate">
          <el-date-picker
            v-model="form.bookDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择预约日期"
            :picker-options="pickerOptions"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="预约时段" prop="timeSlot">
          <el-select v-model="form.timeSlot" placeholder="请先选择日期" style="width:100%" :disabled="!form.bookDate">
            <el-option
              v-for="slot in availableSlots"
              :key="slot"
              :label="slot"
              :value="slot"
              :disabled="occupiedSlots.includes(slot)"
            >
              <span>{{ slot }}</span>
              <span v-if="occupiedSlots.includes(slot)" class="occupied-tag">已占用</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预约标题" prop="bookTitle">
          <el-input v-model="form.bookTitle" placeholder="如：产品评审会" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="参会人数" prop="peopleNum">
          <el-input-number v-model="form.peopleNum" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getRoom } from '@/api/room'
import { addBook, occupiedSlots } from '@/api/book'

export default {
  name: 'RoomDetail',
  data() {
    return {
      roomId: this.$route.params.id,
      room: {},
      allSlots: [],
      occupiedSlots: [],
      form: { bookDate: '', timeSlot: '', bookTitle: '', peopleNum: 1 },
      rules: {
        bookDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
        timeSlot: [{ required: true, message: '请选择预约时段', trigger: 'change' }],
        bookTitle: [{ required: true, message: '请输入预约标题', trigger: 'blur' }],
        peopleNum: [{ required: true, message: '请输入参会人数', trigger: 'blur' }]
      },
      submitting: false,
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
        }
      }
    }
  },
  computed: {
    availableSlots() {
      return this.allSlots
    }
  },
  created() {
    this.loadRoom()
  },
  methods: {
    loadRoom() {
      // 使用详情接口，避免拉取全量列表再匹配（B-18）
      getRoom(this.roomId).then(res => {
        this.room = res.data || {}
        this.allSlots = this.room.availableTimeSlot || []
      })
    },
    handleDateChange(date) {
      this.form.timeSlot = ''
      if (!date) return
      // 查询该会议室当天已通过的预约，标记占用时段
      occupiedSlots({ roomId: this.roomId, bookDate: date }).then(res => {
        this.occupiedSlots = res.data || []
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        // 前端先校验容量，给出明确提示而非强制截断
        const capacity = this.room.capacity || 0
        if (capacity > 0 && this.form.peopleNum > capacity) {
          this.$message.error(`参会人数不能超过会议室容纳人数（${capacity}人）`)
          return
        }
        this.submitting = true
        addBook({
          roomId: this.roomId,
          bookTitle: this.form.bookTitle,
          bookDate: this.form.bookDate,
          timeSlot: this.form.timeSlot,
          peopleNum: this.form.peopleNum
        }).then(() => {
          this.$message.success('预约提交成功，等待审核')
          this.$router.push('/my-bookings')
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.detail-header h2 {
  margin: 0 0 12px;
}
.meta span {
  margin-right: 20px;
  color: #606266;
}
.meta i {
  color: #909399;
  margin-right: 4px;
}
.remark {
  color: #909399;
  font-size: 13px;
}
.occupied-tag {
  float: right;
  color: #f56c6c;
  font-size: 12px;
}
</style>
