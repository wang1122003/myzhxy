/**
 * 课程模块UI组件
 * 提供课程相关的UI功能
 */
const UICourse = {
    /**
     * 初始化课程列表
     * @param {string} containerId 容器ID
     * @param {boolean} showActions 是否显示操作按钮
     * @param {Function} callback 回调函数
     */
    initCourseList: function(containerId, showActions = true, callback = null) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.course.getCourses()
            .then(response => {
                if (response.code === 200) {
                    const courses = response.data;
                    
                    if (courses.length === 0) {
                        container.innerHTML = '<div class="empty-state">暂无课程</div>';
                        return;
                    }
                    
                    // 创建课程列表
                    let html = '<div class="course-grid">';
                    
                    courses.forEach(course => {
                        // 根据课程状态设置样式
                        let statusClass = '';
                        let statusText = '';
                        
                        switch (course.status) {
                            case 1:
                                statusClass = 'ongoing';
                                statusText = '进行中';
                                break;
                            case 2:
                                statusClass = 'ended';
                                statusText = '已结束';
                                break;
                            default:
                                statusClass = 'upcoming';
                                statusText = '未开始';
                        }
                        
                        html += `
                            <div class="course-card" data-id="${course.id}">
                                <div class="course-status ${statusClass}">${statusText}</div>
                                <div class="course-header">
                                    <h3 class="course-name">${course.name}</h3>
                                </div>
                                <div class="course-body">
                                    <div class="course-info">
                                        <div class="info-item">
                                            <i class="bi bi-person"></i>
                                            <span>${course.teacherName || '未知教师'}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-calendar-event"></i>
                                            <span>${course.semester || '未知学期'}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-people"></i>
                                            <span>${course.studentCount || 0}人</span>
                                        </div>
                                    </div>
                                    <p class="course-description">${course.description || '暂无描述'}</p>
                                </div>
                                <div class="course-footer">
                                    <button class="btn btn-primary view-course" data-id="${course.id}">查看详情</button>
                                    ${showActions ? `
                                        <button class="btn btn-outline-primary edit-course" data-id="${course.id}">编辑</button>
                                        <button class="btn btn-outline-danger delete-course" data-id="${course.id}">删除</button>
                                    ` : ''}
                                </div>
                            </div>
                        `;
                    });
                    
                    html += '</div>';
                    container.innerHTML = html;
                    
                    // 绑定事件
                    this.bindCourseEvents(container, showActions);
                    
                    // 执行回调
                    if (callback && typeof callback === 'function') {
                        callback(courses);
                    }
                } else {
                    container.innerHTML = `<div class="error-state">加载失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('获取课程列表失败:', error);
                container.innerHTML = '<div class="error-state">加载失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 绑定课程操作事件
     * @param {HTMLElement} container 容器元素
     * @param {boolean} showActions 是否显示操作按钮
     */
    bindCourseEvents: function(container, showActions) {
        // 查看课程详情
        const viewButtons = container.querySelectorAll('.view-course');
        viewButtons.forEach(button => {
            button.addEventListener('click', () => {
                const courseId = button.getAttribute('data-id');
                this.viewCourseDetail(courseId);
            });
        });
        
        // 如果显示操作按钮，绑定编辑和删除事件
        if (showActions) {
            // 编辑课程
            const editButtons = container.querySelectorAll('.edit-course');
            editButtons.forEach(button => {
                button.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const courseId = button.getAttribute('data-id');
                    
                    UI.showLoading();
                    API.course.getCourseById(courseId)
                        .then(response => {
                            if (response.code === 200) {
                                this.editCourse(response.data);
                            } else {
                                UI.showMessage(response.message || '获取课程信息失败', 'error');
                            }
                        })
                        .catch(error => {
                            console.error('获取课程信息失败:', error);
                            UI.showMessage('获取课程信息失败', 'error');
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                });
            });
            
            // 删除课程
            const deleteButtons = container.querySelectorAll('.delete-course');
            deleteButtons.forEach(button => {
                button.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const courseId = button.getAttribute('data-id');
                    this.deleteCourse(courseId);
                });
            });
        }
    },
    
    /**
     * 查看课程详情
     * @param {string} courseId 课程ID
     */
    viewCourseDetail: function(courseId) {
        UI.showLoading();
        
        API.course.getCourseById(courseId)
            .then(response => {
                if (response.code === 200) {
                    const course = response.data;
                    
                    // 创建对话框内容
                    let content = `
                        <div class="course-detail">
                            <div class="detail-header">
                                <h2 class="course-name">${course.name}</h2>
                                <div class="course-meta">
                                    <span class="course-teacher">
                                        <i class="bi bi-person"></i> ${course.teacherName || '未知教师'}
                                    </span>
                                    <span class="course-semester">
                                        <i class="bi bi-calendar"></i> ${course.semester || '未知学期'}
                                    </span>
                                </div>
                            </div>
                            
                            <div class="detail-section">
                                <h4>课程信息</h4>
                                <dl class="detail-list">
                                    <dt>课程编号</dt>
                                    <dd>${course.code || '-'}</dd>
                                    
                                    <dt>学分</dt>
                                    <dd>${course.credits || '-'}</dd>
                                    
                                    <dt>课程类型</dt>
                                    <dd>${course.type || '-'}</dd>
                                    
                                    <dt>授课方式</dt>
                                    <dd>${course.teachingMode || '-'}</dd>
                                    
                                    <dt>开始时间</dt>
                                    <dd>${course.startTime ? new Date(course.startTime).toLocaleDateString() : '-'}</dd>
                                    
                                    <dt>结束时间</dt>
                                    <dd>${course.endTime ? new Date(course.endTime).toLocaleDateString() : '-'}</dd>
                                    
                                    <dt>选课人数</dt>
                                    <dd>${course.studentCount || 0}人</dd>
                                </dl>
                            </div>
                            
                            <div class="detail-section">
                                <h4>课程描述</h4>
                                <div class="course-description">
                                    ${course.description || '暂无描述'}
                                </div>
                            </div>
                            
                            <div class="detail-actions">
                                <button id="manageCourseBtn" class="btn btn-primary">管理课程</button>
                            </div>
                        </div>
                    `;
                    
                    // 创建对话框
                    const dialog = UI.createDialog('课程详情', content, '800px');
                    
                    // 添加管理课程按钮事件
                    const manageCourseBtn = dialog.body.querySelector('#manageCourseBtn');
                    if (manageCourseBtn) {
                        manageCourseBtn.addEventListener('click', () => {
                            dialog.close();
                            this.manageCourse(courseId);
                        });
                    }
                } else {
                    UI.showMessage(response.message || '获取课程详情失败', 'error');
                }
            })
            .catch(error => {
                console.error('获取课程详情失败:', error);
                UI.showMessage('获取课程详情失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * The rest of the implementation for manageCourse, editCourse, deleteCourse, etc.
     */
    
    /**
     * 编辑课程
     * @param {Object} course 课程对象
     */
    editCourse: function(course) {
        const isAdd = !course || !course.id;
        const title = isAdd ? '添加课程' : '编辑课程';
        
        // 创建表单字段
        const fields = [
            { name: 'name', label: '课程名称', type: 'text', required: true },
            { name: 'code', label: '课程编号', type: 'text' },
            { name: 'credits', label: '学分', type: 'number', min: 0, step: 0.5 },
            { name: 'type', label: '课程类型', type: 'select', options: ['必修课', '选修课', '通识课'] },
            { name: 'teachingMode', label: '授课方式', type: 'select', options: ['线下课堂', '线上直播', '线上录播', '混合式'] },
            { name: 'semester', label: '学期', type: 'select' },
            { name: 'startTime', label: '开始时间', type: 'date' },
            { name: 'endTime', label: '结束时间', type: 'date' },
            { name: 'description', label: '课程描述', type: 'textarea', rows: 4 }
        ];
        
        // 创建表单
        const form = UI.createForm(fields, course || {}, (formData) => {
            UI.showLoading();
            
            const apiMethod = isAdd ? API.course.addCourse : API.course.updateCourse;
            
            // 如果是更新，添加ID
            if (!isAdd) {
                formData.id = course.id;
            }
            
            apiMethod(formData)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage(isAdd ? '课程添加成功' : '课程更新成功', 'success');
                        dialog.close();
                        
                        // 刷新课程列表
                        const courseListContainer = document.getElementById('courseList');
                        if (courseListContainer) {
                            this.initCourseList('courseList', true);
                        }
                    } else {
                        UI.showMessage(response.message || (isAdd ? '课程添加失败' : '课程更新失败'), 'error');
                    }
                })
                .catch(error => {
                    console.error(isAdd ? '添加课程失败:' : '更新课程失败:', error);
                    UI.showMessage(isAdd ? '添加课程失败' : '更新课程失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        });
        
        // 创建对话框
        const dialog = UI.createDialog(title, form, '600px');
        
        // 获取学期列表
        API.common.getSemesters()
            .then(response => {
                if (response.code === 200) {
                    const semesters = response.data;
                    const semesterSelect = document.querySelector('select[name="semester"]');
                    
                    if (semesterSelect && semesters && semesters.length > 0) {
                        // 清空原有选项
                        semesterSelect.innerHTML = '';
                        
                        // 添加默认选项
                        const defaultOption = document.createElement('option');
                        defaultOption.value = '';
                        defaultOption.textContent = '请选择学期';
                        semesterSelect.appendChild(defaultOption);
                        
                        // 添加学期选项
                        semesters.forEach(semester => {
                            const option = document.createElement('option');
                            option.value = semester.name;
                            option.textContent = semester.name;
                            
                            // 如果是编辑模式且匹配当前课程的学期，选中该选项
                            if (!isAdd && course.semester === semester.name) {
                                option.selected = true;
                            }
                            
                            semesterSelect.appendChild(option);
                        });
                    }
                }
            })
            .catch(error => {
                console.error('获取学期列表失败:', error);
            });
    },
    
    /**
     * 删除课程
     * @param {string} courseId 课程ID
     */
    deleteCourse: function(courseId) {
        if (confirm('确定要删除这个课程吗？删除后无法恢复。')) {
            UI.showLoading();
            
            API.course.deleteCourse(courseId)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('课程删除成功', 'success');
                        
                        // 移除课程卡片
                        const courseCard = document.querySelector(`.course-card[data-id="${courseId}"]`);
                        if (courseCard) {
                            courseCard.remove();
                        }
                        
                        // 检查是否还有课程
                        const courseCards = document.querySelectorAll('.course-card');
                        if (courseCards.length === 0) {
                            const courseListContainer = document.getElementById('courseList');
                            if (courseListContainer) {
                                courseListContainer.innerHTML = '<div class="empty-state">暂无课程</div>';
                            }
                        }
                    } else {
                        UI.showMessage(response.message || '课程删除失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('删除课程失败:', error);
                    UI.showMessage('删除课程失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    },
    
    /**
     * 管理课程（跳转到课程管理页面）
     * @param {string} courseId 课程ID
     */
    manageCourse: function(courseId) {
        // 这里可以跳转到课程管理页面，或者打开一个更复杂的模态框
        window.location.href = `course-management.html?id=${courseId}`;
    },
    
    /**
     * 初始化选课列表
     * @param {string} containerId 容器ID
     */
    initCourseSelection: function(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.student.getAvailableCourses()
            .then(response => {
                if (response.code === 200) {
                    const courses = response.data;
                    
                    if (courses.length === 0) {
                        container.innerHTML = '<div class="empty-state">当前没有可选课程</div>';
                        return;
                    }
                    
                    // 创建课程列表
                    let html = '<div class="course-grid">';
                    
                    courses.forEach(course => {
                        // 根据课程状态设置样式
                        let statusBadge = '';
                        if (course.selected) {
                            statusBadge = '<span class="badge badge-success">已选</span>';
                        } else if (course.full) {
                            statusBadge = '<span class="badge badge-danger">已满</span>';
                        } else {
                            statusBadge = '<span class="badge badge-primary">可选</span>';
                        }
                        
                        html += `
                            <div class="course-card" data-id="${course.id}">
                                <div class="course-header">
                                    <h3 class="course-name">${course.name} ${statusBadge}</h3>
                                </div>
                                <div class="course-body">
                                    <div class="course-info">
                                        <div class="info-item">
                                            <i class="bi bi-person"></i>
                                            <span>${course.teacherName || '未知教师'}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-calendar-event"></i>
                                            <span>${course.semester || '未知学期'}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-people"></i>
                                            <span>${course.studentCount || 0}/${course.capacity || '不限'}</span>
                                        </div>
                                    </div>
                                    <p class="course-description">${course.description || '暂无描述'}</p>
                                </div>
                                <div class="course-footer">
                                    <button class="btn btn-primary view-course" data-id="${course.id}">查看详情</button>
                                    ${!course.selected && !course.full ? 
                                        `<button class="btn btn-success select-course" data-id="${course.id}">选择课程</button>` :
                                        course.selected ? 
                                        `<button class="btn btn-danger unselect-course" data-id="${course.id}">退选课程</button>` : 
                                        `<button class="btn btn-secondary" disabled>已满</button>`
                                    }
                                </div>
                            </div>
                        `;
                    });
                    
                    html += '</div>';
                    container.innerHTML = html;
                    
                    // 绑定事件
                    this.bindCourseSelectionEvents(container);
                } else {
                    container.innerHTML = `<div class="error-state">加载失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('获取选课列表失败:', error);
                container.innerHTML = '<div class="error-state">加载失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 绑定选课事件
     * @param {HTMLElement} container 容器元素
     */
    bindCourseSelectionEvents: function(container) {
        // 查看课程详情
        const viewButtons = container.querySelectorAll('.view-course');
        viewButtons.forEach(button => {
            button.addEventListener('click', () => {
                const courseId = button.getAttribute('data-id');
                this.viewCourseDetail(courseId);
            });
        });
        
        // 选择课程
        const selectButtons = container.querySelectorAll('.select-course');
        selectButtons.forEach(button => {
            button.addEventListener('click', () => {
                const courseId = button.getAttribute('data-id');
                this.selectCourse(courseId);
            });
        });
        
        // 退选课程
        const unselectButtons = container.querySelectorAll('.unselect-course');
        unselectButtons.forEach(button => {
            button.addEventListener('click', () => {
                const courseId = button.getAttribute('data-id');
                this.unselectCourse(courseId);
            });
        });
    },
    
    /**
     * Implementations for selectCourse and unselectCourse
     */
   
    /**
     * 选择课程
     * @param {string} courseId 课程ID
     */
    selectCourse: function(courseId) {
        UI.showLoading();
        
        API.student.selectCourse(courseId)
            .then(response => {
                if (response.code === 200) {
                    UI.showMessage('选课成功', 'success');
                    
                    // 更新UI
                    const courseCard = document.querySelector(`.course-card[data-id="${courseId}"]`);
                    if (courseCard) {
                        // 更新状态标签
                        const courseHeader = courseCard.querySelector('.course-header');
                        if (courseHeader) {
                            const courseName = courseHeader.querySelector('.course-name').textContent.split(' ')[0];
                            courseHeader.querySelector('.course-name').innerHTML = `${courseName} <span class="badge badge-success">已选</span>`;
                        }
                        
                        // 更新按钮
                        const courseFooter = courseCard.querySelector('.course-footer');
                        if (courseFooter) {
                            const selectButton = courseFooter.querySelector('.select-course');
                            if (selectButton) {
                                selectButton.className = 'btn btn-danger unselect-course';
                                selectButton.textContent = '退选课程';
                                
                                // 重新绑定事件
                                selectButton.removeEventListener('click', this.selectCourse);
                                selectButton.addEventListener('click', () => {
                                    this.unselectCourse(courseId);
                                });
                            }
                        }
                    }
                } else {
                    UI.showMessage(response.message || '选课失败', 'error');
                }
            })
            .catch(error => {
                console.error('选课失败:', error);
                UI.showMessage('选课失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 退选课程
     * @param {string} courseId 课程ID
     */
    unselectCourse: function(courseId) {
        if (confirm('确定要退选这门课程吗？')) {
            UI.showLoading();
            
            API.student.unselectCourse(courseId)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('退选成功', 'success');
                        
                        // 更新UI
                        const courseCard = document.querySelector(`.course-card[data-id="${courseId}"]`);
                        if (courseCard) {
                            // 更新状态标签
                            const courseHeader = courseCard.querySelector('.course-header');
                            if (courseHeader) {
                                const courseName = courseHeader.querySelector('.course-name').textContent.split(' ')[0];
                                courseHeader.querySelector('.course-name').innerHTML = `${courseName} <span class="badge badge-primary">可选</span>`;
                            }
                            
                            // 更新按钮
                            const courseFooter = courseCard.querySelector('.course-footer');
                            if (courseFooter) {
                                const unselectButton = courseFooter.querySelector('.unselect-course');
                                if (unselectButton) {
                                    unselectButton.className = 'btn btn-success select-course';
                                    unselectButton.textContent = '选择课程';
                                    
                                    // 重新绑定事件
                                    unselectButton.removeEventListener('click', this.unselectCourse);
                                    unselectButton.addEventListener('click', () => {
                                        this.selectCourse(courseId);
                                    });
                                }
                            }
                        }
                    } else {
                        UI.showMessage(response.message || '退选失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('退选课程失败:', error);
                    UI.showMessage('退选失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    }
};

// 将UICourse添加到全局UI对象中
if (typeof UI !== 'undefined') {
    UI.course = UICourse;
} 