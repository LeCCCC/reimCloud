import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import vueDevTools from 'vite-plugin-vue-devtools'
// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(),
    vueDevTools(),
    AutoImport({
      //在代码中自动导入 Element Plus 的组件或函数
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver({
        //在你使用 Element Plus 组件时，自动按需导入组件并且导入它们的样式。避免一次性加载所有样式文件。
        importStyle: 'css',
      })],
    }),
  ],
  server:{
    proxy: {
      '/api':{
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }

})
