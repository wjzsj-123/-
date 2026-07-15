# 在线题库与组卷练习平台

面向课程学习与自我测评的 学生自主学习平台 Web 应用，支持私有题库维护、Excel 批量导入、随机/定制组卷、在线答题、公共题库与试卷分享、学习计划、用户中心与消息通知等功能。

采用 **前后端分离** 架构：前端 Vue 3 + Vite，后端 Spring Boot + MyBatis，数据存储 MySQL。开发时可前后端分端口联调；生产可将前端构建产物并入后端，单端口访问。

---

## 功能概览

| 模块 | 说明 |
|------|------|
| 账号 | 注册、登录、个人信息维护 |
| 题库管理 | 新建/编辑/删除题库，单选/多选/填空题维护，Excel 导入导出 |
| 试卷管理 | 随机组卷、定制组卷、答题、成绩与解析、重做 |
| 在线题库 | 浏览公共题库、点赞点踩、导入为私有、讨论区 |
| 在线试卷 | 浏览公共试卷、复制为我的试卷 |
| 学习计划 | 按题库制定每日题量、刷题、错题复习 |
| 用户中心 | 公开资源展示、关注/粉丝 |
| 消息中心 | 关注动态、未读提醒 |

**热度算法（公共资源排序）**

- 公共题库：`点赞数 × 2 + 被导入次数 × 3`
- 公共试卷：`作答人数 × 2 + 被复制次数 × 3`

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3、Vue Router、Vite、Element Plus |
| 后端 | Spring Boot 2.6、MyBatis |
| 数据库 | MySQL 5.7+ / 8.x |
| 其他 | EasyExcel（题目导入）、Lombok |

---

## 目录结构

```
实践/
├── frontend/                 # Vue 3 前端工程
│   ├── src/views/            # 页面组件
│   ├── src/router/           # 路由（Hash 模式）
│   └── vite.config.js        # 开发代理 /api → 8080
├── demo/                     # Spring Boot 后端工程
│   ├── src/main/java/        # Controller / Service / Mapper
│   ├── src/main/resources/
│   │   ├── application.yml   # 数据源、MyBatis 配置
│   │   ├── application.properties  # 端口等
│   │   ├── mapper/*.xml      # SQL 映射
│   │   └── static/dist/      # 前端构建产物（一体化部署）
│   └── pom.xml
└── README.md
```

---

## 环境要求

- **JDK 8+**
- **Maven 3.6+**
- **Node.js 18+**（建议 LTS）与 **npm**
- **MySQL** 已安装并启动

---

## 快速开始（开发模式）

开发模式推荐 **前后端分开启动**：前端 `5173`，后端 `8080`，由 Vite 代理 API。

### 1. 准备数据库

在 MySQL 中创建库（名称与配置一致即可，默认示例为 `lp`）：

```sql
CREATE DATABASE IF NOT EXISTS lp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改后端数据源配置：`demo/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true
    username: root
    password: 你的密码
```

> **说明**：首次启动后端时，`DatabaseSchemaInitializer` 会自动执行表结构自检（`CREATE TABLE IF NOT EXISTS`、补列、补索引），一般无需手工导入 SQL 脚本。

### 2. 启动后端

```bash
cd demo
mvn spring-boot:run
```

或在 IDE 中运行主类：`com.example.demo.DemoApplication`。

默认端口见 `demo/src/main/resources/application.properties`：`server.port=8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问：**http://localhost:5173/#/login**

- 无账号请先访问 `/#/register` 注册
- 登录后进入 `/#/home`

---

## 生产部署（一体化）

将前端打包后放入后端 `static/dist`，仅启动 Spring Boot，通过 **8080** 同时提供页面与 API。

### 1. 构建前端

```bash
cd frontend
npm install
npm run build
```

产物目录：`frontend/dist/`。

### 2. 复制到后端静态资源目录

将 `frontend/dist/` 下全部文件覆盖到：

`demo/src/main/resources/static/dist/`

**Windows（PowerShell）示例：**

```powershell
Remove-Item -Recurse -Force demo\src\main\resources\static\dist\* -ErrorAction SilentlyContinue
Copy-Item -Recurse -Force frontend\dist\* demo\src\main\resources\static\dist\
```

**Linux / macOS 示例：**

```bash
rm -rf demo/src/main/resources/static/dist/*
cp -r frontend/dist/* demo/src/main/resources/static/dist/
```

### 3. 打包并运行后端

```bash
cd demo
mvn clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

若 `pom.xml` 中 Spring Boot 打包插件被跳过，可直接在 IDE 运行 `DemoApplication`，或使用：

```bash
mvn spring-boot:run
```

### 4. 访问地址

- 首页/登录：**http://localhost:8080/** 或 **http://localhost:8080/dist/index.html**
- 前端路由由 `FrontendRouteController` 转发至 `dist/index.html`（支持 `/`、`/login`、`/register`、`/home/**`）

> 前端使用 **Hash 路由**，地址栏形如 `http://localhost:8080/#/home`。

---

## 配置说明

| 文件 | 作用 |
|------|------|
| `demo/src/main/resources/application.yml` | MySQL 连接、MyBatis、静态资源路径 |
| `demo/src/main/resources/application.properties` | 服务端口（默认 8080） |
| `frontend/vite.config.js` | 开发服务器端口（5173）、`/api` 代理目标 |

**接口约定**：REST 路径前缀 `/api/*`，统一响应体 `Result`（`code === 0` 表示成功）。

**登录态**：登录成功后用户信息存于浏览器 `localStorage`（键名 `userInfo`），访问 `/home` 下路由需已登录。

---

## 常见问题

### 启动后端报数据库连接失败

- 确认 MySQL 已启动
- 检查库名、用户名、密码、时区参数是否与 `application.yml` 一致
- 确认已执行 `CREATE DATABASE`

### 题库列表报「查询异常」或接口 500

- 重启后端，确保 `DatabaseSchemaInitializer` 已执行（补全 `question_set_vote`、`source_public_set_id` 等）
- 查看控制台 SQL 日志（`application.yml` 中已开启 MyBatis 日志时可定位具体语句）

### 前端能开页面但接口失败

- 开发模式：确认后端 `8080` 已启动，且访问的是 `5173` 端口（走 Vite 代理）
- 生产模式：确认 `static/dist` 已更新为最新 `npm run build` 产物

### 修改前端后生产环境无变化

- 重新执行 `npm run build` 并复制到 `demo/src/main/resources/static/dist/`，再重启后端

---

## 安全提示（上线前建议）

当前版本面向课程/演示场景：

- 数据库账号密码写在配置文件中，生产环境请改为环境变量或外部配置中心
- 登录态依赖前端 `localStorage`，生产环境建议升级为 JWT 或 Session + 服务端鉴权
- 用户密码存储策略请按实际安全要求加固（如 BCrypt 加盐）

---

## 主要 API 前缀（便于联调）

| 前缀 | 说明 |
|------|------|
| `/api/user` | 注册、登录、用户 CRUD |
| `/api/question-set` | 题库、公共库、导入导出 |
| `/api/question` | 题目 |
| `/api/paper` | 试卷、组卷、公共试卷 |
| `/api/answer` | 答题暂存与提交 |
| `/api/study-plan` | 学习计划 |
| `/api/user-center` | 用户中心、关注 |
| `/api/messages` | 消息中心 |

---

## 许可证

本项目为实践/课程用途，具体授权以仓库说明为准。
