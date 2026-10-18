<template>
  <div>
    <el-card>
      <div class="header">
        <span>会议室管理</span>
        <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增会议室</el-button>
      </div>
      <el-table :data="list" border style="width:100%;margin-top:16px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roomName" label="名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容纳人数" />
        <el-table-column prop="equipment" label="设备" />
        <el-table-column label="可预约时段">
          <template slot-scope="scope">
            <el-tag v-for="s in scope.row.availableTimeSlot" :key="s" size="mini" style="margin:2px">{{ s }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template slot-scope="scope">
            <el-button size="mini" @click="openEdit(scope.row)">编辑</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="roomName">
          <el-input v-model="form.roomName" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="设备" prop="equipment">
          <el-input v-model="form.equipment" placeholder="如：投影仪、白板" />
        </el-form-item>
        <el-form-item label="可预约时段" prop="availableTimeSlot">
          <el-select v-model="form.availableTimeSlot" multiple placeholder="多选可预约时段" style="width:100%">
            <el-option v-for="s in timeSlotOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRooms, addRoom, updateRoom, deleteRoom } from '@/api/room'

export default {
  name: 'RoomManage',
  data() {
    return {
      list: [],
      total: 0,
      query: { page: 1, size: 10 },
      dialogVisible: false,
      dialogTitle: '新增会议室',
      saving: false,
      form: { id: null, roomName: '', location: '', capacity: 10, equipment: '', availableTimeSlot: [], remark: '' },
      rules: {
        roomName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
        availableTimeSlot: [{ required: true, type: 'array', min: 1, message: '至少选择一个时段', trigger: 'change' }]
      },
      timeSlotOptions: [
        '08:00-08:45', '09:00-09:45', '10:00-10:45', '11:00-11:45',
        '14:00-14:45', '15:00-15:45', '16:00-16:45', '17:00-17:45'
      ]
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
    openAdd() {
      this.dialogTitle = '新增会议室'
      this.form = { id: null, roomName: '', location: '', capacity: 10, equipment: '', availableTimeSlot: [], remark: '' }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    openEdit(row) {
      this.dialogTitle = '编辑会议室'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.saving = true
        const req = this.form.id ? updateRoom(this.form) : addRoom(this.form)
        req.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.fetch()
        }).finally(() => {
          this.saving = false
        })
      })
    },
    handleDelete(row) {
      this.$confirm(`确定删除会议室「${row.roomName}」吗？`, '提示', { type: 'warning' }).then(() => {
        deleteRoom(row.id).then(() => {
          this.$message.success('删除成功')
          this.fetch()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
