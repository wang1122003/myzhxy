<template>
  <div class="grade-container">
    <div class="page-header">
      <h2>成绩查询</h2>
      <div class="filter-container">
        <el-select
            v-model="filter.semester"
            clearable
            placeholder="选择学期"
            style="margin-right: 10px;"
            @change="handleFilterChange"
        >
          <el-option
              v-for="item in semesters"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <el-select
            v-model="filter.gradeType"
            clearable
            placeholder="成绩类型"
            style="margin-right: 10px;"
            @change="handleFilterChange"
        >
          <el-option
              label="全部"
              value=""
          />
          <el-option
              label="通过"
              value="pass"
          />
          <el-option
              label="不通过"
              value="fail"
          />
        </el-select>
      </div>
    </div>

    <el-card class="grade-card">
      <el-tabs
          v-model="activeTab"
          @tab-click="handleTabChange"
      >
        <el-tab-pane
            label="成绩列表"
            name="list"
        >
          <el-table
              :data="grades"
              border
              style="width: 100%"
          >
            <el-table-column
                label="课程名称"
                prop="courseName"
            />
            <el-table-column
                label="课程代码"
                prop="courseCode"
                width="120"
            />
            <el-table-column
                label="学分"
                prop="credit"
                width="80"
            />
            <el-table-column
                label="成绩"
                prop="grade"
                width="80"
            >
              <template #default="scope">
                <span :class="{ 'text-danger': scope.row.grade < 60 }">
                  {{ scope.row.grade }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
                label="绩点"
                prop="gradePoint"
                width="80"
            />
            <el-table-column
                label="学期"
                prop="semester"
                width="150"
            />
            <el-table-column
                label="课程类型"
                prop="courseType"
                width="120"
            />
            <el-table-column
                label="考试类型"
                prop="examType"
                width="120"
            />
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
        </el-tab-pane>

        <el-tab-pane
            label="统计分析"
            name="stats"
        >
          <div class="stats-container">
            <div class="stats-card">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <span>学期统计</span>
                  </div>
                </template>
                <div class="stats-summary">
                  <div class="stats-item">
                    <div class="stats-label">
                      已修学分
                    </div>
                    <div class="stats-value">
                      {{ semesterStats.totalCredit }}
                    </div>
                  </div>
                  <div class="stats-item">
                    <div class="stats-label">
                      平均分
                    </div>
                    <div class="stats-value">
                      {{ semesterStats.averageGrade.toFixed(2) }}
                    </div>
                  </div>
                  <div class="stats-item">
                    <div class="stats-label">
                      平均绩点
                    </div>
                    <div class="stats-value">
                      {{ semesterStats.gpa.toFixed(2) }}
                    </div>
                  </div>
                  <div class="stats-item">
                    <div class="stats-label">
                      课程总数
                    </div>
                    <div class="stats-value">
                      {{ semesterStats.totalCourses }}
                    </div>
                  </div>
                </div>
              </el-card>
            </div>

            <div class="stats-card">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <span>总体统计</span>
                  </div>
                </template>
                <div class="stats-summary">
                  <div class="stats-item">
                    <div class="stats-label">
                      总学分
                    </div>
                    <div class="stats-value">
                      {{ overallStats.totalCredit }}
                    </div>
                  </div>
                  <div class="stats-item">
                    <div class="stats-label">
                      总平均分
                    </div>
                    <div class="stats-value">
                      {{ overallStats.averageGrade.toFixed(2) }}
                    </div>
                  </div>
                  <div class="stats-item">
                    <div class="stats-label">
                      总绩点
                    </div>
                    <div class="stats-value">
                      {{ overallStats.gpa.toFixed(2) }}
                    </div>
                  </div>
                  <div class="stats-item">
                    <div class="stats-label">
                      总课程数
                    </div>
                    <div class="stats-value">
                      {{ overallStats.totalCourses }}
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>

          <div class="chart-container">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>成绩分布</span>
                </div>
              </template>
              <div class="chart-area">
                <div
                    ref="pieChartRef"
                    class="pie-chart"
                />
                <div
                    ref="barChartRef"
                    class="bar-chart"
                />
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import {onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {getMyScores} from '@/api/grade'
import * as echarts from 'echarts/core'
import {BarChart, PieChart} from 'echarts/charts'
import {GridComponent, LegendComponent, TitleComponent, TooltipComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'

echarts.use([
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  TitleComponent,
  LegendComponent,
  CanvasRenderer
])

export default {
  name: 'StudentGrade',
  setup() {
    const grades = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const activeTab = ref('list')

    const pieChartRef = ref(null)
    const barChartRef = ref(null)
    let pieChart = null
    let barChart = null

    const semesterStats = reactive({
      totalCredit: 0,
      averageGrade: 0,
      gpa: 0,
      totalCourses: 0
    })

    const overallStats = reactive({
      totalCredit: 0,
      averageGrade: 0,
      gpa: 0,
      totalCourses: 0
    })

    const semesters = ref([
      {label: '2023-2024学年第一学期', value: '2023-2024-1'},
      {label: '2023-2024学年第二学期', value: '2023-2024-2'},
      {label: '2022-2023学年第一学期', value: '2022-2023-1'},
      {label: '2022-2023学年第二学期', value: '2022-2023-2'}
    ])

    const filter = reactive({
      semester: '',
      gradeType: ''
    })

    // 用于防抖动的定时器
    let resizeTimer = null

    // 监听窗口大小变化，重绘图表（带防抖功能）
    const handleResize = () => {
      if (resizeTimer) clearTimeout(resizeTimer)

      resizeTimer = setTimeout(() => {
        if (pieChart) pieChart.resize()
        if (barChart) barChart.resize()
      }, 100)
    }

    const fetchGrades = () => {
      const params = {
        page: currentPage.value - 1,
        size: pageSize.value,
        semester: filter.semester || null,
        gradeType: filter.gradeType || null
      }

      getMyScores(params).then(response => {
        grades.value = response.data?.content || response.data || []
        total.value = response.data?.totalElements || 0
      }).catch(error => {
        console.error('获取成绩失败', error)
        ElMessage.error('获取成绩失败')
      })
    }

    const handleTabChange = () => {
      currentPage.value = 1
      // Comment out stats logic in tab change if any
    }

    const handleFilterChange = () => {
      currentPage.value = 1
      fetchGrades()
      // Comment out stats logic in filter change
      // if (activeTab.value === 'stats') {
      //   fetchGradeStats()
      // }
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchGrades()
    }

    const initCharts = () => {
      if (pieChartRef.value) {
        pieChart = echarts.init(pieChartRef.value)
        pieChart.setOption({
          title: {
            text: '成绩区间分布',
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            data: ['90-100', '80-89', '70-79', '60-69', '0-59']
          },
          series: [
            {
              name: '成绩区间',
              type: 'pie',
              radius: '60%',
              data: [],
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }
          ]
        })
      }

      if (barChartRef.value) {
        barChart = echarts.init(barChartRef.value)
        barChart.setOption({
          title: {
            text: '各科成绩对比',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: [],
            axisTick: {
              alignWithLabel: true
            },
            axisLabel: {
              interval: 0,
              rotate: 45
            }
          },
          yAxis: {
            type: 'value',
            max: 100
          },
          series: [
            {
              name: '成绩',
              type: 'bar',
              barWidth: '60%',
              data: []
            }
          ]
        })
      }
    }

    const updateCharts = (data) => {
      if (pieChart) {
        pieChart.setOption({
          series: [
            {
              data: [
                {value: data.gradeDistribution['90-100'], name: '90-100'},
                {value: data.gradeDistribution['80-89'], name: '80-89'},
                {value: data.gradeDistribution['70-79'], name: '70-79'},
                {value: data.gradeDistribution['60-69'], name: '60-69'},
                {value: data.gradeDistribution['0-59'], name: '0-59'}
              ]
            }
          ]
        })
      }

      if (barChart) {
        const courseNames = []
        const courseGrades = []

        data.courseGrades.forEach(item => {
          courseNames.push(item.courseName)
          courseGrades.push({
            value: item.grade,
            itemStyle: {
              color: item.grade < 60 ? '#F56C6C' : '#67C23A'
            }
          })
        })

        barChart.setOption({
          xAxis: {
            data: courseNames
          },
          series: [
            {
              data: courseGrades
            }
          ]
        })
      }
    }

    onMounted(() => {
      fetchGrades()
      window.addEventListener('resize', handleResize)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', handleResize)
      if (pieChart) pieChart.dispose()
      if (barChart) barChart.dispose()
      if (resizeTimer) clearTimeout(resizeTimer)
    })

    return {
      grades,
      total,
      pageSize,
      currentPage,
      activeTab,
      semesters,
      filter,
      semesterStats,
      overallStats,
      pieChartRef,
      barChartRef,
      handleTabChange,
      handleFilterChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.grade-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  align-items: center;
}

.grade-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.text-danger {
  color: #F56C6C;
  font-weight: bold;
}

.stats-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stats-card {
  flex: 1;
  min-width: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.stats-item {
  flex: 1;
  min-width: 100px;
  text-align: center;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.chart-container {
  margin-top: 20px;
}

.chart-area {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.pie-chart {
  height: 400px;
  flex: 1;
  min-width: 300px;
}

.bar-chart {
  height: 400px;
  flex: 2;
  min-width: 500px;
}
</style> 