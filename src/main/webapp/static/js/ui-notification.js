/**
 * 通知模块UI组件
 * 提供通知显示、处理和管理功能
 */
const UINotification = {
    /**
     * 初始化通知中心
     * @param {string} containerId 容器ID
     * @param {Object} options 选项
     */
    initNotificationCenter: function(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 默认选项
        const defaultOptions = {
            updateInterval: 60000, // 通知更新间隔（毫秒）
            maxNotificationCount: 50, // 最大通知显示数量
            showPriority: true, // 是否显示优先级
            showTime: true, // 是否显示时间
        };
        
        // 合并选项
        const mergedOptions = { ...defaultOptions, ...options };
        this.options = mergedOptions;
        
        // 创建通知中心界面
        const html = `
            <div class="notification-center">
                <div class="notification-header">
                    <h3 class="title">通知中心</h3>
                    <div class="notification-actions">
                        <button class="btn btn-sm btn-outline-primary mark-all-read-btn">
                            <i class="bi bi-check-all"></i> 标记全部已读
                        </button>
                        <button class="btn btn-sm btn-outline-danger clear-all-btn">
                            <i class="bi bi-trash"></i> 清除全部
                        </button>
                    </div>
                </div>
                
                <div class="notification-tabs">
                    <ul class="nav nav-tabs">
                        <li class="nav-item">
                            <a class="nav-link active" data-tab="all">全部</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" data-tab="unread">未读</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" data-tab="read">已读</a>
                        </li>
                    </ul>
                </div>
                
                <div class="notification-list-container">
                    <div class="notification-list" id="notificationList"></div>
                    <div class="notification-empty-state" style="display: none;">
                        <i class="bi bi-bell"></i>
                        <p>暂无通知</p>
                    </div>
                    <div class="notification-loading" style="display: none;">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">正在加载...</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        container.innerHTML = html;
        
        // 绑定事件
        this.bindNotificationEvents(container);
        
        // 加载通知
        this.loadNotifications();
        
        // 设置定时更新
        this.startNotificationUpdates();
    },
    
    /**
     * 绑定通知中心事件
     * @param {HTMLElement} container 容器元素
     */
    bindNotificationEvents: function(container) {
        // 标记全部已读按钮
        const markAllReadBtn = container.querySelector('.mark-all-read-btn');
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', () => {
                this.markAllAsRead();
            });
        }
        
        // 清除全部按钮
        const clearAllBtn = container.querySelector('.clear-all-btn');
        if (clearAllBtn) {
            clearAllBtn.addEventListener('click', () => {
                this.clearAllNotifications();
            });
        }
        
        // 标签切换
        const tabLinks = container.querySelectorAll('.notification-tabs .nav-link');
        tabLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                
                // 更新标签状态
                tabLinks.forEach(tab => tab.classList.remove('active'));
                link.classList.add('active');
                
                // 加载对应标签的通知
                const tab = link.getAttribute('data-tab');
                this.loadNotifications(tab);
            });
        });
    },
    
    /**
     * 加载通知
     * @param {string} tab 标签类型：'all', 'unread', 'read'
     */
    loadNotifications: function(tab = 'all') {
        const container = document.getElementById('notificationList');
        if (!container) return;
        
        // 显示加载状态
        const loadingEl = document.querySelector('.notification-loading');
        const emptyStateEl = document.querySelector('.notification-empty-state');
        
        if (loadingEl) loadingEl.style.display = 'flex';
        if (emptyStateEl) emptyStateEl.style.display = 'none';
        if (container) container.style.display = 'none';
        
        // 准备请求参数
        const params = {
            status: tab === 'read' ? 1 : (tab === 'unread' ? 0 : undefined),
            limit: this.options.maxNotificationCount
        };
        
        // 发送API请求
        API.notification.getNotifications(params)
            .then(response => {
                if (response.code === 200) {
                    const notifications = response.data;
                    
                    if (notifications && notifications.length > 0) {
                        this.renderNotifications(container, notifications);
                        
                        if (container) container.style.display = 'block';
                        if (emptyStateEl) emptyStateEl.style.display = 'none';
                    } else {
                        if (container) container.style.display = 'none';
                        if (emptyStateEl) emptyStateEl.style.display = 'flex';
                    }
                } else {
                    UI.showMessage(response.message || '加载通知失败', 'error');
                    
                    if (container) container.style.display = 'none';
                    if (emptyStateEl) {
                        emptyStateEl.style.display = 'flex';
                        emptyStateEl.querySelector('p').textContent = '加载通知失败';
                    }
                }
            })
            .catch(error => {
                console.error('加载通知失败:', error);
                UI.showMessage('加载通知失败', 'error');
                
                if (container) container.style.display = 'none';
                if (emptyStateEl) {
                    emptyStateEl.style.display = 'flex';
                    emptyStateEl.querySelector('p').textContent = '加载通知失败';
                }
            })
            .finally(() => {
                if (loadingEl) loadingEl.style.display = 'none';
            });
    },
    
    /**
     * 渲染通知列表
     * @param {HTMLElement} container 容器元素
     * @param {Array} notifications 通知数据
     */
    renderNotifications: function(container, notifications) {
        if (!container) return;
        
        let html = '';
        
        notifications.forEach(notification => {
            // 根据优先级定义样式类
            let priorityClass = '';
            switch (notification.priority) {
                case 3: // 高
                    priorityClass = 'high-priority';
                    break;
                case 2: // 中
                    priorityClass = 'medium-priority';
                    break;
                case 1: // 低
                    priorityClass = 'low-priority';
                    break;
                default:
                    priorityClass = '';
            }
            
            // 未读/已读状态
            const readStatusClass = notification.status === 0 ? 'unread' : 'read';
            
            // 格式化时间
            const timeString = this.formatTime(notification.sendTime);
            
            html += `
                <div class="notification-item ${readStatusClass} ${priorityClass}" data-id="${notification.id}">
                    <div class="notification-indicator"></div>
                    <div class="notification-content">
                        <div class="notification-title">${notification.title}</div>
                        <div class="notification-body">${notification.content}</div>
                        <div class="notification-footer">
                            ${this.options.showPriority && notification.priority > 1 ? `
                                <span class="notification-priority priority-${notification.priority}">
                                    ${this.formatPriority(notification.priority)}
                                </span>
                            ` : ''}
                            ${notification.sender ? `
                                <span class="notification-sender">${notification.sender.realName || notification.sender.username}</span>
                            ` : ''}
                            ${this.options.showTime ? `
                                <span class="notification-time">${timeString}</span>
                            ` : ''}
                        </div>
                    </div>
                    <div class="notification-actions">
                        ${notification.status === 0 ? `
                            <button class="btn btn-sm btn-link mark-read-btn" data-id="${notification.id}" title="标记为已读">
                                <i class="bi bi-check"></i>
                            </button>
                        ` : ''}
                        <button class="btn btn-sm btn-link delete-notification-btn" data-id="${notification.id}" title="删除">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
        
        // 绑定通知项事件
        this.bindNotificationItemEvents(container);
    },
    
    /**
     * 绑定通知项事件
     * @param {HTMLElement} container 容器元素
     */
    bindNotificationItemEvents: function(container) {
        // 点击通知项
        const notificationItems = container.querySelectorAll('.notification-item');
        notificationItems.forEach(item => {
            const itemId = item.getAttribute('data-id');
            
            // 点击整个通知项打开详情
            item.addEventListener('click', (e) => {
                // 如果点击的是操作按钮，则不打开详情
                if (e.target.closest('.notification-actions')) {
                    return;
                }
                
                this.viewNotificationDetail(itemId);
                
                // 如果通知是未读状态，标记为已读
                if (item.classList.contains('unread')) {
                    this.markAsRead(itemId);
                    item.classList.remove('unread');
                    item.classList.add('read');
                    
                    // 隐藏标记为已读按钮
                    const markReadBtn = item.querySelector('.mark-read-btn');
                    if (markReadBtn) {
                        markReadBtn.style.display = 'none';
                    }
                }
            });
            
            // 标记为已读按钮
            const markReadBtn = item.querySelector('.mark-read-btn');
            if (markReadBtn) {
                markReadBtn.addEventListener('click', (e) => {
                    e.stopPropagation(); // 阻止冒泡
                    this.markAsRead(itemId);
                    item.classList.remove('unread');
                    item.classList.add('read');
                    markReadBtn.style.display = 'none';
                });
            }
            
            // 删除按钮
            const deleteBtn = item.querySelector('.delete-notification-btn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', (e) => {
                    e.stopPropagation(); // 阻止冒泡
                    this.deleteNotification(itemId);
                    // 从界面移除
                    item.classList.add('removing');
                    setTimeout(() => {
                        item.remove();
                        
                        // 检查是否已经没有通知了
                        const remainingItems = container.querySelectorAll('.notification-item');
                        if (remainingItems.length === 0) {
                            const emptyStateEl = document.querySelector('.notification-empty-state');
                            if (emptyStateEl) emptyStateEl.style.display = 'flex';
                            container.style.display = 'none';
                        }
                    }, 300);
                });
            }
        });
    },
    
    /**
     * 查看通知详情
     * @param {string} notificationId 通知ID
     */
    viewNotificationDetail: function(notificationId) {
        API.notification.getNotificationDetail(notificationId)
            .then(response => {
                if (response.code === 200) {
                    const notification = response.data;
                    
                    // 创建详情对话框内容
                    const content = `
                        <div class="notification-detail">
                            <h3 class="notification-title">${notification.title}</h3>
                            
                            <div class="notification-meta">
                                ${notification.sender ? `
                                    <div class="meta-item sender">
                                        <label>发送者：</label>
                                        <span>${notification.sender.realName || notification.sender.username}</span>
                                    </div>
                                ` : ''}
                                <div class="meta-item time">
                                    <label>发送时间：</label>
                                    <span>${this.formatFullTime(notification.sendTime)}</span>
                                </div>
                                ${notification.expireTime ? `
                                    <div class="meta-item expire-time">
                                        <label>过期时间：</label>
                                        <span>${this.formatFullTime(notification.expireTime)}</span>
                                    </div>
                                ` : ''}
                                <div class="meta-item priority">
                                    <label>优先级：</label>
                                    <span class="priority-${notification.priority}">${this.formatPriority(notification.priority)}</span>
                                </div>
                                <div class="meta-item type">
                                    <label>类型：</label>
                                    <span>${this.formatType(notification.type)}</span>
                                </div>
                            </div>
                            
                            <div class="notification-content">
                                ${notification.content}
                            </div>
                            
                            ${notification.type === 'link' && notification.link ? `
                                <div class="notification-link">
                                    <a href="${notification.link}" class="btn btn-primary" target="_blank">
                                        <i class="bi bi-box-arrow-up-right"></i> 访问链接
                                    </a>
                                </div>
                            ` : ''}
                        </div>
                    `;
                    
                    // 创建对话框
                    UI.createDialog('通知详情', content, '600px');
                } else {
                    UI.showMessage(response.message || '获取通知详情失败', 'error');
                }
            })
            .catch(error => {
                console.error('获取通知详情失败:', error);
                UI.showMessage('获取通知详情失败', 'error');
            });
    },
    
    /**
     * 标记通知为已读
     * @param {string} notificationId 通知ID
     */
    markAsRead: function(notificationId) {
        API.notification.markAsRead(notificationId)
            .then(response => {
                if (response.code !== 200) {
                    UI.showMessage(response.message || '标记已读失败', 'error');
                }
            })
            .catch(error => {
                console.error('标记已读失败:', error);
                UI.showMessage('标记已读失败', 'error');
            });
    },
    
    /**
     * 标记所有通知为已读
     */
    markAllAsRead: function() {
        UI.showConfirm('确认', '是否标记所有通知为已读？', () => {
            API.notification.markAllAsRead()
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('已标记所有通知为已读', 'success');
                        this.loadNotifications();
                        
                        // 更新未读通知数量
                        this.updateUnreadCount();
                    } else {
                        UI.showMessage(response.message || '操作失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('标记所有已读失败:', error);
                    UI.showMessage('操作失败', 'error');
                });
        });
    },
    
    /**
     * 删除单个通知
     * @param {string} notificationId 通知ID
     */
    deleteNotification: function(notificationId) {
        API.notification.deleteNotification(notificationId)
            .then(response => {
                if (response.code !== 200) {
                    UI.showMessage(response.message || '删除通知失败', 'error');
                }
            })
            .catch(error => {
                console.error('删除通知失败:', error);
                UI.showMessage('删除通知失败', 'error');
            });
    },
    
    /**
     * 清除所有通知
     */
    clearAllNotifications: function() {
        UI.showConfirm('确认', '是否清除所有通知？此操作不可撤销。', () => {
            API.notification.clearAllNotifications()
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('已清除所有通知', 'success');
                        this.loadNotifications();
                        
                        // 更新未读通知数量
                        this.updateUnreadCount(0);
                    } else {
                        UI.showMessage(response.message || '操作失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('清除所有通知失败:', error);
                    UI.showMessage('操作失败', 'error');
                });
        });
    },
    
    /**
     * 初始化通知提示
     * @param {string} elementId 元素ID
     */
    initNotificationBadge: function(elementId) {
        // 初始化未读数量
        this.updateUnreadCount();
        
        // 设置定时更新
        setInterval(() => {
            this.updateUnreadCount();
        }, this.options.updateInterval);
    },
    
    /**
     * 更新未读通知数量
     * @param {number} count 未读数量，如果不提供则会从API获取
     */
    updateUnreadCount: function(count) {
        if (count !== undefined) {
            this.setUnreadCount(count);
            return;
        }
        
        API.notification.getUnreadCount()
            .then(response => {
                if (response.code === 200) {
                    this.setUnreadCount(response.data.count);
                }
            })
            .catch(error => {
                console.error('获取未读通知数量失败:', error);
            });
    },
    
    /**
     * 设置未读通知数量
     * @param {number} count 未读数量
     */
    setUnreadCount: function(count) {
        // 更新所有通知徽章
        const badges = document.querySelectorAll('.notification-badge');
        badges.forEach(badge => {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        });
    },
    
    /**
     * 开始定时更新通知
     */
    startNotificationUpdates: function() {
        setInterval(() => {
            // 获取当前活动的标签
            const activeTab = document.querySelector('.notification-tabs .nav-link.active');
            if (activeTab) {
                const tab = activeTab.getAttribute('data-tab');
                this.loadNotifications(tab);
            }
        }, this.options.updateInterval);
    },
    
    /**
     * 格式化时间（相对时间）
     * @param {string|number} timestamp 时间戳
     * @returns {string} 格式化后的时间
     */
    formatTime: function(timestamp) {
        if (!timestamp) return '';
        
        const now = new Date();
        const date = new Date(timestamp);
        const diffSeconds = Math.floor((now - date) / 1000);
        
        if (diffSeconds < 60) {
            return '刚刚';
        } else if (diffSeconds < 3600) {
            return Math.floor(diffSeconds / 60) + '分钟前';
        } else if (diffSeconds < 86400) {
            return Math.floor(diffSeconds / 3600) + '小时前';
        } else if (diffSeconds < 604800) {
            return Math.floor(diffSeconds / 86400) + '天前';
        } else {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        }
    },
    
    /**
     * 格式化完整时间
     * @param {string|number} timestamp 时间戳
     * @returns {string} 格式化后的时间
     */
    formatFullTime: function(timestamp) {
        if (!timestamp) return '';
        
        const date = new Date(timestamp);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    
    /**
     * 格式化优先级
     * @param {number} priority 优先级
     * @returns {string} 格式化后的优先级
     */
    formatPriority: function(priority) {
        switch (priority) {
            case 3:
                return '高';
            case 2:
                return '中';
            case 1:
                return '低';
            default:
                return '普通';
        }
    },
    
    /**
     * 格式化通知类型
     * @param {string} type 通知类型
     * @returns {string} 格式化后的类型
     */
    formatType: function(type) {
        switch (type) {
            case 'system':
                return '系统通知';
            case 'announcement':
                return '公告';
            case 'message':
                return '消息';
            case 'alert':
                return '提醒';
            case 'warning':
                return '警告';
            case 'link':
                return '链接';
            default:
                return '其他';
        }
    }
};

// 将UINotification添加到全局UI对象中
if (typeof UI !== 'undefined') {
    UI.notification = UINotification;
} 