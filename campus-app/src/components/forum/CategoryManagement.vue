<template>
  <div class="category-management-container">
    <div class="header">
      <h3>板块管理</h3>
      <el-button type="primary" @click="handleAddCategory">
        <el-icon>
          <Plus/>
        </el-icon>
        添加板块
      </el-button>
    </div>

    <el-table
        v-loading="loading"
        :data="categories"
        border
        style="width: 100%"
    >
      <el-table-column label="板块名称" min-width="150" prop="name"/>
      <el-table-column label="排序" prop="sort" width="80"/>
      <el-table-column label="描述" min-width="200" prop="description" show-overflow-tooltip/>
      <el-table-column label="帖子数量" prop="postCount" width="100"/>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="150">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEditCategory(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDeleteCategory(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑板块对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑板块' : '添加板块'"
        width="500px"
    >
      <el-form
          ref="categoryFormRef"
          :model="categoryForm"
          :rules="rules"
          label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入板块名称"/>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :max="999" :min="0"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="categoryForm.description"
              :rows="3"
              placeholder="请输入板块描述"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="categoryForm.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button :loading="submitting" type="primary" @click="submitCategoryForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {
  createForumCategory,
  deleteForumCategory,
  getForumCategories,
  updateForumCategory,
  updateForumCategoryStatus
} from '@/api/forum'

export default {
  name: 'CategoryManagement',
  components: {
    Plus
  },
  emits: ['refresh'],
  setup(props, {emit}) {
    const loading = ref(false)
    const categories = ref([])
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const submitting = ref(false)
    const categoryFormRef = ref(null)
    const categoryForm = reactive({
      id: null,
      name: '',
      sort: 0,
      description: '',
      status: 1
    })

    const rules = {
      name: [
        {required: true, message: '请输入板块名称', trigger: 'blur'},
        {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
      ],
      sort: [
        {required: true, message: '请输入排序值', trigger: 'blur'}
      ]
    }

    // 获取板块列表
    const fetchCategories = async () => {
      loading.value = true
      try {
        const res = await getForumCategories()
        categories.value = res.data || []
      } catch (error) {
        console.error('获取板块列表失败:', error)
        ElMessage.error('获取板块列表失败')
      } finally {
        loading.value = false
      }
    }

    // 添加板块
    const handleAddCategory = () => {
      isEdit.value = false
      resetForm()
      dialogVisible.value = true
    }

    // 编辑板块
    const handleEditCategory = (row) => {
      isEdit.value = true
      resetForm()
      Object.assign(categoryForm, row)
      dialogVisible.value = true
    }

    // 删除板块
    const handleDeleteCategory = (row) => {
      ElMessageBox.confirm(`确定要删除板块 "${row.name}" 吗？删除后该板块下的所有帖子将被移动到默认板块。`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteForumCategory(row.id)
          ElMessage.success('删除成功')
          fetchCategories()
          emit('refresh')
        } catch (error) {
          console.error('删除板块失败:', error)
          ElMessage.error('删除板块失败')
        }
      }).catch(() => {
        ElMessage.info('已取消删除')
      })
    }

    // 更改板块状态
    const handleStatusChange = async (row) => {
      try {
        await updateForumCategoryStatus(row.id, row.status)
        ElMessage.success(`${row.status === 1 ? '启用' : '禁用'}成功`)
        emit('refresh')
      } catch (error) {
        console.error('更新板块状态失败:', error)
        ElMessage.error('更新状态失败')
        // 恢复原状态
        row.status = row.status === 1 ? 0 : 1
      }
    }

    // 提交表单
    const submitCategoryForm = async () => {
      if (!categoryFormRef.value) return

      await categoryFormRef.value.validate(async (valid) => {
        if (!valid) return

        submitting.value = true
        try {
          if (isEdit.value) {
            // 编辑板块
            await updateForumCategory(categoryForm.id, categoryForm)
            ElMessage.success('更新成功')
          } else {
            // 添加板块
            await createForumCategory(categoryForm)
            ElMessage.success('添加成功')
          }
          dialogVisible.value = false
          fetchCategories()
          emit('refresh')
        } catch (error) {
          console.error('保存板块失败:', error)
          ElMessage.error('保存失败')
        } finally {
          submitting.value = false
        }
      })
    }

    // 重置表单
    const resetForm = () => {
      if (categoryFormRef.value) {
        categoryFormRef.value.resetFields()
      }
      categoryForm.id = null
      categoryForm.name = ''
      categoryForm.sort = 0
      categoryForm.description = ''
      categoryForm.status = 1
    }

    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return '-'
      try {
        const date = new Date(timeStr)
        return date.toLocaleString('zh-CN', {hour12: false})
      } catch (e) {
        return timeStr
      }
    }

    onMounted(() => {
      fetchCategories()
    })

    return {
      loading,
      categories,
      dialogVisible,
      isEdit,
      submitting,
      categoryFormRef,
      categoryForm,
      rules,
      handleAddCategory,
      handleEditCategory,
      handleDeleteCategory,
      handleStatusChange,
      submitCategoryForm,
      formatTime
    }
  }
}
</script>

<style scoped>
.category-management-container {
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.header h3 {
  margin: 0;
}

.dialog-footer {
  text-align: right;
}
</style> 