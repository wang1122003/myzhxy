import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// 导入Pinia
import {createPinia} from 'pinia'
// 导入Element Plus图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入全局样式
import './assets/styles/global.scss'
import './assets/styles/common.css'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'

// 导入通用组件
import PageHeader from './components/common/PageHeader.vue'
import StatsCard from './components/common/StatsCard.vue'
import NavMenu from './components/common/NavMenu.vue'
import DataDisplay from './components/common/DataDisplay.vue'
import AppHeader from './components/common/AppHeader.vue'
import NoticeDetailDialog from './components/common/NoticeDetailDialog.vue'
import RichTextEditor from './components/common/RichTextEditor.vue'

// 导入核心组件
import EnhancedPageContainer from './components/common/EnhancedPageContainer.vue'
import DataTable from './components/common/DataTable.vue'
import SmartForm from './components/common/SmartForm.vue'
import AdvancedFilterForm from './components/common/AdvancedFilterForm.vue'

// 添加Vue特性标志配置
window.__VUE_PROD_HYDRATION_MISMATCH_DETAILS__ = false

// 修复ResizeObserver错误
const originalError = window.console.error
window.console.error = (...args) => {
    if (typeof args[0] === 'string' && args[0].includes('ResizeObserver loop')) {
        return
    }
    originalError.apply(window.console, args)
}

const app = createApp(App)

// 创建Pinia实例
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 注册通用组件
app.component('PageHeader', PageHeader)
app.component('StatsCard', StatsCard)
app.component('NavMenu', NavMenu)
app.component('DataDisplay', DataDisplay)
app.component('AppHeader', AppHeader)
app.component('NoticeDetailDialog', NoticeDetailDialog)
app.component('RichTextEditor', RichTextEditor)

// 注册核心组件（使用简单统一的名称）
app.component('PageContainer', EnhancedPageContainer) // 保持与旧版的兼容性
app.component('TableView', DataTable)
app.component('FormView', SmartForm)
app.component('FilterForm', AdvancedFilterForm)

// 使用Pinia（必须在使用router前初始化）
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
    locale: zhCn
})

app.mount('#app')
