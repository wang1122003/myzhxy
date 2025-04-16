import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList } from '@/api/user'
import { getCourseList } from '@/api/course'
import { getClassroomList } from '@/api/classroom'
import { getScheduleList } from '@/api/schedule'
import { getActivityList } from '@/api/activity'
import { getNoticeList } from '@/api/notice'
import { getPostList } from '@/api/forum'

export default {
name: 'AdminHome',
setup() {
const userCount = ref(0)
const courseCount = ref(0)
const classroomCount = ref(0)
const scheduleCount = ref(0)
const activityCount = ref(0)
const noticeCount = ref(0)
const postCount = ref(0)

const fetchStatistics = () => {
// 获取用户数量
getUserList({ page: 0, size: 1 }).then(response => {
userCount.value = response.data.totalElements
}).catch((error) => {
console.error('获取用户数量失败', error)
ElMessage.error('获取用户数量失败')
})

// 获取课程数量
getCourseList({ page: 0, size: 1 }).then(response => {
courseCount.value = response.data.totalElements
}).catch((error) => {
console.error('获取课程数量失败', error)
ElMessage.error('获取课程数量失败')
})

// 获取教室数量
getClassroomList({ page: 0, size: 1 }).then(response => {
classroomCount.value = response.data.totalElements
}).catch((error) => {
console.error('获取教室数量失败', error)
ElMessage.error('获取教室数量失败')
})

// 获取课表数量
getScheduleList({ page: 0, size: 1 }).then(response => {
scheduleCount.value = response.data.totalElements
}).catch((error) => {
console.error('获取课表数量失败', error)
ElMessage.error('获取课表数量失败')
})

// 获取活动数量
getActivityList({ page: 0, size: 1 }).then(response => {
activityCount.value = response.data.totalElements
}).catch((error) => {
console.error('获取活动数量失败', error)
ElMessage.error('获取活动数量失败')
})

// 获取通知数量
getNoticeList({ page: 0, size: 1 }).then(response => {
noticeCount.value = response.data.totalElements
}).catch((error) => {
console.error('获取通知数量失败', error)
ElMessage.error('获取通知数量失败')
})

// 获取帖子数量
getPostList({ page: 0, size: 1 }).then(response => {
postCount.value = response.data.totalElements
}).catch((error) => {
console.error('获取帖子数量失败', error)
ElMessage.error('获取帖子数量失败')
})
}

onMounted(() => {
fetchStatistics()
})

return {
userCount,
courseCount,
classroomCount,
scheduleCount,
activityCount,
noticeCount,
postCount
}
}
} 