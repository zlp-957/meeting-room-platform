import Vue from 'vue'
import App from './App.vue'
import router from './router'

// Element UI 按需引入（B-19）：显著减小 chunk-vendors 体积
import {
  Aside,
  Button,
  Card,
  Container,
  DatePicker,
  Dialog,
  Empty,
  Form,
  FormItem,
  Header,
  Input,
  InputNumber,
  Main,
  Menu,
  MenuItem,
  Submenu,
  Option,
  Pagination,
  Select,
  Table,
  TableColumn,
  Tag,
  Message,
  MessageBox
} from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

const components = [
  Aside, Button, Card, Container, DatePicker, Dialog, Empty,
  Form, FormItem, Header, Input, InputNumber, Main,
  Menu, MenuItem, Submenu, Option, Pagination, Select,
  Table, TableColumn, Tag
]
components.forEach(c => Vue.use(c))
// JS 插件式调用
// 注意：Message/MessageBox 是函数而非 Vue 插件，不能用 Vue.use()
// （Vue.use 对函数会直接调用它，导致应用启动时弹出空白弹窗/空提示）
Vue.prototype.$message = Message
Vue.prototype.$confirm = MessageBox.confirm

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
