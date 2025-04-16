<template>
  <div class="notice-container">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="handleAdd">添加公告</el-button>
    </div>

    <el-card class="notice-card">
      <el-table :data="notices" border style="width: 100%">
        <el-table-column label="公告标题" prop="title"/>
        <el-table-column label="公告类型" prop="type">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布人" prop="publisher"/>
        <el-table-column label="发布时间" prop="publishTime"/>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '撤回' : '发布' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            background
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="700px"
    >
      <el-form ref="noticeFormRef" :model="noticeForm" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="noticeForm.title"/>
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="noticeForm.type" style="width: 100%">
            <el-option label="通知公告" value="notice"/>
            <el-option label="教务通知" value="academic"/>
            <el-option label="活动通知" value="activity"/>
            <el-option label="紧急通知" value="urgent"/>
          </el-select>
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
              v-model="noticeForm.content"
              :rows="10"
              placeholder="请输入公告内容"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="附件" prop="attachments">
          <el-upload
              :file-list="noticeForm.attachments"
              :on-remove="handleUploadRemove"
              :on-success="handleUploadSuccess"
              action="/api/upload"
              class="upload-demo"
              multiple
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持任意格式文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {addNotice, deleteNotice, getNoticeList, updateNotice} from '@/api/notice'

export default {
  name: 'AdminNotice',
  setup() {
    const notices = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const noticeFormRef = ref(null)
    const noticeForm = reactive({
      id: '',
      title: '',
      type: '',
      content: '',
      attachments: [],
      status: 0
    })

    const rules = {
      title: [{required: true, message: '请输入公告标题', trigger: 'blur'}],
      type: [{required: true, message: '请选择公告类型', trigger: 'change'}],
      content: [{required: true, message: '请输入公告内容', trigger: 'blur'}]
    }

    const fetchNotices = () => {
      getNoticeList({
        page: currentPage.value - 1,
        size: pageSize.value
      }).then(response => {
        notices.value = response.data.content
        total.value = response.data.totalElements
      }).catch((error) => {
        console.error('获取公告列表失败', error)
        ElMessage.error('获取公告列表失败')
      })
    }

    const getTypeTag = (type) => {
      const tags = {
        notice: 'info',
        academic: 'primary',
        activity: 'success',
        urgent: 'danger'
      }
      return tags[type] || 'info'
    }

    const getTypeName = (type) => {
      const names = {
        notice: '通知公告',
        academic: '教务通知',
        activity: '活动通知',
        urgent: '紧急通知'
      }
      return names[type] || type
    }

    const handleAdd = () => {
      dialogTitle.value = '添加公告'
      dialogVisible.value = true
      Object.keys(noticeForm).forEach(key => {
        noticeForm[key] = ''
      })
      noticeForm.attachments = []
      noticeForm.status = 0
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑公告'
      dialogVisible.value = true
      Object.keys(noticeForm).forEach(key => {
        noticeForm[key] = row[key]
      })
    }

    const handleToggleStatus = (row) => {
      const newStatus = row.status === 1 ? 0 : 1
      updateNotice(row.id, {...row, status: newStatus}).then(() => {
        ElMessage.success(newStatus === 1 ? '发布成功' : '已撤回')
        fetchNotices()
      }).catch((error) => {
        console.error('更新公告状态失败', error)
        ElMessage.error('操作失败')
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteNotice(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchNotices()
        }).catch((error) => {
          console.error('删除公告失败', error)
          ElMessage.error('删除失败')
        })
      })
    }

    const handleUploadSuccess = (response, file, fileList) => {
      noticeForm.attachments = fileList.map(file => ({
        name: file.name,
        url: file.response?.data || file.url
      }))
    }

    const handleUploadRemove = (file, fileList) => {
      noticeForm.attachments = fileList.map(file => ({
        name: file.name,
        url: file.response?.data || file.url
      }))
    }

    const handleSubmit = () => {
      noticeFormRef.value.validate((valid) => {
        if (valid) {
          if (noticeForm.id) {
            updateNotice(noticeForm.id, noticeForm).then(() => {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              fetchNotices()
            }).catch((error) => {
              console.error('更新公告失败', error)
              ElMessage.error('更新失败')
            })
          } else {
            addNotice(noticeForm).then(() => {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              fetchNotices()
            }).catch((error) => {
              console.error('添加公告失败', error)
              ElMessage.error('添加失败')
            })
          }
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchNotices()
    }

    onMounted(() => {
      fetchNotices()
    })

    return {
      notices,
      total,
      pageSize,
      currentPage,
      dialogVisible,
      dialogTitle,
      noticeFormRef,
      noticeForm,
      rules,
      getTypeTag,
      getTypeName,
      handleAdd,
      handleEdit,
      handleToggleStatus,
      handleDelete,
      handleUploadSuccess,
      handleUploadRemove,
      handleSubmit,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.notice-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notice-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 