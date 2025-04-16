/**
 * 文件管理模块UI组件
 * 提供文件上传、下载、预览等功能
 */
const UIFile = {
    /**
     * 初始化文件管理器
     * @param {string} containerId 容器ID
     * @param {Object} options 选项
     */
    initFileManager: function(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 默认选项
        const defaultOptions = {
            mode: 'list', // list或grid
            allowUpload: true,
            allowDelete: true,
            allowDownload: true,
            showSearch: true,
            category: 'all',
            page: 1,
            pageSize: 20
        };
        
        // 合并选项
        const mergedOptions = { ...defaultOptions, ...options };
        this.options = mergedOptions;
        
        // 创建文件管理器界面
        let html = `
            <div class="file-manager-container">
                <div class="file-manager-header">
                    <div class="file-filter">
                        <select class="form-select file-category-select">
                            <option value="all" ${mergedOptions.category === 'all' ? 'selected' : ''}>全部文件</option>
                            <option value="image" ${mergedOptions.category === 'image' ? 'selected' : ''}>图片</option>
                            <option value="document" ${mergedOptions.category === 'document' ? 'selected' : ''}>文档</option>
                            <option value="video" ${mergedOptions.category === 'video' ? 'selected' : ''}>视频</option>
                            <option value="audio" ${mergedOptions.category === 'audio' ? 'selected' : ''}>音频</option>
                            <option value="other" ${mergedOptions.category === 'other' ? 'selected' : ''}>其他</option>
                        </select>
                    </div>
                    
                    ${mergedOptions.showSearch ? `
                        <div class="file-search">
                            <input type="text" class="form-control file-search-input" placeholder="搜索文件...">
                            <button class="btn btn-outline-primary file-search-btn">
                                <i class="bi bi-search"></i>
                            </button>
                        </div>
                    ` : ''}
                    
                    <div class="file-view-mode">
                        <div class="btn-group" role="group">
                            <button type="button" class="btn ${mergedOptions.mode === 'list' ? 'btn-primary' : 'btn-outline-primary'} list-mode-btn">
                                <i class="bi bi-list-ul"></i>
                            </button>
                            <button type="button" class="btn ${mergedOptions.mode === 'grid' ? 'btn-primary' : 'btn-outline-primary'} grid-mode-btn">
                                <i class="bi bi-grid-3x3-gap"></i>
                            </button>
                        </div>
                    </div>
                    
                    ${mergedOptions.allowUpload ? `
                        <div class="file-actions">
                            <button class="btn btn-primary upload-file-btn">
                                <i class="bi bi-cloud-upload"></i> 上传文件
                            </button>
                        </div>
                    ` : ''}
                </div>
                
                <div class="file-manager-body">
                    <div class="file-list ${mergedOptions.mode === 'grid' ? 'grid-mode' : 'list-mode'}">
                        <!-- 文件列表将在这里渲染 -->
                    </div>
                </div>
                
                <div class="file-manager-footer">
                    <div class="file-pagination"></div>
                </div>
            </div>
        `;
        
        container.innerHTML = html;
        
        // 绑定事件
        this.bindEvents(container);
        
        // 加载文件
        this.loadFiles(container);
    },
    
    /**
     * 绑定事件
     * @param {HTMLElement} container 容器元素
     */
    bindEvents: function(container) {
        // 分类筛选
        const categorySelect = container.querySelector('.file-category-select');
        if (categorySelect) {
            categorySelect.addEventListener('change', () => {
                this.options.category = categorySelect.value;
                this.options.page = 1;
                this.loadFiles(container);
            });
        }
        
        // 搜索
        const searchBtn = container.querySelector('.file-search-btn');
        const searchInput = container.querySelector('.file-search-input');
        if (searchBtn && searchInput) {
            searchBtn.addEventListener('click', () => {
                this.options.keyword = searchInput.value.trim();
                this.options.page = 1;
                this.loadFiles(container);
            });
            
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    searchBtn.click();
                }
            });
        }
        
        // 视图模式切换
        const listModeBtn = container.querySelector('.list-mode-btn');
        const gridModeBtn = container.querySelector('.grid-mode-btn');
        const fileList = container.querySelector('.file-list');
        
        if (listModeBtn && gridModeBtn && fileList) {
            listModeBtn.addEventListener('click', () => {
                listModeBtn.classList.remove('btn-outline-primary');
                listModeBtn.classList.add('btn-primary');
                gridModeBtn.classList.remove('btn-primary');
                gridModeBtn.classList.add('btn-outline-primary');
                
                fileList.classList.remove('grid-mode');
                fileList.classList.add('list-mode');
                
                this.options.mode = 'list';
            });
            
            gridModeBtn.addEventListener('click', () => {
                gridModeBtn.classList.remove('btn-outline-primary');
                gridModeBtn.classList.add('btn-primary');
                listModeBtn.classList.remove('btn-primary');
                listModeBtn.classList.add('btn-outline-primary');
                
                fileList.classList.remove('list-mode');
                fileList.classList.add('grid-mode');
                
                this.options.mode = 'grid';
            });
        }
        
        // 上传文件
        const uploadBtn = container.querySelector('.upload-file-btn');
        if (uploadBtn) {
            uploadBtn.addEventListener('click', () => {
                this.showUploadDialog();
            });
        }
    },
    
    /**
     * 加载文件列表
     * @param {HTMLElement} container 容器元素
     */
    loadFiles: function(container) {
        UI.showLoading();
        
        // 准备API请求参数
        const params = {
            page: this.options.page,
            pageSize: this.options.pageSize,
            category: this.options.category !== 'all' ? this.options.category : undefined,
            keyword: this.options.keyword
        };
        
        API.file.getFileList(params)
            .then(response => {
                if (response.code === 200) {
                    const result = response.data;
                    
                    if (!result.items || result.items.length === 0) {
                        const fileList = container.querySelector('.file-list');
                        fileList.innerHTML = '<div class="empty-state">暂无文件</div>';
                        
                        // 清空分页
                        const pagination = container.querySelector('.file-pagination');
                        pagination.innerHTML = '';
                        return;
                    }
                    
                    // 渲染文件列表
                    this.renderFileList(container, result.items);
                    
                    // 渲染分页
                    const totalPages = Math.ceil(result.total / this.options.pageSize);
                    if (totalPages > 1) {
                        const pagination = container.querySelector('.file-pagination');
                        UI.renderPagination(pagination, this.options.page, totalPages, (newPage) => {
                            this.options.page = newPage;
                            this.loadFiles(container);
                        });
                    } else {
                        // 清空分页
                        const pagination = container.querySelector('.file-pagination');
                        pagination.innerHTML = '';
                    }
                } else {
                    const fileList = container.querySelector('.file-list');
                    fileList.innerHTML = `<div class="error-state">加载文件失败：${response.message}</div>`;
                }
            })
            .catch(error => {
                console.error('加载文件失败:', error);
                const fileList = container.querySelector('.file-list');
                fileList.innerHTML = '<div class="error-state">加载文件失败</div>';
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 渲染文件列表
     * @param {HTMLElement} container 容器元素
     * @param {Array} files 文件数据
     */
    renderFileList: function(container, files) {
        const fileList = container.querySelector('.file-list');
        if (!fileList) return;
        
        let html = '';
        
        if (this.options.mode === 'list') {
            html += `
                <div class="file-list-header">
                    <div class="file-column file-name-column">文件名</div>
                    <div class="file-column file-size-column">大小</div>
                    <div class="file-column file-type-column">类型</div>
                    <div class="file-column file-uploader-column">上传者</div>
                    <div class="file-column file-date-column">上传时间</div>
                    <div class="file-column file-actions-column">操作</div>
                </div>
            `;
            
            files.forEach(file => {
                html += `
                    <div class="file-item" data-id="${file.id}">
                        <div class="file-column file-name-column">
                            <div class="file-icon">${this.getFileIcon(file.type)}</div>
                            <div class="file-name" title="${file.name}">${file.name}</div>
                        </div>
                        <div class="file-column file-size-column">${this.formatFileSize(file.size)}</div>
                        <div class="file-column file-type-column">${this.formatFileType(file.type)}</div>
                        <div class="file-column file-uploader-column">${file.uploaderName}</div>
                        <div class="file-column file-date-column">${this.formatDate(file.uploadTime)}</div>
                        <div class="file-column file-actions-column">
                            <div class="btn-group">
                                <button class="btn btn-sm btn-outline-primary preview-file-btn" data-id="${file.id}" title="预览">
                                    <i class="bi bi-eye"></i>
                                </button>
                                ${this.options.allowDownload ? `
                                    <button class="btn btn-sm btn-outline-primary download-file-btn" data-id="${file.id}" title="下载">
                                        <i class="bi bi-download"></i>
                                    </button>
                                ` : ''}
                                ${this.options.allowDelete && (Auth.isAdmin() || Auth.getUserId() === file.uploaderId) ? `
                                    <button class="btn btn-sm btn-outline-danger delete-file-btn" data-id="${file.id}" title="删除">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                ` : ''}
                            </div>
                        </div>
                    </div>
                `;
            });
        } else {
            files.forEach(file => {
                html += `
                    <div class="file-card" data-id="${file.id}">
                        <div class="file-card-preview">
                            ${file.type.startsWith('image/') ? 
                                `<img src="${file.url}" alt="${file.name}" class="file-thumbnail">` : 
                                `<div class="file-icon-large">${this.getFileIcon(file.type)}</div>`
                            }
                        </div>
                        <div class="file-card-info">
                            <div class="file-name" title="${file.name}">${file.name}</div>
                            <div class="file-meta">
                                <span>${this.formatFileSize(file.size)}</span>
                                <span>${this.formatDate(file.uploadTime)}</span>
                            </div>
                        </div>
                        <div class="file-card-actions">
                            <button class="btn btn-sm btn-outline-primary preview-file-btn" data-id="${file.id}" title="预览">
                                <i class="bi bi-eye"></i>
                            </button>
                            ${this.options.allowDownload ? `
                                <button class="btn btn-sm btn-outline-primary download-file-btn" data-id="${file.id}" title="下载">
                                    <i class="bi bi-download"></i>
                                </button>
                            ` : ''}
                            ${this.options.allowDelete && (Auth.isAdmin() || Auth.getUserId() === file.uploaderId) ? `
                                <button class="btn btn-sm btn-outline-danger delete-file-btn" data-id="${file.id}" title="删除">
                                    <i class="bi bi-trash"></i>
                                </button>
                            ` : ''}
                        </div>
                    </div>
                `;
            });
        }
        
        fileList.innerHTML = html;
        
        // 绑定文件操作事件
        this.bindFileEvents(fileList);
    },
    
    /**
     * 绑定文件操作事件
     * @param {HTMLElement} fileList 文件列表元素
     */
    bindFileEvents: function(fileList) {
        // 预览按钮
        const previewBtns = fileList.querySelectorAll('.preview-file-btn');
        previewBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const fileId = btn.getAttribute('data-id');
                this.previewFile(fileId);
            });
        });
        
        // 下载按钮
        const downloadBtns = fileList.querySelectorAll('.download-file-btn');
        downloadBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const fileId = btn.getAttribute('data-id');
                this.downloadFile(fileId);
            });
        });
        
        // 删除按钮
        const deleteBtns = fileList.querySelectorAll('.delete-file-btn');
        deleteBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const fileId = btn.getAttribute('data-id');
                this.confirmDeleteFile(fileId);
            });
        });
        
        // 点击文件项（列表模式）
        const fileItems = fileList.querySelectorAll('.file-item');
        fileItems.forEach(item => {
            item.addEventListener('click', () => {
                const fileId = item.getAttribute('data-id');
                this.previewFile(fileId);
            });
        });
        
        // 点击文件卡片（网格模式）
        const fileCards = fileList.querySelectorAll('.file-card');
        fileCards.forEach(card => {
            card.addEventListener('click', () => {
                const fileId = card.getAttribute('data-id');
                this.previewFile(fileId);
            });
        });
    },
    
    /**
     * 显示上传对话框
     */
    showUploadDialog: function() {
        // 创建对话框内容
        let content = `
            <div class="file-upload-container">
                <div class="file-drop-zone" id="fileDropZone">
                    <i class="bi bi-cloud-upload"></i>
                    <p>拖放文件到这里或点击选择文件</p>
                </div>
                <input type="file" id="fileInput" multiple style="display: none;">
                <div class="selected-files" id="selectedFiles"></div>
            </div>
        `;
        
        // 创建对话框
        const dialog = UI.createDialog('上传文件', content, '600px', {
            confirmText: '上传',
            cancelText: '取消',
            onConfirm: () => {
                const fileInput = dialog.body.querySelector('#fileInput');
                if (fileInput.files.length === 0) {
                    UI.showMessage('请选择文件', 'warning');
                    return false; // 不关闭对话框
                }
                
                this.uploadFiles(fileInput.files);
                return true; // 可以关闭对话框
            }
        });
        
        // 绑定拖放和选择文件事件
        const fileDropZone = dialog.body.querySelector('#fileDropZone');
        const fileInput = dialog.body.querySelector('#fileInput');
        const selectedFiles = dialog.body.querySelector('#selectedFiles');
        
        fileDropZone.addEventListener('click', () => {
            fileInput.click();
        });
        
        fileDropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            fileDropZone.classList.add('active');
        });
        
        fileDropZone.addEventListener('dragleave', () => {
            fileDropZone.classList.remove('active');
        });
        
        fileDropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            fileDropZone.classList.remove('active');
            
            if (e.dataTransfer.files.length > 0) {
                fileInput.files = e.dataTransfer.files;
                this.updateSelectedFilesList(selectedFiles, fileInput.files);
            }
        });
        
        fileInput.addEventListener('change', () => {
            this.updateSelectedFilesList(selectedFiles, fileInput.files);
        });
    },
    
    /**
     * 更新已选文件列表
     * @param {HTMLElement} container 容器元素
     * @param {FileList} files 文件列表
     */
    updateSelectedFilesList: function(container, files) {
        if (files.length === 0) {
            container.innerHTML = '';
            return;
        }
        
        let html = '<ul class="selected-files-list">';
        
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            html += `
                <li class="selected-file-item">
                    <div class="file-info">
                        <span class="file-name">${file.name}</span>
                        <span class="file-size">(${this.formatFileSize(file.size)})</span>
                    </div>
                </li>
            `;
        }
        
        html += '</ul>';
        container.innerHTML = html;
    },
    
    /**
     * 上传文件
     * @param {FileList} files 文件列表
     */
    uploadFiles: function(files) {
        UI.showLoading();
        
        const formData = new FormData();
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }
        
        formData.append('uploaderId', Auth.getUserId());
        
        API.file.uploadFiles(formData)
            .then(response => {
                if (response.code === 200) {
                    UI.showMessage('上传成功', 'success');
                    
                    // 刷新文件列表
                    const container = document.querySelector('.file-manager-container').parentNode;
                    this.loadFiles(container);
                } else {
                    UI.showMessage(response.message || '上传失败', 'error');
                }
            })
            .catch(error => {
                console.error('上传文件失败:', error);
                UI.showMessage('上传失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 预览文件
     * @param {string} fileId 文件ID
     */
    previewFile: function(fileId) {
        UI.showLoading();
        
        API.file.getFileDetail(fileId)
            .then(response => {
                if (response.code === 200) {
                    const file = response.data;
                    this.showFilePreview(file);
                } else {
                    UI.showMessage(response.message || '获取文件详情失败', 'error');
                }
            })
            .catch(error => {
                console.error('获取文件详情失败:', error);
                UI.showMessage('获取文件详情失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 显示文件预览
     * @param {Object} file 文件数据
     */
    showFilePreview: function(file) {
        let previewContent = '';
        
        // 根据文件类型生成预览内容
        if (file.type.startsWith('image/')) {
            // 图片预览
            previewContent = `<img src="${file.url}" alt="${file.name}" class="file-preview-image">`;
        } else if (file.type.startsWith('video/')) {
            // 视频预览
            previewContent = `
                <video controls class="file-preview-video">
                    <source src="${file.url}" type="${file.type}">
                    您的浏览器不支持视频播放
                </video>
            `;
        } else if (file.type.startsWith('audio/')) {
            // 音频预览
            previewContent = `
                <audio controls class="file-preview-audio">
                    <source src="${file.url}" type="${file.type}">
                    您的浏览器不支持音频播放
                </audio>
            `;
        } else if (file.type === 'application/pdf') {
            // PDF预览
            previewContent = `
                <iframe src="${file.url}" class="file-preview-pdf" frameborder="0"></iframe>
            `;
        } else {
            // 不支持预览的文件类型
            previewContent = `
                <div class="file-preview-unsupported">
                    <div class="file-icon-large">${this.getFileIcon(file.type)}</div>
                    <p>无法预览此文件类型</p>
                    ${this.options.allowDownload ? `
                        <button class="btn btn-primary download-current-file-btn" data-id="${file.id}">
                            <i class="bi bi-download"></i> 下载文件
                        </button>
                    ` : ''}
                </div>
            `;
        }
        
        // 文件信息
        const fileInfo = `
            <div class="file-preview-info">
                <div class="file-preview-name">${file.name}</div>
                <div class="file-preview-meta">
                    <span>${this.formatFileType(file.type)}</span>
                    <span>${this.formatFileSize(file.size)}</span>
                    <span>上传者: ${file.uploaderName}</span>
                    <span>上传时间: ${this.formatDate(file.uploadTime)}</span>
                </div>
            </div>
        `;
        
        // 创建对话框
        const dialog = UI.createDialog('文件预览', fileInfo + previewContent, '800px', {
            fullscreen: file.type.startsWith('image/') || file.type.startsWith('video/') || file.type === 'application/pdf'
        });
        
        // 绑定下载按钮事件
        const downloadBtn = dialog.body.querySelector('.download-current-file-btn');
        if (downloadBtn) {
            downloadBtn.addEventListener('click', () => {
                this.downloadFile(file.id);
            });
        }
    },
    
    /**
     * 下载文件
     * @param {string} fileId 文件ID
     */
    downloadFile: function(fileId) {
        UI.showLoading();
        
        API.file.getFileDetail(fileId)
            .then(response => {
                if (response.code === 200) {
                    const file = response.data;
                    
                    // 创建下载链接
                    const link = document.createElement('a');
                    link.href = file.url;
                    link.download = file.name;
                    link.target = '_blank';
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                    
                    // 记录下载次数
                    API.file.recordFileDownload(fileId)
                        .catch(error => {
                            console.error('记录文件下载失败:', error);
                        });
                } else {
                    UI.showMessage(response.message || '下载文件失败', 'error');
                }
            })
            .catch(error => {
                console.error('下载文件失败:', error);
                UI.showMessage('下载文件失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 确认删除文件
     * @param {string} fileId 文件ID
     */
    confirmDeleteFile: function(fileId) {
        UI.showConfirm('确定要删除这个文件吗？', '删除后将无法恢复', () => {
            this.deleteFile(fileId);
        });
    },
    
    /**
     * 删除文件
     * @param {string} fileId 文件ID
     */
    deleteFile: function(fileId) {
        UI.showLoading();
        
        API.file.deleteFile(fileId)
            .then(response => {
                if (response.code === 200) {
                    UI.showMessage('删除成功', 'success');
                    
                    // 刷新文件列表
                    const container = document.querySelector('.file-manager-container').parentNode;
                    this.loadFiles(container);
                } else {
                    UI.showMessage(response.message || '删除失败', 'error');
                }
            })
            .catch(error => {
                console.error('删除文件失败:', error);
                UI.showMessage('删除失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 获取文件图标
     * @param {string} fileType 文件MIME类型
     * @returns {string} 图标HTML
     */
    getFileIcon: function(fileType) {
        if (fileType.startsWith('image/')) {
            return '<i class="bi bi-file-image"></i>';
        } else if (fileType.startsWith('video/')) {
            return '<i class="bi bi-file-play"></i>';
        } else if (fileType.startsWith('audio/')) {
            return '<i class="bi bi-file-music"></i>';
        } else if (fileType === 'application/pdf') {
            return '<i class="bi bi-file-pdf"></i>';
        } else if (fileType === 'application/msword' || fileType === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') {
            return '<i class="bi bi-file-word"></i>';
        } else if (fileType === 'application/vnd.ms-excel' || fileType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
            return '<i class="bi bi-file-excel"></i>';
        } else if (fileType === 'application/vnd.ms-powerpoint' || fileType === 'application/vnd.openxmlformats-officedocument.presentationml.presentation') {
            return '<i class="bi bi-file-ppt"></i>';
        } else if (fileType === 'application/zip' || fileType === 'application/x-rar-compressed') {
            return '<i class="bi bi-file-zip"></i>';
        } else if (fileType === 'text/plain') {
            return '<i class="bi bi-file-text"></i>';
        } else if (fileType === 'text/html' || fileType === 'application/xhtml+xml') {
            return '<i class="bi bi-file-code"></i>';
        } else {
            return '<i class="bi bi-file"></i>';
        }
    },
    
    /**
     * 格式化文件大小
     * @param {number} size 文件大小（字节）
     * @returns {string} 格式化后的文件大小
     */
    formatFileSize: function(size) {
        if (size < 1024) {
            return size + ' B';
        } else if (size < 1024 * 1024) {
            return (size / 1024).toFixed(2) + ' KB';
        } else if (size < 1024 * 1024 * 1024) {
            return (size / (1024 * 1024)).toFixed(2) + ' MB';
        } else {
            return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
        }
    },
    
    /**
     * 格式化文件类型
     * @param {string} type 文件MIME类型
     * @returns {string} 格式化后的文件类型
     */
    formatFileType: function(type) {
        if (type.startsWith('image/')) {
            return '图片';
        } else if (type.startsWith('video/')) {
            return '视频';
        } else if (type.startsWith('audio/')) {
            return '音频';
        } else if (type === 'application/pdf') {
            return 'PDF';
        } else if (type === 'application/msword' || type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') {
            return 'Word文档';
        } else if (type === 'application/vnd.ms-excel' || type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
            return 'Excel表格';
        } else if (type === 'application/vnd.ms-powerpoint' || type === 'application/vnd.openxmlformats-officedocument.presentationml.presentation') {
            return 'PPT幻灯片';
        } else if (type === 'application/zip') {
            return 'ZIP压缩包';
        } else if (type === 'application/x-rar-compressed') {
            return 'RAR压缩包';
        } else if (type === 'text/plain') {
            return '文本文件';
        } else if (type === 'text/html' || type === 'application/xhtml+xml') {
            return 'HTML文件';
        } else {
            return '未知类型';
        }
    },
    
    /**
     * 格式化日期
     * @param {string|number} timestamp 时间戳
     * @returns {string} 格式化后的日期
     */
    formatDate: function(timestamp) {
        if (!timestamp) return '-';
        
        const date = new Date(timestamp);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        
        return `${year}-${month}-${day} ${hours}:${minutes}`;
    }
};

// 将UIFile添加到全局UI对象中
if (typeof UI !== 'undefined') {
    UI.file = UIFile;
} 