import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  createReimbursementAPI,
  getReimbursementDetailAPI,
  getSubsidyCalendarAPI,
  saveSubsidyCalendarAPI,
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
import { validateBaseInfo } from './reimbursementDetailValidators'
import { useSubsidyManager } from './useSubsidyManager'
import { useTripEditor } from './useTripEditor'

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
    businessTypeSelectOptions,
    loadCommonSelectOptions,
    getBusinessTypeById
  } = useBusinessTypeOptions()
  
  const sectionState = reactive({
    base: true,
    trip: true,
    subsidy: true,
    summary: true,
    allocation: true,
    remark: true
  })
  
  const detailForm = reactive({
    id: '',
    billNo: '',
    billStatus: '0',
    creationTime: '',
    reimbursementTitle: '',
    businessTripReason: '',
    reimburserId: '',
    reimburserNo: '',
    reimburserName: '',
    reimDepartmentId: '',
    reimDepartmentNo: '',
    reimDepartmentName: '',
    reimCompanyId: '',
    reimCompanyNo: '',
    reimCompanyName: '',
    businessTypeId: '',
    businessTypeNo: '',
    businessTypeName: '',
    remarks: '',
    tripList: [],
    subsidyList: [],
    allocationList: []
  })
  
  const employeeMap = computed(() =>
    new Map(employeeOptions.value.map((item) => [item.reimburserId, item]))
  )
  
  const companyMap = computed(() =>
    new Map(companyOptions.value.map((item) => [item.id, item]))
  )
  
  const departmentMap = computed(() =>
    new Map(departmentOptions.value.map((item) => [item.id, item]))
  )
  
  const projectMap = computed(() =>
    new Map(projectOptions.value.map((item) => [item.projectId, item]))
  )
  
  const cityMap = computed(() => new Map(cityOptions.value.map((item) => [item.cityNo, item])))
  
  const isCreateMode = computed(() => !route.params.id)
  const pageMode = computed(() => route.query.mode || (isCreateMode.value ? 'edit' : 'view'))
  const isReadonly = computed(() => pageMode.value === 'view')
  const pageTitle = computed(() => '差旅费用报销单')
  const billDate = computed(() => {
    if (detailForm.creationTime) {
      return String(detailForm.creationTime).slice(0, 10)
    }
    return dayjs().format('YYYY-MM-DD')
  })
  
  const costSummary = computed(() => {
    const summary = detailForm.subsidyList.reduce(
      (acc, item) => {
        acc.subsidyTotal += Number(item.subsidyAmount || 0)
        acc.mealAllowance += Number(item.mealAllowance || 0)
        acc.transportationAllowance += Number(item.transportationAllowance || 0)
        acc.phoneAllowance += Number(item.phoneAllowance || 0)
        return acc
      },
      {
        subsidyTotal: 0,
        mealAllowance: 0,
        transportationAllowance: 0,
        phoneAllowance: 0
      }
    )
  
    return {
      subsidyTotal: toMoney(summary.subsidyTotal),
      mealAllowance: toMoney(summary.mealAllowance),
      transportationAllowance: toMoney(summary.transportationAllowance),
      phoneAllowance: toMoney(summary.phoneAllowance)
    }
  })
  
  const allocationTotalAmount = computed(() =>
    toMoney(detailForm.allocationList.reduce((sum, item) => sum + Number(item.allocationAmount || 0), 0))
  )
  
  const allocationTotalPercent = computed(() =>
    toPercent(detailForm.allocationList.reduce((sum, item) => sum + Number(item.allocationRatioPercent || 0), 0))
  )

  const {
    createAllocationRow,
    ensureDefaultAllocationRow,
    normalizeAllocationRows,
    updateAllocationCompany,
    updateAllocationProject,
    handleAllocationRatioInput,
    handleAddAllocationRow,
    handleDeleteAllocationRow,
    handleEqualAllocation,
    buildAllocationPayload,
    validateAllocationRows
  } = useAllocationManager({
    detailForm,
    costSummary,
    companyMap,
    projectMap,
    toMoney,
    ElMessage,
    ElMessageBox
  })

  const {
    subsidyDialogVisible,
    subsidyDialogLoading,
    subsidyDialogSaving,
    subsidyCalendarList,
    subsidyDialogMeta,
    createSubsidyRowFromCalendar,
    rebuildSubsidyList,
    openSubsidyDialog,
    saveSubsidyDialog
  } = useSubsidyManager({
    detailForm,
    cityMap,
    isReadonly,
    normalizeAllocationRows,
    toMoney,
    getSubsidyCalendarAPI,
    saveSubsidyCalendarAPI,
    ElMessage
  })

  const {
    tripDialogVisible,
    tripDialogMode,
    tripDialogForm,
    openCreateTripDialog,
    openEditTripDialog,
    openCopyTripDialog,
    handleTripDialogSubmit,
    handleDeleteTrip,
    buildTripPayloadList
  } = useTripEditor({
    detailForm,
    isReadonly,
    employeeMap,
    cityMap,
    parseDateRange,
    rebuildSubsidyList,
    ElMessage,
    ElMessageBox
  })
  
  watch(
    () => detailForm.reimburserId,
    (value) => {
      const matched = employeeMap.value.get(value)
      if (!matched) return
      detailForm.reimburserNo = matched.reimburserNo
      detailForm.reimburserName = matched.reimburserName
    }
  )
  
  watch(
    () => detailForm.reimDepartmentId,
    (value) => {
      const matched = departmentMap.value.get(value)
      if (!matched) return
      detailForm.reimDepartmentNo = matched.no
      detailForm.reimDepartmentName = matched.name
    }
  )
  
  watch(
    () => detailForm.reimCompanyId,
    (value) => {
      const matched = companyMap.value.get(value)
      if (!matched) return
      detailForm.reimCompanyNo = matched.no
      detailForm.reimCompanyName = matched.name
  
      if (detailForm.allocationList.length > 0 && !detailForm.allocationList[0].reimCompanyId) {
        updateAllocationCompany(0, value)
      }
    }
  )
  
  watch(
    () => detailForm.businessTypeId,
    (value) => {
      const matched = getBusinessTypeById(value)
      if (!matched) return
      detailForm.businessTypeNo = matched.businessTypeNo
      detailForm.businessTypeName = matched.businessTypeName
    }
  )
  
  function toggleSection(key) {
    sectionState[key] = !sectionState[key]
  }
  
  function toMoney(value) {
    return Number(value || 0).toFixed(2)
  }
  
  function toPercent(value) {
    return Number(value || 0).toFixed(2)
  }
  
  function parseDateRange(range) {
    return {
      departureDate: range?.[0] || '',
      arrivalDate: range?.[1] || ''
    }
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

  async function clearRemarksWithConfirm() {
    if (isReadonly.value || !detailForm.remarks) return
  
    try {
      await ElMessageBox.confirm('确定删除当前备注内容吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
      detailForm.remarks = ''
    } catch {}
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
        const payload = buildSubmitPayload()
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
  
    const errorMessage = validateDetailForm()
    if (errorMessage) {
      ElMessage.warning(errorMessage)
      return
    }
  
    submitLoading.value = true
  
    try {
      const payload = buildSubmitPayload()
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
  
  function fillFormFromDetail(detail) {
    detailForm.id = detail.id || ''
    detailForm.billNo = detail.billNo || ''
    detailForm.billStatus = detail.billStatus || '0'
    detailForm.creationTime = detail.creationTime || ''
    detailForm.reimbursementTitle = detail.reimbursementTitle || ''
    detailForm.businessTripReason = detail.businessTripReason || ''
    detailForm.reimburserId = detail.reimburserId || ''
    detailForm.reimburserNo = detail.reimburserNo || ''
    detailForm.reimburserName = detail.reimburserName || ''
    detailForm.reimDepartmentId = detail.reimDepartmentId || ''
    detailForm.reimDepartmentNo = detail.reimDepartmentNo || ''
    detailForm.reimDepartmentName = detail.reimDepartmentName || ''
    detailForm.reimCompanyId = detail.reimCompanyId || ''
    detailForm.reimCompanyNo = detail.reimCompanyNo || ''
    detailForm.reimCompanyName = detail.reimCompanyName || ''
    detailForm.businessTypeId = detail.businessTypeId || ''
    detailForm.businessTypeNo = detail.businessTypeNo || ''
    detailForm.businessTypeName = detail.businessTypeName || ''
    detailForm.remarks = detail.remarks || ''
  
    detailForm.tripList = (detail.tripList || []).map((item) => {
      const [departureDate, arrivalDate] = String(item.tripDateRange || '').split('至').map((part) => part.trim())
      const [departureCityName, arrivalCityName] = String(item.tripRoute || '').split('-')
      const departureCity = cityOptions.value.find((option) => option.cityName === departureCityName)
      const arrivalCity = cityOptions.value.find((option) => option.cityName === arrivalCityName)
      const traveler = employeeOptions.value.find((option) => option.reimburserId === item.travelerId)
  
      return {
        tripId: item.tripId,
        subsidyId: item.subsidyId,
        travelerId: item.travelerId,
        travelerNo: traveler?.reimburserNo || '',
        travelerName: item.travelerName,
        departureCityNo: departureCity?.cityNo || '',
        departureCityName: departureCityName || '',
        arrivalCityNo: arrivalCity?.cityNo || '',
        arrivalCityName: arrivalCityName || '',
        departureDate: departureDate || '',
        arrivalDate: arrivalDate || '',
        tripDescription: item.tripDescription || ''
      }
    })
  
    detailForm.subsidyList = (detail.subsidyList || []).map((item) => ({ ...item }))
    detailForm.allocationList = (detail.allocationList || []).map((item) =>
      createAllocationRow({
        allocationId: item.allocationId,
        reimCompanyId: item.reimCompanyId,
        reimCompanyNo: item.reimCompanyNo,
        reimCompanyName: item.reimCompanyName,
        projectId: item.projectId,
        projectNo: item.projectNo,
        projectName: item.projectName,
        allocationRatioPercent: Number(item.allocationRatio || 0) * 100,
        allocationAmount: Number(item.allocationAmount || 0)
      })
    )
  
    ensureDefaultAllocationRow()
    if (!detailForm.subsidyList.length && detailForm.tripList.length) {
      rebuildSubsidyList()
    } else {
      normalizeAllocationRows()
    }
  }
  
  function initCreateForm() {
    detailForm.creationTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
    detailForm.tripList = []
    detailForm.subsidyList = []
    detailForm.allocationList = []
    ensureDefaultAllocationRow()
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
      initCreateForm()
      return
    }
  
    loading.value = true
  
    try {
      const res = await getReimbursementDetailAPI(route.params.id)
      fillFormFromDetail(res?.data || {})
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
  
  function validateDetailForm() {
    const baseError = validateBaseInfo(detailForm)
    if (baseError) return baseError

    if (!detailForm.tripList.length) return '请至少补录一条行程'

    return validateAllocationRows()
  }
  
  function buildSubmitPayload() {
    return {
      reimbursementTitle: detailForm.reimbursementTitle,
      businessTripReason: detailForm.businessTripReason,
      reimburserId: detailForm.reimburserId,
      reimburserNo: detailForm.reimburserNo,
      reimburserName: detailForm.reimburserName,
      reimDepartmentId: detailForm.reimDepartmentId,
      reimDepartmentNo: detailForm.reimDepartmentNo,
      reimDepartmentName: detailForm.reimDepartmentName,
      reimCompanyId: detailForm.reimCompanyId,
      reimCompanyNo: detailForm.reimCompanyNo,
      reimCompanyName: detailForm.reimCompanyName,
      businessTypeId: detailForm.businessTypeId,
      businessTypeNo: detailForm.businessTypeNo,
      businessTypeName: detailForm.businessTypeName,
      tripList: buildTripPayloadList(),
      allocationList: buildAllocationPayload(),
      remarks: detailForm.remarks
    }
  }
  
  async function handleSubmit() {
    if (isReadonly.value) {
      router.push('/reimburse/list')
      return
    }
  
    const errorMessage = validateDetailForm()
    if (errorMessage) {
      ElMessage.warning(errorMessage)
      return
    }
  
    submitLoading.value = true
  
    try {
      const payload = buildSubmitPayload()
      let reimbursementId = detailForm.id
  
      if (isCreateMode.value) {
        const createData = unwrapApiResult(
          await createReimbursementAPI(payload),
          '保存失败',
          { requireId: true }
        )
        reimbursementId = createData.id
      } else {
        unwrapApiResult(await updateReimbursementAPI(reimbursementId, payload), '保存失败')
      }
  
      triggerReimbursementListRefresh()
      ElMessage.success('保存成功')
      router.push('/reimburse/list')
    } catch (error) {
      console.error('保存失败', error)
      ElMessage.error(error?.message || error?.response?.data?.message || '保存失败')
    } finally {
      submitLoading.value = false
    }
  }
  
  function handleClose() {
    router.push('/reimburse/list')
  }
  
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
    tripDialogVisible,
    tripDialogMode,
    companyOptions,
    departmentOptions,
    employeeOptions,
    businessTypeSelectOptions,
    cityOptions,
    projectOptions,
    sectionState,
    detailForm,
    tripDialogForm,
    isReadonly,
    pageTitle,
    billDate,
    costSummary,
    allocationTotalAmount,
    allocationTotalPercent,
    toggleSection,
    updateAllocationCompany,
    updateAllocationProject,
    toMoney,
    handleAllocationRatioInput,
    handleAddAllocationRow,
    handleDeleteAllocationRow,
    handleEqualAllocation,
    openCreateTripDialog,
    openEditTripDialog,
    openCopyTripDialog,
    handleTripDialogSubmit,
    handleDeleteTrip,
    openSubsidyDialog,
    clearRemarksWithConfirm,
    confirmClose,
    submitAndComplete
  }
}
