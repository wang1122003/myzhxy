import {createRouter, createWebHistory} from 'vue-router'
// 导入 Element Plus 图标
import {
    Bell,
    Calendar,
    ChatDotRound,
    DataAnalysis,
    Document,
    Flag,
    Notebook,
    OfficeBuilding,
    Reading,
    User
} from '@element-plus/icons-vue'

// 导入用户状态store
import {useUserStore} from '@/stores/userStore'

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
        component: () => import('../views/forum/Forum.vue')
    },
    {
        path: '/forum/create',
        name: 'CreatePost',
        component: () => import('../views/forum/CreatePost.vue'),
        meta: {requiresAuth: true, title: '创建帖子'}
    },
    {
        path: '/forum/post/:id',
        name: 'PostDetail',
        component: () => import('../views/forum/PostDetail.vue'),
        meta: {title: '帖子详情'}
    },
    // 学生页面路由
    {
        path: '/student',
        name: 'StudentLayout',
        component: () => import('../views/student/Index.vue'),
        meta: {requiresAuth: true, role: 'student'},
        children: [
            {
                path: '',
                name: 'StudentHome',
                component: () => import('../views/student/Notices.vue'),
                meta: {title: '通知公告', icon: Bell, showInSidebar: true},
            },
            {
                path: 'profile',
                name: 'StudentProfile',
                component: () => import('../views/common/Profile.vue'),
                meta: {title: '个人资料', icon: User, showInSidebar: false}
            },
            {
                path: 'schedule',
                name: 'StudentSchedule',
                component: () => import('../views/student/Schedule.vue'),
                meta: {title: '课程表', icon: Calendar, showInSidebar: true}
            },
            {
                path: 'grades',
                name: 'StudentMyGrades',
                component: () => import('@/views/common/GradeSystem.vue'),
                meta: {title: '我的成绩', icon: Document, showInSidebar: true}
            },
            {
                path: 'courses',
                name: 'StudentCourses',
                component: () => import('../views/student/Course.vue'),
                meta: {title: '我的课程', icon: Notebook, showInSidebar: true}
            },
            {
                path: 'activities',
                name: 'StudentActivities',
                component: () => import('../views/student/Activities.vue'),
                meta: {title: '校园活动', icon: Flag, showInSidebar: true}
            },
            {
                path: 'activity/:id',
                name: 'StudentActivityDetail',
                component: () => import('../views/student/ActivityDetail.vue')
            },
            {
                path: 'classroom-usage',
                name: 'StudentClassroomUsage',
                component: () => import('../views/common/ClassroomUsage.vue'),
                meta: {title: '教室使用情况', icon: OfficeBuilding, showInSidebar: true}
            }
        ]
    },
    // 教师页面路由
    {
        path: '/teacher',
        name: 'TeacherLayout',
        component: () => import('../views/teacher/Index.vue'),
        meta: {requiresAuth: true, role: 'teacher'},
        children: [
            {
                path: '',
                name: 'TeacherHome',
                component: () => import('../views/teacher/TeacherNoticeView.vue'),
                meta: {title: '通知公告', icon: Bell, showInSidebar: true}
            },
            {
                path: 'profile',
                name: 'TeacherProfile',
                component: () => import('../views/common/Profile.vue'),
                meta: {title: '个人资料', icon: User, showInSidebar: false}
            },
            {
                path: 'schedule',
                name: 'TeacherSchedule',
                component: () => import('../views/teacher/Schedule.vue'),
                meta: {title: '我的课表', icon: Calendar, showInSidebar: true}
            },
            {
                path: 'courses',
                name: 'TeacherCourses',
                component: () => import('../views/teacher/TeacherCourseManagement.vue'),
                meta: {title: '我的课程', icon: Reading, showInSidebar: true}
            },
            {
                path: 'grades',
                name: 'TeacherGradeManagementEntry',
                component: () => import('@/views/common/GradeSystem.vue'),
                meta: {title: '成绩管理', icon: DataAnalysis, showInSidebar: true}
            },
            {
                path: 'courses/:courseId/grades',
                name: 'TeacherCourseGradeManagement',
                component: () => import('../views/teacher/CourseGradeManagement.vue')
            },
            {
                path: 'classroom-usage',
                name: 'TeacherClassroomUsage',
                component: () => import('../views/common/ClassroomUsage.vue'),
                meta: {title: '教室使用情况', icon: OfficeBuilding, showInSidebar: true}
            }
        ]
    },
    // 管理员页面路由
    {
        path: '/admin',
        name: 'AdminLayout',
        component: () => import('../views/admin/Index.vue'),
        meta: {requiresAuth: true, role: 'admin'},
        children: [
            {
                path: 'notice',
                name: 'AdminHome',
                component: () => import('../views/admin/Notice.vue'),
                meta: {title: '公告管理', icon: Bell, showInSidebar: true}
            },
            {
                path: 'profile',
                name: 'AdminProfile',
                component: () => import('../views/common/Profile.vue'),
                meta: {title: '管理员信息', icon: User, showInSidebar: false}
            },
            {
                path: 'user',
                name: 'AdminUser',
                component: () => import('../views/admin/User.vue'),
                meta: {title: '用户管理', icon: User, showInSidebar: true}
            },
            {
                path: 'course',
                name: 'AdminCourse',
                component: () => import('../views/admin/Course.vue'),
                meta: {title: '课程管理', icon: Notebook, showInSidebar: true}
            },
            {
                path: 'term',
                name: 'AdminTerm',
                component: () => import('../views/admin/Term.vue'),
                meta: {title: '学期管理', icon: Calendar, showInSidebar: true}
            },
            {
                path: 'classroom',
                name: 'AdminClassroom',
                component: () => import('../views/admin/Classroom.vue'),
                meta: {title: '教室管理', icon: OfficeBuilding, showInSidebar: true}
            },
            {
                path: 'schedule',
                name: 'AdminSchedule',
                component: () => import('../views/admin/Schedule.vue'),
                meta: {title: '排课管理', icon: Calendar, showInSidebar: true}
            },
            {
                path: 'activity',
                name: 'AdminActivity',
                component: () => import('../views/admin/Activity.vue'),
                meta: {title: '活动管理', icon: Flag, showInSidebar: true}
            },
            {
                path: 'forum',
                name: 'AdminForum',
                component: () => import('../views/admin/Forum.vue'),
                meta: {title: '论坛管理', icon: ChatDotRound, showInSidebar: true}
            },
            {
                path: 'notices',
                name: 'AdminNotices',
                component: () => import('../views/admin/Notice.vue'),
                meta: {title: '通知公告', icon: Bell, showInSidebar: true}
            },
            {
                path: 'classroom-usage',
                name: 'AdminClassroomUsage',
                component: () => import('../views/common/ClassroomUsage.vue'),
                meta: {title: '教室使用情况', icon: OfficeBuilding, showInSidebar: true}
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*', // 捕获所有未匹配的路由
        name: 'NotFound',
        component: () => import('../views/NotFound.vue') // 指向 404 页面
    },
    // 恢复 /notices 直接指向 NoticeList.vue
    {
        path: '/notices',
        name: 'NoticeList',
        component: () => import('../views/NoticeList.vue'),
        meta: {title: '通知公告'}
    },
    // 添加统一成绩组件的路由
    {
        path: '/grade',
        name: 'GradeSystem',
        component: () => import('@/views/common/GradeSystem.vue'),
        meta: {
            title: '成绩管理',
            requiresAuth: true
        }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 应用初始化时尝试恢复用户状态 (逻辑已移至 App.vue)
// 移除旧的 tryRestoreUserSession 函数

// 路由守卫
router.beforeEach((to, from, next) => {
    // 使用Pinia store获取登录状态
    const userStore = useUserStore();
    const _isLoggedIn = userStore.isLoggedIn();
    const _userRole = userStore.userRole();

    console.log(`路由跳转：${from.path} -> ${to.path}`);

    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title + ' - 智慧化校园服务系统'
    } else {
        document.title = '智慧化校园服务系统'
    }

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiredRole = to.meta.role;

    if (requiresAuth) {
        // 1. 目标路由需要认证
        if (!_isLoggedIn) {
            // 1.1 未登录 -> 跳转登录页
            console.log('需要认证，但未登录，跳转到 /');
            next({path: '/', query: {redirect: to.fullPath}});
        } else {
            // 1.2 已登录
            if (requiredRole && _userRole !== requiredRole) {
                // 1.2.1 角色不匹配 -> 跳转错误页
                console.warn(`权限不足: 目标路由 ${to.path} 需要 ${requiredRole}, 当前角色为 ${_userRole}，跳转到 /error`);
                next('/error');
            } else {
                // 1.2.2 角色匹配 或 路由不需要特定角色 -> 允许访问
                console.log(`认证通过 (${requiredRole ? '角色匹配' : '无需特定角色'})，放行到 ${to.path}`);
                next();
            }
        }
    } else {
        // 2. 目标路由不需要认证
        if (to.name === 'Home' && _isLoggedIn) {
            // 2.1 访问的是主页，但用户已登录 -> 重定向到角色首页
            console.log(`已登录，访问首页，重定向到角色首页 (${_userRole})`);
            if (_userRole === 'admin') {
                next('/admin/notice');
            } else if (_userRole === 'teacher') {
                next('/teacher');
            } else if (_userRole === 'student') {
                next('/student');
            } else {
                console.warn('未知角色，重定向到 /');
                next('/');
            }
        } else {
            // 2.2 访问其他公共页面 -> 允许访问
            console.log(`访问公共页面 ${to.path}，放行`);
            next();
        }
    }
});

// 路由后置钩子
// eslint-disable-next-line no-unused-vars
router.afterEach((_to, _from) => {
    // 页面跳转后滚动到顶部
    window.scrollTo(0, 0)
})

export default router 