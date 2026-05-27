<script setup>
import { onActivated, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReimbursementListAPI } from '@/apis/reimbursement'
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

const tableData = ref([])
const total = ref(0)
const latestRefreshToken = ref(0)

const {
  companyOptions,
  departmentOptions,
  employeeOptions,
  businessTypeSelectOptions,
  loadCommonSelectOptions
} = useBusinessTypeOptions()

//获取列表数据
const getList = async () => {
  try {
    const res = await getReimbursementListAPI(queryForm)
    tableData.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
    queryForm.current = Number(res?.data?.current || 1)
    queryForm.size = Number(res?.data?.size || 10)
  } catch (error) {
    console.error('获取报销单列表失败', error)
    ElMessage.error('获取报销单列表失败')
  }
}
 
//获取筛选项
const getSelectOptions = async () => {
  try {
    await loadCommonSelectOptions()
  } catch (error) {
    console.error('获取筛选项失败', error)
    ElMessage.warning('筛选项加载失败，但列表仍可使用')
  }
}
//搜索和重置
const handleSearch = () => {
  queryForm.current = 1
  getList()
}
const handleReset = () => {
  Object.assign(queryForm, createDefaultQueryForm())
  getList()
}
//跳转到新增页
const goToCreatePage = () => {
  router.push('/reimburse/detail')
}
//跳转到详情页
const goToDetailPage = (row, mode = 'view') => {
  router.push({
    path: `/reimburse/detail/${row.id}`,
    query: { mode }
  })
}
//新增、查看、编辑
const handleAdd = () => {
  goToCreatePage()
}

const handleView = (row) => {
  goToDetailPage(row, 'view')
}

const handleEdit = (row) => {
  goToDetailPage(row, 'edit')
}
//分页处理 页码和页面尺寸改变
const handleCurrentChange = (page) => {
  queryForm.current = page
  getList()
}

const handleSizeChange = (size) => {
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
      @search="handleSearch"
      @reset="handleReset"
      @add="handleAdd"
    />

    <ListTablePanel
      :table-data="tableData"
      :current="queryForm.current"
      :size="queryForm.size"
      :total="total"
      @view="handleView"
      @edit="handleEdit"
      @refresh="getList"
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
