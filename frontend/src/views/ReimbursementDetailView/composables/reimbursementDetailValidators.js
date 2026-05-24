import dayjs from 'dayjs'

export function validateBaseInfo(detailForm) {
  if (detailForm.reimbursementTitle.length > 500) return '报销标题不能超过 500 字'
  if (detailForm.businessTripReason.length > 500) return '出差事由不能超过 500 字'
  if (detailForm.remarks.length > 1000) return '备注不能超过 1000 字'
  if (!detailForm.reimbursementTitle.trim()) return '请输入报销标题'
  if (!detailForm.reimburserId) return '请选择报销人'
  if (!detailForm.reimDepartmentId) return '请选择报销部门'
  if (!detailForm.reimCompanyId) return '请选择费用归属公司'
  if (!detailForm.businessTypeId) return '请选择业务类型'
  if (!detailForm.businessTripReason.trim()) return '请输入出差事由'

  return ''
}

export function validateTripForm(trip) {
  if (!trip.travelerId) return '请选择出行人'
  if (!trip.departureCityNo) return '请选择出发城市'
  if (!trip.arrivalCityNo) return '请选择到达城市'
  if (!trip.departureDate || !trip.arrivalDate) return '请选择出差日期'
  if (!trip.tripDescription.trim()) return '请输入行程说明'
  if (dayjs(trip.arrivalDate).isBefore(dayjs(trip.departureDate))) return '到达日期不能早于出发日期'
  if (dayjs(trip.arrivalDate).isAfter(dayjs())) return '到达日期不能晚于当前日期'

  return ''
}

export function validateTripDuplicate(detailTripList, trip, editingIndex, mode) {
  const duplicated = detailTripList.some((item, index) => {
    if (mode === 'edit' && index === editingIndex) {
      return false
    }

    if (item.travelerId !== trip.travelerId) {
      return false
    }

    const start = dayjs(item.departureDate)
    const end = dayjs(item.arrivalDate)
    const newStart = dayjs(trip.departureDate)
    const newEnd = dayjs(trip.arrivalDate)
    return !newStart.isAfter(end) && !newEnd.isBefore(start)
  })

  if (duplicated) return '同一出行人的行程日期不能重复或重叠'
  return ''
}

export function validateSubsidyCalendarRow(row) {
  const validations = [
    ['mealSelected', 'mealAmount', 'mealStandard', '餐补'],
    ['transportSelected', 'transportAmount', 'transportStandard', '交通补'],
    ['phoneSelected', 'phoneAmount', 'phoneStandard', '通讯补']
  ]

  for (const [selectedKey, amountKey, standardKey, label] of validations) {
    if (!row[selectedKey]) continue

    const amount = Number(row[amountKey] || 0)
    const standard = Number(row[standardKey] || 0)

    if (amount < 0) return `${label}金额不能小于 0`
    if (amount > standard) return `${label}金额不能大于标准金额`
  }

  return ''
}

export function validateSubsidyCalendarList(calendarList) {
  for (const row of calendarList) {
    const errorMessage = validateSubsidyCalendarRow(row)
    if (errorMessage) return errorMessage
  }

  return ''
}

export function validateAllocationRows(allocationList, subsidyTotal) {
  const ratioTotal = allocationList.reduce(
    (sum, item) => sum + Number(item.allocationRatioPercent || 0),
    0
  )

  if (Math.abs(ratioTotal - 100) > 0.01) return '分摊比例合计必须等于 100%'

  const amountTotal = allocationList.reduce(
    (sum, item) => sum + Number(item.allocationAmount || 0),
    0
  )

  if (Math.abs(amountTotal - Number(subsidyTotal || 0)) > 0.01) {
    return '分摊金额合计必须等于补助总金额'
  }

  const invalidRow = allocationList.find(
    (item) => !item.reimCompanyId || !item.projectId
  )

  if (invalidRow) return '请补充分摊信息中的费用归属和项目'
  return ''
}
