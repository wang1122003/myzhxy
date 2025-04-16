<template>
  <div class="file-manager">
    <h1>文件管理</h1>
    <div class="file-content">
      <div class="file-tools">
        <el-button size="small" type="primary">上传文件</el-button>
        <el-button size="small" type="danger">删除</el-button>
      </div>
      <div class="file-list">
        <el-table v-loading="loading" :data="fileList" style="width: 100%">
          <el-table-column label="文件名" prop="fileName"></el-table-column>
          <el-table-column label="大小" prop="fileSize"></el-table-column>
          <el-table-column label="上传时间" prop="uploadTime"></el-table-column>
          <el-table-column label="操作">
            <template #default>
              <el-button size="small" type="primary">下载</el-button>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FileManager',
  data() {
    return {
      loading: false,
      fileList: []
    }
  },
  created() {
    // 初始化数据
  },
  methods: {
    // 方法定义
  }
}
</script>

<style scoped>
.file-manager {
  padding: 20px;
}

.file-content {
  margin-top: 20px;
}

.file-tools {
  margin-bottom: 15px;
}
</style> 