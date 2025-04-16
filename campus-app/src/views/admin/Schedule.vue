<template>
  <div class="schedule-container">
    <div class="page-header">
      <h2>课表管理</h2>
      <el-button type="primary" @click="handleAdd">添加课表</el-button>
    </div>

    <el-card class="schedule-card">
      <el-table :data="schedules" border style="width: 100%">
        <el-table-column label="课程名称" prop="courseName"/>
        <el-table-column label="授课教师" prop="teacherName"/>
        <el-table-column label="教室" prop="classroom"/>
        <el-table-column label="星期" prop="weekday">
          <template #default="scope">
            {{ getWeekdayName(scope.row.weekday) }}
          </template>
        </el-table-column>
        <el-table-column label="开始时间" prop="startTime"/>
        <el-table-column label="结束时间" prop="endTime"/>
        <el-table-column label="学期" prop="semester"/>
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

    <!-- 添加/编辑课表对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
    >
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="rules" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="scheduleForm.courseId" style="width: 100%" @change="handleCourseChange">
            <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" prop="classroomId">
          <el-select v-model="scheduleForm.classroomId" style="width: 100%">
            <el-option
                v-for="classroom in classrooms"
                :key="classroom.id"
                :label="`${classroom.building}-${classroom.roomNumber}`"
                :value="classroom.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="星期" prop="weekday">
          <el-select v-model="scheduleForm.weekday" style="width: 100%">
            <el-option :value="1" label="星期一"/>
            <el-option :value="2" label="星期二"/>
            <el-option :value="3" label="星期三"/>
            <el-option :value="4" label="星期四"/>
            <el-option :value="5" label="星期五"/>
            <el-option :value="6" label="星期六"/>
            <el-option :value="7" label="星期日"/>
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
              v-model="scheduleForm.startTime"
              format="HH:mm"
              placeholder="选择时间"
              value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
              v-model="scheduleForm.endTime"
              format="HH:mm"
              placeholder="选择时间"
              value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="scheduleForm.semester"/>
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
import {addSchedule, deleteSchedule, getScheduleList, updateSchedule} from '@/api/schedule'
import {getCourseList} from '@/api/course'
import {getClassroomList} from '@/api/classroom'

export default {
  name: 'AdminSchedule',
  setup() {
    const schedules = ref([])
    const courses = ref([])
    const classrooms = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const scheduleFormRef = ref(null)
    const scheduleForm = reactive({
      id: '',
      courseId: '',
      classroomId: '',
      weekday: 1,
      startTime: '',
      endTime: '',
      semester: ''
    })

    const rules = {
      courseId: [{required: true, message: '请选择课程', trigger: 'change'}],
      classroomId: [{required: true, message: '请选择教室', trigger: 'change'}],
      weekday: [{required: true, message: '请选择星期', trigger: 'change'}],
      startTime: [{required: true, message: '请选择开始时间', trigger: 'change'}],
      endTime: [{required: true, message: '请选择结束时间', trigger: 'change'}],
      semester: [{required: true, message: '请输入学期', trigger: 'blur'}]
    }

    const fetchSchedules = () => {
      getScheduleList({
        page: currentPage.value - 1,
        size: pageSize.value
      }).then(response => {
        schedules.value = response.data.content
        total.value = response.data.totalElements
      }).catch((error) => {
        console.error('获取课表列表失败', error)
        ElMessage.error('获取课表列表失败')
      })
    }

    const fetchCourses = () => {
      getCourseList({
        page: 0,
        size: 1000
      }).then(response => {
        courses.value = response.data.content
      }).catch((error) => {
        console.error('获取课程列表失败', error)
        ElMessage.error('获取课程列表失败')
      })
    }

    const fetchClassrooms = () => {
      getClassroomList({
        page: 0,
        size: 1000
      }).then(response => {
        classrooms.value = response.data.content
      }).catch((error) => {
        console.error('获取教室列表失败', error)
        ElMessage.error('获取教室列表失败')
      })
    }

    const getWeekdayName = (weekday) => {
      const weekdays = ['', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']
      return weekdays[weekday] || ''
    }

    const handleCourseChange = (courseId) => {
      const course = courses.value.find(c => c.id === courseId)
      if (course) {
        scheduleForm.teacherId = course.teacherId
      }
    }

    const handleAdd = () => {
      dialogTitle.value = '添加课表'
      dialogVisible.value = true
      Object.keys(scheduleForm).forEach(key => {
        scheduleForm[key] = ''
      })
      scheduleForm.weekday = 1
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑课表'
      dialogVisible.value = true
      Object.keys(scheduleForm).forEach(key => {
        scheduleForm[key] = row[key]
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该课表吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteSchedule(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchSchedules()
        }).catch((error) => {
          console.error('删除课表失败', error)
          ElMessage.error('删除课表失败')
        })
      })
    }

    const handleSubmit = () => {
      scheduleFormRef.value.validate((valid) => {
        if (valid) {
          if (scheduleForm.id) {
            updateSchedule(scheduleForm.id, scheduleForm).then(() => {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              fetchSchedules()
            }).catch((error) => {
              console.error('更新课表失败', error)
              ElMessage.error('更新课表失败')
            })
          } else {
            addSchedule(scheduleForm).then(() => {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              fetchSchedules()
            }).catch((error) => {
              console.error('添加课表失败', error)
              ElMessage.error('添加课表失败')
            })
          }
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchSchedules()
    }

    onMounted(() => {
      fetchSchedules()
      fetchCourses()
      fetchClassrooms()
    })

    return {
      schedules,
      courses,
      classrooms,
      total,
      pageSize,
      currentPage,
      dialogVisible,
      dialogTitle,
      scheduleFormRef,
      scheduleForm,
      rules,
      getWeekdayName,
      handleCourseChange,
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
.schedule-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.schedule-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 