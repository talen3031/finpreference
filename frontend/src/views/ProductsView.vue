<template>
  <div style="max-width:860px;margin:24px auto;padding:12px;">
    <h2 style="margin-bottom:12px;">金融商品列表</h2>

    <!-- 錯誤顯示 -->
    <p v-if="err" style="color:#c00;margin-bottom:8px;">{{ err }}</p>

    <!-- 清單 -->
    <table style="width:100%;border-collapse:collapse;">
      <thead>
        <tr style="background:#f8f8f8;">
          <th style="text-align:left;padding:8px;border-bottom:1px solid #eee;">產品名稱</th>
          <th style="text-align:right;padding:8px;border-bottom:1px solid #eee;">價格</th>
          <th style="text-align:right;padding:8px;border-bottom:1px solid #eee;">手續費率</th>
          <th style="text-align:center;padding:8px;border-bottom:1px solid #eee;"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in items" :key="p.no">
          <td style="padding:8px;border-bottom:1px solid #f1f1f1;">{{ p.productName }}</td>
          <td style="padding:8px;border-bottom:1px solid #f1f1f1;text-align:right;">
            {{ Number(p.price ?? 0).toFixed(2) }}
          </td>
          <td style="padding:8px;border-bottom:1px solid #f1f1f1;text-align:right;">
            {{ (Number(p.feeRate ?? 0) * 100).toFixed(2) }}%
          </td>
          <td style="padding:8px;border-bottom:1px solid #f1f1f1;text-align:center;">
            <button class="btn" @click="openAddLike(p)">加入喜好</button>
          </td>
        </tr>
        <tr v-if="!items.length && !err">
          <td colspan="4" style="padding:16px;text-align:center;color:#888;">目前沒有商品</td>
        </tr>
      </tbody>
    </table>

    <!-- 成功訊息（可選） -->
    <p v-if="notice" style="margin-top:10px;color:#067;">{{ notice }}</p>

    <!-- 加入喜好：覆蓋層 Modal -->
    <div v-if="showAddLike" class="overlay" @click.self="closeAddLike">
      <div class="modal">
        <h3 class="modal-title">
          加入喜好：{{ selectedProduct?.productName }}
        </h3>

        <div class="hint">
          單價 ${{ money(selectedProduct?.price) }}，手續費率 {{ percent(selectedProduct?.feeRate) }}
        </div>

        <form @submit.prevent="submitAddLike" class="form-grid">
          <label class="field">
            <span class="label">扣款帳號</span>
            <input v-model="likeForm.account" class="input" placeholder="請輸入扣款帳號" required />
          </label>

          <label class="field">
            <span class="label">數量</span>
            <input v-model.number="likeForm.quantity" class="input" type="number" min="1" required />
          </label>

          <div class="actions">
            <button class="btn primary" :disabled="busyAddLike">
              <span v-if="busyAddLike" class="spinner"></span>{{ busyAddLike ? '處理中...' : '確認加入' }}
            </button>
            <button class="btn ghost" :disabled="busyAddLike" @click.prevent="closeAddLike">取消</button>
          </div>

          <p v-if="addErr" class="err">{{ addErr }}</p>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Products, Likes } from '../api'

const items = ref([])
const err = ref('')
const notice = ref('')

// 覆蓋層/加入喜好相關狀態
const showAddLike = ref(false)
const selectedProduct = ref(null)
const likeForm = ref({ account: '', quantity: 1 })
const addErr = ref('')
const busyAddLike = ref(false)

function money(n) {
  const v = Number(n ?? 0)
  return v.toFixed(2)
}
function percent(n) {
  const v = Number(n ?? 0) * 100
  return v.toFixed(2) + '%'
}

async function load(){
  err.value = ''
  try{
    const list = await Products.list()
    items.value = Array.isArray(list) ? list : []
  }catch(e){
    console.error(e)
    err.value = '載入商品失敗'
  }
}

function openAddLike(p){
  addErr.value = ''
  notice.value = ''
  selectedProduct.value = p
  likeForm.value = { account: '', quantity: 1 }
  showAddLike.value = true
}

function closeAddLike(){
  showAddLike.value = false
}

async function submitAddLike(){
  addErr.value = ''
  busyAddLike.value = true
  try{
    const productNo = Number(selectedProduct.value?.no)
    if (!productNo) throw new Error('找不到商品編號')

    await Likes.create({
      productNo,
      account: likeForm.value.account,
      quantity: Number(likeForm.value.quantity || 1)
    })

    showAddLike.value = false
    notice.value = `已將「${selectedProduct.value.productName}」加入喜好清單`
  }catch(e){
    console.error(e)
    addErr.value =
      e?.response?.data?.error ||
      e?.responseBody?.error ||
      e?.message ||
      '加入喜好失敗'
  }finally{
    busyAddLike.value = false
  }
}

onMounted(load)
</script>

<style scoped>
/* Buttons */
.btn {
  border: 1px solid #d9d9d9;
  background: #fafafa;
  padding: 8px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: background .15s, transform .02s, border-color .15s;
  font-weight: 600;
}
.btn:hover { background: #f3f3f3; }
.btn:active { transform: translateY(1px); }
.btn:disabled { opacity: .6; cursor: not-allowed; }
.btn.primary {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
}
.btn.primary:hover { background: #3b8cff; border-color: #3b8cff; }
.btn.ghost { background: transparent; }

/* Overlay Modal */
.overlay{
  position: fixed; inset: 0;
  background: rgba(0,0,0,.35);
  display: grid; place-items: center;
  padding: 16px;
  z-index: 1000;
}
.modal{
  width: 100%;
  max-width: 520px;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 12px 28px rgba(0,0,0,.15);
}
.modal-title{
  font-size: 18px; font-weight: 700; margin: 4px 0 8px;
}
.hint{
  font-size: 13px; color:#666; margin-bottom: 12px;
}

/* Form inside modal */
.form-grid{
  display: grid;
  gap: 12px 16px;
  grid-template-columns: 1fr 1fr;
}
.field{ display:flex; flex-direction:column; gap:6px; }
.label{ font-size:13px; color:#666; }
.input{
  height: 36px;
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 10px;
  outline: none;
  transition: border-color .15s, box-shadow .15s;
  background: #fff;
}
.input:focus{
  border-color: #6aa1ff;
  box-shadow: 0 0 0 3px rgba(24,144,255,.15);
}
.actions{
  grid-column: 1 / -1;
  display:flex; gap:10px; align-items:center; margin-top: 4px;
}
.err{ color:#c00; font-size:13px; margin-top:4px; }

/* Loading spinner */
.spinner {
  display: inline-block;
  width: 10px; height: 10px;
  border-radius: 50%;
  margin-right: 8px;
  border: 2px solid transparent;
  border-top-color: rgba(255,255,255,.9);
  border-right-color: rgba(255,255,255,.9);
  animation: spin .6s linear infinite;
  vertical-align: -2px;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 560px){
  .form-grid{ grid-template-columns: 1fr; }
}
</style>
