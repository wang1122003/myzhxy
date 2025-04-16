/**
 * 错误处理工具类
 * 用于统一处理前端API调用错误和页面错误
 */
const ErrorHandler = {
    // 存储全局错误回调
    callbacks: {},
    
    /**
     * 初始化全局AJAX错误处理
     */
    init() {
        // 设置全局AJAX错误处理
        if (window.$ && $.ajaxSetup) {
            $.ajaxSetup({
                error: (xhr, status, error) => {
                    this.handleApiError(xhr, status, error);
                }
            });
        }
        
        // 添加全局未捕获错误处理
        window.addEventListener('error', (event) => {
            console.error('全局错误:', event.error);
            if (event.error && this.callbacks['global']) {
                this.callbacks['global'](event.error);
            }
        });
        
        // 添加Promise未捕获错误处理
        window.addEventListener('unhandledrejection', (event) => {
            console.error('未处理的Promise错误:', event.reason);
            if (event.reason && this.callbacks['promise']) {
                this.callbacks['promise'](event.reason);
            }
        });
    },
    
    /**
     * 注册错误回调
     * @param {string} type 错误类型: 'api', 'global', 'promise'
     * @param {function} callback 回调函数
     */
    registerCallback(type, callback) {
        if (typeof callback === 'function') {
            this.callbacks[type] = callback;
        }
    },
    
    /**
     * 处理API错误
     * @param {Object} xhr AJAX请求对象
     * @param {string} status 错误状态
     * @param {string} error 错误信息
     */
    handleApiError(xhr, status, error) {
        console.error('API错误:', status, error);
        
        let errorMessage = '未知错误';
        let errorTitle = '请求失败';
        let errorCode = xhr.status || 500;
        let errorDetail = '';
        
        // 尝试解析错误响应
        try {
            if (xhr.responseJSON) {
                errorMessage = xhr.responseJSON.message || errorMessage;
                errorDetail = xhr.responseJSON.detail || '';
            } else if (xhr.responseText) {
                const response = JSON.parse(xhr.responseText);
                errorMessage = response.message || errorMessage;
                errorDetail = response.detail || '';
            }
        } catch (e) {
            console.warn('无法解析错误响应:', e);
        }
        
        // 处理常见HTTP错误状态码
        switch (xhr.status) {
            case 400:
                errorTitle = '请求无效';
                break;
            case 401:
                errorTitle = '未授权访问';
                this.handleUnauthorized();
                break;
            case 403:
                errorTitle = '禁止访问';
                break;
            case 404:
                errorTitle = '资源不存在';
                break;
            case 500:
                errorTitle = '服务器内部错误';
                break;
            case 502:
                errorTitle = '网关错误';
                break;
            case 503:
                errorTitle = '服务不可用';
                break;
            case 504:
                errorTitle = '网关超时';
                break;
        }
        
        // 调用自定义API错误回调
        if (this.callbacks['api']) {
            this.callbacks['api'](errorCode, errorTitle, errorMessage, errorDetail);
        }
        
        // 默认错误处理方式 - 显示错误信息或跳转到错误页面
        if (xhr.status === 401) {
            // 未登录错误特殊处理，跳转到登录页面
            this.redirectToLogin();
        } else if (xhr.status >= 500) {
            // 服务器错误，显示错误页面
            this.showErrorPage(errorCode, errorTitle, errorMessage, errorDetail);
        } else {
            // 客户端错误，显示消息提示
            this.showErrorMessage(errorTitle, errorMessage);
        }
    },
    
    /**
     * 处理未授权错误 (401)
     */
    handleUnauthorized() {
        // 清除登录信息
        localStorage.removeItem('userInfo');
        localStorage.removeItem('token');
        
        // 等待片刻后跳转到登录页面
        setTimeout(() => {
            this.redirectToLogin();
        }, 1000);
    },
    
    /**
     * 重定向到登录页面
     */
    redirectToLogin() {
        window.location.href = '/campus/index.html';
    },
    
    /**
     * 显示错误页面
     * @param {number} code HTTP状态码
     * @param {string} title 错误标题
     * @param {string} message 错误消息
     * @param {string} detail 错误详情
     */
    showErrorPage(code, title, message, detail) {
        // 构建错误页面URL并跳转
        const errorPageUrl = `/campus/error.html?code=${encodeURIComponent(code)}&title=${encodeURIComponent(title)}&message=${encodeURIComponent(message)}&detail=${encodeURIComponent(detail)}`;
        window.location.href = errorPageUrl;
    },
    
    /**
     * 显示内联错误消息
     * @param {string} title 错误标题
     * @param {string} message 错误消息
     */
    showErrorMessage(title, message) {
        // 如果已经存在错误提示，则移除
        const existingError = document.getElementById('inline-error-message');
        if (existingError) {
            existingError.remove();
        }
        
        // 创建错误提示样式
        if (!document.getElementById('error-message-style')) {
            const style = document.createElement('style');
            style.id = 'error-message-style';
            style.textContent = `
                .inline-error {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    max-width: 350px;
                    background-color: white;
                    border-left: 4px solid #dc3545;
                    border-radius: 4px;
                    box-shadow: 0 5px 15px rgba(0,0,0,0.1);
                    padding: 15px;
                    z-index: 9999;
                    animation: slideIn 0.3s ease-out forwards;
                }
                
                .inline-error-title {
                    font-weight: bold;
                    margin-bottom: 5px;
                    display: flex;
                    justify-content: space-between;
                }
                
                .inline-error-close {
                    cursor: pointer;
                    opacity: 0.6;
                }
                
                .inline-error-close:hover {
                    opacity: 1;
                }
                
                .inline-error-message {
                    font-size: 14px;
                    color: #666;
                }
                
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                
                @keyframes slideOut {
                    from { transform: translateX(0); opacity: 1; }
                    to { transform: translateX(100%); opacity: 0; }
                }
                
                .error-exit {
                    animation: slideOut 0.3s ease-in forwards;
                }
            `;
            document.head.appendChild(style);
        }
        
        // 创建错误提示元素
        const errorEl = document.createElement('div');
        errorEl.id = 'inline-error-message';
        errorEl.className = 'inline-error';
        errorEl.innerHTML = `
            <div class="inline-error-title">
                <span>${title}</span>
                <span class="inline-error-close">×</span>
            </div>
            <div class="inline-error-message">${message}</div>
        `;
        
        // 添加到页面
        document.body.appendChild(errorEl);
        
        // 添加关闭按钮事件
        errorEl.querySelector('.inline-error-close').addEventListener('click', () => {
            this.closeErrorMessage(errorEl);
        });
        
        // 设置自动关闭
        setTimeout(() => {
            this.closeErrorMessage(errorEl);
        }, 5000);
    },
    
    /**
     * 关闭错误消息
     * @param {HTMLElement} errorEl 错误消息元素
     */
    closeErrorMessage(errorEl) {
        errorEl.classList.add('error-exit');
        setTimeout(() => {
            if (errorEl.parentNode) {
                errorEl.parentNode.removeChild(errorEl);
            }
        }, 300);
    },
    
    /**
     * 包装API调用，添加统一错误处理
     * @param {Function} apiFn API调用函数
     * @param {Object} options 选项
     * @returns {Promise} Promise对象
     */
    wrapApiCall(apiFn, options = {}) {
        return new Promise((resolve, reject) => {
            try {
                apiFn()
                    .then(resolve)
                    .catch((error) => {
                        console.error('API调用错误:', error);
                        
                        // 如果提供了自定义错误处理，则调用
                        if (options.onError && typeof options.onError === 'function') {
                            options.onError(error);
                        } else {
                            // 否则显示默认错误消息
                            this.showErrorMessage('请求失败', error.message || '发生未知错误');
                        }
                        
                        reject(error);
                    });
            } catch (error) {
                console.error('API调用异常:', error);
                this.showErrorMessage('请求异常', error.message || '发生未知错误');
                reject(error);
            }
        });
    }
};

// 初始化错误处理器
document.addEventListener('DOMContentLoaded', () => {
    ErrorHandler.init();
}); 