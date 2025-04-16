/**
 * API工具类
 * 处理与后端API的交互
 */
const API = {
    // 基础URL
    baseUrl: '/campus/api',
    
    // 发送请求
    async request(endpoint, method = 'GET', data = null, isFormData = false) {
        try {
            let url = this.baseUrl + endpoint;
            const options = {
                method,
                headers: {}
            };
            
            // 添加认证头
            if (Auth.isLoggedIn()) {
                options.headers['Authorization'] = 'Bearer ' + Auth.getToken();
            }
            
            // 添加请求体
            if (data) {
                if (method === 'GET') {
                    // 对于GET请求，转换为URL参数
                    const params = new URLSearchParams();
                    Object.keys(data).forEach(key => {
                        if (data[key] !== null && data[key] !== undefined) {
                        params.append(key, data[key]);
                        }
                    });
                    const queryString = params.toString();
                    if (queryString) {
                        url += (url.includes('?') ? '&' : '?') + queryString;
                    }
                } else if (isFormData) {
                    // 如果是FormData对象，直接使用
                    options.body = data;
                } else {
                    // 其他请求，JSON序列化
                    options.headers['Content-Type'] = 'application/json';
                    options.body = JSON.stringify(data);
                }
            }
            
            // 发送请求
            const response = await fetch(url, options);
            
            // 检查认证错误
            if (response.status === 401) {
                // 认证失败，清除token
                Auth.clearUserData();
                throw new Error('认证失败，请重新登录');
            }
            
            // 尝试解析为JSON
            let result;
            try {
                result = await response.json();
            } catch (e) {
                // 如果不是JSON，返回文本
                result = await response.text();
            }
            
            // 检查错误
            if (!response.ok) {
                throw new Error(result.message || response.statusText);
            }
            
            return result;
        } catch (error) {
            console.error('API请求出错:', error);
            // 处理常见错误
            if (error.message === '认证失败，请重新登录') {
                // 显示错误消息
                if (typeof UI !== 'undefined' && UI.showMessage) {
                    UI.showMessage('您的登录已过期，请重新登录', 'error');
                }
                // 延迟跳转到登录页
                setTimeout(() => {
                    window.location.href = '/campus/index.html';
                }, 1500);
            }
            throw error;
        }
    },
    
    // 登录
    async login(username, password) {
        return Auth.login(username, password);
    },
    
    // 登出
    async logout() {
        return Auth.logout();
    },
    
    // 检查权限
    async checkPermission(permission) {
        const userType = Auth.getUserType();
        
        // 未登录
        if (userType === null) {
            return false;
        }
        
        // 管理员拥有所有权限
        if (userType === 0) {
            return true;
        }
        
        // 教师权限
        if (userType === 2) {
            const teacherPermissions = ['course_manage', 'classroom_manage', 'schedule_manage', 'teacher'];
            if (permission === 'admin') {
                return false;
            }
            return teacherPermissions.includes(permission);
        }
        
        // 学生权限
        if (userType === 1) {
            const studentPermissions = ['activity_manage', 'student'];
            if (permission === 'admin' || permission === 'teacher') {
                return false;
            }
            return studentPermissions.includes(permission);
        }
        
        return false;
    },
    
    // 根据角色重定向到对应页面
    redirectToRolePage() {
        const role = Auth.getUserRole();
        let url = '/campus/index.html';
        
        if (role === 'STUDENT') {
            url = '/campus/student/';
        } else if (role === 'TEACHER') {
            url = '/campus/teacher/';
        } else if (role === 'ADMIN') {
            url = '/campus/admin/';
        }
        
        window.location.href = url;
    },
    
    // 获取用户个人空间URL
    getUserSpaceUrl() {
        const role = Auth.getUserRole();
        
        if (role === 'STUDENT') {
            return '/campus/student/';
        } else if (role === 'TEACHER') {
            return '/campus/teacher/';
        } else if (role === 'ADMIN') {
            return '/campus/admin/';
        }
        
        return '/campus/index.html';
    },
    
    // 学生模块API接口
    student: {
        // 个人信息
        getProfile: async function() {
            return API.request('/users/current');
        },
        
        updateProfile: async function(data) {
            return API.request('/users/profile', 'PUT', data);
        },
        
        changePassword: async function(currentPassword, newPassword) {
            return API.request('/users/password', 'PUT', {
                currentPassword,
                newPassword
            });
        },
        
        // 统计信息
        getStats: async function() {
            return Promise.all([
                API.request('/students/stats/courses'),
                API.request('/students/stats/activities'),
                API.request('/students/stats/files'),
                API.request('/students/stats/forum-posts')
            ]);
        },
        
        // 文件管理
        getFiles: async function(params) {
            return API.request('/files/my', 'GET', params);
        },
        
        getRecentFiles: async function(limit = 5) {
            return API.request('/files/my/recent', 'GET', { limit });
        },
        
        uploadFile: async function(fileData, type, metadata) {
            const formData = new FormData();
            formData.append('file', fileData);
            formData.append('type', type);
            
            if (metadata) {
                formData.append('metadata', JSON.stringify(metadata));
            }
            
            return API.request('/files/upload', 'POST', formData, true);
        },
        
        downloadFile: async function(fileId) {
            return API.request('/files/' + fileId + '/download');
        },
        
        deleteFile: async function(fileId) {
            return API.request('/files/' + fileId, 'DELETE');
        },
        
        searchFiles: async function(keyword, params = {}) {
            params.keyword = keyword;
            return API.request('/files/my/search', 'GET', params);
        },
        
        // 活动管理
        getAllActivities: async function(params) {
            return API.request('/activities', 'GET', params);
        },
        
        getOngoingActivities: async function(params) {
            return API.request('/activities/ongoing', 'GET', params);
        },
        
        getUpcomingActivities: async function(params) {
            return API.request('/activities/upcoming', 'GET', params);
        },
        
        getMyActivities: async function(params) {
            return API.request('/activities/my', 'GET', params);
        },
        
        getActivityById: async function(activityId) {
            return API.request('/activities/' + activityId);
        },
        
        signUpActivity: async function(activityId) {
            return API.request('/activities/' + activityId + '/signup', 'POST');
        },
        
        cancelActivitySignUp: async function(activityId) {
            return API.request('/activities/' + activityId + '/signup', 'DELETE');
        },
        
        checkActivitySignUpStatus: async function(activityId) {
            return API.request('/activities/' + activityId + '/signup/status');
        },
        
        getActivityParticipants: async function(activityId, params) {
            return API.request('/activities/' + activityId + '/participants', 'GET', params);
        },
        
        searchActivities: async function(keyword, params) {
            params = params || {};
            params.keyword = keyword;
            return API.request('/activities/search', 'GET', params);
        },
        
        // 课程和成绩
        getSchedule: async function(weekNumber) {
            return API.request('/schedules/week/' + weekNumber);
        },
        
        getTodaySchedule: async function() {
            return API.request('/schedules/today');
        },
        
        getGrades: async function(term) {
            return API.request('/grades', 'GET', { term });
        },
        
        getGradesSummary: async function() {
            return API.request('/grades/summary');
        },
        
        getGPA: async function(termId) {
            return API.request('/grades/gpa', 'GET', { termId });
        },
        
        getScoreDistribution: async function(termId) {
            return API.request('/grades/distribution', 'GET', { termId });
        },
        
        getScoreTrend: async function() {
            return API.request('/grades/trend');
        },
        
        // 课程选择
        getCourseList: async function(params) {
            return API.request('/courses', 'GET', params);
        },
        
        getMyCourses: async function() {
            return API.request('/courses/my');
        },
        
        getCourseById: async function(courseId) {
            return API.request('/courses/' + courseId);
        },
        
        selectCourse: async function(courseId) {
            return API.request('/courses/' + courseId + '/select', 'POST');
        },
        
        unselectCourse: async function(courseId) {
            return API.request('/courses/' + courseId + '/unselect', 'DELETE');
        },
        
        getCourseResources: async function(courseId) {
            return API.request('/courses/' + courseId + '/resources');
        },
        
        // 通知
        getRecentNotices: async function(limit) {
            return API.request('/notices/recent', 'GET', { limit });
        },
        
        getAllNotices: async function(params) {
            return API.request('/notices', 'GET', params);
        },
        
        getNoticeById: async function(noticeId) {
            return API.request('/notices/' + noticeId);
        },
        
        markNoticeAsRead: async function(noticeId) {
            return API.request('/notices/' + noticeId + '/read', 'POST');
        }
    },
    
    // 活动API模块
    activity: {
        // 获取活动列表
        getAll: async function(params) {
            return API.request('/activities', 'GET', params);
        },
        
        // 获取正在进行中的活动
        getOngoing: async function(params) {
            return API.request('/activities/ongoing', 'GET', params);
        },
        
        // 获取即将开始的活动
        getUpcoming: async function(params) {
            return API.request('/activities/upcoming', 'GET', params);
        },
        
        // 获取我参与的活动
        getMy: async function(params) {
            return API.request('/activities/my', 'GET', params);
        },
        
        // 获取活动详情
        getById: async function(activityId) {
            return API.request('/activities/' + activityId);
        },
        
        // 报名活动
        signUp: async function(activityId) {
            return API.request('/activities/' + activityId + '/signup', 'POST');
        },
        
        // 取消报名
        cancelSignUp: async function(activityId) {
            return API.request('/activities/' + activityId + '/signup', 'DELETE');
        },
        
        // 检查报名状态
        checkSignUpStatus: async function(activityId) {
            return API.request('/activities/' + activityId + '/signup/status');
        },
        
        // 获取活动参与者
        getParticipants: async function(activityId, params) {
            return API.request('/activities/' + activityId + '/participants', 'GET', params);
        },
        
        // 搜索活动
        search: async function(keyword, params = {}) {
            params.keyword = keyword;
            return API.request('/activities/search', 'GET', params);
        }
    },
    
    // 论坛API模块
    forum: {
        // 获取论坛分类
        getCategories: async function() {
            return API.request('/forum/categories');
        },
        
        // 获取话题列表
        getTopics: async function(params = {}) {
            return API.request('/forum/topics', 'GET', params);
        },
        
        // 获取话题详情
        getTopicById: async function(topicId) {
            return API.request('/forum/topics/' + topicId);
        },
        
        // 创建新话题
        createTopic: async function(data) {
            if (!data || !data.title || !data.content) {
                throw new Error('标题和内容不能为空');
            }
            return API.request('/forum/topics', 'POST', data);
        },
        
        // 更新话题
        updateTopic: async function(topicId, data) {
            if (!topicId) {
                throw new Error('话题ID不能为空');
            }
            return API.request('/forum/topics/' + topicId, 'PUT', data);
        },
        
        // 删除话题
        deleteTopic: async function(topicId) {
            if (!topicId) {
                throw new Error('话题ID不能为空');
            }
            return API.request('/forum/topics/' + topicId, 'DELETE');
        },
        
        // 获取话题评论
        getComments: async function(topicId, params = {}) {
            if (!topicId) {
                throw new Error('话题ID不能为空');
            }
            return API.request('/forum/topics/' + topicId + '/comments', 'GET', params);
        },
        
        // 添加评论
        addComment: async function(topicId, content) {
            if (!topicId || !content) {
                throw new Error('话题ID和评论内容不能为空');
            }
            return API.request('/forum/topics/' + topicId + '/comments', 'POST', { content });
        },
        
        // 删除评论
        deleteComment: async function(commentId) {
            if (!commentId) {
                throw new Error('评论ID不能为空');
            }
            return API.request('/forum/comments/' + commentId, 'DELETE');
        },
        
        // 点赞/取消点赞
        toggleLike: async function(type, contentId, isLike = true) {
            if (!type || !contentId) {
                throw new Error('类型和内容ID不能为空');
            }
            if (isLike) {
                return API.request('/forum/' + type + '/' + contentId + '/like', 'POST');
            } else {
                return API.request('/forum/' + type + '/' + contentId + '/like', 'DELETE');
            }
        },
        
        // 获取热门话题
        getHotTopics: async function(limit = 5) {
            return API.request('/forum/topics/hot', 'GET', { limit });
        },
        
        // 获取我的话题
        getMyTopics: async function(params = {}) {
            return API.request('/forum/topics/my', 'GET', params);
        },
        
        // 获取我的评论
        getMyComments: async function(params = {}) {
            return API.request('/forum/comments/my', 'GET', params);
        },
        
        // 搜索论坛
        search: async function(keyword, params = {}) {
            if (!keyword) {
                throw new Error('搜索关键词不能为空');
            }
            params.keyword = keyword;
            return API.request('/forum/search', 'GET', params);
        }
    },
    
    // 通用用户API
    user: {
        // 获取用户信息
        getById: async function(userId) {
            return API.request('/users/' + userId);
        },
        
        // 通过用户名获取用户
        getByUsername: async function(username) {
            return API.request('/users/username/' + username);
        }
    },
    
    // 通知API
    notice: {
        // 获取所有通知
        getAll: async function(page = 1, size = 10, type = null) {
            let params = { page: page-1, size: size };
            if (type) params.type = type;
            return API.request('/notices', 'GET', params);
        },
        
        // 获取通知详情
        getById: async function(noticeId) {
            return API.request('/notices/' + noticeId);
        },
        
        // 获取最近通知
        getRecent: async function(limit = 5) {
            return API.request('/notices/recent', 'GET', { limit });
        }
    },
    
    // 教师模块API接口
    teacher: {
        // 获取教师课程列表
        getCourses: async function(page = 1, pageSize = 10) {
            return API.request('/teacher/courses', 'GET', {
                page: page,
                pageSize: pageSize
            });
        },
        
        // 获取课程详情
        getCourseDetail: async function(courseId) {
            if (!courseId) {
                throw new Error('课程ID不能为空');
            }
            
            return API.request('/teacher/courses/' + courseId);
        },
        
        // 创建新课程
        createCourse: async function(courseData) {
            if (!courseData || !courseData.name || !courseData.code) {
                throw new Error('课程名称和编号不能为空');
            }
            
            return API.request('/teacher/courses', 'POST', courseData);
        },
        
        // 更新课程信息
        updateCourse: async function(courseId, courseData) {
            if (!courseId) {
                throw new Error('课程ID不能为空');
            }
            
            return API.request('/teacher/courses/' + courseId, 'PUT', courseData);
        },
        
        // 获取课程学生列表
        getCourseStudents: async function(courseId) {
            if (!courseId) {
                throw new Error('课程ID不能为空');
            }
            
            return API.request('/teacher/courses/' + courseId + '/students');
        },
        
        // 录入学生成绩
        enterStudentScore: async function(scoreData) {
            if (!scoreData || !scoreData.courseId || !scoreData.studentId) {
                throw new Error('课程ID和学生ID不能为空');
            }
            
            return API.request('/teacher/scores', 'POST', scoreData);
        },
        
        // 获取教师课表
        getSchedule: async function() {
            return API.request('/teacher/schedules');
        },
        
        // 更新教师个人信息
        updateProfile: async function(profileData) {
            return API.request('/teacher/profile', 'PUT', profileData);
        },
        
        // 获取考勤统计
        getAttendanceStats: async function(courseId) {
            if (!courseId) {
                throw new Error('课程ID不能为空');
            }
            
            return API.request('/teacher/courses/' + courseId + '/attendance');
        },
        
        // 记录考勤
        recordAttendance: async function(attendanceData) {
            if (!attendanceData || !attendanceData.courseId) {
                throw new Error('课程ID不能为空');
            }
            
            return API.request('/teacher/attendance', 'POST', attendanceData);
        },
        
        // 获取教师信息
        getTeacherInfo: async function() {
            return API.request('/teacher/profile');
        }
    },
    
    // 管理员模块API接口
    admin: {
        // 用户管理
        getUsers: async function(params = {}) {
            return API.request('/admin/users', 'GET', params);
        },
        
        getUserById: async function(userId) {
            if (!userId) {
                throw new Error('用户ID不能为空');
            }
            return API.request('/admin/users/' + userId);
        },
        
        createUser: async function(userData) {
            if (!userData || !userData.username || !userData.password) {
                throw new Error('用户名和密码不能为空');
            }
            return API.request('/admin/users', 'POST', userData);
        },
        
        updateUser: async function(userId, userData) {
            if (!userId) {
                throw new Error('用户ID不能为空');
            }
            return API.request('/admin/users/' + userId, 'PUT', userData);
        },
        
        deleteUser: async function(userId) {
            if (!userId) {
                throw new Error('用户ID不能为空');
            }
            return API.request('/admin/users/' + userId, 'DELETE');
        },
        
        resetUserPassword: async function(userId, newPassword) {
            if (!userId || !newPassword) {
                throw new Error('用户ID和新密码不能为空');
            }
            return API.request('/admin/users/' + userId + '/reset-password', 'POST', { newPassword });
        },
        
        // 教室管理
        getClassrooms: async function(params = {}) {
            return API.request('/classrooms', 'GET', params);
        },
        
        getClassroomById: async function(classroomId) {
            if (!classroomId) {
                throw new Error('教室ID不能为空');
            }
            return API.request('/classrooms/' + classroomId);
        },
        
        createClassroom: async function(classroomData) {
            if (!classroomData || !classroomData.name || !classroomData.capacity) {
                throw new Error('教室名称和容量不能为空');
            }
            return API.request('/classrooms', 'POST', classroomData);
        },
        
        updateClassroom: async function(classroomId, classroomData) {
            if (!classroomId) {
                throw new Error('教室ID不能为空');
            }
            return API.request('/classrooms/' + classroomId, 'PUT', classroomData);
        },
        
        deleteClassroom: async function(classroomId) {
            if (!classroomId) {
                throw new Error('教室ID不能为空');
            }
            return API.request('/classrooms/' + classroomId, 'DELETE');
        },
        
        // 课程管理
        getCourses: async function(params = {}) {
            return API.request('/courses', 'GET', params);
        },
        
        getCourseById: async function(courseId) {
            if (!courseId) {
                throw new Error('课程ID不能为空');
            }
            return API.request('/courses/' + courseId);
        },
        
        createCourse: async function(courseData) {
            if (!courseData || !courseData.name || !courseData.code) {
                throw new Error('课程名称和编号不能为空');
            }
            return API.request('/courses', 'POST', courseData);
        },
        
        updateCourse: async function(courseId, courseData) {
            if (!courseId) {
                throw new Error('课程ID不能为空');
            }
            return API.request('/courses/' + courseId, 'PUT', courseData);
        },
        
        deleteCourse: async function(courseId) {
            if (!courseId) {
                throw new Error('课程ID不能为空');
            }
            return API.request('/courses/' + courseId, 'DELETE');
        },
        
        // 通知管理
        getNotices: async function(params = {}) {
            return API.request('/notices', 'GET', params);
        },
        
        getNoticeById: async function(noticeId) {
            if (!noticeId) {
                throw new Error('通知ID不能为空');
            }
            return API.request('/notices/' + noticeId);
        },
        
        createNotice: async function(noticeData) {
            if (!noticeData || !noticeData.title || !noticeData.content) {
                throw new Error('通知标题和内容不能为空');
            }
            return API.request('/notices', 'POST', noticeData);
        },
        
        updateNotice: async function(noticeId, noticeData) {
            if (!noticeId) {
                throw new Error('通知ID不能为空');
            }
            return API.request('/notices/' + noticeId, 'PUT', noticeData);
        },
        
        deleteNotice: async function(noticeId) {
            if (!noticeId) {
                throw new Error('通知ID不能为空');
            }
            return API.request('/notices/' + noticeId, 'DELETE');
        },
        
        publishNotice: async function(noticeId) {
            if (!noticeId) {
                throw new Error('通知ID不能为空');
            }
            return API.request('/notices/' + noticeId + '/publish', 'POST');
        },
        
        unpublishNotice: async function(noticeId) {
            if (!noticeId) {
                throw new Error('通知ID不能为空');
            }
            return API.request('/notices/' + noticeId + '/unpublish', 'POST');
        },
        
        // 系统设置
        getSystemSettings: async function() {
            return API.request('/admin/settings');
        },
        
        updateSystemSettings: async function(settingsData) {
            return API.request('/admin/settings', 'PUT', settingsData);
        },
        
        // 统计信息
        getDashboardStats: async function() {
            return API.request('/admin/dashboard/stats');
        },
        
        getUserStats: async function() {
            return API.request('/admin/stats/users');
        },
        
        getCourseStats: async function() {
            return API.request('/admin/stats/courses');
        },
        
        getActivityStats: async function() {
            return API.request('/admin/stats/activities');
        }
    },
    
    // 路由和权限拦截器
    router: {
        // 路由映射表：路径 -> 所需权限
        routes: {
            '/admin/': 'admin',
            '/admin/index.html': 'admin',
            '/teacher/': 'teacher',
            '/teacher/index.html': 'teacher',
            '/student/': 'student',
            '/student/index.html': 'student',
            '/forum.html': 'user' // 通用权限，只要登录即可
        },
        
        /**
         * 初始化路由
         */
        init() {
            // 获取当前路径
            const path = window.location.pathname;
            const relativePath = path.startsWith('/campus') ? path.substring('/campus'.length) : path;
            
            // 检查是否需要权限
            const requiredPermission = this.getRequiredPermission(relativePath);
            if (requiredPermission) {
                this.checkAccess(requiredPermission);
            }
        },
        
        /**
         * 获取访问路径所需的权限
         * @param {string} path 路径
         * @returns {string|null} 所需权限
         */
        getRequiredPermission(path) {
            // 遍历路由表，检查路径是否匹配
            for (let route in this.routes) {
                if (path.startsWith(route)) {
                    return this.routes[route];
                }
            }
            return null;
        },
        
        /**
         * 检查用户是否有权限访问
         * @param {string} permission 所需权限
         */
        async checkAccess(permission) {
            // 如果未登录，直接重定向到登录页
            if (!API.isLoggedIn()) {
                window.location.href = '/campus/index.html';
                return;
            }
            
            // 检查特定权限
            if (permission === 'user') {
                // 只要登录即有权限
                return;
            }
            
            // 检查具体角色权限
            const hasPermission = await API.checkPermission(permission);
            if (!hasPermission) {
                // 显示错误消息
                if (typeof UI !== 'undefined' && UI.showMessage) {
                    UI.showMessage('您没有权限访问此页面', 'error');
                } else {
                    alert('您没有权限访问此页面');
                }
                
                // 延迟重定向到首页
                setTimeout(() => {
                    window.location.href = '/campus/index.html';
                }, 1500);
            }
        }
    }
};

// 初始化API
API.init();
// 初始化路由
API.router.init();

// 为了兼容旧代码，保留简写方式
// 但推荐使用新的命名空间化的API调用
// API.activity = API.student; 
// 上面这行被替换为更清晰的API结构 