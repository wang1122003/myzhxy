import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';
import {fileURLToPath, URL} from 'node:url';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)), // 设置@别名，兼容Vue CLI项目
        }
    },
    server: {
        port: 8088, // 保持与之前相同的端口
        open: true
    },
    // 兼容原有的环境变量配置
    define: {
        'process.env': process.env
    }
}); 