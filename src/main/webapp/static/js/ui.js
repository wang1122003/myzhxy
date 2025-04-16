/**
 * 核心UI组件库
 * 提供通用UI功能和工具方法
 */
const UI = {
    /**
     * 显示加载中提示
     */
    showLoading: function() {
        const loading = document.getElementById('loading');
        if (loading) {
            loading.style.display = 'flex';
        } else {
            const loadingEl = document.createElement('div');
            loadingEl.id = 'loading';
            loadingEl.className = 'loading-overlay';
            loadingEl.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner"></div>
                    <div class="loading-text">加载中...</div>
                </div>
            `;
            loadingEl.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9999;
            `;
            document.body.appendChild(loadingEl);
        }
    },
    
    /**
     * 隐藏加载中提示
     */
    hideLoading: function() {
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
    showMessage: function(message, type = 'info') {
        // 检查是否已存在消息容器
        let container = document.querySelector('.message-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'message-container';
            container.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 10000;
            `;
            document.body.appendChild(container);
        }
        
        // 创建消息元素
        const messageEl = document.createElement('div');
        messageEl.className = `message message-${type}`;
        messageEl.innerHTML = `
            <div class="message-icon">
                <i class="bi ${type === 'success' ? 'bi-check-circle' : 
                               type === 'error' ? 'bi-x-circle' : 
                               type === 'warning' ? 'bi-exclamation-triangle' : 
                               'bi-info-circle'}"></i>
            </div>
            <div class="message-content">${message}</div>
            <button class="message-close">
                <i class="bi bi-x"></i>
            </button>
        `;
        messageEl.style.cssText = `
            display: flex;
            align-items: center;
            margin-bottom: 10px;
            padding: 12px 15px;
            background-color: ${type === 'success' ? '#e6f7e9' : 
                               type === 'error' ? '#fce6e6' : 
                               type === 'warning' ? '#fff5e6' : 
                               '#e6f3ff'};
            color: ${type === 'success' ? '#2c7a39' : 
                    type === 'error' ? '#d33838' : 
                    type === 'warning' ? '#b37400' : 
                    '#0066cc'};
            border-left: 4px solid ${type === 'success' ? '#42b155' : 
                                      type === 'error' ? '#e74c3c' : 
                                      type === 'warning' ? '#f39c12' : 
                                      '#3498db'};
            border-radius: 4px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
            opacity: 0;
            transform: translateX(20px);
            transition: opacity 0.3s, transform 0.3s;
        `;
        
        container.appendChild(messageEl);
        
        // 显示消息动画
        setTimeout(() => {
            messageEl.style.opacity = '1';
            messageEl.style.transform = 'translateX(0)';
        }, 10);
        
        // 关闭按钮事件
        const closeBtn = messageEl.querySelector('.message-close');
        closeBtn.addEventListener('click', () => {
            hideMessage(messageEl);
        });
        
        // 自动关闭
        const duration = type === 'error' ? 6000 : 3000;
        const timer = setTimeout(() => {
            hideMessage(messageEl);
        }, duration);
        
        // 鼠标悬停时暂停自动关闭
        messageEl.addEventListener('mouseenter', () => {
            clearTimeout(timer);
        });
        
        messageEl.addEventListener('mouseleave', () => {
            setTimeout(() => {
                hideMessage(messageEl);
            }, duration / 2);
        });
        
        // 关闭消息的函数
        function hideMessage(el) {
            el.style.opacity = '0';
            el.style.transform = 'translateX(20px)';
            setTimeout(() => {
                if (el.parentNode) {
                    el.parentNode.removeChild(el);
                }
            }, 300);
        }
    },
    
    /**
     * 创建表格
     * @param {Array} columns 列定义
     * @param {Array} data 数据
     * @param {Object} actions 操作按钮定义
     * @returns {HTMLElement} 表格元素
     */
    createTable: function(columns, data, actions = null) {
        const table = document.createElement('table');
        table.className = 'table';
        
        // 创建表头
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        
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
            const th = document.createElement('th');
            th.textContent = '操作';
            th.style.width = '150px';
            headerRow.appendChild(th);
        }
        
        thead.appendChild(headerRow);
        table.appendChild(thead);
        
        // 创建表体
        const tbody = document.createElement('tbody');
        
        if (data && data.length > 0) {
            data.forEach(item => {
                const row = document.createElement('tr');
                
                columns.forEach(column => {
                    const td = document.createElement('td');
                    
                    // 自定义渲染
                    if (column.render) {
                        td.innerHTML = column.render(item[column.key], item);
                    } else {
                        td.textContent = item[column.key] || '';
                    }
                    
                    row.appendChild(td);
                });
                
                // 添加操作按钮
                if (actions) {
                    const td = document.createElement('td');
                    td.className = 'table-actions';
                    
                    Object.keys(actions).forEach(key => {
                        const action = actions[key];
                        
                        // 检查是否应该显示此按钮
                        if (action.visible && !action.visible(item)) {
                            return;
                        }
                        
                        const button = document.createElement('button');
                        button.className = `btn btn-sm ${action.className || 'btn-outline-primary'}`;
                        button.innerHTML = action.icon ? `<i class="${action.icon}"></i> ${action.text}` : action.text;
                        button.title = action.text;
                        
                        // 禁用检查
                        if (action.disabled && action.disabled(item)) {
                            button.disabled = true;
                            button.classList.add('disabled');
                        }
                        
                        // 添加点击事件
                        button.addEventListener('click', (e) => {
                            e.preventDefault();
                            action.onClick(item, button);
                        });
                        
                        td.appendChild(button);
                    });
                    
                    row.appendChild(td);
                }
                
                tbody.appendChild(row);
            });
        } else {
            // 没有数据时显示空状态
            const row = document.createElement('tr');
            const td = document.createElement('td');
            td.colSpan = columns.length + (actions ? 1 : 0);
            td.className = 'text-center text-muted py-4';
            td.innerHTML = '<i class="bi bi-inbox me-2"></i>暂无数据';
            row.appendChild(td);
            tbody.appendChild(row);
        }
        
        table.appendChild(tbody);
        return table;
    },
    
    /**
     * 创建表单
     * @param {Array} fields 字段定义
     * @param {Object} values 表单值
     * @param {Function} submitHandler 提交处理函数
     * @returns {HTMLElement} 表单元素
     */
    createForm: function(fields, values = {}, submitHandler = null) {
        const form = document.createElement('form');
        
        fields.forEach(field => {
            const formGroup = document.createElement('div');
            formGroup.className = 'form-group mb-3';
            
            // 创建标签
            if (field.label) {
                const label = document.createElement('label');
                label.htmlFor = field.id || field.name;
                label.className = 'form-label';
                label.textContent = field.label;
                
                if (field.required) {
                    const requiredMark = document.createElement('span');
                    requiredMark.className = 'text-danger ms-1';
                    requiredMark.textContent = '*';
                    label.appendChild(requiredMark);
                }
                
                formGroup.appendChild(label);
            }
            
            // 根据字段类型创建控件
            let input;
            
            switch (field.type) {
                case 'textarea':
                    input = document.createElement('textarea');
                    input.className = 'form-control';
                    input.rows = field.rows || 3;
                    break;
                    
                case 'select':
                    input = document.createElement('select');
                    input.className = 'form-select';
                    
                    // 如果有选项，添加选项
                    if (field.options) {
                        // 添加默认空选项
                        if (!field.required) {
                            const defaultOption = document.createElement('option');
                            defaultOption.value = '';
                            defaultOption.textContent = '请选择' + (field.label || '');
                            input.appendChild(defaultOption);
                        }
                        
                        // 添加其他选项
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
                    break;
                    
                case 'checkbox':
                    const checkWrapper = document.createElement('div');
                    checkWrapper.className = 'form-check';
                    
                    input = document.createElement('input');
                    input.type = 'checkbox';
                    input.className = 'form-check-input';
                    
                    const checkLabel = document.createElement('label');
                    checkLabel.className = 'form-check-label';
                    checkLabel.htmlFor = field.id || field.name;
                    checkLabel.textContent = field.checkLabel || field.label;
                    
                    checkWrapper.appendChild(input);
                    checkWrapper.appendChild(checkLabel);
                    formGroup.appendChild(checkWrapper);
                    break;
                    
                case 'radio':
                    const radioContainer = document.createElement('div');
                    
                    if (field.options) {
                        field.options.forEach((option, index) => {
                            const radioWrapper = document.createElement('div');
                            radioWrapper.className = 'form-check';
                            
                            const radioInput = document.createElement('input');
                            radioInput.type = 'radio';
                            radioInput.className = 'form-check-input';
                            radioInput.name = field.name;
                            radioInput.id = `${field.name}_${index}`;
                            
                            if (typeof option === 'object') {
                                radioInput.value = option.value;
                                const radioLabel = document.createElement('label');
                                radioLabel.className = 'form-check-label';
                                radioLabel.htmlFor = `${field.name}_${index}`;
                                radioLabel.textContent = option.label;
                                radioWrapper.appendChild(radioInput);
                                radioWrapper.appendChild(radioLabel);
                            } else {
                                radioInput.value = option;
                                const radioLabel = document.createElement('label');
                                radioLabel.className = 'form-check-label';
                                radioLabel.htmlFor = `${field.name}_${index}`;
                                radioLabel.textContent = option;
                                radioWrapper.appendChild(radioInput);
                                radioWrapper.appendChild(radioLabel);
                            }
                            
                            radioContainer.appendChild(radioWrapper);
                        });
                    }
                    
                    formGroup.appendChild(radioContainer);
                    break;
                    
                case 'date':
                    input = document.createElement('input');
                    input.type = 'date';
                    input.className = 'form-control';
                    break;
                    
                case 'time':
                    input = document.createElement('input');
                    input.type = 'time';
                    input.className = 'form-control';
                    break;
                    
                case 'datetime-local':
                    input = document.createElement('input');
                    input.type = 'datetime-local';
                    input.className = 'form-control';
                    break;
                    
                case 'number':
                    input = document.createElement('input');
                    input.type = 'number';
                    input.className = 'form-control';
                    
                    if (field.min !== undefined) {
                        input.min = field.min;
                    }
                    
                    if (field.max !== undefined) {
                        input.max = field.max;
                    }
                    
                    if (field.step !== undefined) {
                        input.step = field.step;
                    }
                    break;
                    
                case 'file':
                    input = document.createElement('input');
                    input.type = 'file';
                    input.className = 'form-control';
                    
                    if (field.accept) {
                        input.accept = field.accept;
                    }
                    
                    if (field.multiple) {
                        input.multiple = true;
                    }
                    break;
                    
                default: // 默认文本输入
                    input = document.createElement('input');
                    input.type = field.type || 'text';
                    input.className = 'form-control';
            }
            
            // 设置通用属性
            if (field.type !== 'radio') {
                input.id = field.id || field.name;
                input.name = field.name;
                
                if (field.placeholder) {
                    input.placeholder = field.placeholder;
                }
                
                if (field.required) {
                    input.required = true;
                }
                
                if (field.disabled) {
                    input.disabled = true;
                }
                
                if (field.readonly) {
                    input.readOnly = true;
                }
                
                // 如果有值，设置值
                if (values[field.name] !== undefined && field.type !== 'file') {
                    if (field.type === 'checkbox') {
                        input.checked = !!values[field.name];
                    } else {
                        input.value = values[field.name];
                    }
                }
                
                // 如果有验证规则，添加验证
                if (field.pattern) {
                    input.pattern = field.pattern;
                }
                
                // 如果有自定义属性，添加
                if (field.attributes) {
                    Object.keys(field.attributes).forEach(key => {
                        input.setAttribute(key, field.attributes[key]);
                    });
                }
                
                // 添加到表单组
                if (field.type !== 'checkbox') {
                    formGroup.appendChild(input);
                }
            }
            
            // 添加帮助文本
            if (field.helpText) {
                const helpText = document.createElement('div');
                helpText.className = 'form-text text-muted';
                helpText.textContent = field.helpText;
                formGroup.appendChild(helpText);
            }
            
            form.appendChild(formGroup);
        });
        
        // 添加提交按钮
        if (submitHandler) {
            const formActions = document.createElement('div');
            formActions.className = 'form-actions mt-4';
            
            const submitButton = document.createElement('button');
            submitButton.type = 'submit';
            submitButton.className = 'btn btn-primary';
            submitButton.textContent = '提交';
            
            const cancelButton = document.createElement('button');
            cancelButton.type = 'button';
            cancelButton.className = 'btn btn-secondary ms-2';
            cancelButton.textContent = '取消';
            
            formActions.appendChild(submitButton);
            formActions.appendChild(cancelButton);
            
            form.appendChild(formActions);
            
            // 添加提交事件
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                
                // 收集表单数据
                const formData = new FormData(form);
                const formValues = {};
                
                fields.forEach(field => {
                    if (field.type === 'checkbox') {
                        formValues[field.name] = form.elements[field.name].checked;
                    } else if (field.type === 'file') {
                        const files = form.elements[field.name].files;
                        if (files.length > 0) {
                            formValues[field.name] = field.multiple ? files : files[0];
                        }
                    } else if (field.type === 'number') {
                        const value = form.elements[field.name].value;
                        formValues[field.name] = value ? parseFloat(value) : null;
                    } else {
                        formValues[field.name] = formData.get(field.name);
                    }
                });
                
                // 调用提交处理函数
                submitHandler(formValues, form);
            });
            
            // 添加取消按钮事件
            cancelButton.addEventListener('click', () => {
                // 查找对话框并关闭
                const dialog = form.closest('.dialog');
                if (dialog && dialog.querySelector('.dialog-close')) {
                    dialog.querySelector('.dialog-close').click();
                }
            });
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
    createDialog: function(title, content, width = '500px') {
        // 创建蒙层
        const overlay = document.createElement('div');
        overlay.className = 'dialog-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        `;
        
        // 创建对话框
        const dialog = document.createElement('div');
        dialog.className = 'dialog';
        dialog.style.cssText = `
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            width: ${width};
            max-width: 95%;
            max-height: 90vh;
            display: flex;
            flex-direction: column;
            animation: dialogFadeIn 0.3s;
        `;
        
        // 创建对话框头部
        const dialogHeader = document.createElement('div');
        dialogHeader.className = 'dialog-header';
        dialogHeader.style.cssText = `
            padding: 15px 20px;
            border-bottom: 1px solid #e9ecef;
            display: flex;
            justify-content: space-between;
            align-items: center;
        `;
        
        // 创建标题
        const dialogTitle = document.createElement('h5');
        dialogTitle.className = 'dialog-title';
        dialogTitle.textContent = title;
        dialogTitle.style.cssText = `
            margin: 0;
            font-weight: 600;
        `;
        
        // 创建关闭按钮
        const closeButton = document.createElement('button');
        closeButton.className = 'dialog-close';
        closeButton.innerHTML = '&times;';
        closeButton.style.cssText = `
            background: none;
            border: none;
            font-size: 1.5rem;
            cursor: pointer;
            padding: 0;
            line-height: 1;
            color: #6c757d;
        `;
        
        dialogHeader.appendChild(dialogTitle);
        dialogHeader.appendChild(closeButton);
        
        // 创建对话框主体
        const dialogBody = document.createElement('div');
        dialogBody.className = 'dialog-body';
        dialogBody.style.cssText = `
            padding: 20px;
            overflow-y: auto;
        `;
        
        // 设置内容
        if (typeof content === 'string') {
            dialogBody.innerHTML = content;
        } else {
            dialogBody.appendChild(content);
        }
        
        // 将所有元素添加到对话框中
        dialog.appendChild(dialogHeader);
        dialog.appendChild(dialogBody);
        
        // 将对话框添加到蒙层中
        overlay.appendChild(dialog);
        
        // 将对话框添加到body中
        document.body.appendChild(overlay);
        
        // 添加ESC键关闭事件
        const keyHandler = (e) => {
            if (e.key === 'Escape') {
                close();
            }
        };
        document.addEventListener('keydown', keyHandler);
        
        // 添加点击蒙层关闭事件
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                close();
            }
        });
        
        // 添加关闭按钮事件
        closeButton.addEventListener('click', close);
        
        // 关闭对话框函数
        function close() {
            dialog.style.animation = 'dialogFadeOut 0.3s';
            overlay.style.animation = 'dialogOverlayFadeOut 0.3s';
            
            setTimeout(() => {
                document.body.removeChild(overlay);
                document.removeEventListener('keydown', keyHandler);
            }, 300);
        }
        
        // 添加动画样式
        const style = document.createElement('style');
        style.textContent = `
            @keyframes dialogFadeIn {
                from { opacity: 0; transform: translateY(-20px); }
                to { opacity: 1; transform: translateY(0); }
            }
            @keyframes dialogFadeOut {
                from { opacity: 1; transform: translateY(0); }
                to { opacity: 0; transform: translateY(-20px); }
            }
            @keyframes dialogOverlayFadeOut {
                from { opacity: 1; }
                to { opacity: 0; }
            }
        `;
        document.head.appendChild(style);
        
        // 返回对话框对象
        return {
            overlay,
            dialog,
            body: dialogBody,
            title: dialogTitle,
            
            // 关闭方法
            close: close,
            
            // 设置标题方法
            setTitle: function(newTitle) {
                dialogTitle.textContent = newTitle;
            },
            
            // 设置内容方法
            setContent: function(newContent) {
                dialogBody.innerHTML = '';
                if (typeof newContent === 'string') {
                    dialogBody.innerHTML = newContent;
                } else {
                    dialogBody.appendChild(newContent);
                }
            }
        };
    },
    
    /**
     * 渲染分页
     * @param {string} containerId 容器ID
     * @param {number} currentPage 当前页
     * @param {number} totalPages 总页数
     * @param {Function} callback 页码点击回调
     */
    renderPagination: function(containerId, currentPage, totalPages, callback) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        let html = '<div class="pagination-container">';
        
        // 上一页按钮
        html += `<button class="page-btn prev ${currentPage === 1 ? 'disabled' : ''}" ${currentPage === 1 ? 'disabled' : ''}>
            <i class="bi bi-chevron-left"></i>
        </button>`;
        
        // 确定显示的页码范围
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);
        
        // 调整startPage保证显示5个页码（如果有足够的页数）
        if (endPage - startPage < 4 && totalPages > 5) {
            startPage = Math.max(1, endPage - 4);
        }
        
        // 第一页
        if (startPage > 1) {
            html += `<button class="page-btn" data-page="1">1</button>`;
            
            // 显示省略号
            if (startPage > 2) {
                html += `<span class="page-ellipsis">...</span>`;
            }
        }
        
        // 页码按钮
        for (let i = startPage; i <= endPage; i++) {
            html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" data-page="${i}">${i}</button>`;
        }
        
        // 最后一页
        if (endPage < totalPages) {
            // 显示省略号
            if (endPage < totalPages - 1) {
                html += `<span class="page-ellipsis">...</span>`;
            }
            
            html += `<button class="page-btn" data-page="${totalPages}">${totalPages}</button>`;
        }
        
        // 下一页按钮
        html += `<button class="page-btn next ${currentPage === totalPages ? 'disabled' : ''}" ${currentPage === totalPages ? 'disabled' : ''}>
            <i class="bi bi-chevron-right"></i>
        </button>`;
        
        html += '</div>';
        
        container.innerHTML = html;
        
        // 添加点击事件
        const pageButtons = container.querySelectorAll('.page-btn[data-page]');
        pageButtons.forEach(button => {
            button.addEventListener('click', function() {
                const page = parseInt(this.getAttribute('data-page'));
                if (callback && typeof callback === 'function') {
                    callback(page);
                }
            });
        });
        
        // 上一页按钮
        const prevButton = container.querySelector('.page-btn.prev');
        if (prevButton) {
            prevButton.addEventListener('click', function() {
                if (currentPage > 1 && callback && typeof callback === 'function') {
                    callback(currentPage - 1);
                }
            });
        }
        
        // 下一页按钮
        const nextButton = container.querySelector('.page-btn.next');
        if (nextButton) {
            nextButton.addEventListener('click', function() {
                if (currentPage < totalPages && callback && typeof callback === 'function') {
                    callback(currentPage + 1);
                }
            });
        }
    },
    
    /**
     * 格式化日期时间
     * @param {string|number|Date} timestamp 时间戳或日期对象
     * @param {string} format 格式化模式：'date', 'time', 'datetime', 'fromNow'
     * @returns {string} 格式化后的日期时间
     */
    formatDateTime: function(timestamp, format = 'datetime') {
        if (!timestamp) return '';
        
        const date = timestamp instanceof Date ? timestamp : new Date(timestamp);
        
        // 格式化日期部分
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        
        // 格式化时间部分
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        
        // 根据不同的格式返回
        switch (format) {
            case 'date':
                return `${year}-${month}-${day}`;
            case 'time':
                return `${hours}:${minutes}`;
            case 'datetime':
                return `${year}-${month}-${day} ${hours}:${minutes}`;
            case 'full':
                return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
            case 'fromNow':
                return this.timeAgo(date);
            default:
                return `${year}-${month}-${day}`;
        }
    },
    
    /**
     * 计算时间差（多久之前）
     * @param {Date} date 日期对象
     * @returns {string} 多久之前
     */
    timeAgo: function(date) {
        const now = new Date();
        const diff = Math.floor((now - date) / 1000); // 时间差（秒）
        
        if (diff < 60) {
            return `${diff}秒前`;
        } else if (diff < 3600) {
            return `${Math.floor(diff / 60)}分钟前`;
        } else if (diff < 86400) {
            return `${Math.floor(diff / 3600)}小时前`;
        } else if (diff < 604800) {
            return `${Math.floor(diff / 86400)}天前`;
        } else if (diff < 2592000) {
            return `${Math.floor(diff / 604800)}周前`;
        } else if (diff < 31536000) {
            return `${Math.floor(diff / 2592000)}个月前`;
        } else {
            return `${Math.floor(diff / 31536000)}年前`;
        }
    },
    
    /**
     * 格式化文件大小
     * @param {number} bytes 文件大小（字节）
     * @returns {string} 格式化后的大小
     */
    formatFileSize: function(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
};

// 在页面加载完成后初始化UI
document.addEventListener('DOMContentLoaded', function() {
    // 添加样式
    const style = document.createElement('style');
    style.textContent = `
        .page-btn {
            min-width: 40px;
            height: 36px;
            padding: 0 12px;
            margin: 0 4px;
            background-color: #f5f5f5;
            border: 1px solid #ddd;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .page-btn:hover {
            background-color: #e9e9e9;
        }
        
        .page-btn.active {
            background-color: #007bff;
            color: white;
            border-color: #007bff;
        }
        
        .page-btn.disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
        
        .pagination-container {
            display: flex;
            justify-content: center;
            align-items: center;
            margin: 20px 0;
        }
        
        .page-ellipsis {
            margin: 0 8px;
        }
    `;
    document.head.appendChild(style);
});