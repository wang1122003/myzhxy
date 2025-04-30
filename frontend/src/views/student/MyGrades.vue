<template>
  <div class="page-container">
    <h1>成绩查询</h1>

    <div class="action-bar">
      <el-button type="primary" @click="refreshData">
        <el-icon>
          <Search/>
        </el-icon>
        查询
      </el-button>
      <el-button type="info" @click="resetData">
        <el-icon>
          <Refresh/>
        </el-icon>
        重置
      </el-button>
    </div>

    <div class="tab-container">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="成绩列表" name="scores">
          <!-- 移动端卡片视图 -->
          <div v-if="isMobileView" class="grades-mobile-view">
            <div v-loading="loading">
              <div v-if="tableData.length === 0" class="no-data">
                <el-empty description="暂无成绩数据"/>
              </div>

              <div v-else>
                <el-card v-for="grade in tableData" :key="grade.id || Math.random()" class="grade-card">
                  <div class="grade-card-header">
                    <div class="course-name">{{ grade.courseName }}</div>
                    <div :class="getGradeClass(grade.grade)" class="grade-value">{{ grade.grade || '--' }}</div>
                  </div>

                  <div class="grade-info">
                    <div class="info-item">
                      <span class="label">课程代码:</span>
                      <span class="value">{{ grade.courseCode }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">学分:</span>
                      <span class="value">{{ grade.credit }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">绩点:</span>
                      <span class="value">{{ grade.gradePoint }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">学期:</span>
                      <span class="value">{{ grade.term }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">课程类型:</span>
                      <span class="value">{{ grade.courseType }}</span>
                    </div>
                  </div>
                </el-card>

                <div class="mobile-pagination">
                  <el-pagination
                      :current-page="currentPage"
                      :page-size="pageSize"
                      :total="totalItems"
                      layout="prev, pager, next"
                      @current-change="handleCurrentChange"
                  ></el-pagination>
                </div>
              </div>
            </div>
          </div>

          <!-- 桌面端表格视图 -->
          <div v-else>
            <el-table
                v-loading="loading"
                :data="tableData"
                border
                stripe
                style="width: 100%"
            >
              <el-table-column label="课程名称" min-width="180" prop="courseName"></el-table-column>
              <el-table-column label="课程代码" min-width="120" prop="courseCode"></el-table-column>
              <el-table-column label="学分" min-width="80" prop="credit"></el-table-column>
              <el-table-column label="成绩" min-width="80" prop="grade"></el-table-column>
              <el-table-column label="绩点" min-width="80" prop="gradePoint"></el-table-column>
              <el-table-column label="学期" min-width="120" prop="term"></el-table-column>
              <el-table-column label="课程类型" min-width="120" prop="courseType"></el-table-column>
              <el-table-column label="考试类型" min-width="120" prop="examType"></el-table-column>
            </el-table>

            <div v-if="totalItems > 0" class="pagination-container">
              <el-pagination
                  :current-page="currentPage"
                  :page-size="pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="totalItems"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
              ></el-pagination>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import {onMounted, ref} from 'vue'
import {Refresh, Search} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {getMyScores} from '@/api/grade.js'

export default {
  name: 'MyGrades',
  components: {
    Search,
    Refresh
  },
  setup() {
    const loading = ref(false)
    const tableData = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalItems = ref(0)
    const activeTab = ref('scores')
    const isMobileView = ref(window.innerWidth < 768)

    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      isMobileView.value = window.innerWidth < 768
    })

    // 获取成绩数据
    const fetchData = async () => {
      loading.value = true
      try {
        console.log('开始请求成绩数据')
        const response = await getMyScores({
          page: currentPage.value,
          size: pageSize.value
        })

        console.log('成绩数据响应:', response)

        // 根据实际数据结构处理数据
        if (response && response.records) {
          // response是分页格式
          tableData.value = response.records.map(formatScoreItem)
          totalItems.value = response.total || 0
        } else if (response && Array.isArray(response)) {
          // response直接是数组
          tableData.value = response.map(formatScoreItem)
          totalItems.value = response.length
        } else if (response && response.data && response.data.records) {
          // 嵌套格式
          tableData.value = response.data.records.map(formatScoreItem)
          totalItems.value = response.data.total || 0
        } else {
          // 尝试直接使用响应
          if (Array.isArray(response)) {
            tableData.value = response.map(formatScoreItem)
            totalItems.value = response.length
          } else {
            console.error('未识别的响应格式:', response)
            tableData.value = []
            totalItems.value = 0
            ElMessage.warning('无法识别返回的成绩数据格式')
          }
        }

        if (tableData.value.length === 0) {
          ElMessage.info('暂无成绩数据')
        }
      } catch (error) {
        console.error('获取成绩列表失败:', error)
        ElMessage.error('获取成绩列表失败')
        tableData.value = []
        totalItems.value = 0
      } finally {
        loading.value = false
      }
    }

    // 格式化成绩项
    const formatScoreItem = (item) => {
      console.log('格式化成绩项:', item)

      // 检查item.course是否为空
      if (!item.course && item.courseId) {
        // 尝试从其他字段提取课程信息
        console.log('成绩数据中缺少课程对象，尝试从其他字段提取信息')
      }

      return {
        id: item.id,
        // 直接从item本身提取更多可能的字段名
        courseName: item.courseName || (item.course && item.course.courseName) || '未知课程',
        courseCode: item.courseCode || (item.course && item.course.courseCode) || '--',
        credit: item.credit || (item.course && item.course.credit) || '--',
        grade: item.grade || item.totalScore || item.score || '--', // 尝试多种可能的字段名
        term: item.termInfo || item.term || '--',
        courseType: item.courseType || (item.course && item.course.courseType) || '--',
        examType: item.examType || item.scoreType || '--',
        gradePoint: item.gradePoint || item.gpa || convertGradeToPoint(item.grade || item.totalScore || item.score) || '--'
      }
    }

    // 将字母成绩或百分制成绩转换为绩点
    const convertGradeToPoint = (grade) => {
      if (!grade) return null;

      // 如果已经是绩点格式，直接返回
      if (!isNaN(grade) && grade <= 4.0) return grade;

      // 处理字母成绩
      const letterMap = {
        'A+': 4.0, 'A': 4.0, 'A-': 3.7,
        'B+': 3.3, 'B': 3.0, 'B-': 2.7,
        'C+': 2.3, 'C': 2.0, 'C-': 1.7,
        'D+': 1.3, 'D': 1.0,
        'F': 0
      };

      if (letterMap[grade]) return letterMap[grade];

      // 处理百分制成绩
      const numGrade = parseFloat(grade);
      if (isNaN(numGrade)) return null;

      if (numGrade >= 90) return 4.0;
      if (numGrade >= 85) return 3.7;
      if (numGrade >= 82) return 3.3;
      if (numGrade >= 78) return 3.0;
      if (numGrade >= 75) return 2.7;
      if (numGrade >= 72) return 2.3;
      if (numGrade >= 68) return 2.0;
      if (numGrade >= 66) return 1.7;
      if (numGrade >= 64) return 1.3;
      if (numGrade >= 60) return 1.0;
      return 0;
    }

    const refreshData = () => {
      currentPage.value = 1
      fetchData()
    }

    const resetData = () => {
      currentPage.value = 1
      pageSize.value = 10
      fetchData()
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchData()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchData()
    }

    // 根据成绩返回对应的CSS类名
    const getGradeClass = (grade) => {
      if (!grade) return ''
      const numGrade = parseFloat(grade)
      if (numGrade >= 90) return 'grade-excellent'
      if (numGrade >= 80) return 'grade-good'
      if (numGrade >= 70) return 'grade-average'
      if (numGrade >= 60) return 'grade-pass'
      return 'grade-fail'
    }

    onMounted(() => {
      fetchData()
    })

    return {
      loading,
      tableData,
      currentPage,
      pageSize,
      totalItems,
      activeTab,
      isMobileView,
      fetchData,
      refreshData,
      resetData,
      handleSizeChange,
      handleCurrentChange,
      getGradeClass
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.tab-container {
  background: white;
  padding: 20px;
  border-radius: 5px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 移动端样式 */
.grades-mobile-view {
  width: 100%;
}

.grade-card {
  margin-bottom: 16px;
  border-radius: 8px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.grade-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.grade-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.course-name {
  font-weight: bold;
  font-size: 16px;
  max-width: 70%;
}

.grade-value {
  font-size: 18px;
  font-weight: bold;
  padding: 3px 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.grade-excellent {
  color: #67c23a;
  background-color: rgba(103, 194, 58, 0.1);
}

.grade-good {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
}

.grade-average {
  color: #e6a23c;
  background-color: rgba(230, 162, 60, 0.1);
}

.grade-pass {
  color: #909399;
  background-color: rgba(144, 147, 153, 0.1);
}

.grade-fail {
  color: #f56c6c;
  background-color: rgba(245, 108, 108, 0.1);
}

.grade-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.label {
  color: #909399;
  font-size: 12px;
  margin-bottom: 2px;
}

.value {
  font-size: 14px;
}

.mobile-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.no-data {
  padding: 20px 0;
}

@media (max-width: 480px) {
  .page-container {
    padding: 10px;
  }

  .tab-container {
    padding: 10px;
  }

  .grade-info {
    grid-template-columns: 1fr;
  }

  .action-bar {
    flex-wrap: wrap;
  }
}
</style> 