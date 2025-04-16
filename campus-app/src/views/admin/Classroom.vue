<template>
  <div class="classroom-container">
    <div class="page-header">
      <h2>教室管理</h2>
      <el-button type="primary" @click="handleAdd">添加教室</el-button>
    </div>

    <el-card class="classroom-card">
      <el-table :data="classrooms" border style="width: 100%">
        <el-table-column label="教学楼" prop="building"/>
        <el-table-column label="教室号" prop="room"/>
        <el-table-column label="容量" prop="capacity"/>
        <el-table-column label="类型" prop="type">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">{{ getTypeText(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- 添加/编辑教室对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="教学楼" prop="building">
          <el-input v-model="form.building"/>
        </el-form-item>
        <el-form-item label="教室号" prop="room">
          <el-input v-model="form.room"/>
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :max="300" :min="20"/>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="普通教室" value="normal"/>
            <el-option label="实验室" value="lab"/>
            <el-option label="多媒体教室" value="multimedia"/>
            <el-option label="阶梯教室" value="lecture"/>
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="equipment">
          <el-input
              v-model="form.equipment"
              :rows="3"
              placeholder="请输入教室设备信息"
              type="textarea"
          />
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
import {addClassroom, deleteClassroom, getClassroomList, updateClassroom} from '@/api/classroom'

export default {
  name: 'AdminClassroom',
  setup() {
    const classrooms = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const formRef = ref(null)
    const form = reactive({
      id: '',
      building: '',
      room: '',
      capacity: 60,
      type: 'normal',
      equipment: '',
      status: 1
    })

    const rules = {
      building: [{required: true, message: '请输入教学楼', trigger: 'blur'}],
      room: [{required: true, message: '请输入教室号', trigger: 'blur'}],
      capacity: [{required: true, message: '请输入容量', trigger: 'blur'}],
      type: [{required: true, message: '请选择类型', trigger: 'change'}]
    }

    const fetchClassrooms = () => {
      getClassroomList({
        page: currentPage.value - 1,
        size: pageSize.value
      }).then(response => {
        classrooms.value = response.data.content
        total.value = response.data.totalElements
      }).catch((error) => {
        console.error('获取教室列表失败', error)
        ElMessage.error('获取教室列表失败')
      })
    }

    const getTypeTag = (type) => {
      const tags = {
        normal: '',
        lab: 'danger',
        multimedia: 'success',
        lecture: 'warning'
      }
      return tags[type] || 'info'
    }

    const getTypeText = (type) => {
      const texts = {
        normal: '普通教室',
        lab: '实验室',
        multimedia: '多媒体教室',
        lecture: '阶梯教室'
      }
      return texts[type] || type
    }

    const handleAdd = () => {
      dialogTitle.value = '添加教室'
      dialogVisible.value = true
      Object.keys(form).forEach(key => {
        form[key] = ''
      })
      form.capacity = 60
      form.type = 'normal'
      form.status = 1
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑教室'
      dialogVisible.value = true
      Object.keys(form).forEach(key => {
        form[key] = row[key]
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该教室吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteClassroom(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchClassrooms()
        }).catch((error) => {
          console.error('删除教室失败', error)
          ElMessage.error('删除教室失败')
        })
      })
    }

    const handleSubmit = () => {
      formRef.value.validate((valid) => {
        if (valid) {
          if (form.id) {
            updateClassroom(form.id, form).then(() => {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              fetchClassrooms()
            }).catch((error) => {
              console.error('更新教室失败', error)
              ElMessage.error('更新教室失败')
            })
          } else {
            addClassroom(form).then(() => {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              fetchClassrooms()
            }).catch((error) => {
              console.error('添加教室失败', error)
              ElMessage.error('添加教室失败')
            })
          }
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchClassrooms()
    }

    onMounted(() => {
      fetchClassrooms()
    })

    return {
      classrooms,
      total,
      pageSize,
      currentPage,
      dialogVisible,
      dialogTitle,
      formRef,
      form,
      rules,
      getTypeTag,
      getTypeText,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.classroom-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.classroom-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 