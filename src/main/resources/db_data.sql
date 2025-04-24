# 初始数据部分

-- 插入部门数据
INSERT INTO `department` (`id`, `name`, `parent_id`, `introduction`, `office_location`, `contact_phone`, `contact_email`, `website_url`)
SELECT 1, '信息工程学院', NULL, '包含计算机科学、软件工程等专业', 'A座401', '010-12345678', 'cs@example.com', 'http://cs.example.com'
WHERE NOT EXISTS (SELECT 1 FROM `department` WHERE `id` = 1);

INSERT INTO `department` (`id`, `name`, `parent_id`, `introduction`, `office_location`, `contact_phone`, `contact_email`)
SELECT 2, '计算机科学系', 1, '计算机科学与技术专业', 'A座402', '010-12345679', 'cs-dept@example.com'
WHERE NOT EXISTS (SELECT 1 FROM `department` WHERE `id` = 2);

INSERT INTO `department` (`id`, `name`, `parent_id`, `introduction`, `office_location`, `contact_phone`, `contact_email`)
SELECT 3, '软件工程系', 1, '软件工程专业', 'A座403', '010-12345680', 'se-dept@example.com'
WHERE NOT EXISTS (SELECT 1 FROM `department` WHERE `id` = 3);

INSERT INTO `department` (`id`, `name`, `parent_id`, `introduction`, `office_location`, `contact_phone`, `contact_email`)
SELECT 4, '数学与统计学院', NULL, '数学与统计相关专业', 'B座201', '010-87654321', 'math@example.com'
WHERE NOT EXISTS (SELECT 1 FROM `department` WHERE `id` = 4);

INSERT INTO `department` (`id`, `name`, `parent_id`, `introduction`, `office_location`, `contact_phone`, `contact_email`)
SELECT 5, '应用数学系', 4, '应用数学专业', 'B座202', '010-87654322', 'amath-dept@example.com'
WHERE NOT EXISTS (SELECT 1 FROM `department` WHERE `id` = 5);

ALTER TABLE `department` AUTO_INCREMENT = 6;

-- 插入用户数据 (密码统一为 '123456')
INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'admin',        -- username
    '123456',       -- password (plain text)
    '系统管理员',      -- real_name
    1,              -- gender (Male)
    'admin@example.com', -- email
    '13800138000',   -- phone
    'avatar/admin.jpg', -- avatar_url
    'Admin',        -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'teacher1',     -- username
    '123456',       -- password
    '张老师',         -- real_name
    1,              -- gender (Male)
    'teacher1@example.com', -- email
    '13800138001',   -- phone
    'avatar/teacher1.jpg', -- avatar_url
    'Teacher',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'teacher1');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'teacher2',     -- username
    '123456',       -- password
    '李老师',         -- real_name
    0,              -- gender (Female)
    'teacher2@example.com', -- email
    '13800138002',   -- phone
    'avatar/teacher2.jpg', -- avatar_url
    'Teacher',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'teacher2');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'teacher3',     -- username
    '123456',       -- password
    '周教授',         -- real_name (Updated from previous insert)
    1,              -- gender (Male)
    'zhouprof@example.com', -- email (Updated)
    '13600004444',   -- phone (Updated)
    'avatar/teacher3.jpg', -- avatar_url
    'Teacher',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'teacher3');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'teacher4',     -- username
    '123456',       -- password
    '张教授',         -- real_name (Updated)
    0,              -- gender (Female)
    'zhangprof@example.com', -- email (Updated)
    '13500005555',   -- phone (Updated)
    'avatar/teacher4.jpg', -- avatar_url
    'Teacher',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'teacher4');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'student1',     -- username
    '123456',       -- password
    '王同学',         -- real_name
    1,              -- gender (Male)
    'student1@example.com', -- email
    '13800138003',   -- phone
    'avatar/student1.jpg', -- avatar_url
    'Student',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'student1');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'student2',     -- username
    '123456',       -- password
    '赵同学',         -- real_name
    0,              -- gender (Female)
    'student2@example.com', -- email
    '13800138004',   -- phone
    'avatar/student2.jpg', -- avatar_url
    'Student',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'student2');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'student3',     -- username
    '123456',       -- password
    '李明',         -- real_name (Updated)
    1,              -- gender (Male)
    'liming@example.com', -- email (Updated)
    '13900001111',   -- phone (Updated)
    'avatar/student3.jpg', -- avatar_url
    'Student',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'student3');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'student4',     -- username
    '123456',       -- password
    '王芳',         -- real_name (Updated)
    0,              -- gender (Female)
    'wangfang@example.com', -- email (Updated)
    '13800002222',   -- phone (Updated)
    'avatar/student4.jpg', -- avatar_url
    'Student',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'student4');

INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar_url`, `user_type`, `status`)
SELECT
    'student5',     -- username
    '123456',       -- password
    '赵强',         -- real_name (Updated)
    1,              -- gender (Male)
    'zhaoqiang@example.com', -- email (Updated)
    '13700003333',   -- phone (Updated)
    'avatar/student5.jpg', -- avatar_url
    'Student',      -- user_type
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'student5');

-- 插入教师数据
INSERT INTO `teacher` (`user_id`, `teacher_no`, `department_id`, `title`, `research_area`, `status`, `hire_date`)
SELECT
    (SELECT id FROM `user` WHERE username = 'teacher1'), -- user_id
    'T001',         -- teacher_no
    (SELECT id FROM `department` WHERE name = '计算机科学系'), -- department_id
    '副教授',         -- title
    '计算机网络, 数据挖掘', -- research_area
    'Active',       -- status
    '2018-07-01'    -- hire_date
WHERE NOT EXISTS (SELECT 1 FROM `teacher` WHERE `teacher_no` = 'T001');

INSERT INTO `teacher` (`user_id`, `teacher_no`, `department_id`, `title`, `research_area`, `status`, `hire_date`)
SELECT
    (SELECT id FROM `user` WHERE username = 'teacher2'), -- user_id
    'T002',         -- teacher_no
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    '讲师',           -- title
    '软件工程, Web开发', -- research_area
    'Active',       -- status
    '2020-08-15'    -- hire_date
WHERE NOT EXISTS (SELECT 1 FROM `teacher` WHERE `teacher_no` = 'T002');

INSERT INTO `teacher` (`user_id`, `teacher_no`, `department_id`, `title`, `status`, `hire_date`)
SELECT
    (SELECT id FROM `user` WHERE username = 'teacher3'), -- user_id
    'T003',         -- teacher_no
    (SELECT id FROM `department` WHERE name = '应用数学系'), -- department_id
    '副教授',         -- title
    'Active',       -- status
    '2010-07-01'    -- hire_date
WHERE NOT EXISTS (SELECT 1 FROM `teacher` WHERE `teacher_no` = 'T003');

INSERT INTO `teacher` (`user_id`, `teacher_no`, `department_id`, `title`, `status`, `hire_date`)
SELECT
    (SELECT id FROM `user` WHERE username = 'teacher4'), -- user_id
    'T004',         -- teacher_no
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    '教授',           -- title
    'Active',       -- status
    '2005-07-01'    -- hire_date
WHERE NOT EXISTS (SELECT 1 FROM `teacher` WHERE `teacher_no` = 'T004');

-- 更新部门负责人 director_id (在 teacher 插入后)
UPDATE `department` SET `director_id` = (SELECT id FROM `user` WHERE username = 'teacher1') WHERE `id` = 2 AND `director_id` IS NULL;
UPDATE `department` SET `director_id` = (SELECT id FROM `user` WHERE username = 'teacher2') WHERE `id` = 3 AND `director_id` IS NULL;

-- 插入班级数据
INSERT INTO `clazz` (`name`, `grade`, `department_id`, `instructor_id`)
SELECT
    '计算机2301班', -- name
    '2023',         -- grade
    (SELECT id FROM `department` WHERE name = '计算机科学系'), -- department_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T001')  -- instructor_id
WHERE NOT EXISTS (SELECT 1 FROM `clazz` WHERE `name` = '计算机2301班');

INSERT INTO `clazz` (`name`, `grade`, `department_id`, `instructor_id`)
SELECT
    '软件2301班',   -- name (修正之前的 2302)
    '2023',         -- grade
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T002')  -- instructor_id
WHERE NOT EXISTS (SELECT 1 FROM `clazz` WHERE `name` = '软件2301班');

INSERT INTO `clazz` (`name`, `grade`, `department_id`, `instructor_id`)
SELECT
    '数学2302班',   -- name
    '2023',         -- grade
    (SELECT id FROM `department` WHERE name = '应用数学系'), -- department_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T003')  -- instructor_id (假设 T003 负责)
WHERE NOT EXISTS (SELECT 1 FROM `clazz` WHERE `name` = '数学2302班');


-- 插入学生数据
INSERT INTO `student` (`user_id`, `student_no`, `department_id`, `class_id`, `status`, `enrollment_date`, `major`)
SELECT
    (SELECT id FROM `user` WHERE username = 'student1'), -- user_id
    '2023001',      -- student_no
    (SELECT id FROM `department` WHERE name = '计算机科学系'), -- department_id
    (SELECT id FROM `clazz` WHERE name = '计算机2301班'), -- class_id
    'Active',       -- status
    '2023-09-01',   -- enrollment_date
    '计算机科学与技术' -- major
WHERE NOT EXISTS (SELECT 1 FROM `student` WHERE `student_no` = '2023001');

INSERT INTO `student` (`user_id`, `student_no`, `department_id`, `class_id`, `status`, `enrollment_date`, `major`)
SELECT
    (SELECT id FROM `user` WHERE username = 'student2'), -- user_id
    '2023002',      -- student_no
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    (SELECT id FROM `clazz` WHERE name = '软件2301班'), -- class_id
    'Active',       -- status
    '2023-09-01',   -- enrollment_date
    '软件工程'       -- major
WHERE NOT EXISTS (SELECT 1 FROM `student` WHERE `student_no` = '2023002');

INSERT INTO `student` (`user_id`, `student_no`, `department_id`, `class_id`, `status`, `enrollment_date`, `graduation_date`, `major`)
SELECT
    (SELECT id FROM `user` WHERE username = 'student3'), -- user_id
    '2023240301',   -- student_no (Updated)
    (SELECT id FROM `department` WHERE name = '应用数学系'), -- department_id
    (SELECT id FROM `clazz` WHERE name = '数学2302班'), -- class_id
    'Active',       -- status
    '2023-09-01',   -- enrollment_date
    '2027-07-01',   -- graduation_date
    '应用数学'       -- major
WHERE NOT EXISTS (SELECT 1 FROM `student` WHERE `student_no` = '2023240301');

INSERT INTO `student` (`user_id`, `student_no`, `department_id`, `class_id`, `status`, `enrollment_date`, `graduation_date`, `major`)
SELECT
    (SELECT id FROM `user` WHERE username = 'student4'), -- user_id
    '2023130401',   -- student_no (Updated)
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    (SELECT id FROM `clazz` WHERE name = '软件2301班'), -- class_id
    'Active',       -- status
    '2023-09-01',   -- enrollment_date
    '2027-07-01',   -- graduation_date
    '软件工程'       -- major
WHERE NOT EXISTS (SELECT 1 FROM `student` WHERE `student_no` = '2023130401');

INSERT INTO `student` (`user_id`, `student_no`, `department_id`, `class_id`, `status`, `enrollment_date`, `graduation_date`, `major`)
SELECT
    (SELECT id FROM `user` WHERE username = 'student5'), -- user_id
    '2023130402',   -- student_no (Updated)
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    (SELECT id FROM `clazz` WHERE name = '软件2301班'), -- class_id
    'Active',       -- status
    '2023-09-01',   -- enrollment_date
    '2027-07-01',   -- graduation_date
    '软件工程'       -- major
WHERE NOT EXISTS (SELECT 1 FROM `student` WHERE `student_no` = '2023130402');


-- 插入学期数据
INSERT INTO `term` (`term_name`, `code`, `start_date`, `end_date`, `current`, `status`)
SELECT '2023-2024学年第一学期', '2023-2024-1', '2023-09-04', '2024-01-12', 1, 1 -- 假设当前是这个学期
WHERE NOT EXISTS (SELECT 1 FROM `term` WHERE `code` = '2023-2024-1');

INSERT INTO `term` (`term_name`, `code`, `start_date`, `end_date`, `current`, `status`)
SELECT '2023-2024学年第二学期', '2023-2024-2', '2024-02-26', '2024-07-05', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `term` WHERE `code` = '2023-2024-2');

INSERT INTO `term` (`term_name`, `code`, `start_date`, `end_date`, `current`, `status`)
SELECT '2024-2025学年第一学期', '2024-2025-1', '2024-09-02', '2025-01-10', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `term` WHERE `code` = '2024-2025-1');

ALTER TABLE `term` AUTO_INCREMENT = 4;

-- 插入教室数据
INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
SELECT 'A101', 60, 'A教学楼', 1, 2 -- 多媒体
WHERE NOT EXISTS (SELECT 1 FROM `classroom` WHERE `name` = 'A101' AND `building` = 'A教学楼');

INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
SELECT 'A102', 120, 'A教学楼', 1, 2 -- 多媒体
WHERE NOT EXISTS (SELECT 1 FROM `classroom` WHERE `name` = 'A102' AND `building` = 'A教学楼');

INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
SELECT 'B201', 80, 'B教学楼', 1, 1 -- 普通
WHERE NOT EXISTS (SELECT 1 FROM `classroom` WHERE `name` = 'B201' AND `building` = 'B教学楼');

INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
SELECT 'C301', 40, 'C教学楼', 1, 3 -- 实验室
WHERE NOT EXISTS (SELECT 1 FROM `classroom` WHERE `name` = 'C301' AND `building` = 'C教学楼');

INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
SELECT '101', 50, '数学楼', 1, 1 -- 普通 (用于新加的课程)
WHERE NOT EXISTS (SELECT 1 FROM `classroom` WHERE `name` = '101' AND `building` = '数学楼');

INSERT INTO `classroom` (`name`, `capacity`, `building`, `status`, `room_type`)
SELECT '204', 70, '软件楼', 1, 2 -- 多媒体 (用于新加的课程)
WHERE NOT EXISTS (SELECT 1 FROM `classroom` WHERE `name` = '204' AND `building` = '软件楼');


ALTER TABLE `classroom` AUTO_INCREMENT = 7;

-- 插入课程数据
INSERT INTO `course` (`course_name`, `course_code`, `department_id`, `credit`, `hours`, `course_type`, `introduction`, `teacher_id`, `status`)
SELECT
    '计算机网络',     -- course_name
    'CS301',        -- course_code (修正之前的 CS301-2023)
    (SELECT id FROM `department` WHERE name = '计算机科学系'), -- department_id
    3.0,            -- credit
    48,             -- hours
    1,              -- course_type (必修)
    '计算机网络基础课程，主要介绍网络协议和网络编程', -- introduction
    (SELECT id FROM `teacher` WHERE teacher_no = 'T001'), -- teacher_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `course` WHERE `course_code` = 'CS301');

INSERT INTO `course` (`course_name`, `course_code`, `department_id`, `credit`, `hours`, `course_type`, `introduction`, `teacher_id`, `status`)
SELECT
    '数据结构与算法', -- course_name
    'CS201',        -- course_code (修正)
    (SELECT id FROM `department` WHERE name = '计算机科学系'), -- department_id
    4.0,            -- credit
    64,             -- hours
    1,              -- course_type (必修)
    '数据结构与算法分析，包括常见数据结构和算法设计', -- introduction
    (SELECT id FROM `teacher` WHERE teacher_no = 'T001'), -- teacher_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `course` WHERE `course_code` = 'CS201');

INSERT INTO `course` (`course_name`, `course_code`, `department_id`, `credit`, `hours`, `course_type`, `introduction`, `teacher_id`, `status`)
SELECT
    '软件工程',       -- course_name
    'SE302',        -- course_code (修正)
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    3.5,            -- credit
    56,             -- hours
    1,              -- course_type (必修)
    '软件开发流程、需求分析、设计模式等内容', -- introduction
    (SELECT id FROM `teacher` WHERE teacher_no = 'T002'), -- teacher_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `course` WHERE `course_code` = 'SE302');

INSERT INTO `course` (`course_name`, `course_code`, `department_id`, `credit`, `hours`, `course_type`, `introduction`, `teacher_id`, `status`)
SELECT
    '高等数学',       -- course_name
    'MA101',        -- course_code (修正)
    (SELECT id FROM `department` WHERE name = '应用数学系'), -- department_id
    5.0,            -- credit
    80,             -- hours
    1,              -- course_type (必修)
    '微积分、线性代数等高等数学基础', -- introduction
    (SELECT id FROM `teacher` WHERE teacher_no = 'T003'), -- teacher_id (假设 T003 教高数)
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `course` WHERE `course_code` = 'MA101');

INSERT INTO `course` (`course_name`, `course_code`, `department_id`, `credit`, `hours`, `course_type`, `introduction`, `teacher_id`, `status`)
SELECT
    'Web前端开发',    -- course_name
    'SE2001',       -- course_code
    (SELECT id FROM `department` WHERE name = '软件工程系'), -- department_id
    3.0,            -- credit
    48,             -- hours
    2,              -- course_type (选修)
    '学习HTML、CSS、JavaScript等Web前端技术及主流框架应用。', -- introduction
    (SELECT id FROM `teacher` WHERE teacher_no = 'T004'), -- teacher_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `course` WHERE `course_code` = 'SE2001');

ALTER TABLE `course` AUTO_INCREMENT = 6;

-- 插入课程选课数据
INSERT INTO `course_selection` (`student_id`, `course_id`, `term_id`, `selection_time`, `status`)
SELECT
    (SELECT id FROM `student` WHERE student_no = '2023001'), -- student_id
    (SELECT id FROM `course` WHERE course_code = 'CS301'),   -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),      -- term_id
    NOW(),          -- selection_time
    'Selected'      -- status
WHERE NOT EXISTS (SELECT 1 FROM `course_selection` WHERE student_id = (SELECT id FROM `student` WHERE student_no = '2023001') AND course_id = (SELECT id FROM `course` WHERE course_code = 'CS301') AND term_id = (SELECT id FROM `term` WHERE code = '2023-2024-1'));

INSERT INTO `course_selection` (`student_id`, `course_id`, `term_id`, `selection_time`, `status`)
SELECT
    (SELECT id FROM `student` WHERE student_no = '2023001'), -- student_id
    (SELECT id FROM `course` WHERE course_code = 'CS201'),   -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),      -- term_id
    NOW(),          -- selection_time
    'Selected'      -- status
WHERE NOT EXISTS (SELECT 1 FROM `course_selection` WHERE student_id = (SELECT id FROM `student` WHERE student_no = '2023001') AND course_id = (SELECT id FROM `course` WHERE course_code = 'CS201') AND term_id = (SELECT id FROM `term` WHERE code = '2023-2024-1'));

INSERT INTO `course_selection` (`student_id`, `course_id`, `term_id`, `selection_time`, `status`)
SELECT
    (SELECT id FROM `student` WHERE student_no = '2023002'), -- student_id
    (SELECT id FROM `course` WHERE course_code = 'CS201'),   -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),      -- term_id
    NOW(),          -- selection_time
    'Selected'      -- status
WHERE NOT EXISTS (SELECT 1 FROM `course_selection` WHERE student_id = (SELECT id FROM `student` WHERE student_no = '2023002') AND course_id = (SELECT id FROM `course` WHERE course_code = 'CS201') AND term_id = (SELECT id FROM `term` WHERE code = '2023-2024-1'));

INSERT INTO `course_selection` (`student_id`, `course_id`, `term_id`, `selection_time`, `status`)
SELECT
    (SELECT id FROM `student` WHERE student_no = '2023002'), -- student_id
    (SELECT id FROM `course` WHERE course_code = 'SE302'),   -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),      -- term_id
    NOW(),          -- selection_time
    'Selected'      -- status
WHERE NOT EXISTS (SELECT 1 FROM `course_selection` WHERE student_id = (SELECT id FROM `student` WHERE student_no = '2023002') AND course_id = (SELECT id FROM `course` WHERE course_code = 'SE302') AND term_id = (SELECT id FROM `term` WHERE code = '2023-2024-1'));

INSERT INTO `course_selection` (`student_id`, `course_id`, `term_id`, `selection_time`, `status`)
SELECT
    (SELECT id FROM `student` WHERE student_no = '2023240301'), -- student_id (student3)
    (SELECT id FROM `course` WHERE course_code = 'MA101'),   -- course_id (高等数学)
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),      -- term_id
    '2023-09-05',   -- selection_time
    'Selected'      -- status
WHERE NOT EXISTS (SELECT 1 FROM `course_selection` WHERE student_id = (SELECT id FROM `student` WHERE student_no = '2023240301') AND course_id = (SELECT id FROM `course` WHERE course_code = 'MA101') AND term_id = (SELECT id FROM `term` WHERE code = '2023-2024-1'));

INSERT INTO `course_selection` (`student_id`, `course_id`, `term_id`, `selection_time`, `status`)
SELECT
    (SELECT id FROM `student` WHERE student_no = '2023130401'), -- student_id (student4)
    (SELECT id FROM `course` WHERE course_code = 'SE2001'),   -- course_id (Web前端)
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),      -- term_id
    '2023-09-05',   -- selection_time
    'Selected'      -- status
WHERE NOT EXISTS (SELECT 1 FROM `course_selection` WHERE student_id = (SELECT id FROM `student` WHERE student_no = '2023130401') AND course_id = (SELECT id FROM `course` WHERE course_code = 'SE2001') AND term_id = (SELECT id FROM `term` WHERE code = '2023-2024-1'));

INSERT INTO `course_selection` (`student_id`, `course_id`, `term_id`, `selection_time`, `status`)
SELECT
    (SELECT id FROM `student` WHERE student_no = '2023130402'), -- student_id (student5)
    (SELECT id FROM `course` WHERE course_code = 'SE2001'),   -- course_id (Web前端)
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),      -- term_id
    '2023-09-05',   -- selection_time
    'Selected'      -- status
WHERE NOT EXISTS (SELECT 1 FROM `course_selection` WHERE student_id = (SELECT id FROM `student` WHERE student_no = '2023130402') AND course_id = (SELECT id FROM `course` WHERE course_code = 'SE2001') AND term_id = (SELECT id FROM `term` WHERE code = '2023-2024-1'));

-- 插入成绩数据 (关联 course_selection_id)
INSERT INTO `score` (`course_selection_id`, `score_value`, `grade`, `gpa`, `regular_score`, `midterm_score`, `final_exam_score`, `comment`, `evaluation_date`)
SELECT
    cs.id,       -- course_selection_id
    85.50,       -- score_value
    'B',         -- grade
    3.5,         -- gpa
    80.00,       -- regular_score
    85.00,       -- midterm_score
    88.00,       -- final_exam_score
    NULL,        -- comment
    '2024-01-15' -- evaluation_date
FROM `course_selection` cs
JOIN `student` s ON cs.student_id = s.id
JOIN `course` c ON cs.course_id = c.id
JOIN `term` t ON cs.term_id = t.id
WHERE s.student_no = '2023001' AND c.course_code = 'CS301' AND t.code = '2023-2024-1'
AND NOT EXISTS (SELECT 1 FROM `score` WHERE `course_selection_id` = cs.id);

INSERT INTO `score` (`course_selection_id`, `score_value`, `grade`, `gpa`, `regular_score`, `midterm_score`, `final_exam_score`, `comment`, `evaluation_date`)
SELECT
    cs.id,       -- course_selection_id
    92.00,       -- score_value
    'A',         -- grade
    4.0,         -- gpa
    90.00,       -- regular_score
    92.00,       -- midterm_score
    93.00,       -- final_exam_score
    '表现优秀',   -- comment
    '2024-01-15' -- evaluation_date
FROM `course_selection` cs
JOIN `student` s ON cs.student_id = s.id
JOIN `course` c ON cs.course_id = c.id
JOIN `term` t ON cs.term_id = t.id
WHERE s.student_no = '2023001' AND c.course_code = 'CS201' AND t.code = '2023-2024-1'
AND NOT EXISTS (SELECT 1 FROM `score` WHERE `course_selection_id` = cs.id);

INSERT INTO `score` (`course_selection_id`, `score_value`, `grade`, `gpa`, `regular_score`, `midterm_score`, `final_exam_score`, `comment`, `evaluation_date`)
SELECT
    cs.id,       -- course_selection_id
    78.50,       -- score_value
    'C+',        -- grade
    2.8,         -- gpa
    75.00,       -- regular_score
    78.00,       -- midterm_score
    80.00,       -- final_exam_score
    NULL,        -- comment
    '2024-01-15' -- evaluation_date
FROM `course_selection` cs
JOIN `student` s ON cs.student_id = s.id
JOIN `course` c ON cs.course_id = c.id
JOIN `term` t ON cs.term_id = t.id
WHERE s.student_no = '2023002' AND c.course_code = 'CS201' AND t.code = '2023-2024-1'
AND NOT EXISTS (SELECT 1 FROM `score` WHERE `course_selection_id` = cs.id);

INSERT INTO `score` (`course_selection_id`, `score_value`, `grade`, `gpa`, `regular_score`, `midterm_score`, `final_exam_score`, `comment`, `evaluation_date`)
SELECT
    cs.id,       -- course_selection_id
    88.00,       -- score_value
    'B+',        -- grade
    3.8,         -- gpa
    85.00,       -- regular_score
    88.00,       -- midterm_score
    90.00,       -- final_exam_score
    NULL,        -- comment
    '2024-01-15' -- evaluation_date
FROM `course_selection` cs
JOIN `student` s ON cs.student_id = s.id
JOIN `course` c ON cs.course_id = c.id
JOIN `term` t ON cs.term_id = t.id
WHERE s.student_no = '2023002' AND c.course_code = 'SE302' AND t.code = '2023-2024-1'
AND NOT EXISTS (SELECT 1 FROM `score` WHERE `course_selection_id` = cs.id);

INSERT INTO `score` (`course_selection_id`, `score_value`, `grade`, `gpa`, `comment`, `evaluation_date`)
SELECT
    cs.id,       -- course_selection_id
    89.00,       -- score_value
    'B+',        -- grade
    3.7,         -- gpa
    '基础扎实，逻辑清晰，有进步空间。', -- comment
    '2024-01-20' -- evaluation_date
FROM `course_selection` cs
JOIN `student` s ON cs.student_id = s.id
JOIN `course` c ON cs.course_id = c.id
JOIN `term` t ON cs.term_id = t.id
WHERE s.student_no = '2023240301' AND c.course_code = 'MA101' AND t.code = '2023-2024-1'
AND NOT EXISTS (SELECT 1 FROM `score` WHERE `course_selection_id` = cs.id);


-- 插入课表数据
INSERT INTO `schedule` (`course_id`, `term_id`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`, `start_week`, `end_week`, `class_id`, `status`)
SELECT
    (SELECT id FROM `course` WHERE course_code = 'CS301'),    -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),       -- term_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T001'),    -- teacher_id
    (SELECT id FROM `classroom` WHERE name = 'A101' AND building = 'A教学楼'), -- classroom_id
    'Monday',       -- day_of_week
    '08:00:00',     -- start_time
    '09:40:00',     -- end_time
    1,              -- start_week
    16,             -- end_week
    (SELECT id FROM `clazz` WHERE name = '计算机2301班'), -- class_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `schedule` sch
                  JOIN `course` c ON sch.course_id = c.id
                  JOIN `term` t ON sch.term_id = t.id
                  JOIN `classroom` cl ON sch.classroom_id = cl.id
                  WHERE c.course_code = 'CS301' AND t.code = '2023-2024-1' AND cl.name = 'A101' AND cl.building = 'A教学楼' AND sch.day_of_week = 'Monday' AND sch.start_time = '08:00:00');

INSERT INTO `schedule` (`course_id`, `term_id`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`, `start_week`, `end_week`, `class_id`, `status`)
SELECT
    (SELECT id FROM `course` WHERE course_code = 'CS201'),    -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),       -- term_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T001'),    -- teacher_id
    (SELECT id FROM `classroom` WHERE name = 'A102' AND building = 'A教学楼'), -- classroom_id
    'Tuesday',      -- day_of_week
    '10:00:00',     -- start_time
    '11:40:00',     -- end_time
    1,              -- start_week
    16,             -- end_week
    (SELECT id FROM `clazz` WHERE name = '计算机2301班'), -- class_id (假设也给计科班上)
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `schedule` sch
                  JOIN `course` c ON sch.course_id = c.id
                  JOIN `term` t ON sch.term_id = t.id
                  JOIN `classroom` cl ON sch.classroom_id = cl.id
                  WHERE c.course_code = 'CS201' AND t.code = '2023-2024-1' AND cl.name = 'A102' AND cl.building = 'A教学楼' AND sch.day_of_week = 'Tuesday' AND sch.start_time = '10:00:00');

INSERT INTO `schedule` (`course_id`, `term_id`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`, `start_week`, `end_week`, `class_id`, `status`)
SELECT
    (SELECT id FROM `course` WHERE course_code = 'SE302'),    -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),       -- term_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T002'),    -- teacher_id
    (SELECT id FROM `classroom` WHERE name = 'B201' AND building = 'B教学楼'), -- classroom_id
    'Wednesday',    -- day_of_week
    '14:00:00',     -- start_time
    '15:40:00',     -- end_time
    1,              -- start_week
    16,             -- end_week
    (SELECT id FROM `clazz` WHERE name = '软件2301班'), -- class_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `schedule` sch
                  JOIN `course` c ON sch.course_id = c.id
                  JOIN `term` t ON sch.term_id = t.id
                  JOIN `classroom` cl ON sch.classroom_id = cl.id
                  WHERE c.course_code = 'SE302' AND t.code = '2023-2024-1' AND cl.name = 'B201' AND cl.building = 'B教学楼' AND sch.day_of_week = 'Wednesday' AND sch.start_time = '14:00:00');

INSERT INTO `schedule` (`course_id`, `term_id`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`, `start_week`, `end_week`, `class_id`, `status`)
SELECT
    (SELECT id FROM `course` WHERE course_code = 'MA101'),    -- course_id
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),       -- term_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T003'),    -- teacher_id
    (SELECT id FROM `classroom` WHERE name = 'C301' AND building = 'C教学楼'), -- classroom_id
    'Thursday',     -- day_of_week
    '16:00:00',     -- start_time
    '17:40:00',     -- end_time
    1,              -- start_week
    16,             -- end_week
    (SELECT id FROM `clazz` WHERE name = '数学2302班'), -- class_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `schedule` sch
                  JOIN `course` c ON sch.course_id = c.id
                  JOIN `term` t ON sch.term_id = t.id
                  JOIN `classroom` cl ON sch.classroom_id = cl.id
                  WHERE c.course_code = 'MA101' AND t.code = '2023-2024-1' AND cl.name = 'C301' AND cl.building = 'C教学楼' AND sch.day_of_week = 'Thursday' AND sch.start_time = '16:00:00');

INSERT INTO `schedule` (`course_id`, `term_id`, `teacher_id`, `classroom_id`, `day_of_week`, `start_time`, `end_time`, `start_week`, `end_week`, `class_id`, `status`)
SELECT
    (SELECT id FROM `course` WHERE course_code = 'SE2001'),   -- course_id (Web前端)
    (SELECT id FROM `term` WHERE code = '2023-2024-1'),       -- term_id
    (SELECT id FROM `teacher` WHERE teacher_no = 'T004'),    -- teacher_id
    (SELECT id FROM `classroom` WHERE name = '204' AND building = '软件楼'), -- classroom_id
    'Wednesday',    -- day_of_week
    '14:00:00',     -- start_time
    '15:40:00',     -- end_time
    1,              -- start_week
    16,             -- end_week
    (SELECT id FROM `clazz` WHERE name = '软件2301班'), -- class_id
    'Active'        -- status
WHERE NOT EXISTS (SELECT 1 FROM `schedule` sch
                  JOIN `course` c ON sch.course_id = c.id
                  JOIN `term` t ON sch.term_id = t.id
                  JOIN `classroom` cl ON sch.classroom_id = cl.id
                  WHERE c.course_code = 'SE2001' AND t.code = '2023-2024-1' AND cl.name = '204' AND cl.building = '软件楼' AND sch.day_of_week = 'Wednesday' AND sch.start_time = '14:00:00');


-- 插入活动数据
INSERT INTO `activity` (`title`, `description`, `type`, `poster_url`, `organizer_id`, `publisher_id`, `organizer_name`, `contact`, `location`, `start_time`, `end_time`, `max_participants`, `status`, `registration_deadline`)
SELECT
    '校园歌手大赛',                         -- title
    '一年一度的校园歌手大赛来啦！欢迎各位有才艺的同学踊跃报名参加...', -- description
    'Competition',                        -- type
    'uploads/posters/singer.jpg',         -- poster_url
    (SELECT id FROM `user` WHERE username = 'admin'), -- organizer_id
    (SELECT id FROM `user` WHERE username = 'admin'), -- publisher_id
    '系统管理员',                           -- organizer_name
    '13800138000',                        -- contact
    '大学生活动中心',                       -- location
    '2024-05-15 18:30:00',                -- start_time (Updated year)
    '2024-05-15 22:00:00',                -- end_time (Updated year)
    200,                                  -- max_participants
    'Active',                             -- status
    '2024-05-15 23:59:59'                 -- registration_deadline
WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `title` = '校园歌手大赛' AND YEAR(start_time) = 2024);

INSERT INTO `activity` (`title`, `description`, `type`, `poster_url`, `organizer_id`, `publisher_id`, `organizer_name`, `contact`, `location`, `start_time`, `end_time`, `max_participants`, `status`, `registration_deadline`)
SELECT
    '职业发展讲座：IT行业就业指南',           -- title
    '邀请知名IT企业HR和技术专家，分享IT行业就业经验和职业规划建议...', -- description
    'Lecture',                            -- type
    'uploads/posters/it_job.jpg',         -- poster_url
    (SELECT id FROM `user` WHERE username = 'teacher1'), -- organizer_id
    (SELECT id FROM `user` WHERE username = 'teacher1'), -- publisher_id
    '张老师',                               -- organizer_name
    '13800138001',                        -- contact
    '教学楼A栋报告厅',                      -- location
    '2024-05-20 14:00:00',                -- start_time (Updated year)
    '2024-05-20 16:30:00',                -- end_time (Updated year)
    150,                                  -- max_participants
    'Active',                             -- status
    '2024-05-20 23:59:59'                 -- registration_deadline
WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `title` = '职业发展讲座：IT行业就业指南' AND YEAR(start_time) = 2024);

INSERT INTO `activity` (`title`, `description`, `type`, `poster_url`, `organizer_id`, `publisher_id`, `organizer_name`, `contact`, `location`, `start_time`, `end_time`, `max_participants`, `status`, `registration_deadline`)
SELECT
    '软件创新大赛',                         -- title
    '面向全校学生的软件创新设计比赛，鼓励创意与技术结合。', -- description
    'Competition',                        -- type
    NULL,                                 -- poster_url
    (SELECT id FROM `user` WHERE username = 'teacher4'), -- organizer_id
    (SELECT id FROM `user` WHERE username = 'teacher4'), -- publisher_id
    '张教授',                               -- organizer_name
    '13500005555',                        -- contact
    '计算机科学楼报告厅',                   -- location
    '2024-05-20 09:00:00',                -- start_time
    '2024-05-20 17:00:00',                -- end_time
    50,                                   -- max_participants
    'Active',                             -- status
    '2024-05-19 23:59:59'                 -- registration_deadline
WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `title` = '软件创新大赛');


-- 插入活动参与者数据
INSERT INTO `activity_participant` (`activity_id`, `user_id`, `registration_time`, `status`)
SELECT
    (SELECT id FROM `activity` WHERE title = '校园歌手大赛' AND YEAR(start_time) = 2024), -- activity_id
    (SELECT id FROM `user` WHERE username = 'student1'), -- user_id
    '2024-04-10 14:30:00',                -- registration_time
    'Confirmed'                           -- status
WHERE (SELECT id FROM `activity` WHERE title = '校园歌手大赛' AND YEAR(start_time) = 2024) IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `activity_participant` ap JOIN `activity` a ON ap.activity_id = a.id WHERE a.title = '校园歌手大赛' AND YEAR(a.start_time) = 2024 AND ap.user_id = (SELECT id FROM `user` WHERE username = 'student1'));

INSERT INTO `activity_participant` (`activity_id`, `user_id`, `registration_time`, `status`)
SELECT
    (SELECT id FROM `activity` WHERE title = '软件创新大赛'), -- activity_id
    (SELECT id FROM `user` WHERE username = 'student4'), -- user_id
    '2024-04-20 15:30:00',                -- registration_time
    'Confirmed'                           -- status
WHERE (SELECT id FROM `activity` WHERE title = '软件创新大赛') IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `activity_participant` ap JOIN `activity` a ON ap.activity_id = a.id WHERE a.title = '软件创新大赛' AND ap.user_id = (SELECT id FROM `user` WHERE username = 'student4'));

INSERT INTO `activity_participant` (`activity_id`, `user_id`, `registration_time`, `status`)
SELECT
    (SELECT id FROM `activity` WHERE title = '软件创新大赛'), -- activity_id
    (SELECT id FROM `user` WHERE username = 'student5'), -- user_id
    '2024-04-21 09:15:00',                -- registration_time
    'Confirmed'                           -- status
WHERE (SELECT id FROM `activity` WHERE title = '软件创新大赛') IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `activity_participant` ap JOIN `activity` a ON ap.activity_id = a.id WHERE a.title = '软件创新大赛' AND ap.user_id = (SELECT id FROM `user` WHERE username = 'student5'));


-- 更新活动表中的参与人数 (确保在参与者插入后执行)
UPDATE `activity` a
SET a.`current_participants` = (SELECT COUNT(*) FROM `activity_participant` ap WHERE ap.`activity_id` = a.`id` AND ap.status = 'Confirmed')
WHERE EXISTS (SELECT 1 FROM `activity_participant` ap WHERE ap.`activity_id` = a.`id`);


-- 插入论坛帖子数据
INSERT INTO `post` (`title`, `content`, `user_id`, `category`, `tags`, `status`, `is_top`, `is_essence`)
SELECT
    '计算机网络期末复习资料分享',             -- title
    '整理了一份计算机网络的期末复习资料，包含重点章节、历年真题和模拟题，希望对大家有帮助...', -- content
    (SELECT id FROM `user` WHERE username = 'teacher1'), -- user_id
    'Study',                              -- category
    '["复习资料", "计算机网络", "期末考试"]', -- tags (JSON Array)
    'Active',                             -- status
    1,                                    -- is_top
    1                                     -- is_essence
WHERE NOT EXISTS (SELECT 1 FROM `post` WHERE `title` = '计算机网络期末复习资料分享');

INSERT INTO `post` (`title`, `content`, `user_id`, `category`, `tags`, `status`)
SELECT
    '关于高数难点解析的讨论',               -- title
    '高等数学中的泰勒展开式和傅里叶变换理解起来比较困难，大家有什么好的学习方法或者推荐的资源吗？一起讨论下！', -- content
    (SELECT id FROM `user` WHERE username = 'student1'), -- user_id
    'Study',                              -- category
    '["高等数学", "学习方法", "难点解析"]', -- tags
    'Active'                              -- status
WHERE NOT EXISTS (SELECT 1 FROM `post` WHERE `title` = '关于高数难点解析的讨论');

INSERT INTO `post` (`title`, `content`, `user_id`, `category`, `tags`, `status`)
SELECT
    '高等数学学习心得',                     -- title
    '分享一下我在学习高等数学过程中的一些方法和体会，希望能帮助到大家。首先，理解概念比记忆公式更重要；其次，多做习题是掌握知识的关键；最后，形成学习小组相互讨论可以加深理解。', -- content
    (SELECT id FROM `user` WHERE username = 'student3'), -- user_id
    'Study',                              -- category
    '["高等数学", "学习心得"]',         -- tags
    'Active'                              -- status
WHERE NOT EXISTS (SELECT 1 FROM `post` WHERE `title` = '高等数学学习心得');


-- 插入评论数据
INSERT INTO `comment` (`post_id`, `user_id`, `content`, `status`)
SELECT
    (SELECT id FROM `post` WHERE title = '计算机网络期末复习资料分享'), -- post_id
    (SELECT id FROM `user` WHERE username = 'student1'),           -- user_id
    '感谢老师分享，资料很全面！太及时了！',                         -- content
    'Active'                                                     -- status
WHERE (SELECT id FROM `post` WHERE title = '计算机网络期末复习资料分享') IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `comment` c JOIN `post` p ON c.post_id = p.id WHERE p.title = '计算机网络期末复习资料分享' AND c.user_id = (SELECT id FROM `user` WHERE username = 'student1') AND c.content LIKE '感谢老师分享%');

INSERT INTO `comment` (`post_id`, `user_id`, `content`, `status`)
SELECT
    (SELECT id FROM `post` WHERE title = '高等数学学习心得'),           -- post_id
    (SELECT id FROM `user` WHERE username = 'teacher3'),           -- user_id (周教授)
    '非常好的学习方法总结，特别是关于理解概念的部分。我建议同学们在做习题时注重思考过程，不要急于求解。', -- content
    'Active'                                                     -- status
WHERE (SELECT id FROM `post` WHERE title = '高等数学学习心得') IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `comment` c JOIN `post` p ON c.post_id = p.id WHERE p.title = '高等数学学习心得' AND c.user_id = (SELECT id FROM `user` WHERE username = 'teacher3'));


-- 插入通知数据
INSERT INTO `notification` (`title`, `content`, `sender_id`, `publisher_id`, `publisher_name`, `type`, `priority`, `target_type`, `status`, `is_top`)
SELECT
    '关于2023-2024学年第一学期选课的通知', -- title
    '各位同学，2023-2024学年第一学期选课将于指定日期开始，请及时登录教务系统进行选课操作。详情请查阅附件或咨询教务处。', -- content
    (SELECT id FROM `user` WHERE username = 'admin'), -- sender_id
    (SELECT id FROM `user` WHERE username = 'admin'), -- publisher_id
    '教务处',                               -- publisher_name
    'System',                             -- type
    1,                                    -- priority (重要)
    'All',                                -- target_type
    'Active',                             -- status
    1                                     -- is_top
WHERE NOT EXISTS (SELECT 1 FROM `notification` WHERE `title` = '关于2023-2024学年第一学期选课的通知');

INSERT INTO `notification` (`title`, `content`, `sender_id`, `publisher_id`, `publisher_name`, `type`, `priority`, `target_type`, `target_ids`, `status`)
SELECT
    '软件创新大赛参赛指南',                 -- title
    '各位报名参加软件创新大赛的同学，请注意以下事项：1.比赛当天请携带学生证；2.每个团队演示时间为10分钟；3.请提前准备好演示PPT和演示环境。预祝比赛顺利！', -- content
    (SELECT id FROM `user` WHERE username = 'teacher4'), -- sender_id (张教授)
    (SELECT id FROM `user` WHERE username = 'teacher4'), -- publisher_id
    '张教授',                               -- publisher_name
    'Activity',                           -- type
    0,                                    -- priority (普通)
    'User',                               -- target_type
    (SELECT GROUP_CONCAT(u.id) FROM `user` u JOIN `activity_participant` ap ON u.id = ap.user_id JOIN `activity` a ON ap.activity_id = a.id WHERE a.title = '软件创新大赛'), -- target_ids (查询所有参赛者ID)
    'Active'                              -- status
WHERE (SELECT COUNT(*) FROM `activity_participant` ap JOIN `activity` a ON ap.activity_id = a.id WHERE a.title = '软件创新大赛') > 0
  AND NOT EXISTS (SELECT 1 FROM `notification` WHERE `title` = '软件创新大赛参赛指南');


-- 插入通知接收者数据
-- 为 '选课通知' 添加所有学生和教师为接收者 (示例)
INSERT INTO `notification_receiver` (`notification_id`, `receiver_id`, `read_status`)
SELECT
    (SELECT id FROM `notification` WHERE title = '关于2023-2024学年第一学期选课的通知'), -- notification_id
    u.id,                                         -- receiver_id
    'Unread'                                      -- read_status
FROM `user` u
WHERE u.user_type IN ('Student', 'Teacher')
  AND (SELECT id FROM `notification` WHERE title = '关于2023-2024学年第一学期选课的通知') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `notification_receiver` nr
      WHERE nr.notification_id = (SELECT id FROM `notification` WHERE title = '关于2023-2024学年第一学期选课的通知')
        AND nr.receiver_id = u.id
  );

-- 为 '软件创新大赛参赛指南' 添加参与者为接收者 (student4, student5)
INSERT INTO `notification_receiver` (`notification_id`, `receiver_id`, `read_status`)
SELECT
    (SELECT id FROM `notification` WHERE title = '软件创新大赛参赛指南'), -- notification_id
    ap.user_id,                                     -- receiver_id
    'Unread'                                        -- read_status
FROM `activity_participant` ap
JOIN `activity` a ON ap.activity_id = a.id
WHERE a.title = '软件创新大赛'
  AND (SELECT id FROM `notification` WHERE title = '软件创新大赛参赛指南') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `notification_receiver` nr
      WHERE nr.notification_id = (SELECT id FROM `notification` WHERE title = '软件创新大赛参赛指南')
        AND nr.receiver_id = ap.user_id
  ); 