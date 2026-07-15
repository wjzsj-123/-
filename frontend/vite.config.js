import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  base: './', // 相对路径，确保构建后资源加载正常
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src') // 配置路径别名
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端 Spring Boot 地址
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist', // 前端本地构建目录
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        entryFileNames: 'assets/[name].[hash].js',
        chunkFileNames: 'assets/[name].[hash].js',
        assetFileNames: 'assets/[name].[hash].[ext]'
      }
    },
    target: 'es2015'
  }
})