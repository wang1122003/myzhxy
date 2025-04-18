<template>
  <div class="student-course-container">
    <div class="header">
      <h2>课程选择</h2>
      <div class="filter-container">
        <el-select
            v-model="semester"
            clearable
            placeholder="学期"
            @change="fetchCourses"
        >
          <el-option
              v-for="item in semesterOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>

        <el-select
            v-model="courseType"
            clearable
            placeholder="课程类型"
            @change="fetchCourses"
        >
          <el-option
              v-for="item in courseTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>

        <el-input
            v-model="searchKeyword"
            clearable
            placeholder="搜索课程名称或教师"
            @input="fetchCourses"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane
          label="可选课程"
          name="available"
      >
        <el-table
            v-loading="loading"
            :data="availableCourses"
            style="width: 100%"
        >
          <el-table-column
              label="课程代码"
              prop="courseCode"
              width="120"
          />
          <el-table-column
              label="课程名称"
              prop="courseName"
              width="180"
          />
          <el-table-column
              label="授课教师"
              prop="teacher"
              width="120"
          />
          <el-table-column
              label="课程类型"
              prop="courseType"
              width="120"
          >
            <template #default="scope">
              <el-tag :type="getCourseTypeTag(scope.row.courseType)">
                {{ getCourseTypeName(scope.row.courseType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
              label="学分"
              prop="credit"
              width="80"
          />
          <el-table-column
              label="学期"
              prop="semester"
              width="120"
          />
          <el-table-column
              label="上课时间"
              min-width="180"
              prop="schedule"
          />
          <el-table-column
              label="上课地点"
              prop="classroom"
              width="120"
          />
          <el-table-column
              label="容量"
              prop="capacity"
              width="120"
          >
            <template #default="scope">
              {{ scope.row.selectedCount }}/{{ scope.row.capacity }}
              <el-progress
                  :percentage="(scope.row.selectedCount / scope.row.capacity) * 100"
                  :status="getCapacityStatus(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              width="120"
          >
            <template #default="scope">
              <el-button
                  :disabled="scope.row.selectedCount >= scope.row.capacity || scope.row.isConflict"
                  :loading="submitting"
                  size="small"
                  type="primary"
                  @click="selectCourse(scope.row)"
              >
                选课
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 30, 50]"
              :total="totalAvailable"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane
          label="已选课程"
          name="selected"
      >
        <el-table
            v-loading="loadingSelected"
            :data="selectedCourses"
            style="width: 100%"
        >
          <el-table-column
              label="课程代码"
              prop="courseCode"
              width="120"
          />
          <el-table-column
              label="课程名称"
              prop="courseName"
              width="180"
          />
          <el-table-column
              label="授课教师"
              prop="teacher"
              width="120"
          />
          <el-table-column
              label="课程类型"
              prop="courseType"
              width="120"
          >
            <template #default="scope">
              <el-tag :type="getCourseTypeTag(scope.row.courseType)">
                {{ getCourseTypeName(scope.row.courseType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
              label="学分"
              prop="credit"
              width="80"
          />
          <el-table-column
              label="学期"
              prop="semester"
              width="120"
          />
          <el-table-column
              label="上课时间"
              min-width="180"
              prop="schedule"
          />
          <el-table-column
              label="上课地点"
              prop="classroom"
              width="120"
          />
          <el-table-column
              label="选课时间"
              prop="selectTime"
              width="180"
          />
          <el-table-column
              fixed="right"
              label="操作"
              width="120"
          >
            <template #default="scope">
              <el-button
                  :loading="submitting"
                  size="small"
                  type="danger"
                  @click="dropCourse(scope.row)"
              >
                退课
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 课程详情对话框 -->
    <el-dialog
        v-model="courseDetailVisible"
        title="课程详情"
        width="60%"
    >
      <template v-if="currentCourse">
        <el-descriptions
            :column="2"
            border
        >
          <el-descriptions-item label="课程代码">
            {{ currentCourse.courseCode }}
          </el-descriptions-item>
          <el-descriptions-item label="课程名称">
            {{ currentCourse.courseName }}
          </el-descriptions-item>
          <el-descriptions-item label="课程类型">
            <el-tag :type="getCourseTypeTag(currentCourse.courseType)">
              {{ getCourseTypeName(currentCourse.courseType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学分">
            {{ currentCourse.credit }}
          </el-descriptions-item>
          <el-descriptions-item label="授课教师">
            {{ currentCourse.teacher }}
          </el-descriptions-item>
          <el-descriptions-item label="学期">
            {{ currentCourse.semester }}
          </el-descriptions-item>
          <el-descriptions-item
              :span="2"
              label="上课时间"
          >
            {{ currentCourse.schedule }}
          </el-descriptions-item>
          <el-descriptions-item label="上课地点">
            {{ currentCourse.classroom }}
          </el-descriptions-item>
          <el-descriptions-item label="课程容量">
            {{ currentCourse.selectedCount }}/{{ currentCourse.capacity }}
            <el-progress
                :percentage="(currentCourse.selectedCount / currentCourse.capacity) * 100"
                :status="getCapacityStatus(currentCourse)"
            />
          </el-descriptions-item>
          <el-descriptions-item
              :span="2"
              label="课程简介"
          >
            {{ currentCourse.description || '暂无课程简介' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {Search} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  dropCourse as apiDropCourse,
  getAvailableCourses,
  getSelectedCourses,
  selectCourse as apiSelectCourse
} from '@/api/course'

// 可选择的学期选项
const semesterOptions = ref([
  {value: '2023-2024-1', label: '2023-2024学年第一学期'},
  {value: '2023-2024-2', label: '2023-2024学年第二学期'},
  {value: '2024-2025-1', label: '2024-2025学年第一学期'}
])

// 课程类型选项
const courseTypeOptions = ref([
  {value: 'REQUIRED', label: '必修课'},
  {value: 'ELECTIVE', label: '选修课'},
  {value: 'GENERAL', label: '通识课'},
  {value: 'PHYSICAL', label: '体育课'}
])

// 响应式数据
const semester = ref(semesterOptions.value[0].value)
const courseType = ref('')
const searchKeyword = ref('')
const activeTab = ref('available')
const availableCourses = ref([])
const selectedCourses = ref([])
const loading = ref(false)
const loadingSelected = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalAvailable = ref(0)
const courseDetailVisible = ref(false)
const currentCourse = ref(null)
const submitting = ref(false)

// 获取可选课程
const fetchCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      semester: semester.value,
      courseType: courseType.value,
      keyword: searchKeyword.value
    }

    const res = await getAvailableCourses(params)
    availableCourses.value = res.data.records.map(course => ({
      ...course,
      isConflict: checkTimeConflict(course)
    }))
    totalAvailable.value = res.data.total
  } catch (error) {
    console.error('获取可选课程失败', error)
    ElMessage.error('获取可选课程失败')
  } finally {
    loading.value = false
  }
}

// 获取已选课程
const fetchSelectedCourses = async () => {
  loadingSelected.value = true
  try {
    const res = await getSelectedCourses({semester: semester.value})
    selectedCourses.value = res.data
  } catch (error) {
    console.error('获取已选课程失败', error)
    ElMessage.error('获取已选课程失败')
  } finally {
    loadingSelected.value = false
  }
}

// 检查时间冲突
const checkTimeConflict = (course) => {
  if (!selectedCourses.value.length) return false

  // 这里简化处理，实际应该解析课程时间并检查冲突
  // 例如：周一(1-2)、周三(3-4)这样的格式需要解析后比较
  return selectedCourses.value.some(selected => {
    return selected.schedule === course.schedule
  })
}

// 选课
const selectCourse = async (course) => {
  submitting.value = true
  try {
    await ElMessageBox.confirm(
        `确认选择课程《${course.courseName}》吗？`,
        '选课确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'info'
        }
    )

    await apiSelectCourse(course.id)
    ElMessage.success('选课成功')
    fetchCourses()
    fetchSelectedCourses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('选课失败', error)
      ElMessage.error(error.response?.data?.message || '选课失败')
    }
  } finally {
    submitting.value = false
  }
}

// 退课
const dropCourse = async (course) => {
  submitting.value = true
  try {
    await ElMessageBox.confirm(
        `确认退选课程《${course.courseName}》吗？`,
        '退课确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await apiDropCourse(course.id)
    ElMessage.success('退课成功')
    fetchSelectedCourses()
    fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退课失败', error)
      ElMessage.error(error.response?.data?.message || '退课失败')
    }
  } finally {
    submitting.value = false
  }
}

// 查看课程详情
const viewCourseDetail = (course) => {
  currentCourse.value = course
  courseDetailVisible.value = true
}

// 处理分页变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchCourses()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchCourses()
}

// 获取课程类型标签样式
const getCourseTypeTag = (type) => {
  const map = {
    'REQUIRED': 'danger',
    'ELECTIVE': 'success',
    'GENERAL': 'info',
    'PHYSICAL': 'warning'
  }
  return map[type] || 'info'
}

// 获取课程类型名称
const getCourseTypeName = (type) => {
  const map = {
    'REQUIRED': '必修课',
    'ELECTIVE': '选修课',
    'GENERAL': '通识课',
    'PHYSICAL': '体育课'
  }
  return map[type] || '未知'
}

// 获取容量状态
const getCapacityStatus = (course) => {
  const ratio = course.selectedCount / course.capacity
  if (ratio >= 1) return 'exception'
  if (ratio >= 0.8) return 'warning'
  return 'success'
}

// 页面加载时获取数据
onMounted(() => {
  fetchCourses()
  fetchSelectedCourses()
})
</script>

<style scoped>
.student-course-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  gap: 15px;
}

.filter-container .el-select {
  width: 180px;
}

.filter-container .el-input {
  width: 220px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 