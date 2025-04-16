import {createRouter, createWebHistory} from 'vue-router'

// 公共页面路由
const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/error',
        name: 'Error',
        component: () => import('../views/Error.vue')
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

    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.meta.role && to.meta.role !== userRole) {
        next('/error')
    } else {
        next()
    }
})

export default router 