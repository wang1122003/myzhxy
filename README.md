# 智慧校园服务系统

## 项目介绍
智慧校园服务系统是一个面向学校师生的综合服务平台，提供课表管理、成绩管理、通知公告、论坛交流等功能。

## 项目结构
本项目采用前后端分离的架构：
- 后端：基于 Spring MVC + MyBatis Plus 的 Java Web 应用
- 前端：基于 Vue.js + Element Plus 的单页面应用

## 开发环境配置

### 后端开发
1. Java 21+
2. Maven 3.8+
3. MySQL 8.0+

### 前端开发
1. Node.js 20+
2. npm 10+

## 项目部署

### 后端部署
1. 使用 Maven 构建后端项目
   ```bash
   mvn clean package
   ```
2. 将生成的 war 包部署到 Tomcat 10+ 或其他 Servlet 容器中

### 前端部署
1. 进入前端项目目录
   ```bash
   cd campus-app
   ```
2. 安装依赖
   ```bash
   npm install
   ```
3. 构建前端项目
   ```bash
   npm run build
   ```
4. 将生成的 dist 目录部署到 Web 服务器（如 Nginx）

## 前后端接口说明
- 后端接口统一前缀：`/api`
- 前端配置了 API 请求的 baseURL：`http://localhost:8888/campus/api`（开发环境）
- 如需修改后端接口地址，请在前端 `campus-app/src/utils/request.js` 中修改 baseURL

## 开发环境启动

### 后端启动
使用你喜欢的 IDE 启动 Spring MVC 应用（如 IntelliJ IDEA）

### 前端启动
```bash
cd campus-app
npm run dev
```

## 项目维护说明
本项目前后端已完全分离，不再使用 Maven 前端插件进行一体化构建，前后端可以分别独立开发和部署。

## 修复记录

### 2025-04-24
1. 分离前后端构建过程，移除 `frontend-maven-plugin`
2. 修复路由配置，将 `/forum/post/:id` 路径指向 `../views/forum/PostDetail.vue`
3. 修复 API 导入路径，将 `@/api/forum` 改为 `@/api/post`：
   - 修复 `forum/Create.vue` 中的 API 导入
   - 修复 `forum/Index.vue` 中的 API 导入
   - 修复 `components/forum/CreatePost.vue` 中的 API 导入
4. 更新 `WebMvcConfig` 配置，正确处理 API 请求