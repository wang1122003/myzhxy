import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// 导入Pinia
import {createPinia} from 'pinia'

// 导入全局样式
import './assets/styles/global.scss'

// 导入全局通用组件
import PageHeader from './components/common/PageHeader.vue'
import StatsCard from './components/common/StatsCard.vue'
import FormPanel from './components/common/FormPanel.vue'
import NavMenu from './components/common/NavMenu.vue'
import DataDisplay from './components/common/DataDisplay.vue'
import BaseTable from './components/common/BaseTable.vue'

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

// 注册全局组件
app.component('PageHeader', PageHeader)
app.component('StatsCard', StatsCard)
app.component('FormPanel', FormPanel)
app.component('NavMenu', NavMenu)
app.component('DataDisplay', DataDisplay)
app.component('BaseTable', BaseTable)

// 使用Pinia（必须在使用router前初始化）
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
    locale: zhCn
})

app.mount('#app')
