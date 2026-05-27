//页面级流程
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createReimbursementAPI,
  getReimbursementDetailAPI,
  submitReimbursementAPI,
  updateReimbursementAPI
} from '@/apis/reimbursement'
import {
  getCityOptionsAPI,
  getProjectOptionsAPI
} from '@/apis/selectOptions'
import { useBusinessTypeOptions } from '@/composables/useReimbursementSelectOptions'
import { triggerReimbursementListRefresh } from '@/store/reimbursement'
import { useAllocationManager } from './useAllocationManager'
import { toMoney } from './reimbursementDetailShared'
import { useDetailFormModel } from './useDetailFormModel'
import { buildSubsidyListFromTrips } from './subsidyRows'
import { buildSubmitPayload } from './reimbursementDetailPayload'

export function useReimbursementDetail() {
  const router = useRouter()
  const route = useRoute()

  const loading = ref(false)
  const submitLoading = ref(false)

  const cityOptions = ref([])
  const projectOptions = ref([])
  const {
    companyOptions,
    departmentOptions,
    employeeOptions,
    businessTypeTreeOptions,
    loadCommonSelectOptions,
    getBusinessTypeById
  } = useBusinessTypeOptions()

  const sectionState = reactive({
    basic: true,
    trip: true,
    subsidy: true,
    summary: true,
    allocation: true,
    remark: true
  })

  const cityMap = computed(() =>
    new Map(cityOptions.value.map((item) => [item.cityNo, item]))
  )

  const companyMap = computed(() =>
    new Map(companyOptions.value.map((item) => [item.id, item]))
  )

  const isCreateMode = computed(() => !route.params.id)
  const pageMode = computed(() => route.query.mode || (isCreateMode.value ? 'edit' : 'view'))
  const isReadonly = computed(() => pageMode.value === 'view')
  const pageTitle = computed(() => '差旅费用报销单')

  const {
    detailForm,
    billDate,
    costSummary,
    allocationTotalAmount,
    allocationTotalPercent,
    fillFormFromDetail,
    initCreateForm,
    validateDetailForm
  } = useDetailFormModel({
    employeeOptions,
    companyOptions,
    departmentOptions,
    cityOptions,
    getBusinessTypeById,
    updateAllocationCompany: (...args) => updateAllocationCompany(...args)
  })

  const {
    createAllocationRow,
    ensureDefaultAllocationRow,
    normalizeAllocationRows,
    updateAllocationCompany,
    buildAllocationPayload,
    validateAllocationRows
  } = useAllocationManager({
    detailForm,
    costSummary,
    companyMap
  })

  function rebuildSubsidyList() {
    detailForm.subsidyList = buildSubsidyListFromTrips(
      detailForm.tripList,
      cityMap.value,
      toMoney
    )
    normalizeAllocationRows()
  }

  function toggleSection(key) {
    sectionState[key] = !sectionState[key]
  }

  function unwrapApiResult(result, fallbackMessage, options = {}) {
    if (
      result &&
      typeof result === 'object' &&
      'code' in result &&
      !['0', 0, '200', 200].includes(result.code)
    ) {
      throw new Error(result.message || fallbackMessage)
    }

    const data = result?.data ?? result

    if (options.requireId && !data?.id) {
      throw new Error(fallbackMessage)
    }

    return data
  }

  async function confirmClose() {
    if (isReadonly.value) {
      router.push('/reimburse/list')
      return
    }

    try {
      await ElMessageBox.confirm('是否保存当前单据为草稿后再关闭？', '提示', {
        type: 'warning',
        confirmButtonText: '保存并关闭',
        cancelButtonText: '直接关闭',
        distinguishCancelAndClose: true
      })

      submitLoading.value = true

      try {
        const payload = buildSubmitPayload(detailForm, buildAllocationPayload())
        let reimbursementId = detailForm.id

        detailForm.billStatus = '0'

        if (isCreateMode.value) {
          const createData = unwrapApiResult(
            await createReimbursementAPI(payload),
            '草稿保存失败',
            { requireId: true }
          )
          reimbursementId = createData.id
          detailForm.id = reimbursementId || ''
        } else {
          unwrapApiResult(await updateReimbursementAPI(reimbursementId, payload), '草稿保存失败')
        }

        triggerReimbursementListRefresh()
        ElMessage.success('草稿保存成功')
        router.push('/reimburse/list')
      } catch (error) {
        console.error('草稿保存失败', error)
        ElMessage.error(error?.message || error?.response?.data?.message || '草稿保存失败')
      } finally {
        submitLoading.value = false
      }
    } catch (error) {
      if (error === 'cancel') {
        router.push('/reimburse/list')
      }
    }
  }

  async function submitAndComplete() {
    if (isReadonly.value) {
      router.push('/reimburse/list')
      return
    }

    const errorMessage = validateDetailForm(validateAllocationRows)
    if (errorMessage) {
      ElMessage.warning(errorMessage)
      return
    }

    submitLoading.value = true

    try {
      const payload = buildSubmitPayload(detailForm, buildAllocationPayload())
      let reimbursementId = detailForm.id

      if (isCreateMode.value) {
        const createData = unwrapApiResult(
          await createReimbursementAPI(payload),
          '提交失败',
          { requireId: true }
        )
        reimbursementId = createData.id
      } else {
        unwrapApiResult(await updateReimbursementAPI(reimbursementId, payload), '提交失败')
      }

      unwrapApiResult(await submitReimbursementAPI(reimbursementId), '提交失败')
      triggerReimbursementListRefresh()
      await ElMessageBox.alert('提交成功', '提示', {
        confirmButtonText: '确定'
      })
      router.push('/reimburse/list')
    } catch (error) {
      console.error('提交失败', error)
      ElMessage.error(error?.message || error?.response?.data?.message || '提交失败')
    } finally {
      submitLoading.value = false
    }
  }

  async function loadSelectOptions() {
    const [cityRes, projectRes] = await Promise.all([
      getCityOptionsAPI(),
      getProjectOptionsAPI(),
      loadCommonSelectOptions()
    ])

    cityOptions.value = cityRes?.data || []
    projectOptions.value = projectRes?.data || []
  }

  async function loadDetail() {
    if (!route.params.id) {
      initCreateForm(ensureDefaultAllocationRow)
      return
    }

    loading.value = true

    try {
      const res = await getReimbursementDetailAPI(route.params.id)
      fillFormFromDetail(res?.data || {}, {
        createAllocationRow,
        ensureDefaultAllocationRow,
        normalizeAllocationRows,
        rebuildSubsidyList
      })
    } catch (error) {
      console.error('获取报销单详情失败', error)
      ElMessage.error('获取报销单详情失败')
    } finally {
      loading.value = false
    }
  }

  watch(
    () => detailForm.billStatus,
    (value) => {
      if (route.query.mode === 'edit' && value === '1') {
        ElMessage.warning('已完成单据不允许编辑')
        router.replace({
          path: route.path,
          query: {
            ...route.query,
            mode: 'view'
          }
        })
      }
    }
  )

  onMounted(async () => {
    try {
      await loadSelectOptions()
      await loadDetail()
    } catch (error) {
      console.error('页面初始化失败', error)
      ElMessage.error('页面初始化失败')
    }
  })

  return {
    loading,
    submitLoading,
    companyOptions,
    departmentOptions,
    employeeOptions,
    businessTypeTreeOptions,
    cityOptions,
    projectOptions,
    sectionState,
    detailForm,
    isReadonly,
    pageTitle,
    billDate,
    costSummary,
    allocationTotalAmount,
    allocationTotalPercent,
    toggleSection,
    normalizeAllocationRows,
    toMoney,
    rebuildSubsidyList,
    confirmClose,
    submitAndComplete
  }
}
