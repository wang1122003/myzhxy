<template>
  <div class="courses-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="handleAdd">添加课程</el-button>
    </div>

    <el-card class="courses-card">
      <el-table :data="courses" border style="width: 100%">
        <el-table-column label="课程名称" prop="name"/>
        <el-table-column label="课程代码" prop="code"/>
        <el-table-column label="班级" prop="className"/>
        <el-table-column label="上课时间" prop="time"/>
        <el-table-column label="教室" prop="classroom"/>
        <el-table-column label="学生人数" prop="studentCount"/>
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

    <!-- 添加/编辑课程对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
    >
      <el-form ref="courseFormRef" :model="courseForm" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name"/>
        </el-form-item>
        <el-form-item label="课程代码" prop="code">
          <el-input v-model="courseForm.code"/>
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="courseForm.className"/>
        </el-form-item>
        <el-form-item label="上课时间" prop="time">
          <el-input v-model="courseForm.time"/>
        </el-form-item>
        <el-form-item label="教室" prop="classroom">
          <el-input v-model="courseForm.classroom"/>
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
import {addCourse, deleteCourse, getTeacherCourses, updateCourse} from '@/api/course'

export default {
  name: 'TeacherCourses',
  setup() {
    const courses = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加课程')
    const courseFormRef = ref(null)
    const courseForm = reactive({
      id: '',
      name: '',
      code: '',
      className: '',
      time: '',
      classroom: ''
    })

    const rules = {
      name: [{required: true, message: '请输入课程名称', trigger: 'blur'}],
      code: [{required: true, message: '请输入课程代码', trigger: 'blur'}],
      className: [{required: true, message: '请输入班级', trigger: 'blur'}],
      time: [{required: true, message: '请输入上课时间', trigger: 'blur'}],
      classroom: [{required: true, message: '请输入教室', trigger: 'blur'}]
    }

    onMounted(() => {
      fetchCourses()
    })

    const fetchCourses = () => {
      getTeacherCourses({
        page: currentPage.value,
        pageSize: pageSize.value
      }).then(response => {
        courses.value = response.data.list
        total.value = response.data.total
      }).catch(() => {
        ElMessage.error('获取课程列表失败')
      })
    }

    const handleAdd = () => {
      dialogTitle.value = '添加课程'
      Object.keys(courseForm).forEach(key => {
        courseForm[key] = ''
      })
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑课程'
      Object.keys(courseForm).forEach(key => {
        courseForm[key] = row[key]
      })
      dialogVisible.value = true
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该课程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCourse(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchCourses()
        }).catch(() => {
          ElMessage.error('删除失败')
        })
      })
    }

    const handleSubmit = () => {
      courseFormRef.value.validate((valid) => {
        if (valid) {
          const api = courseForm.id ? updateCourse : addCourse
          api(courseForm).then(() => {
            ElMessage.success(courseForm.id ? '更新成功' : '添加成功')
            dialogVisible.value = false
            fetchCourses()
          }).catch(() => {
            ElMessage.error(courseForm.id ? '更新失败' : '添加失败')
          })
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchCourses()
    }

    return {
      courses,
      total,
      pageSize,
      currentPage,
      dialogVisible,
      dialogTitle,
      courseFormRef,
      courseForm,
      rules,
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
.courses-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.courses-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 