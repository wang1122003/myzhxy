<template>
  <el-dialog
      v-model="visible"
      :title="course?.courseName || '课程详情'"
      :width="width"
      @close="handleClose"
  >
    <div v-loading="loading">
      <el-descriptions v-if="course" :column="column" border>
        <!-- 基本信息 -->
        <el-descriptions-item label="课程代码">{{ course.courseCode || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="课程类型">{{ formatCourseType(course.courseType) }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ course.credit || 0 }}</el-descriptions-item>
        <el-descriptions-item label="课时">{{ course.hours || 0 }}</el-descriptions-item>

        <!-- 根据角色显示不同的信息 -->
        <template v-if="role === 'student'">
          <el-descriptions-item label="开课学院">{{ course.collegeName || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ course.teacherName || '待定' }}</el-descriptions-item>
          <el-descriptions-item v-if="showCapacity" label="限选人数">
            {{ course.maxStudents > 0 ? course.maxStudents : '不限' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="showCapacity" label="已选人数">
            {{ course.selectedCount || 0 }}
          </el-descriptions-item>
        </template>

        <template v-if="role === 'teacher'">
          <el-descriptions-item label="授课班级">{{ course.className || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="课程人数">{{ course.studentCount || 0 }}</el-descriptions-item>
        </template>

        <template v-if="role === 'admin'">
          <el-descriptions-item label="开课学院">{{ course.collegeName || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ course.teacherName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="课程状态">{{ formatStatus(course.status) }}</el-descriptions-item>
        </template>

        <!-- 课程安排/上课时间 -->
        <el-descriptions-item :span="column" label="上课时间">
          <div v-if="course.schedules && course.schedules.length">
            <p v-for="(schedule, index) in course.schedules" :key="index">
              {{ formatScheduleTime(schedule) }}
            </p>
          </div>
          <span v-else>待安排</span>
        </el-descriptions-item>

        <!-- 课程介绍 -->
        <el-descriptions-item :span="column" label="课程介绍">
          <div v-html="course.introduction || '暂无介绍'"></div>
        </el-descriptions-item>
      </el-descriptions>

      <div v-else class="empty-content">
        <el-empty description="暂无课程详情"/>
      </div>
    </div>

    <!-- 自定义底部插槽 -->
    <template #footer>
      <slot name="footer">
        <span class="dialog-footer">
          <el-button @click="handleClose">关闭</el-button>
          <slot name="actions"></slot>
        </span>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, watch} from 'vue';
import {ElButton, ElDescriptions, ElDescriptionsItem, ElDialog, ElEmpty} from 'element-plus';
import {getCourseById} from '@/api/course';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  courseId: {
    type: [Number, String],
    default: null
  },
  initialCourse: {
    type: Object,
    default: () => ({})
  },
  role: {
    type: String,
    default: 'student',
    validator: (value) => ['student', 'teacher', 'admin'].includes(value)
  },
  width: {
    type: String,
    default: '650px'
  },
  column: {
    type: Number,
    default: 2
  },
  showCapacity: {
    type: Boolean,
    default: true
  },
  autoLoadDetails: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['update:modelValue', 'close', 'loaded']);

// 内部状态
const visible = ref(props.modelValue);
const loading = ref(false);
const course = ref({...props.initialCourse});

// 计算属性和格式化函数
const formatCourseType = (type) => {
  const map = {
    'COMPULSORY': '必修课',
    'ELECTIVE': '选修课',
    'GENERAL': '通识课'
  };
  return map[type] || type || '未知';
};

const formatStatus = (status) => {
  const map = {
    'ACTIVE': '正常',
    'SUSPENDED': '暂停',
    'CLOSED': '已关闭',
    'PENDING': '待审核'
  };
  return map[status] || status || '未知';
};

const formatScheduleTime = (schedule) => {
  if (!schedule) return '待安排';

  const weekdays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const day = weekdays[schedule.weekday] || `周${schedule.weekday}`;
  const time = `${schedule.startTime?.substring(0, 5) || '?'}-${schedule.endTime?.substring(0, 5) || '?'}`;
  const weeks = `第${schedule.startWeek || '?'}-${schedule.endWeek || '?'}周`;
  const location = schedule.classroomName || '地点待定';

  return `${day} ${time} (${weeks}) @ ${location}`;
};

// 方法
const loadCourseDetails = async (id) => {
  if (!id) return;

  loading.value = true;
  try {
    const res = await getCourseById(id);

    // 健壮的数据处理
    if (res && res.data) {
      course.value = res.data;
    } else if (res && typeof res === 'object') {
      // 如果res本身有数据但没有data属性，尝试直接使用res
      course.value = {...course.value, ...res};
    }

    // 确保schedules存在且是数组
    if (!course.value.schedules) {
      course.value.schedules = [];
    } else if (!Array.isArray(course.value.schedules)) {
      console.warn("课程安排数据非数组格式:", course.value.schedules);
      course.value.schedules = [];
    }

    emit('loaded', course.value);
  } catch (error) {
    console.error("获取课程详情失败:", error);
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  visible.value = false;
  emit('update:modelValue', false);
  emit('close');
};

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal;
  if (newVal && props.autoLoadDetails && props.courseId) {
    loadCourseDetails(props.courseId);
  }
});

watch(() => props.courseId, (newVal, oldVal) => {
  if (newVal !== oldVal && newVal && visible.value && props.autoLoadDetails) {
    loadCourseDetails(newVal);
  }
});

watch(() => props.initialCourse, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    course.value = {...newVal};
  }
}, {deep: true});

// 初始化
if (props.modelValue && props.autoLoadDetails && props.courseId) {
  loadCourseDetails(props.courseId);
} else if (props.initialCourse && Object.keys(props.initialCourse).length > 0) {
  course.value = {...props.initialCourse};
}

// 对外暴露方法
defineExpose({
  loadCourseDetails,
  course
});
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  width: 100%;
}

.empty-content {
  padding: 20px;
  display: flex;
  justify-content: center;
}
</style> 