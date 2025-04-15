/**
 * 教师模块UI组件库
 * 提供教师模块所需的UI交互组件
 */
var TeacherUI = {
    /**
     * 初始化教师个人资料页面
     */
    initProfile: function() {
        var userInfo = JSON.parse(localStorage.getItem('userInfo')) || {};
        $('#teacherName').text(userInfo.name || '');
        $('#teacherEmail').text(userInfo.email || '');
        $('#teacherDepartment').text(userInfo.department || '');
        $('#teacherPhone').text(userInfo.phone || '');
        
        // 绑定更新资料按钮事件
        $('#updateProfileBtn').on('click', function() {
            TeacherUI.showUpdateProfileModal();
        });
    },
    
    /**
     * 显示更新个人资料模态框
     */
    showUpdateProfileModal: function() {
        var userInfo = JSON.parse(localStorage.getItem('userInfo')) || {};
        
        $('#profileName').val(userInfo.name || '');
        $('#profileEmail').val(userInfo.email || '');
        $('#profilePhone').val(userInfo.phone || '');
        
        $('#updateProfileModal').modal('show');
        
        // 绑定保存按钮事件
        $('#saveProfileBtn').off('click').on('click', function() {
            var profileData = {
                name: $('#profileName').val(),
                email: $('#profileEmail').val(),
                phone: $('#profilePhone').val()
            };
            
            API.teacher.updateProfile(profileData, function(success, data) {
                if (success) {
                    // 更新本地存储的用户信息
                    var userInfo = JSON.parse(localStorage.getItem('userInfo')) || {};
                    userInfo.name = profileData.name;
                    userInfo.email = profileData.email;
                    userInfo.phone = profileData.phone;
                    localStorage.setItem('userInfo', JSON.stringify(userInfo));
                    
                    // 更新界面
                    TeacherUI.initProfile();
                    $('#updateProfileModal').modal('hide');
                    UI.showMessage('个人资料更新成功');
                } else {
                    UI.showMessage(data, 'error');
                }
            });
        });
    },
    
    /**
     * 初始化课程列表页面
     * @param {number} page 页码
     * @param {number} pageSize 每页条数
     */
    initCourseList: function(page, pageSize) {
        page = page || 1;
        pageSize = pageSize || 10;
        
        UI.showLoading();
        
        API.teacher.getCourses(page, pageSize, function(success, data) {
            UI.hideLoading();
            
            if (success) {
                var courses = data.list || [];
                var total = data.total || 0;
                
                // 清空课程列表
                $('#courseList').empty();
                
                if (courses.length === 0) {
                    $('#courseList').html('<tr><td colspan="5" class="text-center">暂无课程数据</td></tr>');
                    return;
                }
                
                // 渲染课程列表
                courses.forEach(function(course) {
                    var courseRow = $('<tr></tr>');
                    courseRow.append('<td>' + course.code + '</td>');
                    courseRow.append('<td>' + course.name + '</td>');
                    courseRow.append('<td>' + course.credit + '</td>');
                    courseRow.append('<td>' + course.studentCount + '/' + course.capacity + '</td>');
                    
                    var actionCell = $('<td></td>');
                    var viewBtn = $('<button class="btn btn-sm btn-info mr-2">查看</button>');
                    var scoreBtn = $('<button class="btn btn-sm btn-primary mr-2">成绩</button>');
                    var attendanceBtn = $('<button class="btn btn-sm btn-warning">考勤</button>');
                    
                    // 绑定查看按钮事件
                    viewBtn.on('click', function() {
                        TeacherUI.viewCourseDetail(course.id);
                    });
                    
                    // 绑定成绩按钮事件
                    scoreBtn.on('click', function() {
                        TeacherUI.manageStudentScores(course.id);
                    });
                    
                    // 绑定考勤按钮事件
                    attendanceBtn.on('click', function() {
                        TeacherUI.manageAttendance(course.id);
                    });
                    
                    actionCell.append(viewBtn).append(scoreBtn).append(attendanceBtn);
                    courseRow.append(actionCell);
                    
                    $('#courseList').append(courseRow);
                });
                
                // 更新分页
                TeacherUI.updatePagination(page, Math.ceil(total / pageSize), function(newPage) {
                    TeacherUI.initCourseList(newPage, pageSize);
                });
            } else {
                UI.showMessage(data, 'error');
            }
        });
        
        // 绑定创建课程按钮事件
        $('#createCourseBtn').off('click').on('click', function() {
            TeacherUI.showCreateCourseModal();
        });
    },
    
    /**
     * 更新分页控件
     * @param {number} currentPage 当前页码
     * @param {number} totalPages 总页数
     * @param {function} callback 页码变化回调
     */
    updatePagination: function(currentPage, totalPages, callback) {
        var pagination = $('#pagination');
        pagination.empty();
        
        if (totalPages <= 1) {
            return;
        }
        
        // 上一页按钮
        var prevBtn = $('<li class="page-item"><a class="page-link" href="javascript:void(0)">上一页</a></li>');
        if (currentPage === 1) {
            prevBtn.addClass('disabled');
        } else {
            prevBtn.on('click', function() {
                callback(currentPage - 1);
            });
        }
        pagination.append(prevBtn);
        
        // 页码按钮
        var startPage = Math.max(1, currentPage - 2);
        var endPage = Math.min(totalPages, startPage + 4);
        
        for (var i = startPage; i <= endPage; i++) {
            var pageBtn = $('<li class="page-item"><a class="page-link" href="javascript:void(0)">' + i + '</a></li>');
            if (i === currentPage) {
                pageBtn.addClass('active');
            } else {
                pageBtn.on('click', function() {
                    callback(parseInt($(this).text()));
                });
            }
            pagination.append(pageBtn);
        }
        
        // 下一页按钮
        var nextBtn = $('<li class="page-item"><a class="page-link" href="javascript:void(0)">下一页</a></li>');
        if (currentPage === totalPages) {
            nextBtn.addClass('disabled');
        } else {
            nextBtn.on('click', function() {
                callback(currentPage + 1);
            });
        }
        pagination.append(nextBtn);
    },
    
    /**
     * 显示创建课程模态框
     */
    showCreateCourseModal: function() {
        // 重置表单
        $('#courseForm')[0].reset();
        
        // 显示模态框
        $('#courseModal').modal('show');
        $('#courseModalTitle').text('创建新课程');
        
        // 绑定保存按钮事件
        $('#saveCourseBtn').off('click').on('click', function() {
            var courseData = {
                name: $('#courseName').val(),
                code: $('#courseCode').val(),
                credit: $('#courseCredit').val(),
                capacity: $('#courseCapacity').val(),
                description: $('#courseDescription').val()
            };
            
            API.teacher.createCourse(courseData, function(success, data) {
                if (success) {
                    $('#courseModal').modal('hide');
                    UI.showMessage('课程创建成功');
                    TeacherUI.initCourseList();
                } else {
                    UI.showMessage(data, 'error');
                }
            });
        });
    },
    
    /**
     * 查看课程详情
     * @param {number} courseId 课程ID
     */
    viewCourseDetail: function(courseId) {
        UI.showLoading();
        
        API.teacher.getCourseDetail(courseId, function(success, data) {
            UI.hideLoading();
            
            if (success) {
                // 填充课程详情
                $('#courseDetailName').text(data.name);
                $('#courseDetailCode').text(data.code);
                $('#courseDetailCredit').text(data.credit);
                $('#courseDetailCapacity').text(data.studentCount + '/' + data.capacity);
                $('#courseDetailDescription').text(data.description || '暂无描述');
                
                // 显示详情模态框
                $('#courseDetailModal').modal('show');
                
                // 绑定学生列表按钮事件
                $('#viewStudentsBtn').off('click').on('click', function() {
                    $('#courseDetailModal').modal('hide');
                    TeacherUI.viewCourseStudents(courseId);
                });
                
                // 绑定编辑按钮事件
                $('#editCourseBtn').off('click').on('click', function() {
                    $('#courseDetailModal').modal('hide');
                    TeacherUI.showEditCourseModal(courseId, data);
                });
            } else {
                UI.showMessage(data, 'error');
            }
        });
    },
    
    /**
     * 显示编辑课程模态框
     * @param {number} courseId 课程ID
     * @param {object} courseData 课程数据
     */
    showEditCourseModal: function(courseId, courseData) {
        // 填充表单
        $('#courseName').val(courseData.name);
        $('#courseCode').val(courseData.code);
        $('#courseCredit').val(courseData.credit);
        $('#courseCapacity').val(courseData.capacity);
        $('#courseDescription').val(courseData.description || '');
        
        // 显示模态框
        $('#courseModal').modal('show');
        $('#courseModalTitle').text('编辑课程');
        
        // 绑定保存按钮事件
        $('#saveCourseBtn').off('click').on('click', function() {
            var updatedData = {
                name: $('#courseName').val(),
                code: $('#courseCode').val(),
                credit: $('#courseCredit').val(),
                capacity: $('#courseCapacity').val(),
                description: $('#courseDescription').val()
            };
            
            API.teacher.updateCourse(courseId, updatedData, function(success, data) {
                if (success) {
                    $('#courseModal').modal('hide');
                    UI.showMessage('课程更新成功');
                    TeacherUI.initCourseList();
                } else {
                    UI.showMessage(data, 'error');
                }
            });
        });
    },
    
    /**
     * 查看课程学生列表
     * @param {number} courseId 课程ID
     */
    viewCourseStudents: function(courseId) {
        UI.showLoading();
        
        API.teacher.getCourseStudents(courseId, function(success, data) {
            UI.hideLoading();
            
            if (success) {
                var students = data || [];
                
                // 清空学生列表
                $('#studentList').empty();
                
                if (students.length === 0) {
                    $('#studentList').html('<tr><td colspan="5" class="text-center">暂无学生数据</td></tr>');
                    return;
                }
                
                // 渲染学生列表
                students.forEach(function(student) {
                    var row = $('<tr></tr>');
                    row.append('<td>' + student.id + '</td>');
                    row.append('<td>' + student.name + '</td>');
                    row.append('<td>' + student.department + '</td>');
                    row.append('<td>' + (student.score !== undefined ? student.score : '未评分') + '</td>');
                    
                    var actionCell = $('<td></td>');
                    var scoreBtn = $('<button class="btn btn-sm btn-primary">录入成绩</button>');
                    
                    scoreBtn.on('click', function() {
                        TeacherUI.showEnterScoreModal(courseId, student);
                    });
                    
                    actionCell.append(scoreBtn);
                    row.append(actionCell);
                    
                    $('#studentList').append(row);
                });
                
                // 显示学生列表模态框
                $('#studentListModal').modal('show');
                $('#studentListTitle').text('课程学生列表');
            } else {
                UI.showMessage(data, 'error');
            }
        });
    },
    
    /**
     * 显示录入成绩模态框
     * @param {number} courseId 课程ID
     * @param {object} student 学生数据
     */
    showEnterScoreModal: function(courseId, student) {
        $('#scoreStudentName').text(student.name);
        $('#scoreStudentId').text(student.id);
        $('#scoreInput').val(student.score || '');
        
        $('#enterScoreModal').modal('show');
        
        $('#saveScoreBtn').off('click').on('click', function() {
            var score = $('#scoreInput').val();
            
            if (!score || isNaN(score) || score < 0 || score > 100) {
                UI.showMessage('请输入0-100之间的有效成绩', 'error');
                return;
            }
            
            var scoreData = {
                courseId: courseId,
                studentId: student.id,
                score: parseFloat(score)
            };
            
            API.teacher.enterStudentScore(scoreData, function(success, data) {
                if (success) {
                    $('#enterScoreModal').modal('hide');
                    UI.showMessage('成绩录入成功');
                    // 刷新学生列表
                    TeacherUI.viewCourseStudents(courseId);
                } else {
                    UI.showMessage(data, 'error');
                }
            });
        });
    },
    
    /**
     * 管理学生成绩
     * @param {number} courseId 课程ID
     */
    manageStudentScores: function(courseId) {
        TeacherUI.viewCourseStudents(courseId);
    },
    
    /**
     * 管理考勤
     * @param {number} courseId 课程ID
     */
    manageAttendance: function(courseId) {
        UI.showLoading();
        
        API.teacher.getAttendanceStats(courseId, function(success, data) {
            UI.hideLoading();
            
            if (success) {
                // 填充考勤统计数据
                $('#attendanceCourseId').val(courseId);
                $('#attendanceDate').val(new Date().toISOString().split('T')[0]);
                
                // 清空考勤列表
                $('#attendanceList').empty();
                
                var students = data.students || [];
                
                if (students.length === 0) {
                    $('#attendanceList').html('<tr><td colspan="4" class="text-center">暂无学生数据</td></tr>');
                } else {
                    // 渲染学生考勤列表
                    students.forEach(function(student) {
                        var row = $('<tr></tr>');
                        row.append('<td>' + student.id + '</td>');
                        row.append('<td>' + student.name + '</td>');
                        
                        var statusCell = $('<td></td>');
                        var select = $('<select class="form-control attendance-status"></select>');
                        select.append('<option value="present">出席</option>');
                        select.append('<option value="late">迟到</option>');
                        select.append('<option value="absent">缺席</option>');
                        select.append('<option value="leave">请假</option>');
                        select.data('studentId', student.id);
                        
                        statusCell.append(select);
                        row.append(statusCell);
                        
                        $('#attendanceList').append(row);
                    });
                }
                
                // 显示考勤模态框
                $('#attendanceModal').modal('show');
                
                // 绑定保存考勤按钮事件
                $('#saveAttendanceBtn').off('click').on('click', function() {
                    var attendanceData = {
                        courseId: courseId,
                        date: $('#attendanceDate').val(),
                        students: []
                    };
                    
                    $('.attendance-status').each(function() {
                        attendanceData.students.push({
                            studentId: $(this).data('studentId'),
                            status: $(this).val()
                        });
                    });
                    
                    API.teacher.recordAttendance(attendanceData, function(success, data) {
                        if (success) {
                            $('#attendanceModal').modal('hide');
                            UI.showMessage('考勤记录成功');
                        } else {
                            UI.showMessage(data, 'error');
                        }
                    });
                });
            } else {
                UI.showMessage(data, 'error');
            }
        });
    },
    
    /**
     * 初始化教师课表页面
     */
    initSchedule: function() {
        UI.showLoading();
        
        API.teacher.getSchedule(function(success, data) {
            UI.hideLoading();
            
            if (success) {
                TeacherUI.renderSchedule(data);
            } else {
                UI.showMessage(data, 'error');
            }
        });
    },
    
    /**
     * 渲染课表
     * @param {object} scheduleData 课表数据
     */
    renderSchedule: function(scheduleData) {
        var schedule = scheduleData.schedule || [];
        var weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
        var timeSlots = [
            '第1节 (8:00-8:45)', 
            '第2节 (8:55-9:40)', 
            '第3节 (10:00-10:45)', 
            '第4节 (10:55-11:40)',
            '第5节 (14:00-14:45)', 
            '第6节 (14:55-15:40)', 
            '第7节 (16:00-16:45)', 
            '第8节 (16:55-17:40)',
            '第9节 (19:00-19:45)', 
            '第10节 (19:55-20:40)', 
            '第11节 (20:50-21:35)'
        ];
        
        // 创建表头
        var tableHead = $('<thead></thead>');
        var headerRow = $('<tr></tr>');
        headerRow.append('<th>时间</th>');
        
        weekdays.forEach(function(day) {
            headerRow.append('<th>' + day + '</th>');
        });
        
        tableHead.append(headerRow);
        $('#scheduleTable').empty().append(tableHead);
        
        // 创建表体
        var tableBody = $('<tbody></tbody>');
        
        timeSlots.forEach(function(slot, slotIndex) {
            var row = $('<tr></tr>');
            row.append('<td>' + slot + '</td>');
            
            weekdays.forEach(function(day, dayIndex) {
                var cell = $('<td></td>');
                var course = TeacherUI.findCourse(schedule, dayIndex + 1, slotIndex + 1);
                
                if (course) {
                    var courseDiv = $('<div class="schedule-course"></div>');
                    courseDiv.append('<div class="course-name">' + course.name + '</div>');
                    courseDiv.append('<div class="course-room">' + course.classroom + '</div>');
                    cell.append(courseDiv);
                    
                    // 添加悬停信息
                    courseDiv.attr('title', course.name + '\n' + 
                                         '地点: ' + course.classroom + '\n' + 
                                         '班级: ' + course.className);
                }
                
                row.append(cell);
            });
            
            tableBody.append(row);
        });
        
        $('#scheduleTable').append(tableBody);
    },
    
    /**
     * 查找指定时间段的课程
     * @param {array} schedule 课表数据
     * @param {number} day 星期几(1-7)
     * @param {number} slot 第几节课(1-11)
     * @returns {object|null} 课程信息
     */
    findCourse: function(schedule, day, slot) {
        for (var i = 0; i < schedule.length; i++) {
            var course = schedule[i];
            if (course.day === day && course.startSlot <= slot && slot <= course.endSlot) {
                return course;
            }
        }
        return null;
    }
}; 