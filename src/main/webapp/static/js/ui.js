/**
 * UI工具类
 * 处理界面相关的通用功能
 */
const UI = {
    /**
     * 显示加载中提示
     */
    showLoading() {
        const loading = document.getElementById('loading');
        if (loading) {
            loading.style.display = 'flex';
        } else {
            // 如果页面中没有加载元素，创建一个
            const loadingEl = document.createElement('div');
            loadingEl.id = 'loading';
            loadingEl.className = 'loading-container';
            loadingEl.innerHTML = `
                <div class="loading-spinner"></div>
                <div class="loading-text">加载中...</div>
            `;
            document.body.appendChild(loadingEl);
        }
    },
    
    /**
     * 隐藏加载中提示
     */
    hideLoading() {
        const loading = document.getElementById('loading');
        if (loading) {
            loading.style.display = 'none';
        }
    },
    
    /**
     * 显示消息提示
     * @param {string} message 消息内容
     * @param {string} type 消息类型：success, error, warning, info
     */
    showMessage(message, type = 'info') {
        // 检查是否已存在消息容器
        let container = document.querySelector('.message-container');
        
        // 如果不存在，创建一个
        if (!container) {
            container = document.createElement('div');
            container.className = 'message-container';
            document.body.appendChild(container);
        }
        
        // 创建消息元素
        const messageEl = document.createElement('div');
        messageEl.className = `message message-${type}`;
        messageEl.textContent = message;
        
        // 添加到容器
        container.appendChild(messageEl);
        
        // 设置自动消失
        setTimeout(() => {
            messageEl.style.animation = 'slideOut 0.3s ease-out forwards';
            setTimeout(() => {
                container.removeChild(messageEl);
                
                // 如果容器为空，移除容器
                if (container.children.length === 0) {
                    document.body.removeChild(container);
                }
            }, 300);
        }, 2000);
    },
    
    /**
     * 创建表格
     * @param {Array} columns 列配置 [{title, field, width, render}]
     * @param {Array} data 数据
     * @param {Object} actions 行操作按钮配置 {edit, delete, view, custom}
     * @returns {HTMLElement} 表格元素
     */
    createTable(columns, data, actions = null) {
        const table = document.createElement('table');
        table.className = 'table';
        
        // 创建表头
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        
        // 添加列头
        columns.forEach(column => {
            const th = document.createElement('th');
            th.textContent = column.title;
            if (column.width) {
                th.style.width = column.width;
            }
            headerRow.appendChild(th);
        });
        
        // 如果有操作按钮，添加操作列
        if (actions) {
            const actionTh = document.createElement('th');
            actionTh.textContent = '操作';
            actionTh.style.width = '150px';
            headerRow.appendChild(actionTh);
        }
        
        thead.appendChild(headerRow);
        table.appendChild(thead);
        
        // 创建表格内容
        const tbody = document.createElement('tbody');
        
        // 如果数据为空
        if (!data || data.length === 0) {
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.colSpan = columns.length + (actions ? 1 : 0);
            emptyCell.textContent = '暂无数据';
            emptyCell.style.textAlign = 'center';
            emptyCell.style.padding = '1.5rem';
            emptyRow.appendChild(emptyCell);
            tbody.appendChild(emptyRow);
        } else {
            // 添加数据行
            data.forEach(item => {
                const row = document.createElement('tr');
                
                // 添加数据列
                columns.forEach(column => {
                    const cell = document.createElement('td');
                    
                    // 如果列有自定义渲染函数，使用它
                    if (column.render) {
                        cell.innerHTML = column.render(item[column.field], item);
                    } else {
                        cell.textContent = item[column.field] !== undefined ? item[column.field] : '';
                    }
                    
                    row.appendChild(cell);
                });
                
                // 如果有操作按钮，添加操作列
                if (actions) {
                    const actionCell = document.createElement('td');
                    
                    // 添加操作按钮
                    const actionButtons = [];
                    
                    if (actions.view) {
                        const viewBtn = document.createElement('button');
                        viewBtn.className = 'btn btn-default';
                        viewBtn.textContent = '查看';
                        viewBtn.style.marginRight = '5px';
                        viewBtn.onclick = () => actions.view(item);
                        actionButtons.push(viewBtn);
                    }
                    
                    if (actions.edit) {
                        const editBtn = document.createElement('button');
                        editBtn.className = 'btn btn-primary';
                        editBtn.textContent = '编辑';
                        editBtn.style.marginRight = '5px';
                        editBtn.onclick = () => actions.edit(item);
                        actionButtons.push(editBtn);
                    }
                    
                    if (actions.delete) {
                        const deleteBtn = document.createElement('button');
                        deleteBtn.className = 'btn btn-danger';
                        deleteBtn.textContent = '删除';
                        deleteBtn.onclick = () => actions.delete(item);
                        actionButtons.push(deleteBtn);
                    }
                    
                    // 添加自定义按钮
                    if (actions.custom) {
                        actions.custom.forEach(customAction => {
                            if (typeof customAction === 'function') {
                                const customButton = customAction(item);
                                actionButtons.push(customButton);
                            } else {
                                const customBtn = document.createElement('button');
                                customBtn.className = customAction.className || 'btn btn-default';
                                customBtn.textContent = customAction.text || '';
                                if (customAction.style) {
                                    Object.assign(customBtn.style, customAction.style);
                                }
                                if (customAction.onClick) {
                                    customBtn.onclick = () => customAction.onClick(item);
                                }
                                actionButtons.push(customBtn);
                            }
                        });
                    }
                    
                    // 将按钮添加到操作单元格
                    actionButtons.forEach(btn => {
                        actionCell.appendChild(btn);
                    });
                    
                    row.appendChild(actionCell);
                }
                
                tbody.appendChild(row);
            });
        }
        
        table.appendChild(tbody);
        return table;
    },
    
    /**
     * 创建表单
     * @param {Array} fields 字段配置 [{name, label, type, required, options, placeholder, value, disabled, readonly}]
     * @param {Object} values 表单初始值
     * @param {Function} submitHandler 提交处理函数
     * @returns {HTMLElement} 表单元素
     */
    createForm(fields, values = {}, submitHandler = null) {
        const form = document.createElement('form');
        
        // 阻止表单原生提交行为
        form.onsubmit = e => {
            e.preventDefault();
            
            // 如果有提交处理函数，收集表单数据并调用
            if (submitHandler) {
                const formData = {};
                
                // 收集所有字段的值
                fields.forEach(field => {
                    const input = form.querySelector(`[name="${field.name}"]`);
                    if (input) {
                        if (field.type === 'checkbox') {
                            formData[field.name] = input.checked;
                        } else if (field.type === 'number') {
                            formData[field.name] = input.value !== '' ? Number(input.value) : null;
                        } else {
                            formData[field.name] = input.value;
                        }
                    }
                });
                
                // 调用处理函数
                submitHandler(formData);
            }
        };
        
        // 添加表单字段
        fields.forEach(field => {
            const formGroup = document.createElement('div');
            formGroup.className = 'form-group';
            
            // 添加标签
            if (field.label) {
                const label = document.createElement('label');
                label.htmlFor = field.name;
                label.textContent = field.label;
                if (field.required) {
                    const requiredMark = document.createElement('span');
                    requiredMark.textContent = ' *';
                    requiredMark.style.color = 'red';
                    label.appendChild(requiredMark);
                }
                formGroup.appendChild(label);
            }
            
            let input;
            
            // 根据字段类型创建输入控件
            switch (field.type) {
                case 'textarea':
                    input = document.createElement('textarea');
                    input.className = 'form-control';
                    input.name = field.name;
                    input.placeholder = field.placeholder || '';
                    input.value = values[field.name] !== undefined ? values[field.name] : (field.value || '');
                    input.required = field.required || false;
                    input.disabled = field.disabled || false;
                    input.readOnly = field.readonly || false;
                    if (field.rows) {
                        input.rows = field.rows;
                    }
                    break;
                    
                case 'select':
                    input = document.createElement('select');
                    input.className = 'form-select';
                    input.name = field.name;
                    input.required = field.required || false;
                    input.disabled = field.disabled || false;
                    
                    // 添加选项
                    if (field.options) {
                        field.options.forEach(option => {
                            const optionEl = document.createElement('option');
                            if (typeof option === 'object') {
                                optionEl.value = option.value;
                                optionEl.textContent = option.label;
                            } else {
                                optionEl.value = option;
                                optionEl.textContent = option;
                            }
                            input.appendChild(optionEl);
                        });
                    }
                    
                    // 设置选中值
                    input.value = values[field.name] !== undefined ? values[field.name] : (field.value || '');
                    break;
                    
                case 'checkbox':
                    const checkboxWrapper = document.createElement('div');
                    checkboxWrapper.style.display = 'flex';
                    checkboxWrapper.style.alignItems = 'center';
                    checkboxWrapper.style.marginTop = '8px';
                    
                    input = document.createElement('input');
                    input.type = 'checkbox';
                    input.name = field.name;
                    input.id = field.name;
                    input.checked = values[field.name] !== undefined ? values[field.name] : (field.value || false);
                    input.disabled = field.disabled || false;
                    
                    const checkboxLabel = document.createElement('label');
                    checkboxLabel.htmlFor = field.name;
                    checkboxLabel.textContent = field.checkboxLabel || '';
                    checkboxLabel.style.marginLeft = '8px';
                    checkboxLabel.style.marginBottom = '0';
                    
                    checkboxWrapper.appendChild(input);
                    checkboxWrapper.appendChild(checkboxLabel);
                    formGroup.appendChild(checkboxWrapper);
                    break;
                    
                case 'radio':
                    const radioWrapper = document.createElement('div');
                    
                    if (field.options) {
                        field.options.forEach((option, index) => {
                            const radioDiv = document.createElement('div');
                            radioDiv.style.display = 'flex';
                            radioDiv.style.alignItems = 'center';
                            radioDiv.style.marginTop = index === 0 ? '8px' : '4px';
                            
                            const radioInput = document.createElement('input');
                            radioInput.type = 'radio';
                            radioInput.name = field.name;
                            
                            if (typeof option === 'object') {
                                radioInput.value = option.value;
                                radioInput.id = `${field.name}_${option.value}`;
                                const radioLabel = document.createElement('label');
                                radioLabel.htmlFor = `${field.name}_${option.value}`;
                                radioLabel.textContent = option.label;
                                radioLabel.style.marginLeft = '8px';
                                radioLabel.style.marginBottom = '0';
                                
                                radioDiv.appendChild(radioInput);
                                radioDiv.appendChild(radioLabel);
                            } else {
                                radioInput.value = option;
                                radioInput.id = `${field.name}_${option}`;
                                const radioLabel = document.createElement('label');
                                radioLabel.htmlFor = `${field.name}_${option}`;
                                radioLabel.textContent = option;
                                radioLabel.style.marginLeft = '8px';
                                radioLabel.style.marginBottom = '0';
                                
                                radioDiv.appendChild(radioInput);
                                radioDiv.appendChild(radioLabel);
                            }
                            
                            // 设置选中值
                            const currentValue = values[field.name] !== undefined ? values[field.name] : (field.value || '');
                            if (typeof option === 'object') {
                                radioInput.checked = currentValue === option.value;
                            } else {
                                radioInput.checked = currentValue === option;
                            }
                            
                            radioInput.disabled = field.disabled || false;
                            
                            radioWrapper.appendChild(radioDiv);
                        });
                    }
                    
                    formGroup.appendChild(radioWrapper);
                    break;
                    
                default:
                    input = document.createElement('input');
                    input.className = 'form-control';
                    input.type = field.type || 'text';
                    input.name = field.name;
                    input.placeholder = field.placeholder || '';
                    input.value = values[field.name] !== undefined ? values[field.name] : (field.value || '');
                    input.required = field.required || false;
                    input.disabled = field.disabled || false;
                    input.readOnly = field.readonly || false;
                    
                    if (field.min !== undefined) input.min = field.min;
                    if (field.max !== undefined) input.max = field.max;
                    break;
            }
            
            // 如果不是复选框或单选按钮，添加到表单组
            if (field.type !== 'checkbox' && field.type !== 'radio') {
                formGroup.appendChild(input);
            }
            
            // 如果有帮助文本，添加
            if (field.help) {
                const helpText = document.createElement('div');
                helpText.className = 'help-text';
                helpText.textContent = field.help;
                helpText.style.fontSize = '0.85rem';
                helpText.style.color = '#666';
                helpText.style.marginTop = '4px';
                formGroup.appendChild(helpText);
            }
            
            form.appendChild(formGroup);
        });
        
        // 添加提交按钮
        if (submitHandler) {
            const formGroup = document.createElement('div');
            formGroup.className = 'form-group';
            formGroup.style.marginTop = '1.5rem';
            
            const submitBtn = document.createElement('button');
            submitBtn.type = 'submit';
            submitBtn.className = 'btn btn-primary';
            submitBtn.textContent = '提交';
            
            formGroup.appendChild(submitBtn);
            form.appendChild(formGroup);
        }
        
        return form;
    },
    
    /**
     * 创建对话框
     * @param {string} title 对话框标题
     * @param {string|HTMLElement} content 对话框内容
     * @param {string} width 对话框宽度
     * @returns {Object} 对话框对象
     */
    createDialog(title, content, width = '500px') {
        // 创建蒙层
        const overlay = document.createElement('div');
        overlay.style.position = 'fixed';
        overlay.style.top = '0';
        overlay.style.left = '0';
        overlay.style.width = '100%';
        overlay.style.height = '100%';
        overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        overlay.style.display = 'flex';
        overlay.style.justifyContent = 'center';
        overlay.style.alignItems = 'center';
        overlay.style.zIndex = '9999';
        
        // 创建对话框
        const dialog = document.createElement('div');
        dialog.style.backgroundColor = 'white';
        dialog.style.borderRadius = '4px';
        dialog.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
        dialog.style.width = width;
        dialog.style.maxWidth = '95%';
        dialog.style.maxHeight = '90vh';
        dialog.style.display = 'flex';
        dialog.style.flexDirection = 'column';
        
        // 创建对话框头部
        const header = document.createElement('div');
        header.style.padding = '16px';
        header.style.borderBottom = '1px solid #e8e8e8';
        header.style.display = 'flex';
        header.style.justifyContent = 'space-between';
        header.style.alignItems = 'center';
        
        // 添加标题
        const titleEl = document.createElement('h3');
        titleEl.style.margin = '0';
        titleEl.style.fontSize = '16px';
        titleEl.style.fontWeight = '500';
        titleEl.textContent = title;
        header.appendChild(titleEl);
        
        // 添加关闭按钮
        const closeBtn = document.createElement('button');
        closeBtn.style.background = 'none';
        closeBtn.style.border = 'none';
        closeBtn.style.cursor = 'pointer';
        closeBtn.style.fontSize = '20px';
        closeBtn.style.color = '#999';
        closeBtn.innerHTML = '&times;';
        closeBtn.onclick = () => {
            document.body.removeChild(overlay);
        };
        header.appendChild(closeBtn);
        
        dialog.appendChild(header);
        
        // 创建对话框内容
        const body = document.createElement('div');
        body.style.padding = '16px';
        body.style.overflowY = 'auto';
        
        // 添加内容
        if (typeof content === 'string') {
            body.innerHTML = content;
        } else {
            body.appendChild(content);
        }
        
        dialog.appendChild(body);
        
        // 将对话框添加到蒙层
        overlay.appendChild(dialog);
        
        // 添加到页面
        document.body.appendChild(overlay);
        
        // 返回对话框对象
        return {
            close() {
                document.body.removeChild(overlay);
            },
            setTitle(newTitle) {
                titleEl.textContent = newTitle;
            },
            setContent(newContent) {
                body.innerHTML = '';
                if (typeof newContent === 'string') {
                    body.innerHTML = newContent;
                } else {
                    body.appendChild(newContent);
                }
            }
        };
    }
};

/**
 * 用户认证与权限组件
 * 提供用户认证状态管理和权限验证功能
 */
UI.auth = {
    /**
     * 初始化认证区域
     * @param {string} containerId 容器ID
     * @param {Function} onLogin 登录成功回调
     * @param {Function} onLogout 登出成功回调
     */
    initAuthArea: function(containerId, onLogin, onLogout) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        if (API.isLoggedIn()) {
            this.renderUserInfo(container);
            if (typeof onLogin === 'function') {
                onLogin();
            }
        } else {
            this.renderLoginForm(container, onLogin);
        }
        
        // 绑定退出登录事件
        const logoutBtn = document.getElementById('ui-auth-logout');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                API.logout();
                UI.showMessage('已安全退出登录', 'info');
                
                // 重新渲染登录表单
                this.renderLoginForm(container, onLogin);
                
                if (typeof onLogout === 'function') {
                    onLogout();
                }
            });
        }
    },
    
    /**
     * 渲染用户信息
     * @param {HTMLElement} container 容器元素
     */
    renderUserInfo: function(container) {
        const username = API.getUsername();
        const role = API.getUserRole();
        
        let roleText = '学生';
        if (role === 'TEACHER') {
            roleText = '教师';
        } else if (role === 'ADMIN') {
            roleText = '管理员';
        }
        
        container.innerHTML = `
            <div class="auth-info">
                <div class="user-avatar">
                    <img src="https://via.placeholder.com/50" alt="用户头像">
                </div>
                <div class="user-details">
                    <h3>${username}</h3>
                    <p>角色: ${roleText}</p>
                </div>
                <div class="auth-actions">
                    <button id="ui-auth-space" class="btn btn-sm btn-primary">进入空间</button>
                    <button id="ui-auth-logout" class="btn btn-sm btn-default">退出登录</button>
                </div>
            </div>
        `;
        
        // 绑定进入空间事件
        const spaceBtn = document.getElementById('ui-auth-space');
        if (spaceBtn) {
            spaceBtn.addEventListener('click', () => {
                window.location.href = API.getUserSpaceUrl();
            });
        }
    },
    
    /**
     * 渲染登录表单
     * @param {HTMLElement} container 容器元素
     * @param {Function} onLogin 登录成功回调
     */
    renderLoginForm: function(container, onLogin) {
        container.innerHTML = `
            <div class="auth-form">
                <h3>用户登录</h3>
                <div class="form-group">
                    <label for="ui-auth-username">用户名</label>
                    <input type="text" id="ui-auth-username" class="form-control" placeholder="请输入用户名/学号/工号">
                </div>
                <div class="form-group">
                    <label for="ui-auth-password">密码</label>
                    <input type="password" id="ui-auth-password" class="form-control" placeholder="请输入密码">
                </div>
                <div class="form-actions">
                    <button id="ui-auth-login" class="btn btn-primary">登录</button>
                </div>
            </div>
        `;
        
        // 绑定登录事件
        const loginBtn = document.getElementById('ui-auth-login');
        if (loginBtn) {
            loginBtn.addEventListener('click', async () => {
                const username = document.getElementById('ui-auth-username').value;
                const password = document.getElementById('ui-auth-password').value;
                
                if (!username || !password) {
                    UI.showMessage('请输入用户名和密码', 'warning');
                    return;
                }
                
                UI.showLoading();
                
                try {
                    // 调用API登录方法，API层会处理重定向
                    const result = await API.login(username, password);
                    
                    if (result && result.success) {
                        UI.showMessage('登录成功', 'success');
                        
                        // 更新登录区域显示
                        this.renderUserInfo(container);
                        
                        // 执行登录成功回调
                        if (typeof onLogin === 'function') {
                            onLogin();
                        }
                    } else {
                        UI.showMessage('登录失败：' + (result.message || '用户名或密码错误'), 'error');
                    }
                } catch (error) {
                    console.error('登录错误:', error);
                    UI.showMessage('登录失败：' + (error.message || '服务器错误，请稍后再试'), 'error');
                } finally {
                    UI.hideLoading();
                }
            });
            
            // 密码输入框回车事件
            const passwordInput = document.getElementById('ui-auth-password');
            if (passwordInput) {
                passwordInput.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') {
                        loginBtn.click();
                    }
                });
            }
        }
    },
    
    /**
     * 检查权限并处理无权限情况
     * @param {string} requiredRole 所需角色
     * @param {Function} onGranted 有权限时的回调
     * @param {Function} onDenied 无权限时的回调
     */
    checkPermission: function(requiredRole, onGranted, onDenied) {
        if (API.hasPermission(requiredRole)) {
            if (typeof onGranted === 'function') {
                onGranted();
            }
            return true;
        } else {
            if (typeof onDenied === 'function') {
                onDenied();
            } else {
                // 默认处理
                if (!API.isLoggedIn()) {
                    UI.showMessage('请先登录', 'warning');
                    setTimeout(() => {
                        window.location.href = '/campus/index.html';
                    }, 1500);
                } else {
                    UI.showMessage('您没有权限执行此操作', 'error');
                }
            }
            return false;
        }
    },
    
    /**
     * 权限按钮
     * @param {HTMLElement} button 按钮元素
     * @param {string} requiredRole 所需角色
     * @param {Function} onClick 点击回调
     */
    permissionButton: function(button, requiredRole, onClick) {
        if (!button) return;
        
        // 检查权限
        if (!API.hasPermission(requiredRole)) {
            button.disabled = true;
            button.classList.add('disabled');
            button.title = '无权限执行此操作';
            return;
        }
        
        // 绑定点击事件
        button.addEventListener('click', (e) => {
            e.preventDefault();
            if (typeof onClick === 'function') {
                onClick(e);
            }
        });
    }
};

/**
 * 课程模块UI组件
 */
UI.course = {
    /**
     * 初始化课程列表
     * @param {string} containerId 容器ID
     * @param {boolean} showActions 是否显示操作按钮
     * @param {Function} callback 回调函数
     */
    initCourseList(containerId, showActions = true, callback = null) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 确定API方法
        let apiMethod = API.isLoggedIn() ? 
            (API.getUserRole() === 'STUDENT' ? API.course.getMyCourses : 
             API.getUserRole() === 'TEACHER' ? API.course.getMyTeachingCourses : 
             API.course.getAll) : 
            API.course.getAll;
        
        // 获取课程数据
        apiMethod()
            .then(courses => {
                // 定义表格列
                const columns = [
                    { title: '课程名称', field: 'name' },
                    { title: '课程代码', field: 'code' },
                    { title: '学分', field: 'credit' },
                    { title: '教师', field: 'teacherName' },
                    { title: '开课时间', field: 'startDate' },
                    { title: '状态', field: 'status', render: (status) => {
                        return status === 1 ? 
                            '<span class="status-tag status-active">进行中</span>' : 
                            '<span class="status-tag status-inactive">已结束</span>';
                    }}
                ];
                
                // 定义操作按钮
                let actions = null;
                if (showActions) {
                    if (API.getUserRole() === 'ADMIN' || API.getUserRole() === 'TEACHER') {
                        actions = [
                            { text: '查看', type: 'primary', handler: (course) => this.viewCourseDetail(course.id) },
                            { text: '编辑', type: 'default', handler: (course) => this.editCourse(course) },
                            { text: '删除', type: 'danger', handler: (course) => this.deleteCourse(course.id) }
                        ];
                    } else if (API.getUserRole() === 'STUDENT') {
                        actions = [
                            { text: '查看', type: 'primary', handler: (course) => this.viewCourseDetail(course.id) },
                            { text: '退选', type: 'danger', handler: (course) => this.unselectCourse(course.id) }
                        ];
                    } else {
                        actions = [
                            { text: '查看', type: 'primary', handler: (course) => this.viewCourseDetail(course.id) }
                        ];
                    }
                }
                
                // 创建表格
                const table = UI.createTable(columns, courses, actions);
                container.innerHTML = '';
                container.appendChild(table);
                
                if (callback) callback(courses);
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载课程失败: ${error.message}</div>`;
                console.error('加载课程失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 查看课程详情
     * @param {number} courseId 课程ID
     */
    viewCourseDetail(courseId) {
        UI.showLoading();
        
        API.course.getById(courseId)
            .then(course => {
                const dialog = UI.createDialog('课程详情', '', '700px');
                
                let contentHtml = `
                    <div class="course-detail">
                        <div class="course-header">
                            <h2>${course.name}</h2>
                            <span class="course-code">${course.code}</span>
                        </div>
                        <div class="course-info">
                            <div class="info-item">
                                <span class="label">教师:</span>
                                <span class="value">${course.teacherName}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">学分:</span>
                                <span class="value">${course.credit}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">开课时间:</span>
                                <span class="value">${course.startDate}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">结束时间:</span>
                                <span class="value">${course.endDate || '待定'}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">状态:</span>
                                <span class="value">${course.status === 1 ? '进行中' : '已结束'}</span>
                            </div>
                        </div>
                        <div class="course-description">
                            <h3>课程描述</h3>
                            <p>${course.description || '暂无课程描述'}</p>
                        </div>
                    </div>
                `;
                
                // 如果是教师或管理员，显示学生列表
                if (API.getUserRole() === 'ADMIN' || API.getUserRole() === 'TEACHER') {
                    contentHtml += `<div class="student-list-container">
                        <h3>选课学生</h3>
                        <div id="student-list" class="loading">加载中...</div>
                    </div>`;
                }
                
                dialog.setContent(contentHtml);
                
                // 加载学生列表
                if (API.getUserRole() === 'ADMIN' || API.getUserRole() === 'TEACHER') {
                    API.course.getStudents(courseId)
                        .then(students => {
                            const studentList = document.getElementById('student-list');
                            if (!studentList) return;
                            
                            if (students.length === 0) {
                                studentList.innerHTML = '暂无学生选修此课程';
                                studentList.classList.remove('loading');
                                return;
                            }
                            
                            // 创建学生列表表格
                            const columns = [
                                { title: '学号', field: 'username' },
                                { title: '姓名', field: 'realName' },
                                { title: '性别', field: 'gender' },
                                { title: '班级', field: 'className' }
                            ];
                            
                            const table = UI.createTable(columns, students);
                            studentList.innerHTML = '';
                            studentList.appendChild(table);
                            studentList.classList.remove('loading');
                        })
                        .catch(error => {
                            const studentList = document.getElementById('student-list');
                            if (studentList) {
                                studentList.innerHTML = `加载学生列表失败: ${error.message}`;
                                studentList.classList.remove('loading');
                            }
                            console.error('加载学生列表失败:', error);
                        });
                }
                
                dialog.show();
            })
            .catch(error => {
                UI.showMessage(`加载课程详情失败: ${error.message}`, 'error');
                console.error('加载课程详情失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 编辑课程
     * @param {Object} course 课程对象
     */
    editCourse(course) {
        const isAdd = !course || !course.id;
        const title = isAdd ? '添加课程' : '编辑课程';
        
        // 定义表单字段
        const fields = [
            { name: 'name', label: '课程名称', type: 'text', required: true, value: course?.name || '' },
            { name: 'code', label: '课程代码', type: 'text', required: true, value: course?.code || '' },
            { name: 'credit', label: '学分', type: 'number', required: true, value: course?.credit || 0 },
            { name: 'teacherId', label: '教师', type: 'select', required: true, value: course?.teacherId || 0, options: [] },
            { name: 'startDate', label: '开课时间', type: 'date', required: true, value: course?.startDate || '' },
            { name: 'endDate', label: '结束时间', type: 'date', required: false, value: course?.endDate || '' },
            { name: 'description', label: '课程描述', type: 'textarea', required: false, value: course?.description || '' },
            { name: 'status', label: '状态', type: 'select', required: true, value: course?.status || 1, options: [
                { value: 1, label: '进行中' },
                { value: 0, label: '已结束' }
            ]}
        ];
        
        UI.showLoading();
        
        // 获取教师列表
        API.request('/users?userType=2')
            .then(teachers => {
                // 更新教师下拉框选项
                const teacherField = fields.find(f => f.name === 'teacherId');
                if (teacherField) {
                    teacherField.options = teachers.map(t => ({
                        value: t.id,
                        label: t.realName || t.username
                    }));
                }
                
                // 创建表单对话框
                const dialog = UI.createDialog(title, '', '700px');
                const form = UI.createForm(fields, {}, (formData) => {
                    UI.showLoading();
                    
                    // 保存课程
                    const saveMethod = isAdd ? 
                        API.course.add(formData) : 
                        API.course.update(course.id, formData);
                    
                    saveMethod
                        .then(() => {
                            dialog.close();
                            UI.showMessage(`课程${isAdd ? '添加' : '更新'}成功`, 'success');
                            // 刷新课程列表
                            this.initCourseList('course-list');
                        })
                        .catch(error => {
                            UI.showMessage(`课程${isAdd ? '添加' : '更新'}失败: ${error.message}`, 'error');
                            console.error(`课程${isAdd ? '添加' : '更新'}失败:`, error);
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                });
                
                dialog.setContent(form);
                dialog.show();
            })
            .catch(error => {
                UI.showMessage(`加载教师列表失败: ${error.message}`, 'error');
                console.error('加载教师列表失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 删除课程
     * @param {number} courseId 课程ID
     */
    deleteCourse(courseId) {
        if (confirm('确定要删除这个课程吗？删除后无法恢复。')) {
            UI.showLoading();
            
            API.course.delete(courseId)
                .then(() => {
                    UI.showMessage('课程删除成功', 'success');
                    // 刷新课程列表
                    this.initCourseList('course-list');
                })
                .catch(error => {
                    UI.showMessage(`课程删除失败: ${error.message}`, 'error');
                    console.error('课程删除失败:', error);
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    },
    
    /**
     * 学生退选课程
     * @param {number} courseId 课程ID
     */
    unselectCourse(courseId) {
        if (confirm('确定要退选这门课程吗？')) {
            UI.showLoading();
            
            API.course.unselectCourse(courseId)
                .then(() => {
                    UI.showMessage('课程退选成功', 'success');
                    // 刷新课程列表
                    this.initCourseList('course-list');
                })
                .catch(error => {
                    UI.showMessage(`课程退选失败: ${error.message}`, 'error');
                    console.error('课程退选失败:', error);
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    },
    
    /**
     * 初始化选课页面
     * @param {string} containerId 容器ID
     */
    initCourseSelection(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 获取所有课程
        API.course.getAll()
            .then(allCourses => {
                // 获取已选课程
                return API.course.getMyCourses()
                    .then(myCourses => {
                        // 过滤掉已选的课程
                        const selectedIds = myCourses.map(c => c.id);
                        const availableCourses = allCourses.filter(c => !selectedIds.includes(c.id));
                        
                        // 定义表格列
                        const columns = [
                            { title: '课程名称', field: 'name' },
                            { title: '课程代码', field: 'code' },
                            { title: '学分', field: 'credit' },
                            { title: '教师', field: 'teacherName' },
                            { title: '开课时间', field: 'startDate' }
                        ];
                        
                        // 定义操作按钮
                        const actions = [
                            { text: '选修', type: 'primary', handler: (course) => this.selectCourse(course.id) }
                        ];
                        
                        // 创建表格
                        const table = UI.createTable(columns, availableCourses, actions);
                        
                        // 创建页面内容
                        container.innerHTML = `
                            <div class="course-selection">
                                <h2>可选课程</h2>
                                <div class="course-list-container"></div>
                                
                                <h2 style="margin-top: 2rem;">已选课程</h2>
                                <div class="my-course-list-container"></div>
                            </div>
                        `;
                        
                        // 添加表格到容器
                        const courseListContainer = container.querySelector('.course-list-container');
                        courseListContainer.appendChild(table);
                        
                        // 初始化已选课程列表
                        const myColumns = [
                            { title: '课程名称', field: 'name' },
                            { title: '课程代码', field: 'code' },
                            { title: '学分', field: 'credit' },
                            { title: '教师', field: 'teacherName' },
                            { title: '开课时间', field: 'startDate' }
                        ];
                        
                        const myActions = [
                            { text: '退选', type: 'danger', handler: (course) => this.unselectCourse(course.id) }
                        ];
                        
                        const myTable = UI.createTable(myColumns, myCourses, myActions);
                        const myCourseListContainer = container.querySelector('.my-course-list-container');
                        myCourseListContainer.appendChild(myTable);
                    });
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载课程失败: ${error.message}</div>`;
                console.error('加载课程失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 学生选修课程
     * @param {number} courseId 课程ID
     */
    selectCourse(courseId) {
        UI.showLoading();
        
        API.course.selectCourse(courseId)
            .then(() => {
                UI.showMessage('选课成功', 'success');
                // 刷新选课页面
                this.initCourseSelection('course-selection-container');
            })
            .catch(error => {
                UI.showMessage(`选课失败: ${error.message}`, 'error');
                console.error('选课失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    }
};

/**
 * 活动模块UI组件
 */
UI.activity = {
    /**
     * 初始化活动列表
     * @param {string} containerId 容器ID
     * @param {boolean} showActions 是否显示操作按钮
     * @param {Function} callback 回调函数
     */
    initActivityList(containerId, showActions = true, callback = null) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 获取活动数据
        API.activity.getAll()
            .then(activities => {
                // 定义表格列
                const columns = [
                    { title: '活动名称', field: 'name' },
                    { title: '开始时间', field: 'startTime' },
                    { title: '结束时间', field: 'endTime' },
                    { title: '地点', field: 'location' },
                    { title: '组织者', field: 'organizer' },
                    { title: '状态', field: 'status', render: (status) => {
                        if (status === 0) return '<span class="status-tag status-inactive">已结束</span>';
                        if (status === 1) return '<span class="status-tag status-active">进行中</span>';
                        if (status === 2) return '<span class="status-tag status-pending">未开始</span>';
                        return '<span class="status-tag status-default">未知</span>';
                    }}
                ];
                
                // 定义操作按钮
                let actions = null;
                if (showActions) {
                    if (API.getUserRole() === 'ADMIN' || API.getUserRole() === 'TEACHER') {
                        actions = [
                            { text: '查看', type: 'primary', handler: (activity) => this.viewActivityDetail(activity.id) },
                            { text: '编辑', type: 'default', handler: (activity) => this.editActivity(activity) },
                            { text: '删除', type: 'danger', handler: (activity) => this.deleteActivity(activity.id) }
                        ];
                    } else {
                        actions = [
                            { text: '查看', type: 'primary', handler: (activity) => this.viewActivityDetail(activity.id) },
                            { text: '参与', type: 'success', handler: (activity) => this.joinActivity(activity.id), condition: (activity) => activity.status !== 0 && !activity.isJoined },
                            { text: '取消参与', type: 'danger', handler: (activity) => this.leaveActivity(activity.id), condition: (activity) => activity.status !== 0 && activity.isJoined }
                        ];
                    }
                }
                
                // 创建表格
                const table = UI.createTable(columns, activities, actions);
                container.innerHTML = '';
                container.appendChild(table);
                
                if (callback) callback(activities);
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载活动失败: ${error.message}</div>`;
                console.error('加载活动失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 查看活动详情
     * @param {number} activityId 活动ID
     */
    viewActivityDetail(activityId) {
        UI.showLoading();
        
        API.activity.getById(activityId)
            .then(activity => {
                const dialog = UI.createDialog('活动详情', '', '700px');
                
                let contentHtml = `
                    <div class="activity-detail">
                        <div class="activity-header">
                            <h2>${activity.name}</h2>
                            <span class="activity-status status-tag status-${
                                activity.status === 0 ? 'inactive' : 
                                activity.status === 1 ? 'active' : 
                                activity.status === 2 ? 'pending' : 'default'
                            }">${
                                activity.status === 0 ? '已结束' : 
                                activity.status === 1 ? '进行中' : 
                                activity.status === 2 ? '未开始' : '未知'
                            }</span>
                        </div>
                        <div class="activity-info">
                            <div class="info-item">
                                <span class="label">开始时间:</span>
                                <span class="value">${activity.startTime}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">结束时间:</span>
                                <span class="value">${activity.endTime}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">地点:</span>
                                <span class="value">${activity.location}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">组织者:</span>
                                <span class="value">${activity.organizer}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">联系方式:</span>
                                <span class="value">${activity.contact || '无'}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">最大参与人数:</span>
                                <span class="value">${activity.maxParticipants || '不限'}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">当前参与人数:</span>
                                <span class="value">${activity.currentParticipants || 0}</span>
                            </div>
                        </div>
                        <div class="activity-description">
                            <h3>活动描述</h3>
                            <p>${activity.description || '暂无活动描述'}</p>
                        </div>
                    </div>
                `;
                
                // 显示参与者列表
                contentHtml += `<div class="participants-list-container">
                    <h3>参与者</h3>
                    <div id="participants-list" class="loading">加载中...</div>
                </div>`;
                
                // 添加参与/取消参与按钮
                if (API.isLoggedIn() && API.getUserRole() === 'STUDENT' && activity.status !== 0) {
                    contentHtml += `<div class="activity-actions">
                        ${!activity.isJoined ? 
                            `<button id="join-activity-btn" class="btn btn-primary">参与活动</button>` :
                            `<button id="leave-activity-btn" class="btn btn-danger">取消参与</button>`
                        }
                    </div>`;
                }
                
                dialog.setContent(contentHtml);
                
                // 绑定按钮事件
                if (API.isLoggedIn() && API.getUserRole() === 'STUDENT' && activity.status !== 0) {
                    if (!activity.isJoined) {
                        const joinBtn = document.getElementById('join-activity-btn');
                        if (joinBtn) {
                            joinBtn.addEventListener('click', () => {
                                this.joinActivity(activityId);
                                dialog.close();
                            });
                        }
                    } else {
                        const leaveBtn = document.getElementById('leave-activity-btn');
                        if (leaveBtn) {
                            leaveBtn.addEventListener('click', () => {
                                this.leaveActivity(activityId);
                                dialog.close();
                            });
                        }
                    }
                }
                
                // 加载参与者列表
                API.activity.getParticipants(activityId)
                    .then(participants => {
                        const participantsList = document.getElementById('participants-list');
                        if (!participantsList) return;
                        
                        if (participants.length === 0) {
                            participantsList.innerHTML = '暂无人参与此活动';
                            participantsList.classList.remove('loading');
                            return;
                        }
                        
                        // 创建参与者列表表格
                        const columns = [
                            { title: '姓名', field: 'realName' },
                            { title: '用户名', field: 'username' },
                            { title: '参与时间', field: 'joinTime' }
                        ];
                        
                        const table = UI.createTable(columns, participants);
                        participantsList.innerHTML = '';
                        participantsList.appendChild(table);
                        participantsList.classList.remove('loading');
                    })
                    .catch(error => {
                        const participantsList = document.getElementById('participants-list');
                        if (participantsList) {
                            participantsList.innerHTML = `加载参与者列表失败: ${error.message}`;
                            participantsList.classList.remove('loading');
                        }
                        console.error('加载参与者列表失败:', error);
                    });
                
                dialog.show();
            })
            .catch(error => {
                UI.showMessage(`加载活动详情失败: ${error.message}`, 'error');
                console.error('加载活动详情失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 编辑活动
     * @param {Object} activity 活动对象
     */
    editActivity(activity) {
        const isAdd = !activity || !activity.id;
        const title = isAdd ? '添加活动' : '编辑活动';
        
        // 定义表单字段
        const fields = [
            { name: 'name', label: '活动名称', type: 'text', required: true, value: activity?.name || '' },
            { name: 'startTime', label: '开始时间', type: 'datetime-local', required: true, value: activity?.startTime || '' },
            { name: 'endTime', label: '结束时间', type: 'datetime-local', required: true, value: activity?.endTime || '' },
            { name: 'location', label: '活动地点', type: 'text', required: true, value: activity?.location || '' },
            { name: 'organizer', label: '组织者', type: 'text', required: true, value: activity?.organizer || '' },
            { name: 'contact', label: '联系方式', type: 'text', required: false, value: activity?.contact || '' },
            { name: 'maxParticipants', label: '最大参与人数', type: 'number', required: false, value: activity?.maxParticipants || 0 },
            { name: 'description', label: '活动描述', type: 'textarea', required: false, value: activity?.description || '' },
            { name: 'status', label: '状态', type: 'select', required: true, value: activity?.status || 2, options: [
                { value: 0, label: '已结束' },
                { value: 1, label: '进行中' },
                { value: 2, label: '未开始' }
            ]}
        ];
        
        // 创建表单对话框
        const dialog = UI.createDialog(title, '', '700px');
        const form = UI.createForm(fields, {}, (formData) => {
            UI.showLoading();
            
            // 保存活动
            const saveMethod = isAdd ? 
                API.activity.add(formData) : 
                API.activity.update(activity.id, formData);
            
            saveMethod
                .then(() => {
                    dialog.close();
                    UI.showMessage(`活动${isAdd ? '添加' : '更新'}成功`, 'success');
                    // 刷新活动列表
                    this.initActivityList('activity-list');
                })
                .catch(error => {
                    UI.showMessage(`活动${isAdd ? '添加' : '更新'}失败: ${error.message}`, 'error');
                    console.error(`活动${isAdd ? '添加' : '更新'}失败:`, error);
                })
                .finally(() => {
                    UI.hideLoading();
                });
        });
        
        dialog.setContent(form);
        dialog.show();
    },
    
    /**
     * 删除活动
     * @param {number} activityId 活动ID
     */
    deleteActivity(activityId) {
        if (confirm('确定要删除这个活动吗？删除后无法恢复。')) {
            UI.showLoading();
            
            API.activity.delete(activityId)
                .then(() => {
                    UI.showMessage('活动删除成功', 'success');
                    // 刷新活动列表
                    this.initActivityList('activity-list');
                })
                .catch(error => {
                    UI.showMessage(`活动删除失败: ${error.message}`, 'error');
                    console.error('活动删除失败:', error);
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    },
    
    /**
     * 参与活动
     * @param {number} activityId 活动ID
     */
    joinActivity(activityId) {
        UI.showLoading();
        
        API.activity.join(activityId)
            .then(() => {
                UI.showMessage('成功参与活动', 'success');
                // 刷新活动列表
                this.initActivityList('activity-list');
            })
            .catch(error => {
                UI.showMessage(`参与活动失败: ${error.message}`, 'error');
                console.error('参与活动失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 取消参与活动
     * @param {number} activityId 活动ID
     */
    leaveActivity(activityId) {
        if (confirm('确定要取消参与这个活动吗？')) {
            UI.showLoading();
            
            API.activity.leave(activityId)
                .then(() => {
                    UI.showMessage('已取消参与活动', 'success');
                    // 刷新活动列表
                    this.initActivityList('activity-list');
                })
                .catch(error => {
                    UI.showMessage(`取消参与失败: ${error.message}`, 'error');
                    console.error('取消参与失败:', error);
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    },
    
    /**
     * 初始化我参与的活动
     * @param {string} containerId 容器ID
     */
    initMyActivities(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 获取我参与的活动
        API.activity.getMyActivities()
            .then(activities => {
                if (activities.length === 0) {
                    container.innerHTML = '<div class="empty-data">您还没有参与任何活动</div>';
                    return;
                }
                
                // 定义表格列
                const columns = [
                    { title: '活动名称', field: 'name' },
                    { title: '开始时间', field: 'startTime' },
                    { title: '结束时间', field: 'endTime' },
                    { title: '地点', field: 'location' },
                    { title: '状态', field: 'status', render: (status) => {
                        if (status === 0) return '<span class="status-tag status-inactive">已结束</span>';
                        if (status === 1) return '<span class="status-tag status-active">进行中</span>';
                        if (status === 2) return '<span class="status-tag status-pending">未开始</span>';
                        return '<span class="status-tag status-default">未知</span>';
                    }}
                ];
                
                // 定义操作按钮
                const actions = [
                    { text: '查看', type: 'primary', handler: (activity) => this.viewActivityDetail(activity.id) },
                    { text: '取消参与', type: 'danger', handler: (activity) => this.leaveActivity(activity.id), condition: (activity) => activity.status !== 0 }
                ];
                
                // 创建表格
                const table = UI.createTable(columns, activities, actions);
                container.innerHTML = '';
                container.appendChild(table);
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载活动失败: ${error.message}</div>`;
                console.error('加载活动失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    }
};

/**
 * 课表模块UI组件
 */
UI.schedule = {
    /**
     * 初始化课表
     * @param {string} containerId 容器ID
     * @param {string} mode 模式，可选值：day（当日）, week（本周）, all（所有）
     * @param {Object} options 选项
     */
    initSchedule(containerId, mode = 'week', options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 确定要使用的API方法
        let apiMethod;
        switch (mode) {
            case 'day':
                apiMethod = API.schedule.getTodayClasses;
                break;
            case 'week':
                apiMethod = API.schedule.getThisWeekClasses;
                break;
            case 'all':
            default:
                apiMethod = API.schedule.getMy;
                break;
        }
        
        // 获取课表数据
        apiMethod()
            .then(scheduleData => {
                if (mode === 'week') {
                    // 周课表视图
                    this.renderWeekSchedule(container, scheduleData, options);
                } else if (mode === 'day') {
                    // 日课表视图
                    this.renderDaySchedule(container, scheduleData, options);
                } else {
                    // 列表视图
                    this.renderScheduleList(container, scheduleData, options);
                }
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载课表失败: ${error.message}</div>`;
                console.error('加载课表失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 渲染周课表
     * @param {HTMLElement} container 容器元素
     * @param {Array} scheduleData 课表数据
     * @param {Object} options 选项
     */
    renderWeekSchedule(container, scheduleData, options = {}) {
        // 周一到周日
        const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
        // 时间段，可根据需要调整
        const timeSlots = [
            { id: 1, name: '第1节', time: '08:00-08:45' },
            { id: 2, name: '第2节', time: '08:55-09:40' },
            { id: 3, name: '第3节', time: '10:00-10:45' },
            { id: 4, name: '第4节', time: '10:55-11:40' },
            { id: 5, name: '第5节', time: '13:30-14:15' },
            { id: 6, name: '第6节', time: '14:25-15:10' },
            { id: 7, name: '第7节', time: '15:30-16:15' },
            { id: 8, name: '第8节', time: '16:25-17:10' },
            { id: 9, name: '第9节', time: '18:30-19:15' },
            { id: 10, name: '第10节', time: '19:25-20:10' }
        ];
        
        // 创建课表表格
        let tableHtml = `
            <div class="schedule-header">
                <h2>课程表</h2>
                <div class="schedule-actions">
                    <button class="btn btn-primary" id="today-schedule-btn">今日课程</button>
                    <button class="btn btn-default" id="week-schedule-btn">本周课程</button>
                    <button class="btn btn-default" id="all-schedule-btn">所有课程</button>
                </div>
            </div>
            <div class="schedule-container">
                <table class="schedule-table">
                    <thead>
                        <tr>
                            <th>时间</th>
                            ${weekdays.map(day => `<th>${day}</th>`).join('')}
                        </tr>
                    </thead>
                    <tbody>
        `;
        
        // 为每个时间段创建行
        timeSlots.forEach(slot => {
            tableHtml += `
                <tr>
                    <td class="time-slot">
                        <div class="slot-name">${slot.name}</div>
                        <div class="slot-time">${slot.time}</div>
                    </td>
            `;
            
            // 为每天创建单元格
            for (let day = 1; day <= 7; day++) {
                // 查找当前时间段和日期的课程
                const classes = scheduleData.filter(c => c.weekday === day && c.timeSlot === slot.id);
                
                if (classes.length > 0) {
                    const cls = classes[0]; // 目前假设一个时间段只有一门课
                    tableHtml += `
                        <td class="has-class" data-course-id="${cls.courseId}">
                            <div class="course-name">${cls.courseName}</div>
                            <div class="course-location">${cls.location || ''}</div>
                            <div class="course-teacher">${cls.teacherName || ''}</div>
                        </td>
                    `;
                } else {
                    tableHtml += `<td class="no-class"></td>`;
                }
            }
            
            tableHtml += `</tr>`;
        });
        
        tableHtml += `
                    </tbody>
                </table>
            </div>
        `;
        
        container.innerHTML = tableHtml;
        
        // 绑定按钮事件
        document.getElementById('today-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'day', options);
        });
        
        document.getElementById('week-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'week', options);
        });
        
        document.getElementById('all-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'all', options);
        });
        
        // 绑定课程点击事件
        const courseCells = container.querySelectorAll('.has-class');
        courseCells.forEach(cell => {
            cell.addEventListener('click', () => {
                const courseId = cell.getAttribute('data-course-id');
                if (courseId) {
                    UI.course.viewCourseDetail(parseInt(courseId));
                }
            });
        });
    },
    
    /**
     * 渲染日课表
     * @param {HTMLElement} container 容器元素
     * @param {Array} scheduleData 课表数据
     * @param {Object} options 选项
     */
    renderDaySchedule(container, scheduleData, options = {}) {
        // 今天是星期几
        const today = new Date().getDay() || 7; // 0代表周日，转为7
        
        // 如果没有今天的课程
        if (scheduleData.length === 0) {
            const tableHtml = `
                <div class="schedule-header">
                    <h2>今日课程</h2>
                    <div class="schedule-actions">
                        <button class="btn btn-default" id="today-schedule-btn">今日课程</button>
                        <button class="btn btn-primary" id="week-schedule-btn">本周课程</button>
                        <button class="btn btn-default" id="all-schedule-btn">所有课程</button>
                    </div>
                </div>
                <div class="schedule-container">
                    <div class="empty-schedule">
                        <p>今天没有课程安排 🎉</p>
                    </div>
                </div>
            `;
            
            container.innerHTML = tableHtml;
            
            // 绑定按钮事件
            document.getElementById('today-schedule-btn').addEventListener('click', () => {
                this.initSchedule(container.id, 'day', options);
            });
            
            document.getElementById('week-schedule-btn').addEventListener('click', () => {
                this.initSchedule(container.id, 'week', options);
            });
            
            document.getElementById('all-schedule-btn').addEventListener('click', () => {
                this.initSchedule(container.id, 'all', options);
            });
            
            return;
        }
        
        // 排序课程，按时间顺序
        scheduleData.sort((a, b) => a.timeSlot - b.timeSlot);
        
        // 创建今日课表
        let tableHtml = `
            <div class="schedule-header">
                <h2>今日课程</h2>
                <div class="schedule-actions">
                    <button class="btn btn-primary" id="today-schedule-btn">今日课程</button>
                    <button class="btn btn-default" id="week-schedule-btn">本周课程</button>
                    <button class="btn btn-default" id="all-schedule-btn">所有课程</button>
                </div>
            </div>
            <div class="schedule-container">
                <div class="day-schedule">
        `;
        
        // 添加每节课
        scheduleData.forEach(cls => {
            tableHtml += `
                <div class="day-class-item" data-course-id="${cls.courseId}">
                    <div class="class-time">${this.getTimeBySlot(cls.timeSlot)}</div>
                    <div class="class-content">
                        <div class="class-name">${cls.courseName}</div>
                        <div class="class-info">
                            <span class="class-location">${cls.location || '未指定地点'}</span>
                            <span class="class-teacher">${cls.teacherName || ''}</span>
                        </div>
                    </div>
                </div>
            `;
        });
        
        tableHtml += `
                </div>
            </div>
        `;
        
        container.innerHTML = tableHtml;
        
        // 绑定按钮事件
        document.getElementById('today-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'day', options);
        });
        
        document.getElementById('week-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'week', options);
        });
        
        document.getElementById('all-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'all', options);
        });
        
        // 绑定课程点击事件
        const courseItems = container.querySelectorAll('.day-class-item');
        courseItems.forEach(item => {
            item.addEventListener('click', () => {
                const courseId = item.getAttribute('data-course-id');
                if (courseId) {
                    UI.course.viewCourseDetail(parseInt(courseId));
                }
            });
        });
    },
    
    /**
     * 渲染课表列表
     * @param {HTMLElement} container 容器元素
     * @param {Array} scheduleData 课表数据
     * @param {Object} options 选项
     */
    renderScheduleList(container, scheduleData, options = {}) {
        // 按照课程分组
        const courseGroups = {};
        scheduleData.forEach(item => {
            if (!courseGroups[item.courseId]) {
                courseGroups[item.courseId] = {
                    courseId: item.courseId,
                    courseName: item.courseName,
                    teacherName: item.teacherName,
                    classes: []
                };
            }
            courseGroups[item.courseId].classes.push(item);
        });
        
        // 创建列表视图
        let html = `
            <div class="schedule-header">
                <h2>所有课程</h2>
                <div class="schedule-actions">
                    <button class="btn btn-default" id="today-schedule-btn">今日课程</button>
                    <button class="btn btn-default" id="week-schedule-btn">本周课程</button>
                    <button class="btn btn-primary" id="all-schedule-btn">所有课程</button>
                </div>
            </div>
            <div class="schedule-container">
                <div class="course-list">
        `;
        
        // 没有课程的情况
        if (Object.keys(courseGroups).length === 0) {
            html += `
                <div class="empty-schedule">
                    <p>暂无课程安排</p>
                </div>
            `;
        } else {
            // 添加每门课程
            Object.values(courseGroups).forEach(course => {
                html += `
                    <div class="course-card" data-course-id="${course.courseId}">
                        <div class="course-header">
                            <h3>${course.courseName}</h3>
                            <span class="teacher-name">授课教师: ${course.teacherName || '未指定'}</span>
                        </div>
                        <div class="course-schedule">
                            <h4>上课时间:</h4>
                            <ul class="schedule-list">
                `;
                
                // 添加每节课的时间
                course.classes.forEach(cls => {
                    html += `
                        <li>
                            <span class="weekday">周${this.getWeekdayName(cls.weekday)}</span>
                            <span class="time-slot">${this.getTimeBySlot(cls.timeSlot)}</span>
                            <span class="location">${cls.location || '未指定地点'}</span>
                        </li>
                    `;
                });
                
                html += `
                            </ul>
                        </div>
                        <div class="course-actions">
                            <button class="btn btn-primary view-course-btn" data-course-id="${course.courseId}">课程详情</button>
                        </div>
                    </div>
                `;
            });
        }
        
        html += `
                </div>
            </div>
        `;
        
        container.innerHTML = html;
        
        // 绑定按钮事件
        document.getElementById('today-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'day', options);
        });
        
        document.getElementById('week-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'week', options);
        });
        
        document.getElementById('all-schedule-btn').addEventListener('click', () => {
            this.initSchedule(container.id, 'all', options);
        });
        
        // 绑定课程详情按钮点击事件
        const viewButtons = container.querySelectorAll('.view-course-btn');
        viewButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const courseId = button.getAttribute('data-course-id');
                if (courseId) {
                    UI.course.viewCourseDetail(parseInt(courseId));
                }
            });
        });
        
        // 绑定课程卡片点击事件
        const courseCards = container.querySelectorAll('.course-card');
        courseCards.forEach(card => {
            card.addEventListener('click', () => {
                const courseId = card.getAttribute('data-course-id');
                if (courseId) {
                    UI.course.viewCourseDetail(parseInt(courseId));
                }
            });
        });
    },
    
    /**
     * 根据时间段ID获取时间字符串
     * @param {number} slotId 时间段ID
     * @returns {string} 时间字符串
     */
    getTimeBySlot(slotId) {
        const timeMap = {
            1: '08:00-08:45',
            2: '08:55-09:40',
            3: '10:00-10:45',
            4: '10:55-11:40',
            5: '13:30-14:15',
            6: '14:25-15:10',
            7: '15:30-16:15',
            8: '16:25-17:10',
            9: '18:30-19:15',
            10: '19:25-20:10'
        };
        return timeMap[slotId] || `第${slotId}节`;
    },
    
    /**
     * 获取星期几的名称
     * @param {number} weekday 星期几的数字表示（1-7）
     * @returns {string} 星期几的名称
     */
    getWeekdayName(weekday) {
        const weekdayNames = ['一', '二', '三', '四', '五', '六', '日'];
        return weekdayNames[weekday - 1] || weekday;
    },
    
    /**
     * 初始化教室预约
     * @param {string} containerId 容器ID
     */
    initClassroomReservation(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 获取教室列表
        API.classroom.getAll()
            .then(classrooms => {
                // 创建预约界面
                let html = `
                    <div class="reservation-container">
                        <h2>教室预约</h2>
                        <div class="reservation-form-container">
                            <div class="form-group">
                                <label for="reservation-date">日期</label>
                                <input type="date" id="reservation-date" class="form-control" min="${new Date().toISOString().split('T')[0]}">
                            </div>
                            <div class="form-group">
                                <label for="reservation-time-slot">时间段</label>
                                <select id="reservation-time-slot" class="form-control">
                                    <option value="1">第1节 (08:00-08:45)</option>
                                    <option value="2">第2节 (08:55-09:40)</option>
                                    <option value="3">第3节 (10:00-10:45)</option>
                                    <option value="4">第4节 (10:55-11:40)</option>
                                    <option value="5">第5节 (13:30-14:15)</option>
                                    <option value="6">第6节 (14:25-15:10)</option>
                                    <option value="7">第7节 (15:30-16:15)</option>
                                    <option value="8">第8节 (16:25-17:10)</option>
                                    <option value="9">第9节 (18:30-19:15)</option>
                                    <option value="10">第10节 (19:25-20:10)</option>
                                </select>
                            </div>
                            <button id="check-availability-btn" class="btn btn-primary">查询可用教室</button>
                        </div>
                        <div id="available-classrooms-container" class="available-classrooms-container">
                            <p>请选择日期和时间段查询可用教室</p>
                        </div>
                    </div>
                    
                    <div class="my-reservations-container">
                        <h2>我的预约</h2>
                        <div id="my-reservations-list" class="my-reservations-list">
                            <p>加载中...</p>
                        </div>
                    </div>
                `;
                
                container.innerHTML = html;
                
                // 加载我的预约
                this.loadMyReservations('my-reservations-list');
                
                // 绑定查询按钮事件
                document.getElementById('check-availability-btn').addEventListener('click', () => {
                    const date = document.getElementById('reservation-date').value;
                    const timeSlot = document.getElementById('reservation-time-slot').value;
                    
                    if (!date) {
                        UI.showMessage('请选择日期', 'warning');
                        return;
                    }
                    
                    this.checkAvailableClassrooms(date, timeSlot);
                });
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载教室数据失败: ${error.message}</div>`;
                console.error('加载教室数据失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 查询可用教室
     * @param {string} date 日期
     * @param {string} timeSlot 时间段
     */
    checkAvailableClassrooms(date, timeSlot) {
        const container = document.getElementById('available-classrooms-container');
        if (!container) return;
        
        container.innerHTML = '<p>查询中...</p>';
        UI.showLoading();
        
        API.classroom.getAvailable(date, timeSlot)
            .then(classrooms => {
                if (classrooms.length === 0) {
                    container.innerHTML = '<p>当前时段没有可用教室</p>';
                    return;
                }
                
                let html = '<div class="classroom-grid">';
                classrooms.forEach(classroom => {
                    html += `
                        <div class="classroom-card">
                            <div class="classroom-info">
                                <h3>${classroom.name}</h3>
                                <p>容量: ${classroom.capacity}人</p>
                                <p>位置: ${classroom.location || '未指定'}</p>
                                <p>设施: ${classroom.facilities || '标准配置'}</p>
                            </div>
                            <div class="classroom-actions">
                                <button class="btn btn-primary reserve-btn" data-id="${classroom.id}">预约</button>
                            </div>
                        </div>
                    `;
                });
                html += '</div>';
                
                container.innerHTML = html;
                
                // 绑定预约按钮点击事件
                const reserveButtons = container.querySelectorAll('.reserve-btn');
                reserveButtons.forEach(button => {
                    button.addEventListener('click', () => {
                        const classroomId = button.getAttribute('data-id');
                        const date = document.getElementById('reservation-date').value;
                        const timeSlot = document.getElementById('reservation-time-slot').value;
                        this.showReservationForm(classroomId, date, timeSlot);
                    });
                });
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">查询可用教室失败: ${error.message}</div>`;
                console.error('查询可用教室失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 显示预约表单
     * @param {number} classroomId 教室ID
     * @param {string} date 日期
     * @param {string} timeSlot 时间段
     */
    showReservationForm(classroomId, date, timeSlot) {
        // 获取教室信息
        UI.showLoading();
        
        API.classroom.getById(classroomId)
            .then(classroom => {
                const fields = [
                    { name: 'classroomId', type: 'hidden', value: classroomId },
                    { name: 'date', type: 'hidden', value: date },
                    { name: 'timeSlot', type: 'hidden', value: timeSlot },
                    { name: 'purpose', label: '用途', type: 'text', required: true }
                ];
                
                const dialog = UI.createDialog('预约教室', '', '500px');
                
                let content = `
                    <div class="reservation-dialog-info">
                        <div class="info-item">
                            <span class="label">教室:</span>
                            <span class="value">${classroom.name}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">日期:</span>
                            <span class="value">${date}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">时间段:</span>
                            <span class="value">第${timeSlot}节 (${this.getTimeBySlot(timeSlot)})</span>
                        </div>
                    </div>
                `;
                
                // 创建表单
                const form = UI.createForm(fields, {}, (formData) => {
                    UI.showLoading();
                    
                    // 发送预约请求
                    API.classroom.reserve(formData.classroomId, formData.date, formData.timeSlot, formData.purpose)
                        .then(() => {
                            dialog.close();
                            UI.showMessage('教室预约成功', 'success');
                            // 刷新可用教室列表
                            this.checkAvailableClassrooms(date, timeSlot);
                            // 刷新我的预约列表
                            this.loadMyReservations('my-reservations-list');
                        })
                        .catch(error => {
                            UI.showMessage(`预约失败: ${error.message}`, 'error');
                            console.error('预约失败:', error);
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                });
                
                // 设置对话框内容
                dialog.setContent(content + form);
                dialog.show();
            })
            .catch(error => {
                UI.showMessage(`获取教室信息失败: ${error.message}`, 'error');
                console.error('获取教室信息失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 加载我的预约
     * @param {string} containerId 容器ID
     */
    loadMyReservations(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        container.innerHTML = '<p>加载中...</p>';
        
        API.classroom.getMyReservations()
            .then(reservations => {
                if (reservations.length === 0) {
                    container.innerHTML = '<p>您还没有预约记录</p>';
                    return;
                }
                
                // 定义表格列
                const columns = [
                    { title: '教室', field: 'classroomName' },
                    { title: '日期', field: 'date' },
                    { title: '时间段', field: 'timeSlot', render: (timeSlot) => `第${timeSlot}节 (${this.getTimeBySlot(timeSlot)})` },
                    { title: '用途', field: 'purpose' },
                    { title: '状态', field: 'status', render: (status) => {
                        return status === 1 ? 
                            '<span class="status-tag status-active">已确认</span>' : 
                            status === 0 ? 
                            '<span class="status-tag status-pending">待确认</span>' : 
                            '<span class="status-tag status-inactive">已取消</span>';
                    }}
                ];
                
                // 定义操作按钮
                const actions = [
                    { text: '取消预约', type: 'danger', handler: (reservation) => this.cancelReservation(reservation.id), condition: (reservation) => reservation.status !== 2 }
                ];
                
                // 创建表格
                const table = UI.createTable(columns, reservations, actions);
                container.innerHTML = '';
                container.appendChild(table);
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载预约记录失败: ${error.message}</div>`;
                console.error('加载预约记录失败:', error);
            });
    },
    
    /**
     * 取消预约
     * @param {number} reservationId 预约ID
     */
    cancelReservation(reservationId) {
        if (confirm('确定要取消这个预约吗？')) {
            UI.showLoading();
            
            API.classroom.cancelReservation(reservationId)
                .then(() => {
                    UI.showMessage('预约已取消', 'success');
                    // 刷新我的预约列表
                    this.loadMyReservations('my-reservations-list');
                })
                .catch(error => {
                    UI.showMessage(`取消预约失败: ${error.message}`, 'error');
                    console.error('取消预约失败:', error);
                })
                .finally(() => {
                    UI.hideLoading();
                });
        }
    }
}; 

/**
 * 论坛模块UI组件
 */
UI.forum = {
    /**
     * 初始化论坛
     * @param {string} containerId 容器ID
     * @param {Object} options 选项
     */
    initForum(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 创建论坛基本结构
        container.innerHTML = `
            <div class="forum-container">
                <div class="forum-header">
                    <h2>校园论坛</h2>
                    <div class="forum-actions">
                        <button id="create-post-btn" class="btn btn-primary">发布新帖</button>
                        <div class="search-box">
                            <input type="text" id="forum-search-input" placeholder="搜索帖子..." class="form-control">
                            <button id="forum-search-btn" class="btn btn-default">搜索</button>
                        </div>
                    </div>
                </div>
                <div class="forum-filter">
                    <div class="filter-item active" data-filter="all">全部</div>
                    <div class="filter-item" data-filter="announcement">公告</div>
                    <div class="filter-item" data-filter="question">问答</div>
                    <div class="filter-item" data-filter="discussion">讨论</div>
                    <div class="filter-item" data-filter="resource">资源</div>
                    <div class="filter-item" data-filter="my">我的帖子</div>
                </div>
                <div id="forum-post-list" class="forum-post-list">
                    <div class="loading-spinner"></div>
                </div>
                <div id="forum-pagination" class="forum-pagination"></div>
            </div>
        `;
        
        // 加载帖子列表
        this.loadPosts('forum-post-list', 'all', 1, options.pageSize || 10);
        
        // 绑定发布新帖按钮事件
        document.getElementById('create-post-btn').addEventListener('click', () => {
            this.showCreatePostForm();
        });
        
        // 绑定搜索按钮事件
        document.getElementById('forum-search-btn').addEventListener('click', () => {
            const keyword = document.getElementById('forum-search-input').value.trim();
            if (keyword) {
                this.searchPosts('forum-post-list', keyword, 1, options.pageSize || 10);
            }
        });
        
        // 绑定搜索框回车事件
        document.getElementById('forum-search-input').addEventListener('keyup', (e) => {
            if (e.key === 'Enter') {
                const keyword = e.target.value.trim();
                if (keyword) {
                    this.searchPosts('forum-post-list', keyword, 1, options.pageSize || 10);
                }
            }
        });
        
        // 绑定分类筛选事件
        const filterItems = container.querySelectorAll('.filter-item');
        filterItems.forEach(item => {
            item.addEventListener('click', () => {
                // 更新选中状态
                filterItems.forEach(i => i.classList.remove('active'));
                item.classList.add('active');
                
                // 加载对应分类的帖子
                const filter = item.getAttribute('data-filter');
                this.loadPosts('forum-post-list', filter, 1, options.pageSize || 10);
            });
        });
        
        UI.hideLoading();
    },
    
    /**
     * 加载帖子列表
     * @param {string} containerId 容器ID
     * @param {string} filter 筛选条件
     * @param {number} page 页码
     * @param {number} pageSize 每页数量
     */
    loadPosts(containerId, filter = 'all', page = 1, pageSize = 10) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        container.innerHTML = '<div class="loading-spinner"></div>';
        
        // 选择API方法
        let apiMethod;
        switch (filter) {
            case 'my':
                apiMethod = API.post.getMy;
                break;
            case 'announcement':
            case 'question':
            case 'discussion':
            case 'resource':
                apiMethod = (page, pageSize) => API.post.getByType(filter, page, pageSize);
                break;
            case 'all':
            default:
                apiMethod = API.post.getAll;
                break;
        }
        
        // 获取帖子数据
        apiMethod(page, pageSize)
            .then(response => {
                const { posts, total } = response;
                
                if (posts.length === 0) {
                    container.innerHTML = '<div class="empty-posts">暂无帖子</div>';
                    document.getElementById('forum-pagination').innerHTML = '';
                    return;
                }
                
                // 渲染帖子列表
                this.renderPostList(container, posts);
                
                // 渲染分页
                this.renderPagination('forum-pagination', page, Math.ceil(total / pageSize), (p) => {
                    this.loadPosts(containerId, filter, p, pageSize);
                });
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">加载帖子失败: ${error.message}</div>`;
                console.error('加载帖子失败:', error);
            });
    },
    
    /**
     * 搜索帖子
     * @param {string} containerId 容器ID
     * @param {string} keyword 关键词
     * @param {number} page 页码
     * @param {number} pageSize 每页数量
     */
    searchPosts(containerId, keyword, page = 1, pageSize = 10) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        container.innerHTML = '<div class="loading-spinner"></div>';
        
        API.post.search(keyword, page, pageSize)
            .then(response => {
                const { posts, total } = response;
                
                if (posts.length === 0) {
                    container.innerHTML = '<div class="empty-posts">没有找到匹配的帖子</div>';
                    document.getElementById('forum-pagination').innerHTML = '';
                    return;
                }
                
                // 渲染帖子列表
                this.renderPostList(container, posts);
                
                // 渲染分页
                this.renderPagination('forum-pagination', page, Math.ceil(total / pageSize), (p) => {
                    this.searchPosts(containerId, keyword, p, pageSize);
                });
            })
            .catch(error => {
                container.innerHTML = `<div class="error-message">搜索帖子失败: ${error.message}</div>`;
                console.error('搜索帖子失败:', error);
            });
    },
    
    /**
     * 渲染帖子列表
     * @param {HTMLElement} container 容器元素
     * @param {Array} posts 帖子数据
     */
    renderPostList(container, posts) {
        let html = '';
        
        posts.forEach(post => {
            const tagHtml = post.tags ? post.tags.map(tag => 
                `<span class="post-tag" style="background-color: ${tag.color || '#5c6bc0'}">${tag.name}</span>`
            ).join('') : '';
            
            // 帖子类型对应的图标
            const typeIcon = {
                'announcement': 'bi-megaphone-fill',
                'question': 'bi-question-circle-fill',
                'discussion': 'bi-chat-fill',
                'resource': 'bi-file-earmark-fill'
            }[post.type] || 'bi-file-text-fill';
            
            html += `
                <div class="post-item" data-post-id="${post.id}">
                    <div class="post-icon">
                        <i class="bi ${typeIcon}"></i>
                    </div>
                    <div class="post-content">
                        <div class="post-header">
                            <h3 class="post-title">${post.title}</h3>
                            <div class="post-tags">
                                ${tagHtml}
                            </div>
                        </div>
                        <div class="post-summary">${post.summary || post.content.substring(0, 100) + '...'}</div>
                        <div class="post-meta">
                            <span class="post-author">${post.authorName}</span>
                            <span class="post-time">${this.formatPostTime(post.createTime)}</span>
                            <span class="post-views"><i class="bi bi-eye"></i> ${post.viewCount || 0}</span>
                            <span class="post-comments"><i class="bi bi-chat"></i> ${post.commentCount || 0}</span>
                            ${post.attachment ? '<span class="post-attachment"><i class="bi bi-paperclip"></i> 附件</span>' : ''}
                        </div>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
        
        // 绑定帖子点击事件
        const postItems = container.querySelectorAll('.post-item');
        postItems.forEach(item => {
            item.addEventListener('click', () => {
                const postId = item.getAttribute('data-post-id');
                if (postId) {
                    this.showPostDetail(parseInt(postId));
                }
            });
        });
    },
    
    /**
     * 渲染分页
     * @param {string} containerId 容器ID
     * @param {number} currentPage 当前页码
     * @param {number} totalPages 总页数
     * @param {Function} callback 页码点击回调
     */
    renderPagination(containerId, currentPage, totalPages, callback) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        if (totalPages <= 1) {
            container.innerHTML = '';
            return;
        }
        
        let html = '<div class="pagination">';
        
        // 上一页
        if (currentPage > 1) {
            html += `<span class="page-item" data-page="${currentPage - 1}">上一页</span>`;
        } else {
            html += `<span class="page-item disabled">上一页</span>`;
        }
        
        // 页码
        const maxPageButtons = 5;
        let startPage = Math.max(1, currentPage - Math.floor(maxPageButtons / 2));
        let endPage = Math.min(totalPages, startPage + maxPageButtons - 1);
        
        if (endPage - startPage + 1 < maxPageButtons) {
            startPage = Math.max(1, endPage - maxPageButtons + 1);
        }
        
        if (startPage > 1) {
            html += `<span class="page-item" data-page="1">1</span>`;
            if (startPage > 2) {
                html += `<span class="page-item disabled">...</span>`;
            }
        }
        
        for (let i = startPage; i <= endPage; i++) {
            if (i === currentPage) {
                html += `<span class="page-item active">${i}</span>`;
            } else {
                html += `<span class="page-item" data-page="${i}">${i}</span>`;
            }
        }
        
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<span class="page-item disabled">...</span>`;
            }
            html += `<span class="page-item" data-page="${totalPages}">${totalPages}</span>`;
        }
        
        // 下一页
        if (currentPage < totalPages) {
            html += `<span class="page-item" data-page="${currentPage + 1}">下一页</span>`;
        } else {
            html += `<span class="page-item disabled">下一页</span>`;
        }
        
        html += '</div>';
        
        container.innerHTML = html;
        
        // 绑定页码点击事件
        const pageItems = container.querySelectorAll('.page-item:not(.disabled):not(.active)');
        pageItems.forEach(item => {
            item.addEventListener('click', () => {
                const page = parseInt(item.getAttribute('data-page'));
                if (page && callback) {
                    callback(page);
                }
            });
        });
    },
    
    /**
     * 显示帖子详情
     * @param {number} postId 帖子ID
     */
    showPostDetail(postId) {
        UI.showLoading();
        
        API.post.getById(postId)
            .then(post => {
                const dialog = UI.createDialog(post.title, '', '800px');
                
                // 帖子类型显示
                const typeNames = {
                    'announcement': '公告',
                    'question': '问答',
                    'discussion': '讨论',
                    'resource': '资源'
                };
                
                const typeName = typeNames[post.type] || '帖子';
                
                // 帖子标签
                const tagHtml = post.tags ? post.tags.map(tag => 
                    `<span class="post-tag" style="background-color: ${tag.color || '#5c6bc0'}">${tag.name}</span>`
                ).join('') : '';
                
                // 附件
                let attachmentHtml = '';
                if (post.attachment) {
                    attachmentHtml = `
                        <div class="post-attachment-section">
                            <h4><i class="bi bi-paperclip"></i> 附件</h4>
                            <div class="attachment-item">
                                <i class="bi bi-file-earmark"></i>
                                <span class="attachment-name">${post.attachment.filename}</span>
                                <a href="${API.post.getAttachmentUrl(post.attachment.id)}" class="btn btn-sm btn-primary" download>下载</a>
                            </div>
                        </div>
                    `;
                }
                
                // 帖子内容
                let content = `
                    <div class="post-detail">
                        <div class="post-header">
                            <div class="post-meta">
                                <span class="post-type">${typeName}</span>
                                <span class="post-author">${post.authorName}</span>
                                <span class="post-time">${this.formatPostTime(post.createTime)}</span>
                                <span class="post-views"><i class="bi bi-eye"></i> ${post.viewCount || 0}</span>
                            </div>
                            <div class="post-tags">
                                ${tagHtml}
                            </div>
                        </div>
                        <div class="post-content">
                            ${post.content}
                        </div>
                        ${attachmentHtml}
                    </div>
                    <div class="post-comments-section">
                        <h3>评论 (${post.comments ? post.comments.length : 0})</h3>
                        <div class="comment-form">
                            <textarea id="comment-content" class="form-control" rows="3" placeholder="写下你的评论..."></textarea>
                            <button id="submit-comment-btn" class="btn btn-primary" data-post-id="${post.id}">提交评论</button>
                        </div>
                        <div class="comments-list">
                `;
                
                // 评论列表
                if (post.comments && post.comments.length > 0) {
                    post.comments.forEach(comment => {
                        content += `
                            <div class="comment-item">
                                <div class="comment-header">
                                    <span class="comment-author">${comment.authorName}</span>
                                    <span class="comment-time">${this.formatPostTime(comment.createTime)}</span>
                                </div>
                                <div class="comment-content">${comment.content}</div>
                            </div>
                        `;
                    });
                } else {
                    content += '<div class="empty-comments">暂无评论</div>';
                }
                
                content += `
                        </div>
                    </div>
                `;
                
                dialog.setContent(content);
                dialog.show();
                
                // 绑定评论提交按钮事件
                document.getElementById('submit-comment-btn').addEventListener('click', (e) => {
                    const postId = e.target.getAttribute('data-post-id');
                    const content = document.getElementById('comment-content').value.trim();
                    
                    if (!content) {
                        UI.showMessage('评论内容不能为空', 'warning');
                        return;
                    }
                    
                    this.submitComment(postId, content, () => {
                        // 重新显示帖子详情
                        dialog.close();
                        this.showPostDetail(postId);
                    });
                });
            })
            .catch(error => {
                UI.showMessage(`加载帖子详情失败: ${error.message}`, 'error');
                console.error('加载帖子详情失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 提交评论
     * @param {number} postId 帖子ID
     * @param {string} content 评论内容
     * @param {Function} callback 成功回调
     */
    submitComment(postId, content, callback) {
        UI.showLoading();
        
        API.post.comment(postId, content)
            .then(() => {
                UI.showMessage('评论发布成功', 'success');
                if (callback) {
                    callback();
                }
            })
            .catch(error => {
                UI.showMessage(`评论发布失败: ${error.message}`, 'error');
                console.error('评论发布失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 显示创建帖子表单
     */
    showCreatePostForm() {
        // 创建对话框
        const dialog = UI.createDialog('发布新帖', '', '800px');
        
        // 获取标签列表
        API.post.getTags()
            .then(tags => {
                const fields = [
                    { name: 'title', label: '标题', type: 'text', required: true },
                    { 
                        name: 'type', 
                        label: '类型', 
                        type: 'select', 
                        required: true,
                        options: [
                            { value: 'discussion', text: '讨论' },
                            { value: 'question', text: '问答' },
                            { value: 'resource', text: '资源' },
                            { value: 'announcement', text: '公告' }
                        ]
                    },
                    { name: 'content', label: '内容', type: 'textarea', required: true, rows: 10 },
                    { 
                        name: 'tags', 
                        label: '标签', 
                        type: 'checkboxGroup',
                        options: tags.map(tag => ({ value: tag.id.toString(), text: tag.name, style: `color: white; background-color: ${tag.color || '#5c6bc0'}` }))
                    },
                    { name: 'attachment', label: '附件', type: 'file' }
                ];
                
                // 创建表单
                const form = UI.createForm(fields, {}, (formData) => {
                    UI.showLoading();
                    
                    // 添加默认值
                    if (!formData.tags) {
                        formData.tags = [];
                    }
                    
                    // 发布帖子
                    API.post.create(formData)
                        .then(() => {
                            dialog.close();
                            UI.showMessage('帖子发布成功', 'success');
                            
                            // 刷新帖子列表
                            this.loadPosts('forum-post-list', 'all', 1, 10);
                        })
                        .catch(error => {
                            UI.showMessage(`发布帖子失败: ${error.message}`, 'error');
                            console.error('发布帖子失败:', error);
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                });
                
                dialog.setContent(form);
                dialog.show();
            })
            .catch(error => {
                dialog.close();
                UI.showMessage(`加载标签数据失败: ${error.message}`, 'error');
                console.error('加载标签数据失败:', error);
            });
    },
    
    /**
     * 格式化帖子时间
     * @param {string} timestamp 时间戳
     * @returns {string} 格式化后的时间
     */
    formatPostTime(timestamp) {
        if (!timestamp) return '';
        
        const date = new Date(timestamp);
        const now = new Date();
        const diff = Math.floor((now - date) / 1000); // 秒数差
        
        if (diff < 60) {
            return '刚刚';
        } else if (diff < 3600) {
            return Math.floor(diff / 60) + '分钟前';
        } else if (diff < 86400) {
            return Math.floor(diff / 3600) + '小时前';
        } else if (diff < 2592000) {
            return Math.floor(diff / 86400) + '天前';
        } else {
            const year = date.getFullYear();
            const month = ('0' + (date.getMonth() + 1)).slice(-2);
            const day = ('0' + date.getDate()).slice(-2);
            return `${year}-${month}-${day}`;
        }
    }
};

/**
 * 管理员后台UI组件
 */
UI.admin = {
    /**
     * 初始化标签管理模块
     */
    initTagModule: function() {
        const tagContainer = document.getElementById('tagListContainer');
        const refreshBtn = document.getElementById('refreshTagBtn');
        const addBtn = document.getElementById('addTagBtn');
        const searchInput = document.getElementById('tagSearchInput');
        const searchBtn = document.getElementById('tagSearchBtn');
        
        if (!tagContainer) return;
        
        // 加载标签列表
        this.loadTags();
        
        // 绑定刷新按钮
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                this.loadTags();
            });
        }
        
        // 绑定添加按钮
        if (addBtn) {
            addBtn.addEventListener('click', () => {
                this.showTagForm();
            });
        }
        
        // 绑定搜索按钮
        if (searchBtn && searchInput) {
            searchBtn.addEventListener('click', () => {
                const keyword = searchInput.value.trim();
                this.searchTags(keyword);
            });
            
            // 绑定回车搜索
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    const keyword = searchInput.value.trim();
                    this.searchTags(keyword);
                }
            });
        }
    },
    
    /**
     * 加载标签列表
     */
    loadTags: function() {
        const container = document.getElementById('tagListContainer');
        if (!container) return;
        
        container.innerHTML = '<div class="loading-placeholder">加载中，请稍候...</div>';
        
        // 发送请求获取标签列表
        API.request('/api/tags')
            .then(data => {
                if (data && data.length > 0) {
                    this.renderTags(data);
                } else {
                    container.innerHTML = '<div class="empty-data">暂无标签数据</div>';
                }
            })
            .catch(error => {
                console.error('加载标签失败:', error);
                container.innerHTML = '<div class="error-message">加载标签失败，请稍后重试</div>';
            });
    },
    
    /**
     * 搜索标签
     * @param {string} keyword 搜索关键词
     */
    searchTags: function(keyword) {
        const container = document.getElementById('tagListContainer');
        if (!container) return;
        
        if (!keyword) {
            this.loadTags();
            return;
        }
        
        container.innerHTML = '<div class="loading-placeholder">搜索中，请稍候...</div>';
        
        // 发送请求搜索标签
        API.request(`/api/tags/search?keyword=${encodeURIComponent(keyword)}`)
            .then(data => {
                if (data && data.length > 0) {
                    this.renderTags(data);
                } else {
                    container.innerHTML = '<div class="empty-data">未找到匹配的标签</div>';
                }
            })
            .catch(error => {
                console.error('搜索标签失败:', error);
                container.innerHTML = '<div class="error-message">搜索标签失败，请稍后重试</div>';
            });
    },
    
    /**
     * 渲染标签列表
     * @param {Array} tags 标签数据
     */
    renderTags: function(tags) {
        const container = document.getElementById('tagListContainer');
        if (!container) return;
        
        let html = `
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>名称</th>
                        <th>颜色</th>
                        <th>创建时间</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        tags.forEach(tag => {
            const createTime = tag.createTime ? new Date(tag.createTime).toLocaleString() : '-';
            
            html += `
                <tr>
                    <td>${tag.id}</td>
                    <td>
                        <span class="tag-item" style="background-color: ${tag.color || '#5c6bc0'}">${tag.name}</span>
                    </td>
                    <td>
                        <div class="color-box" style="background-color: ${tag.color || '#5c6bc0'}"></div>
                        ${tag.color || '-'}
                    </td>
                    <td>${createTime}</td>
                    <td>
                        <button class="btn btn-sm btn-info edit-tag-btn" data-id="${tag.id}">编辑</button>
                        <button class="btn btn-sm btn-danger delete-tag-btn" data-id="${tag.id}">删除</button>
                    </td>
                </tr>
            `;
        });
        
        html += '</tbody></table>';
        
        container.innerHTML = html;
        
        // 绑定编辑按钮事件
        container.querySelectorAll('.edit-tag-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const tagId = btn.getAttribute('data-id');
                this.showTagForm(tagId);
            });
        });
        
        // 绑定删除按钮事件
        container.querySelectorAll('.delete-tag-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const tagId = btn.getAttribute('data-id');
                this.confirmDeleteTag(tagId);
            });
        });
    },
    
    /**
     * 显示标签表单
     * @param {number|null} tagId 标签ID，null表示新增
     */
    showTagForm: function(tagId = null) {
        const isEdit = tagId !== null;
        const title = isEdit ? '编辑标签' : '添加标签';
        
        let content = `
            <form id="tagForm" class="form">
                <div class="form-group">
                    <label for="tagName">标签名称</label>
                    <input type="text" id="tagName" class="form-control" placeholder="请输入标签名称" required>
                </div>
                <div class="form-group">
                    <label for="tagColor">标签颜色</label>
                    <input type="color" id="tagColor" class="form-control color-picker" value="#5c6bc0">
                </div>
                <div class="form-group">
                    <label>预览效果</label>
                    <div class="tag-preview">
                        <span id="tagPreview" class="tag-item" style="background-color: #5c6bc0">标签预览</span>
                    </div>
                </div>
            </form>
        `;
        
        const dialog = UI.createDialog(title, content, '400px');
        
        const nameInput = document.getElementById('tagName');
        const colorInput = document.getElementById('tagColor');
        const tagPreview = document.getElementById('tagPreview');
        
        // 加载标签数据
        if (isEdit) {
            UI.showLoading();
            
            API.request(`/api/tags/${tagId}`)
                .then(tag => {
                    nameInput.value = tag.name || '';
                    colorInput.value = tag.color || '#5c6bc0';
                    tagPreview.style.backgroundColor = tag.color || '#5c6bc0';
                    tagPreview.textContent = tag.name || '标签预览';
                    
                    UI.hideLoading();
                })
                .catch(error => {
                    console.error('加载标签详情失败:', error);
                    UI.hideLoading();
                    UI.showMessage('加载标签详情失败', 'error');
                    dialog.close();
                });
        }
        
        // 预览效果
        colorInput.addEventListener('input', () => {
            tagPreview.style.backgroundColor = colorInput.value;
        });
        
        nameInput.addEventListener('input', () => {
            tagPreview.textContent = nameInput.value || '标签预览';
        });
        
        // 添加表单按钮
        dialog.addButton('取消', () => {
            dialog.close();
        });
        
        dialog.addButton(isEdit ? '保存' : '添加', () => {
            const name = nameInput.value.trim();
            const color = colorInput.value;
            
            if (!name) {
                UI.showMessage('请输入标签名称', 'warning');
                return;
            }
            
            this.saveTag(tagId, name, color, dialog);
        }, 'primary');
    },
    
    /**
     * 保存标签
     * @param {number|null} tagId 标签ID，null表示新增
     * @param {string} name 标签名称
     * @param {string} color 标签颜色
     * @param {object} dialog 对话框对象
     */
    saveTag: function(tagId, name, color, dialog) {
        const isEdit = tagId !== null;
        const url = isEdit ? `/api/tags/${tagId}` : '/api/tags';
        const method = isEdit ? 'PUT' : 'POST';
        const data = { name, color };
        
        if (tagId) {
            data.id = tagId;
        }
        
        UI.showLoading();
        
        API.request(url, method, data)
            .then(() => {
                UI.hideLoading();
                UI.showMessage(isEdit ? '标签更新成功' : '标签添加成功', 'success');
                dialog.close();
                this.loadTags();
            })
            .catch(error => {
                console.error('保存标签失败:', error);
                UI.hideLoading();
                UI.showMessage('保存标签失败: ' + (error.message || '未知错误'), 'error');
            });
    },
    
    /**
     * 确认删除标签
     * @param {number} tagId 标签ID
     */
    confirmDeleteTag: function(tagId) {
        UI.confirm('确定要删除这个标签吗？', '删除后无法恢复', () => {
            this.deleteTag(tagId);
        });
    },
    
    /**
     * 删除标签
     * @param {number} tagId 标签ID
     */
    deleteTag: function(tagId) {
        UI.showLoading();
        
        API.request(`/api/tags/${tagId}`, 'DELETE')
            .then(() => {
                UI.hideLoading();
                UI.showMessage('标签删除成功', 'success');
                this.loadTags();
            })
            .catch(error => {
                console.error('删除标签失败:', error);
                UI.hideLoading();
                UI.showMessage('删除标签失败: ' + (error.message || '未知错误'), 'error');
            });
    },
    
    // ... existing code ...
};

// 标签管理模块
UI.tag = {
    // 初始化标签管理模块
    initTagManagement: function(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 显示加载中状态
        UI.showLoading();
        
        // 绑定按钮事件
        const refreshBtn = document.getElementById('refreshTagBtn');
        const addTagBtn = document.getElementById('addTagBtn');
        const searchBtn = document.getElementById('tagSearchBtn');
        const searchInput = document.getElementById('tagSearchInput');
        
        if (refreshBtn) refreshBtn.addEventListener('click', () => this.loadTags(containerId));
        if (addTagBtn) addTagBtn.addEventListener('click', () => this.showTagForm());
        if (searchBtn && searchInput) {
            searchBtn.addEventListener('click', () => {
                const keyword = searchInput.value.trim();
                this.searchTags(containerId, keyword);
            });
            searchInput.addEventListener('keyup', (e) => {
                if (e.key === 'Enter') {
                    const keyword = e.target.value.trim();
                    this.searchTags(containerId, keyword);
                }
            });
        }
        
        // 加载标签数据
        this.loadTags(containerId);
    },
    
    // 加载标签列表
    loadTags: function(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        const tagListContainer = document.getElementById('tagList');
        if (!tagListContainer) return;
        
        // 显示加载中状态
        tagListContainer.innerHTML = '<div class="loading-placeholder">加载标签数据中，请稍候...</div>';
        
        // 调用API获取标签数据
        API.request('/api/tags/list')
            .then(response => {
                UI.hideLoading();
                if (response && Array.isArray(response)) {
                    this.renderTagList(tagListContainer, response);
                } else {
                    tagListContainer.innerHTML = '<div class="empty-placeholder">暂无标签数据</div>';
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('获取标签数据失败:', error);
                tagListContainer.innerHTML = '<div class="error-placeholder">获取标签数据失败</div>';
                UI.showMessage('获取标签数据失败: ' + (error.message || '未知错误'), 'error');
            });
    },
    
    // 搜索标签
    searchTags: function(containerId, keyword) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        const tagListContainer = document.getElementById('tagList');
        if (!tagListContainer) return;
        
        if (!keyword) {
            // 如果关键词为空，则加载所有标签
            this.loadTags(containerId);
            return;
        }
        
        // 显示加载中状态
        tagListContainer.innerHTML = '<div class="loading-placeholder">搜索中，请稍候...</div>';
        
        // 调用API搜索标签
        API.request('/api/tags/search?keyword=' + encodeURIComponent(keyword))
            .then(response => {
                if (response && Array.isArray(response)) {
                    this.renderTagList(tagListContainer, response);
                } else {
                    tagListContainer.innerHTML = '<div class="empty-placeholder">未找到匹配的标签</div>';
                }
            })
            .catch(error => {
                console.error('搜索标签失败:', error);
                tagListContainer.innerHTML = '<div class="error-placeholder">搜索标签失败</div>';
                UI.showMessage('搜索标签失败: ' + (error.message || '未知错误'), 'error');
            });
    },
    
    // 渲染标签列表
    renderTagList: function(container, tags) {
        if (!container || !tags) return;
        
        if (tags.length === 0) {
            container.innerHTML = '<div class="empty-placeholder">暂无标签数据</div>';
            return;
        }
        
        // 创建表格
        const columns = [
            { field: 'id', title: 'ID', width: '80px' },
            { field: 'name', title: '标签名称', width: '180px' },
            { field: 'color', title: '颜色', width: '120px', render: (data) => {
                return `<span class="color-block" style="background-color: ${data.color}"></span> ${data.color}`;
            } },
            { field: 'createTime', title: '创建时间', width: '150px', render: (data) => {
                return data.createTime ? new Date(data.createTime).toLocaleString() : '未知';
            } }
        ];
        
        const actions = [
            { name: '编辑', icon: 'pencil', handler: (tag) => this.editTag(tag) },
            { name: '删除', icon: 'trash', handler: (tag) => this.deleteTag(tag.id) }
        ];
        
        const table = UI.createTable(columns, tags, actions);
        container.innerHTML = '';
        container.appendChild(table);
    },
    
    // 显示标签表单
    showTagForm: function(tag = null) {
        const isEdit = !!tag;
        const title = isEdit ? '编辑标签' : '添加标签';
        
        const fields = [
            { name: 'name', label: '标签名称', type: 'text', required: true, value: tag ? tag.name : '', placeholder: '请输入标签名称' },
            { name: 'color', label: '标签颜色', type: 'color', required: true, value: tag ? tag.color : '#1890ff', placeholder: '请选择颜色' }
        ];
        
        if (isEdit) {
            fields.unshift({ name: 'id', label: 'ID', type: 'hidden', value: tag.id });
        }
        
        const form = UI.createForm(fields, {}, (formData) => {
            if (isEdit) {
                this.updateTag(formData);
            } else {
                this.createTag(formData);
            }
        });
        
        UI.createDialog(title, form);
    },
    
    // 创建标签
    createTag: function(tagData) {
        if (!tagData || !tagData.name || !tagData.color) {
            UI.showMessage('请填写完整的标签信息', 'warning');
            return;
        }
        
        UI.showLoading();
        
        API.request('/api/tags/add', 'POST', tagData)
            .then(response => {
                UI.hideLoading();
                if (response && response.success) {
                    UI.showMessage('标签添加成功', 'success');
                    UI.hideDialog();
                    this.loadTags('tag');
                } else {
                    UI.showMessage('标签添加失败: ' + (response.message || '未知错误'), 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('标签添加失败:', error);
                UI.showMessage('标签添加失败: ' + (error.message || '未知错误'), 'error');
            });
    },
    
    // 更新标签
    updateTag: function(tagData) {
        if (!tagData || !tagData.id || !tagData.name || !tagData.color) {
            UI.showMessage('请填写完整的标签信息', 'warning');
            return;
        }
        
        UI.showLoading();
        
        API.request('/api/tags/update', 'POST', tagData)
            .then(response => {
                UI.hideLoading();
                if (response && response.success) {
                    UI.showMessage('标签更新成功', 'success');
                    UI.hideDialog();
                    this.loadTags('tag');
                } else {
                    UI.showMessage('标签更新失败: ' + (response.message || '未知错误'), 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('标签更新失败:', error);
                UI.showMessage('标签更新失败: ' + (error.message || '未知错误'), 'error');
            });
    },
    
    // 编辑标签
    editTag: function(tag) {
        if (!tag || !tag.id) {
            UI.showMessage('标签信息不完整', 'warning');
            return;
        }
        
        UI.showLoading();
        
        // 获取最新的标签信息
        API.request('/api/tags/get?id=' + tag.id)
            .then(response => {
                UI.hideLoading();
                if (response) {
                    this.showTagForm(response);
                } else {
                    UI.showMessage('获取标签信息失败', 'error');
                }
            })
            .catch(error => {
                UI.hideLoading();
                console.error('获取标签信息失败:', error);
                UI.showMessage('获取标签信息失败: ' + (error.message || '未知错误'), 'error');
            });
    },
    
    // 删除标签
    deleteTag: function(tagId) {
        if (!tagId) {
            UI.showMessage('标签ID不能为空', 'warning');
            return;
        }
        
        UI.confirm('确定要删除这个标签吗？', '此操作不可恢复，标签删除后将无法还原。', () => {
            UI.showLoading();
            
            API.request('/api/tags/delete?id=' + tagId, 'DELETE')
                .then(response => {
                    UI.hideLoading();
                    if (response && response.success) {
                        UI.showMessage('标签删除成功', 'success');
                        this.loadTags('tag');
                    } else {
                        UI.showMessage('标签删除失败: ' + (response.message || '未知错误'), 'error');
                    }
                })
                .catch(error => {
                    UI.hideLoading();
                    console.error('标签删除失败:', error);
                    UI.showMessage('标签删除失败: ' + (error.message || '未知错误'), 'error');
                });
        });
    }
};

/**
 * 学生模块UI组件
 */
UI.student = {
    /**
     * 初始化个人信息页面
     */
    initProfile() {
        UI.showLoading();
        
        API.student.getProfile()
            .then(data => {
                if (data) {
                    // 填充个人信息表单
                    document.querySelectorAll('[data-field]').forEach(field => {
                        const fieldName = field.getAttribute('data-field');
                        if (data[fieldName] !== undefined) {
                            field.value = data[fieldName];
                        }
                    });
                    
                    // 显示头像
                    const avatarImg = document.getElementById('avatarPreview');
                    if (avatarImg && data.avatar) {
                        avatarImg.src = data.avatar;
                    }
                }
            })
            .catch(error => {
                UI.showMessage('获取个人信息失败: ' + error.message, 'error');
                console.error('获取个人信息失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 更新个人信息
     * @param {Object} formData 表单数据
     */
    updateProfile(formData) {
        UI.showLoading();
        
        API.student.updateProfile(formData)
            .then(result => {
                UI.showMessage('个人信息更新成功', 'success');
                
                // 更新本地存储的用户信息
                const user = JSON.parse(localStorage.getItem('user')) || {};
                Object.assign(user, formData);
                localStorage.setItem('user', JSON.stringify(user));
            })
            .catch(error => {
                UI.showMessage('更新个人信息失败: ' + error.message, 'error');
                console.error('更新个人信息失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 初始化文件列表
     * @param {string} containerId 容器ID
     * @param {Object} params 查询参数
     */
    initFilesList(containerId, params = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.student.getFiles(params)
            .then(result => {
                if (!result || !result.data || result.data.length === 0) {
                    container.innerHTML = '<div class="empty-state">暂无文件</div>';
                    return;
                }
                
                const files = result.data;
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
                    const size = this.formatFileSize(file.size);
                    
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
                                <button class="btn btn-icon delete-file" title="删除">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                    `;
                });
                
                container.innerHTML = html;
                
                // 绑定文件操作事件
                this.bindFileEvents(container);
                
                // 更新分页
                if (result.total > result.pageSize) {
                    this.updatePagination(result.page, result.pageSize, result.total, params);
                }
            })
            .catch(error => {
                container.innerHTML = '<div class="error-state">加载文件失败</div>';
                UI.showMessage('加载文件失败: ' + error.message, 'error');
                console.error('加载文件失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 格式化文件大小
     * @param {number} bytes 文件大小（字节）
     * @returns {string} 格式化后的大小
     */
    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    /**
     * 绑定文件操作事件
     * @param {HTMLElement} container 文件容器
     */
    bindFileEvents(container) {
        // 下载文件
        container.querySelectorAll('.download-file').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const fileItem = btn.closest('.file-item');
                const fileId = fileItem.getAttribute('data-id');
                
                this.downloadFile(fileId);
            });
        });
        
        // 删除文件
        container.querySelectorAll('.delete-file').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const fileItem = btn.closest('.file-item');
                const fileId = fileItem.getAttribute('data-id');
                const fileName = fileItem.querySelector('.file-name').textContent;
                
                if (confirm(`确定要删除文件 "${fileName}" 吗？`)) {
                    this.deleteFile(fileId, fileItem);
                }
            });
        });
    },
    
    /**
     * 下载文件
     * @param {string} fileId 文件ID
     */
    downloadFile(fileId) {
        UI.showLoading();
        
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
                UI.showMessage('下载文件失败: ' + error.message, 'error');
                console.error('下载文件失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 删除文件
     * @param {string} fileId 文件ID
     * @param {HTMLElement} fileItem 文件元素
     */
    deleteFile(fileId, fileItem) {
        UI.showLoading();
        
        API.student.deleteFile(fileId)
            .then(() => {
                UI.showMessage('文件删除成功', 'success');
                
                // 从DOM中移除
                if (fileItem) {
                    fileItem.remove();
                    
                    // 检查是否为空
                    const container = fileItem.parentNode;
                    if (container && container.children.length === 0) {
                        container.innerHTML = '<div class="empty-state">暂无文件</div>';
                    }
                }
            })
            .catch(error => {
                UI.showMessage('删除文件失败: ' + error.message, 'error');
                console.error('删除文件失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 初始化活动列表
     * @param {string} containerId 容器ID
     * @param {string} type 活动类型：all, ongoing, upcoming, my
     * @param {Object} params 查询参数
     */
    initActivitiesList(containerId, type = 'all', params = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 确定API方法
        let apiMethod;
        switch (type) {
            case 'ongoing':
                apiMethod = API.student.getOngoingActivities;
                break;
            case 'upcoming':
                apiMethod = API.student.getUpcomingActivities;
                break;
            case 'my':
                apiMethod = API.student.getMyActivities;
                break;
            default:
                apiMethod = API.student.getAllActivities;
        }
        
        apiMethod(params)
            .then(result => {
                if (!result || !result.data || result.data.length === 0) {
                    container.innerHTML = '<div class="empty-state">暂无活动</div>';
                    return;
                }
                
                const activities = result.data;
                let html = '';
                
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
                    
                    // 格式化日期
                    const startDate = new Date(activity.startTime).toLocaleDateString('zh-CN');
                    const endDate = new Date(activity.endTime).toLocaleDateString('zh-CN');
                    
                    html += `
                        <div class="activity-card" data-id="${activity.id}">
                            <div class="activity-header">
                                <h3 class="activity-title">${activity.title}</h3>
                                <span class="activity-status ${statusClass}">${statusText}</span>
                            </div>
                            <div class="activity-content">
                                <p class="activity-description">${activity.description || '暂无描述'}</p>
                                <div class="activity-meta">
                                    <div class="meta-item">
                                        <i class="bi bi-geo-alt"></i>
                                        <span>${activity.location || '暂无地点'}</span>
                                    </div>
                                    <div class="meta-item">
                                        <i class="bi bi-calendar-event"></i>
                                        <span>${startDate} - ${endDate}</span>
                                    </div>
                                    <div class="meta-item">
                                        <i class="bi bi-person"></i>
                                        <span>${activity.organizer || '暂无组织者'}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="activity-footer">
                                <button class="btn view-activity">查看详情</button>
                                ${now <= endTime ? `<button class="btn btn-primary signup-activity">立即报名</button>` : ''}
                            </div>
                        </div>
                    `;
                });
                
                container.innerHTML = html;
                
                // 绑定活动操作事件
                this.bindActivityEvents(container);
                
                // 更新分页
                if (result.total > result.pageSize) {
                    this.updatePagination(result.page, result.pageSize, result.total, params);
                }
            })
            .catch(error => {
                container.innerHTML = '<div class="error-state">加载活动失败</div>';
                UI.showMessage('加载活动失败: ' + error.message, 'error');
                console.error('加载活动失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    }
};

/**
 * 教师模块UI组件
 */
UI.teacher = {
    /**
     * 初始化个人信息页面
     */
    initProfile: function() {
        API.getCurrentUser(function(success, user) {
            if (success && user) {
                $('#teacherName').text(user.name);
                $('#teacherId').text(user.id);
                $('#teacherEmail').text(user.email || '未设置');
    initProfile() {
        UI.showLoading();
        
        API.teacher.getProfile()
            .then(data => {
                if (data) {
                    // 填充个人信息表单
                    document.querySelectorAll('[data-field]').forEach(field => {
                        const fieldName = field.getAttribute('data-field');
                        if (data[fieldName] !== undefined) {
                            field.value = data[fieldName];
                        }
                    });
                    
                    // 显示头像
                    const avatarImg = document.getElementById('avatarPreview');
                    if (avatarImg && data.avatar) {
                        avatarImg.src = data.avatar;
                    }
                }
            })
            .catch(error => {
                UI.showMessage('获取个人信息失败: ' + error.message, 'error');
                console.error('获取个人信息失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 更新个人信息
     * @param {Object} formData 表单数据
     */
    updateProfile(formData) {
        UI.showLoading();
        
        API.teacher.updateProfile(formData)
            .then(result => {
                UI.showMessage('个人信息更新成功', 'success');
                
                // 更新本地存储的用户信息
                const user = JSON.parse(localStorage.getItem('user')) || {};
                Object.assign(user, formData);
                localStorage.setItem('user', JSON.stringify(user));
            })
            .catch(error => {
                UI.showMessage('更新个人信息失败: ' + error.message, 'error');
                console.error('更新个人信息失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 初始化课程列表
     * @param {string} containerId 容器ID
     * @param {Object} params 查询参数
     */
    initCourseList(containerId, params = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.teacher.getMyCourses(params)
            .then(result => {
                if (!result || !result.data || result.data.length === 0) {
                    container.innerHTML = '<div class="empty-state">暂无课程</div>';
                    return;
                }
                
                const courses = result.data;
                let html = '';
                
                courses.forEach(course => {
                    // 格式化课程信息
                    const termText = course.termName || '未知学期';
                    const statusClass = course.status === 1 ? 'status-active' : 'status-inactive';
                    const statusText = course.status === 1 ? '进行中' : '已结束';
                    
                    html += `
                        <div class="course-card" data-id="${course.id}">
                            <div class="course-header">
                                <h3 class="course-title">${course.name}</h3>
                                <span class="course-status ${statusClass}">${statusText}</span>
                            </div>
                            <div class="course-content">
                                <p class="course-description">${course.description || '暂无描述'}</p>
                                <div class="course-meta">
                                    <div class="meta-item">
                                        <i class="bi bi-calendar"></i>
                                        <span>${termText}</span>
                                    </div>
                                    <div class="meta-item">
                                        <i class="bi bi-people"></i>
                                        <span>${course.studentCount || 0} 名学生</span>
                                    </div>
                                    <div class="meta-item">
                                        <i class="bi bi-clock"></i>
                                        <span>${course.creditHours || 0} 学时</span>
                                    </div>
                                </div>
                            </div>
                            <div class="course-footer">
                                <button class="btn view-course" data-id="${course.id}">查看详情</button>
                                <button class="btn btn-primary manage-course" data-id="${course.id}">管理课程</button>
                            </div>
                        </div>
                    `;
                });
                
                container.innerHTML = html;
                
                // 绑定课程操作事件
                this.bindCourseEvents(container);
                
                // 更新分页
                if (result.total > result.pageSize) {
                    UI.renderPagination('pagination', result.page, Math.ceil(result.total / result.pageSize), 
                        (page) => this.initCourseList(containerId, { ...params, page }));
                }
            })
            .catch(error => {
                container.innerHTML = '<div class="error-state">加载课程失败</div>';
                UI.showMessage('加载课程失败: ' + error.message, 'error');
                console.error('加载课程失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 绑定课程操作事件
     * @param {HTMLElement} container 课程容器
     */
    bindCourseEvents(container) {
        // 查看课程详情
        container.querySelectorAll('.view-course').forEach(btn => {
            btn.addEventListener('click', () => {
                const courseId = btn.getAttribute('data-id');
                this.viewCourseDetail(courseId);
            });
        });
        
        // 管理课程
        container.querySelectorAll('.manage-course').forEach(btn => {
            btn.addEventListener('click', () => {
                const courseId = btn.getAttribute('data-id');
                this.manageCourse(courseId);
            });
        });
    },
    
    /**
     * 查看课程详情
     * @param {string} courseId 课程ID
     */
    viewCourseDetail(courseId) {
        UI.showLoading();
        
        API.teacher.getCourseById(courseId)
            .then(course => {
                if (!course) {
                    UI.showMessage('课程不存在', 'error');
                    return;
                }
                
                // 创建模态框显示课程详情
                let modalContent = `
                    <div class="course-detail">
                        <div class="detail-header">
                            <h2>${course.name}</h2>
                            <span class="badge ${course.status === 1 ? 'badge-success' : 'badge-secondary'}">
                                ${course.status === 1 ? '进行中' : '已结束'}
                            </span>
                        </div>
                        <div class="detail-body">
                            <div class="detail-item">
                                <span class="detail-label">课程编号：</span>
                                <span class="detail-value">${course.courseNo || '-'}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">学期：</span>
                                <span class="detail-value">${course.termName || '-'}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">学分：</span>
                                <span class="detail-value">${course.credit || 0}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">学时：</span>
                                <span class="detail-value">${course.creditHours || 0}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">授课教师：</span>
                                <span class="detail-value">${course.teacherName || '-'}</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">选课人数：</span>
                                <span class="detail-value">${course.studentCount || 0}</span>
                            </div>
                            <div class="detail-item full-width">
                                <span class="detail-label">课程描述：</span>
                                <div class="detail-value description">${course.description || '暂无描述'}</div>
                            </div>
                        </div>
                    </div>
                `;
                
                UI.createDialog('课程详情', modalContent, '600px');
            })
            .catch(error => {
                UI.showMessage('获取课程详情失败: ' + error.message, 'error');
                console.error('获取课程详情失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 管理课程（跳转到课程管理页面）
     * @param {string} courseId 课程ID
     */
    manageCourse(courseId) {
        // 这里可以跳转到课程管理页面，或者打开一个更复杂的模态框
        window.location.href = `course-management.html?id=${courseId}`;
    },
    
    /**
     * 创建课程
     */
    createCourse() {
        // 创建表单字段定义
        const fields = [
            { name: 'name', label: '课程名称', type: 'text', required: true },
            { name: 'courseNo', label: '课程编号', type: 'text', required: true },
            { name: 'termId', label: '学期', type: 'select', required: true, options: [] },
            { name: 'credit', label: '学分', type: 'number', min: 0, step: 0.5, required: true },
            { name: 'creditHours', label: '学时', type: 'number', min: 0, required: true },
            { name: 'description', label: '课程描述', type: 'textarea' }
        ];
        
        // 先获取学期列表
        UI.showLoading();
        
        API.request('/terms')
            .then(terms => {
                UI.hideLoading();
                
                // 找到表单中的学期字段
                const termField = fields.find(field => field.name === 'termId');
                
                // 设置学期选项
                if (termField && terms && terms.length > 0) {
                    termField.options = terms.map(term => ({
                        value: term.id,
                        label: term.name
                    }));
                }
                
                // 创建表单并显示在对话框中
                const formHtml = UI.createForm(fields, {}, (formData) => {
                    // 提交表单
                    UI.showLoading();
                    
                    API.teacher.createCourse(formData)
                        .then(result => {
                            UI.showMessage('课程创建成功', 'success');
                            UI.hideDialog();
                            
                            // 刷新课程列表
                            this.initCourseList('courses-container');
                        })
                        .catch(error => {
                            UI.showMessage('创建课程失败: ' + error.message, 'error');
                            console.error('创建课程失败:', error);
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                });
                
                UI.createDialog('创建新课程', formHtml, '600px');
            })
            .catch(error => {
                UI.hideLoading();
                UI.showMessage('获取学期列表失败: ' + error.message, 'error');
                console.error('获取学期列表失败:', error);
            });
    },
    
    /**
     * 初始化学生列表
     * @param {string} containerId 容器ID
     * @param {string} courseId 课程ID
     */
    initStudentList(containerId, courseId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.teacher.getCourseStudents(courseId)
            .then(result => {
                if (!result || !result.data || result.data.length === 0) {
                    container.innerHTML = '<div class="empty-state">暂无学生</div>';
                    return;
                }
                
                const students = result.data;
                
                // 定义表格列
                const columns = [
                    { key: 'studentNo', title: '学号' },
                    { key: 'name', title: '姓名' },
                    { key: 'gender', title: '性别', render: (row) => row.gender === 1 ? '男' : '女' },
                    { key: 'className', title: '班级' },
                    { key: 'majorName', title: '专业' },
                    { key: 'collegeName', title: '学院' },
                    { key: 'score', title: '成绩', render: (row) => row.score !== undefined ? row.score : '-' }
                ];
                
                // 定义操作列
                const actions = [
                    { 
                        text: '录入成绩', 
                        class: 'btn-primary', 
                        handler: (row) => this.enterScore(courseId, row.id, row.name)
                    },
                    { 
                        text: '删除', 
                        class: 'btn-danger', 
                        confirm: '确定要将该学生从课程中移除吗？',
                        handler: (row) => this.removeStudentFromCourse(courseId, row.id)
                    }
                ];
                
                // 创建表格
                const tableHtml = UI.createTable(columns, students, actions);
                container.innerHTML = tableHtml;
            })
            .catch(error => {
                container.innerHTML = '<div class="error-state">加载学生名单失败</div>';
                UI.showMessage('加载学生名单失败: ' + error.message, 'error');
                console.error('加载学生名单失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 录入学生成绩
     * @param {string} courseId 课程ID
     * @param {string} studentId 学生ID
     * @param {string} studentName 学生姓名
     */
    enterScore(courseId, studentId, studentName) {
        // 创建表单字段定义
        const fields = [
            { name: 'score', label: '成绩', type: 'number', min: 0, max: 100, step: 0.1, required: true },
            { name: 'comment', label: '评语', type: 'textarea' }
        ];
        
        // 获取当前成绩
        UI.showLoading();
        
        API.request(`/courses/${courseId}/students/${studentId}/score`)
            .then(scoreData => {
                UI.hideLoading();
                
                const initialValues = scoreData || { score: '', comment: '' };
                
                // 创建表单并显示在对话框中
                const formHtml = UI.createForm(fields, initialValues, (formData) => {
                    // 提交表单
                    UI.showLoading();
                    
                    API.teacher.updateStudentScore(`${courseId}/${studentId}`, formData)
                        .then(result => {
                            UI.showMessage('成绩录入成功', 'success');
                            UI.hideDialog();
                            
                            // 刷新学生列表
                            this.initStudentList('students-container', courseId);
                        })
                        .catch(error => {
                            UI.showMessage('成绩录入失败: ' + error.message, 'error');
                            console.error('成绩录入失败:', error);
                        })
                        .finally(() => {
                            UI.hideLoading();
                        });
                });
                
                UI.createDialog(`录入 ${studentName} 的成绩`, formHtml, '500px');
            })
            .catch(error => {
                UI.hideLoading();
                UI.showMessage('获取成绩信息失败: ' + error.message, 'error');
                console.error('获取成绩信息失败:', error);
            });
    },
    
    /**
     * 将学生从课程中移除
     * @param {string} courseId 课程ID
     * @param {string} studentId 学生ID
     */
    removeStudentFromCourse(courseId, studentId) {
        UI.showLoading();
        
        API.teacher.removeStudentFromCourse(courseId, studentId)
            .then(result => {
                UI.showMessage('学生已成功从课程中移除', 'success');
                
                // 刷新学生列表
                this.initStudentList('students-container', courseId);
            })
            .catch(error => {
                UI.showMessage('移除学生失败: ' + error.message, 'error');
                console.error('移除学生失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 初始化课表
     * @param {string} containerId 容器ID
     * @param {number} weekNumber 周次
     */
    initSchedule(containerId, weekNumber = null) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 如果没有指定周次，则获取当前周
        const requestWeek = weekNumber || getCurrentWeek();
        
        API.teacher.getSchedule(requestWeek)
            .then(scheduleData => {
                if (!scheduleData || !scheduleData.schedules) {
                    container.innerHTML = '<div class="empty-state">本周暂无课程安排</div>';
                    return;
                }
                
                // 渲染周课表
                const scheduleHtml = this.renderWeekSchedule(scheduleData, requestWeek);
                container.innerHTML = scheduleHtml;
                
                // 绑定课程点击事件
                container.querySelectorAll('.course-item').forEach(item => {
                    item.addEventListener('click', () => {
                        const courseId = item.getAttribute('data-course-id');
                        if (courseId) {
                            this.viewCourseDetail(courseId);
                        }
                    });
                });
                
                // 绑定周次切换事件
                const prevWeekBtn = container.querySelector('#prevWeekBtn');
                const nextWeekBtn = container.querySelector('#nextWeekBtn');
                
                if (prevWeekBtn) {
                    prevWeekBtn.addEventListener('click', () => {
                        this.initSchedule(containerId, requestWeek - 1);
                    });
                }
                
                if (nextWeekBtn) {
                    nextWeekBtn.addEventListener('click', () => {
                        this.initSchedule(containerId, requestWeek + 1);
                    });
                }
            })
            .catch(error => {
                container.innerHTML = '<div class="error-state">加载课表失败</div>';
                UI.showMessage('加载课表失败: ' + error.message, 'error');
                console.error('加载课表失败:', error);
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 渲染周课表
     * @param {Object} scheduleData 课表数据
     * @param {number} weekNumber 周次
     * @returns {string} 渲染后的HTML
     */
    renderWeekSchedule(scheduleData, weekNumber) {
        // 课程时间段
        const timeSlots = [
            { id: 1, name: '第一节', time: '08:00-08:45' },
            { id: 2, name: '第二节', time: '08:55-09:40' },
            { id: 3, name: '第三节', time: '10:00-10:45' },
            { id: 4, name: '第四节', time: '10:55-11:40' },
            { id: 5, name: '第五节', time: '13:30-14:15' },
            { id: 6, name: '第六节', time: '14:25-15:10' },
            { id: 7, name: '第七节', time: '15:30-16:15' },
            { id: 8, name: '第八节', time: '16:25-17:10' },
            { id: 9, name: '第九节', time: '18:30-19:15' },
            { id: 10, name: '第十节', time: '19:25-20:10' }
        ];
        
        // 获取当前周范围的日期字符串
        const weekDates = getWeekDates(weekNumber);
        
        // 生成课表头部
        let html = `
            <div class="schedule-container">
                <div class="schedule-header">
                    <button id="prevWeekBtn" class="btn btn-sm"><i class="bi bi-chevron-left"></i></button>
                    <h3>第 ${weekNumber} 周课表</h3>
                    <button id="nextWeekBtn" class="btn btn-sm"><i class="bi bi-chevron-right"></i></button>
                </div>
                <div class="schedule-grid">
                    <div class="schedule-row header-row">
                        <div class="time-column">时间</div>
                        <div class="day-column">周一<br>${weekDates[0]}</div>
                        <div class="day-column">周二<br>${weekDates[1]}</div>
                        <div class="day-column">周三<br>${weekDates[2]}</div>
                        <div class="day-column">周四<br>${weekDates[3]}</div>
                        <div class="day-column">周五<br>${weekDates[4]}</div>
                        <div class="day-column">周六<br>${weekDates[5]}</div>
                        <div class="day-column">周日<br>${weekDates[6]}</div>
                    </div>
        `;
        
        // 生成时间段行
        timeSlots.forEach(slot => {
            html += `
                <div class="schedule-row">
                    <div class="time-column">
                        <div class="time-slot">
                            <span class="slot-name">${slot.name}</span>
                            <span class="slot-time">${slot.time}</span>
                        </div>
                    </div>
            `;
            
            // 生成每天的单元格
            for (let day = 1; day <= 7; day++) {
                // 查找该时间段该天的课程
                const course = scheduleData.schedules.find(c => 
                    c.weekday === day && c.timeSlot === slot.id);
                
                if (course) {
                    // 如果有课程，显示课程信息
                    html += `
                        <div class="day-column">
                            <div class="course-item" data-course-id="${course.courseId}">
                                <div class="course-name">${course.courseName}</div>
                                <div class="course-location">${course.classroom || '未排教室'}</div>
                            </div>
                        </div>
                    `;
                } else {
                    // 如果没有课程，显示空白单元格
                    html += `<div class="day-column"></div>`;
                }
            }
            
            html += `</div>`;
        });
        
        html += `</div></div>`;
        
        return html;
    }
};

// 辅助函数：获取当前教学周次
function getCurrentWeek() {
    // 实际应用中，这里应该从后端获取当前的教学周信息
    // 这里简单返回1，表示第一周
    return 1;
}

// 辅助函数：根据周次获取一周的日期字符串
function getWeekDates(weekNumber) {
    // 实际应用中，这里应该根据学期开始日期和周次计算实际日期
    // 这里简单返回占位符
    return ['03-01', '03-02', '03-03', '03-04', '03-05', '03-06', '03-07'];
}