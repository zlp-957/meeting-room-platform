# 会议室预约平台

企业内部会议室在线预约系统，前后端分离架构。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Spring Boot 2.7.x + MyBatis-Plus + MySQL 8.0 + Sa-Token + Lombok + Hutool + Validation + Knife4j |
| 前端 | Vue2 + Element UI + Vue Router + Axios |
| 构建 | Maven（后端） / npm（前端） |

## 项目结构

```
会议室预约平台/
├── backend/                 # SpringBoot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/meetingroom/
│       │   ├── MeetingRoomApplication.java
│       │   ├── config/      # Sa-Token、MyBatis-Plus、CORS、Knife4j
│       │   ├── controller/  # UserController、RoomController、BookController
│       │   ├── service/     # 业务逻辑（含预约冲突校验）
│       │   ├── mapper/      # MyBatis-Plus Mapper
│       │   ├── entity/      # User、MeetingRoom、MeetingBook
│       │   ├── dto/         # 入参 DTO
│       │   ├── vo/          # 返回 VO
│       │   └── common/      # Result 统一返回、全局异常处理
│       └── resources/
│           ├── application.yml
│           └── db/init.sql  # 建表 + 初始化数据
└── frontend/                # Vue2 前端
    ├── package.json
    ├── vue.config.js
    └── src/
        ├── main.js
        ├── router/          # 路由 + 权限守卫
        ├── utils/request.js # axios 封装
        ├── api/             # user.js、room.js、book.js
        └── views/           # 页面（登录、会议室、预约、管理后台）
```

## 环境要求

| 软件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 8 | 本机已装 Oracle JDK 8（`C:\Program Files\Java\jdk1.8.0_361`） |
| Maven | 3.9.9 | 已内置到项目 `tools/` 目录，无需单独安装 |
| MySQL | 8.0.17 | 本机已装（`C:\AppServ\MySQL`，服务名 `mysql8`） |
| Node.js | 14+ | 本机已装 Node 22 |

> Maven 无需安装，已下载到 `tools/apache-maven-3.9.9`，并使用 `tools/settings.xml`（阿里云镜像加速）。

## 启动步骤

### 1. 初始化数据库（一次性）

用你的 MySQL 客户端（或命令行）执行以下 SQL：

```sql
-- 建库
CREATE DATABASE meeting_room DEFAULT CHARACTER SET utf8mb4;
```

然后导入初始化脚本 `backend/src/main/resources/db/init.sql`（建表 + 初始数据）：

```bash
"C:\AppServ\MySQL\bin\mysql.exe" -u root -p meeting_room < backend/src/main/resources/db/init.sql
```

> 导入前，请先把 `backend/src/main/resources/application.yml` 里的 `password` 改成你的 MySQL root 真实密码。

### 2. 启动后端

双击项目根目录的 **`启动后端.bat`**（自动使用内置 Maven）。

或手动执行：

```bash
cd backend
..\tools\apache-maven-3.9.9\bin\mvn -s ..\tools\settings.xml spring-boot:run
```

后端启动于 `http://localhost:8080`，接口文档 Knife4j：`http://localhost:8080/doc.html`

### 3. 启动前端

双击项目根目录的 **`启动前端.bat`**（首次会自动安装依赖）。

或手动执行：

```bash
cd frontend
npm install
npm run serve
```

前端启动于 `http://localhost:8081`（已配置代理，`/api` 转发到后端 8080）。

访问 `http://localhost:8081` 即可使用。

## 默认账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | 管理员 |
| user | 123456 | 普通用户 |

## 核心业务规则

1. 管理员为每个会议室配置固定可预约时段（如 `08:00-08:45`），用户只能在预设时段内选择
2. 同一会议室 + 同一天 + 同一时段，只能存在一条**已通过**预约
3. 用户提交预约后状态为【待审核】，管理员审核通过/驳回
4. **待审核、已通过**的预约，用户均可自行取消；取消后时段释放
5. 不能选择已经过期的日期预约

## 接口一览

| 模块 | 接口 | 说明 | 权限 |
| --- | --- | --- | --- |
| 用户 | POST /user/register | 注册 | 开放 |
| 用户 | POST /user/login | 登录 | 开放 |
| 用户 | GET /user/info | 当前用户信息 | 登录 |
| 用户 | PUT /user/updateInfo | 修改个人信息 | 登录 |
| 用户 | GET /user/list | 用户列表 | admin |
| 会议室 | GET /room/list | 分页查询 | 登录 |
| 会议室 | GET /room/getTimeSlot/{id} | 获取时段 | 登录 |
| 会议室 | POST /room/add | 新增 | admin |
| 会议室 | PUT /room/update | 修改 | admin |
| 会议室 | DELETE /room/delete/{id} | 删除 | admin |
| 预约 | POST /book/add | 提交预约 | 登录 |
| 预约 | PUT /book/audit | 审核 | admin |
| 预约 | PUT /book/cancel/{id} | 取消 | 本人 |
| 预约 | GET /book/list | 本人记录 | 登录 |
| 预约 | GET /book/all | 全部记录 | admin |
