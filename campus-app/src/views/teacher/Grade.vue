<template>
  <div class="grade-container">
    <div class="page-header">
      <h2>成绩管理</h2>
      <div class="course-selector">
        <el-select
            v-model="selectedCourseId"
            clearable
            placeholder="选择课程"
            style="width: 220px;"
            @change="handleCourseChange"
        >
          <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
          />
        </el-select>
      </div>
    </div>

    <!-- 课程未选择提示 -->
    <el-empty v-if="!selectedCourseId" description="请选择一个课程进行成绩管理"/>

    <!-- 课程选择后的成绩管理 -->
    <template v-else>
      <el-card class="grade-card">
        <el-tabs v-model="activeTab">
          <!-- 成绩项管理 -->
          <el-tab-pane label="成绩项管理" name="items">
            <div class="tab-header">
              <el-button type="primary" @click="addGradeItem">添加评分项</el-button>
            </div>

            <el-table v-loading="itemsLoading" :data="gradeItems" border style="width: 100%">
              <el-table-column label="评分项名称" min-width="150" prop="itemName"/>
              <el-table-column label="权重" prop="weight" width="80">
                <template #default="scope">
                  {{ scope.row.weight }}%
                </template>
              </el-table-column>
              <el-table-column label="满分" prop="maxScore" width="80"/>
              <el-table-column label="评分进度" width="200">
                <template #default="scope">
                  <el-progress
                      :format="() => `${scope.row.gradedCount}/${scope.row.totalCount}`"
                      :percentage="getGradePercentage(scope.row)"
                      :status="scope.row.gradedCount === scope.row.totalCount ? 'success' : ''"
                  ></el-progress>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="250">
                <template #default="scope">
                  <el-button link type="primary" @click="gradeStudents(scope.row)">评分</el-button>
                  <el-button link type="primary" @click="editGradeItem(scope.row)">编辑</el-button>
                  <el-button link type="danger" @click="deleteGradeItem(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-empty v-if="gradeItems.length === 0 && !itemsLoading" description="暂无评分项"/>
          </el-tab-pane>

          <!-- 学生成绩管理 -->
          <el-tab-pane label="学生成绩管理" name="students">
            <div class="filter-container">
              <el-input
                  v-model="searchQuery"
                  clearable
                  placeholder="搜索学生"
                  style="width: 200px; margin-right: 10px"
                  @clear="handleFilter"
                  @keyup.enter="handleFilter"
              >
                <template #prefix>
                  <el-icon>
                    <Search/>
                  </el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="handleFilter">搜索</el-button>
              <el-button type="success" @click="exportGrades">导出成绩</el-button>
            </div>

            <el-table v-loading="studentsLoading" :data="filteredStudents" border style="width: 100%">
              <el-table-column fixed="left" label="学号" prop="studentId" width="120"/>
              <el-table-column fixed="left" label="姓名" prop="studentName" width="100"/>
              <el-table-column
                  v-for="item in gradeItems"
                  :key="item.id"
                  :label="item.itemName + ' (' + item.maxScore + '分)'"
                  :min-width="120"
                  :prop="'grades.' + item.id"
              >
                <template #default="scope">
                  <div v-if="getStudentItemGrade(scope.row, item.id)">
                    {{ getStudentItemGrade(scope.row, item.id).score || '-' }}
                  </div>
                  <el-button v-else size="small" @click="addStudentGrade(scope.row, item)">录入</el-button>
                </template>
              </el-table-column>
              <el-table-column label="总评" width="100">
                <template #default="scope">
                  {{ calculateTotalScore(scope.row) }}
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="150">
                <template #default="scope">
                  <el-button link type="primary" @click="editStudentGrades(scope.row)">编辑成绩</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-empty v-if="students.length === 0 && !studentsLoading" description="暂无学生信息"/>
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <!-- 成绩项对话框 -->
      <el-dialog
          v-model="itemDialogVisible"
          :title="itemDialogType === 'add' ? '添加评分项' : '编辑评分项'"
          width="500px"
      >
        <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
          <el-form-item label="评分项名称" prop="itemName">
            <el-input v-model="itemForm.itemName" placeholder="请输入评分项名称"/>
          </el-form-item>
          <el-form-item label="权重" prop="weight">
            <el-input-number
                v-model="itemForm.weight"
                :max="100"
                :min="0"
                :step="5"
                style="width: 180px;"
            />
            <span class="form-hint">总权重: {{ totalWeight }}%{{ totalWeight > 100 ? ' (超出100%)' : '' }}</span>
          </el-form-item>
          <el-form-item label="满分" prop="maxScore">
            <el-input-number
                v-model="itemForm.maxScore"
                :max="100"
                :min="0"
                :step="5"
                style="width: 180px;"
            />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input
                v-model="itemForm.description"
                :rows="3"
                placeholder="请输入评分项描述"
                type="textarea"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="itemDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitGradeItem">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 学生评分对话框 -->
      <el-dialog
          v-model="gradeDialogVisible"
          :title="gradeDialogTitle"
          width="800px"
      >
        <div v-if="gradeDialogType === 'list'">
          <div class="grade-list-filter">
            <el-input
                v-model="gradeSearchQuery"
                clearable
                placeholder="搜索学生"
                style="width: 200px; margin-right: 10px"
            >
              <template #prefix>
                <el-icon>
                  <Search/>
                </el-icon>
              </template>
            </el-input>
          </div>

          <el-table :data="filteredGradeStudents" border style="width: 100%">
            <el-table-column label="学号" prop="studentId" width="120"/>
            <el-table-column label="姓名" prop="studentName" width="100"/>
            <el-table-column label="成绩" width="150">
              <template #default="scope">
                <el-input-number
                    v-model="scope.row.score"
                    :max="currentGradeItem.maxScore"
                    :min="0"
                    :precision="1"
                    :step="1"
                    style="width: 120px;"
                />
              </template>
            </el-table-column>
            <el-table-column label="评语" min-width="200">
              <template #default="scope">
                <el-input
                    v-model="scope.row.comment"
                    placeholder="评语(可选)"
                />
              </template>
            </el-table-column>
          </el-table>

          <div class="batch-actions">
            <span class="batch-label">批量设置:</span>
            <el-input-number
                v-model="batchScore"
                :max="currentGradeItem.maxScore"
                :min="0"
                :precision="1"
                :step="1"
                style="width: 120px; margin-right: 10px;"
            />
            <el-button type="primary" @click="setBatchScore">应用到所有未评分学生</el-button>
          </div>
        </div>

        <div v-else-if="gradeDialogType === 'single'">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="学号">{{ currentStudent.studentId }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ currentStudent.studentName }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
          </el-descriptions>

          <div class="student-grades-form">
            <h3>成绩明细</h3>
            <el-form ref="studentGradesFormRef" :model="studentGradesForm" label-width="150px">
              <el-form-item
                  v-for="item in gradeItems"
                  :key="item.id"
                  :label="item.itemName + ' (' + item.weight + '%，满分' + item.maxScore + ')'"
              >
                <el-input-number
                    v-model="studentGradesForm[item.id]"
                    :max="item.maxScore"
                    :min="0"
                    :precision="1"
                    :step="1"
                    style="width: 120px; margin-right: 10px;"
                />
                <span v-if="studentGradesForm[item.id] !== null" class="score-hint">
                  折算后: {{ calculateWeightedScore(studentGradesForm[item.id], item) }}
                </span>
              </el-form-item>

              <el-form-item label="总评">
                <span class="total-score">{{ calculateFormTotalScore() }}</span>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="gradeDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitGrades">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search} from '@element-plus/icons-vue'
import {getTeacherCourses} from '@/api/course'
import {
  createGradeItem,
  deleteGradeItem as deleteGradeItemApi,
  exportGrades as exportGradesApi,
  getCourseGradeItems,
  getStudentGrades,
  submitStudentGrades,
  updateGradeItem
} from '@/api/grade'

const route = useRoute()
const router = useRouter()

// 数据加载状态
const itemsLoading = ref(false)
const studentsLoading = ref(false)

// 课程相关
const courses = ref([])
const selectedCourseId = ref('')
const selectedCourse = computed(() => {
  return courses.value.find(c => c.id === selectedCourseId.value) || {}
})

// 标签页
const activeTab = ref('items')

// 成绩项管理
const gradeItems = ref([])
const itemDialogVisible = ref(false)
const itemDialogType = ref('add')
const itemForm = reactive({
  id: undefined,
  itemName: '',
  weight: 100,
  maxScore: 100,
  description: ''
})
const itemFormRef = ref(null)
const itemRules = {
  itemName: [{required: true, message: '请输入评分项名称', trigger: 'blur'}],
  weight: [{required: true, message: '请输入权重', trigger: 'change'}],
  maxScore: [{required: true, message: '请输入满分', trigger: 'change'}]
}

// 学生成绩管理
const students = ref([])
const searchQuery = ref('')
const filteredStudents = computed(() => {
  if (!searchQuery.value) {
    return students.value
  }
  const search = searchQuery.value.toLowerCase()
  return students.value.filter(student =>
      student.studentId.includes(search) ||
      student.studentName.toLowerCase().includes(search) ||
      (student.className && student.className.toLowerCase().includes(search))
  )
})

// 学生评分对话框
const gradeDialogVisible = ref(false)
const gradeDialogType = ref('list') // 'list' 批量评分, 'single' 单个学生评分
const gradeDialogTitle = computed(() => {
  if (gradeDialogType.value === 'list') {
    return `${currentGradeItem.value?.itemName || ''}评分`
  } else {
    return `${currentStudent.value?.studentName || ''}的成绩`
  }
})
const currentGradeItem = ref({})
const currentStudent = ref({})
const gradeStudentsList = ref([])
const gradeSearchQuery = ref('')
const filteredGradeStudents = computed(() => {
  if (!gradeSearchQuery.value) {
    return gradeStudentsList.value
  }
  const search = gradeSearchQuery.value.toLowerCase()
  return gradeStudentsList.value.filter(student =>
      student.studentId.includes(search) ||
      student.studentName.toLowerCase().includes(search)
  )
})
const batchScore = ref(0)
const studentGradesForm = reactive({})

// 计算总权重
const totalWeight = computed(() => {
  if (itemDialogType.value === 'add') {
    return gradeItems.value.reduce((sum, item) => sum + (item.weight || 0), 0) + (itemForm.weight || 0)
  } else {
    const currentItemId = itemForm.id
    return gradeItems.value.reduce((sum, item) => {
      if (item.id === currentItemId) {
        return sum
      }
      return sum + (item.weight || 0)
    }, 0) + (itemForm.weight || 0)
  }
})

// 获取成绩项完成百分比
const getGradePercentage = (item) => {
  if (!item.totalCount || item.totalCount === 0) return 0
  return Math.round((item.gradedCount / item.totalCount) * 100)
}

// 获取学生特定评分项的成绩
const getStudentItemGrade = (student, itemId) => {
  if (!student.grades) return null
  return student.grades.find(grade => grade.itemId === itemId)
}

// 计算学生总分
const calculateTotalScore = (student) => {
  if (!student.grades || student.grades.length === 0) return '-'

  let totalScore = 0
  let totalWeight = 0

  student.grades.forEach(grade => {
    const item = gradeItems.value.find(i => i.id === grade.itemId)
    if (item && grade.score !== null && grade.score !== undefined) {
      const weightedScore = (grade.score / item.maxScore) * item.weight
      totalScore += weightedScore
      totalWeight += item.weight
    }
  })

  if (totalWeight === 0) return '-'

  // 按照总权重比例缩放
  if (totalWeight < 100) {
    totalScore = (totalScore / totalWeight) * 100
  }

  return totalScore.toFixed(1)
}

// 计算表单中的总分
const calculateFormTotalScore = () => {
  let totalScore = 0
  let totalWeight = 0

  gradeItems.value.forEach(item => {
    const score = studentGradesForm[item.id]
    if (score !== null && score !== undefined) {
      const weightedScore = (score / item.maxScore) * item.weight
      totalScore += weightedScore
      totalWeight += item.weight
    }
  })

  if (totalWeight === 0) return '-'

  // 按照总权重比例缩放
  if (totalWeight < 100) {
    totalScore = (totalScore / totalWeight) * 100
  }

  return totalScore.toFixed(1)
}

// 计算加权分数
const calculateWeightedScore = (score, item) => {
  if (score === null || score === undefined) return '-'
  return (((score / item.maxScore) * item.weight)).toFixed(2)
}

// 初始化数据
const initData = async () => {
  try {
    await fetchCourses()

    // 检查URL参数中是否有指定的课程ID
    if (route.query.courseId) {
      selectedCourseId.value = route.query.courseId
      await handleCourseChange()

      // 如果URL参数中同时指定了学生ID，则打开该学生的成绩编辑
      if (route.query.studentId) {
        await fetchStudents()
        const student = students.value.find(s => s.id === route.query.studentId)
        if (student) {
          editStudentGrades(student)
        }
      }

      // 如果URL参数中指定了评分项ID，则打开批量评分
      if (route.query.itemId) {
        await fetchGradeItems()
        const item = gradeItems.value.find(i => i.id === route.query.itemId)
        if (item) {
          gradeStudents(item)
        }
      }
    }
  } catch (error) {
    console.error('初始化数据失败', error)
  }
}

// 获取教师课程列表
const fetchCourses = async () => {
  try {
    const response = await getTeacherCourses()
    courses.value = response.data || []
  } catch (error) {
    console.error('获取课程列表失败', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 课程变更处理
const handleCourseChange = async () => {
  if (!selectedCourseId.value) return

  // 默认加载成绩项
  activeTab.value = 'items'

  await fetchGradeItems()
}

// 获取成绩项列表
const fetchGradeItems = async () => {
  if (!selectedCourseId.value) return

  itemsLoading.value = true
  try {
    const response = await getCourseGradeItems(selectedCourseId.value)
    gradeItems.value = response.data || []
  } catch (error) {
    console.error('获取成绩项列表失败', error)
    ElMessage.error('获取成绩项列表失败')
  } finally {
    itemsLoading.value = false
  }
}

// 添加成绩项
const addGradeItem = () => {
  resetItemForm()
  itemDialogType.value = 'add'
  itemDialogVisible.value = true
}

// 编辑成绩项
const editGradeItem = (item) => {
  resetItemForm()

  itemForm.id = item.id
  itemForm.itemName = item.itemName
  itemForm.weight = item.weight
  itemForm.maxScore = item.maxScore
  itemForm.description = item.description

  itemDialogType.value = 'edit'
  itemDialogVisible.value = true
}

// 重置成绩项表单
const resetItemForm = () => {
  if (itemFormRef.value) {
    itemFormRef.value.resetFields()
  }

  itemForm.id = undefined
  itemForm.itemName = ''
  itemForm.weight = 100
  itemForm.maxScore = 100
  itemForm.description = ''
}

// 提交成绩项表单
const submitGradeItem = async () => {
  if (!itemFormRef.value) return

  try {
    await itemFormRef.value.validate()

    if (totalWeight > 100) {
      const confirmResult = await ElMessageBox.confirm(
          '当前评分项权重总和超过100%，是否继续？',
          '警告',
          {
            confirmButtonText: '继续',
            cancelButtonText: '取消',
            type: 'warning'
          }
      )

      if (confirmResult === 'cancel') {
        return
      }
    }

    const formData = {
      courseId: selectedCourseId.value,
      itemName: itemForm.itemName,
      weight: itemForm.weight,
      maxScore: itemForm.maxScore,
      description: itemForm.description
    }

    if (itemDialogType.value === 'add') {
      await createGradeItem(formData)
      ElMessage.success('添加评分项成功')
    } else {
      await updateGradeItem(itemForm.id, formData)
      ElMessage.success('更新评分项成功')
    }

    itemDialogVisible.value = false
    fetchGradeItems()
  } catch (error) {
    if (error === 'cancel') return

    console.error('提交表单失败', error)
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('操作失败，请检查表单内容')
    }
  }
}

// 删除成绩项
const deleteGradeItem = async (item) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该评分项吗？删除后将无法恢复，且所有学生在该项的成绩也将被删除。',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteGradeItemApi(item.id)
    ElMessage.success('删除评分项成功')
    fetchGradeItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评分项失败', error)
      ElMessage.error('删除评分项失败')
    }
  }
}

// 批量评分
const gradeStudents = async (item) => {
  if (!item) return

  currentGradeItem.value = item
  gradeDialogType.value = 'list'
  gradeSearchQuery.value = ''

  try {
    // 加载学生列表
    if (students.value.length === 0) {
      await fetchStudents()
    }

    // 准备评分数据
    gradeStudentsList.value = students.value.map(student => {
      // 查找学生该项的成绩
      const existingGrade = getStudentItemGrade(student, item.id)

      return {
        studentId: student.studentId,
        studentName: student.studentName,
        id: student.id,
        score: existingGrade ? existingGrade.score : null,
        comment: existingGrade ? existingGrade.comment : ''
      }
    })

    gradeDialogVisible.value = true
  } catch (error) {
    console.error('准备评分数据失败', error)
    ElMessage.error('准备评分数据失败')
  }
}

// 批量设置分数
const setBatchScore = () => {
  gradeStudentsList.value.forEach(student => {
    if (student.score === null) {
      student.score = batchScore.value
    }
  })
}

// 编辑单个学生成绩
const editStudentGrades = async (student) => {
  if (!student) return

  currentStudent.value = student
  gradeDialogType.value = 'single'

  // 重置表单
  gradeItems.value.forEach(item => {
    studentGradesForm[item.id] = null
  })

  // 填充已有成绩
  if (student.grades) {
    student.grades.forEach(grade => {
      studentGradesForm[grade.itemId] = grade.score
    })
  }

  gradeDialogVisible.value = true
}

// 添加单个学生的单项成绩
const addStudentGrade = async (student, item) => {
  if (!student || !item) return

  await editStudentGrades(student)
}

// 提交成绩
const submitGrades = async () => {
  try {
    if (gradeDialogType.value === 'list') {
      // 批量提交
      const gradesData = {
        courseId: selectedCourseId.value,
        itemId: currentGradeItem.value.id,
        grades: gradeStudentsList.value.map(student => ({
          studentId: student.id,
          score: student.score,
          comment: student.comment
        })).filter(grade => grade.score !== null)
      }

      await submitStudentGrades(gradesData)
      ElMessage.success('保存成绩成功')
    } else {
      // 单个学生提交
      const gradesData = {
        courseId: selectedCourseId.value,
        studentId: currentStudent.value.id,
        grades: Object.keys(studentGradesForm).map(itemId => ({
          itemId,
          score: studentGradesForm[itemId],
          comment: ''
        })).filter(grade => grade.score !== null)
      }

      await submitStudentGrades(gradesData)
      ElMessage.success('保存成绩成功')
    }

    gradeDialogVisible.value = false

    // 重新加载学生成绩
    await fetchStudents()
  } catch (error) {
    console.error('提交成绩失败', error)
    ElMessage.error('提交成绩失败')
  }
}

// 获取学生列表
const fetchStudents = async () => {
  if (!selectedCourseId.value) return

  studentsLoading.value = true
  try {
    const response = await getStudentGrades(selectedCourseId.value)
    students.value = response.data || []
  } catch (error) {
    console.error('获取学生列表失败', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    studentsLoading.value = false
  }
}

// 处理搜索过滤
const handleFilter = () => {
  // 无需额外处理，使用计算属性filteredStudents
}

// 导出成绩
const exportGrades = () => {
  if (!selectedCourseId.value) return

  exportGradesApi(selectedCourseId.value)
      .then(() => {
        ElMessage.success('成绩导出成功')
      })
      .catch(() => {
        ElMessage.error('成绩导出失败')
      })
}

// 监听标签页变化
watch(activeTab, async (newVal) => {
  if (newVal === 'students' && students.value.length === 0) {
    await fetchStudents()
  }
})

// 页面挂载时加载数据
onMounted(() => {
  initData()
})
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

.grade-card {
  margin-bottom: 20px;
}

.tab-header {
  margin-bottom: 15px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.form-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}

.grade-list-filter {
  margin-bottom: 15px;
}

.batch-actions {
  margin-top: 20px;
  display: flex;
  align-items: center;
}

.batch-label {
  margin-right: 10px;
  font-weight: bold;
}

.student-grades-form {
  margin-top: 20px;
}

.score-hint {
  margin-left: 5px;
  color: #409EFF;
}

.total-score {
  font-size: 18px;
  font-weight: bold;
  color: #E6A23C;
}
</style> 