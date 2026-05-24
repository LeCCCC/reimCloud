<script setup>
import { onActivated, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReimbursementListAPI, voidReimbursementAPI } from '@/apis/reimbursement'
import { useBusinessTypeOptions } from '@/composables/useReimbursementSelectOptions'
import { reimbursementViewState } from '@/store/reimbursement'
import ListFilterPanel from './components/ListFilterPanel.vue'
import ListTablePanel from './components/ListTablePanel.vue'

const router = useRouter()

const createDefaultQueryForm = () => ({
  current: 1,
  size: 10,
  billNo: '',
  reimbursementTitle: '',
  businessTripReason: '',
  reimCompanyId: '',
  reimDepartmentId: '',
  reimburserId: '',
  businessTypeId: '',
  billStatus: ''
})

const queryForm = reactive(createDefaultQueryForm())

const billStatusOptions = [
  { label: '草稿', value: '0' },
  { label: '已完成', value: '1' },
  { label: '已作废', value: '2' }
]

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const latestRefreshToken = ref(0)

const {
  companyOptions,
  departmentOptions,
  employeeOptions,
  businessTypeSelectOptions,
  loadCommonSelectOptions
} = useBusinessTypeOptions()

async function getList() {
  loading.value = true

  try {
    const res = await getReimbursementListAPI(queryForm)
    tableData.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
    queryForm.current = Number(res?.data?.current || 1)
    queryForm.size = Number(res?.data?.size || 10)
  } catch (error) {
    console.error('获取报销单列表失败', error)
    ElMessage.error('获取报销单列表失败')
  } finally {
    loading.value = false
  }
}

async function getSelectOptions() {
  try {
    await loadCommonSelectOptions()
  } catch (error) {
    console.error('获取筛选项失败', error)
    ElMessage.warning('筛选项加载失败，但列表仍可使用')
  }
}

function handleSearch() {
  queryForm.current = 1
  getList()
}
function handleReset() {
  Object.assign(queryForm, createDefaultQueryForm())
  getList()
}

function goToCreatePage() {
  router.push('/reimburse/detail')
}

function goToDetailPage(row, mode = 'view') {
  router.push({
    path: `/reimburse/detail/${row.id}`,
    query: { mode }
  })
}

function handleAdd() {
  goToCreatePage()
}

function handleView(row) {
  goToDetailPage(row, 'view')
}

function handleEdit(row) {
  if (row.billStatus === '1') {
    ElMessage.warning('已完成单据不允许编辑')
    return
  }

  goToDetailPage(row, 'edit')
}

async function handleVoid(row) {
  try {
    await ElMessageBox.confirm(`确认作废报销单 ${row.billNo} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })

    await voidReimbursementAPI(row.id)
    ElMessage.success('作废成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('作废失败', error)
      ElMessage.error(error?.response?.data?.message || '作废失败')
    }
  }
}

async function copyBillNo(row) {
  try {
    await navigator.clipboard.writeText(row.billNo || '')
    ElMessage.success('报销单号已复制')
  } catch (error) {
    console.error('复制失败', error)
    ElMessage.warning('复制失败，请手动复制')
  }
}

function handleMoreCommand(command, row) {
  if (command === 'copy') {
    copyBillNo(row)
    return
  }

  if (command === 'void') {
    handleVoid(row)
  }
}

function handleCurrentChange(page) {
  queryForm.current = page
  getList()
}

function handleSizeChange(size) {
  queryForm.size = size
  queryForm.current = 1
  getList()
}

onMounted(async () => {
  await getSelectOptions()
  await getList()
  latestRefreshToken.value = reimbursementViewState.listRefreshToken
})

onActivated(() => {
  if (latestRefreshToken.value !== reimbursementViewState.listRefreshToken) {
    latestRefreshToken.value = reimbursementViewState.listRefreshToken
    getList()
  }
})
</script>

<template>
  <div class="reimbursement-list-page">
    <ListFilterPanel
      :model-value="queryForm"
      :company-options="companyOptions"
      :department-options="departmentOptions"
      :employee-options="employeeOptions"
      :business-type-options="businessTypeSelectOptions"
      :bill-status-options="billStatusOptions"
      @search="handleSearch"
      @reset="handleReset"
      @add="handleAdd"
    />

    <ListTablePanel
      :table-data="tableData"
      :loading="loading"
      :current="queryForm.current"
      :size="queryForm.size"
      :total="total"
      @view="handleView"
      @edit="handleEdit"
      @more-command="handleMoreCommand"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<style scoped>
.reimbursement-list-page {
  min-height: 100vh;
  padding: 18px 24px 24px;
  background:
    radial-gradient(circle at top left, rgba(78, 136, 255, 0.08), transparent 28%),
    linear-gradient(180deg, #f7faff 0%, #f2f5fb 100%);
  box-sizing: border-box;
}

@media (max-width: 1080px) {
  .reimbursement-list-page {
    padding: 14px;
  }
}
</style>
