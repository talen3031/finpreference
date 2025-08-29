<template>
  <div class="container">
    <h2 class="page-title">喜好清單</h2>

    <!-- 新增表單 -->
    <form @submit.prevent="addLike" class="card form-card">
      <div class="field">
        <label class="label">選擇商品</label>
        <select v-model="form.productNo" required class="select">
          <option :value="null" disabled>選商品</option>
          <option v-for="p in products" :key="p.no" :value="p.no">
            {{ p.productName }} (${{ money(p.price) }} @{{ percent(p.feeRate) }})
          </option>
        </select>
      </div>

      <div class="field">
        <label class="label">扣款帳號</label>
        <input v-model="form.account" placeholder="扣款帳號" required class="input">
      </div>

      <div class="field">
        <label class="label">數量</label>
        <input v-model.number="form.quantity" type="number" min="1" placeholder="數量" required class="input">
      </div>

      <div class="actions">
        <button :disabled="busyAdd" class="btn primary">
          <span v-if="busyAdd" class="spinner"></span>{{ busyAdd ? '處理中...' : '新增喜好' }}
        </button>
      </div>

      <!-- 錯誤訊息（新增） -->
      <p v-if="err" class="helper-text error">{{ err }}</p>
    </form>

    <!-- 清單 -->
    <div class="card table-card">
      <div class="table-scroll">
        <table class="table">
          <thead>
            <tr>
              <th>商品</th>
              <th class="th-num">價格</th>
              <th class="th-num">手續費率</th>
              <th class="th-num">單筆手續費</th>
              <th class="th-num">數量</th>
              <th>扣款帳號</th>
              <th class="th-num">總金額</th>
              <th class="th-num">總手續費</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="x in likes" :key="x.sn">
              <td class="cell-ellipsis" :title="x.product_name">{{ x.product_name }}</td>
              <td class="num">${{ money(x.price) }}</td>
              <td class="num">{{ percent(x.feeRate) }}</td>
              <td class="num">{{ money(x.price * x.feeRate) }}</td>
              <td class="num">{{ x.quantity }}</td>
              <td class="cell-ellipsis" :title="x.account">{{ x.account }}</td>
              <td class="num">{{ money(x.total_amount) }}</td>
              <td class="num">{{ money(x.total_fee) }}</td>
              <td class="ops">
                <button @click="prefill(x)" :disabled="busyRow[x.sn]" class="btn">
                  更改
                </button>
                <button @click="remove(x)" :disabled="busyRow[x.sn]" class="btn danger">
                  刪除
                </button>
              </td>
            </tr>
            <tr v-if="!likes.length">
              <td colspan="9" class="empty">目前沒有資料</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 編輯區 -->
    <div v-if="editing" class="card edit-card">
      <h3 class="section-title">更改 SN {{ editForm.sn }}</h3>
      <form @submit.prevent="updateLike" class="edit-grid">
        <div class="field">
          <label class="label">扣款帳號：</label>
          <input v-model="editForm.account" required class="input">
        </div>

        <div class="field">
          <label class="label">數量：</label>
          <input v-model.number="editForm.quantity" type="number" min="1" required class="input">
        </div>

        <div class="actions">
          <button :disabled="busyUpdate" class="btn primary">
            <span v-if="busyUpdate" class="spinner"></span>{{ busyUpdate ? '儲存中...' : '儲存' }}
          </button>
          <button @click.prevent="cancelEdit" :disabled="busyUpdate" class="btn ghost">取消</button>
        </div>

        <!-- 錯誤訊息（編輯） -->
        <p v-if="editErr" class="helper-text error">{{ editErr }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Likes, Products } from '../api'

const products = ref([])
const likes = ref([])

// 新增
const form = ref({ productNo: null, account: '', quantity: 1 })
const err = ref('')
const busyAdd = ref(false)

// 編輯
const editing = ref(false)
const editForm = ref({ sn: null, account: '', quantity: 1 })
const editErr = ref('')
const busyUpdate = ref(false)

// 每列忙碌狀態（刪除/更改按鈕防重複）
const busyRow = ref({})

function money(n) {
  const v = Number(n ?? 0)
  return v.toFixed(2)
}
function percent(n) {
  const v = Number(n ?? 0) * 100
  return v.toFixed(2) + '%'
}

async function load() {
  try {
    const [plist, llist] = await Promise.all([Products.list(), Likes.list()])
    products.value = Array.isArray(plist) ? plist : []

    likes.value = (Array.isArray(llist) ? llist : []).map(l => {
      // 用 product_name 去 products 找對應
      const p = products.value.find(pp => pp.productName === l.product_name)

      return {
        ...l,
        price: p?.price ?? 0,
        feeRate: p?.feeRate ?? 0,
        total_amount: (p?.price ?? 0) * (l.quantity ?? 0),
        total_fee: ((p?.price ?? 0) * (l.quantity ?? 0)) * (p?.feeRate ?? 0)
      }
    })
  } catch (e) {
    console.error(e)
  }
}

async function addLike() {
  err.value = ''
  if (form.value.productNo == null) {
    err.value = '請選擇商品'
    return
  }
  busyAdd.value = true
  try {
    const payload = {
      productNo: Number(form.value.productNo),
      account: form.value.account,
      quantity: Number(form.value.quantity)
    }
    await Likes.create(payload)
    form.value = { productNo: null, account: '', quantity: 1 }
    await load()
  } catch (e) {
    err.value =
      e?.response?.data?.error ||
      e?.responseBody?.error ||
      e?.message ||
      '新增失敗'
  } finally {
    busyAdd.value = false
  }
}

function prefill(x) {
  editing.value = true
  editErr.value = ''
  editForm.value = { sn: x.sn, account: x.account, quantity: x.quantity }
}

function cancelEdit() {
  editing.value = false
  editErr.value = ''
  editForm.value = { sn: null, account: '', quantity: 1 }
}

async function updateLike() {
  editErr.value = ''
  busyUpdate.value = true
  try {
    await Likes.update(editForm.value.sn, {
      account: editForm.value.account,
      quantity: Number(editForm.value.quantity)
    })
    cancelEdit()
    await load()
  } catch (e) {
    editErr.value =
      e?.response?.data?.error ||
      e?.responseBody?.error ||
      e?.message ||
      '更新失敗'
  } finally {
    busyUpdate.value = false
  }
}

async function remove(row) {
  if (!confirm(`確定刪除 ${row.product_name} ?`)) return
  busyRow.value = { ...busyRow.value, [row.sn]: true }
  try {
    await Likes.remove(row.sn)
    await load()
  } catch (e) {
    alert(
      e?.response?.data?.error ||
      e?.responseBody?.error ||
      e?.message ||
      '刪除失敗'
    )
  } finally {
    const { [row.sn]: _, ...rest } = busyRow.value
    busyRow.value = rest
  }
}

onMounted(load)
</script>

<style scoped>
/* Layout */
.container {
  max-width: 980px;
  margin: 24px auto;
  padding: 0 16px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin: 8px 0 16px;
  letter-spacing: .5px;
}

/* Card */
.card {
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 4px 14px rgba(0,0,0,.04);
}

/* Form (新增) */
.form-card {
  display: grid;
  gap: 12px 16px;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  align-items: end;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.label {
  font-size: 13px;
  color: #666;
}
.input, .select {
  height: 36px;
  padding: 6px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 10px;
  outline: none;
  transition: border-color .15s, box-shadow .15s;
  background: #fff;
}
.input:focus, .select:focus {
  border-color: #6aa1ff;
  box-shadow: 0 0 0 3px rgba(24,144,255,.15);
}
.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

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
.btn:disabled {
  opacity: .6;
  cursor: not-allowed;
}
.btn.primary {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
}
.btn.primary:hover { background: #3b8cff; border-color: #3b8cff; }
.btn.danger {
  border-color: #ffa39e;
  background: #fff1f0;
  color: #cf1322;
}
.btn.danger:hover { background: #ffe1df; }
.btn.ghost {
  background: transparent;
  border-color: #d9d9d9;
}

/* Loading dot */
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

/* Table */
.table-card { margin-top: 16px; padding: 0; }
.table-scroll { overflow: auto; border-radius: 14px; }
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 760px;
}
.table thead th {
  position: sticky;
  top: 0;
  background: #f7f9fc;
  color: #555;
  text-align: left;
  font-weight: 700;
  padding: 12px;
  border-bottom: 1px solid #e8e8e8;
}
.table tbody td {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}
.table tbody tr:hover td {
  background: #fafcff;
}
.th-num, .num { text-align: right; font-variant-numeric: tabular-nums; }
.ops {
  display: flex;
  gap: 8px;
  align-items: center;
}
.empty {
  text-align: center;
  color: #888;
  padding: 18px;
}

/* 編輯區 */
.edit-card { margin-top: 16px; }
.section-title {
  font-size: 16px; font-weight: 700; margin-bottom: 12px;
}
.edit-grid {
  display: grid;
  gap: 12px 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  align-items: end;
}

/* Helpers */
.helper-text {
  margin: 4px 0 0;
  font-size: 13px;
}
.helper-text.error {
  color: #c00;
}
.cell-ellipsis {
  max-width: 280px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* Responsive tweaks */
@media (max-width: 640px) {
  .cell-ellipsis { max-width: 180px; }
}
</style>
