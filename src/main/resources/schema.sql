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
    `real_name` VARCHAR(50),
    `gender` TINYINT DEFAULT 1,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `avatar` VARCHAR(200),
    `user_type` TINYINT DEFAULT 1,
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(200),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建部门表
CREATE TABLE IF NOT EXISTS `department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `parent_id` BIGINT,
    `description` VARCHAR(200),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建教师表
CREATE TABLE IF NOT EXISTS `teacher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `department_id` BIGINT NOT NULL,
    `title` VARCHAR(50),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建学生表
CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `department_id` BIGINT NOT NULL,
    `student_id` VARCHAR(20) NOT NULL,
    `class_name` VARCHAR(50),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建教室表
CREATE TABLE IF NOT EXISTS `classroom` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `capacity` INT NOT NULL,
    `location` VARCHAR(200),
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建课程表
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `credit` INT NOT NULL,
    `description` TEXT,
    `teacher_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建课程选课表
CREATE TABLE IF NOT EXISTS `course_selection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建成绩表
CREATE TABLE IF NOT EXISTS `score` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `score` DECIMAL(5,2),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`student_id`) REFERENCES `student` (`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
    UNIQUE KEY `uk_student_course` (`student_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建课表表
CREATE TABLE IF NOT EXISTS `schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `course_id` BIGINT NOT NULL,
    `classroom_id` BIGINT NOT NULL,
    `week_day` TINYINT NOT NULL,
    `start_time` TIME NOT NULL,
    `end_time` TIME NOT NULL,
    `start_week` INT NOT NULL,
    `end_week` INT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
    FOREIGN KEY (`classroom_id`) REFERENCES `classroom` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建活动表（已集成参与者信息）
CREATE TABLE IF NOT EXISTS `activity` (
                                          `id`                BIGINT       NOT NULL AUTO_INCREMENT,
                                          `title`             VARCHAR(200) NOT NULL COMMENT '活动标题',
                                          `description`       TEXT         NOT NULL COMMENT '活动描述',
                                          `activity_type` TINYINT NULL COMMENT '活动类型：1-娱乐比赛，2-公益服务，3-学术讲座，4-社团活动，5-其他',
                                          `poster`            VARCHAR(255) NULL COMMENT '活动海报URL',
                                          `organizer_id`      BIGINT       NOT NULL COMMENT '组织者ID',
                                          `location`          VARCHAR(255) NULL COMMENT '活动地点',
                                          `start_time`        DATETIME     NOT NULL COMMENT '活动开始时间',
                                          `end_time`          DATETIME     NOT NULL COMMENT '活动结束时间',
                                          `max_participants`  INT      DEFAULT 0 COMMENT '最大参与人数，0表示不限',
                                          `participants`      JSON         NULL COMMENT '参与者数组，格式：[{user_id, name, register_time, status}]',
                                          `participant_count` INT      DEFAULT 0 COMMENT '当前参与人数',
                                          `status`            TINYINT  DEFAULT 1 COMMENT '状态：1-正常，0-取消',
                                          `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP,
                                          `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`),
                                          INDEX `idx_organizer` (`organizer_id`),
                                          INDEX `idx_time` (`start_time`, `end_time`),
                                          CONSTRAINT `fk_activity_user` FOREIGN KEY (`organizer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='活动表（已集成参与者信息）';

-- 创建通知表（已集成阅读计数）
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `sender_id` BIGINT NOT NULL,
    `publisher_id`     BIGINT       NOT NULL COMMENT '发布者ID (同步sender_id字段)',
    `publisher_name`   VARCHAR(100) NULL COMMENT '发布者姓名',
    `notice_type`      TINYINT      NULL COMMENT '通知类型（数字格式）',
    `type`             VARCHAR(50)  NOT NULL COMMENT '通知类型（字符串格式）',
    `priority`         INT     DEFAULT 0 COMMENT '优先级',
    `target_type`      VARCHAR(50)  NULL COMMENT '目标类型: 全体/学院/班级/个人',
    `target_id`        BIGINT       NULL COMMENT '目标ID',
    `status`           TINYINT DEFAULT 0 COMMENT '状态：0-草稿, 1-已发布, 2-已撤回',
    `is_top`           TINYINT DEFAULT 0 COMMENT '是否置顶：0-否, 1-是',
    `view_count`       INT     DEFAULT 0 COMMENT '阅读次数',
    `send_time`        DATETIME     NULL COMMENT '发送时间',
    `expire_time`      DATETIME     NULL COMMENT '过期时间',
    `attachments_json` TEXT         NULL COMMENT '附件信息列表JSON字符串',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='通知表（已集成阅读计数）';

-- 创建帖子表（已集成论坛板块和标签信息）
CREATE TABLE IF NOT EXISTS `post` (
                                      `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                                      `title`         VARCHAR(200) NOT NULL COMMENT '帖子标题',
                                      `content`       TEXT         NOT NULL COMMENT '帖子内容',
                                      `author_id`     BIGINT       NOT NULL COMMENT '作者ID',
                                      `forum_type`    VARCHAR(50)  NULL COMMENT '板块类型，例如：学习交流、校园生活、招聘信息等',
                                      `forum_color`   VARCHAR(7) DEFAULT '#cccccc' COMMENT '板块颜色代码，用于前端显示',
                                      `tags`          JSON         NULL COMMENT '标签数组，格式：["标签1", "标签2"]',
                                      `view_count`    INT        DEFAULT 0 COMMENT '浏览次数',
                                      `like_count`    INT        DEFAULT 0 COMMENT '点赞次数',
                                      `comment_count` INT        DEFAULT 0 COMMENT '评论次数',
                                      `status`        TINYINT    DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
                                      `is_top`        TINYINT    DEFAULT 0 COMMENT '是否置顶：1-置顶，0-不置顶',
                                      `is_essence`    TINYINT    DEFAULT 0 COMMENT '是否精华：1-精华，0-普通',
                                      `create_time`   DATETIME   DEFAULT CURRENT_TIMESTAMP,
                                      `update_time`   DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      INDEX `idx_author` (`author_id`),
                                      INDEX `idx_forum_type` (`forum_type`),
                                      CONSTRAINT `fk_post_user` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子表（已集成论坛板块和标签信息）';

-- 创建评论表（使用数组形式存储嵌套评论）
CREATE TABLE IF NOT EXISTS `comment` (
                                         `id`          BIGINT NOT NULL AUTO_INCREMENT,
                                         `post_id`     BIGINT NOT NULL COMMENT '帖子ID',
                                         `content`     TEXT   NOT NULL COMMENT '评论内容',
                                         `author_id`   BIGINT NOT NULL COMMENT '评论作者ID',
                                         `parent_id`   BIGINT NULL COMMENT '父评论ID，用于回复功能',
                                         `replies`     JSON   NULL COMMENT '回复数组，格式：[{id, content, author_id, create_time}]',
                                         `like_count`  INT      DEFAULT 0 COMMENT '点赞次数',
                                         `status`      TINYINT  DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
                                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                         `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`),
                                         INDEX `idx_post` (`post_id`),
                                         INDEX `idx_author` (`author_id`),
                                         INDEX `idx_parent` (`parent_id`),
                                         CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
                                         CONSTRAINT `fk_comment_user` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                                         CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子评论表（使用数组形式存储嵌套评论）';