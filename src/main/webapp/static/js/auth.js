/**
 * 认证工具类
 * 统一管理前端认证逻辑
 */
const Auth = {
    // Token键名
    tokenKey: 'campus_token',
    
    /**
     * 初始化认证模块
     */
    init() {
        // 验证当前页面的权限
        this.validatePageAccess();
        
        // 绑定登出事件
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.logout();
            });
        }
    },
    
    /**
     * 登录
     * @param {string} username 用户名
     * @param {string} password 密码
     * @returns {Promise} 登录结果
     */
    async login(username, password) {
        try {
            const response = await fetch('/campus/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
            });
            
            const result = await response.json();
            
            if (result.code === 200) {
                // 登录成功，保存用户信息到本地存储
                this.setToken(result.data.token);
                this.setUserInfo(result.data.user);
                return { success: true, data: result.data };
            } else {
                return { success: false, message: result.message || '登录失败' };
            }
        } catch (error) {
            console.error('登录出错:', error);
            return { success: false, message: '网络错误，请稍后再试' };
        }
    },
    
    /**
     * 登出
     */
    async logout() {
        try {
            // 可选：调用后端登出接口
            if (this.isLoggedIn()) {
                await fetch('/campus/api/users/logout', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${this.getToken()}`
                    }
                });
            }
        } catch (error) {
            console.error('登出错误:', error);
        } finally {
            // 无论请求成功与否，都清除本地存储
            this.clearUserData();
            // 跳转到登录页
            window.location.href = '/campus/index.html';
        }
    },
    
    /**
     * 保存token
     * @param {string} token JWT token
     */
    setToken(token) {
        if (!token) return;
        
        // 保存到localStorage
        localStorage.setItem(this.tokenKey, token);
        
        // 保存到cookie，便于服务端获取
        const secure = window.location.protocol === 'https:' ? '; secure' : '';
        document.cookie = `${this.tokenKey}=${token}; path=/; max-age=${24*60*60}${secure}; SameSite=Lax`;
    },
    
    /**
     * 获取token
     * @returns {string|null} JWT token
     */
    getToken() {
        return localStorage.getItem(this.tokenKey);
    },
    
    /**
     * 保存用户信息
     * @param {Object} user 用户信息
     */
    setUserInfo(user) {
        if (!user) return;
        
        localStorage.setItem('username', user.username || '');
        localStorage.setItem('userId', user.id || '');
        localStorage.setItem('userType', user.userType || '');
        localStorage.setItem('userRole', this.getUserRoleName(user.userType));
        
        // 保存完整用户对象
        localStorage.setItem('user', JSON.stringify(user));
    },
    
    /**
     * 根据用户类型获取角色名称
     * @param {number} userType 用户类型
     * @returns {string} 角色名称
     */
    getUserRoleName(userType) {
        switch (userType) {
            case 0: return 'ADMIN';
            case 1: return 'STUDENT';
            case 2: return 'TEACHER';
            default: return 'GUEST';
        }
    },
    
    /**
     * 获取当前用户信息
     * @returns {Object} 用户信息
     */
    getUserInfo() {
        const userJson = localStorage.getItem('user');
        return userJson ? JSON.parse(userJson) : null;
    },
    
    /**
     * 获取当前用户角色
     * @returns {string} 角色名称
     */
    getUserRole() {
        return localStorage.getItem('userRole') || 'GUEST';
    },
    
    /**
     * 获取当前用户类型
     * @returns {number} 用户类型
     */
    getUserType() {
        const userType = localStorage.getItem('userType');
        return userType ? parseInt(userType) : null;
    },
    
    /**
     * 验证当前页面访问权限
     */
    validatePageAccess() {
        // 获取当前路径
        const path = window.location.pathname;
        const role = this.getUserRole();
        
        // 登录页不需要验证
        if (path.endsWith('index.html') || path === '/' || path.endsWith('/')) {
            return;
        }
        
        // 检查是否已登录
        if (!this.isLoggedIn()) {
            this.redirectToLogin();
            return;
        }
        
        // 验证角色权限
        if (path.includes('/admin/') && role !== 'ADMIN') {
            this.showAccessDenied();
            return;
        }
        
        if (path.includes('/teacher/') && role !== 'TEACHER' && role !== 'ADMIN') {
            this.showAccessDenied();
            return;
        }
        
        if (path.includes('/student/') && role !== 'STUDENT' && role !== 'ADMIN') {
            this.showAccessDenied();
            return;
        }
    },
    
    /**
     * 显示无权访问提示并跳转
     */
    showAccessDenied() {
        alert('您没有权限访问此页面');
        this.redirectToLogin();
    },
    
    /**
     * 重定向到登录页
     */
    redirectToLogin() {
        window.location.href = '/campus/index.html';
    },
    
    /**
     * 清除用户数据
     */
    clearUserData() {
        // 清除localStorage
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
        localStorage.removeItem('userType');
        localStorage.removeItem('userRole');
        localStorage.removeItem('user');
        
        // 清除cookie
        document.cookie = `${this.tokenKey}=; path=/; max-age=0`;
    },
    
    /**
     * 检查是否已登录
     * @returns {boolean} 是否已登录
     */
    isLoggedIn() {
        return !!this.getToken();
    },
    
    /**
     * 检查是否是管理员
     * @returns {boolean} 是否是管理员
     */
    isAdmin() {
        return this.getUserType() === 0;
    },
    
    /**
     * 检查是否是教师
     * @returns {boolean} 是否是教师
     */
    isTeacher() {
        return this.getUserType() === 2;
    },
    
    /**
     * 检查是否是学生
     * @returns {boolean} 是否是学生
     */
    isStudent() {
        return this.getUserType() === 1;
    },
    
    /**
     * 获取验证请求头
     * @returns {Object} 请求头对象
     */
    getAuthHeaders() {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    },
    
    /**
     * 带验证的Fetch请求
     * @param {string} url 请求地址
     * @param {Object} options 请求选项
     * @returns {Promise} Fetch响应
     */
    async fetchWithAuth(url, options = {}) {
        options.headers = {
            ...options.headers,
            ...this.getAuthHeaders()
        };
        return fetch(url, options);
    }
};

// 页面加载时初始化认证模块
document.addEventListener('DOMContentLoaded', () => {
    Auth.init();
}); 