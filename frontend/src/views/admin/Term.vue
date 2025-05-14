<template>
  <PageContainer title="学期管理">
    <template #header-actions>
      <el-button type="primary" @click="handleAddTerm">添加学期</el-button>
      <el-button @click="fetchTerms">刷新</el-button>
    </template>

    <!-- 学期列表表格 -->
    <TableView
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :data="terms"
        :loading="loading"
        :total="totalTerms"
        border
        @refresh="fetchTerms"
    >
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
          min-width="180">
        <template #default="scope">
          <span :class="{ 'current-term': scope.row.isCurrent === 1 }">
            {{ scope.row.name }}
            <el-tag v-if="scope.row.isCurrent === 1 || scope.row.current === 1" size="small"
                    type="success">当前学期</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column
          label="学年"
          prop="academicYear"
          width="120">
      </el-table-column>
      <el-table-column
          label="学期序号"
          prop="termNumber"
          width="100">
        <template #default="scope">
          <span>{{ getTermNumberText(scope.row.termNumber) }}</span>
        </template>
      </el-table-column>
      <el-table-column
          label="学期日期"
          min-width="220">
        <template #default="scope">
          {{ formatDate(scope.row.startDate) }} 至 {{ formatDate(scope.row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column
          label="状态"
          prop="status"
          width="100">
        <template #default="scope">
          <el-tag :type="(scope.row.status === 1 || scope.row.status === '1') ? 'success' : 'info'">
            {{ (scope.row.status === 1 || scope.row.status === '1') ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
          label="操作"
          fixed="right"
          width="300"
      >
        <template #default="scope">
          <el-button
              size="small"
              link
              type="primary"
              @click="handleEditTerm(scope.row)">编辑
          </el-button>
          <el-button
              v-if="scope.row.isCurrent !== 1 && scope.row.current !== 1"
              size="small"
              link
              type="success"
              @click="handleSetCurrentTerm(scope.row)">设为当前学期
          </el-button>
          <el-button
              :type="(scope.row.status === 1 || scope.row.status === '1') ? 'warning' : 'success'"
              size="small"
              link
              @click="handleChangeStatus(scope.row)">
            {{ (scope.row.status === 1 || scope.row.status === '1') ? '禁用' : '启用' }}
          </el-button>
          <el-button
              v-if="scope.row.isCurrent !== 1 && scope.row.current !== 1"
              size="small"
              type="danger"
              link
              @click="handleDeleteTerm(scope.row)">删除
          </el-button>
        </template>
      </el-table-column>
    </TableView>

    <!-- 添加/编辑学期对话框 -->
    <DialogWrapper
        v-model:visible="dialogVisible"
        :title="isEdit ? '编辑学期' : '添加学期'"
        width="650px"
        @close="resetForm"
    >
      <el-form ref="termFormRef" :model="termForm" :rules="termRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学期代码" prop="code">
              <el-input v-model="termForm.code" :disabled="isEdit" placeholder="例：2023-2024-1"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期名称" prop="name">
              <el-input v-model="termForm.name" placeholder="例：2023-2024学年第一学期"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学年" prop="academicYear">
              <el-input v-model="termForm.academicYear" placeholder="例：2023-2024"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期序号" prop="termNumber">
              <el-select v-model="termForm.termNumber" placeholder="请选择学期序号" style="width: 100%">
                <el-option :value="1" label="第一学期"></el-option>
                <el-option :value="2" label="第二学期"></el-option>
                <el-option :value="3" label="第三学期(短学期/暑期)"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                  v-model="termForm.startDate"
                  format="YYYY-MM-DD"
                  placeholder="选择开始日期"
                  style="width: 100%"
                  type="date"
                  value-format="YYYY-MM-DD"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                  v-model="termForm.endDate"
                  format="YYYY-MM-DD"
                  placeholder="选择结束日期"
                  style="width: 100%"
                  type="date"
                  value-format="YYYY-MM-DD"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否当前学期" prop="isCurrent">
              <el-switch
                  v-model="termForm.isCurrent"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="是"
                  inactive-text="否"
              >
              </el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch
                  v-model="termForm.status"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="启用"
                  inactive-text="禁用"
              >
              </el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="termForm.description" :rows="3" placeholder="学期描述信息" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button :loading="submitting" type="primary" @click="submitTermForm">确 定</el-button>
        </span>
      </template>
    </DialogWrapper>
  </PageContainer>
</template>

<script setup>
import {ref, reactive, onMounted, nextTick} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {getAllTerms, addTerm, updateTerm, deleteTermById, setCurrentTerm as apiSetCurrentTerm} from '@/api/term';
import PageContainer from '@/views/layouts/EnhancedPageContainer.vue';
import TableView from '@/views/ui/TableView.vue';
import DialogWrapper from '@/views/ui/DialogWrapper.vue';

const terms = ref([]);
const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const termFormRef = ref(null);
const pagination = ref({pageNum: 1, pageSize: 10}); // 假设后端支持分页
const totalTerms = ref(0);

const termForm = ref({
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
});

const termRules = reactive({
  code: [{required: true, message: '请输入学期代码', trigger: 'blur'}],
  name: [{required: true, message: '请输入学期名称', trigger: 'blur'}],
  academicYear: [{required: true, message: '请输入学年', trigger: 'blur'}],
  termNumber: [{required: true, message: '请选择学期序号', trigger: 'change'}],
  startDate: [{required: true, message: '请选择开始日期', trigger: 'change'}],
  endDate: [{required: true, message: '请选择结束日期', trigger: 'change'}]
});

const fetchTerms = async () => {
  loading.value = true;
  try {
    const params = {pageNum: pagination.value.pageNum, pageSize: pagination.value.pageSize};
    // 根据持续的日志和用户提供的数据片段，我们假设 getAllTerms(params)
    // 直接返回后端接口中 data 字段的内容，即 {total: N, rows: []} 结构
    const responseData = await getAllTerms(params);

    if (responseData && responseData.rows !== undefined && responseData.total !== undefined) {
      // 处理 {total: N, rows: [...]} 结构
      terms.value = responseData.rows;
      totalTerms.value = responseData.total;
    } else if (Array.isArray(responseData)) {
      // 为了代码的健壮性，也处理一下如果 getAllTerms 返回的是一个数组的情况
      // (虽然在 Term.vue 的当前调用场景下，期望的是分页对象)
      terms.value = responseData;
      totalTerms.value = responseData.length;
      pagination.value.pageNum = 1; // 如果是数组，通常意味着非分页，重置页码
    } else {
      // 如果 responseData 不是预期的分页对象也不是数组
      console.error('获取学期列表失败，响应数据格式不符合预期:', responseData);
      ElMessage.error('获取学期列表数据格式错误');
      terms.value = [];
      totalTerms.value = 0;
    }
  } catch (error) {
    console.error('获取学期列表时发生错误:', error);
    ElMessage.error(error.message || '获取学期列表失败');
    terms.value = [];
    totalTerms.value = 0;
  } finally {
    loading.value = false;
  }
};

const formatDate = (date) => {
  if (!date) return '未设置';
  try {
    // 尝试直接格式化，适用于 YYYY-MM-DD 或 ISO 字符串
    return new Date(date).toLocaleDateString('zh-CN');
  } catch (e) {
    return date; // 格式化失败返回原值
  }
};

const getTermNumberText = (termNumber) => {
  const termMap = {
    1: '第一学期',
    2: '第二学期',
    3: '第三学期(短学期/暑期)'
  };
  return termMap[termNumber] || '未知';
};

const resetForm = () => {
  if (termFormRef.value) {
    termFormRef.value.resetFields();
  }
  termForm.value = {
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
};

const handleAddTerm = () => {
  resetForm();
  isEdit.value = false;
  dialogVisible.value = true;
};

const handleEditTerm = (term) => {
  resetForm();
  isEdit.value = true;
  // 深拷贝以避免直接修改表格数据
  termForm.value = JSON.parse(JSON.stringify(term));
  // 确保 isCurrent 和 status 是数字类型
  termForm.value.isCurrent = Number(termForm.value.isCurrent ?? termForm.value.current ?? 0);
  termForm.value.status = Number(termForm.value.status ?? 1);

  nextTick(() => {
    dialogVisible.value = true;
  });
};

const submitTermForm = async () => {
  if (!termFormRef.value) return;
  await termFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const formData = {
          ...termForm.value,
          // 确保 isCurrent 和 status 是数字
          isCurrent: Number(termForm.value.isCurrent),
          status: Number(termForm.value.status)
        };

        if (isEdit.value) {
          await updateTerm(formData);
          ElMessage.success('学期更新成功');
        } else {
          await addTerm(formData);
          ElMessage.success('学期添加成功');
        }
        dialogVisible.value = false;
        fetchTerms();
      } catch (error) {
        console.error('提交学期表单失败', error);
        ElMessage.error(error.message || '操作失败');
      } finally {
        submitting.value = false;
      }
    } else {
      console.log('表单验证失败');
      return false;
    }
  });
};

const handleSetCurrentTerm = (term) => {
  ElMessageBox.confirm(`确定要将 【${term.name}】 设置为当前学期吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await apiSetCurrentTerm(term.id);
      ElMessage.success('设置成功');
      fetchTerms(); // 刷新列表以更新状态
    } catch (error) {
      console.error('设置当前学期失败', error);
      ElMessage.error(error.message || '设置失败');
    }
  }).catch(() => {
    ElMessage.info('已取消操作');
  });
};

const handleChangeStatus = (term) => {
  const newStatus = (term.status === 1 || term.status === '1') ? 0 : 1;
  const actionText = newStatus === 1 ? '启用' : '禁用';
  ElMessageBox.confirm(`确定要${actionText}学期 【${term.name}】 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const termToUpdate = {...term, status: newStatus};
      await updateTerm(termToUpdate);
      ElMessage.success(`${actionText}成功`);
      fetchTerms();
    } catch (error) {
      console.error(`更改学期状态失败`, error);
      ElMessage.error(error.message || `${actionText}失败`);
    }
  }).catch(() => {
    ElMessage.info('已取消操作');
  });
};

const handleDeleteTerm = (term) => {
  if (term.isCurrent === 1 || term.current === 1) {
    ElMessage.warning('不能删除当前学期');
    return;
  }
  ElMessageBox.confirm(`确定要删除学期 【${term.name}】 吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'danger'
  }).then(async () => {
    try {
      await deleteTermById(term.id);
      ElMessage.success('删除成功');
      fetchTerms();
    } catch (error) {
      console.error('删除学期失败', error);
      ElMessage.error(error.message || '删除失败');
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

onMounted(() => {
  fetchTerms();
});
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.current-term {
  font-weight: bold;
  color: var(--el-color-success);
}

.dialog-footer {
  text-align: right;
}
</style> 