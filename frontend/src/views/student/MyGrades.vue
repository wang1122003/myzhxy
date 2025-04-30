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
          <el-table
              v-loading="loading"
              style="width: 100%"
              :data="tableData"
              border
              stripe
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
                :page-sizes="[10, 20, 50, 100]"
                :page-size="pageSize"
                :total="totalItems"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            ></el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import {ref, onMounted} from 'vue'
import {Search, Refresh} from '@element-plus/icons-vue'
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

    // 获取成绩数据
    const fetchData = async () => {
      loading.value = true
      try {
        const response = await getMyScores({
          page: currentPage.value,
          size: pageSize.value
        })

        if (response.data && response.data.records) {
          tableData.value = response.data.records.map(item => ({
            ...item,
            courseName: item.course?.courseName || '未知课程',
            courseCode: item.course?.courseCode || '--',
            credit: item.course?.credit || '--',
            term: item.termInfo || '--',
            courseType: item.course?.courseType || '--',
            examType: item.scoreType || '--',
            gradePoint: item.gpa || '--'
          }))
          totalItems.value = response.data.total || 0
        } else {
          tableData.value = []
          totalItems.value = 0
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
      fetchData,
      refreshData,
      resetData,
      handleSizeChange,
      handleCurrentChange
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
</style> 