import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 添加Vue特性标志配置
window.__VUE_PROD_HYDRATION_MISMATCH_DETAILS__ = false

// 修复ResizeObserver错误
const originalError = window.console.error
window.console.error = (...args) => {
    if (args[0].includes('ResizeObserver loop')) {
        return
    }
    originalError.apply(window.console, args)
}

const app = createApp(App)

app.use(router)
app.use(ElementPlus, {
    locale: zhCn
})

app.mount('#app')
