<template>
  <div class="courses-container">
    <div class="page-header">
      <h2>我的课程</h2>
      <!-- 可以添加按学期筛选 -->
      <!-- <el-select v-model="semesterFilter" placeholder="筛选学期" clearable @change="fetchCourses">
        <el-option v-for="item in semesters" :key="item.value" :label="item.label" :value="item.value"/>
      </el-select> -->
    </div>

    <el-card v-loading="loadingCourses" class="courses-card">
      <el-table :data="courses" border style="width: 100%">
        <el-table-column label="课程代码" prop="courseCode" width="150"/>
        <el-table-column label="课程名称" min-width="200" prop="courseName"/>
        <el-table-column label="学分" prop="credit" width="80"/>
        <el-table-column label="开课学期" prop="semester" width="180"/>
        <el-table-column label="选课人数" prop="studentCount" width="100"/>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="manageResources(scope.row)">管理资源</el-button>
            <el-button size="small" type="success" @click="manageGrades(scope.row)">查看成绩</el-button>
            <!-- 可以添加更多操作，如查看学生名单等 -->
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="courses.length === 0 && !loadingCourses" description="您当前没有教授任何课程"/>
    </el-card>

    <!-- 后续可能需要添加管理资源/成绩的对话框或子组件 -->

  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElButton, ElCard, ElEmpty, ElMessage, ElTable, ElTableColumn} from 'element-plus'
import {getTeacherCourses} from '@/api/course' // 引入 API
// import { getTerms } from '@/api/common'; // 如果需要学期筛选，取消注释

// 修改组件名称为多词组合
defineOptions({
  name: 'CourseManagement'
})

const router = useRouter()
const loadingCourses = ref(false)
const courses = ref([])
// const loadingSemesters = ref(false); // 如果需要学期筛选
// const semesters = ref([]); // 如果需要学期筛选
// const semesterFilter = ref(null); // 如果需要学期筛选

// 获取课程列表
const fetchCourses = async () => {
  loadingCourses.value = true
  try {
    // const params = { semester: semesterFilter.value }; // 如果需要学期筛选
    const res = await getTeacherCourses(/* params */) // 调用 API
    courses.value = res.data || []
  } catch (error) {
    console.error("获取课程列表失败", error)
    ElMessage.error("获取课程列表失败")
    courses.value = []
  } finally {
    loadingCourses.value = false
  }
}

// 获取学期列表 (如果需要筛选)
// const fetchSemesters = async () => { ... };

// 管理课程资源 (跳转到文件管理或特定页面)
const manageResources = (course) => {
  // 示例：跳转到学生文件管理页面，并带上课程 ID 作为筛选条件
  // 这需要 Files.vue 支持按课程筛选
  router.push({path: '/student/files', query: {courseId: course.id, tab: 'course'}})
  // 或者跳转到一个专门的课程资源管理页面
  // router.push({ name: 'TeacherCourseResources', params: { courseId: course.id } });
  ElMessage.info(`功能待实现：管理课程 ${course.courseName} 的资源`)
}

// 管理学生成绩 (跳转到成绩录入/查看页面)
const manageGrades = (course) => {
  // 跳转到一个专门的成绩管理页面
  // router.push({ name: 'TeacherCourseGrades', params: { courseId: course.id } });
  ElMessage.info(`功能待实现：管理/查看课程 ${course.courseName} 的学生成绩`)
}

// 组件挂载后加载数据
onMounted(async () => {
  // await fetchSemesters(); // 如果需要学期筛选
  await fetchCourses()
    }
)
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
  min-height: 300px;
}
</style> 