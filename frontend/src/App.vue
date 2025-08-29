<template>
  <nav class="navbar">
    <!-- 左邊：主要功能 -->
    <div class="nav-left">
      <router-link v-if="isLoggedIn" to="/app/likes" class="nav-btn">喜好清單</router-link>
      <a v-else href="#" @click.prevent="promptLogin" class="nav-btn">喜好清單</a>

      <router-link v-if="isLoggedIn" to="/app/products" class="nav-btn">商品列表</router-link>
      <a v-else href="#" @click.prevent="promptLogin" class="nav-btn">商品列表</a>
    </div>

    <!-- 右邊：登入 / 註冊 / 登出 -->
    <div class="nav-right">
      <router-link v-if="!isLoggedIn" to="/login" class="nav-btn primary">登入</router-link>
      <router-link v-if="!isLoggedIn" to="/register" class="nav-btn secondary">註冊</router-link>
      <a v-else href="#" @click.prevent="logout" class="nav-btn danger">登出</a>
    </div>
  </nav>
  <router-view />
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Auth } from './api'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(!!localStorage.getItem('accessToken'))

function updateLoginFlag(){
  isLoggedIn.value = !!localStorage.getItem('accessToken')
}

function promptLogin(){
  alert('請先登入後再查看這個頁面')
  router.push('/login')
}

async function logout(){
  await Auth.logout()
  updateLoginFlag()
  router.push('/login')
}

function onAuthChanged(){ updateLoginFlag() }
function onStorage(e){ if (e.key === 'accessToken') updateLoginFlag() }

onMounted(() => {
  window.addEventListener('auth-changed', onAuthChanged)
  window.addEventListener('storage', onStorage)
})

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', onAuthChanged)
  window.removeEventListener('storage', onStorage)
})
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between; /* 左右分開 */
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ddd;
  background: #f9fafb;
}

.nav-left,
.nav-right {
  display: flex;
  gap: 12px;
}

.nav-btn {
  padding: 6px 14px;
  border-radius: 6px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid transparent;
  color: #333;
  background: #fff;
  transition: all 0.2s ease;
}

.nav-btn:hover {
  background: #f3f4f6;
  border-color: #ccc;
}

/* 特殊按鈕樣式 */
.nav-btn.primary {
  background: #2563eb;
  color: #fff;
}
.nav-btn.primary:hover {
  background: #1d4ed8;
}

.nav-btn.secondary {
  background: #10b981;
  color: #fff;
}
.nav-btn.secondary:hover {
  background: #059669;
}

.nav-btn.danger {
  background: #ef4444;
  color: #fff;
}
.nav-btn.danger:hover {
  background: #dc2626;
}
</style>
