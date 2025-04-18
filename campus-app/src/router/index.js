import {createRouter, createWebHistory} from 'vue-router'

// 公共页面路由
const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/HomePage.vue')
    },
    {
        path: '/error',
        name: 'Error',
        component: () => import('../views/Error.vue')
    },
    // 论坛页面路由 (公共访问)
    {
        path: '/forum',
        name: 'Forum',
        component: () => import('../views/Forum.vue')
    },
    {
        path: '/forum/post/:id',
        name: 'PostDetail',
        component: () => import('../views/PostDetail.vue'),
        meta: {title: '帖子详情'}
    },
    // 学生页面路由
    {
        path: '/student',
        name: 'StudentHome',
        component: () => import('../views/student/Index.vue'),
        meta: {requiresAuth: true, role: 'student'},
        children: [
            {
                path: 'profile',
                name: 'StudentProfile',
                component: () => import('../views/student/Profile.vue')
            },
            {
                path: 'schedule',
                name: 'StudentSchedule',
                component: () => import('../views/student/Schedule.vue')
            },
            {
                path: 'grades',
                name: 'StudentGrades',
                component: () => import('../views/student/Grades.vue')
            },
            {
                path: 'activities',
                name: 'StudentActivities',
                component: () => import('../views/student/Activities.vue')
            },
            {
                path: 'activity/:id',
                name: 'StudentActivity',
                component: () => import('../views/student/Activity.vue')
            },
            {
                path: 'files',
                name: 'StudentFiles',
                component: () => import('../views/student/Files.vue')
            }
        ]
    },
    // 教师页面路由
    {
        path: '/teacher',
        name: 'TeacherHome',
        component: () => import('../views/teacher/Index.vue'),
        meta: {requiresAuth: true, role: 'teacher'},
        children: [
            {
                path: 'profile',
                name: 'TeacherProfile',
                component: () => import('../views/teacher/Profile.vue')
            },
            {
                path: 'schedule',
                name: 'TeacherSchedule',
                component: () => import('../views/teacher/Schedule.vue')
            },
            {
                path: 'courses',
                name: 'TeacherCourses',
                component: () => import('../views/teacher/Courses.vue')
            },
            {
                path: 'courses/:courseId/resources',
                name: 'TeacherCourseResources',
                component: () => import('../views/teacher/CourseResources.vue'),
                meta: {title: '课程资源管理'}
            },
            {
                path: 'courses/:courseId/grades',
                name: 'TeacherCourseGrades',
                component: () => import('../views/teacher/CourseGrades.vue'),
                meta: {title: '课程成绩管理'}
            }
        ]
    },
    // 管理员页面路由
    {
        path: '/admin',
        name: 'AdminHome',
        component: () => import('../views/admin/Index.vue'),
        meta: {requiresAuth: true, role: 'admin'},
        children: [
            {
                path: 'profile',
                name: 'AdminProfile',
                component: () => import('../views/admin/Profile.vue')
            },
            {
                path: 'user',
                name: 'AdminUser',
                component: () => import('../views/admin/User.vue')
            },
            {
                path: 'course',
                name: 'AdminCourse',
                component: () => import('../views/admin/Course.vue')
            },
            {
                path: 'classroom',
                name: 'AdminClassroom',
                component: () => import('../views/admin/Classroom.vue')
            },
            {
                path: 'schedule',
                name: 'AdminSchedule',
                component: () => import('../views/admin/Schedule.vue')
            },
            {
                path: 'activity',
                name: 'AdminActivity',
                component: () => import('../views/admin/Activity.vue')
            },
            {
                path: 'notice',
                name: 'AdminNotice',
                component: () => import('../views/admin/Notice.vue')
            },
            {
                path: 'forum',
                name: 'AdminForum',
                component: () => import('../views/admin/Forum.vue')
            },
            {
                path: 'file',
                name: 'AdminFile',
                component: () => import('../views/admin/File.vue')
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*', // 捕获所有未匹配的路由
        name: 'NotFound',
        component: () => import('../views/NotFound.vue') // 指向 404 页面
    },
    // 添加通知列表公共页面路由
    {
        path: '/notices',
        name: 'NoticeList',
        component: () => import('../views/NoticeList.vue'), // 新建的通知列表组件
        meta: {title: '通知公告'} // 可选的路由元信息
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('role')

    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title + ' - 校园管理系统'
    } else {
        document.title = '校园管理系统'
    }

    // 存储前一个页面的路径（用于实现"返回"功能）
    localStorage.setItem('prevPath', from.path)

    // 访问权限控制
    if (to.meta.requiresAuth && !token) {
        // 未登录访问受保护页面，重定向到首页 /
        next({path: '/', query: {redirect: to.fullPath}}) // 带上原目标路径
    } else if (to.meta.role && to.meta.role !== userRole) {
        // 角色不匹配，重定向到错误页
        next('/error')
    } else {
        // 控制台输出路由跳转信息（仅在开发环境）
        if (process.env.NODE_ENV === 'development') {
            console.log(`路由跳转：${from.path} -> ${to.path}`)
        }
        
        next()
    }
})

// 路由后置钩子
// eslint-disable-next-line no-unused-vars
router.afterEach((_to, _from) => {
    // 页面跳转后滚动到顶部
    window.scrollTo(0, 0)
})

export default router 