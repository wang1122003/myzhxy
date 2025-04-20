-- 插入角色数据 (使用 role_name)
INSERT INTO `role` (`role_name`, `description`)
VALUES ('Admin', '系统管理员'),
       ('Teacher', '教师'),
       ('Student', '学生');

-- 插入用户数据 (更新: user_type, status -> VARCHAR, avatar -> avatar_url, 添加 real_name)
INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`,
                    `status`)
VALUES ('admin', '123456', '系统管理员', 1, 'admin@example.com', '13800138000', '/avatar/admin.jpg', 'Admin', 'Active'),
       ('teacher1', '123456', '张老师', 1, 'teacher1@example.com', '13800138001', '/avatar/teacher1.jpg', 'Teacher',
        'Active'),
       ('teacher2', '123456', '李老师', 0, 'teacher2@example.com', '13800138002', '/avatar/teacher2.jpg', 'Teacher',
        'Active'),
       ('student1', '123456', '王同学', 1, 'student1@example.com', '13800138003', '/avatar/student1.jpg', 'Student',
        'Active'),
       ('student2', '123456', '赵同学', 0, 'student2@example.com', '13800138004', '/avatar/student2.jpg', 'Student',
        'Active');

-- 插入用户角色关联数据 (使用 SELECT)
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u,
     `role` r
WHERE u.username = 'admin'
  AND r.role_name = 'Admin';
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u,
     `role` r
WHERE u.username = 'teacher1'
  AND r.role_name = 'Teacher';
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u,
     `role` r
WHERE u.username = 'teacher2'
  AND r.role_name = 'Teacher';
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u,
     `role` r
WHERE u.username = 'student1'
  AND r.role_name = 'Student';
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u,
     `role` r
WHERE u.username = 'student2'
  AND r.role_name = 'Student';

-- 插入部门数据 (添加新字段)
INSERT INTO `department` (`id`, `name`, `parent_id`, `introduction`, `director_id`, `office_location`, `contact_phone`,
                          `contact_email`, `website_url`)
VALUES (1, '信息工程学院', NULL, '包含计算机科学、软件工程等专业', NULL, 'A座401', '010-12345678', 'cs@example.com',
        'http://cs.example.com'),
       (2, '计算机科学系', 1, '计算机科学与技术专业', (SELECT id FROM `user` WHERE username = 'teacher1'), 'A座402',
        '010-12345679', 'cs-dept@example.com', NULL),
       (3, '软件工程系', 1, '软件工程专业', (SELECT id FROM `user` WHERE username = 'teacher2'), 'A座403',
        '010-12345680', 'se-dept@example.com', NULL),
       (4, '数学与统计学院', NULL, '数学与统计相关专业', NULL, 'B座201', '010-87654321', 'math@example.com', NULL),
       (5, '应用数学系', 4, '应用数学专业', NULL, 'B座202', '010-87654322', 'amath-dept@example.com', NULL);
ALTER TABLE `department`
    AUTO_INCREMENT = 6;
-- 重置自增起始值 (可选但推荐)

-- 插入教师数据 (使用 user_id, 添加新字段)
INSERT INTO `teacher` (`id`, `user_id`, `teacher_no`, `department_id`, `title`, `research_area`, `status`, `hire_date`)
VALUES (1, (SELECT id FROM `user` WHERE username = 'teacher1'), 'T001', 2, '副教授', '计算机网络, 数据挖掘', 'Active',
        '2018-07-01'),
       (2, (SELECT id FROM `user` WHERE username = 'teacher2'), 'T002', 3, '讲师', '软件工程, Web开发', 'Active',
        '2020-08-15');
ALTER TABLE `teacher`
    AUTO_INCREMENT = 3;

-- 插入班级数据 (新增)
INSERT INTO `clazz` (`id`, `name`, `grade`, `department_id`, `instructor_id`)
VALUES (1, '计算机2301班', '2023', 2, (SELECT id FROM `teacher` WHERE teacher_no = 'T001')),
       (2, '软件2302班', '2023', 3, (SELECT id FROM `teacher` WHERE teacher_no = 'T002'));
ALTER TABLE `clazz`
    AUTO_INCREMENT = 3;

-- 插入学生数据 (使用 user_id, 添加新字段)
INSERT INTO `student` (`id`, `user_id`, `student_no`, `department_id`, `class_id`, `status`, `enrollment_date`)
VALUES (1, (SELECT id FROM `user` WHERE username = 'student1'), '2023001', 2, 1, 'Active', '2023-09-01'),
       (2, (SELECT id FROM `user` WHERE username = 'student2'), '2023002', 3, 2, 'Active', '2023-09-01');
ALTER TABLE `student`
    AUTO_INCREMENT = 3;

-- 插入教室数据 (使用 building)
INSERT INTO `classroom` (`id`, `name`, `capacity`, `building`, `status`, `room_type`)
VALUES (1, 'A101', 60, 'A教学楼', 1, 2),
       (2, 'A102', 120, 'A教学楼', 1, 2),
       (3, 'B201', 80, 'B教学楼', 1, 1),
       (4, 'C301', 40, 'C教学楼', 1, 3);
ALTER TABLE `classroom`
    AUTO_INCREMENT = 5;

-- 插入课程数据 (使用 course_name, course_code, course_no, credit(DECIMAL))
INSERT INTO `course` (`id`, `course_name`, `course_no`, `course_code`, `credit`, `introduction`, `teacher_id`,
                      `course_type`)
VALUES (1, '计算机网络', 'CS301', 'CS301-2023', 3.0, '计算机网络基础课程，主要介绍网络协议和网络编程',
        (SELECT id FROM `teacher` WHERE teacher_no = 'T001'), 1),
       (2, '数据结构与算法', 'CS201', 'CS201-2023', 4.0, '数据结构与算法分析，包括常见数据结构和算法设计',
        (SELECT id FROM `teacher` WHERE teacher_no = 'T001'), 1),
       (3, '软件工程', 'SE302', 'SE302-2023', 3.5, '软件开发流程、需求分析、设计模式等内容',
        (SELECT id FROM `teacher` WHERE teacher_no = 'T002'), 1),
       (4, '高等数学', 'MA101', 'MA101-2023', 5.0, '微积分、线性代数等高等数学基础',
        (SELECT id FROM `teacher` WHERE teacher_no = 'T002'), 1);
ALTER TABLE `course`
    AUTO_INCREMENT = 5;

-- 插入学期数据
INSERT INTO `term` (`id`, `term_name`, `code`, `start_date`, `end_date`, `current`, `status`)
VALUES (1, '2023-2024学年第一学期', '2023-2024-1', '2023-09-04', '2024-01-12', 1, 1),
       (2, '2023-2024学年第二学期', '2023-2024-2', '2024-02-26', '2024-07-05', 0, 0),
       (3, '2024-2025学年第一学期', '2024-2025-1', '2024-09-02', '2025-01-10', 0, 0);
ALTER TABLE `term`
    AUTO_INCREMENT = 4;

-- 插入课程选课数据 (添加 term_id)
INSERT INTO `course_selection` (`student_id`, `course_id`, `status`, `term_id`)
VALUES ((SELECT id FROM `student` WHERE student_no = '2023001'),
        (SELECT id FROM `course` WHERE course_code = 'CS301-2023'), 1,
        (SELECT id FROM `term` WHERE code = '2023-2024-1')),
       ((SELECT id FROM `student` WHERE student_no = '2023001'),
        (SELECT id FROM `course` WHERE course_code = 'CS201-2023'), 1,
        (SELECT id FROM `term` WHERE code = '2023-2024-1')),
       ((SELECT id FROM `student` WHERE student_no = '2023002'),
        (SELECT id FROM `course` WHERE course_code = 'CS201-2023'), 1,
        (SELECT id FROM `term` WHERE code = '2023-2024-1')),
       ((SELECT id FROM `student` WHERE student_no = '2023002'),
        (SELECT id FROM `course` WHERE course_code = 'SE302-2023'), 1,
        (SELECT id FROM `term` WHERE code = '2023-2024-1'));

-- 插入成绩数据 (添加新字段, 移除旧 score)
INSERT INTO `score` (`student_id`, `course_id`, `term`, `regular_score`, `midterm_score`, `final_score`, `total_score`,
                     `gpa`)
VALUES ((SELECT id FROM `student` WHERE student_no = '2023001'),
        (SELECT id FROM `course` WHERE course_code = 'CS301-2023'), '2023-2024-1', 80.00, 85.00, 88.00, 85.50, 3.5),
       ((SELECT id FROM `student` WHERE student_no = '2023001'),
        (SELECT id FROM `course` WHERE course_code = 'CS201-2023'), '2023-2024-1', 90.00, 92.00, 93.00, 92.00, 4.0),
       ((SELECT id FROM `student` WHERE student_no = '2023002'),
        (SELECT id FROM `course` WHERE course_code = 'CS201-2023'), '2023-2024-1', 75.00, 78.00, 80.00, 78.50, 2.8),
       ((SELECT id FROM `student` WHERE student_no = '2023002'),
        (SELECT id FROM `course` WHERE course_code = 'SE302-2023'), '2023-2024-1', 85.00, 88.00, 90.00, 88.00, 3.8);

-- 插入课表数据 (通过 course_code 查询 course_id, 添加 class_id)
INSERT INTO `schedule` (`course_id`, `classroom_id`, `teacher_id`, `class_id`, `week_day`, `start_time`, `end_time`,
                        `start_week`, `end_week`, `term_id`, `status`)
VALUES ((SELECT id FROM `course` WHERE course_code = 'CS301-2023'), (SELECT id FROM `classroom` WHERE name = 'A101'),
        (SELECT id FROM `teacher` WHERE teacher_no = 'T001'), 1, 1, '08:00:00', '09:40:00', 1, 16,
        (SELECT id FROM `term` WHERE code = '2023-2024-1'), 1),
       ((SELECT id FROM `course` WHERE course_code = 'CS201-2023'), (SELECT id FROM `classroom` WHERE name = 'A102'),
        (SELECT id FROM `teacher` WHERE teacher_no = 'T001'), 1, 2, '10:00:00', '11:40:00', 1, 16,
        (SELECT id FROM `term` WHERE code = '2023-2024-1'), 1),
       ((SELECT id FROM `course` WHERE course_code = 'SE302-2023'), (SELECT id FROM `classroom` WHERE name = 'B201'),
        (SELECT id FROM `teacher` WHERE teacher_no = 'T002'), 2, 3, '14:00:00', '15:40:00', 1, 16,
        (SELECT id FROM `term` WHERE code = '2023-2024-1'), 1),
       ((SELECT id FROM `course` WHERE course_code = 'MA101-2023'), (SELECT id FROM `classroom` WHERE name = 'C301'),
        (SELECT id FROM `teacher` WHERE teacher_no = 'T002'), NULL, 4, '16:00:00', '17:40:00', 1, 16,
        (SELECT id FROM `term` WHERE code = '2023-2024-1'), 1);

-- 插入活动数据 (移除 participants JSON, 更新字段名, 添加 publisher_id)
INSERT INTO `activity` (`id`, `title`, `description`, `activity_type`, `poster_url`, `organizer_id`, `publisher_id`,
                        `organizer_name`, `contact`, `location`, `start_time`, `end_time`, `max_participants`,
                        `current_participants`, `status`)
VALUES (1, '校园歌手大赛', '一年一度的校园歌手大赛来啦！欢迎各位有才艺的同学踊跃报名参加...', 1,
        '/uploads/posters/singer.jpg', (SELECT id FROM `user` WHERE username = 'admin'),
        (SELECT id FROM `user` WHERE username = 'admin'), '系统管理员', '13800138000', '大学生活动中心',
        '2023-05-15 18:30:00', '2023-05-15 22:00:00', 200, 0, 1),
       (2, '职业发展讲座：IT行业就业指南', '邀请知名IT企业HR和技术专家，分享IT行业就业经验和职业规划建议...', 3,
        '/uploads/posters/it_job.jpg', (SELECT id FROM `user` WHERE username = 'teacher1'),
        (SELECT id FROM `user` WHERE username = 'teacher1'), '张老师', '13800138001', '教学楼A栋报告厅',
        '2023-05-20 14:00:00', '2023-05-20 16:30:00', 150, 0, 1),
       (3, '篮球友谊赛', '计算机学院vs经管学院篮球友谊赛，欢迎前来观赛助威...', 1, '/uploads/posters/basketball.jpg',
        (SELECT id FROM `user` WHERE username = 'teacher2'), (SELECT id FROM `user` WHERE username = 'teacher2'),
        '李老师', '13800138002', '体育馆', '2023-05-25 15:00:00', '2023-05-25 17:00:00', 300, 0, 1);
ALTER TABLE `activity`
    AUTO_INCREMENT = 4;

-- 插入活动参与者数据 (使用子查询获取 activity_id 和 user_id)
INSERT INTO `activity_participant` (`activity_id`, `user_id`, `join_time`)
VALUES ((SELECT id FROM `activity` WHERE title = '校园歌手大赛'), (SELECT id FROM `user` WHERE username = 'teacher1'),
        '2023-04-10 14:30:00'),
       ((SELECT id FROM `activity` WHERE title = '校园歌手大赛'), (SELECT id FROM `user` WHERE username = 'teacher2'),
        '2023-04-11 09:15:00'),
       ((SELECT id FROM `activity` WHERE title = '职业发展讲座：IT行业就业指南'),
        (SELECT id FROM `user` WHERE username = 'admin'), '2023-04-12 11:20:00'),
       ((SELECT id FROM `activity` WHERE title = '职业发展讲座：IT行业就业指南'),
        (SELECT id FROM `user` WHERE username = 'student1'), '2023-04-13 16:45:00'),
       ((SELECT id FROM `activity` WHERE title = '职业发展讲座：IT行业就业指南'),
        (SELECT id FROM `user` WHERE username = 'student2'), '2023-04-14 10:30:00');

-- 更新活动表中的 current_participants 计数
UPDATE `activity`
SET `current_participants` = (SELECT COUNT(*)
                              FROM `activity_participant`
                              WHERE `activity_participant`.`activity_id` = `activity`.`id`);

-- 插入帖子数据 (forum_type 保持 VARCHAR)
INSERT INTO `post` (`id`, `title`, `content`, `author_id`, `forum_type`, `forum_color`, `tags`, `view_count`,
                    `like_count`, `comment_count`, `status`, `is_top`, `is_essence`)
VALUES (1, '计算机网络期末复习资料分享', '整理了一份计算机网络的期末复习资料，希望对大家有帮助...',
        (SELECT id FROM `user` WHERE username = 'teacher1'), '学习交流', '#3498db', '[
    "复习资料",
    "计算机网络",
    "期末考试"
  ]', 120, 25, 8, 1, 1, 1),
       (2, '关于高数难点解析的讨论', '高等数学中的泰勒展开式和傅里叶变换理解起来比较困难，有什么好的学习方法吗？',
        (SELECT id FROM `user` WHERE username = 'student1'), '学习交流', '#3498db', '[
         "高等数学",
         "学习方法",
         "难点解析"
       ]', 85, 16, 5, 1, 0, 0),
       (3, '校园美食推荐：隐藏在角落的美味', '发现了一家藏在图书馆后面的小店，他们家的兰州拉面特别正宗...',
        (SELECT id FROM `user` WHERE username = 'student2'), '校园生活', '#e74c3c', '[
         "美食",
         "校园生活",
         "推荐"
       ]', 200, 45, 12, 1, 0, 1),
       (4, '2025届毕业生校园招聘信息汇总', '整理了最近各大企业的校园招聘信息，包括招聘要求、薪资福利等详细内容...',
        (SELECT id FROM `user` WHERE username = 'teacher1'), '招聘信息', '#2ecc71', '[
         "校招",
         "就业",
         "招聘信息"
       ]', 350, 89, 25, 1, 1, 1),
       (5, '摄影协会周末外拍活动通知', '本周末摄影协会将组织前往市郊森林公园外拍活动，欢迎摄影爱好者参加...',
        (SELECT id FROM `user` WHERE username = 'teacher2'), '活动公告', '#f39c12', '[
         "摄影",
         "社团活动",
         "外拍"
       ]', 90, 22, 10, 1, 0, 0),
       (6, '心情随笔：大学四年的感悟', '即将毕业，回首四年大学生活，有太多感慨和收获...',
        (SELECT id FROM `user` WHERE username = 'student1'), '心灵驿站', '#9b59b6', '[
         "随笔",
         "毕业",
         "感悟"
       ]', 78, 36, 9, 1, 0, 1);
ALTER TABLE `post`
    AUTO_INCREMENT = 7;

-- 插入评论数据 (使用子查询获取 post_id, author_id, parent_id)
-- 注意: 为了获取 parent_id，我们需要分步插入或者假设已知评论的 ID。这里为了简化，我们先插入父评论，然后更新它们的ID（或者假设已知ID）
INSERT INTO `comment` (`id`, `post_id`, `content`, `author_id`, `parent_id`, `like_count`, `status`)
VALUES (1, (SELECT id FROM `post` WHERE title LIKE '计算机网络期末复习资料分享%'), '感谢分享，资料很全面！',
        (SELECT id FROM `user` WHERE username = 'student1'), NULL, 5, 1),
       (2, (SELECT id FROM `post` WHERE title LIKE '计算机网络期末复习资料分享%'), '请问包含实验部分的内容吗？',
        (SELECT id FROM `user` WHERE username = 'student2'), NULL, 2, 1),
       (3, (SELECT id FROM `post` WHERE title LIKE '关于高数难点解析的讨论%'),
        '推荐你看看3Blue1Brown的视频，对理解数学概念很有帮助', (SELECT id FROM `user` WHERE username = 'teacher1'), NULL,
        8, 1),
       (4, (SELECT id FROM `post` WHERE title LIKE '校园美食推荐：%'), '这家店确实不错，他们家的牛肉面我吃过，味道很赞！',
        (SELECT id FROM `user` WHERE username = 'admin'), NULL, 4, 1),
       (5, (SELECT id FROM `post` WHERE title LIKE '2025届毕业生校园招聘信息汇总%'), '感谢整理，信息很全面！',
        (SELECT id FROM `user` WHERE username = 'student2'), NULL, 6, 1);
-- 插入子评论，需要先知道父评论的 ID (这里假设为 1 和 4)
INSERT INTO `comment` (`id`, `post_id`, `content`, `author_id`, `parent_id`, `like_count`, `status`)
VALUES (6, (SELECT id FROM `post` WHERE title LIKE '计算机网络期末复习资料分享%'), '不客气，希望对你有帮助！',
        (SELECT id FROM `user` WHERE username = 'teacher1'), 1, 3, 1), -- 回复第1条评论
       (7, (SELECT id FROM `post` WHERE title LIKE '校园美食推荐：%'), '他们家的饺子也很好吃，推荐尝试',
        (SELECT id FROM `user` WHERE username = 'student2'), 4, 2, 1); -- 回复第4条评论
ALTER TABLE `comment`
    AUTO_INCREMENT = 8;


-- 插入通知数据 (检查 sender_id 和 publisher_id)
INSERT INTO `notification` (`id`, `title`, `content`, `sender_id`, `publisher_id`, `publisher_name`, `notice_type`,
                            `type`, `priority`, `target_type`, `target_id`, `status`, `is_top`, `view_count`,
                            `send_time`, `expire_time`, `attachments_json`)
VALUES (1, '关于2023年暑假放假安排的通知',
        '根据学校安排，2023年暑假将于7月15日开始，9月1日结束，请各位同学做好假期规划...',
        (SELECT id FROM `user` WHERE username = 'admin'), (SELECT id FROM `user` WHERE username = 'admin'),
        '系统管理员', 1, '学校通知', 1, '全体', NULL, 1, 1, 245, NOW(), '2023-07-15 00:00:00',
        '[{"name":"暑假安排详情.pdf", "url":"/uploads/files/summer_schedule.pdf", "size":1024}]'),
       (2, '计算机学院2023级新生入学指南',
        '欢迎2023级新同学加入计算机学院大家庭！本通知包含入学报到流程、宿舍分配等重要信息...',
        (SELECT id FROM `user` WHERE username = 'teacher1'), (SELECT id FROM `user` WHERE username = 'teacher1'),
        '张老师', 2, '学院通知', 2, '学院', 1, 1, 0, 120, NOW(), '2023-09-01 00:00:00', NULL),
       (3, '软件工程期中考试安排', '软件工程课程期中考试将于第10周周三下午14:00在A102教室进行，请携带学生证准时参加...',
        (SELECT id FROM `user` WHERE username = 'teacher2'), (SELECT id FROM `user` WHERE username = 'teacher2'),
        '李老师', 3, '考试通知', 3, '班级', 2, 1, 0, 45, NOW(), '2023-05-10 00:00:00', NULL);
ALTER TABLE `notification`
    AUTO_INCREMENT = 4;


-- 插入通知接收者数据 (使用子查询)
-- 假设通知1发给所有用户，通知2发给学生1和学生2，通知3发给学生2
INSERT INTO `notification_receiver` (`notification_id`, `receiver_id`, `is_read`)
SELECT (SELECT id FROM `notification` WHERE title LIKE '关于2023年暑假%'), id, 0
FROM `user`;

INSERT INTO `notification_receiver` (`notification_id`, `receiver_id`, `is_read`)
VALUES ((SELECT id FROM `notification` WHERE title LIKE '计算机学院2023级%'),
        (SELECT id FROM `user` WHERE username = 'student1'), 0),
       ((SELECT id FROM `notification` WHERE title LIKE '计算机学院2023级%'),
        (SELECT id FROM `user` WHERE username = 'student2'), 0);

INSERT INTO `notification_receiver` (`notification_id`, `receiver_id`, `is_read`)
VALUES ((SELECT id FROM `notification` WHERE title LIKE '软件工程期中考试安排%'),
        (SELECT id FROM `user` WHERE username = 'student2'), 1); -- 假设学生2已读