-- 插入用户数据（密码为123456的MD5值）
INSERT INTO `user` (`username`, `password`, `real_name`, `gender`, `email`, `phone`, `avatar`, `user_type`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 'admin@example.com', '13800138000', '/avatar/admin.jpg', 0, 1),
('teacher1', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 1, 'teacher1@example.com', '13800138001', '/avatar/teacher1.jpg', 2, 1),
('teacher2', 'e10adc3949ba59abbe56e057f20f883e', '李老师', 0, 'teacher2@example.com', '13800138002', '/avatar/teacher2.jpg', 2, 1),
('student1', 'e10adc3949ba59abbe56e057f20f883e', '王同学', 1, 'student1@example.com', '13800138003', '/avatar/student1.jpg', 1, 1),
('student2', 'e10adc3949ba59abbe56e057f20f883e', '赵同学', 0, 'student2@example.com', '13800138004', '/avatar/student2.jpg', 1, 1); 