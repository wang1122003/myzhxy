USE campus_db;

-- 添加更多课程
INSERT INTO `course` (`course_name`, `course_code`, `credit`, `hours`, `course_type`, `introduction`, `teacher_id`,
                      `term_info`, `status`)
VALUES ('操作系统原理', 'CS302', 4.0, 64, 1, '操作系统基本原理、进程管理、内存管理、文件系统等内容', 2, '2024-2025-2', 1),
       ('数据库系统', 'CS401', 4.0, 64, 1, '数据库设计、SQL语言、事务处理与并发控制等内容', 3, '2024-2025-2', 1),
       ('人工智能导论', 'CS501', 3.5, 56, 2, '人工智能基础、搜索算法、机器学习、神经网络等内容', 4, '2024-2025-2', 1),
       ('计算机图形学', 'CS402', 3.0, 48, 2, '图形学基础、渲染技术、三维建模等内容', 2, '2024-2025-2', 1),
       ('程序设计实践', 'SE401', 4.0, 64, 1, '综合性程序设计实践课程，包括各类实用项目开发', 3, '2024-2025-2', 1),
       ('Java高级程序设计', 'SE301', 3.5, 56, 2, 'Java高级特性、多线程编程、网络编程等内容', 4, '2024-2025-2', 1);

-- 为新学生选课
INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `phone`, `email`, `user_type`, `status`, `user_no`)
VALUES ('student4', '123456', '周同学', 0, '13800000007', 'student4@example.com', 'Student', 'Active', '2023004'),
       ('student5', '123456', '吴同学', 1, '13800000008', 'student5@example.com', 'Student', 'Active', '2023005');

-- 添加选课记录
INSERT INTO `course_selection` (`user_id`, `course_id`, `term_info`, `selection_time`, `status`)
VALUES (5, (SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', NOW(), 1),
       (6, (SELECT id FROM `course` WHERE course_code = 'SE401'), '2024-2025-2', NOW(), 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', NOW(), 1),
       (7, (SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', NOW(), 1),
       (8, (SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', NOW(), 1),
       (8, (SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', NOW(), 1),
       (9, (SELECT id FROM `course` WHERE course_code = 'SE401'), '2024-2025-2', NOW(), 1),
       (9, (SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', NOW(), 1);

-- 添加更多教室
INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
VALUES ('D101', 80, 'D教学楼', 1, 2),
       ('D102', 100, 'D教学楼', 1, 1),
       ('E201', 60, 'E教学楼', 1, 2),
       ('实验楼301', 40, '实验楼', 1, 3);

-- 添加排课记录 (包括跨多个节次的长课程)
INSERT INTO `schedule` (`course_id`, `term_info`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`,
                        `start_week`, `end_week`, `status`)
VALUES
    -- 标准长度课程
    ((SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', 2,
     (SELECT id FROM `classroom` WHERE name = 'D101'), 1, '14:00:00', '15:40:00', 1, 16, 1),

    ((SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', 3,
     (SELECT id FROM `classroom` WHERE name = 'D102'), 2, '08:00:00', '09:40:00', 1, 16, 1),

    -- 三节连上的长课程
    ((SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', 4,
     (SELECT id FROM `classroom` WHERE name = 'E201'), 3, '08:00:00', '11:40:00', 1, 16, 1),

    -- 四节连上的实验课
    ((SELECT id FROM `course` WHERE course_code = 'SE401'), '2024-2025-2', 3,
     (SELECT id FROM `classroom` WHERE name = '实验楼301'), 4, '14:00:00', '17:40:00', 1, 16, 1),

    -- 跨越中午的长课程
    ((SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', 2,
     (SELECT id FROM `classroom` WHERE name = 'A101'), 5, '10:00:00', '15:40:00', 1, 16, 1),

    -- 晚上的课程
    ((SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', 4,
     (SELECT id FROM `classroom` WHERE name = 'B201'), 2, '19:00:00', '21:35:00', 1, 16, 1);

-- 为当前学期学生5添加连续上课的课程安排
INSERT INTO `schedule` (`course_id`, `term_info`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`,
                        `start_week`, `end_week`, `status`)
VALUES
    -- 上午1-2节
    ((SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', 4,
     (SELECT id FROM `classroom` WHERE name = 'A101'), 3, '08:00:00', '09:40:00', 1, 16, 1),

    -- 上午3-4节
    ((SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', 2,
     (SELECT id FROM `classroom` WHERE name = 'A102'), 3, '10:00:00', '11:40:00', 1, 16, 1),

    -- 下午5-6节
    ((SELECT id FROM `course` WHERE course_code = 'CS401'), '2024-2025-2', 3,
     (SELECT id FROM `classroom` WHERE name = 'B201'), 3, '14:00:00', '15:40:00', 1, 16, 1),

    -- 下午7-8节
    ((SELECT id FROM `course` WHERE course_code = 'CS302'), '2024-2025-2', 2,
     (SELECT id FROM `classroom` WHERE name = 'C301'), 3, '16:00:00', '17:40:00', 1, 16, 1),

    -- 晚上9-11节
    ((SELECT id FROM `course` WHERE course_code = 'CS501'), '2024-2025-2', 4,
     (SELECT id FROM `classroom` WHERE name = 'D101'), 3, '19:00:00', '21:35:00', 1, 16, 1);

-- 确保学生5选了这些课程
INSERT INTO `course_selection` (`user_id`, `course_id`, `term_info`, `selection_time`, `status`)
VALUES (5, (SELECT id FROM `course` WHERE course_code = 'SE301'), '2024-2025-2', NOW(), 1),
       (5, (SELECT id FROM `course` WHERE course_code = 'CS402'), '2024-2025-2', NOW(), 1);