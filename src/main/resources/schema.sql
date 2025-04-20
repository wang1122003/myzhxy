# 删除现有数据库
DROP DATABASE IF EXISTS campus_db;

# 创建数据库
CREATE DATABASE IF NOT EXISTS campus_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `real_name`  VARCHAR(50)  NULL,
    `gender` TINYINT DEFAULT 1,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `avatar_url` VARCHAR(255) NULL,
    `user_type`  VARCHAR(20)  NOT NULL,
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'Active',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(200),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建部门表
CREATE TABLE IF NOT EXISTS `department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT,
    `introduction`    VARCHAR(500) NULL,
    `director_id`     BIGINT       NULL,
    `office_location` VARCHAR(255) NULL,
    `contact_phone`   VARCHAR(50)  NULL,
    `contact_email`   VARCHAR(100) NULL,
    `website_url`     VARCHAR(255) NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `department` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建教师表
CREATE TABLE IF NOT EXISTS `teacher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `teacher_no`    VARCHAR(50)  NULL,
    `hire_date`     DATE         NULL,
    `department_id` BIGINT NOT NULL,
    `title` VARCHAR(50),
    `research_area` VARCHAR(255) NULL,
    `status`        VARCHAR(20) DEFAULT 'Active',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_teacher_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建班级表 (新增)
CREATE TABLE IF NOT EXISTS `clazz`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(100) NOT NULL COMMENT '班级名称, 如 计算机2301班',
    `grade`         VARCHAR(20)  NULL COMMENT '年级, 如 2023',
    `department_id` BIGINT       NOT NULL COMMENT '所属院系ID',
    `instructor_id` BIGINT       NULL COMMENT '辅导员/班主任教师ID',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_clazz_name` (`name`), -- 班级名称通常唯一
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`instructor_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='行政班级表';

-- 创建学生表
CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `student_no`      VARCHAR(50)  NOT NULL,
    `enrollment_date` DATE         NULL,
    `graduation_date` DATE         NULL,
    `department_id` BIGINT NOT NULL,
    `major`           VARCHAR(100) NULL,
    `class_id`        BIGINT       NULL COMMENT '所属班级ID',
    `status`          VARCHAR(20) DEFAULT 'Active',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_user_id` (`user_id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`class_id`) REFERENCES `clazz` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建学期表 (Moved Before Schedule and Course Selection)
CREATE TABLE IF NOT EXISTS `term`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `term_name`   VARCHAR(100) NOT NULL COMMENT '学期名称, 如 2023-2024学年第一学期',
    `code`        VARCHAR(50)  NOT NULL COMMENT '学期代码, 如 2023-2024-1',
    `start_date`  DATE         NULL COMMENT '学期开始日期',
    `end_date`    DATE         NULL COMMENT '学期结束日期',
    `current`     TINYINT  DEFAULT 0 COMMENT '是否当前学期: 0-否, 1-是',
    `status`      TINYINT  DEFAULT 0 COMMENT '学期状态: 0-未开始, 1-进行中, 2-已结束',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_term_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学期表';

-- 创建教室表
CREATE TABLE IF NOT EXISTS `classroom` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name`      VARCHAR(50)  NOT NULL COMMENT '教室编号',
    `building`  VARCHAR(100) NULL COMMENT '教学楼',
    `capacity`  INT          NOT NULL COMMENT '教室容量',
    `status`    TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `room_type` TINYINT DEFAULT 1 COMMENT '教室类型：1-普通，2-多媒体，3-实验室',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classroom_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建课程表
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `course_name`  VARCHAR(100)  NOT NULL,
    `course_no`    VARCHAR(50)   NOT NULL COMMENT '旧的 code 字段，保留兼容性或用于显示',
    `course_code`  VARCHAR(50)   NOT NULL COMMENT '新的课程代码字段',
    `credit`       DECIMAL(3, 1) NOT NULL COMMENT '学分 (允许小数)',
    `course_type`  TINYINT DEFAULT 1 COMMENT '课程类型：1-必修课，2-选修课',
    `introduction` TEXT          NULL COMMENT '课程简介',
    `teacher_id`   BIGINT        NOT NULL COMMENT '授课教师 ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT,
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建课程选课表
CREATE TABLE IF NOT EXISTS `course_selection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `term_id`        BIGINT NULL COMMENT '学期 ID',
    `selection_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `status`         TINYINT  DEFAULT 1 COMMENT '状态：1-已选，0-已退选',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE SET NULL,
    UNIQUE KEY `uk_student_course_term` (`student_id`, `course_id`, `term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建成绩表
CREATE TABLE IF NOT EXISTS `score` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `term`          VARCHAR(50)   NULL COMMENT '学期，例如 2023-2024-1',
    `regular_score` DECIMAL(5, 2) NULL COMMENT '平时成绩',
    `midterm_score` DECIMAL(5, 2) NULL COMMENT '期中成绩',
    `final_score`   DECIMAL(5, 2) NULL COMMENT '期末成绩',
    `total_score`   DECIMAL(5, 2) NULL COMMENT '总成绩',
    `gpa`           DECIMAL(3, 2) NULL COMMENT '绩点',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_student_course_term_score` (`student_id`, `course_id`, `term`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建课表表
CREATE TABLE IF NOT EXISTS `schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `course_id` BIGINT NOT NULL,
    `classroom_id` BIGINT NOT NULL,
    `teacher_id` BIGINT  NOT NULL COMMENT '教师ID',
    `week_day`   TINYINT NOT NULL COMMENT '星期几 (1-7)',
    `start_time` TIME    NOT NULL COMMENT '开始时间',
    `end_time`   TIME    NOT NULL COMMENT '结束时间',
    `start_week` INT     NOT NULL COMMENT '开始周',
    `end_week`   INT     NOT NULL COMMENT '结束周',
    `term_id`    BIGINT  NULL COMMENT '学期ID',
    `class_id`   BIGINT  NULL COMMENT '班级ID',
    `status`     TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`classroom_id`) REFERENCES `classroom` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`term_id`) REFERENCES `term` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`class_id`) REFERENCES `clazz` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建活动表
CREATE TABLE IF NOT EXISTS `activity` (
                                          `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
                                          `title`                VARCHAR(200) NOT NULL COMMENT '活动标题',
                                          `description`          TEXT         NOT NULL COMMENT '活动描述',
                                          `activity_type`        TINYINT      NULL COMMENT '活动类型：1-娱乐比赛，2-公益服务，3-学术讲座，4-社团活动，5-其他',
                                          `poster_url`           VARCHAR(255) NULL COMMENT '活动海报URL',
                                          `organizer_id`         BIGINT       NOT NULL COMMENT '组织者用户ID',
                                          `organizer_name`       VARCHAR(100) NULL COMMENT '组织者姓名 (冗余)',
                                          `publisher_id`         BIGINT       NULL COMMENT '发布者用户ID',
                                          `contact`              VARCHAR(100) NULL COMMENT '活动联系方式',
                                          `location`             VARCHAR(255) NULL COMMENT '活动地点',
                                          `start_time`           DATETIME     NOT NULL COMMENT '活动开始时间',
                                          `end_time`             DATETIME     NOT NULL COMMENT '活动结束时间',
                                          `max_participants`     INT      DEFAULT 0 COMMENT '最大参与人数，0表示不限',
                                          `current_participants` INT      DEFAULT 0 COMMENT '当前参与人数',
                                          `status`               TINYINT  DEFAULT 1 COMMENT '状态：1-正常，0-取消',
                                          `create_time`          DATETIME DEFAULT CURRENT_TIMESTAMP,
                                          `update_time`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`),
                                          INDEX `idx_activity_organizer` (`organizer_id`),
                                          INDEX `idx_activity_publisher` (`publisher_id`),
                                          INDEX `idx_activity_time` (`start_time`, `end_time`),
                                          FOREIGN KEY (`organizer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                                          FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='活动表';

-- 创建活动参与者关联表
CREATE TABLE IF NOT EXISTS `activity_participant`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id`     BIGINT NOT NULL COMMENT '参与用户ID',
    `join_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='活动参与者关联表';

-- 创建通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `sender_id`      BIGINT       NOT NULL COMMENT '发送者用户ID (兼容旧字段)',
    `publisher_id`   BIGINT       NULL COMMENT '发布者用户ID (推荐使用)',
    `publisher_name` VARCHAR(100) NULL COMMENT '发布者姓名 (冗余)',
    `notice_type`      TINYINT      NULL COMMENT '通知类型（数字格式）',
    `type`           VARCHAR(50)  NULL COMMENT '通知类型（字符串格式）',
    `priority`         INT     DEFAULT 0 COMMENT '优先级',
    `target_type`      VARCHAR(50)  NULL COMMENT '目标类型: 全体/学院/班级/个人',
    `target_id`      BIGINT       NULL COMMENT '目标ID (配合 target_type)',
    `status`           TINYINT DEFAULT 0 COMMENT '状态：0-草稿, 1-已发布, 2-已撤回',
    `is_top`           TINYINT DEFAULT 0 COMMENT '是否置顶：0-否, 1-是',
    `view_count`       INT     DEFAULT 0 COMMENT '阅读次数',
    `send_time`        DATETIME     NULL COMMENT '发送时间',
    `expire_time`      DATETIME     NULL COMMENT '过期时间',
    `attachments_json` TEXT         NULL COMMENT '附件信息列表JSON字符串',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='通知表';

-- 创建通知接收者表
CREATE TABLE IF NOT EXISTS `notification_receiver`
(
    `id`              BIGINT   NOT NULL AUTO_INCREMENT,
    `notification_id` BIGINT   NOT NULL COMMENT '通知ID',
    `receiver_id`     BIGINT   NOT NULL COMMENT '接收用户ID',
    `is_read`         TINYINT  DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `read_time`       DATETIME NULL COMMENT '阅读时间',
    `status`          TINYINT  DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_receiver` (`notification_id`, `receiver_id`),
    FOREIGN KEY (`notification_id`) REFERENCES `notification` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='通知接收者阅读状态表';

-- 创建帖子表
CREATE TABLE IF NOT EXISTS `post` (
                                      `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                                      `title`         VARCHAR(200) NOT NULL COMMENT '帖子标题',
                                      `content`       TEXT         NOT NULL COMMENT '帖子内容',
                                      `author_id`     BIGINT       NOT NULL COMMENT '作者ID',
                                      `forum_type`  VARCHAR(50) NULL COMMENT '板块类型 (字符串)',
                                      `forum_color` VARCHAR(7) DEFAULT '#cccccc' COMMENT '板块颜色代码',
                                      `tags`        JSON        NULL COMMENT '标签数组',
                                      `view_count`    INT        DEFAULT 0 COMMENT '浏览次数',
                                      `like_count`    INT        DEFAULT 0 COMMENT '点赞次数',
                                      `comment_count` INT        DEFAULT 0 COMMENT '评论次数',
                                      `status`        TINYINT    DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
                                      `is_top`        TINYINT    DEFAULT 0 COMMENT '是否置顶：1-置顶，0-不置顶',
                                      `is_essence`    TINYINT    DEFAULT 0 COMMENT '是否精华：1-精华，0-普通',
                                      `create_time`   DATETIME   DEFAULT CURRENT_TIMESTAMP,
                                      `update_time`   DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      INDEX `idx_post_author` (`author_id`),
                                      INDEX `idx_post_forum_type` (`forum_type`),
                                      FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子表';

-- 创建评论表
CREATE TABLE IF NOT EXISTS `comment` (
                                         `id`          BIGINT NOT NULL AUTO_INCREMENT,
                                         `post_id`     BIGINT NOT NULL COMMENT '帖子ID',
                                         `content`     TEXT   NOT NULL COMMENT '评论内容',
                                         `author_id`   BIGINT NOT NULL COMMENT '评论作者ID',
                                         `parent_id`   BIGINT NULL COMMENT '父评论ID，用于回复功能',
                                         `replies` JSON NULL COMMENT '回复数组 JSON',
                                         `like_count`  INT      DEFAULT 0 COMMENT '点赞次数',
                                         `status`      TINYINT  DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
                                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                         `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`),
                                         INDEX `idx_comment_post` (`post_id`),
                                         INDEX `idx_comment_author` (`author_id`),
                                         INDEX `idx_comment_parent` (`parent_id`),
                                         FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
                                         FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                                         FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子评论表';