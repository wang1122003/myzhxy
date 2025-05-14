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

// 通用/核心组件不再全局导入和注册，改为在使用的页面中局部导入
// import StatsCard from './components/common/StatsCard.vue' 
// import DataDisplay from './components/common/DataDisplay.vue' 
// import NoticeDetailDialog from './components/common/NoticeDetailDialog.vue'
// import RichTextEditor from './components/common/RichTextEditor.vue' 
// import DataTable from './components/common/DataTable.vue' // 废弃，使用 TableView.vue
// import TableView from './components/common/TableView.vue' // 改为局部导入
// import SmartForm from './components/common/SmartForm.vue' // 改为局部导入
// import AdvancedFilterForm from './components/common/AdvancedFilterForm.vue' // 改为局部导入

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

// 不再全局注册通用组件
// app.component('StatsCard', StatsCard)
// app.component('DataDisplay', DataDisplay)
// app.component('NoticeDetailDialog', NoticeDetailDialog)
// app.component('RichTextEditor', RichTextEditor)
// app.component('TableView', DataTable) // 移除错误注册
// app.component('TableView', TableView) // 移除全局注册，改为局部导入
// app.component('FormView', SmartForm) // 移除全局注册，改为局部导入
// app.component('FilterForm', AdvancedFilterForm) // 移除全局注册，改为局部导入

// 使用Pinia（必须在使用router前初始化）
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
    locale: zhCn
})

app.mount('#app')
