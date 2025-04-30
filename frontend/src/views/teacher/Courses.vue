<template>
  <PageContainer title="我的课程">
    <!-- 可以添加按学期筛选 -->
    <!-- <template #header-actions>
      <el-select v-model="semesterFilter" placeholder="筛选学期" clearable @change="fetchCourses">
        <el-option v-for="item in semesters" :key="item.value" :label="item.label" :value="item.value"/>
      </el-select>
    </template> -->

    <TableView
        :data="courses"
        :loading="loadingCourses"
        border
    >
      <el-table-column
          label="课程代码"
          prop="courseCode"
          width="150"
      />
      <el-table-column
          label="课程名称"
          min-width="200"
          prop="courseName"
      />
      <el-table-column
          label="学分"
          prop="credit"
          width="80"
      />
      <el-table-column
          label="开课学期"
          prop="semester"
          width="180"
      />
      <el-table-column
          label="选课人数"
          prop="studentCount"
          width="100"
      />
      <el-table-column
          fixed="right"
          label="操作"
          width="200"
      >
        <template #default="scope">
          <el-button
              size="small"
              type="primary"
              @click="manageResources(scope.row)"
          >
            管理资源
          </el-button>
          <el-button
              size="small"
              type="success"
              @click="manageGrades(scope.row)"
          >
            查看成绩
          </el-button>
        </template>
      </el-table-column>
    </TableView>
  </PageContainer>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getTeacherCourses} from '@/api/course'
import PageContainer from '@/components/common/EnhancedPageContainer.vue';
import TableView from '@/components/common/TableView.vue';

const router = useRouter()
const loadingCourses = ref(false)
const courses = ref([])

// 获取课程列表
const fetchCourses = async () => {
  loadingCourses.value = true
  try {
    const res = await getTeacherCourses()
    courses.value = res.data || []
  } catch (error) {
    console.error("获取课程列表失败", error)
    ElMessage.error("获取课程列表失败")
    courses.value = []
  } finally {
    loadingCourses.value = false
  }
}

// 管理课程资源
const manageResources = (course) => {
  router.push({
    name: 'TeacherCourseResources',
    params: {courseId: course.id},
    query: {courseName: course.courseName}
  });
}

// 管理学生成绩
const manageGrades = (course) => {
  router.push({
    name: 'TeacherCourseGrades',
    params: {courseId: course.id},
    query: {courseName: course.courseName}
  });
}

// 组件挂载后加载数据
onMounted(async () => {
  await fetchCourses()
})
</script>

<script>
// 添加标准的 export default
export default {
  name: 'TeacherCourses' // 保持多词名称
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
  min-height: 300px;
}
</style> 