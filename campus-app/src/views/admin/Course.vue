<template>
  <div class="course-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="handleAdd">添加课程</el-button>
    </div>

    <el-card class="course-card">
      <el-table :data="courses" border style="width: 100%">
        <el-table-column label="课程名称" prop="name"/>
        <el-table-column label="课程代码" prop="code"/>
        <el-table-column label="授课教师" prop="teacher"/>
        <el-table-column label="所属院系" prop="department"/>
        <el-table-column label="学分" prop="credit"/>
        <el-table-column label="学时" prop="hours"/>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停开' }}
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
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="courseForm.teacherId" style="width: 100%">
            <el-option
                v-for="teacher in teachers"
                :key="teacher.id"
                :label="teacher.name"
                :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属院系" prop="department">
          <el-input v-model="courseForm.department"/>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :max="10" :min="1"/>
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="courseForm.hours" :max="64" :min="16"/>
        </el-form-item>
        <el-form-item label="课程简介" prop="description">
          <el-input
              v-model="courseForm.description"
              :rows="4"
              placeholder="请输入课程简介"
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
import {addCourse, deleteCourse, getCourseList, updateCourse} from '@/api/course'
import {getUserList} from '@/api/user'

export default {
  name: 'AdminCourse',
  setup() {
    const courses = ref([])
    const teachers = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const courseFormRef = ref(null)
    const courseForm = reactive({
      id: '',
      name: '',
      code: '',
      teacherId: '',
      department: '',
      credit: 2,
      hours: 32,
      description: ''
    })

    const rules = {
      name: [{required: true, message: '请输入课程名称', trigger: 'blur'}],
      code: [{required: true, message: '请输入课程代码', trigger: 'blur'}],
      teacherId: [{required: true, message: '请选择授课教师', trigger: 'change'}],
      department: [{required: true, message: '请输入所属院系', trigger: 'blur'}],
      credit: [{required: true, message: '请输入学分', trigger: 'blur'}],
      hours: [{required: true, message: '请输入学时', trigger: 'blur'}]
    }

    const fetchCourses = () => {
      getCourseList({
        page: currentPage.value - 1,
        size: pageSize.value
      }).then(response => {
        courses.value = response.data.content
        total.value = response.data.totalElements
      }).catch((error) => {
        console.error('获取课程列表失败', error)
        ElMessage.error('获取课程列表失败')
      })
    }

    const fetchTeachers = () => {
      getUserList({
        role: 'teacher',
        page: 0,
        size: 1000
      }).then(response => {
        teachers.value = response.data.content
      }).catch((error) => {
        console.error('获取教师列表失败', error)
        ElMessage.error('获取教师列表失败')
      })
    }

    const handleAdd = () => {
      dialogTitle.value = '添加课程'
      dialogVisible.value = true
      Object.keys(courseForm).forEach(key => {
        courseForm[key] = ''
      })
      courseForm.credit = 2
      courseForm.hours = 32
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑课程'
      dialogVisible.value = true
      Object.keys(courseForm).forEach(key => {
        courseForm[key] = row[key]
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该课程吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteCourse(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchCourses()
        }).catch((error) => {
          console.error('删除课程失败', error)
          ElMessage.error('删除课程失败')
        })
      })
    }

    const handleSubmit = () => {
      courseFormRef.value.validate((valid) => {
        if (valid) {
          if (courseForm.id) {
            updateCourse(courseForm.id, courseForm).then(() => {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              fetchCourses()
            }).catch((error) => {
              console.error('更新课程失败', error)
              ElMessage.error('更新课程失败')
            })
          } else {
            addCourse(courseForm).then(() => {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              fetchCourses()
            }).catch((error) => {
              console.error('添加课程失败', error)
              ElMessage.error('添加课程失败')
            })
          }
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchCourses()
    }

    onMounted(() => {
      fetchCourses()
      fetchTeachers()
    })

    return {
      courses,
      teachers,
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
.course-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.course-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 