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
    `password`    VARCHAR(100) NOT NULL, 
    `real_name`   VARCHAR(50)  NULL,
    `gender`      TINYINT      DEFAULT 1 COMMENT '1: 男, 0: 女, 2: 其他',
    `email`       VARCHAR(100),
    `phone`       VARCHAR(20),
    `user_type`   VARCHAR(20)  NOT NULL COMMENT '管理员, 教师, 学生',
    `status`      VARCHAR(20) DEFAULT 'Active' COMMENT '用户状态：Active-正常, Inactive-禁用',
    `user_no`     VARCHAR(50)  NULL COMMENT '学工号',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_user_no` (`user_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建教室表
CREATE TABLE IF NOT EXISTS `classroom`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(50)  NOT NULL COMMENT '教室编号, 如 A101',
    `building`    VARCHAR(100) NULL COMMENT '教学楼',
    `capacity`    INT          NOT NULL COMMENT '教室容量',
    `status` TINYINT DEFAULT 1 COMMENT '教室状态：0-禁用，1-正常，2-维护中',
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
    `credit`        DECIMAL(3, 1) NOT NULL COMMENT '学分 (允许小数)',
    `hours`      INT         NULL COMMENT '学时',
    `course_type`   TINYINT   DEFAULT 1 COMMENT '课程类型：1-必修，2-选修, 3-通识 等',
    `introduction`  TEXT          NULL COMMENT '课程简介',
    `teacher_id` BIGINT      NULL COMMENT '授课教师 ID (user.id)',
    `term_info`  VARCHAR(50) NULL COMMENT '学期信息, 如 2023-2024-1',
    `status`     TINYINT DEFAULT 1 COMMENT '课程状态：0-禁用，1-启用',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建课程选课表 (学生选课记录)
CREATE TABLE IF NOT EXISTS `course_selection`
(
    `id`             BIGINT NOT NULL AUTO_INCREMENT,
    `user_id`   BIGINT      NOT NULL COMMENT '学生用户ID',
    `course_id`      BIGINT NOT NULL,
    `term_info` VARCHAR(50) NOT NULL COMMENT '学期信息, 如 2023-2024-1',
    `selection_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `status`    TINYINT DEFAULT 1 COMMENT '选课状态：0-已退课，1-已选',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_student_course_term` (`user_id`, `course_id`, `term_info`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建成绩表
CREATE TABLE IF NOT EXISTS `score`
(
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `selection_id`    BIGINT        NOT NULL COMMENT '选课ID',
    `student_id`      BIGINT        NOT NULL COMMENT '学生ID',
    `course_id`       BIGINT        NOT NULL COMMENT '课程ID',
    `term_info`       VARCHAR(50)   NOT NULL COMMENT '学期信息',
    `total_score`     DECIMAL(5, 2) NULL COMMENT '最终成绩分数',
    `grade`           VARCHAR(10)   NULL COMMENT '等级成绩 (A, B+, C等)',
    `gpa`             DECIMAL(3, 2) NULL COMMENT '绩点',
    `regular_score`   DECIMAL(5, 2) NULL COMMENT '平时成绩',
    `midterm_score`   DECIMAL(5, 2) NULL COMMENT '期中成绩',
    `final_score`     DECIMAL(5, 2) NULL COMMENT '期末考试成绩',
    `score_type`      VARCHAR(20)   NULL COMMENT '成绩类型',
    `comment`         VARCHAR(500)  NULL COMMENT '教师评语',
    `evaluation_date` DATETIME      NULL COMMENT '成绩评定日期',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`selection_id`) REFERENCES `course_selection` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_selection_id` (`selection_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建课表表 (教学班排课信息)
CREATE TABLE IF NOT EXISTS `schedule`
(
    `id`           BIGINT    NOT NULL AUTO_INCREMENT,
    `course_id`    BIGINT    NOT NULL COMMENT '课程ID',
    `term_info`  VARCHAR(50) NOT NULL COMMENT '学期信息, 如 2023-2024-1',
    `teacher_id` BIGINT      NULL COMMENT '授课教师ID (user.id)',
    `classroom_id` BIGINT    NOT NULL COMMENT '教室ID',
    `day_of_week` INT NOT NULL COMMENT '星期几 (1-7)',
    `start_time`   TIME      NOT NULL COMMENT '上课开始时间',
    `end_time`     TIME      NOT NULL COMMENT '上课结束时间',
    `start_week`   INT       NULL COMMENT '开始周',
    `end_week`     INT       NULL COMMENT '结束周',
    `week_parity`  TINYINT   NULL COMMENT '周次奇偶性: 0-所有周, 1-奇数周, 2-偶数周',
    `status` TINYINT DEFAULT 1 COMMENT '排课状态：0-取消，1-正常',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`classroom_id`) REFERENCES `classroom` (`id`) ON DELETE RESTRICT
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
    `contact`              VARCHAR(100) NULL COMMENT '活动联系方式',
    `location`             VARCHAR(255) NULL COMMENT '活动地点',
    `start_time`           DATETIME     NOT NULL COMMENT '活动开始时间',
    `end_time`             DATETIME     NOT NULL COMMENT '活动结束时间',
    `registration_deadline` DATETIME    NULL COMMENT '报名截止时间',
    `max_participants`     INT      DEFAULT 0 COMMENT '最大参与人数，0表示不限',
    `current_participants` INT      DEFAULT 0 COMMENT '当前参与人数',
    `participants_json` TEXT NULL COMMENT '活动参与者JSON数据 [{"userId": 1, "registrationTime": "2023-01-01 10:00:00", "status": 1}]',
    `status`            VARCHAR(20) DEFAULT '1' COMMENT '状态：0-已取消, 1-进行中, 2-已结束',
    `create_time`          DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`organizer_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX `idx_activity_type` (`type`),
    INDEX `idx_activity_time` (`start_time`, `end_time`)
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
    `type`             VARCHAR(50)  NOT NULL COMMENT '通知类型：系统, 课程, 活动, 通用',
    `priority`         TINYINT  DEFAULT 0 COMMENT '优先级：0-普通，1-重要，2-紧急',
    `status`         VARCHAR(20) DEFAULT '1' COMMENT '状态：0-已撤回，1-正常',
    `is_top`           TINYINT  DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    `view_count`       INT      DEFAULT 0 COMMENT '查看次数',
    `target_type`    VARCHAR(50) NULL COMMENT '目标类型：全体、学院、班级、个人',
    `target_ids`     TEXT        NULL COMMENT '目标ID列表，JSON格式',
    `send_time`        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送/计划发送时间',
    `expire_time`      DATETIME     NULL COMMENT '过期时间',
    `attachments_json` TEXT COMMENT '附件列表，JSON格式 [{"name": "file.pdf", "url": "/path/to/file.pdf"}]',
    `receivers_json` TEXT        NULL COMMENT '通知接收者JSON数据 [{"receiverId": 1, "isRead": 1, "readTime": "2023-01-01 10:00:00"}]',
    `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    INDEX `idx_notification_type` (`type`),
    INDEX `idx_notification_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建论坛帖子表 (包含评论)
CREATE TABLE IF NOT EXISTS `post`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `title`         VARCHAR(200) NOT NULL,
    `content`       TEXT         NOT NULL,
    `user_id`       BIGINT       NOT NULL COMMENT '作者用户ID',
    `tags`          TEXT         NULL COMMENT '标签，JSON数组 ["tag1", "tag2"]',
    `view_count`    INT         DEFAULT 0 COMMENT '浏览次数',
    `like_count`    INT         DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT         DEFAULT 0 COMMENT '评论数',
    `comments_json` TEXT NULL COMMENT '评论JSON数据 [{"id": 1, "userId": 2, "content": "评论内容", "creationTime": "2023-01-01 10:00:00", "likeCount": 0, "status": 1}]',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常，2-隐藏',
    `is_top`        TINYINT     DEFAULT 0 COMMENT '是否置顶：1-是，0-否',
    `is_essence`    TINYINT     DEFAULT 0 COMMENT '是否精华：1-是，0-否',
    `creation_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_post_user` (`user_id`),
    INDEX `idx_post_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 创建文件记录表
CREATE TABLE IF NOT EXISTS `file`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL COMMENT '上传者用户ID',
    `filename`     VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path`    VARCHAR(500) NOT NULL COMMENT '文件存储路径或唯一标识符',
    `file_size`    BIGINT       NULL COMMENT '文件大小 (bytes)',
    `file_type`    VARCHAR(100) NULL COMMENT '文件MIME类型或扩展名',
    `upload_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `context_type` VARCHAR(50)  NULL COMMENT '文件上下文类型 (personal, course, avatar, activity_poster, etc.)',
    `context_id`   BIGINT       NULL COMMENT '关联上下文的ID (如 course_id)',
    `status`       TINYINT     DEFAULT 1 COMMENT '文件状态：0-已删除, 1-正常',
    `storage_type` VARCHAR(20) DEFAULT 'local' COMMENT '存储类型 (local, s3, etc.)',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    INDEX `idx_file_user` (`user_id`),
    INDEX `idx_file_context` (`context_type`, `context_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '文件上传记录表';