/**
 * 学生中心脚本
 */

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 检查登录状态
    checkLoginStatus();
    
    // 绑定事件
    bindEvents();
    
    // 初始化页面
    initPage();
});

/**
 * 检查登录状态
 */
function checkLoginStatus() {
    if (!API.isLoggedIn()) {
        // 未登录，重定向到登录页
        window.location.href = '../index.html';
        return;
    }
    
    // 检查用户类型
    const userType = parseInt(localStorage.getItem('userType'));
    if (userType !== 1 && userType !== 0) { // 学生或管理员可以访问
        UI.showMessage('您没有访问学生中心的权限', 'error');
        setTimeout(() => {
            window.location.href = '../index.html';
        }, 2000);
        return;
    }
}

/**
 * 绑定事件
 */
function bindEvents() {
    // 退出登录按钮
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    }
    
    // 用户菜单
    const userMenu = document.querySelector('.user-menu');
    if (userMenu) {
        userMenu.addEventListener('click', function() {
            this.classList.toggle('active');
        });
        
        // 点击外部关闭菜单
        document.addEventListener('click', function(e) {
            if (!userMenu.contains(e.target)) {
                userMenu.classList.remove('active');
            }
        });
    }
}

/**
 * 初始化页面
 */
function initPage() {
    // 显示加载动画
    showLoading();
    
    // 加载用户信息
    loadUserInfo();
    
    // 设置日期信息
    setDateInfo();
    
    // 加载今日课程
    loadTodayClasses();
    
    // 加载最新通知
    loadNotifications();
    
    // 加载最近文件
    loadRecentFiles();
    
    // 隐藏加载动画
    hideLoading(1000);
}

/**
 * 加载用户信息
 */
function loadUserInfo() {
    const userName = document.getElementById('userName');
    const welcomeMessage = document.querySelector('.welcome-message');
    
    // 从本地存储获取用户信息
    const user = JSON.parse(localStorage.getItem('user')) || {};
    const username = user.name || user.username || '同学';
    
    // 设置用户名
    if (userName) {
        userName.textContent = username;
    }
    
    // 设置欢迎消息
    if (welcomeMessage) {
        welcomeMessage.textContent = `你好，${username}！`;
    }
    
    // 如果本地没有完整的用户信息，则从API获取
    if (!user.realName) {
        API.student.getProfile()
            .then(userInfo => {
                if (userInfo) {
                    // 更新本地存储
                    localStorage.setItem('user', JSON.stringify(userInfo));
                    
                    // 更新界面
                    const fullName = userInfo.realName || userInfo.username || '同学';
                    if (userName) {
                        userName.textContent = fullName;
                    }
                    if (welcomeMessage) {
                        welcomeMessage.textContent = `你好，${fullName}！`;
                    }
                }
            })
            .catch(error => {
                console.error('获取用户信息失败:', error);
            });
    }
}

/**
 * 设置日期信息
 */
function setDateInfo() {
    const welcomeDate = document.querySelector('.welcome-date');
    if (!welcomeDate) return;
    
    const now = new Date();
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    
    welcomeDate.textContent = now.toLocaleDateString('zh-CN', options);
}

/**
 * 加载今日课程
 */
function loadTodayClasses() {
    const scheduleContainer = document.querySelector('.today-schedule');
    if (!scheduleContainer) return;
    
    scheduleContainer.innerHTML = '<div class="loading-state">加载中...</div>';
    
    // 获取今日课程
    API.student.getTodaySchedule()
        .then(classes => {
            if (!classes || classes.length === 0) {
                scheduleContainer.innerHTML = '<div class="empty-state">今天没有课程安排</div>';
                return;
            }
            
            // 按照时间排序
            classes.sort((a, b) => a.timeSlot - b.timeSlot);
            
            let html = '';
            classes.forEach(cls => {
                // 获取开始时间和结束时间
                const timeMap = {
                    1: { start: '08:00', end: '08:45' },
                    2: { start: '08:55', end: '09:40' },
                    3: { start: '10:00', end: '10:45' },
                    4: { start: '10:55', end: '11:40' },
                    5: { start: '13:30', end: '14:15' },
                    6: { start: '14:25', end: '15:10' },
                    7: { start: '15:30', end: '16:15' },
                    8: { start: '16:25', end: '17:10' },
                    9: { start: '18:30', end: '19:15' },
                    10: { start: '19:25', end: '20:10' }
                };
                
                const time = timeMap[cls.timeSlot] || { start: '未知', end: '未知' };
                
                html += `
                    <div class="schedule-item">
                        <div class="schedule-time">
                            <span class="time-start">${time.start}</span>
                            <span class="time-end">${time.end}</span>
                        </div>
                        <div class="schedule-content">
                            <h4 class="course-name">${cls.courseName || '未知课程'}</h4>
                            <p class="course-info">
                                <span class="course-teacher">${cls.teacherName || '未知教师'}</span>
                                <span class="course-location">${cls.classroom || '未知地点'}</span>
                            </p>
                        </div>
                    </div>
                `;
            });
            
            scheduleContainer.innerHTML = html;
        })
        .catch(error => {
            console.error('获取今日课程失败:', error);
            scheduleContainer.innerHTML = '<div class="error-state">获取课程失败，请稍后再试</div>';
        });
}

/**
 * 加载最新通知
 */
function loadNotifications() {
    const noticesContainer = document.querySelector('.notifications-list');
    if (!noticesContainer) return;
    
    noticesContainer.innerHTML = '<div class="loading-state">加载中...</div>';
    
    API.student.getRecentNotices(5)
        .then(notices => {
            if (!notices || notices.length === 0) {
                noticesContainer.innerHTML = '<div class="empty-state">暂无通知</div>';
                return;
            }
            
            let html = '';
            notices.forEach(notice => {
                const date = new Date(notice.publishTime).toLocaleDateString('zh-CN');
                const typeClass = getNoticeTypeClass(notice.type);
                
                html += `
                    <div class="notice-item" data-id="${notice.id}">
                        <div class="notice-icon ${typeClass}">
                            <i class="bi bi-bell"></i>
                        </div>
                        <div class="notice-content">
                            <h4 class="notice-title">${notice.title}</h4>
                            <p class="notice-meta">
                                <span class="notice-time">${date}</span>
                                <span class="notice-publisher">${notice.publisher || '系统通知'}</span>
                            </p>
                        </div>
                    </div>
                `;
            });
            
            noticesContainer.innerHTML = html;
            
            // 绑定通知点击事件
            noticesContainer.querySelectorAll('.notice-item').forEach(item => {
                item.addEventListener('click', function() {
                    const noticeId = this.getAttribute('data-id');
                    showNoticeDetail(noticeId);
                });
            });
        })
        .catch(error => {
            console.error('获取通知失败:', error);
            noticesContainer.innerHTML = '<div class="error-state">获取通知失败，请稍后再试</div>';
        });
}

/**
 * 获取通知类型样式类
 * @param {number} type 通知类型
 * @returns {string} 样式类名
 */
function getNoticeTypeClass(type) {
    switch (type) {
        case 1: return 'notice-important';
        case 2: return 'notice-warning';
        case 3: return 'notice-course';
        case 4: return 'notice-activity';
        default: return 'notice-normal';
    }
}

/**
 * 显示通知详情
 * @param {string} noticeId 通知ID
 */
function showNoticeDetail(noticeId) {
    showLoading();
    
    API.student.getNoticeById(noticeId)
        .then(notice => {
            if (!notice) {
                UI.showMessage('通知不存在', 'error');
                return;
            }
            
            // 标记为已读
            API.student.markNoticeAsRead(noticeId).catch(err => console.error('标记已读失败:', err));
            
            // 格式化通知内容
            const publishTime = new Date(notice.publishTime).toLocaleString('zh-CN');
            const expireTime = notice.expireTime ? new Date(notice.expireTime).toLocaleString('zh-CN') : '无期限';
            
            // 创建模态框
            let modalHtml = `
                <div class="modal notice-modal">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>${notice.title}</h3>
                            <button class="close-btn">&times;</button>
                        </div>
                        <div class="modal-body">
                            <div class="notice-meta">
                                <div class="meta-item">
                                    <span class="meta-label">发布者：</span>
                                    <span class="meta-value">${notice.publisher || '系统通知'}</span>
                                </div>
                                <div class="meta-item">
                                    <span class="meta-label">发布时间：</span>
                                    <span class="meta-value">${publishTime}</span>
                                </div>
                                <div class="meta-item">
                                    <span class="meta-label">到期时间：</span>
                                    <span class="meta-value">${expireTime}</span>
                                </div>
                            </div>
                            <div class="notice-detail">${notice.content || '暂无内容'}</div>
                        </div>
                        <div class="modal-footer">
                            <button class="btn btn-primary close-modal">确定</button>
                        </div>
                    </div>
                </div>
            `;
            
            // 添加到DOM
            const modalContainer = document.createElement('div');
            modalContainer.className = 'modal-container';
            modalContainer.innerHTML = modalHtml;
            document.body.appendChild(modalContainer);
            
            // 绑定关闭事件
            modalContainer.querySelector('.close-btn').addEventListener('click', () => {
                document.body.removeChild(modalContainer);
            });
            
            modalContainer.querySelector('.close-modal').addEventListener('click', () => {
                document.body.removeChild(modalContainer);
            });
            
            // 点击空白处关闭
            modalContainer.addEventListener('click', (e) => {
                if (e.target === modalContainer) {
                    document.body.removeChild(modalContainer);
                }
            });
        })
        .catch(error => {
            console.error('获取通知详情失败:', error);
            UI.showMessage('获取通知详情失败', 'error');
        })
        .finally(() => {
            hideLoading();
        });
}

/**
 * 加载最近文件
 */
function loadRecentFiles() {
    const filesContainer = document.querySelector('.recent-files');
    if (!filesContainer) return;
    
    filesContainer.innerHTML = '<div class="loading-state">加载中...</div>';
    
    API.student.getRecentFiles(5)
        .then(files => {
            if (!files || files.length === 0) {
                filesContainer.innerHTML = '<div class="empty-state">暂无文件</div>';
                return;
            }
            
            let html = '';
            files.forEach(file => {
                // 获取文件图标
                let iconClass = 'bi-file-earmark';
                if (file.mimeType) {
                    if (file.mimeType.includes('image')) {
                        iconClass = 'bi-file-earmark-image';
                    } else if (file.mimeType.includes('pdf')) {
                        iconClass = 'bi-file-earmark-pdf';
                    } else if (file.mimeType.includes('word')) {
                        iconClass = 'bi-file-earmark-word';
                    } else if (file.mimeType.includes('excel') || file.mimeType.includes('spreadsheet')) {
                        iconClass = 'bi-file-earmark-excel';
                    } else if (file.mimeType.includes('presentation') || file.mimeType.includes('powerpoint')) {
                        iconClass = 'bi-file-earmark-ppt';
                    } else if (file.mimeType.includes('zip') || file.mimeType.includes('rar') || file.mimeType.includes('compressed')) {
                        iconClass = 'bi-file-earmark-zip';
                    }
                }
                
                // 格式化文件大小
                const size = formatFileSize(file.size);
                
                // 格式化日期
                const date = new Date(file.uploadTime).toLocaleDateString('zh-CN');
                
                html += `
                    <div class="file-item" data-id="${file.id}">
                        <div class="file-icon">
                            <i class="bi ${iconClass}"></i>
                        </div>
                        <div class="file-info">
                            <h4 class="file-name">${file.name}</h4>
                            <p class="file-meta">
                                <span class="file-size">${size}</span>
                                <span class="file-date">${date}</span>
                            </p>
                        </div>
                        <div class="file-actions">
                            <button class="btn btn-icon download-file" title="下载">
                                <i class="bi bi-download"></i>
                            </button>
                        </div>
                    </div>
                `;
            });
            
            filesContainer.innerHTML = html;
            
            // 绑定文件操作事件
            filesContainer.querySelectorAll('.download-file').forEach(btn => {
                btn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    const fileItem = this.closest('.file-item');
                    const fileId = fileItem.getAttribute('data-id');
                    downloadFile(fileId);
                });
            });
            
            // 点击文件项也可下载
            filesContainer.querySelectorAll('.file-item').forEach(item => {
                item.addEventListener('click', function() {
                    const fileId = this.getAttribute('data-id');
                    downloadFile(fileId);
                });
            });
        })
        .catch(error => {
            console.error('获取文件失败:', error);
            filesContainer.innerHTML = '<div class="error-state">获取文件失败，请稍后再试</div>';
        });
}

/**
 * 格式化文件大小
 * @param {number} bytes 文件大小（字节）
 * @returns {string} 格式化后的大小
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * 下载文件
 * @param {string} fileId 文件ID
 */
function downloadFile(fileId) {
    showLoading();
    
    API.student.downloadFile(fileId)
        .then(result => {
            // 创建临时链接并点击下载
            const downloadUrl = result.downloadUrl || `/api/files/${fileId}/download`;
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.target = '_blank';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        })
        .catch(error => {
            console.error('下载文件失败:', error);
            UI.showMessage('下载文件失败', 'error');
        })
        .finally(() => {
            hideLoading();
        });
}

/**
 * 退出登录
 */
function logout() {
    if (confirm('确定要退出登录吗？')) {
        showLoading();
        
        API.logout()
            .then(() => {
                window.location.href = '../index.html';
            })
            .catch(error => {
                console.error('退出登录失败:', error);
                UI.showMessage('退出登录失败', 'error');
                hideLoading();
            });
    }
}

/**
 * 显示加载动画
 */
function showLoading() {
    document.getElementById('loadingMask').classList.add('active');
}

/**
 * 隐藏加载动画
 * @param {number} delay - 延迟时间（毫秒）
 */
function hideLoading(delay = 0) {
    setTimeout(() => {
        document.getElementById('loadingMask').classList.remove('active');
    }, delay);
}

/**
 * 截断文本
 * @param {string} text 文本
 * @param {number} maxLength 最大长度
 * @returns {string} 截断后的文本
 */
function truncateText(text, maxLength) {
    if (!text || text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
} 