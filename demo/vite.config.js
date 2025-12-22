import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src/main/resources/static') // 配置路径别名
        }
    },
    server: {
        port: 3000, // 前端开发端口
        proxy: {
            // 代理后端接口（解决跨域）
            '/api': {
                target: 'http://localhost:8080', // 后端服务地址
                changeOrigin: true
            }
        }
    }
})