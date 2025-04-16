<template>
  <div class="course-container">
    <div class="course-header">
      <h2>我的课程</h2>
      <el-select v-model="currentSemester" placeholder="选择学期" @change="fetchCourses">
        <el-option
            v-for="item in semesterOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
    </div>

    <!-- 课程列表 -->
    <div v-if="!loading" class="course-list">
      <el-empty v-if="courses.length === 0" description="当前学期没有课程"/>
      <div v-else class="course-cards">
        <el-card
            v-for="course in courses"
            :key="course.id"
            :body-style="{ padding: '0px' }"
            class="course-card"
        >
          <div :style="{ backgroundColor: getCourseColor(course.id) }" class="course-card-header">
            <div class="course-code">{{ course.courseCode }}</div>
            <div class="course-type">{{ getCourseTypeName(course.courseType) }}</div>
          </div>
          <div class="course-card-content">
            <div class="course-name">{{ course.courseName }}</div>
            <div class="course-info">
              <div class="info-item">
                <el-icon>
                  <User/>
                </el-icon>
                <span>{{ course.studentCount || 0 }}名学生</span>
              </div>
              <div class="info-item">
                <el-icon>
                  <Clock/>
                </el-icon>
                <span>{{ course.credit }}学分 / {{ course.hours }}学时</span>
              </div>
              <div class="info-item">
                <el-icon>
                  <Calendar/>
                </el-icon>
                <span>{{ course.classTime || '时间待定' }}</span>
              </div>
              <div class="info-item">
                <el-icon>
                  <Location/>
                </el-icon>
                <span>{{ course.classroom || '地点待定' }}</span>
              </div>
            </div>
            <div class="course-actions">
              <el-button type="primary" @click="manageCourse(course)">课程管理</el-button>
              <el-button @click="viewStudents(course)">学生名单</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    <div v-else class="loading-container">
      <el-skeleton :rows="5" animated/>
    </div>

    <!-- 学生名单对话框 -->
    <el-dialog
        v-model="studentsDialogVisible"
        :title="`${currentCourse?.courseName || ''} - 学生名单`"
        width="800px"
    >
      <div class="student-list-header">
        <span>共 {{ studentList.length }} 名学生</span>
        <div class="student-actions">
          <el-input
              v-model="studentSearch"
              clearable
              placeholder="搜索学生"
              prefix-icon="Search"
              style="width: 200px; margin-right: 10px;"
          />
          <el-button type="primary" @click="exportStudentList">导出名单</el-button>
        </div>
      </div>
      <el-table :data="filteredStudents" border style="width: 100%">
        <el-table-column label="学号" prop="studentId" width="120"/>
        <el-table-column label="姓名" prop="studentName" width="100"/>
        <el-table-column label="性别" prop="gender" width="60">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '男' : scope.row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="专业" prop="major"/>
        <el-table-column label="班级" prop="className"/>
        <el-table-column label="选课时间" prop="enrollTime" width="160"/>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
                link
                type="primary"
                @click="viewStudentDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 学生详情对话框 -->
    <el-dialog
        v-model="studentDetailDialogVisible"
        :title="currentStudent?.studentName || '学生详情'"
        width="500px"
    >
      <template v-if="currentStudent">
        <el-descriptions :column="1" border title="">
          <el-descriptions-item label="学号">{{ currentStudent.studentId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentStudent.studentName }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ currentStudent.gender === 1 ? '男' : currentStudent.gender === 2 ? '女' : '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ currentStudent.grade }}</el-descriptions-item>
          <el-descriptions-item label="选课时间">{{ currentStudent.enrollTime }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentStudent.grades" class="student-grades">
          <div class="grades-title">成绩情况</div>
          <el-table :data="currentStudent.grades" border size="small" style="width: 100%">
            <el-table-column label="项目" prop="itemName"/>
            <el-table-column label="分数" prop="score" width="80"/>
            <el-table-column label="权重" prop="weight" width="80">
              <template #default="scope">{{ scope.row.weight }}%</template>
            </el-table-column>
            <el-table-column label="提交时间" prop="submitTime" width="160"/>
          </el-table>
        </div>

        <div v-if="currentStudent.attendance" class="student-attendance">
          <div class="attendance-title">出勤情况</div>
          <el-progress
              :color="getAttendanceColor(currentStudent.attendance)"
              :format="() => `${currentStudent.attendance.present}/${currentStudent.attendance.total}`"
              :percentage="getAttendancePercentage(currentStudent.attendance)"
          ></el-progress>
          <div class="attendance-details">
            <span>出勤: {{ currentStudent.attendance.present }}</span>
            <span>缺勤: {{ currentStudent.attendance.absent }}</span>
            <span>请假: {{ currentStudent.attendance.leave }}</span>
            <span>迟到: {{ currentStudent.attendance.late }}</span>
          </div>
        </div>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="studentDetailDialogVisible = false">关闭</el-button>
          <el-button
              type="primary"
              @click="editStudentGrade(currentStudent)"
          >编辑成绩</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 课程管理对话框 -->
    <el-dialog
        v-model="courseManageDialogVisible"
        :title="`${currentCourse?.courseName || ''} - 课程管理`"
        width="800px"
    >
      <el-tabs>
        <el-tab-pane label="基本信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="课程名称">{{ currentCourse?.courseName }}</el-descriptions-item>
            <el-descriptions-item label="课程代码">{{ currentCourse?.courseCode }}</el-descriptions-item>
            <el-descriptions-item label="课程类型">{{
                getCourseTypeName(currentCourse?.courseType)
              }}
            </el-descriptions-item>
            <el-descriptions-item label="学分">{{ currentCourse?.credit }}</el-descriptions-item>
            <el-descriptions-item label="学时">{{ currentCourse?.hours }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ currentCourse?.semester }}</el-descriptions-item>
            <el-descriptions-item label="上课时间">{{ currentCourse?.classTime || '时间待定' }}</el-descriptions-item>
            <el-descriptions-item label="上课地点">{{ currentCourse?.classroom || '地点待定' }}</el-descriptions-item>
            <el-descriptions-item label="学生人数">{{ currentCourse?.studentCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="最大容量">{{ currentCourse?.capacity || '无限制' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="教学大纲">
          <div class="syllabus-content">
            <div class="syllabus-item">
              <div class="syllabus-title">课程目标</div>
              <div class="syllabus-text">{{ currentCourse?.syllabus?.objectives || '暂无内容' }}</div>
            </div>
            <div class="syllabus-item">
              <div class="syllabus-title">教学内容</div>
              <div class="syllabus-text">{{ currentCourse?.syllabus?.content || '暂无内容' }}</div>
            </div>
            <div class="syllabus-item">
              <div class="syllabus-title">教学方法</div>
              <div class="syllabus-text">{{ currentCourse?.syllabus?.methods || '暂无内容' }}</div>
            </div>
            <div class="syllabus-item">
              <div class="syllabus-title">参考教材</div>
              <div class="syllabus-text">{{ currentCourse?.syllabus?.references || '暂无内容' }}</div>
            </div>
            <div class="syllabus-item">
              <div class="syllabus-title">成绩评定</div>
              <div class="syllabus-text">{{ currentCourse?.syllabus?.evaluation || '暂无内容' }}</div>
            </div>
          </div>
          <div class="syllabus-actions">
            <el-button type="primary" @click="editSyllabus">编辑大纲</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="作业管理">
          <div class="assignment-header">
            <el-button type="primary" @click="createAssignment">新建作业</el-button>
          </div>
          <el-table v-if="assignments.length > 0" :data="assignments" border style="width: 100%">
            <el-table-column label="标题" prop="title"/>
            <el-table-column label="截止日期" prop="deadline" width="160"/>
            <el-table-column label="状态" prop="status" width="100">
              <template #default="scope">
                <el-tag :type="getAssignmentStatusType(scope.row.status)">
                  {{ getAssignmentStatusName(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交情况" prop="submitCount" width="120">
              <template #default="scope">
                {{ scope.row.submitCount }}/{{ scope.row.totalCount }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button link type="primary" @click="viewAssignment(scope.row)">查看</el-button>
                <el-button link type="primary" @click="editAssignment(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="deleteAssignment(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无作业"/>
        </el-tab-pane>

        <el-tab-pane label="教学资料">
          <div class="material-header">
            <el-button type="primary" @click="uploadMaterial">上传资料</el-button>
          </div>
          <el-table v-if="materials.length > 0" :data="materials" border style="width: 100%">
            <el-table-column label="文件名" prop="fileName"/>
            <el-table-column label="大小" prop="fileSize" width="100">
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column label="上传时间" prop="uploadTime" width="160"/>
            <el-table-column label="下载次数" prop="downloadCount" width="100"/>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button link type="primary" @click="downloadMaterial(scope.row)">下载</el-button>
                <el-button link type="danger" @click="deleteMaterial(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无教学资料"/>
        </el-tab-pane>

        <el-tab-pane label="成绩管理">
          <div class="grade-header">
            <el-button type="primary" @click="createGradeItem">添加评分项</el-button>
            <el-button type="success" @click="exportGrades">导出成绩</el-button>
          </div>
          <el-table v-if="gradeItems.length > 0" :data="gradeItems" border style="width: 100%">
            <el-table-column label="评分项" prop="itemName"/>
            <el-table-column label="权重" prop="weight" width="80">
              <template #default="scope">{{ scope.row.weight }}%</template>
            </el-table-column>
            <el-table-column label="满分" prop="maxScore" width="80"/>
            <el-table-column label="已评分/总数" prop="gradedCount" width="120">
              <template #default="scope">
                {{ scope.row.gradedCount }}/{{ scope.row.totalCount }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button link type="primary" @click="gradeStudents(scope.row)">评分</el-button>
                <el-button link type="primary" @click="editGradeItem(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="deleteGradeItem(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无评分项"/>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getCourseById, getTeacherCourses} from '@/api/course'
import {getStudentDetail, getStudentsByCourse} from '@/api/user'
import {createAssignment, deleteAssignment, getCourseAssignments} from '@/api/assignment'
import {
  deleteMaterial as deleteMaterialApi,
  downloadMaterial as downloadMaterialApi,
  getCourseMaterials,
  uploadMaterial
} from '@/api/file'
import {createGradeItem, deleteGradeItem, exportGrades as exportGradesApi, getCourseGradeItems} from '@/api/grade'
import {exportStudentList as exportStudentListApi} from '@/api/export'
import {Calendar, Clock, Location, User} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const courses = ref([])
const currentCourse = ref(null)
const currentStudent = ref(null)
const studentList = ref([])
const assignments = ref([])
const materials = ref([])
const gradeItems = ref([])
const studentSearch = ref('')
const studentsDialogVisible = ref(false)
const studentDetailDialogVisible = ref(false)
const courseManageDialogVisible = ref(false)
const currentSemester = ref('')

// 课程列表颜色
const courseColors = [
  '#3498db', '#2ecc71', '#e74c3c', '#f39c12', '#9b59b6',
  '#1abc9c', '#d35400', '#34495e', '#27ae60', '#c0392b'
]

// 学期选项
const semesterOptions = [
  {value: '2023-2024-1', label: '2023-2024学年第一学期'},
  {value: '2023-2024-2', label: '2023-2024学年第二学期'},
  {value: '2022-2023-1', label: '2022-2023学年第一学期'},
  {value: '2022-2023-2', label: '2022-2023学年第二学期'}
]

// 过滤后的学生列表
const filteredStudents = computed(() => {
  if (!studentSearch.value) {
    return studentList.value
  }
  const search = studentSearch.value.toLowerCase()
  return studentList.value.filter(student =>
      student.studentId.includes(search) ||
      student.studentName.toLowerCase().includes(search) ||
      (student.major && student.major.toLowerCase().includes(search)) ||
      (student.className && student.className.toLowerCase().includes(search))
  )
})

// 获取课程类型名称
const getCourseTypeName = (type) => {
  const typeMap = {
    1: '必修课',
    2: '选修课',
    3: '公共课',
    4: '专业课'
  }
  return typeMap[type] || '未知'
}

// 获取课程颜色
const getCourseColor = (courseId) => {
  if (!courseId) return courseColors[0]
  // 使用课程ID的最后一个字符的ASCII码对颜色数组长度取模，获取索引
  const lastChar = courseId.toString().slice(-1)
  const index = lastChar.charCodeAt(0) % courseColors.length
  return courseColors[index]
}

// 获取出勤率百分比
const getAttendancePercentage = (attendance) => {
  if (!attendance || !attendance.total || attendance.total === 0) return 0
  return Math.round((attendance.present / attendance.total) * 100)
}

// 获取出勤率颜色
const getAttendanceColor = (attendance) => {
  const percentage = getAttendancePercentage(attendance)
  if (percentage >= 90) return '#67C23A'
  if (percentage >= 80) return '#E6A23C'
  return '#F56C6C'
}

// 获取作业状态类型
const getAssignmentStatusType = (status) => {
  const statusMap = {
    1: 'info',    // 未开始
    2: 'warning', // 进行中
    3: 'success', // 已截止
    4: 'danger'   // 已关闭
  }
  return statusMap[status] || ''
}

// 获取作业状态名称
const getAssignmentStatusName = (status) => {
  const statusMap = {
    1: '未开始',
    2: '进行中',
    3: '已截止',
    4: '已关闭'
  }
  return statusMap[status] || '未知'
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let value = size
  let unitIndex = 0

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex++
  }

  return `${value.toFixed(2)} ${units[unitIndex]}`
}

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    const params = {}
    if (currentSemester.value) {
      params.semester = currentSemester.value
    }
    const response = await getTeacherCourses(params)
    courses.value = response.data || []

    // 检查是否有指定的courseId查询参数
    if (route.query.courseId) {
      const courseId = route.query.courseId
      const course = courses.value.find(c => c.id.toString() === courseId.toString())
      if (course) {
        manageCourse(course)
      }
    }
  } catch (error) {
    console.error('获取课程列表失败', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 查看学生名单
const viewStudents = async (course) => {
  currentCourse.value = course
  studentsDialogVisible.value = true
  studentSearch.value = ''

  try {
    const response = await getStudentsByCourse(course.id)
    studentList.value = response.data || []
  } catch (error) {
    console.error('获取学生名单失败', error)
    ElMessage.error('获取学生名单失败')
  }
}

// 查看学生详情
const viewStudentDetail = async (student) => {
  try {
    const response = await getStudentDetail(student.id, currentCourse.value.id)
    currentStudent.value = response.data
    studentDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取学生详情失败', error)
    ElMessage.error('获取学生详情失败')
  }
}

// 编辑学生成绩
const editStudentGrade = (student) => {
  if (!student || !currentCourse.value) return
  router.push({
    path: '/teacher/grade',
    query: {
      courseId: currentCourse.value.id,
      studentId: student.id
    }
  })
}

// 管理课程
const manageCourse = async (course) => {
  currentCourse.value = course
  courseManageDialogVisible.value = true

  try {
    // 获取课程详情
    const response = await getCourseById(course.id)
    currentCourse.value = {...currentCourse.value, ...response.data}

    // 获取作业列表
    fetchAssignments()

    // 获取教学资料
    fetchMaterials()

    // 获取评分项
    fetchGradeItems()
  } catch (error) {
    console.error('获取课程详情失败', error)
    ElMessage.error('获取课程详情失败')
  }
}

// 获取作业列表
const fetchAssignments = async () => {
  try {
    const response = await getCourseAssignments(currentCourse.value.id)
    assignments.value = response.data || []
  } catch (error) {
    console.error('获取作业列表失败', error)
  }
}

// 获取教学资料
const fetchMaterials = async () => {
  try {
    const response = await getCourseMaterials(currentCourse.value.id)
    materials.value = response.data || []
  } catch (error) {
    console.error('获取教学资料失败', error)
  }
}

// 获取评分项
const fetchGradeItems = async () => {
  try {
    const response = await getCourseGradeItems(currentCourse.value.id)
    gradeItems.value = response.data || []
  } catch (error) {
    console.error('获取评分项失败', error)
  }
}

// 编辑教学大纲
const editSyllabus = () => {
  router.push({
    path: '/teacher/syllabus',
    query: {courseId: currentCourse.value.id}
  })
}

// 创建作业
const createAssignment = () => {
  router.push({
    path: '/teacher/assignment/create',
    query: {courseId: currentCourse.value.id}
  })
}

// 查看作业
const viewAssignment = (assignment) => {
  router.push({
    path: '/teacher/assignment/view',
    query: {assignmentId: assignment.id}
  })
}

// 编辑作业
const editAssignment = (assignment) => {
  router.push({
    path: '/teacher/assignment/edit',
    query: {assignmentId: assignment.id}
  })
}

// 删除作业
const deleteAssignment = async (assignment) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该作业吗？删除后将无法恢复。',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteAssignment(assignment.id)
    ElMessage.success('删除作业成功')
    fetchAssignments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除作业失败', error)
      ElMessage.error('删除作业失败')
    }
  }
}

// 上传教学资料
const uploadMaterial = () => {
  router.push({
    path: '/teacher/material/upload',
    query: {courseId: currentCourse.value.id}
  })
}

// 下载教学资料
const downloadMaterial = (material) => {
  downloadMaterialApi(material.id)
      .then(() => {
        ElMessage.success('下载成功')
      })
      .catch(() => {
        ElMessage.error('下载失败')
      })
}

// 删除教学资料
const deleteMaterial = async (material) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该资料吗？删除后将无法恢复。',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteMaterialApi(material.id)
    ElMessage.success('删除资料成功')
    fetchMaterials()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除资料失败', error)
      ElMessage.error('删除资料失败')
    }
  }
}

// 添加评分项
const createGradeItem = () => {
  router.push({
    path: '/teacher/grade/item/create',
    query: {courseId: currentCourse.value.id}
  })
}

// 编辑评分项
const editGradeItem = (item) => {
  router.push({
    path: '/teacher/grade/item/edit',
    query: {itemId: item.id}
  })
}

// 删除评分项
const deleteGradeItem = async (item) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该评分项吗？删除后将无法恢复，且已录入的成绩也会被删除。',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteGradeItem(item.id)
    ElMessage.success('删除评分项成功')
    fetchGradeItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评分项失败', error)
      ElMessage.error('删除评分项失败')
    }
  }
}

// 为学生评分
const gradeStudents = (item) => {
  router.push({
    path: '/teacher/grade/students',
    query: {
      courseId: currentCourse.value.id,
      itemId: item.id
    }
  })
}

// 导出成绩
const exportGrades = () => {
  exportGradesApi(currentCourse.value.id)
      .then(() => {
        ElMessage.success('成绩导出成功')
      })
      .catch(() => {
        ElMessage.error('成绩导出失败')
      })
}

// 导出学生名单
const exportStudentList = () => {
  exportStudentListApi(currentCourse.value.id)
      .then(() => {
        ElMessage.success('学生名单导出成功')
      })
      .catch(() => {
        ElMessage.error('学生名单导出失败')
      })
}

// 页面挂载时加载数据
onMounted(() => {
  // 默认选择当前学期
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const semester = month >= 2 && month <= 7 ? 2 : 1
  currentSemester.value = `${year - 1}-${year}-${semester}`

  fetchCourses()
})
</script>

<style scoped>
.course-container {
  padding: 20px;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.course-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.course-card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.course-card-header {
  padding: 15px;
  color: white;
  position: relative;
}

.course-code {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 5px;
}

.course-type {
  position: absolute;
  top: 15px;
  right: 15px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background-color: rgba(255, 255, 255, 0.2);
}

.course-card-content {
  padding: 15px;
}

.course-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
}

.course-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #606266;
}

.info-item i {
  margin-right: 8px;
}

.course-actions {
  display: flex;
  justify-content: space-between;
}

.student-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.student-actions {
  display: flex;
  align-items: center;
}

.syllabus-content {
  margin-bottom: 20px;
}

.syllabus-item {
  margin-bottom: 15px;
}

.syllabus-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.syllabus-text {
  color: #606266;
  white-space: pre-line;
}

.syllabus-actions {
  text-align: right;
}

.assignment-header, .material-header, .grade-header {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 15px;
  gap: 10px;
}

.student-grades, .student-attendance {
  margin-top: 20px;
}

.grades-title, .attendance-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.attendance-details {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}
</style> 