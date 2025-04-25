# Campus Management System Backend (SSM)

这是一个基于 SSM (Spring + SpringMVC + MyBatis) 框架构建的校园管理系统后端项目。

## 技术栈

* **后端框架:** Spring, SpringMVC
* **ORM:** MyBatis, Mybatis-Plus
* **数据库:** MySQL (或其他关系型数据库)
* **构建工具:** Maven
* **服务器:** Tomcat (或其他 Servlet 容器)
* **其他:** Lombok, Jackson, SLF4j

## 已实现功能模块

根据当前代码结构和服务接口分析，系统已实现或包含以下核心功能模块：

1. **用户管理 (`UserService`)**
    * 用户（管理员、教师、学生）基本信息 CRUD (增删改查)
    * 用户登录、密码修改、密码重置
    * 按用户名、学工号、类型、状态查询用户
    * 用户状态管理 (激活/禁用)
    * 分页查询用户列表 (支持按关键词、类型筛选)
    * 批量删除用户
    * （部分与教师/学生特定属性相关的查询，如按部门、职称、班级、专业、年级查询，依赖 User 实体字段）

2. **课程管理 (`CourseService`)**
    * 课程信息 CRUD
    * 按课程代码、课程类型查询
    * 分页查询课程列表
    * 批量删除课程
    * 按关键词搜索课程
    * 检查课程代码是否存在

3. **课表管理 (`ScheduleService`)**
    * 课表信息 CRUD
    * 按教师 ID、课程 ID、教室 ID 查询课表
    * 按教师 ID + 学期信息查询课表
    * 按教室 ID + 学期信息查询课表 (Service 层实现)
    * 按用户 ID (学生) 查询课表
    * 批量删除课表
    * 分页查询课表 (支持按学期、课程、教师等筛选)
    * 课表状态管理 (Active/Inactive)
    * **TODO:** 时间冲突检查 (`checkTimeConflict`)
    * **TODO:** 获取周课表 (`getTeacherWeeklySchedule`, `getStudentWeeklySchedule`, `getClassroomWeeklySchedule`)
    * 部分统计功能 (教师/教室/课程/学期课表数量)

4. **选课管理 (`CourseSelectionService`)**
    * 选课记录 CRUD (通过 Service 层方法)
    * 学生选课 (`selectCourse`)
    * 学生退选 (`dropCourse` - 更新状态为 "Dropped")
    * 按学生 ID、课程 ID、学期信息查询选课记录
    * 分页查询选课记录 (支持按用户、课程、学期、状态筛选)
    * 检查学生是否已选某门课
    * 获取课程选课人数

5. **成绩管理 (`ScoreService`)**
    * 成绩记录 CRUD (输入平时/期中/期末分，自动计算总分)
    * 按学生 ID、课程 ID、学期信息查询成绩
    * 按选课 ID 查询成绩
    * 批量录入/删除成绩
    * 获取课程成绩统计 (平均/最高/最低分 - Service 层计算)
    * 计算学生 GPA (Service 层计算)

6. **活动管理 (`ActivityService`)**
    * 活动信息 CRUD
    * 按活动类型、状态查询活动
    * 查询进行中、即将开始的活动
    * 分页查询活动 (支持按关键词、类型、状态筛选)
    * 活动状态管理
    * 批量删除活动

7. **通知公告管理 (`NotificationService`)**
    * 通知信息 CRUD (包含附件信息处理)
    * 按类型、状态、发布者查询通知
    * 查询最新、置顶通知
    * 分页查询通知 (支持按类型、关键词、状态筛选)
    * 通知状态管理
    * 增加阅读次数
    * 发送通知 (给指定用户、按类型、全体 - 基于 JSON 存储接收者)

8. **教室管理 (`ClassroomService`)**
    * 教室信息 CRUD
    * 按教室名称、教学楼、类型查询
    * 分页查询教室 (支持按关键词、教学楼、状态筛选)
    * 获取可用教室 ("Active" 状态)
    * 教室状态管理 (批量更新)
    * **TODO:** 教室可用性检查 (`checkClassroomAvailability`)

## 注意事项

* 部分功能（如 `ScheduleService` 的时间冲突检查和周课表获取，`ClassroomService` 的可用性检查）标记为 **TODO**，需要进一步实现或修复。
* 移除了原先可能存在的基于独立表的用户角色管理、活动报名管理、论坛分类/标签、通知已读未读跟踪等功能。
* 状态字段已统一为 `String` 类型（如 "Active", "Inactive", "Selected", "Dropped" 等）。
* 学期信息使用 `String` 类型的 `termInfo` 字段。
* 密码存储当前为明文，实际部署应使用加密存储。

## 如何运行

1. 配置数据库连接信息 (`src/main/resources/jdbc.properties` 或 Spring 配置文件)。
2. 创建数据库及相关表结构（可能需要提供数据库脚本 `campus.sql`）。
3. 使用 Maven 构建项目 (`mvn clean package`)。
4. 将生成的 WAR 包部署到 Tomcat 或其他 Servlet 容器。 