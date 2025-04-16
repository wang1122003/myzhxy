/**
 * 活动模块UI组件
 * 提供活动相关的UI功能
 */
const UIActivity = {
    /**
     * 初始化活动列表
     * @param {string} containerId 容器ID
     * @param {boolean} showActions 是否显示操作按钮
     * @param {Function} callback 回调函数
     */
    initActivityList: function(containerId, showActions = true, callback = null) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.activity.getActivities()
            .then(response => {
                if (response.code === 200) {
                    const activities = response.data;
                    
                    if (activities.length === 0) {
                        container.innerHTML = '<div class="empty-state">暂无活动</div>';
                        return;
                    }
                    
                    // 创建活动列表
                    let html = '<div class="activity-grid">';
                    
                    activities.forEach(activity => {
                        // 获取活动状态
                        const now = new Date().getTime();
                        const startTime = new Date(activity.startTime).getTime();
                        const endTime = new Date(activity.endTime).getTime();
                        
                        let statusClass = '';
                        let statusText = '';
                        
                        if (now < startTime) {
                            statusClass = 'upcoming';
                            statusText = '即将开始';
                        } else if (now >= startTime && now <= endTime) {
                            statusClass = 'ongoing';
                            statusText = '进行中';
                        } else {
                            statusClass = 'ended';
                            statusText = '已结束';
                        }
                        
                        // 格式化时间
                        const startDate = UI.formatDateTime(activity.startTime, 'datetime');
                        const endDate = UI.formatDateTime(activity.endTime, 'datetime');
                        
                        html += `
                            <div class="activity-card" data-id="${activity.id}">
                                <div class="activity-status ${statusClass}">${statusText}</div>
                                ${activity.posterUrl ? 
                                    `<div class="activity-poster">
                                        <img src="${activity.posterUrl}" alt="${activity.title}">
                                    </div>` : ''
                                }
                                <div class="activity-header">
                                    <h3 class="activity-title">${activity.title}</h3>
                                </div>
                                <div class="activity-body">
                                    <p class="activity-description">${activity.description ? (activity.description.length > 100 ? activity.description.substring(0, 100) + '...' : activity.description) : '暂无描述'}</p>
                                    <div class="activity-info">
                                        <div class="info-item">
                                            <i class="bi bi-geo-alt"></i>
                                            <span>${activity.location || '暂无地点'}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-calendar-event"></i>
                                            <span>${startDate} - ${endDate}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-person"></i>
                                            <span>${activity.organizer || '暂无组织者'}</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="activity-footer">
                                    <button class="btn btn-primary view-activity" data-id="${activity.id}">查看详情</button>
                                    ${showActions ? `
                                        <button class="btn btn-outline-primary edit-activity" data-id="${activity.id}">编辑</button>
                                        <button class="btn btn-outline-danger delete-activity" data-id="${activity.id}">删除</button>
                                    ` : 
                                    now <= endTime ? `
                                        <button class="btn btn-success join-activity" data-id="${activity.id}">参加活动</button>
                                    ` : ''}
                                </div>
                            </div>
                        `;
                    });
                    
                    html += '</div>';
                    container.innerHTML = html;
                    
                    // 绑定事件
                    this.bindActivityEvents(container, showActions);
                    
                    // 执行回调
                    if (callback && typeof callback === 'function') {
                        callback(activities);
                    }
                } else {
                    container.innerHTML = `<div class="error-state">加载失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('获取活动列表失败:', error);
                container.innerHTML = '<div class="error-state">加载失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 绑定活动操作事件
     * @param {HTMLElement} container 容器元素
     * @param {boolean} showActions 是否显示操作按钮
     */
    bindActivityEvents: function(container, showActions) {
        // 查看活动详情
        const viewButtons = container.querySelectorAll('.view-activity');
        viewButtons.forEach(button => {
            button.addEventListener('click', () => {
                const activityId = button.getAttribute('data-id');
                this.viewActivityDetail(activityId);
            });
        });
        
        // 如果显示操作按钮，绑定编辑和删除事件
        if (showActions) {
            // 编辑活动
            const editButtons = container.querySelectorAll('.edit-activity');
            editButtons.forEach(button => {
                button.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const activityId = button.getAttribute('data-id');
                    
                    UI.showLoading();
                    API.activity.getActivityById(activityId)
                        .then(response => {
                            if (response.code === 200) {
                                this.editActivity(response.data);
                            } else {
                                UI.showMessage(response.message || '获取活动信息失败', 'error');
                            }
                        })
                        .catch(error => {
                            console.error('获取活动信息失败:', error);
                            UI.showMessage('获取活动信息失败', 'error');
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                });
            });
            
            // 删除活动
            const deleteButtons = container.querySelectorAll('.delete-activity');
            deleteButtons.forEach(button => {
                button.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const activityId = button.getAttribute('data-id');
                    this.deleteActivity(activityId);
                });
            });
        } else {
            // 参加活动
            const joinButtons = container.querySelectorAll('.join-activity');
            joinButtons.forEach(button => {
                button.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const activityId = button.getAttribute('data-id');
                    this.joinActivity(activityId);
                });
            });
        }
    },
    
    /**
     * 查看活动详情
     * @param {string} activityId 活动ID
     */
    viewActivityDetail: function(activityId) {
        UI.showLoading();
        
        API.activity.getActivityById(activityId)
            .then(response => {
                if (response.code === 200) {
                    const activity = response.data;
                    
                    // 获取活动状态
                    const now = new Date().getTime();
                    const startTime = new Date(activity.startTime).getTime();
                    const endTime = new Date(activity.endTime).getTime();
                    
                    let statusClass = '';
                    let statusText = '';
                    
                    if (now < startTime) {
                        statusClass = 'badge-primary';
                        statusText = '即将开始';
                    } else if (now >= startTime && now <= endTime) {
                        statusClass = 'badge-success';
                        statusText = '进行中';
                    } else {
                        statusClass = 'badge-secondary';
                        statusText = '已结束';
                    }
                    
                    // 格式化时间
                    const startDate = UI.formatDateTime(activity.startTime, 'datetime');
                    const endDate = UI.formatDateTime(activity.endTime, 'datetime');
                    
                    // 创建对话框内容
                    let content = `
                        <div class="activity-detail">
                            ${activity.posterUrl ? 
                                `<div class="detail-poster">
                                    <img src="${activity.posterUrl}" alt="${activity.title}" class="img-fluid">
                                </div>` : ''
                            }
                            
                            <div class="detail-header">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h2 class="activity-title">${activity.title}</h2>
                                    <span class="badge ${statusClass}">${statusText}</span>
                                </div>
                                <div class="activity-meta">
                                    <span class="activity-organizer">
                                        <i class="bi bi-person-circle"></i> ${activity.organizer || '暂无组织者'}
                                    </span>
                                </div>
                            </div>
                            
                            <div class="detail-section">
                                <h4>活动时间</h4>
                                <p><i class="bi bi-calendar-event"></i> ${startDate} - ${endDate}</p>
                            </div>
                            
                            <div class="detail-section">
                                <h4>活动地点</h4>
                                <p><i class="bi bi-geo-alt"></i> ${activity.location || '暂无地点'}</p>
                            </div>
                            
                            <div class="detail-section">
                                <h4>活动详情</h4>
                                <div class="activity-description">
                                    ${activity.description || '暂无详细描述'}
                                </div>
                            </div>
                            
                            <div class="detail-section">
                                <h4>活动要求</h4>
                                <div class="activity-requirements">
                                    ${activity.requirements || '暂无特殊要求'}
                                </div>
                            </div>
                            
                            <div class="detail-actions">
                                ${now <= endTime ? 
                                    `<button id="joinActivityBtn" class="btn btn-success">立即参加</button>` : 
                                    `<button class="btn btn-secondary" disabled>活动已结束</button>`
                                }
                            </div>
                        </div>
                    `;
                    
                    // 创建对话框
                    const dialog = UI.createDialog('活动详情', content, '800px');
                    
                    // 添加参加活动按钮事件
                    const joinActivityBtn = dialog.body.querySelector('#joinActivityBtn');
                    if (joinActivityBtn) {
                        joinActivityBtn.addEventListener('click', () => {
                            dialog.close();
                            this.joinActivity(activityId);
                        });
                    }
                } else {
                    UI.showMessage(response.message || '获取活动详情失败', 'error');
                }
            })
            .catch(error => {
                console.error('获取活动详情失败:', error);
                UI.showMessage('获取活动详情失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 编辑活动
     * @param {Object} activity 活动对象
     */
    editActivity: function(activity) {
        const isAdd = !activity || !activity.id;
        const title = isAdd ? '发布活动' : '编辑活动';
        
        // 创建表单字段
        const fields = [
            { name: 'title', label: '活动标题', type: 'text', required: true },
            { name: 'activityType', label: '活动类型', type: 'select', options: [
                { value: '1', label: '文化活动' },
                { value: '2', label: '体育活动' },
                { value: '3', label: '学术活动' },
                { value: '4', label: '社团活动' },
                { value: '5', label: '志愿服务' },
                { value: '6', label: '其他活动' }
            ]},
            { name: 'startTime', label: '开始时间', type: 'datetime-local', required: true },
            { name: 'endTime', label: '结束时间', type: 'datetime-local', required: true },
            { name: 'location', label: '活动地点', type: 'text', required: true },
            { name: 'organizer', label: '组织者', type: 'text' },
            { name: 'capacity', label: '参与人数上限', type: 'number', min: 0 },
            { name: 'posterUrl', label: '海报链接', type: 'text' },
            { name: 'description', label: '活动详情', type: 'textarea', rows: 5, required: true },
            { name: 'requirements', label: '活动要求', type: 'textarea', rows: 3 }
        ];
        
        // 创建表单
        const form = UI.createForm(fields, activity || {}, (formData) => {
            UI.showLoading();
            
            const apiMethod = isAdd ? API.activity.addActivity : API.activity.updateActivity;
            
            // 如果是更新，添加ID
            if (!isAdd) {
                formData.id = activity.id;
            }
            
            apiMethod(formData)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage(isAdd ? '活动发布成功' : '活动更新成功', 'success');
                        dialog.close();
                        
                        // 刷新活动列表
                        const activityListContainer = document.getElementById('activityList');
                        if (activityListContainer) {
                            this.initActivityList('activityList', true);
                        }
                    } else {
                        UI.showMessage(response.message || (isAdd ? '活动发布失败' : '活动更新失败'), 'error');
                    }
                })
                .catch(error => {
                    console.error(isAdd ? '发布活动失败:' : '更新活动失败:', error);
                    UI.showMessage(isAdd ? '发布活动失败' : '更新活动失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        });
        
        // 创建对话框
        const dialog = UI.createDialog(title, form, '800px');
        
        // 格式化日期时间输入
        if (!isAdd && activity.startTime) {
            const startDateInput = document.querySelector('input[name="startTime"]');
            if (startDateInput) {
                const startDate = new Date(activity.startTime);
                startDateInput.value = startDate.toISOString().slice(0, 16);
            }
        }
        
        if (!isAdd && activity.endTime) {
            const endDateInput = document.querySelector('input[name="endTime"]');
            if (endDateInput) {
                const endDate = new Date(activity.endTime);
                endDateInput.value = endDate.toISOString().slice(0, 16);
            }
        }
        
        // 添加海报上传功能
        const posterUrlInput = document.querySelector('input[name="posterUrl"]');
        if (posterUrlInput) {
            const posterGroup = posterUrlInput.closest('.form-group');
            
            // 创建上传按钮
            const uploadButton = document.createElement('button');
            uploadButton.type = 'button';
            uploadButton.className = 'btn btn-outline-primary mt-2';
            uploadButton.innerHTML = '<i class="bi bi-upload"></i> 上传海报';
            
            // 创建预览区域
            const previewContainer = document.createElement('div');
            previewContainer.className = 'poster-preview mt-2';
            previewContainer.style.display = 'none';
            
            const previewImage = document.createElement('img');
            previewImage.className = 'img-thumbnail';
            previewImage.style.maxHeight = '200px';
            
            previewContainer.appendChild(previewImage);
            
            // 如果有海报，显示预览
            if (!isAdd && activity.posterUrl) {
                previewImage.src = activity.posterUrl;
                previewContainer.style.display = 'block';
            }
            
            // 创建隐藏的文件输入
            const fileInput = document.createElement('input');
            fileInput.type = 'file';
            fileInput.accept = 'image/*';
            fileInput.style.display = 'none';
            
            // 上传按钮点击事件
            uploadButton.addEventListener('click', () => {
                fileInput.click();
            });
            
            // 文件选择事件
            fileInput.addEventListener('change', () => {
                if (fileInput.files && fileInput.files[0]) {
                    UI.showLoading();
                    
                    // 创建表单数据
                    const formData = new FormData();
                    formData.append('file', fileInput.files[0]);
                    
                    // 上传文件
                    API.activity.uploadPoster(formData)
                        .then(response => {
                            if (response.code === 200) {
                                // 设置海报URL
                                posterUrlInput.value = response.data;
                                
                                // 显示预览
                                previewImage.src = response.data;
                                previewContainer.style.display = 'block';
                                
                                UI.showMessage('海报上传成功', 'success');
                            } else {
                                UI.showMessage(response.message || '海报上传失败', 'error');
                            }
                        })
                        .catch(error => {
                            console.error('海报上传失败:', error);
                            UI.showMessage('海报上传失败', 'error');
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                }
            });
            
            // 添加到表单组
            posterGroup.appendChild(fileInput);
            posterGroup.appendChild(uploadButton);
            posterGroup.appendChild(previewContainer);
        }
    },
    
    /**
     * 删除活动
     * @param {string} activityId 活动ID
     */
    deleteActivity: function(activityId) {
        if (confirm('确定要删除这个活动吗？删除后无法恢复。')) {
            UI.showLoading();
            
            API.activity.deleteActivity(activityId)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('活动删除成功', 'success');
                        
                        // 移除活动卡片
                        const activityCard = document.querySelector(`.activity-card[data-id="${activityId}"]`);
                        if (activityCard) {
                            activityCard.remove();
                        }
                        
                        // 检查是否还有活动
                        const activityCards = document.querySelectorAll('.activity-card');
                        if (activityCards.length === 0) {
                            const activityListContainer = document.getElementById('activityList');
                            if (activityListContainer) {
                                activityListContainer.innerHTML = '<div class="empty-state">暂无活动</div>';
                            }
                        }
                    } else {
                        UI.showMessage(response.message || '活动删除失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('删除活动失败:', error);
                    UI.showMessage('删除活动失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    },
    
    /**
     * 参加活动
     * @param {string} activityId 活动ID
     */
    joinActivity: function(activityId) {
        UI.showLoading();
        
        API.activity.joinActivity(activityId)
            .then(response => {
                if (response.code === 200) {
                    UI.showMessage('活动报名成功', 'success');
                    
                    // 更新UI
                    const joinButton = document.querySelector(`.join-activity[data-id="${activityId}"]`);
                    if (joinButton) {
                        joinButton.textContent = '已报名';
                        joinButton.className = 'btn btn-outline-success';
                        joinButton.disabled = true;
                    }
                } else {
                    UI.showMessage(response.message || '活动报名失败', 'error');
                }
            })
            .catch(error => {
                console.error('参加活动失败:', error);
                UI.showMessage('活动报名失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 退出活动
     * @param {string} activityId 活动ID
     */
    leaveActivity: function(activityId) {
        if (confirm('确定要取消参加这个活动吗？')) {
            UI.showLoading();
            
            API.activity.leaveActivity(activityId)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('已取消参加', 'success');
                        
                        // 更新UI
                        const activityCard = document.querySelector(`.activity-card[data-id="${activityId}"]`);
                        if (activityCard) {
                            activityCard.remove();
                        }
                        
                        // 检查是否还有活动
                        const activityCards = document.querySelectorAll('.activity-card');
                        if (activityCards.length === 0) {
                            const container = document.getElementById('myActivitiesList');
                            if (container) {
                                container.innerHTML = '<div class="empty-state">您暂未参加任何活动</div>';
                            }
                        }
                    } else {
                        UI.showMessage(response.message || '取消参加失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('退出活动失败:', error);
                    UI.showMessage('取消参加失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    },
    
    /**
     * 初始化我的活动列表
     * @param {string} containerId 容器ID
     */
    initMyActivities: function(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.activity.getMyActivities()
            .then(response => {
                if (response.code === 200) {
                    const activities = response.data;
                    
                    if (activities.length === 0) {
                        container.innerHTML = '<div class="empty-state">您暂未参加任何活动</div>';
                        return;
                    }
                    
                    // 创建活动列表
                    let html = '<div class="activity-grid">';
                    
                    activities.forEach(activity => {
                        // 获取活动状态
                        const now = new Date().getTime();
                        const startTime = new Date(activity.startTime).getTime();
                        const endTime = new Date(activity.endTime).getTime();
                        
                        let statusClass = '';
                        let statusText = '';
                        
                        if (now < startTime) {
                            statusClass = 'upcoming';
                            statusText = '即将开始';
                        } else if (now >= startTime && now <= endTime) {
                            statusClass = 'ongoing';
                            statusText = '进行中';
                        } else {
                            statusClass = 'ended';
                            statusText = '已结束';
                        }
                        
                        // 格式化时间
                        const startDate = UI.formatDateTime(activity.startTime, 'datetime');
                        const endDate = UI.formatDateTime(activity.endTime, 'datetime');
                        
                        html += `
                            <div class="activity-card" data-id="${activity.id}">
                                <div class="activity-status ${statusClass}">${statusText}</div>
                                ${activity.posterUrl ? 
                                    `<div class="activity-poster">
                                        <img src="${activity.posterUrl}" alt="${activity.title}">
                                    </div>` : ''
                                }
                                <div class="activity-header">
                                    <h3 class="activity-title">${activity.title}</h3>
                                </div>
                                <div class="activity-body">
                                    <p class="activity-description">${activity.description ? (activity.description.length > 100 ? activity.description.substring(0, 100) + '...' : activity.description) : '暂无描述'}</p>
                                    <div class="activity-info">
                                        <div class="info-item">
                                            <i class="bi bi-geo-alt"></i>
                                            <span>${activity.location || '暂无地点'}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-calendar-event"></i>
                                            <span>${startDate} - ${endDate}</span>
                                        </div>
                                        <div class="info-item">
                                            <i class="bi bi-person"></i>
                                            <span>${activity.organizer || '暂无组织者'}</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="activity-footer">
                                    <button class="btn btn-primary view-activity" data-id="${activity.id}">查看详情</button>
                                    ${now <= endTime ? 
                                        `<button class="btn btn-outline-danger leave-activity" data-id="${activity.id}">取消参加</button>` : 
                                        ''
                                    }
                                </div>
                            </div>
                        `;
                    });
                    
                    html += '</div>';
                    container.innerHTML = html;
                    
                    // 绑定事件
                    this.bindMyActivitiesEvents(container);
                } else {
                    container.innerHTML = `<div class="error-state">加载失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('获取我的活动列表失败:', error);
                container.innerHTML = '<div class="error-state">加载失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 绑定我的活动事件
     * @param {HTMLElement} container 容器元素
     */
    bindMyActivitiesEvents: function(container) {
        // 查看活动详情
        const viewButtons = container.querySelectorAll('.view-activity');
        viewButtons.forEach(button => {
            button.addEventListener('click', () => {
                const activityId = button.getAttribute('data-id');
                this.viewActivityDetail(activityId);
            });
        });
        
        // 取消参加
        const leaveButtons = container.querySelectorAll('.leave-activity');
        leaveButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const activityId = button.getAttribute('data-id');
                this.leaveActivity(activityId);
            });
        });
    }
};

// 将UIActivity添加到全局UI对象中
if (typeof UI !== 'undefined') {
    UI.activity = UIActivity;
} 