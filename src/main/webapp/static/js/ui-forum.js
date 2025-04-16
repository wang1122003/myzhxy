/**
 * 论坛模块UI组件
 * 提供论坛相关的UI功能
 */
const UIForum = {
    /**
     * 初始化论坛
     * @param {string} containerId 容器ID
     * @param {Object} options 选项
     */
    initForum: function(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 默认选项
        const defaultOptions = {
            filter: 'all',
            page: 1,
            pageSize: 10,
            searchEnabled: true,
            createEnabled: true
        };
        
        // 合并选项
        const mergedOptions = { ...defaultOptions, ...options };
        
        // 创建论坛界面结构
        let html = `
            <div class="forum-container">
                <div class="forum-header">
                    <div class="forum-filters">
                        <div class="btn-group" role="group">
                            <button type="button" class="btn ${mergedOptions.filter === 'all' ? 'btn-primary' : 'btn-outline-primary'}" data-filter="all">全部</button>
                            <button type="button" class="btn ${mergedOptions.filter === 'hot' ? 'btn-primary' : 'btn-outline-primary'}" data-filter="hot">热门</button>
                            <button type="button" class="btn ${mergedOptions.filter === 'essence' ? 'btn-primary' : 'btn-outline-primary'}" data-filter="essence">精华</button>
                            ${Auth.isLoggedIn() ? `<button type="button" class="btn ${mergedOptions.filter === 'my' ? 'btn-primary' : 'btn-outline-primary'}" data-filter="my">我的发布</button>` : ''}
                        </div>
                    </div>
                    <div class="forum-actions">
                        ${mergedOptions.searchEnabled ? `
                            <div class="search-box">
                                <input type="text" class="form-control" id="forumSearchInput" placeholder="搜索帖子...">
                                <button class="btn btn-outline-primary" id="forumSearchBtn"><i class="bi bi-search"></i></button>
                            </div>
                        ` : ''}
                        ${mergedOptions.createEnabled && Auth.isLoggedIn() ? `
                            <button class="btn btn-primary" id="createPostBtn"><i class="bi bi-plus-circle"></i> 发布帖子</button>
                        ` : ''}
                    </div>
                </div>
                
                <div class="forum-content">
                    <div id="postsContainer" class="posts-container"></div>
                </div>
                
                <div class="forum-footer">
                    <div id="forumPagination" class="pagination-container"></div>
                </div>
            </div>
        `;
        
        container.innerHTML = html;
        
        // 添加事件监听
        const filterButtons = container.querySelectorAll('.forum-filters button');
        filterButtons.forEach(button => {
            button.addEventListener('click', () => {
                const filter = button.getAttribute('data-filter');
                
                // 更新按钮状态
                filterButtons.forEach(btn => {
                    btn.classList.remove('btn-primary');
                    btn.classList.add('btn-outline-primary');
                });
                button.classList.remove('btn-outline-primary');
                button.classList.add('btn-primary');
                
                // 加载帖子
                this.loadPosts('postsContainer', filter, 1, mergedOptions.pageSize);
            });
        });
        
        // 搜索功能
        if (mergedOptions.searchEnabled) {
            const searchInput = container.querySelector('#forumSearchInput');
            const searchBtn = container.querySelector('#forumSearchBtn');
            
            searchBtn.addEventListener('click', () => {
                const keyword = searchInput.value.trim();
                if (keyword) {
                    this.searchPosts('postsContainer', keyword, 1, mergedOptions.pageSize);
                } else {
                    this.loadPosts('postsContainer', mergedOptions.filter, 1, mergedOptions.pageSize);
                }
            });
            
            // 回车搜索
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    searchBtn.click();
                }
            });
        }
        
        // 创建帖子按钮
        if (mergedOptions.createEnabled && Auth.isLoggedIn()) {
            const createBtn = container.querySelector('#createPostBtn');
            createBtn.addEventListener('click', () => {
                this.showCreatePostForm();
            });
        }
        
        // 初始加载帖子
        this.loadPosts('postsContainer', mergedOptions.filter, mergedOptions.page, mergedOptions.pageSize);
    },
    
    /**
     * 加载帖子
     * @param {string} containerId 容器ID
     * @param {string} filter 过滤条件：all, hot, essence, my
     * @param {number} page 页码
     * @param {number} pageSize 每页数量
     */
    loadPosts: function(containerId, filter = 'all', page = 1, pageSize = 10) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        // 确定API方法
        let apiMethod;
        switch (filter) {
            case 'hot':
                apiMethod = API.forum.getHotPosts;
                break;
            case 'essence':
                apiMethod = API.forum.getEssencePosts;
                break;
            case 'my':
                apiMethod = API.forum.getMyPosts;
                break;
            default:
                apiMethod = API.forum.getAllPosts;
        }
        
        apiMethod({ page, pageSize })
            .then(response => {
                if (response.code === 200) {
                    const result = response.data;
                    
                    if (!result.items || result.items.length === 0) {
                        container.innerHTML = '<div class="empty-state">暂无帖子</div>';
                        // 清空分页
                        const paginationContainer = document.getElementById('forumPagination');
                        if (paginationContainer) {
                            paginationContainer.innerHTML = '';
                        }
                        return;
                    }
                    
                    // 渲染帖子列表
                    this.renderPostList(container, result.items);
                    
                    // 渲染分页
                    const totalPages = Math.ceil(result.total / pageSize);
                    if (totalPages > 1) {
                        UI.renderPagination('forumPagination', page, totalPages, (newPage) => {
                            this.loadPosts(containerId, filter, newPage, pageSize);
                        });
                    } else {
                        // 清空分页
                        const paginationContainer = document.getElementById('forumPagination');
                        if (paginationContainer) {
                            paginationContainer.innerHTML = '';
                        }
                    }
                } else {
                    container.innerHTML = `<div class="error-state">加载失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('加载帖子失败:', error);
                container.innerHTML = '<div class="error-state">加载失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 搜索帖子
     * @param {string} containerId 容器ID
     * @param {string} keyword 关键词
     * @param {number} page 页码
     * @param {number} pageSize 每页数量
     */
    searchPosts: function(containerId, keyword, page = 1, pageSize = 10) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        UI.showLoading();
        
        API.forum.searchPosts({ keyword, page, pageSize })
            .then(response => {
                if (response.code === 200) {
                    const result = response.data;
                    
                    if (!result.items || result.items.length === 0) {
                        container.innerHTML = `<div class="empty-state">未找到与"${keyword}"相关的帖子</div>`;
                        // 清空分页
                        const paginationContainer = document.getElementById('forumPagination');
                        if (paginationContainer) {
                            paginationContainer.innerHTML = '';
                        }
                        return;
                    }
                    
                    // 渲染帖子列表
                    this.renderPostList(container, result.items);
                    
                    // 渲染分页
                    const totalPages = Math.ceil(result.total / pageSize);
                    if (totalPages > 1) {
                        UI.renderPagination('forumPagination', page, totalPages, (newPage) => {
                            this.searchPosts(containerId, keyword, newPage, pageSize);
                        });
                    } else {
                        // 清空分页
                        const paginationContainer = document.getElementById('forumPagination');
                        if (paginationContainer) {
                            paginationContainer.innerHTML = '';
                        }
                    }
                } else {
                    container.innerHTML = `<div class="error-state">搜索失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('搜索帖子失败:', error);
                container.innerHTML = '<div class="error-state">搜索失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 渲染帖子列表
     * @param {HTMLElement} container 容器元素
     * @param {Array} posts 帖子数据
     */
    renderPostList: function(container, posts) {
        let html = '';
        
        posts.forEach(post => {
            // 是否是精华帖
            const essenceTag = post.isEssence ? '<span class="badge badge-warning">精华</span>' : '';
            // 是否是热门帖
            const hotTag = post.isHot ? '<span class="badge badge-danger">热门</span>' : '';
            
            // 格式化发布时间
            const postTime = this.formatPostTime(post.createTime);
            
            html += `
                <div class="post-item" data-id="${post.id}">
                    <div class="post-avatar">
                        <img src="${post.authorAvatar || 'static/images/default-avatar.png'}" alt="用户头像">
                    </div>
                    <div class="post-content">
                        <div class="post-header">
                            <h3 class="post-title">
                                ${post.title}
                                ${essenceTag}
                                ${hotTag}
                            </h3>
                            <div class="post-meta">
                                <span class="post-author">${post.authorName}</span>
                                <span class="post-time">${postTime}</span>
                            </div>
                        </div>
                        <div class="post-summary">
                            ${post.summary || post.content.substring(0, 100) + (post.content.length > 100 ? '...' : '')}
                        </div>
                        <div class="post-stats">
                            <span class="post-views"><i class="bi bi-eye"></i> ${post.viewCount || 0}</span>
                            <span class="post-comments"><i class="bi bi-chat"></i> ${post.commentCount || 0}</span>
                            <span class="post-likes"><i class="bi bi-heart"></i> ${post.likeCount || 0}</span>
                        </div>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
        
        // 绑定点击事件
        const postItems = container.querySelectorAll('.post-item');
        postItems.forEach(item => {
            item.addEventListener('click', () => {
                const postId = item.getAttribute('data-id');
                this.showPostDetail(postId);
            });
        });
    },
    
    /**
     * 显示帖子详情
     * @param {string} postId 帖子ID
     */
    showPostDetail: function(postId) {
        UI.showLoading();
        
        API.forum.getPostDetail(postId)
            .then(response => {
                if (response.code === 200) {
                    const post = response.data;
                    
                    // 是否是精华帖
                    const essenceTag = post.isEssence ? '<span class="badge badge-warning">精华</span>' : '';
                    // 是否是热门帖
                    const hotTag = post.isHot ? '<span class="badge badge-danger">热门</span>' : '';
                    
                    // 格式化发布时间
                    const postTime = this.formatPostTime(post.createTime);
                    
                    // 创建对话框内容
                    let content = `
                        <div class="post-detail">
                            <div class="post-header">
                                <h2 class="post-title">
                                    ${post.title}
                                    ${essenceTag}
                                    ${hotTag}
                                </h2>
                                <div class="post-meta">
                                    <div class="post-author-info">
                                        <img src="${post.authorAvatar || 'static/images/default-avatar.png'}" alt="用户头像" class="author-avatar">
                                        <div>
                                            <div class="author-name">${post.authorName}</div>
                                            <div class="post-time">${postTime}</div>
                                        </div>
                                    </div>
                                    <div class="post-stats">
                                        <span class="post-views"><i class="bi bi-eye"></i> ${post.viewCount || 0}</span>
                                        <span class="post-comments"><i class="bi bi-chat"></i> ${post.commentCount || 0}</span>
                                        <span class="post-likes"><i class="bi bi-heart"></i> ${post.likeCount || 0}</span>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="post-body">
                                ${post.content}
                            </div>
                            
                            <div class="post-actions">
                                <button class="btn btn-outline-primary like-post-btn" data-id="${post.id}">
                                    <i class="bi bi-heart"></i> 点赞
                                </button>
                                ${Auth.isLoggedIn() && Auth.getUserId() === post.authorId ? `
                                    <button class="btn btn-outline-primary edit-post-btn" data-id="${post.id}">
                                        <i class="bi bi-pencil"></i> 编辑
                                    </button>
                                    <button class="btn btn-outline-danger delete-post-btn" data-id="${post.id}">
                                        <i class="bi bi-trash"></i> 删除
                                    </button>
                                ` : ''}
                            </div>
                            
                            <div class="post-comments-section">
                                <h4>评论 (${post.commentCount || 0})</h4>
                                <div class="comments-list">
                                    ${this.renderComments(post.comments || [])}
                                </div>
                                
                                ${Auth.isLoggedIn() ? `
                                    <div class="comment-form">
                                        <h5>发表评论</h5>
                                        <textarea class="form-control" id="commentContent" rows="3" placeholder="写下你的评论..."></textarea>
                                        <button class="btn btn-primary mt-2" id="submitCommentBtn">提交评论</button>
                                    </div>
                                ` : `
                                    <div class="comment-login-tip">
                                        <a href="#" class="login-link">登录</a> 后才能发表评论
                                    </div>
                                `}
                            </div>
                        </div>
                    `;
                    
                    // 创建对话框
                    const dialog = UI.createDialog('帖子详情', content, '800px');
                    
                    // 绑定点赞事件
                    const likeBtn = dialog.body.querySelector('.like-post-btn');
                    if (likeBtn) {
                        likeBtn.addEventListener('click', () => {
                            if (!Auth.isLoggedIn()) {
                                UI.showMessage('请先登录', 'warning');
                                return;
                            }
                            
                            UI.showLoading();
                            API.forum.likePost(postId)
                                .then(response => {
                                    if (response.code === 200) {
                                        UI.showMessage('点赞成功', 'success');
                                        // 更新点赞数
                                        const likeCount = parseInt(post.likeCount || 0) + 1;
                                        likeBtn.innerHTML = `<i class="bi bi-heart-fill"></i> 已点赞 (${likeCount})`;
                                        likeBtn.disabled = true;
                                    } else {
                                        UI.showMessage(response.message || '点赞失败', 'error');
                                    }
                                })
                                .catch(error => {
                                    console.error('点赞失败:', error);
                                    UI.showMessage('点赞失败', 'error');
                                })
                                .finally(() => {
                                    UI.hideLoading();
                                });
                        });
                    }
                    
                    // 绑定编辑事件
                    const editBtn = dialog.body.querySelector('.edit-post-btn');
                    if (editBtn) {
                        editBtn.addEventListener('click', () => {
                            dialog.close();
                            this.editPost(post);
                        });
                    }
                    
                    // 绑定删除事件
                    const deleteBtn = dialog.body.querySelector('.delete-post-btn');
                    if (deleteBtn) {
                        deleteBtn.addEventListener('click', () => {
                            if (confirm('确定要删除这个帖子吗？')) {
                                UI.showLoading();
                                API.forum.deletePost(postId)
                                    .then(response => {
                                        if (response.code === 200) {
                                            UI.showMessage('删除成功', 'success');
                                            dialog.close();
                                            // 刷新帖子列表
                                            const postsContainer = document.getElementById('postsContainer');
                                            if (postsContainer) {
                                                this.loadPosts('postsContainer', 'all', 1, 10);
                                            }
                                        } else {
                                            UI.showMessage(response.message || '删除失败', 'error');
                                        }
                                    })
                                    .catch(error => {
                                        console.error('删除帖子失败:', error);
                                        UI.showMessage('删除失败', 'error');
                                    })
                                    .finally(() => {
                                        UI.hideLoading();
                                    });
                            }
                        });
                    }
                    
                    // 绑定提交评论事件
                    const submitCommentBtn = dialog.body.querySelector('#submitCommentBtn');
                    if (submitCommentBtn) {
                        submitCommentBtn.addEventListener('click', () => {
                            const content = dialog.body.querySelector('#commentContent').value.trim();
                            if (!content) {
                                UI.showMessage('请输入评论内容', 'warning');
                                return;
                            }
                            
                            this.submitComment(postId, content, (newComment) => {
                                // 清空输入框
                                dialog.body.querySelector('#commentContent').value = '';
                                
                                // 添加新评论到列表
                                const commentsList = dialog.body.querySelector('.comments-list');
                                if (commentsList) {
                                    const commentHtml = this.renderComment(newComment);
                                    commentsList.insertAdjacentHTML('afterbegin', commentHtml);
                                }
                                
                                // 更新评论数
                                const commentCount = parseInt(post.commentCount || 0) + 1;
                                const commentTitle = dialog.body.querySelector('.post-comments-section h4');
                                if (commentTitle) {
                                    commentTitle.textContent = `评论 (${commentCount})`;
                                }
                            });
                        });
                    }
                    
                    // 如果是登录链接，绑定登录事件
                    const loginLink = dialog.body.querySelector('.login-link');
                    if (loginLink) {
                        loginLink.addEventListener('click', (e) => {
                            e.preventDefault();
                            dialog.close();
                            // 跳转到登录页面
                            window.location.href = 'login.html';
                        });
                    }
                } else {
                    UI.showMessage(response.message || '获取帖子详情失败', 'error');
                }
            })
            .catch(error => {
                console.error('获取帖子详情失败:', error);
                UI.showMessage('获取帖子详情失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 渲染评论列表
     * @param {Array} comments 评论数据
     * @returns {string} 评论列表HTML
     */
    renderComments: function(comments) {
        if (!comments || comments.length === 0) {
            return '<div class="empty-comment">暂无评论</div>';
        }
        
        let html = '';
        comments.forEach(comment => {
            html += this.renderComment(comment);
        });
        
        return html;
    },
    
    /**
     * 渲染单个评论
     * @param {Object} comment 评论数据
     * @returns {string} 评论HTML
     */
    renderComment: function(comment) {
        // 格式化评论时间
        const commentTime = this.formatPostTime(comment.createTime);
        
        return `
            <div class="comment-item" data-id="${comment.id}">
                <div class="comment-avatar">
                    <img src="${comment.authorAvatar || 'static/images/default-avatar.png'}" alt="用户头像">
                </div>
                <div class="comment-content">
                    <div class="comment-header">
                        <span class="comment-author">${comment.authorName}</span>
                        <span class="comment-time">${commentTime}</span>
                    </div>
                    <div class="comment-body">
                        ${comment.content}
                    </div>
                    ${Auth.isLoggedIn() ? `
                        <div class="comment-actions">
                            <button class="btn btn-sm btn-link reply-comment-btn" data-id="${comment.id}">回复</button>
                            ${Auth.getUserId() === comment.authorId ? `
                                <button class="btn btn-sm btn-link delete-comment-btn" data-id="${comment.id}">删除</button>
                            ` : ''}
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    },
    
    /**
     * 提交评论
     * @param {string} postId 帖子ID
     * @param {string} content 评论内容
     * @param {Function} callback 成功回调
     */
    submitComment: function(postId, content, callback) {
        UI.showLoading();
        
        API.forum.addComment(postId, { content })
            .then(response => {
                if (response.code === 200) {
                    UI.showMessage('评论成功', 'success');
                    
                    if (typeof callback === 'function') {
                        callback(response.data);
                    }
                } else {
                    UI.showMessage(response.message || '评论失败', 'error');
                }
            })
            .catch(error => {
                console.error('评论失败:', error);
                UI.showMessage('评论失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 显示创建帖子表单
     */
    showCreatePostForm: function() {
        // 创建表单字段
        const fields = [
            { name: 'title', label: '标题', type: 'text', required: true },
            { name: 'content', label: '内容', type: 'textarea', rows: 10, required: true },
            { name: 'category', label: '分类', type: 'select', options: [
                { value: '1', label: '学习交流' },
                { value: '2', label: '校园生活' },
                { value: '3', label: '求助问答' },
                { value: '4', label: '意见建议' },
                { value: '5', label: '其他' }
            ]},
            { name: 'tags', label: '标签', type: 'text', helpText: '多个标签请用逗号分隔' }
        ];
        
        // 创建表单
        const form = UI.createForm(fields, {}, (formData) => {
            UI.showLoading();
            
            // 处理标签
            if (formData.tags) {
                formData.tags = formData.tags.split(',').map(tag => tag.trim()).filter(tag => tag);
            }
            
            API.forum.createPost(formData)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('发布成功', 'success');
                        dialog.close();
                        
                        // 刷新帖子列表
                        const postsContainer = document.getElementById('postsContainer');
                        if (postsContainer) {
                            this.loadPosts('postsContainer', 'all', 1, 10);
                        }
                    } else {
                        UI.showMessage(response.message || '发布失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('发布帖子失败:', error);
                    UI.showMessage('发布失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        });
        
        // 创建对话框
        const dialog = UI.createDialog('发布帖子', form, '800px');
        
        // 添加富文本编辑器
        /*
        const contentTextarea = dialog.body.querySelector('textarea[name="content"]');
        if (contentTextarea) {
            // 这里可以集成富文本编辑器，如CKEditor、TinyMCE等
            // 示例：
            // ClassicEditor
            //     .create(contentTextarea)
            //     .catch(error => {
            //         console.error(error);
            //     });
        }
        */
    },
    
    /**
     * 编辑帖子
     * @param {Object} post 帖子数据
     */
    editPost: function(post) {
        // 创建表单字段
        const fields = [
            { name: 'title', label: '标题', type: 'text', required: true },
            { name: 'content', label: '内容', type: 'textarea', rows: 10, required: true },
            { name: 'category', label: '分类', type: 'select', options: [
                { value: '1', label: '学习交流' },
                { value: '2', label: '校园生活' },
                { value: '3', label: '求助问答' },
                { value: '4', label: '意见建议' },
                { value: '5', label: '其他' }
            ]},
            { name: 'tags', label: '标签', type: 'text', helpText: '多个标签请用逗号分隔' }
        ];
        
        // 处理标签
        let formValues = { ...post };
        if (post.tags && Array.isArray(post.tags)) {
            formValues.tags = post.tags.join(',');
        }
        
        // 创建表单
        const form = UI.createForm(fields, formValues, (formData) => {
            UI.showLoading();
            
            // 处理标签
            if (formData.tags) {
                formData.tags = formData.tags.split(',').map(tag => tag.trim()).filter(tag => tag);
            }
            
            // 添加ID
            formData.id = post.id;
            
            API.forum.updatePost(formData)
                .then(response => {
                    if (response.code === 200) {
                        UI.showMessage('更新成功', 'success');
                        dialog.close();
                        
                        // 刷新帖子列表
                        const postsContainer = document.getElementById('postsContainer');
                        if (postsContainer) {
                            this.loadPosts('postsContainer', 'all', 1, 10);
                        }
                    } else {
                        UI.showMessage(response.message || '更新失败', 'error');
                    }
                })
                .catch(error => {
                    console.error('更新帖子失败:', error);
                    UI.showMessage('更新失败', 'error');
                })
                .finally(() => {
                    UI.hideLoading();
                });
        });
        
        // 创建对话框
        const dialog = UI.createDialog('编辑帖子', form, '800px');
        
        // 添加富文本编辑器
        /*
        const contentTextarea = dialog.body.querySelector('textarea[name="content"]');
        if (contentTextarea) {
            // 这里可以集成富文本编辑器，如CKEditor、TinyMCE等
            // 示例：
            // ClassicEditor
            //     .create(contentTextarea)
            //     .catch(error => {
            //         console.error(error);
            //     });
        }
        */
    },
    
    /**
     * 格式化帖子时间
     * @param {string|number} timestamp 时间戳
     * @returns {string} 格式化后的时间
     */
    formatPostTime: function(timestamp) {
        if (!timestamp) return '';
        
        const now = new Date();
        const postDate = new Date(timestamp);
        const diffInSeconds = Math.floor((now - postDate) / 1000);
        
        if (diffInSeconds < 60) {
            return '刚刚';
        } else if (diffInSeconds < 3600) {
            return Math.floor(diffInSeconds / 60) + '分钟前';
        } else if (diffInSeconds < 86400) {
            return Math.floor(diffInSeconds / 3600) + '小时前';
        } else if (diffInSeconds < 604800) {
            return Math.floor(diffInSeconds / 86400) + '天前';
        } else {
            const year = postDate.getFullYear();
            const month = String(postDate.getMonth() + 1).padStart(2, '0');
            const day = String(postDate.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        }
    }
};

// 将UIForum添加到全局UI对象中
if (typeof UI !== 'undefined') {
    UI.forum = UIForum;
} 