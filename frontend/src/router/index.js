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
router.beforeEach(async (to, from, next) => {
    // Pinia store instance
    const userStore = useUserStore();

    // 关键：确保 Pinia store 实例有效
    if (!userStore) {
        console.error("[RouterGuard] 严重错误: User store 不可用! 重定向到 /error。");
        // 确保 '/error' 路由存在，或者以其他方式处理此严重错误
        return next({name: 'Error', query: {message: '应用程序严重错误：用户会话管理失败。'}});
    }

    // 从 Pinia store 获取状态 (Pinia 会为 setup stores 自动解包 ref/computed)
    const isLoggedIn = userStore.isLoggedIn;
    const userRole = userStore.userRole;

    console.log(`[RouterGuard] 导航: 从 "${from.path}" 到 "${to.path}". 用户登录状态: ${isLoggedIn}, 角色: "${userRole}"`);

    // 设置页面标题
    if (to.meta && to.meta.title) {
        document.title = `${to.meta.title} - 智慧化校园服务系统`;
    } else {
        document.title = '智慧化校园服务系统';
    }

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiredRouteRole = to.meta.role; // 路由 meta 中定义的目标角色

    if (requiresAuth) {
        // 1. 目标路由需要认证
        if (!isLoggedIn) {
            // 1.1 用户未登录
            console.log(`[RouterGuard] 路由 "${to.path}" 需要认证。用户未登录。重定向到登录页 "/"。`);
            next({path: '/', query: {redirect: to.fullPath}});
        } else {
            // 1.2 用户已登录，检查角色权限
            if (requiredRouteRole) {
                // 1.2.1 路由需要特定角色
                if (userRole === requiredRouteRole) {
                    console.log(`[RouterGuard] 路由 "${to.path}" 需要角色 "${requiredRouteRole}"。用户角色匹配。允许访问。`);
                    next();
                } else {
                    console.warn(`[RouterGuard] 路由 "${to.path}" 需要角色 "${requiredRouteRole}"。用户角色为 "${userRole}"。访问被拒绝。重定向到错误页。`);
                    next({name: 'Error', query: {code: '403', message: '访问被拒绝：您没有查看此页面的权限。'}});
                }
            } else {
                // 1.2.2 路由需要认证，但不需要特定角色
                console.log(`[RouterGuard] 路由 "${to.path}" 需要认证。用户已登录（无需特定角色）。允许访问。`);
                next();
            }
        }
    } else {
        // 2. 目标路由是公共页面 (不需要认证)
        // 如果用户已登录，并且尝试访问仅为未登录用户设计的主页 (例如 '/')
        if (to.name === 'Home' && isLoggedIn) {
            console.log(`[RouterGuard] 用户已登录并尝试访问公共主页 "Home"。根据角色 "${userRole}" 重定向到用户仪表盘。`);
            switch (userRole) {
                case 'admin':
                    next({path: '/admin/notice'}); // 请根据实际的管理员主页路径调整
                    break;
                case 'teacher':
                    next({path: '/teacher'});     // 请根据实际的教师主页路径调整
                    break;
                case 'student':
                    next({path: '/student'});     // 请根据实际的学生主页路径调整
                    break;
                default:
                    console.warn(`[RouterGuard] 已登录用户角色 "${userRole}" 未知或不应从 "Home" 重定向。重定向到 "/"。`);
                    next('/'); // 对于未知角色，或如果 "Home" 页面也可以作为已登录用户的某种着陆页
                    break;
            }
        } else {
            // 2.1 访问其他公共页面，或未登录用户访问 "Home"
            console.log(`[RouterGuard] 路由 "${to.path}" 是公共页面。允许访问。`);
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