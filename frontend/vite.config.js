// vite.config.ts
//支援本機 & Docker 兩種情境
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const PORT = Number(env.VITE_PORT || 5173)
  const HMR_PORT = Number(env.VITE_HMR_PORT || 24678)
  const PROXY_TARGET = env.VITE_PROXY_TARGET || 'http://localhost:8080'
  return {
    plugins: [vue()],
    server: {
      host: true,
      port: PORT,
      hmr: { host: 'localhost', port: HMR_PORT },
      proxy: {
        '/api': {
          target: PROXY_TARGET,    // 本機：http://localhost:8080；Docker：http://backend:8080
          changeOrigin: true,
          ws: true,                // 若後端有 WebSocket/SSE，順便打開
          rewrite: p => p.replace(/^\/api/, ''),
        },
      },
      watch: {
        usePolling: process.env.CHOKIDAR_USEPOLLING === 'true'
      }
    }
  }
})
