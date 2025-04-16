/**
 * 教师模块UI组件库
 * 提供教师模块所需的UI交互组件
 */
var TeacherUI = {
    /**
     * 初始化教师个人资料页面
     */
    initProfilePage: function() {
        // 从本地存储获取用户信息
        const userStr = localStorage.getItem('user');
        if (!userStr) {
            window.location.href = '/campus/index.html';
            return;
        }
        
        const user = JSON.parse(userStr);
        
        // 填充个人信息表单
        document.getElementById('username').value = user.username || '';
        document.getElementById('realName').value = user.realName || '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('phone').value = user.phone || '';
        document.getElementById('department').value = user.department || '';
        document.getElementById('title').value = user.title || '';
        document.getElementById('officeLocation').value = user.officeLocation || '';
        
        // 显示头像
        if (user.avatar) {
            document.getElementById('avatarPreview').src = user.avatar;
        }
        
        // 绑定表单提交事件
        document.getElementById('profileForm').addEventListener('submit', function(e) {
            e.preventDefault();
            TeacherUI.updateProfile();
        });
        
        // 绑定头像上传事件
        document.getElementById('avatarUpload').addEventListener('change', function(e) {
            TeacherUI.handleAvatarUpload(e);
        });
    },
    
    /**
     * 更新教师个人资料
     */
    updateProfile: function() {
        UI.showLoading();
        
        // 获取表单数据
        const profileData = {
            realName: document.getElementById('realName').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            department: document.getElementById('department').value,
            title: document.getElementById('title').value,
            officeLocation: document.getElementById('officeLocation').value
        };
        
        API.teacher.updateProfile(profileData)
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    // 更新本地存储中的用户信息
                    const userStr = localStorage.getItem('user');
                    if (userStr) {
                        const user = JSON.parse(userStr);
                        Object.assign(user, profileData);
                        localStorage.setItem('user', JSON.stringify(user));
                    }
                    
                    UI.showMessage('个人资料更新成功', 'success');
                } else {
                    UI.showMessage(result.message || '更新失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('更新个人资料失败:', error);
                UI.showMessage('更新个人资料失败: ' + error.message, 'error');
            });
    },
    
    /**
     * 处理头像上传
     * @param {Event} e 上传事件
     */
    handleAvatarUpload: function(e) {
        const file = e.target.files[0];
        if (!file) return;
        
        // 检查文件类型
        if (!file.type.match('image.*')) {
            UI.showMessage('请选择图片文件', 'error');
            return;
        }
        
        // 预览图片
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('avatarPreview').src = e.target.result;
        };
        reader.readAsDataURL(file);
        
        // 上传头像
        UI.showLoading();
        
        const formData = new FormData();
        formData.append('avatar', file);
        
        API.request('/teacher/avatar', 'POST', formData, true)
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    // 更新本地存储中的用户头像
                    const userStr = localStorage.getItem('user');
                    if (userStr) {
                        const user = JSON.parse(userStr);
                        user.avatar = result.data.avatarUrl;
                        localStorage.setItem('user', JSON.stringify(user));
                    }
                    
                    UI.showMessage('头像上传成功', 'success');
                } else {
                    UI.showMessage(result.message || '上传失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('上传头像失败:', error);
                UI.showMessage('上传头像失败: ' + error.message, 'error');
            });
    },
    
    /**
     * 初始化课程列表页面
     */
    initCourseList: function() {
        UI.showLoading();
        
        API.teacher.getCourses()
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    const courses = result.data.list || [];
                    TeacherUI.renderCourseList(courses);
                    
                    // 更新分页
                    TeacherUI.updatePagination(
                        result.data.current || 1,
                        result.data.pages || 1,
                        function(page) {
                            API.teacher.getCourses(page)
                                .then(newResult => {
                                    if (newResult.success) {
                                        TeacherUI.renderCourseList(newResult.data.list || []);
                                    } else {
                                        UI.showMessage(newResult.message || '加载失败', 'error');
                                    }
                                })
                                .catch(error => {
                                    console.error('加载课程失败:', error);
                                    UI.showMessage('加载课程失败: ' + error.message, 'error');
                                });
                        }
                    );
                } else {
                    UI.showMessage(result.message || '加载失败', 'error');
                    document.getElementById('courseList').innerHTML = '<div class="empty-state">加载课程失败</div>';
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('加载课程失败:', error);
                UI.showMessage('加载课程失败: ' + error.message, 'error');
                document.getElementById('courseList').innerHTML = '<div class="empty-state">加载课程失败</div>';
            });
    },
    
    /**
     * 渲染课程列表
     * @param {Array} courses 课程数据数组
     */
    renderCourseList: function(courses) {
        const container = document.getElementById('courseList');
        
        if (!courses || courses.length === 0) {
            container.innerHTML = '<div class="empty-state">暂无课程</div>';
            return;
        }
        
        let html = '';
        
        courses.forEach(course => {
            const studentCount = course.studentCount || 0;
            const capacity = course.capacity || 30;
            const percentage = Math.min(Math.round(studentCount / capacity * 100), 100);
            
            let statusClass = 'success';
            if (percentage >= 90) {
                statusClass = 'danger';
            } else if (percentage >= 70) {
                statusClass = 'warning';
            }
            
            html += `
                <div class="course-card">
                    <div class="course-header">
                        <h3>${course.name || '未命名课程'}</h3>
                        <div class="course-code">${course.code || ''}</div>
                    </div>
                    <div class="course-body">
                        <div class="course-info">
                            <div><strong>学分:</strong> ${course.credit || 0}</div>
                            <div><strong>学生数:</strong> ${studentCount}/${capacity}</div>
                        </div>
                        <div class="progress">
                            <div class="progress-bar bg-${statusClass}" style="width: ${percentage}%" 
                                aria-valuenow="${percentage}" aria-valuemin="0" aria-valuemax="100"></div>
                        </div>
                    </div>
                    <div class="course-footer">
                        <button class="btn btn-sm btn-primary view-course" data-id="${course.id}">查看详情</button>
                        <button class="btn btn-sm btn-info edit-course" data-id="${course.id}">编辑</button>
                        <button class="btn btn-sm btn-warning students-course" data-id="${course.id}">学生管理</button>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
        
        // 绑定按钮事件
        document.querySelectorAll('.view-course').forEach(btn => {
            btn.addEventListener('click', function() {
                const courseId = this.getAttribute('data-id');
                TeacherUI.viewCourseDetail(courseId);
            });
        });
        
        document.querySelectorAll('.edit-course').forEach(btn => {
            btn.addEventListener('click', function() {
                const courseId = this.getAttribute('data-id');
                TeacherUI.showEditCourseModal(courseId);
            });
        });
        
        document.querySelectorAll('.students-course').forEach(btn => {
            btn.addEventListener('click', function() {
                const courseId = this.getAttribute('data-id');
                TeacherUI.viewCourseStudents(courseId);
            });
        });
    },
    
    /**
     * 更新分页控件
     * @param {number} current 当前页码
     * @param {number} total 总页数
     * @param {Function} callback 页码变化回调函数
     */
    updatePagination: function(current, total, callback) {
        const paginationEl = document.getElementById('pagination');
        if (!paginationEl) return;
        
        // 清空现有内容
        paginationEl.innerHTML = '';
        
        if (total <= 1) return;
        
        // 创建分页结构
        let html = `
            <li class="page-item ${current === 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${current - 1}">上一页</a>
            </li>
        `;
        
        // 计算显示的页码范围
        let startPage = Math.max(1, current - 2);
        const endPage = Math.min(total, startPage + 4);
        
        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }
        
        for (let i = startPage; i <= endPage; i++) {
            html += `
                <li class="page-item ${i === current ? 'active' : ''}">
                    <a class="page-link" href="#" data-page="${i}">${i}</a>
                </li>
            `;
        }
        
        html += `
            <li class="page-item ${current === total ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${current + 1}">下一页</a>
            </li>
        `;
        
        paginationEl.innerHTML = html;
        
        // 绑定页码点击事件
        paginationEl.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                
                const page = parseInt(this.getAttribute('data-page'), 10);
                if (page < 1 || page > total || page === current) return;
                
                if (typeof callback === 'function') {
                    callback(page);
                }
            });
        });
    },
    
    /**
     * 显示创建课程模态框
     */
    showCreateCourseModal: function() {
        // 清空表单
        document.getElementById('courseForm').reset();
        
        // 设置模态框标题
        document.getElementById('courseModalTitle').textContent = '创建新课程';
        
        // 显示模态框
        $('#courseModal').modal('show');
        
        // 绑定保存按钮事件
        document.getElementById('saveCourseBtn').onclick = function() {
            TeacherUI.saveCourse();
        };
    },
    
    /**
     * 显示编辑课程模态框
     * @param {number} courseId 课程ID
     */
    showEditCourseModal: function(courseId) {
        UI.showLoading();
        
        API.teacher.getCourseDetail(courseId)
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    const course = result.data;
                    
                    // 填充表单
                    document.getElementById('courseName').value = course.name || '';
                    document.getElementById('courseCode').value = course.code || '';
                    document.getElementById('courseCredit').value = course.credit || '';
                    document.getElementById('courseCapacity').value = course.capacity || '';
                    document.getElementById('courseDescription').value = course.description || '';
                    
                    // 设置模态框标题
                    document.getElementById('courseModalTitle').textContent = '编辑课程';
                    
                    // 显示模态框
                    $('#courseModal').modal('show');
                    
                    // 绑定保存按钮事件
                    document.getElementById('saveCourseBtn').onclick = function() {
                        TeacherUI.saveCourse(courseId);
                    };
                } else {
                    UI.showMessage(result.message || '获取课程详情失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('获取课程详情失败:', error);
                UI.showMessage('获取课程详情失败: ' + error.message, 'error');
            });
    },
    
    /**
     * 保存课程信息
     * @param {number} courseId 课程ID，如果为空则为创建新课程
     */
    saveCourse: function(courseId) {
        // 验证表单
        const form = document.getElementById('courseForm');
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }
        
        // 获取表单数据
        const courseData = {
            name: document.getElementById('courseName').value,
            code: document.getElementById('courseCode').value,
            credit: parseFloat(document.getElementById('courseCredit').value),
            capacity: parseInt(document.getElementById('courseCapacity').value, 10),
            description: document.getElementById('courseDescription').value
        };
        
        UI.showLoading();
        
        // 创建或更新课程
        const promise = courseId 
            ? API.teacher.updateCourse(courseId, courseData)
            : API.teacher.createCourse(courseData);
        
        promise
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    UI.showMessage(courseId ? '课程更新成功' : '课程创建成功', 'success');
                    $('#courseModal').modal('hide');
                    
                    // 刷新课程列表
                    TeacherUI.initCourseList();
                } else {
                    UI.showMessage(result.message || (courseId ? '更新失败' : '创建失败'), 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error(courseId ? '更新课程失败:' : '创建课程失败:', error);
                UI.showMessage((courseId ? '更新课程失败: ' : '创建课程失败: ') + error.message, 'error');
            });
    },
    
    /**
     * 查看课程详情
     * @param {number} courseId 课程ID
     */
    viewCourseDetail: function(courseId) {
        UI.showLoading();
        
        API.teacher.getCourseDetail(courseId)
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    const course = result.data;
                    
                    // 填充课程详情
                    document.getElementById('courseDetailName').textContent = course.name || '未命名课程';
                    document.getElementById('courseDetailCode').textContent = course.code || '';
                    document.getElementById('courseDetailCredit').textContent = course.credit || 0;
                    document.getElementById('courseDetailCapacity').textContent = 
                        (course.studentCount || 0) + '/' + (course.capacity || 30);
                    document.getElementById('courseDetailDescription').textContent = 
                        course.description || '暂无描述';
                    
                    // 显示模态框
                    $('#courseDetailModal').modal('show');
                    
                    // 绑定查看学生按钮事件
                    document.getElementById('viewStudentsBtn').onclick = function() {
                        $('#courseDetailModal').modal('hide');
                        TeacherUI.viewCourseStudents(courseId);
                    };
                    
                    // 绑定编辑按钮事件
                    document.getElementById('editCourseBtn').onclick = function() {
                        $('#courseDetailModal').modal('hide');
                        TeacherUI.showEditCourseModal(courseId);
                    };
                } else {
                    UI.showMessage(result.message || '获取课程详情失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('获取课程详情失败:', error);
                UI.showMessage('获取课程详情失败: ' + error.message, 'error');
            });
    },
    
    /**
     * 查看课程学生
     * @param {number} courseId 课程ID
     */
    viewCourseStudents: function(courseId) {
        UI.showLoading();
        
        API.teacher.getCourseStudents(courseId)
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    const students = result.data || [];
                    const studentList = document.getElementById('studentList');
                    
                    if (students.length === 0) {
                        studentList.innerHTML = '<tr><td colspan="5" class="text-center">暂无学生</td></tr>';
                    } else {
                        let html = '';
                        
                        students.forEach(student => {
                            html += `
                                <tr>
                                    <td>${student.studentNo || ''}</td>
                                    <td>${student.realName || ''}</td>
                                    <td>${student.department || ''}</td>
                                    <td>${student.score !== undefined ? student.score : '未评分'}</td>
                                    <td>
                                        <button class="btn btn-sm btn-primary enter-score" 
                                            data-id="${student.id}" 
                                            data-name="${student.realName || ''}"
                                            data-student-no="${student.studentNo || ''}">
                                            录入成绩
                                        </button>
                                    </td>
                                </tr>
                            `;
                        });
                        
                        studentList.innerHTML = html;
                        
                        // 绑定录入成绩按钮事件
                        studentList.querySelectorAll('.enter-score').forEach(btn => {
                            btn.addEventListener('click', function() {
                                const studentId = this.getAttribute('data-id');
                                const studentName = this.getAttribute('data-name');
                                const studentNo = this.getAttribute('data-student-no');
                                
                                TeacherUI.showEnterScoreModal(courseId, studentId, studentName, studentNo);
                            });
                        });
                    }
                    
                    // 显示模态框
                    $('#studentListModal').modal('show');
                } else {
                    UI.showMessage(result.message || '获取学生列表失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('获取学生列表失败:', error);
                UI.showMessage('获取学生列表失败: ' + error.message, 'error');
            });
    },
    
    /**
     * 显示录入成绩模态框
     * @param {number} courseId 课程ID
     * @param {number} studentId 学生ID
     * @param {string} studentName 学生姓名
     * @param {string} studentNo 学生学号
     */
    showEnterScoreModal: function(courseId, studentId, studentName, studentNo) {
        // 填充学生信息
        document.getElementById('scoreStudentName').textContent = studentName;
        document.getElementById('scoreStudentId').textContent = studentNo;
        
        // 显示模态框
        $('#enterScoreModal').modal('show');
        
        // 绑定保存按钮事件
        document.getElementById('saveScoreBtn').onclick = function() {
            const score = document.getElementById('scoreInput').value;
            
            if (!score) {
                UI.showMessage('请输入成绩', 'error');
                return;
            }
            
            TeacherUI.saveStudentScore(courseId, studentId, parseFloat(score));
        };
    },
    
    /**
     * 保存学生成绩
     * @param {number} courseId 课程ID
     * @param {number} studentId 学生ID
     * @param {number} score 成绩
     */
    saveStudentScore: function(courseId, studentId, score) {
        UI.showLoading();
        
        API.teacher.enterStudentScore({
            courseId,
            studentId,
            score
        })
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    UI.showMessage('成绩录入成功', 'success');
                    $('#enterScoreModal').modal('hide');
                    
                    // 刷新学生列表
                    TeacherUI.viewCourseStudents(courseId);
                } else {
                    UI.showMessage(result.message || '成绩录入失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('成绩录入失败:', error);
                UI.showMessage('成绩录入失败: ' + error.message, 'error');
            });
    },
    
    /**
     * 管理考勤
     * @param {number} courseId 课程ID
     */
    manageAttendance: function(courseId) {
        UI.showLoading();
        
        // 设置当前日期
        const today = new Date();
        const dateStr = today.toISOString().split('T')[0];
        document.getElementById('attendanceDate').value = dateStr;
        document.getElementById('attendanceCourseId').value = courseId;
        
        API.teacher.getCourseStudents(courseId)
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    const students = result.data || [];
                    const attendanceList = document.getElementById('attendanceList');
                    
                    if (students.length === 0) {
                        attendanceList.innerHTML = '<tr><td colspan="3" class="text-center">暂无学生</td></tr>';
                    } else {
                        let html = '';
                        
                        students.forEach(student => {
                            html += `
                                <tr>
                                    <td>${student.studentNo || ''}</td>
                                    <td>${student.realName || ''}</td>
                                    <td>
                                        <select class="form-control attendance-status" data-id="${student.id}">
                                            <option value="1">出勤</option>
                                            <option value="2">迟到</option>
                                            <option value="3">早退</option>
                                            <option value="4">缺勤</option>
                                            <option value="5">请假</option>
                                        </select>
                                    </td>
                                </tr>
                            `;
                        });
                        
                        attendanceList.innerHTML = html;
                    }
                    
                    // 显示模态框
                    $('#attendanceModal').modal('show');
                } else {
                    UI.showMessage(result.message || '获取学生列表失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('获取学生列表失败:', error);
                UI.showMessage('获取学生列表失败: ' + error.message, 'error');
            });
            
        // 绑定保存按钮事件
        document.getElementById('saveAttendanceBtn').onclick = function() {
            TeacherUI.saveAttendance();
        };
    },
    
    /**
     * 保存考勤记录
     */
    saveAttendance: function() {
        const courseId = document.getElementById('attendanceCourseId').value;
        const date = document.getElementById('attendanceDate').value;
        
        if (!date) {
            UI.showMessage('请选择考勤日期', 'error');
            return;
        }
        
        // 收集考勤数据
        const attendanceRecords = [];
        document.querySelectorAll('.attendance-status').forEach(select => {
            const studentId = select.getAttribute('data-id');
            const status = parseInt(select.value, 10);
            
            attendanceRecords.push({
                studentId,
                status
            });
        });
        
        if (attendanceRecords.length === 0) {
            UI.showMessage('没有学生需要记录考勤', 'error');
            return;
        }
        
        UI.showLoading();
        
        API.teacher.recordAttendance({
            courseId,
            date,
            records: attendanceRecords
        })
            .then(result => {
                UI.hideLoading();
                
                if (result.success) {
                    UI.showMessage('考勤记录保存成功', 'success');
                    $('#attendanceModal').modal('hide');
                } else {
                    UI.showMessage(result.message || '考勤记录保存失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('考勤记录保存失败:', error);
                UI.showMessage('考勤记录保存失败: ' + error.message, 'error');
            });
    },
    
    /**
     * 管理学生成绩
     * @param {number} courseId 课程ID
     */
    manageStudentScores: function(courseId) {
        TeacherUI.viewCourseStudents(courseId);
    }
}; 