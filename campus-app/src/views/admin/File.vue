<template>
  <div class="file-manager">
    <h1>文件管理</h1>

    <!-- 当前路径显示 -->
    <el-breadcrumb class="path-breadcrumb" separator="/">
      <el-breadcrumb-item :to="{ path: '' }" @click="navigateToPath('')">根目录</el-breadcrumb-item>
      <el-breadcrumb-item
          v-for="(folder, index) in pathSegments"
          :key="index"
          @click="navigateToPath(generatePath(index))"
      >
        {{ folder }}
      </el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 工具栏 -->
    <div class="file-tools">
      <el-upload
          :action="uploadUrl"
          :before-upload="beforeUpload"
          :disabled="uploading"
          :on-error="handleUploadError"
          :on-success="handleUploadSuccess"
          :show-file-list="false"
          class="file-uploader"
          multiple
      >
        <el-button :loading="uploading" type="primary">
          <i class="el-icon-upload"></i> 上传文件
        </el-button>
      </el-upload>

      <el-button
          :disabled="selectedFiles.length === 0"
          type="danger"
          @click="confirmDeleteFiles"
      >
        <i class="el-icon-delete"></i> 删除选中
      </el-button>

      <el-button
          type="info"
          @click="refreshFileList"
      >
        <i class="el-icon-refresh"></i> 刷新
      </el-button>
    </div>

    <!-- 文件统计信息 -->
    <el-card v-if="showStats" class="stats-card">
      <template #header>
        <span>文件统计</span>
        <el-button style="float: right; padding: 3px 0" @click="showStats = false">
          隐藏
        </el-button>
      </template>
      <div v-if="fileStats" class="stats-content">
        <div class="stats-item">
          <div class="stats-label">总文件数:</div>
          <div class="stats-value">{{ fileStats.totalCount }} 个文件</div>
        </div>
        <div class="stats-item">
          <div class="stats-label">总容量:</div>
          <div class="stats-value">{{ formatFileSize(fileStats.totalSize) }}</div>
        </div>
        <div v-if="fileStats.typeStats && fileStats.typeStats.length > 0" class="stats-types">
          <h4>文件类型分布:</h4>
          <el-table :data="fileStats.typeStats.slice(0, 5)" border size="small" stripe>
            <el-table-column label="类型" prop="type" width="100"></el-table-column>
            <el-table-column label="数量" prop="count" width="80"></el-table-column>
            <el-table-column label="大小" prop="size">
              <template #default="scope">
                {{ formatFileSize(scope.row.size) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 文件列表 -->
    <div class="file-list">
      <el-table
          v-loading="loading"
          :data="fileList"
          style="width: 100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column
            type="selection"
            width="55"
        />
        <el-table-column
            label="名称"
            min-width="200"
        >
          <template #default="scope">
            <div class="file-name-cell">
              <i
                  :class="scope.row.isDirectory ? 'el-icon-folder' : getFileIcon(scope.row.name)"
                  class="file-icon"
              ></i>
              <span
                  :class="{'folder-name': scope.row.isDirectory}"
                  class="file-name"
                  @click="scope.row.isDirectory ? enterDirectory(scope.row.name) : ''"
              >
                {{ scope.row.name }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
            label="大小"
            prop="size"
            width="120"
        >
          <template #default="scope">
            {{ scope.row.isDirectory ? '-' : formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column
            label="修改时间"
            prop="lastModified"
            width="180"
        >
          <template #default="scope">
            {{ formatDate(scope.row.lastModified) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
                v-if="!scope.row.isDirectory"
                size="small"
                type="primary"
                @click="downloadFile(scope.row)"
            >
              下载
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="confirmDeleteFile(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 当目录为空时显示 -->
    <el-empty
        v-if="fileList.length === 0 && !loading"
        description="当前目录为空"
    ></el-empty>
  </div>
</template>

<script>
import {batchDeleteFiles, downloadFileByPath, getFileStats, listDirectory} from '@/api/file';
import {FILE_API} from '@/api/api-endpoints';

export default {
  name: 'FileManager',
  data() {
    return {
      loading: false,
      uploading: false,
      fileList: [],
      currentPath: '',
      selectedFiles: [],
      showStats: true,
      fileStats: null,
      uploadUrl: process.env.VUE_APP_BASE_API + FILE_API.UPLOAD_DOCUMENT
    }
  },
  created() {
    this.loadFileList();
    this.loadFileStats();
  },
  computed: {
    pathSegments() {
      if (!this.currentPath) return [];
      return this.currentPath.split('/').filter(segment => segment);
    }
  },
  methods: {
    // 辅助函数：清理路径，移除类似 /d: 的前缀
    cleanPathPrefix(path) {
      if (typeof path !== 'string') return path;
      // 匹配 /<盘符>: 格式，例如 /d: /c:
      const prefixRegex = /^\/[a-zA-Z]:/;
      if (prefixRegex.test(path)) {
        return path.replace(prefixRegex, '');
      }
      return path;
    },

    // 加载文件列表
    loadFileList() {
      this.loading = true;
      // 在发送请求前也清理一次 currentPath
      const cleanedPathToSend = this.cleanPathPrefix(this.currentPath);
      listDirectory(cleanedPathToSend)
          .then(response => {
            if (response.code === 200 && response.data) {
              this.fileList = response.data.files || [];
              // 在更新前清理后端返回的路径
              this.currentPath = this.cleanPathPrefix(response.data.currentPath);
            } else {
              this.$message.error(response.message || '获取文件列表失败');
            }
          })
          .catch(error => {
            console.error('加载文件列表失败', error);
            this.$message.error('加载文件列表失败: ' + (error.message || error));
          })
          .finally(() => {
            this.loading = false;
          });
    },

    // 加载文件统计
    loadFileStats() {
      getFileStats()
          .then(response => {
            if (response.code === 200) {
              this.fileStats = response.data;
            }
          })
          .catch(error => {
            console.error('获取文件统计失败', error);
          });
    },

    // 刷新文件列表
    refreshFileList() {
      this.loadFileList();
    },

    // 下载文件
    downloadFile(file) {
      if (file.isDirectory) return;

      const filePath = file.path;
      downloadFileByPath(filePath)
          .then(response => {
            // 创建Blob对象
            const blob = new Blob([response], {
              type: 'application/octet-stream'
            });

            // 创建下载链接
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = file.name;
            link.click();

            // 释放URL对象
            URL.revokeObjectURL(link.href);

            this.$message.success('下载成功');
          })
          .catch(error => {
            console.error('下载文件失败', error);
            this.$message.error('下载文件失败: ' + (error.message || error));
          });
    },

    // 确认删除单个文件
    confirmDeleteFile(file) {
      this.$confirm(`确定要删除 ${file.name} 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
          .then(() => {
            this.deleteFiles([file.path]);
          })
          .catch(() => {
            // 用户取消删除
          });
    },

    // 确认删除多个文件
    confirmDeleteFiles() {
      if (this.selectedFiles.length === 0) return;

      this.$confirm(`确定要删除选中的 ${this.selectedFiles.length} 个文件吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
          .then(() => {
            const filePaths = this.selectedFiles.map(file => file.path);
            this.deleteFiles(filePaths);
          })
          .catch(() => {
            // 用户取消删除
          });
    },

    // 删除文件
    deleteFiles(filePaths) {
      this.loading = true;
      batchDeleteFiles(filePaths)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('删除成功');
              // 刷新文件列表
              this.loadFileList();
              // 更新文件统计
              this.loadFileStats();
            } else {
              this.$message.error(response.message || '删除失败');
            }
          })
          .catch(error => {
            console.error('删除文件失败', error);
            this.$message.error('删除文件失败: ' + (error.message || error));
          })
          .finally(() => {
            this.loading = false;
          });
    },

    // 进入目录
    enterDirectory(dirName) {
      const newPath = this.currentPath
          ? `${this.currentPath}/${dirName}`
          : dirName;
      // 清理 newPath
      this.currentPath = this.cleanPathPrefix(newPath);
      this.loadFileList();
    },

    // 导航到指定路径
    navigateToPath(path) {
      // 清理 path
      this.currentPath = this.cleanPathPrefix(path);
      this.loadFileList();
    },

    // 生成跳转路径
    generatePath(index) {
      return this.pathSegments.slice(0, index + 1).join('/');
    },

    // 文件上传前检查
    beforeUpload(/* file */) {
      this.uploading = true;
      return true;
    },

    // 文件上传成功
    handleUploadSuccess(response, /* file */) {
      this.uploading = false;
      if (response.code === 200) {
        this.$message.success('文件上传成功');
        // 刷新文件列表
        this.loadFileList();
        // 更新文件统计
        this.loadFileStats();
      } else {
        this.$message.error(response.message || '上传失败');
      }
    },

    // 文件上传失败
    handleUploadError(error) {
      this.uploading = false;
      console.error('上传文件失败', error);
      this.$message.error('上传文件失败: ' + (error.message || error));
    },

    // 表格选择变化
    handleSelectionChange(selection) {
      this.selectedFiles = selection;
    },

    // 格式化文件大小
    formatFileSize(size) {
      if (size === 0) return '0 B';

      const units = ['B', 'KB', 'MB', 'GB', 'TB'];
      const i = Math.floor(Math.log(size) / Math.log(1024));
      return (size / Math.pow(1024, i)).toFixed(2) + ' ' + units[i];
    },

    // 格式化日期
    formatDate(timestamp) {
      const date = new Date(timestamp);
      return date.toLocaleString();
    },

    // 获取文件图标
    getFileIcon(fileName) {
      if (!fileName) return 'el-icon-document';

      const extension = fileName.split('.').pop().toLowerCase();

      const iconMap = {
        // 图片
        jpg: 'el-icon-picture',
        jpeg: 'el-icon-picture',
        png: 'el-icon-picture',
        gif: 'el-icon-picture',
        bmp: 'el-icon-picture',
        svg: 'el-icon-picture',

        // 文档
        doc: 'el-icon-document',
        docx: 'el-icon-document',
        pdf: 'el-icon-document',
        txt: 'el-icon-document-copy',
        md: 'el-icon-document-copy',

        // 表格
        xls: 'el-icon-tickets',
        xlsx: 'el-icon-tickets',
        csv: 'el-icon-tickets',

        // 压缩文件
        zip: 'el-icon-suitcase',
        rar: 'el-icon-suitcase',
        '7z': 'el-icon-suitcase',
        tar: 'el-icon-suitcase',
        gz: 'el-icon-suitcase',

        // 音频
        mp3: 'el-icon-headset',
        wav: 'el-icon-headset',
        ogg: 'el-icon-headset',

        // 视频
        mp4: 'el-icon-video-camera',
        avi: 'el-icon-video-camera',
        mov: 'el-icon-video-camera',
        wmv: 'el-icon-video-camera',

        // 代码
        js: 'el-icon-s-order',
        css: 'el-icon-s-order',
        html: 'el-icon-s-order',
        java: 'el-icon-s-order',
        py: 'el-icon-s-order',

        // 默认
        default: 'el-icon-document'
      };

      return iconMap[extension] || iconMap.default;
    }
  }
}
</script>

<style scoped>
.file-manager {
  padding: 20px;
}

.path-breadcrumb {
  margin: 20px 0;
  font-size: 16px;
}

.file-tools {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.file-list {
  margin-top: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.file-name-cell {
  display: flex;
  align-items: center;
}

.file-icon {
  margin-right: 10px;
  font-size: 18px;
}

.folder-name {
  color: #409eff;
  cursor: pointer;
}

.folder-name:hover {
  text-decoration: underline;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stats-item {
  display: flex;
  align-items: center;
}

.stats-label {
  font-weight: bold;
  margin-right: 8px;
}

.stats-types {
  margin-top: 10px;
}

.stats-types h4 {
  margin-bottom: 8px;
}
</style> 