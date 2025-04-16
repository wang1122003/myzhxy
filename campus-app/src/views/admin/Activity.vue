<template>
  <div class="activity-container">
    <div class="page-header">
      <h2>活动管理</h2>
      <el-button type="primary" @click="handleAdd">添加活动</el-button>
    </div>

    <el-card class="activity-card">
      <el-table :data="activities" border style="width: 100%">
        <el-table-column label="活动标题" prop="title"/>
        <el-table-column label="活动类型" prop="type">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" prop="startTime"/>
        <el-table-column label="结束时间" prop="endTime"/>
        <el-table-column label="活动地点" prop="location"/>
        <el-table-column label="主办方" prop="organizer"/>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '结束' : '开始' }}
            </el-button>
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

    <!-- 添加/编辑活动对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
    >
      <el-form ref="activityFormRef" :model="activityForm" :rules="rules" label-width="100px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="activityForm.title"/>
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="activityForm.type" style="width: 100%">
            <el-option label="学术讲座" value="lecture"/>
            <el-option label="文艺演出" value="performance"/>
            <el-option label="体育比赛" value="sports"/>
            <el-option label="社团活动" value="club"/>
            <el-option label="志愿服务" value="volunteer"/>
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
              v-model="activityForm.startTime"
              placeholder="选择日期时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
              v-model="activityForm.endTime"
              placeholder="选择日期时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="activityForm.location"/>
        </el-form-item>
        <el-form-item label="主办方" prop="organizer">
          <el-input v-model="activityForm.organizer"/>
        </el-form-item>
        <el-form-item label="活动简介" prop="description">
          <el-input
              v-model="activityForm.description"
              :rows="4"
              placeholder="请输入活动简介"
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
import {addActivity, deleteActivity, getActivityList, updateActivity} from '@/api/activity'

export default {
  name: 'AdminActivity',
  setup() {
    const activities = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const activityFormRef = ref(null)
    const activityForm = reactive({
      id: '',
      title: '',
      type: '',
      startTime: '',
      endTime: '',
      location: '',
      organizer: '',
      description: '',
      status: 1
    })

    const rules = {
      title: [{required: true, message: '请输入活动标题', trigger: 'blur'}],
      type: [{required: true, message: '请选择活动类型', trigger: 'change'}],
      startTime: [{required: true, message: '请选择开始时间', trigger: 'change'}],
      endTime: [{required: true, message: '请选择结束时间', trigger: 'change'}],
      location: [{required: true, message: '请输入活动地点', trigger: 'blur'}],
      organizer: [{required: true, message: '请输入主办方', trigger: 'blur'}]
    }

    const fetchActivities = () => {
      getActivityList({
        page: currentPage.value - 1,
        size: pageSize.value
      }).then(response => {
        activities.value = response.data.content
        total.value = response.data.totalElements
      }).catch((error) => {
        console.error('获取活动列表失败', error)
        ElMessage.error('获取活动列表失败')
      })
    }

    const getTypeTag = (type) => {
      const tags = {
        lecture: 'primary',
        performance: 'success',
        sports: 'warning',
        club: 'info',
        volunteer: 'danger'
      }
      return tags[type] || 'info'
    }

    const getTypeName = (type) => {
      const names = {
        lecture: '学术讲座',
        performance: '文艺演出',
        sports: '体育比赛',
        club: '社团活动',
        volunteer: '志愿服务'
      }
      return names[type] || type
    }

    const handleAdd = () => {
      dialogTitle.value = '添加活动'
      dialogVisible.value = true
      Object.keys(activityForm).forEach(key => {
        activityForm[key] = ''
      })
      activityForm.status = 1
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑活动'
      dialogVisible.value = true
      Object.keys(activityForm).forEach(key => {
        activityForm[key] = row[key]
      })
    }

    const handleToggleStatus = (row) => {
      const newStatus = row.status === 1 ? 0 : 1
      updateActivity(row.id, {...row, status: newStatus}).then(() => {
        ElMessage.success(newStatus === 1 ? '活动已开始' : '活动已结束')
        fetchActivities()
      }).catch((error) => {
        console.error('更新活动状态失败', error)
        ElMessage.error('操作失败')
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该活动吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteActivity(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchActivities()
        }).catch((error) => {
          console.error('删除活动失败', error)
          ElMessage.error('删除失败')
        })
      })
    }

    const handleSubmit = () => {
      activityFormRef.value.validate((valid) => {
        if (valid) {
          if (activityForm.id) {
            updateActivity(activityForm.id, activityForm).then(() => {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              fetchActivities()
            }).catch((error) => {
              console.error('更新活动失败', error)
              ElMessage.error('更新失败')
            })
          } else {
            addActivity(activityForm).then(() => {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              fetchActivities()
            }).catch((error) => {
              console.error('添加活动失败', error)
              ElMessage.error('添加失败')
            })
          }
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchActivities()
    }

    onMounted(() => {
      fetchActivities()
    })

    return {
      activities,
      total,
      pageSize,
      currentPage,
      dialogVisible,
      dialogTitle,
      activityFormRef,
      activityForm,
      rules,
      getTypeTag,
      getTypeName,
      handleAdd,
      handleEdit,
      handleToggleStatus,
      handleDelete,
      handleSubmit,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.activity-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.activity-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 