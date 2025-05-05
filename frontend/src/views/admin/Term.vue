<template>
  <div class="term-container">
    <div class="header">
      <h2>学期管理</h2>
      <div>
        <el-button type="primary" @click="addTermDialogVisible = true">添加学期</el-button>
        <el-button @click="fetchTerms">刷新</el-button>
      </div>
    </div>

    <!-- 学期列表表格 -->
    <el-table
        v-loading="loading"
        :data="terms"
        border
        style="width: 100%">
      <el-table-column
          label="ID"
          prop="id"
          width="60">
      </el-table-column>
      <el-table-column
          label="学期代码"
          prop="code"
          width="120">
      </el-table-column>
      <el-table-column
          label="学期名称"
          prop="name"
          width="180">
        <template slot-scope="scope">
          <span :class="{ 'current-term': scope.row.isCurrent === 1 }">
            {{ scope.row.name }}
            <el-tag v-if="scope.row.isCurrent === 1" size="small" type="success">当前学期</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column
          label="学年"
          prop="academicYear"
          width="100">
      </el-table-column>
      <el-table-column
          label="学期序号"
          prop="termNumber"
          width="100">
        <template slot-scope="scope">
          <span>{{ getTermNumberText(scope.row.termNumber) }}</span>
        </template>
      </el-table-column>
      <el-table-column
          label="学期日期"
          width="200">
        <template slot-scope="scope">
          {{ formatDate(scope.row.startDate) }} 至 {{ formatDate(scope.row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column
          label="状态"
          prop="status"
          width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
          label="操作"
          width="280">
        <template slot-scope="scope">
          <el-button
              size="small"
              @click="editTerm(scope.row)">编辑
          </el-button>
          <el-button
              v-if="scope.row.isCurrent !== 1"
              size="small"
              type="primary"
              @click="setCurrentTerm(scope.row.id)">设为当前学期
          </el-button>
          <el-button
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click="changeStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
              v-if="scope.row.isCurrent !== 1"
              size="small"
              type="danger"
              @click="deleteTerm(scope.row.id)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑学期对话框 -->
    <el-dialog :title="isEdit ? '编辑学期' : '添加学期'" :visible.sync="addTermDialogVisible">
      <el-form ref="termForm" :model="termForm" :rules="termRules" label-width="100px">
        <el-form-item label="学期代码" prop="code">
          <el-input v-model="termForm.code" placeholder="例：2023-2024-1"></el-input>
        </el-form-item>
        <el-form-item label="学期名称" prop="name">
          <el-input v-model="termForm.name" placeholder="例：2023-2024学年第一学期"></el-input>
        </el-form-item>
        <el-form-item label="学年" prop="academicYear">
          <el-input v-model="termForm.academicYear" placeholder="例：2023-2024"></el-input>
        </el-form-item>
        <el-form-item label="学期序号" prop="termNumber">
          <el-select v-model="termForm.termNumber" placeholder="请选择学期序号">
            <el-option :value="1" label="第一学期"></el-option>
            <el-option :value="2" label="第二学期"></el-option>
            <el-option :value="3" label="第三学期(短学期/暑期)"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
              v-model="termForm.startDate"
              format="yyyy-MM-dd"
              placeholder="选择开始日期"
              type="date"
              value-format="yyyy-MM-dd">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
              v-model="termForm.endDate"
              format="yyyy-MM-dd"
              placeholder="选择结束日期"
              type="date"
              value-format="yyyy-MM-dd">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="是否当前学期" prop="isCurrent">
          <el-switch
              v-model="termForm.isCurrent"
              :active-value="1"
              :inactive-value="0">
          </el-switch>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="termForm.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用">
          </el-switch>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="termForm.description" placeholder="学期描述信息" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addTermDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitTermForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TermManagement',
  data() {
    return {
      terms: [],
      loading: false,
      addTermDialogVisible: false,
      isEdit: false,
      termForm: {
        id: null,
        code: '',
        name: '',
        academicYear: '',
        termNumber: 1,
        startDate: '',
        endDate: '',
        isCurrent: 0,
        status: 1,
        description: ''
      },
      termRules: {
        code: [{required: true, message: '请输入学期代码', trigger: 'blur'}],
        name: [{required: true, message: '请输入学期名称', trigger: 'blur'}],
        academicYear: [{required: true, message: '请输入学年', trigger: 'blur'}],
        termNumber: [{required: true, message: '请选择学期序号', trigger: 'change'}],
        startDate: [{required: true, message: '请选择开始日期', trigger: 'change'}],
        endDate: [{required: true, message: '请选择结束日期', trigger: 'change'}]
      }
    }
  },
  created() {
    this.fetchTerms();
  },
  methods: {
    fetchTerms() {
      this.loading = true;
      this.$http.get('/api/term/list')
          .then(response => {
            if (response.data.code === 200) {
              this.terms = response.data.data;
            } else {
              this.$message.error(response.data.msg || '获取学期列表失败');
            }
          })
          .catch(error => {
            console.error('获取学期列表错误', error);
            this.$message.error('获取学期列表失败');
          })
          .finally(() => {
            this.loading = false;
          });
    },
    formatDate(date) {
      if (!date) return '未设置';
      // 如果日期是ISO格式字符串，转换为YYYY-MM-DD格式
      if (typeof date === 'string') {
        return date.split('T')[0];
      }
      return date;
    },
    getTermNumberText(termNumber) {
      const termMap = {
        1: '第一学期',
        2: '第二学期',
        3: '第三学期(短学期/暑期)'
      };
      return termMap[termNumber] || '未知';
    },
    resetTermForm() {
      if (this.$refs.termForm) {
        this.$refs.termForm.resetFields();
      }
      this.termForm = {
        id: null,
        code: '',
        name: '',
        academicYear: '',
        termNumber: 1,
        startDate: '',
        endDate: '',
        isCurrent: 0,
        status: 1,
        description: ''
      };
    },
    editTerm(term) {
      this.isEdit = true;
      this.termForm = JSON.parse(JSON.stringify(term)); // 深拷贝
      this.addTermDialogVisible = true;
    },
    submitTermForm() {
      this.$refs.termForm.validate(valid => {
        if (valid) {
          const url = '/api/term';
          const method = this.isEdit ? 'put' : 'post';

          this.$http({
            method: method,
            url: url,
            data: this.termForm
          })
              .then(response => {
                if (response.data.code === 200) {
                  this.$message.success(this.isEdit ? '更新成功' : '添加成功');
                  this.addTermDialogVisible = false;
                  this.fetchTerms();
                  this.resetTermForm();
                  this.isEdit = false;
                } else {
                  this.$message.error(response.data.msg || (this.isEdit ? '更新失败' : '添加失败'));
                }
              })
              .catch(error => {
                console.error(this.isEdit ? '更新学期错误' : '添加学期错误', error);
                this.$message.error(this.isEdit ? '更新失败' : '添加失败');
              });
        } else {
          return false;
        }
      });
    },
    deleteTerm(id) {
      this.$confirm('确定要删除该学期吗？删除后不可恢复', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delete(`/api/term/${id}`)
            .then(response => {
              if (response.data.code === 200) {
                this.$message.success('删除成功');
                this.fetchTerms();
              } else {
                this.$message.error(response.data.msg || '删除失败');
              }
            })
            .catch(error => {
              console.error('删除学期错误', error);
              this.$message.error('删除失败');
            });
      }).catch(() => {
        // 取消删除
      });
    },
    setCurrentTerm(id) {
      this.$confirm('确定要将该学期设为当前学期吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.put(`/api/term/current/${id}`)
            .then(response => {
              if (response.data.code === 200) {
                this.$message.success('设置成功');
                this.fetchTerms();
              } else {
                this.$message.error(response.data.msg || '设置失败');
              }
            })
            .catch(error => {
              console.error('设置当前学期错误', error);
              this.$message.error('设置失败');
            });
      }).catch(() => {
        // 取消设置
      });
    },
    changeStatus(term) {
      const newStatus = term.status === 1 ? 0 : 1;
      const statusText = newStatus === 1 ? '启用' : '禁用';

      this.$confirm(`确定要${statusText}该学期吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const updatedTerm = {...term, status: newStatus};
        this.$http.put('/api/term', updatedTerm)
            .then(response => {
              if (response.data.code === 200) {
                this.$message.success(`${statusText}成功`);
                this.fetchTerms();
              } else {
                this.$message.error(response.data.msg || `${statusText}失败`);
              }
            })
            .catch(error => {
              console.error(`${statusText}学期错误`, error);
              this.$message.error(`${statusText}失败`);
            });
      }).catch(() => {
        // 取消操作
      });
    }
  }
}
</script>

<style scoped>
.term-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-term {
  color: #409EFF;
  font-weight: bold;
}
</style> 