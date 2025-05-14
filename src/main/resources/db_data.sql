USE campus_db;

# 初始数据部分

-- 插入用户数据
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `gender`, `phone`, `email`, `user_type`, `status`,
                    `user_no`, `create_time`, `update_time`)
VALUES (1, 'admin', '123456', '管理员', 1, '13800000000', 'admin@example.com', 'Admin',
        'Active', 'A001', NOW(), NOW()),
       (2, 'teacher1', '123456', '张老师', 1, '13800000001', 'teacher1@example.com',
        'Teacher', 'Active', 'T001', NOW(), NOW()),
       (3, 'teacher2', '123456', '王老师', 1, '13800000002', 'teacher2@example.com',
        'Teacher', 'Active', 'T002', NOW(), NOW()),
       (4, 'teacher3', '123456', '李老师', 0, '13800000003', 'teacher3@example.com',
        'Teacher', 'Active', 'T003', NOW(), NOW()),
       (5, 'student1', '123456', '赵同学', 1, '13800000004', 'student1@example.com',
        'Student', 'Active', '2023001', NOW(), NOW()),
       (6, 'student2', '123456', '钱同学', 0, '13800000005', 'student2@example.com',
        'Student', 'Active', '2023002', NOW(), NOW()),
       (7, 'student3', '123456', '孙同学', 1, '13800000006', 'student3@example.com',
        'Student', 'Active', '2023003', NOW(), NOW()),
       (8, 'student4', '123456', '周同学', 0, '13800000007', 'student4@example.com',
        'Student', 'Active', '2023004', NOW(), NOW()),
       (9, 'student5', '123456', '吴同学', 1, '13800000008', 'student5@example.com',
        'Student', 'Active', '2023005', NOW(), NOW());

ALTER TABLE `user`
    AUTO_INCREMENT = 10;

-- 插入教室数据
INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
VALUES ('A101', 60, 'A教学楼', 1, 2),
       ('A102', 120, 'A教学楼', 1, 2),
       ('B201', 80, 'B教学楼', 1, 1),
       ('C301', 40, 'C教学楼', 1, 3),
       ('101', 50, '数学楼', 1, 1),
       ('204', 70, '软件楼', 1, 2),
       ('D101', 80, 'D教学楼', 1, 2),
       ('D102', 100, 'D教学楼', 1, 1),
       ('E201', 60, 'E教学楼', 1, 2),
       ('实验楼301', 40, '实验楼', 1, 3);

ALTER TABLE `classroom`
    AUTO_INCREMENT = 11;

-- 插入课程数据
INSERT INTO `course` (`course_name`, `course_code`, `credit`, `hours`, `course_type`, `introduction`, `teacher_id`,
                      `term_info`, `status`)
VALUES ('计算机网络', 'CS301', 3.0, 48, 1, '计算机网络基础课程，主要介绍网络协议和网络编程', 2, '2024-2025-2', 1),
       ('数据结构与算法', 'CS201', 4.0, 64, 1, '数据结构与算法分析，包括常见数据结构和算法设计', 2, '2024-2025-2', 1),
       ('软件工程', 'SE302', 3.5, 56, 1, '软件开发流程、需求分析、设计模式等内容', 3, '2024-2025-2', 1),
       ('高等数学', 'MA101', 5.0, 80, 1, '微积分、线性代数等高等数学基础', 4, '2024-2025-2', 1),
       ('Web前端开发', 'SE2001', 3.0, 48, 2, '学习HTML、CSS、JavaScript等Web前端技术及主流框架应用。', 4, '2024-2025-2',
        1),
       ('操作系统原理', 'CS302', 4.0, 64, 1, '操作系统基本原理、进程管理、内存管理、文件系统等内容', 2, '2024-2025-2', 1),
       ('数据库系统', 'CS401', 4.0, 64, 1, '数据库设计、SQL语言、事务处理与并发控制等内容', 3, '2024-2025-2', 1),
       ('人工智能导论', 'CS501', 3.5, 56, 2, '人工智能基础、搜索算法、机器学习、神经网络等内容', 4, '2024-2025-2', 1),
       ('计算机图形学', 'CS402', 3.0, 48, 2, '图形学基础、渲染技术、三维建模等内容', 2, '2024-2025-2', 1),
       ('程序设计实践', 'SE401', 4.0, 64, 1, '综合性程序设计实践课程，包括各类实用项目开发', 3, '2024-2025-2', 1),
       ('Java高级程序设计', 'SE301', 3.5, 56, 2, 'Java高级特性、多线程编程、网络编程等内容', 4, '2024-2025-2', 1);

ALTER TABLE `course`
    AUTO_INCREMENT = 12;

-- 插入课程选课数据
INSERT INTO `course_selection` (`user_id`, `course_id`, `term_info`, `selection_time`, `status`)
VALUES (5, (SELECT id FROM `course` WHERE course_code = 'CS301'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS201'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'SE2001'), '2024-2025-2', '2024-09-05', 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'CS201'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'SE302'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'SE401'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'SE2001'), '2024-2025-2', '2024-09-05', 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'MA101'), '2024-2025-2', '2024-09-05', 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', NOW(), 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', NOW(), 1),
       (8, (SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', NOW(), 1),
       (8, (SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', NOW(), 1),
       (9, (SELECT id FROM `course` WHERE course_code = 'SE401'), '2024-2025-2', NOW(), 1),
       (9, (SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', NOW(), 1);

-- 插入成绩数据
INSERT INTO `score` (`selection_id`, `student_id`, `course_id`, `term_info`, `total_score`, `grade`, `gpa`,
                     `regular_score`, `midterm_score`, `final_score`, `evaluation_date`)
SELECT cs.id,
       cs.user_id,
       cs.course_id,
       cs.term_info,
       85.50,
       'B',
       3.5,
       80.00,
       85.00,
       88.00,
       '2025-07-10'
FROM `course_selection` cs
         JOIN `course` c ON cs.course_id = c.id
WHERE cs.user_id = 5
  AND c.course_code = 'CS301'
  AND cs.term_info = '2024-2025-2';

INSERT INTO `score` (`selection_id`, `student_id`, `course_id`, `term_info`, `total_score`, `grade`, `gpa`,
                     `regular_score`, `midterm_score`, `final_score`, `comment`, `evaluation_date`)
SELECT cs.id,
       cs.user_id,
       cs.course_id,
       cs.term_info,
       92.00,
       'A',
       4.0,
       90.00,
       92.00,
       93.00,
       '表现优秀',
       '2025-07-10'
FROM `course_selection` cs
         JOIN `course` c ON cs.course_id = c.id
WHERE cs.user_id = 5
  AND c.course_code = 'CS201'
  AND cs.term_info = '2024-2025-2';

INSERT INTO `score` (`selection_id`, `student_id`, `course_id`, `term_info`, `total_score`, `grade`, `gpa`,
                     `regular_score`, `midterm_score`, `final_score`, `evaluation_date`)
SELECT cs.id,
       cs.user_id,
       cs.course_id,
       cs.term_info,
       78.50,
       'C+',
       2.8,
       75.00,
       78.00,
       80.00,
       '2025-07-10'
FROM `course_selection` cs
         JOIN `course` c ON cs.course_id = c.id
WHERE cs.user_id = 6
  AND c.course_code = 'CS201'
  AND cs.term_info = '2024-2025-2';

INSERT INTO `score` (`selection_id`, `student_id`, `course_id`, `term_info`, `total_score`, `grade`, `gpa`,
                     `regular_score`, `midterm_score`, `final_score`, `evaluation_date`)
SELECT cs.id,
       cs.user_id,
       cs.course_id,
       cs.term_info,
       88.00,
       'B+',
       3.8,
       85.00,
       88.00,
       90.00,
       '2025-07-10'
FROM `course_selection` cs
         JOIN `course` c ON cs.course_id = c.id
WHERE cs.user_id = 6
  AND c.course_code = 'SE302'
  AND cs.term_info = '2024-2025-2';

INSERT INTO `score` (`selection_id`, `student_id`, `course_id`, `term_info`, `total_score`, `grade`, `gpa`, `comment`,
                     `evaluation_date`)
SELECT cs.id,
       cs.user_id,
       cs.course_id,
       cs.term_info,
       89.00,
       'B+',
       3.7,
       '基础扎实，逻辑清晰，有进步空间。',
       '2025-07-10'
FROM `course_selection` cs
         JOIN `course` c ON cs.course_id = c.id
WHERE cs.user_id = 7
  AND c.course_code = 'MA101'
  AND cs.term_info = '2024-2025-2';

-- 插入课表数据
INSERT INTO `schedule` (`course_id`, `term_info`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`,
                        `start_week`, `end_week`, `status`)
VALUES ((SELECT id FROM `course` WHERE course_code = 'CS301'), '2024-2025-2', 2,
        (SELECT id FROM `classroom` WHERE name = 'A101' AND building = 'A教学楼'), 1, '08:00:00', '09:40:00', 1,
        16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS201'), '2024-2025-2', 2,
        (SELECT id FROM `classroom` WHERE name = 'A102' AND building = 'A教学楼'), 2, '10:00:00', '11:40:00', 1,
        16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'SE302'), '2024-2025-2', 3,
        (SELECT id FROM `classroom` WHERE name = 'B201' AND building = 'B教学楼'), 3, '14:00:00', '15:40:00',
        1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'MA101'), '2024-2025-2', 4,
        (SELECT id FROM `classroom` WHERE name = 'C301' AND building = 'C教学楼'), 4, '16:00:00', '17:40:00',
        1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'SE2001'), '2024-2025-2', 4,
        (SELECT id FROM `classroom` WHERE name = '204' AND building = '软件楼'), 3, '14:00:00', '15:40:00', 1,
        16, 1),
       -- 以下是additional_courses.sql中添加的排课记录
       ((SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', 2,
        (SELECT id FROM `classroom` WHERE name = 'D101'), 1, '14:00:00', '15:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', 3,
        (SELECT id FROM `classroom` WHERE name = 'D102'), 2, '08:00:00', '09:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', 4,
        (SELECT id FROM `classroom` WHERE name = 'E201'), 3, '08:00:00', '11:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'SE401'), '2024-2025-2', 3,
        (SELECT id FROM `classroom` WHERE name = '实验楼301'), 4, '14:00:00', '17:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', 2,
        (SELECT id FROM `classroom` WHERE name = 'A101'), 5, '10:00:00', '15:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', 4,
        (SELECT id FROM `classroom` WHERE name = 'B201'), 2, '19:00:00', '21:35:00', 1, 16, 1),
       -- 为学生5添加连续上课的课程安排
       ((SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', 4,
        (SELECT id FROM `classroom` WHERE name = 'A101'), 3, '08:00:00', '09:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', 2,
        (SELECT id FROM `classroom` WHERE name = 'A102'), 3, '10:00:00', '11:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', 3,
        (SELECT id FROM `classroom` WHERE name = 'B201'), 3, '14:00:00', '15:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', 2,
        (SELECT id FROM `classroom` WHERE name = 'C301'), 3, '16:00:00', '17:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', 4,
        (SELECT id FROM `classroom` WHERE name = 'D101'), 3, '19:00:00', '21:35:00', 1, 16, 1);

-- 插入活动数据
INSERT INTO `activity` (`id`, `title`, `description`, `type`, `poster_url`, `organizer_id`, `contact`, `location`,
                        `start_time`, `end_time`, `max_participants`, `current_participants`, `participants_json`,
                        `status`, `create_time`, `update_time`, `registration_deadline`)
VALUES (1, '2025迎新晚会', '2025年大学迎新晚会', '文化活动', '/posters/activity1.jpg', 1, '13800000000', '大礼堂',
        '2025-05-15 19:00:00', '2025-05-15 21:30:00', 200, 2,
        '[{"userId": 5, "joinTime": "2025-05-10 10:00:00", "status": 1}, {"userId": 6, "joinTime": "2025-05-11 11:00:00", "status": 1}]',
        '1', NOW(), NOW(), '2025-05-13 23:59:59'),
       (2, '2025编程比赛', '2025年大学生编程挑战赛', '竞赛', '/posters/activity2.jpg', 2, '13800000001', '计算机楼102',
        '2025-06-10 09:00:00', '2025-06-10 17:00:00', 50, 1,
        '[{"userId": 5, "joinTime": "2025-06-01 10:00:00", "status": 1}]', '1', NOW(), NOW(), '2025-06-08 23:59:59'),
       (3, '2025校园马拉松', '2025年校园健康马拉松', '体育活动', '/posters/activity3.jpg', 1, '13800000000', '校园操场',
        '2025-05-25 08:00:00', '2025-05-25 12:00:00', 300, 1,
        '[{"userId": 4, "joinTime": "2025-05-20 10:00:00", "status": 1}]', '1', NOW(), NOW(), '2025-05-23 23:59:59');

-- 插入论坛帖子数据
INSERT INTO `post` (`title`, `content`, `user_id`, `tags`, `status`)
VALUES ('计算机网络期末复习资料分享',
        '整理了一份计算机网络的期末复习资料，包含重点章节、历年真题和模拟题，希望对大家有帮助...', 2,
        '["复习资料", "计算机网络", "期末考试"]', 1),
       ('关于高数难点解析的讨论',
        '高等数学中的泰勒展开式和傅里叶变换理解起来比较困难，大家有什么好的学习方法或者推荐的资源吗？一起讨论下！', 5,
        '["高等数学", "学习方法", "难点解析"]', 1),
       ('高等数学学习心得',
        '分享一下我在学习高等数学过程中的一些方法和体会，希望能帮助到大家。首先，理解概念比记忆公式更重要；其次，多做习题是掌握知识的关键；最后，形成学习小组相互讨论可以加深理解。',
        7, '["高等数学", "学习心得"]', 1);

-- 插入通知数据
INSERT INTO `notification` (`id`, `title`, `content`, `sender_id`, `publisher_id`, `type`, `priority`, `target_type`,
                            `target_ids`, `status`, `is_top`, `view_count`, `send_time`, `expire_time`,
                            `attachments_json`, `receivers_json`, `create_time`, `update_time`)
VALUES (1, '关于2025学年秋季学期开学的通知', '各位同学：
新学期即将开始，请大家做好开学准备。
开学时间：2025年9月1日', 1, 1,
        'SYSTEM', 1, 'ALL', NULL, '1', 1, 150, NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH),
        '[{"id": "file1", "filename": "2025秋季开学手册.pdf", "size": 1048576}, {"id": "file2", "filename": "2025-2026学年校历.pdf", "size": 512000}]',
        '[{"userId": 5, "isRead": 1, "readTime": "2025-05-09 10:00:00"}, {"userId": 6, "isRead": 0, "readTime": null}]',
        NOW(), NOW()),
       (2, '关于2025年国家奖学金评定的通知',
        '各位同学：
2025年国家奖学金评定工作即将开始，请符合条件的同学准备材料并按时提交。
截止时间：2025年10月15日', 1, 1, 'ACADEMIC',
        2, 'USER', '5,6,7,8,9', '1', 0, 50, NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY),
        '[{"id": "file3", "filename": "国家奖学金申请表.docx", "size": 25600}]',
        '[{"userId": 5, "isRead": 1, "readTime": "2025-05-09 11:00:00"}, {"userId": 6, "isRead": 1, "readTime": "2025-05-09 12:00:00"}, {"userId": 7, "isRead": 0, "readTime": null}]',
        NOW(), NOW()),
       (3, '关于校园网络维护的通知（2025年9月）',
        '各位师生：
为了提升校园网络服务质量，信息中心计划于本周六凌晨进行网络核心设备维护。
维护时间：2025年05月18日 00:00 - 06:00
期间校园网及相关服务可能会出现短暂中断，敬请谅解。', 1, 1,
        'SYSTEM', 0, 'ALL', NULL, '1', 0, 30, NOW(), '2025-05-19 00:00:00', '[]',
        '[{"receiverId": 2, "isRead": 0, "readTime": null}, {"receiverId": 5, "isRead": 0, "readTime": null}, {"receiverId": 6, "isRead": 0, "readTime": null}]',
        NOW(), NOW()),
       (4, '图书馆国庆节开放时间调整通知',
        '各位读者：
根据学校后续安排，图书馆开放时间调整如下：
具体日期待定
请各位读者相互转告。', 1, 1,
        'GENERAL', 0, 'ALL', NULL, '1', 0, 120, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), '[]', '[]', NOW(), NOW()),
       (5, '计算机学院"代码之星"编程竞赛报名通知',
        '计算机学院将举办第三届"代码之星"编程竞赛，旨在激发学生编程兴趣，提升实践能力。
报名时间：即日起至2025年05月25日
比赛时间：2025年06月01日
欢迎各位同学踊跃报名！', 2, 2,
        'ACTIVITY', 1, 'COLLEGE', '1', '1', 0, 88, NOW(), '2025-05-26 00:00:00',
        '[{"id": "file4", "filename": "编程竞赛规程.pdf", "size": 307200}]', '[]', NOW(), NOW());

ALTER TABLE `notification`
    AUTO_INCREMENT = 6;

-- 为其他学期补充数据 --

-- 学期: 2023-2024-1
-- 选课
INSERT INTO `course_selection` (`user_id`, `course_id`, `term_info`, `selection_time`, `status`)
VALUES (5, (SELECT id FROM `course` WHERE course_code = 'CS301'), '2023-2024-1', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'CS201'), '2023-2024-1', NOW(), 1);
-- 排课
INSERT INTO `schedule` (`course_id`, `term_info`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`,
                        `start_week`, `end_week`, `status`)
VALUES ((SELECT id FROM `course` WHERE course_code = 'CS301'), '2023-2024-1', 2,
        (SELECT id FROM `classroom` WHERE name = 'A101'), 1, '08:00:00', '09:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS201'), '2023-2024-1', 2,
        (SELECT id FROM `classroom` WHERE name = 'A102'), 2, '10:00:00', '11:40:00', 1, 16, 1);

-- 学期: 2023-2024-2
-- 选课
INSERT INTO `course_selection` (`user_id`, `course_id`, `term_info`, `selection_time`, `status`)
VALUES (5, (SELECT id FROM `course` WHERE course_code = 'MA101'), '2023-2024-2', NOW(), 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'SE2001'), '2023-2024-2', NOW(), 1);
-- 排课
INSERT INTO `schedule` (`course_id`, `term_info`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`,
                        `start_week`, `end_week`, `status`)
VALUES ((SELECT id FROM `course` WHERE course_code = 'MA101'), '2023-2024-2', 4,
        (SELECT id FROM `classroom` WHERE name = 'C301'), 3, '14:00:00', '15:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'SE2001'), '2023-2024-2', 4,
        (SELECT id FROM `classroom` WHERE name = '204' AND building = '软件楼'), 4, '16:00:00', '17:40:00', 1, 16, 1);

-- 学期: 2024-2025-1
-- 选课
INSERT INTO `course_selection` (`user_id`, `course_id`, `term_info`, `selection_time`, `status`)
VALUES (6, (SELECT id FROM `course` WHERE course_code = 'CS301'), '2024-2025-1', NOW(), 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'SE302'), '2024-2025-1', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'MA101'), '2024-2025-1', NOW(), 1);
-- 排课
INSERT INTO `schedule` (`course_id`, `term_info`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`,
                        `start_week`, `end_week`, `status`)
VALUES ((SELECT id FROM `course` WHERE course_code = 'CS301'), '2024-2025-1', 2,
        (SELECT id FROM `classroom` WHERE name = 'B201'), 5, '08:00:00', '09:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'SE302'), '2024-2025-1', 3,
        (SELECT id FROM `classroom` WHERE name = 'A101'), 1, '10:00:00', '11:40:00', 1, 16, 1),
       ((SELECT id FROM `course` WHERE course_code = 'MA101'), '2024-2025-1', 4,
        (SELECT id FROM `classroom` WHERE name = '101' AND building = '数学楼'), 2, '14:00:00', '15:40:00', 1, 16, 1);

-- 插入学期数据
INSERT INTO `term` (`code`, `name`, `start_date`, `end_date`, `is_current`, `academic_year`, `term_number`, `status`,
                    `description`)
VALUES ('2023-2024-1', '2023-2024学年第一学期', '2023-09-01', '2024-01-15', 0, '2023-2024', 1, 1,
        '2023-2024学年秋季学期'),
       ('2023-2024-2', '2023-2024学年第二学期', '2024-02-25', '2024-07-05', 0, '2023-2024', 2, 1,
        '2023-2024学年春季学期'),
       ('2023-2024-3', '2023-2024学年第三学期', '2024-07-10', '2024-08-30', 0, '2023-2024', 3, 1,
        '2023-2024学年暑期学期'),
       ('2024-2025-1', '2024-2025学年第一学期', '2024-09-01', '2025-01-15', 0, '2024-2025', 1, 1,
        '2024-2025学年秋季学期'),
       ('2024-2025-2', '2024-2025学年第二学期', '2025-02-25', '2025-07-05', 1, '2024-2025', 2, 1, '当前学期');

ALTER TABLE `term`
    AUTO_INCREMENT = 6;