<template>
  <div style="max-width:360px;margin:48px auto;padding:24px;border:1px solid #eee;border-radius:12px;">
    <h2 style="margin:0 0 16px;">登入</h2>
    <form @submit.prevent="submit">
      <input
        v-model="email"
        type="email"
        placeholder="Email"
        required
        style="width:100%;padding:10px;margin-bottom:10px;border:1px solid #ccc;border-radius:8px;"
      />
      <input
        v-model="password"
        type="password"
        placeholder="密碼"
        required
        style="width:100%;padding:10px;margin-bottom:10px;border:1px solid #ccc;border-radius:8px;"
      />
      <button
        type="submit"
        style="width:100%;padding:10px;border:none;border-radius:8px;background:#1e80ff;color:#fff;cursor:pointer;"
      >
        登入
      </button>
    </form>
    <p v-if="err" style="color:#c00;margin-top:12px;">{{ err }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Auth } from '../api';
import { useRouter } from 'vue-router';

const email = ref('test@email.com');
const password = ref('test123');
const err = ref('');
const router = useRouter();

async function submit(){
  err.value = '';
  try{
    await Auth.login(email.value, password.value);
    router.push('/app/likes');   // ✅ 改這行
  }catch(e){
    console.error(e);
    err.value = '帳密錯誤';
  }
}
</script>
