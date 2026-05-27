//通用工具函数和常量，
import dayjs from 'dayjs'

export const BUSINESS_TYPE_CASCADER_PROPS = {
  value: 'businessTypeId',
  label: 'businessTypeName',
  children: 'children',
  emitPath: false,
  checkStrictly: false
}

const WEEK_NAME_MAP = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

export function toMoney(value) {
  return Number(value || 0).toFixed(2)
}

export function toPercent(value) {
  return Number(value || 0).toFixed(2)
}

export function parseDateRange(range) {
  return {
    departureDate: range?.[0] || '',
    arrivalDate: range?.[1] || ''
  }
}

export function getTripDays(start, end) {
  return dayjs(end).diff(dayjs(start), 'day') + 1
}

export function getWeekName(date) {
  return WEEK_NAME_MAP[dayjs(date).day()] || ''
}

export function getMealStandardByCityType(cityType) {
  if (cityType === '1') return 100
  if (cityType === '2') return 80
  return 50
}

export function clampSubsidyAmount(value, maxValue) {
  const numericValue = Number(value || 0)
  if (!Number.isFinite(numericValue) || numericValue < 0) return 0
  return Number(Math.min(numericValue, Number(maxValue || 0)).toFixed(2))
}

export function syncReimburserInfo(detailForm, employeeMap, reimburserId) {
  const matched = employeeMap.get(reimburserId)
  if (!matched) return
  detailForm.reimburserNo = matched.reimburserNo
  detailForm.reimburserName = matched.reimburserName
}

export function syncDepartmentInfo(detailForm, departmentMap, departmentId) {
  const matched = departmentMap.get(departmentId)
  if (!matched) return
  detailForm.reimDepartmentNo = matched.no
  detailForm.reimDepartmentName = matched.name
}

export function syncCompanyInfo(detailForm, companyMap, companyId, updateAllocationCompany) {
  const matched = companyMap.get(companyId)
  if (!matched) return

  detailForm.reimCompanyNo = matched.no
  detailForm.reimCompanyName = matched.name

  if (detailForm.allocationList.length > 0 && !detailForm.allocationList[0].reimCompanyId) {
    updateAllocationCompany(0, companyId)
  }
}

export function syncBusinessTypeInfo(detailForm, getBusinessTypeById, businessTypeId) {
  const matched = getBusinessTypeById(businessTypeId)
  if (!matched) return
  detailForm.businessTypeNo = matched.businessTypeNo
  detailForm.businessTypeName = matched.businessTypeName
}
