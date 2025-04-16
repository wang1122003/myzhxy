/**
 * 用户资料模块UI组件
 * 提供用户资料相关的UI功能
 */
const UIProfile = {
    /**
     * 初始化用户资料页面
     * @param {string} containerId 容器ID
     * @param {Object} options 选项
     */
    initProfilePage: function(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 默认选项
        const defaultOptions = {
            editable: true,
            showAvatar: true,
            userId: Auth.getUserId()
        };
        
        // 合并选项
        const mergedOptions = { ...defaultOptions, ...options };
        
        // 加载用户资料
        this.loadUserProfile(containerId, mergedOptions);
    },
    
    /**
     * 加载用户资料
     * @param {string} containerId 容器ID
     * @param {Object} options 选项
     */
    loadUserProfile: function(containerId, options) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.user.getUserProfile(options.userId)
            .then(response => {
                if (response.code === 200) {
                    const userProfile = response.data;
                    this.renderUserProfile(container, userProfile, options);
                } else {
                    container.innerHTML = `<div class="error-state">加载用户资料失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('加载用户资料失败:', error);
                container.innerHTML = '<div class="error-state">加载用户资料失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 渲染用户资料
     * @param {HTMLElement} container 容器元素
     * @param {Object} userProfile 用户资料
     * @param {Object} options 选项
     */
    renderUserProfile: function(container, userProfile, options) {
        // 创建用户资料界面结构
        let html = `
            <div class="profile-container">
                <div class="profile-header">
                    ${options.showAvatar ? `
                        <div class="profile-avatar">
                            <img src="${userProfile.avatar || 'static/images/default-avatar.png'}" alt="用户头像" class="avatar-img">
                            ${options.editable ? `
                                <button class="btn btn-sm btn-outline-primary change-avatar-btn">
                                    <i class="bi bi-camera"></i> 更换头像
                                </button>
                            ` : ''}
                        </div>
                    ` : ''}
                    <div class="profile-info">
                        <h2 class="profile-name">${userProfile.name}</h2>
                        <div class="profile-meta">
                            <span class="profile-role">${this.formatRole(userProfile.role)}</span>
                            <span class="profile-id">ID: ${userProfile.id}</span>
                        </div>
                    </div>
                    ${options.editable ? `
                        <div class="profile-actions">
                            <button class="btn btn-primary edit-profile-btn">
                                <i class="bi bi-pencil"></i> 编辑资料
                            </button>
                        </div>
                    ` : ''}
                </div>
                
                <div class="profile-body">
                    <div class="profile-section">
                        <h3 class="section-title">基本信息</h3>
                        <div class="profile-info-list">
                            <div class="info-item">
                                <div class="info-label">姓名</div>
                                <div class="info-value">${userProfile.name}</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">性别</div>
                                <div class="info-value">${this.formatGender(userProfile.gender)}</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">手机号</div>
                                <div class="info-value">${userProfile.phone || '-'}</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">邮箱</div>
                                <div class="info-value">${userProfile.email || '-'}</div>
                            </div>
                        </div>
                    </div>
                    
                    ${userProfile.role === 'student' ? `
                        <div class="profile-section">
                            <h3 class="section-title">学生信息</h3>
                            <div class="profile-info-list">
                                <div class="info-item">
                                    <div class="info-label">学号</div>
                                    <div class="info-value">${userProfile.studentNumber || '-'}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">年级</div>
                                    <div class="info-value">${userProfile.grade || '-'}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">专业</div>
                                    <div class="info-value">${userProfile.major || '-'}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">班级</div>
                                    <div class="info-value">${userProfile.className || '-'}</div>
                                </div>
                            </div>
                        </div>
                    ` : ''}
                    
                    ${userProfile.role === 'teacher' ? `
                        <div class="profile-section">
                            <h3 class="section-title">教师信息</h3>
                            <div class="profile-info-list">
                                <div class="info-item">
                                    <div class="info-label">工号</div>
                                    <div class="info-value">${userProfile.teacherNumber || '-'}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">职称</div>
                                    <div class="info-value">${userProfile.title || '-'}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">院系</div>
                                    <div class="info-value">${userProfile.department || '-'}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">研究方向</div>
                                    <div class="info-value">${userProfile.researchArea || '-'}</div>
                                </div>
                            </div>
                        </div>
                    ` : ''}
                    
                    <div class="profile-section">
                        <h3 class="section-title">个人简介</h3>
                        <div class="profile-bio">
                            ${userProfile.bio || '暂无简介'}
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        container.innerHTML = html;
        
        // 绑定事件
        if (options.editable) {
            // 编辑资料按钮
            const editProfileBtn = container.querySelector('.edit-profile-btn');
            if (editProfileBtn) {
                editProfileBtn.addEventListener('click', () => {
                    this.showEditProfileForm(userProfile);
                });
            }
            
            // 更换头像按钮
            const changeAvatarBtn = container.querySelector('.change-avatar-btn');
            if (changeAvatarBtn) {
                changeAvatarBtn.addEventListener('click', () => {
                    this.showChangeAvatarDialog();
                });
            }
        }
    },
    
    /**
     * 显示编辑资料表单
     * @param {Object} userProfile 用户资料
     */
    showEditProfileForm: function(userProfile) {
        // 基础字段
        const baseFields = [
            { name: 'name', label: '姓名', type: 'text', required: true, value: userProfile.name },
            { name: 'gender', label: '性别', type: 'select', options: [
                { value: 'male', label: '男' },
                { value: 'female', label: '女' },
                { value: 'other', label: '其他' }
            ], value: userProfile.gender },
            { name: 'phone', label: '手机号', type: 'tel', value: userProfile.phone },
            { name: 'email', label: '邮箱', type: 'email', value: userProfile.email },
            { name: 'bio', label: '个人简介', type: 'textarea', rows: 4, value: userProfile.bio }
        ];
        
        // 根据角色添加额外字段
        let fields = [...baseFields];
        
        if (userProfile.role === 'student') {
            fields = fields.concat([
                { name: 'studentNumber', label: '学号', type: 'text', value: userProfile.studentNumber },
                { name: 'grade', label: '年级', type: 'text', value: userProfile.grade },
                { name: 'major', label: '专业', type: 'text', value: userProfile.major },
                { name: 'className', label: '班级', type: 'text', value: userProfile.className }
            ]);
        } else if (userProfile.role === 'teacher') {
            fields = fields.concat([
                { name: 'teacherNumber', label: '工号', type: 'text', value: userProfile.teacherNumber },
                { name: 'title', label: '职称', type: 'text', value: userProfile.title },
                { name: 'department', label: '院系', type: 'text', value: userProfile.department },
                { name: 'researchArea', label: '研究方向', type: 'text', value: userProfile.researchArea }
            ]);
        }
        
        // 创建表单
        const form = UI.createForm(fields, {}, (formData) => {
            UI.showLoading();
            
            // 添加ID和角色
            formData.id = userProfile.id;
            formData.role = userProfile.role;
            
            API.user.updateUserProfile(formData)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('更新成功', 'success');
                        dialog.close();
                        
                        // 刷新用户资料
                        this.loadUserProfile('profileContainer', { 
                            userId: userProfile.id,
                            editable: true,
                            showAvatar: true
                        });
                    } else {
                        UI.showMessage(response.message || '更新失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('更新用户资料失败:', error);
                    UI.showMessage('更新失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        });
        
        // 创建对话框
        const dialog = UI.createDialog('编辑资料', form, '700px');
    },
    
    /**
     * 显示更换头像对话框
     */
    showChangeAvatarDialog: function() {
        // 创建对话框内容
        let content = `
            <div class="avatar-upload-container">
                <div class="avatar-preview">
                    <img src="${Auth.getAvatar() || 'static/images/default-avatar.png'}" alt="头像预览" id="avatarPreview">
                </div>
                <div class="avatar-upload-controls">
                    <input type="file" id="avatarFileInput" accept="image/*" style="display: none;">
                    <button class="btn btn-primary" id="selectFileBtn">
                        <i class="bi bi-file-earmark-image"></i> 选择图片
                    </button>
                    <p class="help-text">支持jpg、png格式，文件小于2MB</p>
                </div>
            </div>
        `;
        
        // 创建对话框
        const dialog = UI.createDialog('更换头像', content, '400px', {
            confirmText: '上传',
            cancelText: '取消',
            onConfirm: () => {
                const fileInput = dialog.body.querySelector('#avatarFileInput');
                if (fileInput.files.length === 0) {
                    UI.showMessage('请选择图片', 'warning');
                    return false; // 不关闭对话框
                }
                
                this.uploadAvatar(fileInput.files[0]);
                return true; // 可以关闭对话框
            }
        });
        
        // 绑定选择文件事件
        const selectFileBtn = dialog.body.querySelector('#selectFileBtn');
        const fileInput = dialog.body.querySelector('#avatarFileInput');
        const avatarPreview = dialog.body.querySelector('#avatarPreview');
        
        selectFileBtn.addEventListener('click', () => {
            fileInput.click();
        });
        
        fileInput.addEventListener('change', (e) => {
            if (e.target.files && e.target.files[0]) {
                const file = e.target.files[0];
                
                // 检查文件类型和大小
                if (!file.type.match('image.*')) {
                    UI.showMessage('请选择图片文件', 'warning');
                    return;
                }
                
                if (file.size > 2 * 1024 * 1024) {
                    UI.showMessage('图片大小不能超过2MB', 'warning');
                    return;
                }
                
                // 预览图片
                const reader = new FileReader();
                reader.onload = (e) => {
                    avatarPreview.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    },
    
    /**
     * 上传头像
     * @param {File} file 图片文件
     */
    uploadAvatar: function(file) {
        UI.showLoading();
        
        const formData = new FormData();
        formData.append('avatar', file);
        formData.append('userId', Auth.getUserId());
        
        API.user.uploadAvatar(formData)
            .then(response => {
                if (response.code === 200) {
                    UI.showMessage('头像更新成功', 'success');
                    
                    // 更新本地存储的头像URL
                    const avatarUrl = response.data.avatarUrl;
                    Auth.setAvatar(avatarUrl);
                    
                    // 更新页面上的头像
                    const avatarImg = document.querySelector('.profile-avatar .avatar-img');
                    if (avatarImg) {
                        avatarImg.src = avatarUrl;
                    }
                } else {
                    UI.showMessage(response.message || '上传失败', 'error');
                }
            })
            .catch(error => {
                console.error('上传头像失败:', error);
                UI.showMessage('上传失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 格式化性别
     * @param {string} gender 性别
     * @returns {string} 格式化后的性别
     */
    formatGender: function(gender) {
        const genderMap = {
            'male': '男',
            'female': '女',
            'other': '其他'
        };
        return genderMap[gender] || '-';
    },
    
    /**
     * 格式化角色
     * @param {string} role 角色
     * @returns {string} 格式化后的角色
     */
    formatRole: function(role) {
        const roleMap = {
            'admin': '管理员',
            'teacher': '教师',
            'student': '学生'
        };
        return roleMap[role] || '-';
    }
};

// 将UIProfile添加到全局UI对象中
if (typeof UI !== 'undefined') {
    UI.profile = UIProfile;
} 