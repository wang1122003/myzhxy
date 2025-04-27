import {createRouter, createWebHistory} from 'vue-router'
// 导入 Element Plus 图标
import {
    House,
    User,
    Notebook,
    OfficeBuilding,
    Calendar,
    Flag, // 用于 Activity
    Bell, // 用于 Notice/Notices
    ChatDotRound, // 用于 Forum
    Files,
    Setting,
    HomeFilled, // 学生/教师首页
    Document, // 学生成绩
    DataAnalysis, // 教师成绩管理入口
    Reading, // 教师课程列表
    Collection // 教师课程管理
    // 可以根据需要添加更多图标
} from '@element-plus/icons-vue'

// 从 @/utils/auth 导入
import {isLoggedIn, token, userRole} from '@/utils/auth'

// 公共页面路由
const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/HomePage.vue'),
        beforeEnter: (to, from, next) => {
            const token = localStorage.getItem('token')
            const userRole = localStorage.getItem('role')

            if (token && userRole) {
                // 已登录用户根据角色重定向到对应的首页
                if (userRole === 'admin') {
                    next('/admin')
                } else if (userRole === 'teacher') {
                    next('/teacher')
                } else if (userRole === 'student') {
                    next('/student')
                } else {
                    next() // 未知角色，显示普通首页
                }
            } else {
                next() // 未登录用户显示普通首页
            }
        }
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
                component: () => import('../views/student/Home.vue'),
                meta: {title: '学生主页', icon: HomeFilled, showInSidebar: true}
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
                component: () => import('../views/student/MyGrades.vue'),
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
                path: 'files',
                name: 'StudentFiles',
                component: () => import('../views/student/Files.vue'),
                meta: {title: '文件中心', icon: Files, showInSidebar: true}
            },
            {
                path: 'notices',
                name: 'StudentNotices',
                component: () => import('../views/student/Notices.vue'),
                meta: {title: '通知公告', icon: Bell, showInSidebar: true}
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
                component: () => import('../views/teacher/Home.vue'),
                meta: {title: '教师工作台', icon: HomeFilled, showInSidebar: true}
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
                component: () => import('../views/teacher/Courses.vue'),
                meta: {title: '课程列表', icon: Reading, showInSidebar: true}
            },
            {
                path: 'course-management',
                name: 'TeacherCourseManagement',
                component: () => import('../views/teacher/TeacherCourseManagement.vue'),
                meta: {title: '我的课程管理', icon: Collection, showInSidebar: true}
            },
            {
                path: 'grades',
                name: 'TeacherGradeManagementEntry',
                component: () => import('../views/teacher/GradeManagementEntry.vue'),
                meta: {title: '成绩管理', icon: DataAnalysis, showInSidebar: true}
            },
            {
                path: 'courses/:courseId/grades',
                name: 'TeacherCourseGradeManagement',
                component: () => import('../views/teacher/CourseGradeManagement.vue')
            },
            {
                path: 'courses/:courseId/resources',
                name: 'TeacherCourseResources',
                component: () => import('../views/teacher/CourseResources.vue')
            },
            {
                path: 'notices',
                name: 'TeacherNotices',
                component: () => import('../views/teacher/TeacherNoticeView.vue'),
                meta: {title: '通知公告', icon: Bell, showInSidebar: true}
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
                path: '',
                name: 'AdminHome',
                component: () => import('../views/admin/Home.vue'),
                meta: {title: '管理员工作台', icon: House, showInSidebar: true}
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
                path: 'notice',
                name: 'AdminNotice',
                component: () => import('../views/admin/Notice.vue'),
                meta: {title: '公告管理', icon: Bell, showInSidebar: true}
            },
            {
                path: 'forum',
                name: 'AdminForum',
                component: () => import('../views/admin/Forum.vue'),
                meta: {title: '论坛管理', icon: ChatDotRound, showInSidebar: true}
            },
            {
                path: 'file',
                name: 'AdminFile',
                component: () => import('../views/admin/File.vue'),
                meta: {title: '文件管理', icon: Files, showInSidebar: true}
            },
            {
                path: 'settings',
                name: 'AdminSettings',
                component: () => import('../views/admin/Settings.vue'),
                meta: {title: '系统设置', icon: Setting, showInSidebar: true}
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

// 应用初始化时尝试恢复用户状态 (逻辑已移至 App.vue)
// 移除旧的 tryRestoreUserSession 函数

// 路由守卫
router.beforeEach((to, from, next) => {
    // 直接使用导入的响应式变量和计算属性
    const _isLoggedIn = isLoggedIn.value;
    const _userRole = userRole.value; // 获取当前用户角色
    // const _token = token.value; // token 检查 _isLoggedIn 内部已包含

    console.log(`路由跳转：${from.path} -> ${to.path}`); // 保留日志

    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title + ' - 智慧化校园服务系统'
    } else {
        document.title = '智慧化校园服务系统'
    }

    // 存储前一个页面的路径（用于实现"返回"功能） - 这个逻辑放这里可能不准确，因为 next() 可能重定向
    // 考虑在 App.vue 或布局组件中使用 watch(route) 来保存前一个路由
    // localStorage.setItem('prevPath', from.path)

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
                next('/error'); // 或者跳转到专门的无权限页面
            } else {
                // 1.2.2 角色匹配 或 路由不需要特定角色 -> 允许访问
                console.log(`认证通过 (${requiredRole ? '角色匹配' : '无需特定角色'})，放行到 ${to.path}`);
                next();
            }
        }
    } else {
        // 2. 目标路由不需要认证
        if (to.name === 'Home' && _isLoggedIn) {
            // 2.1 访问的是主页（通常是登录页或通用首页），但用户已登录 -> 重定向到角色首页
            console.log(`已登录，访问首页，重定向到角色首页 (${_userRole})`);
            if (_userRole === 'admin') {
                next('/admin/notice');
            } else if (_userRole === 'teacher') {
                next('/teacher'); // 假设教师首页是 /teacher
            } else if (_userRole === 'student') {
                next('/student'); // 假设学生首页是 /student
            } else {
                console.warn('未知角色，重定向到 /');
                next('/'); // 未知角色留在首页或特定页面
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