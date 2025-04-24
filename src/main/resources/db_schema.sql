# 检查数据库是否存在，存在则删除
DROP DATABASE IF EXISTS campus_db;

# 检查数据库是否存在，不存在则创建
CREATE DATABASE IF NOT EXISTS campus_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(100) NOT NULL, -- 将存储明文密码 '123456'
    `real_name`   VARCHAR(50)  NULL,
    `gender`      TINYINT      DEFAULT 1 COMMENT '1: 男, 0: 女, 2: 其他',
    `email`       VARCHAR(100),
    `phone`       VARCHAR(20),
    `avatar_url`  VARCHAR(255) DEFAULT 'avatar/default.png',
    `user_type`   VARCHAR(20)  NOT NULL COMMENT '管理员, 教师, 学生',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态（com.campus.enums.StatusEnum）：0-禁用(USER_INACTIVE)，1-正常(USER_ACTIVE)',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建部门表
CREATE TABLE IF NOT EXISTS `department`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(100) NOT NULL,
    `parent_id`       BIGINT,
    `introduction`    VARCHAR(500) NULL,
    `director_id`     BIGINT       NULL, -- 指向 user.id
    `office_location` VARCHAR(255) NULL,
    `contact_phone`   VARCHAR(50)  NULL,
    `contact_email`   VARCHAR(100) NULL,
    `website_url`     VARCHAR(255) NULL,
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `department` (`id`) ON DELETE SET NULL
    -- 注意: director_id 的外键约束建议在 teacher 表创建后添加，或者允许 director_id 指向任何用户类型
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建教师表
CREATE TABLE IF NOT EXISTS `teacher`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL,
    `teacher_no`    VARCHAR(50)  NULL, -- 考虑设为 NOT NULL UNIQUE
    `hire_date`     DATE         NULL,
    `department_id` BIGINT       NOT NULL,
    `title`         VARCHAR(50),
    `research_area` VARCHAR(255) NULL,
    `status` TINYINT DEFAULT 1 COMMENT '通用状态（com.campus.enums.StatusEnum）：0-禁用(USER_INACTIVE)，1-正常(USER_ACTIVE)',
    `create_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_teacher_user_id` (`user_id`),
    UNIQUE KEY `uk_teacher_no` (`teacher_no`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建班级表
CREATE TABLE IF NOT EXISTS `clazz`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(100) NOT NULL COMMENT '班级名称, 如 计算机2301班',
    `grade`         VARCHAR(20)  NULL COMMENT '年级, 如 2023',
    `department_id` BIGINT       NOT NULL COMMENT '所属院系ID',
    `instructor_id` BIGINT       NULL COMMENT '辅导员/班主任教师ID (teacher.id)',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_clazz_name` (`name`), -- 班级名称通常唯一
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`instructor_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
    -- monitor_id 的外键约束建议在 student 表创建后添加
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='行政班级表';


-- 创建学生表
CREATE TABLE IF NOT EXISTS `student`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `student_no`      VARCHAR(50)  NOT NULL,
    `enrollment_date` DATE         NULL, -- 入学日期
    `graduation_date` DATE         NULL, -- 毕业日期
    `department_id`   BIGINT       NOT NULL,
    `major`           VARCHAR(100) NULL, -- 专业名称，如果需要单独管理，可以创建 major 表
    `class_id`        BIGINT       NULL COMMENT '所属行政班级ID (clazz.id)',
    `status` TINYINT DEFAULT 1 COMMENT '学生状态：0-休学，1-在读，2-已毕业（需在枚举类扩展）',
    `create_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_user_id` (`user_id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`class_id`) REFERENCES `clazz` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 为 department 表添加 director_id 的外键约束 (如果 director 可以是任何用户)
ALTER TABLE `department` ADD CONSTRAINT `fk_department_director_user` FOREIGN KEY (`director_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

-- 创建学期表
CREATE TABLE IF NOT EXISTS `term`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `term_name`   VARCHAR(100) NOT NULL COMMENT '学期名称, 如 2023-2024学年第一学期',
    `code`        VARCHAR(50)  NOT NULL COMMENT '学期代码, 如 2023-2024-1',
    `start_date`  DATE         NULL COMMENT '学期开始日期',
    `end_date`    DATE         NULL COMMENT '学期结束日期',
    `current`     TINYINT  DEFAULT 0 COMMENT '是否当前学期: 0-否, 1-是',
    `status` TINYINT DEFAULT 0 COMMENT '学期状态（com.campus.enums.StatusEnum）：0-未开始，1-进行中，2-已结束',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_term_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学期表';

-- 创建教室表
CREATE TABLE IF NOT EXISTS `classroom`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(50)  NOT NULL COMMENT '教室编号, 如 A101',
    `building`    VARCHAR(100) NULL COMMENT '教学楼',
    `capacity`    INT          NOT NULL COMMENT '教室容量',
    `status` TINYINT DEFAULT 1 COMMENT '教室状态：0-禁用，1-正常，2-维护中（需在枚举类扩展）',
    `room_type`   TINYINT  DEFAULT 1 COMMENT '教室类型：1-普通，2-多媒体，3-实验室',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classroom_name_building` (`name`, `building`) -- 同一栋楼教室编号唯一
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 创建课程表
CREATE TABLE IF NOT EXISTS `course`
(
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `course_name`   VARCHAR(100)  NOT NULL,
    `course_code`   VARCHAR(50)   NOT NULL COMMENT '课程代码, 如 CS101',
    `department_id` BIGINT        NULL COMMENT '开课院系ID', -- 建议添加
    `credit`        DECIMAL(3, 1) NOT NULL COMMENT '学分 (允许小数)',
    `hours`         INT           NULL COMMENT '学时', -- 建议添加
    `course_type`   TINYINT   DEFAULT 1 COMMENT '课程类型：1-必修，2-选修, 3-通识 等',
    `introduction`  TEXT          NULL COMMENT '课程简介',
    `teacher_id`    BIGINT        NULL COMMENT '授课教师 ID (可选, 允许多个老师或后续安排)',
    `status` TINYINT DEFAULT 1 COMMENT '课程状态：0-禁用，1-启用',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE SET NULL,
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 创建课程选课表 (学生选课记录)
CREATE TABLE IF NOT EXISTS `course_selection`
(
    `id`             BIGINT NOT NULL AUTO_INCREMENT,
    `student_id`     BIGINT NOT NULL,
    `course_id`      BIGINT NOT NULL,
    `term_id`        BIGINT NOT NULL COMMENT '学期 ID',
    `selection_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `status` TINYINT DEFAULT 1 COMMENT '选课状态：0-已退课，1-已选',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE RESTRICT, -- 学期通常不应删除
    UNIQUE KEY `uk_student_course_term` (`student_id`, `course_id`, `term_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建成绩表
CREATE TABLE IF NOT EXISTS `score`
(
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
    `course_selection_id` BIGINT        NOT NULL COMMENT '关联到具体的选课记录',
    `score_value`         DECIMAL(5, 2) NULL COMMENT '最终成绩分数',
    `grade`               VARCHAR(10)   NULL COMMENT '等级成绩 (A, B+, C等)',
    `gpa`                 DECIMAL(3, 2) NULL COMMENT '绩点',
    `regular_score`       DECIMAL(5, 2) NULL COMMENT '平时成绩',
    `midterm_score`       DECIMAL(5, 2) NULL COMMENT '期中成绩',
    `final_exam_score`    DECIMAL(5, 2) NULL COMMENT '期末考试成绩',
    `comment`             VARCHAR(500)  NULL COMMENT '教师评语',
    `evaluation_date`     DATETIME      NULL COMMENT '成绩评定日期',
    `create_time`         DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_score_selection_id` (`course_selection_id`), -- 一个选课记录只有一条成绩
    FOREIGN KEY (`course_selection_id`) REFERENCES `course_selection` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建课表表 (教学班排课信息)
CREATE TABLE IF NOT EXISTS `schedule`
(
    `id`           BIGINT    NOT NULL AUTO_INCREMENT,
    `course_id`    BIGINT    NOT NULL COMMENT '课程ID',
    `term_id`      BIGINT    NOT NULL COMMENT '学期ID',
    `teacher_id`   BIGINT    NULL COMMENT '授课教师ID (允许为空或后续分配)',
    `classroom_id` BIGINT    NOT NULL COMMENT '教室ID',
    `day_of_week`  VARCHAR(10) NOT NULL COMMENT '星期几',
    `start_time`   TIME      NOT NULL COMMENT '上课开始时间',
    `end_time`     TIME      NOT NULL COMMENT '上课结束时间',
    `start_week`   INT       NULL COMMENT '开始周',
    `end_week`     INT       NULL COMMENT '结束周',
    `week_parity`  TINYINT   NULL COMMENT '周次奇偶性: 0-所有周, 1-奇数周, 2-偶数周',
    `class_id`     BIGINT    NULL COMMENT '关联的行政班级ID (可选)',
    `status` TINYINT DEFAULT 1 COMMENT '排课状态：0-取消，1-正常',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`classroom_id`) REFERENCES `classroom` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`class_id`) REFERENCES `clazz` (`id`) ON DELETE SET NULL
    -- 考虑添加唯一约束防止时间地点冲突
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建活动表
CREATE TABLE IF NOT EXISTS `activity`
(
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
    `title`                VARCHAR(200) NOT NULL COMMENT '活动标题',
    `description`          TEXT         NOT NULL COMMENT '活动描述',
    `type`                 VARCHAR(50)  NULL COMMENT '活动类型：竞赛, 讲座, 服务, 社团, 其他',
    `poster_url`           VARCHAR(255) NULL COMMENT '活动海报URL',
    `organizer_id`         BIGINT       NULL COMMENT '组织者用户ID (user.id)',
    `organizer_name`       VARCHAR(100) NULL COMMENT '组织者名称 (冗余)',
    `publisher_id`         BIGINT       NULL COMMENT '发布者用户ID (user.id)',
    `contact`              VARCHAR(100) NULL COMMENT '活动联系方式',
    `location`             VARCHAR(255) NULL COMMENT '活动地点',
    `start_time`           DATETIME     NOT NULL COMMENT '活动开始时间',
    `end_time`             DATETIME     NOT NULL COMMENT '活动结束时间',
    `registration_deadline` DATETIME    NULL COMMENT '报名截止时间',
    `max_participants`     INT      DEFAULT 0 COMMENT '最大参与人数，0表示不限',
    `current_participants` INT      DEFAULT 0 COMMENT '当前参与人数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已取消, 1-进行中, 2-已结束',
    `create_time`          DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`organizer_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX `idx_activity_type` (`type`),
    INDEX `idx_activity_time` (`start_time`, `end_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 创建活动参与者表
CREATE TABLE IF NOT EXISTS `activity_participant`
(
    `id`                BIGINT   NOT NULL AUTO_INCREMENT,
    `activity_id`       BIGINT   NOT NULL,
    `user_id`           BIGINT   NOT NULL,
    `registration_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已取消, 1-已确认, 2-待定',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_participant` (`activity_id`, `user_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 创建通知表
CREATE TABLE IF NOT EXISTS `notification`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `title`            VARCHAR(200) NOT NULL,
    `content`          TEXT,
    `sender_id`        BIGINT       NULL COMMENT '发送者用户ID (user.id)',
    `publisher_id`     BIGINT       NULL COMMENT '发布者用户ID (user.id)',
    `publisher_name`   VARCHAR(100) NULL COMMENT '发布者名称 (冗余)',
    `type`             VARCHAR(50)  NOT NULL COMMENT '通知类型：系统, 课程, 活动, 通用',
    `priority`         TINYINT  DEFAULT 0 COMMENT '优先级：0-普通，1-重要，2-紧急',
    `target_type`      VARCHAR(50)  NULL COMMENT '目标类型：全体, 部门, 班级, 用户', -- 移除了 Role
    `target_ids`       TEXT         NULL COMMENT '目标ID列表 (逗号分隔或JSON), 配合 target_type 使用',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已撤回，1-正常',
    `is_top`           TINYINT  DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    `view_count`       INT      DEFAULT 0 COMMENT '查看次数',
    `send_time`        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送/计划发送时间',
    `expire_time`      DATETIME     NULL COMMENT '过期时间',
    `attachments_json` TEXT COMMENT '附件列表，JSON格式 [{"name": "file.pdf", "url": "/path/to/file.pdf"}]',
    `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`publisher_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX `idx_notification_type` (`type`),
    INDEX `idx_notification_target` (`target_type`),
    INDEX `idx_notification_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 创建通知接收者表
CREATE TABLE IF NOT EXISTS `notification_receiver`
(
    `id`              BIGINT   NOT NULL AUTO_INCREMENT,
    `notification_id` BIGINT   NOT NULL,
    `receiver_id`     BIGINT   NOT NULL COMMENT '接收者用户ID (user.id)',
    `read_status`     VARCHAR(20) DEFAULT 'Unread' COMMENT '状态：未读, 已读',
    `read_time`       DATETIME NULL COMMENT '阅读时间',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_receiver` (`notification_id`, `receiver_id`),
    FOREIGN KEY (`notification_id`) REFERENCES `notification` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_receiver_read_status` (`receiver_id`, `read_status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 创建论坛帖子表
CREATE TABLE IF NOT EXISTS `post`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `title`         VARCHAR(200) NOT NULL,
    `content`       TEXT         NOT NULL,
    `user_id`       BIGINT       NOT NULL COMMENT '作者用户ID',
    `category`      VARCHAR(50) DEFAULT 'General' COMMENT '帖子分类: 通用, 学习, 生活, 技术, 等',
    `tags`          TEXT         NULL COMMENT '标签，JSON数组 ["tag1", "tag2"]',
    `view_count`    INT         DEFAULT 0 COMMENT '浏览次数',
    `like_count`    INT         DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT         DEFAULT 0 COMMENT '评论数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常，2-隐藏',
    `is_top`        TINYINT     DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    `is_essence`    TINYINT     DEFAULT 0 COMMENT '是否精华：1-是，0-否',
    `creation_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_post_user` (`user_id`),
    INDEX `idx_post_category` (`category`),
    INDEX `idx_post_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建评论表
CREATE TABLE IF NOT EXISTS `comment`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `post_id`     BIGINT   NOT NULL COMMENT '关联的帖子ID',
    `user_id`     BIGINT   NOT NULL COMMENT '评论作者用户ID',
    `content`     TEXT     NOT NULL,
    `parent_id`   BIGINT   NULL COMMENT '父评论ID，为NULL表示顶级评论',
    `like_count`  INT      DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常'
    `creation_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE SET NULL,
    INDEX `idx_comment_post` (`post_id`),
    INDEX `idx_comment_user` (`user_id`),
    INDEX `idx_comment_parent` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;