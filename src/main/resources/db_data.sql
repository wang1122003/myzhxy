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
       (5, (SELECT id FROM `course` WHERE course_code = 'SE2001'), '2024-2025-2', '2023-09-05', 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'CS201'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'SE302'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'SE401'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'SE2001'), '2024-2025-2', '2023-09-05', 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'MA101'), '2024-2025-2', '2023-09-05', 1),
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
       '2024-01-15'
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
       '2024-01-15'
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
       '2024-01-15'
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
       '2024-01-15'
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
       '2024-01-20'
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
VALUES (1, '迎新晚会', '2023年大学迎新晚会', '文化活动', '/posters/activity1.jpg', 1, '13800000000', '大礼堂',
        DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), 200, 50,
        '[{"userId": 5, "joinTime": "2023-09-01 10:00:00", "status": 1}, {"userId": 6, "joinTime": "2023-09-01 11:00:00", "status": 1}]',
        '1', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY)),
       (2, '编程比赛', '2023年大学生编程挑战赛', '竞赛', '/posters/activity2.jpg', 2, '13800000001', '计算机楼102',
        DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), 50, 20,
        '[{"userId": 5, "joinTime": "2023-09-05 10:00:00", "status": 1}]', '1', NOW(), NOW(),
        DATE_ADD(NOW(), INTERVAL 12 DAY)),
       (3, '校园马拉松', '2023年校园健康马拉松', '体育活动', '/posters/activity3.jpg', 1, '13800000000', '校园操场',
        DATE_ADD(NOW(), INTERVAL 21 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), 300, 150,
        '[{"userId": 4, "joinTime": "2023-09-10 10:00:00", "status": 1}]', '1', NOW(), NOW(),
        DATE_ADD(NOW(), INTERVAL 19 DAY));

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
VALUES (1, '关于2023学年开学的通知', '各位同学：\n新学期即将开始，请大家做好开学准备。\n开学时间：2023年9月1日', 2, 2,
        'SYSTEM', 1, 'ALL', NULL, '1', 1, 150, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY),
        '[{"name": "开学手册.pdf", "url": "/files/handbook.pdf"}, {"name": "校历.pdf", "url": "/files/calendar.pdf"}]',
        '[{"receiverId": 5, "isRead": 1, "readTime": "2023-08-20 10:00:00"}, {"receiverId": 6, "isRead": 0, "readTime": null}]',
        NOW(), NOW()),
       (2, '关于2023年奖学金评定的通知',
        '各位同学：\n2023年奖学金评定工作即将开始，请符合条件的同学准备材料。\n截止时间：2023年10月15日', 1, 1, 'ACADEMIC',
        2, 'USER', '5,6', '1', 0, 50, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY),
        '[{"name": "奖学金申请表.docx", "url": "/files/scholarship_form.docx"}]',
        '[{"receiverId": 5, "isRead": 1, "readTime": "2023-08-23 10:00:00"}, {"receiverId": 6, "isRead": 1, "readTime": "2023-08-23 11:00:00"}]',
        NOW(), NOW()),
       (3, '关于校园网络维护的通知',
        '各位师生：\n为了提升校园网络服务质量，将于本周六进行网络设备维护。\n维护时间：2023年9月2日 00:00-06:00', 1, 1,
        'SYSTEM', 0, 'ALL', NULL, '1', 0, 30, NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), '[]',
        '[{"receiverId": 2, "isRead": 0, "readTime": null}, {"receiverId": 5, "isRead": 0, "readTime": null}, {"receiverId": 6, "isRead": 0, "readTime": null}]',
        NOW(), NOW());

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