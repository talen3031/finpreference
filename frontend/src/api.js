// api.js (fixed)
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',                // ← 前端呼叫 /api/xxxx
  withCredentials: true
})

function emitAuthChanged(){ 
  if (typeof window !== 'undefined') window.dispatchEvent(new Event('auth-changed'));
}

function getAccess(){ return localStorage.getItem('accessToken'); }
function setAccess(t){ 
  if (t) localStorage.setItem('accessToken', t); 
  emitAuthChanged();                        // ★ 新增
}
function clearAccess(){ 
  localStorage.removeItem('accessToken'); 
  emitAuthChanged();                        // ★ 新增
}

// 對所有經由 api 的請求自動加 Authorization
api.interceptors.request.use(cfg => {
  const t = getAccess();
  if (t) cfg.headers['Authorization'] = `Bearer ${t}`;
  return cfg;
});

// 嘗試 refresh 邏輯（遇 401 時）
let refreshing = false;
let queue = [];
async function refreshToken(){
  // ★ 改成走 api 實例，會發到 /api/auth/refresh → 由 Vite proxy 轉到後端 /auth/refresh
  const res = await api.post('/auth/refresh', {});
  const newToken = res.data?.data?.accessToken;
  setAccess(newToken);
  return newToken;
}

api.interceptors.response.use(
  r => r,
  async err => {
    const status = err?.response?.status;
    const original = err.config;
    if (status === 401 && !original._retried) {
      original._retried = true;
      try {
        if (!refreshing) {
          refreshing = true;
          const t = await refreshToken();
          refreshing = false;
          queue.forEach(fn => fn(t));
          queue = [];
        } else {
          // 等待前一個 refresh 完成
          const t = await new Promise(resolve => queue.push(resolve));
          original.headers['Authorization'] = `Bearer ${t}`;
          return api.request(original);
        }
        original.headers['Authorization'] = `Bearer ${getAccess()}`;
        return api.request(original);
      } catch(e) {
        refreshing = false; queue = [];
        clearAccess();
        window.location.href = '/login';
        return Promise.reject(e);
      }
    }
    return Promise.reject(err);
  }
);

export const Auth = {
  async login(email, password){
    // ★ 改成走 api 實例（會命中 /api/auth/login → 後端 /auth/login）
    const res = await api.post('/auth/login', { email, password });
    setAccess(res.data.data.accessToken);
    return res.data.data.userId;
  },
  async logout(){
    await api.post('/auth/logout', {}); // 同步改用 api 實例
    clearAccess();
  },
  async register({ userName, email, password, account }) {
    // POST /auth/register → ApiResponse<String>("ok")
    const res = await api.post('/auth/register', { userName, email, password, account });
    return res.data?.data; // "ok"
  },
  async refresh(){
    const res = await api.post('/auth/refresh', {});
    const t = res.data?.data?.accessToken;
    setAccess(t);
    return res.data?.data;
  }
};

export const Products = {
  list: () => api.get('/products').then(r => r.data.data),

};


// ★ 新增：統一處理 { ok, data, error } 響應的工具
function unwrap(response) {
  const body = response?.data;
  // 後端若採用 { ok, data, error } 格式
  if (body && typeof body === 'object' && 'ok' in body) {
    if (body.ok) return body.data;
    const msg = body.error || '操作失敗';
    const err = new Error(msg);
    // 附帶原始資料，方便頁面端需要更多資訊時使用
    err.responseBody = body;
    throw err;
  }
  // 若不是該格式，維持原本取 data.data 的行為
  return body?.data ?? body;
}

// ★ 修改 Likes 這三個方法使用 unwrap（list 是否要用視你的後端回傳格式而定，保守起見也用）
export const Likes = {
  list:   () => api.get('/likes').then(unwrap),
  create: (payload) => api.post('/likes', payload).then(unwrap),
  update: (sn, payload) => api.put(`/likes/${sn}`, payload).then(unwrap),
  remove: (sn) => api.delete(`/likes/${sn}`).then(unwrap),
};