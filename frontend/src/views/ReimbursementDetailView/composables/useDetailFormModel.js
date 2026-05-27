// detailForm、汇总金额、详情回填编排
import { computed, reactive, watch } from 'vue'
import dayjs from 'dayjs'
import { validateBaseInfo } from './reimbursementDetailValidators'
import {
  syncBusinessTypeInfo,
  syncCompanyInfo,
  syncDepartmentInfo,
  syncReimburserInfo,
  toMoney,
  toPercent
} from './reimbursementDetailShared'
import {
  assignBaseDetailForm,
  mapAllocationListFromDetail,
  mapTripListFromDetail
} from './reimbursementDetailMappers'

export function useDetailFormModel({
  employeeOptions,
  companyOptions,
  departmentOptions,
  cityOptions,
  getBusinessTypeById,
  updateAllocationCompany
}) {
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

  watch(
    () => detailForm.reimburserId,
    (value) => {
      syncReimburserInfo(detailForm, employeeMap.value, value)
    }
  )

  watch(
    () => detailForm.reimDepartmentId,
    (value) => {
      syncDepartmentInfo(detailForm, departmentMap.value, value)
    }
  )

  watch(
    () => detailForm.reimCompanyId,
    (value) => {
      syncCompanyInfo(detailForm, companyMap.value, value, updateAllocationCompany)
    }
  )

  watch(
    () => detailForm.businessTypeId,
    (value) => {
      syncBusinessTypeInfo(detailForm, getBusinessTypeById, value)
    }
  )

  function fillFormFromDetail(detail, {
    createAllocationRow,
    ensureDefaultAllocationRow,
    normalizeAllocationRows,
    rebuildSubsidyList
  }) {
    assignBaseDetailForm(detailForm, detail)
    detailForm.tripList = mapTripListFromDetail(detail.tripList || [], {
      cityOptions: cityOptions.value,
      employeeOptions: employeeOptions.value
    })
    detailForm.subsidyList = (detail.subsidyList || []).map((item) => ({ ...item }))
    detailForm.allocationList = mapAllocationListFromDetail(
      detail.allocationList || [],
      createAllocationRow
    )

    ensureDefaultAllocationRow()
    if (!detailForm.subsidyList.length && detailForm.tripList.length) {
      rebuildSubsidyList()
      return
    }

    normalizeAllocationRows()
  }

  function initCreateForm(ensureDefaultAllocationRow) {
    detailForm.creationTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
    detailForm.tripList = []
    detailForm.subsidyList = []
    detailForm.allocationList = []
    ensureDefaultAllocationRow()
  }

  function validateDetailForm(validateAllocationRows) {
    const baseError = validateBaseInfo(detailForm)
    if (baseError) return baseError

    if (!detailForm.tripList.length) return '请至少补录一条行程'

    return validateAllocationRows()
  }

  return {
    detailForm,
    billDate,
    costSummary,
    allocationTotalAmount,
    allocationTotalPercent,
    fillFormFromDetail,
    initCreateForm,
    validateDetailForm
  }
}
