<!-- src/views/RegisterView.vue -->
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Auth } from '../api.js';

const router = useRouter();

const userName = ref('');
const email = ref('');
const password = ref('');
const account = ref('');
const loading = ref(false);
const okMsg = ref('');
const errMsg = ref('');

// 即時過濾：僅允許數字，長度最多 10
function onAccountInput(e) {
  const digitsOnly = (e.target.value || '').replace(/\D+/g, '').slice(0, 10);
  account.value = digitsOnly;
}

function validate() {
  errMsg.value = '';
  if (!userName.value.trim()) { errMsg.value = '請輸入使用者名稱'; return false; }
  if (!email.value.trim())    { errMsg.value = '請輸入 Email'; return false; }
  if (!password.value.trim()) { errMsg.value = '請輸入密碼'; return false; }
  if (!account.value.trim())  { errMsg.value = '請輸入帳號（account）'; return false; }

  // 僅允許 10 位數字，例如 1111999666
  if (!/^\d{10}$/.test(account.value)) {
    errMsg.value = '扣款帳號格式錯誤：必須為連續 10 位數字（例：1111999666）';
    return false;
  }
  return true;
}

async function handleSubmit(){
  if (!validate()) return;
  loading.value = true;
  try {
    await Auth.register({
      userName: userName.value,
      email: email.value,
      password: password.value,
      account: account.value,
    }); // 期待 "ok"
    okMsg.value = '註冊成功，將為您導向登入頁…';
    setTimeout(() => router.push('/login'), 1200);
  } catch (e) {
    errMsg.value = e?.response?.data?.error || '註冊失敗，請稍後再試';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="card">
      <h2>建立新帳號</h2>

      <label>使用者名稱</label>
      <input v-model="userName" type="text" placeholder="請輸入使用者名稱" />

      <label>Email</label>
      <input v-model="email" type="email" placeholder="you@example.com" />

      <label>密碼</label>
      <input v-model="password" type="password" placeholder="至少 6 碼" />

      <label>Account 扣款帳號</label>
      <input
        :value="account"
        @input="onAccountInput"
        inputmode="numeric"
        pattern="\\d{10}"
        maxlength="10"
        type="text"
        placeholder="例如 1111999666"
        aria-describedby="account-hint"
      />
      <small id="account-hint" class="hint">僅限 10 位數字（例如：1111999666）</small>

      <button :disabled="loading" @click="handleSubmit">
        {{ loading ? '建立中…' : '註冊' }}
      </button>

      <p v-if="okMsg" class="ok">{{ okMsg }}</p>
      <p v-if="errMsg" class="err">{{ errMsg }}</p>

      <p class="to-login">
        已有帳號？<router-link to="/login">前往登入</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f6f7fb;
}
.card {
  width: 360px;
  max-width: 92vw;
  background: #fff;
  padding: 24px 20px;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,.06);
}
h2 { margin: 0 0 16px; }
label { display:block; margin: 12px 0 6px; font-size: 14px; color:#555; }
input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 10px;
  outline: none;
}
input:focus { border-color: #6b8afd; }
button {
  width: 100%;
  margin-top: 16px;
  padding: 10px 12px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  background: #6b8afd;
  color: #fff;
  font-weight: 600;
}
.ok { color: #2e7d32; margin-top: 12px; }
.err { color: #d32f2f; margin-top: 12px; }
.to-login { margin-top: 10px; font-size: 14px; }
.hint { display:block; margin-top: 6px; color:#777; font-size: 12px; }
</style>
