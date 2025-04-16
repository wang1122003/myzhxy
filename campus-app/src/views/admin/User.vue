<template>
  <div class="user-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd">添加用户</el-button>
    </div>

    <el-card class="user-card">
      <el-table :data="userList" border style="width: 100%">
        <el-table-column label="用户名" prop="username"/>
        <el-table-column label="姓名" prop="name"/>
        <el-table-column label="角色" prop="role">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="邮箱" prop="email"/>
        <el-table-column label="手机号码" prop="phone"/>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
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
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id"/>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="学生" value="student"/>
            <el-option label="教师" value="teacher"/>
            <el-option label="管理员" value="admin"/>
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone"/>
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" show-password type="password"/>
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
import {addUser, deleteUser, getUserList, updateUser} from '@/api/user'

export default {
  name: 'AdminUser',
  setup() {
    const userList = ref([])
    const total = ref(0)
    const pageSize = ref(10)
    const currentPage = ref(1)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const formRef = ref(null)
    const form = reactive({
      id: '',
      username: '',
      password: '',
      name: '',
      email: '',
      phone: '',
      role: 'student'
    })

    const rules = {
      username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
      password: [{required: true, message: '请输入密码', trigger: 'blur'}],
      name: [{required: true, message: '请输入姓名', trigger: 'blur'}],
      email: [{required: true, message: '请输入邮箱', trigger: 'blur'}],
      phone: [{required: true, message: '请输入手机号', trigger: 'blur'}],
      role: [{required: true, message: '请选择角色', trigger: 'change'}]
    }

    const fetchUsers = () => {
      getUserList({
        page: currentPage.value - 1,
        size: pageSize.value
      }).then(response => {
        userList.value = response.data.content
        total.value = response.data.totalElements
      }).catch((error) => {
        console.error('获取用户列表失败', error)
        ElMessage.error('获取用户列表失败')
      })
    }

    const getRoleType = (role) => {
      const types = {
        student: 'success',
        teacher: 'warning',
        admin: 'danger'
      }
      return types[role] || 'info'
    }

    const getRoleName = (role) => {
      const names = {
        student: '学生',
        teacher: '教师',
        admin: '管理员'
      }
      return names[role] || role
    }

    const handleAdd = () => {
      dialogTitle.value = '添加用户'
      dialogVisible.value = true
      Object.keys(form).forEach(key => {
        form[key] = ''
      })
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑用户'
      dialogVisible.value = true
      Object.keys(form).forEach(key => {
        form[key] = row[key]
      })
    }

    const handleToggleStatus = (row) => {
      const newStatus = row.status === 1 ? 0 : 1
      updateUser(row.id, {...row, status: newStatus}).then(() => {
        ElMessage.success(newStatus === 1 ? '启用成功' : '禁用成功')
        fetchUsers()
      }).catch(() => {
        ElMessage.error(newStatus === 1 ? '启用失败' : '禁用失败')
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteUser(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchUsers()
        }).catch((error) => {
          console.error('删除用户失败', error)
          ElMessage.error('删除用户失败')
        })
      })
    }

    const handleSubmit = () => {
      formRef.value.validate((valid) => {
        if (valid) {
          if (form.id) {
            updateUser(form.id, form).then(() => {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              fetchUsers()
            }).catch((error) => {
              console.error('更新用户失败', error)
              ElMessage.error('更新用户失败')
            })
          } else {
            addUser(form).then(() => {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              fetchUsers()
            }).catch((error) => {
              console.error('添加用户失败', error)
              ElMessage.error('添加用户失败')
            })
          }
        }
      })
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      fetchUsers()
    }

    onMounted(() => {
      fetchUsers()
    })

    return {
      userList,
      total,
      pageSize,
      currentPage,
      dialogVisible,
      dialogTitle,
      formRef,
      form,
      rules,
      getRoleType,
      getRoleName,
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
.user-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.user-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 