import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import {fileURLToPath, URL} from 'node:url';

// 引入 unplugin 相关插件
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers';
import Icons from 'unplugin-icons/vite'; // 用于图标
import IconsResolver from 'unplugin-icons/resolver'; // 用于图标

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        AutoImport({
            // 自动导入 Vue 相关函数，如：ref, reactive, toRef 等
            imports: ['vue'],
            // 自动导入 Element Plus 相关函数，如：ElMessage, ElMessageBox... (根据需要添加)
            // 如果需要自动导入 ElMessage 等，需要在此处配置
            resolvers: [
                ElementPlusResolver(),
                // 自动导入图标组件
                IconsResolver({
                    prefix: 'Icon', // 图标组件前缀，例如 <IconEpEditPen />
                }),
            ],
            dts: 'src/auto-imports.d.ts', // 生成 TypeScript 声明文件
        }),
        Components({
            resolvers: [
                // 自动注册 Element Plus 组件
                ElementPlusResolver(),
                // 自动注册图标组件
                IconsResolver({
                    enabledCollections: ['ep'], // 'ep' 是 @element-plus/icons-vue 的集合名称
                }),
            ],
            dts: 'src/components.d.ts', // 生成 TypeScript 声明文件
        }),
        Icons({
            autoInstall: true, // 自动安装图标库
        }),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)), // 设置@别名，兼容Vue CLI项目
        }
    },
    server: {
        port: 7777,
        open: true,
        proxy: {
            // 代理所有 /api 请求到后端服务器
            '/api': {
                target: 'http://localhost:8080', // SSM服务运行在8080端口
                changeOrigin: true,
                secure: false,
            }
        }
    },
    // 配置构建输出目录到后端项目的静态资源目录
    build: {
        outDir: '../backend/src/main/resources/static',
        emptyOutDir: true
    },
    // 兼容原有的环境变量配置
    define: {
        'process.env': process.env
    }
}); 