<template>
  <div>
    <el-card style="max-width:600px">
      <h3>个人信息</h3>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag>{{ form.role === 'admin' ? '管理员' : '普通用户' }}</el-tag>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="max-width:600px;margin-top:16px">
      <h3>修改密码</h3>
      <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getInfo, updateInfo, updatePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: { username: '', role: '', nickname: '', realName: '', phone: '' },
      rules: {
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      },
      loading: false,
      pwdLoading: false
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      getInfo().then(res => {
        this.form = res.data
      })
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true
        updateInfo({
          nickname: this.form.nickname,
          realName: this.form.realName,
          phone: this.form.phone
        }).then(() => {
          this.$message.success('保存成功')
          // 同步更新本地缓存的用户信息
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          user.nickname = this.form.nickname
          user.realName = this.form.realName
          user.phone = this.form.phone
          localStorage.setItem('user', JSON.stringify(user))
        }).finally(() => {
          this.loading = false
        })
      })
    },
    handleChangePassword() {
      this.$refs.pwdForm.validate(valid => {
        if (!valid) return
        this.pwdLoading = true
        updatePassword({
          oldPassword: this.pwdForm.oldPassword,
          newPassword: this.pwdForm.newPassword
        }).then(() => {
          this.$message.success('密码修改成功，请重新登录')
          this.$refs.pwdForm.resetFields()
          // 修改密码后强制重新登录
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          this.$router.push('/login')
        }).finally(() => {
          this.pwdLoading = false
        })
      })
    }
  }
}
</script>
