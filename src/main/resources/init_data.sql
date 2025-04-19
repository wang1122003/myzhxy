-- 插入角色数据
INSERT INTO `role` (`name`, `description`)
VALUES ('ROLE_ADMIN', '系统管理员'),
       ('ROLE_TEACHER', '教师'),
       ('ROLE_STUDENT', '学生');

-- 插入用户数据（密码为123456的MD5值）
INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar`, `user_type`, `status`)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 'admin@example.com', '13800138000',
        '/avatar/admin.jpg', 0, 1),
       ('teacher1', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 1, 'teacher1@example.com', '13800138001',
        '/avatar/teacher1.jpg', 2, 1),
       ('teacher2', 'e10adc3949ba59abbe56e057f20f883e', '李老师', 0, 'teacher2@example.com', '13800138002',
        '/avatar/teacher2.jpg', 2, 1),
       ('student1', 'e10adc3949ba59abbe56e057f20f883e', '王同学', 1, 'student1@example.com', '13800138003',
        '/avatar/student1.jpg', 1, 1),
       ('student2', 'e10adc3949ba59abbe56e057f20f883e', '赵同学', 0, 'student2@example.com', '13800138004',
        '/avatar/student2.jpg', 1, 1);

-- 插入用户角色关联数据
INSERT INTO `user_role` (`user_id`, `role_id`)
VALUES (1, 1), -- admin - ROLE_ADMIN
       (2, 2), -- teacher1 - ROLE_TEACHER
       (3, 2), -- teacher2 - ROLE_TEACHER
       (4, 3), -- student1 - ROLE_STUDENT
       (5, 3);
-- student2 - ROLE_STUDENT

-- 插入部门数据
INSERT INTO `department` (`name`, `parent_id`, `description`)
VALUES ('信息工程学院', NULL, '包含计算机科学、软件工程等专业'),
       ('计算机科学系', 1, '计算机科学与技术专业'),
       ('软件工程系', 1, '软件工程专业'),
       ('数学与统计学院', NULL, '数学与统计相关专业'),
       ('应用数学系', 4, '应用数学专业');

-- 插入教师数据
INSERT INTO `teacher` (`user_id`, `department_id`, `title`)
VALUES (2, 2, '副教授'),
       (3, 3, '讲师');

-- 插入学生数据
INSERT INTO `student` (`user_id`, `department_id`, `student_id`, `class_name`)
VALUES (4, 2, '2023001', '计算机2301班'),
       (5, 3, '2023002', '软件2302班');

-- 插入教室数据
INSERT INTO `classroom` (`name`, `capacity`, `location`, `status`)
VALUES ('A101', 60, 'A教学楼1层', 1),
       ('A102', 120, 'A教学楼1层', 1),
       ('B201', 80, 'B教学楼2层', 1),
       ('C301', 40, 'C教学楼3层', 1);

-- 插入课程数据
INSERT INTO `course` (`name`, `code`, `credit`, `description`, `teacher_id`)
VALUES ('计算机网络', 'CS301', 3, '计算机网络基础课程，主要介绍网络协议和网络编程', 1),
       ('数据结构与算法', 'CS201', 4, '数据结构与算法分析，包括常见数据结构和算法设计', 1),
       ('软件工程', 'SE302', 3, '软件开发流程、需求分析、设计模式等内容', 2),
       ('高等数学', 'MA101', 5, '微积分、线性代数等高等数学基础', 2);

-- 插入课程选课数据
INSERT INTO `course_selection` (`student_id`, `course_id`, `status`)
VALUES (1, 1, 1),
       (1, 2, 1),
       (2, 2, 1),
       (2, 3, 1);

-- 插入成绩数据
INSERT INTO `score` (`student_id`, `course_id`, `score`)
VALUES (1, 1, 85.5),
       (1, 2, 92.0),
       (2, 2, 78.5),
       (2, 3, 88.0);

-- 插入课表数据
INSERT INTO `schedule` (`course_id`, `classroom_id`, `week_day`, `start_time`, `end_time`, `start_week`, `end_week`)
VALUES (1, 1, 1, '08:00:00', '09:40:00', 1, 16),
       (2, 2, 2, '10:00:00', '11:40:00', 1, 16),
       (3, 3, 3, '14:00:00', '15:40:00', 1, 16),
       (4, 4, 4, '16:00:00', '17:40:00', 1, 16);

-- 插入帖子数据（已集成论坛板块信息）
INSERT INTO `post` (`title`, `content`, `author_id`, `forum_type`, `forum_color`, `tags`, `view_count`, `like_count`,
                    `comment_count`, `status`, `is_top`, `is_essence`, `create_time`)
VALUES ('计算机网络期末复习资料分享', '整理了一份计算机网络的期末复习资料，希望对大家有帮助...', 1, '学习交流',
        '#3498db', '[
    "复习资料",
    "计算机网络",
    "期末考试"
  ]', 120, 25, 8, 1, 1, 1, NOW()),
       ('关于高数难点解析的讨论', '高等数学中的泰勒展开式和傅里叶变换理解起来比较困难，有什么好的学习方法吗？', 2,
        '学习交流', '#3498db', '[
         "高等数学",
         "学习方法",
         "难点解析"
       ]', 85, 16, 5, 1, 0, 0, NOW()),
       ('校园美食推荐：隐藏在角落的美味', '发现了一家藏在图书馆后面的小店，他们家的兰州拉面特别正宗...', 3, '校园生活',
        '#e74c3c', '[
         "美食",
         "校园生活",
         "推荐"
       ]', 200, 45, 12, 1, 0, 1, NOW()),
       ('2025届毕业生校园招聘信息汇总', '整理了最近各大企业的校园招聘信息，包括招聘要求、薪资福利等详细内容...', 2,
        '招聘信息', '#2ecc71', '[
         "校招",
         "就业",
         "招聘信息"
       ]', 350, 89, 25, 1, 1, 1, NOW()),
       ('摄影协会周末外拍活动通知', '本周末摄影协会将组织前往市郊森林公园外拍活动，欢迎摄影爱好者参加...', 4, '活动公告',
        '#f39c12', '[
         "摄影",
         "社团活动",
         "外拍"
       ]', 90, 22, 10, 1, 0, 0, NOW()),
       ('心情随笔：大学四年的感悟', '即将毕业，回首四年大学生活，有太多感慨和收获...', 5, '心灵驿站', '#9b59b6', '[
         "随笔",
         "毕业",
         "感悟"
       ]', 78, 36, 9, 1, 0, 1, NOW());

-- 插入评论数据
INSERT INTO `comment` (`post_id`, `content`, `author_id`, `parent_id`, `like_count`, `status`, `create_time`)
VALUES (1, '感谢分享，资料很全面！', 2, NULL, 5, 1, NOW()),
       (1, '请问包含实验部分的内容吗？', 3, NULL, 2, 1, NOW()),
       (2, '推荐你看看3Blue1Brown的视频，对理解数学概念很有帮助', 4, NULL, 8, 1, NOW()),
       (3, '这家店确实不错，他们家的牛肉面我吃过，味道很赞！', 1, NULL, 4, 1, NOW()),
       (4, '感谢整理，信息很全面！', 5, NULL, 6, 1, NOW()),
       (1, '不客气，希望对你有帮助！', 1, 1, 3, 1, NOW()),
       (3, '他们家的饺子也很好吃，推荐尝试', 5, 4, 2, 1, NOW());

-- 插入活动数据
INSERT INTO `activity` (`title`, `description`, `activity_type`, `poster`, `organizer_id`, `location`, `start_time`,
                        `end_time`,
                        `max_participants`, `participants`, `participant_count`, `status`, `create_time`)
VALUES ('校园歌手大赛', '一年一度的校园歌手大赛来啦！欢迎各位有才艺的同学踊跃报名参加...', 1,
        '/uploads/posters/singer.jpg',
        1, '大学生活动中心', '2023-05-15 18:30:00', '2023-05-15 22:00:00', 200,
        '[
          {
            "userId": 2,
            "name": "张三",
            "avatar": "/uploads/avatars/2.jpg",
            "registerTime": "2023-04-10 14:30:00",
            "status": 1
          },
          {
            "userId": 3,
            "name": "李四",
            "avatar": "/uploads/avatars/3.jpg",
            "registerTime": "2023-04-11 09:15:00",
            "status": 1
          }
        ]',
        2, 1, NOW()),

       ('职业发展讲座：IT行业就业指南', '邀请知名IT企业HR和技术专家，分享IT行业就业经验和职业规划建议...', 3,
        '/uploads/posters/it_job.jpg', 2, '教学楼A栋报告厅', '2023-05-20 14:00:00', '2023-05-20 16:30:00', 150,
        '[
          {
            "userId": 1,
            "name": "管理员",
            "avatar": "/uploads/avatars/admin.jpg",
            "registerTime": "2023-04-12 11:20:00",
            "status": 1
          },
          {
            "userId": 4,
            "name": "王五",
            "avatar": "/uploads/avatars/4.jpg",
            "registerTime": "2023-04-13 16:45:00",
            "status": 1
          },
          {
            "userId": 5,
            "name": "赵六",
            "avatar": "/uploads/avatars/5.jpg",
            "registerTime": "2023-04-14 10:30:00",
            "status": 1
          }
        ]',
        3, 1, NOW()),

       ('篮球友谊赛', '计算机学院vs经管学院篮球友谊赛，欢迎前来观赛助威...', 1,
        '/uploads/posters/basketball.jpg', 3,
        '体育馆', '2023-05-25 15:00:00', '2023-05-25 17:00:00', 300, '[]', 0, 1, NOW());

-- 插入通知数据
INSERT INTO `notification` (`title`, `content`, `sender_id`, `publisher_id`, `publisher_name`, `notice_type`, `type`,
                            `priority`, `target_type`, `target_id`, `status`, `is_top`, `view_count`, `send_time`,
                            `expire_time`, `attachments_json`, `create_time`)
VALUES ('关于2023年暑假放假安排的通知', '根据学校安排，2023年暑假将于7月15日开始，9月1日结束，请各位同学做好假期规划...',
        1, 1, '系统管理员', 1, '学校通知', 1, '全体', NULL, 1, 1, 245, NOW(), '2023-07-15 00:00:00',
        '[{"name":"暑假安排详情.pdf", "url":"/uploads/files/summer_schedule.pdf", "size":1024}]', NOW()),
       ('计算机学院2023级新生入学指南',
        '欢迎2023级新同学加入计算机学院大家庭！本通知包含入学报到流程、宿舍分配等重要信息...', 2, 2, '张老师', 2,
        '学院通知', 2, '学院', 1, 1, 0, 120, NOW(), '2023-09-01 00:00:00', NULL, NOW()),
       ('软件工程期中考试安排', '软件工程课程期中考试将于第10周周三下午14:00在A102教室进行，请携带学生证准时参加...', 3,
        3, '李老师', 3, '考试通知', 3, '班级', 2, 1, 0, 45, NOW(), '2023-05-10 00:00:00', NULL, NOW());