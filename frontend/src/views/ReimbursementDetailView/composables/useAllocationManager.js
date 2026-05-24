import { validateAllocationRows as validateAllocationRowsByRule } from './reimbursementDetailValidators'

export function useAllocationManager({
  detailForm,
  costSummary,
  companyMap,
  projectMap,
  toMoney,
  ElMessage,
  ElMessageBox
}) {
  function createAllocationRow(overrides = {}) {
    return {
      allocationId: overrides.allocationId || '',
      reimCompanyId: overrides.reimCompanyId || '',
      reimCompanyNo: overrides.reimCompanyNo || '',
      reimCompanyName: overrides.reimCompanyName || '',
      projectId: overrides.projectId || '',
      projectNo: overrides.projectNo || '',
      projectName: overrides.projectName || '',
      allocationRatioPercent: Number(overrides.allocationRatioPercent || 0),
      allocationAmount: Number(overrides.allocationAmount || 0)
    }
  }

  function normalizeAllocationRows() {
    const rows = detailForm.allocationList
    const total = Number(costSummary.value.subsidyTotal)

    if (rows.length === 0) {
      return
    }

    if (rows.length === 1) {
      rows[0].allocationRatioPercent = 100
      rows[0].allocationAmount = total
      return
    }

    const othersRatio = rows
      .slice(1)
      .reduce((sum, item) => sum + Number(item.allocationRatioPercent || 0), 0)

    const safeOthersRatio = Math.min(othersRatio, 100)
    rows[0].allocationRatioPercent = Math.max(0, Number((100 - safeOthersRatio).toFixed(2)))

    const baseAmount = Number((total / 100).toFixed(4))
    let otherAmountTotal = 0

    rows.slice(1).forEach((item) => {
      item.allocationAmount = Number((baseAmount * Number(item.allocationRatioPercent || 0)).toFixed(2))
      otherAmountTotal += Number(item.allocationAmount || 0)
    })

    rows[0].allocationAmount = Number((total - otherAmountTotal).toFixed(2))
  }

  function ensureDefaultAllocationRow() {
    if (detailForm.allocationList.length === 0) {
      detailForm.allocationList.push(
        createAllocationRow({
          reimCompanyId: detailForm.reimCompanyId,
          reimCompanyNo: detailForm.reimCompanyNo,
          reimCompanyName: detailForm.reimCompanyName
        })
      )
    }

    normalizeAllocationRows()
  }

  function updateAllocationCompany(index, value) {
    const matched = companyMap.value.get(value)
    const row = detailForm.allocationList[index]
    row.reimCompanyId = value
    row.reimCompanyNo = matched?.no || ''
    row.reimCompanyName = matched?.name || ''
  }

  function updateAllocationProject(index, value) {
    const matched = projectMap.value.get(value)
    const row = detailForm.allocationList[index]
    row.projectId = value
    row.projectNo = matched?.projectNo || ''
    row.projectName = matched?.projectName || ''
  }

  function handleAllocationRatioInput(index, value) {
    if (index === 0) return
    const currentValue = Number(value || 0)
    const others = detailForm.allocationList.filter((_, rowIndex) => rowIndex !== 0 && rowIndex !== index)
    const othersTotal = others.reduce((sum, item) => sum + Number(item.allocationRatioPercent || 0), 0)

    if (othersTotal + currentValue > 100) {
      detailForm.allocationList[index].allocationRatioPercent = null
      ElMessage.warning('除首行外的分摊比例合计不能超过 100%')
    } else {
      detailForm.allocationList[index].allocationRatioPercent = Number(currentValue.toFixed(2))
    }

    normalizeAllocationRows()
  }

  function handleAddAllocationRow() {
    detailForm.allocationList.push(createAllocationRow())
    normalizeAllocationRows()
  }

  async function handleDeleteAllocationRow(index) {
    if (detailForm.allocationList.length === 1) {
      ElMessage.warning('至少保留一条分摊信息')
      return
    }

    await ElMessageBox.confirm('确定删除吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    detailForm.allocationList.splice(index, 1)
    normalizeAllocationRows()
  }

  function handleEqualAllocation() {
    const rows = detailForm.allocationList
    const total = Number(costSummary.value.subsidyTotal)

    if (rows.length === 0) return

    const basePercent = Number((100 / rows.length).toFixed(2))
    const restPercent = Number((basePercent * (rows.length - 1)).toFixed(2))
    rows[0].allocationRatioPercent = Number((100 - restPercent).toFixed(2))

    for (let index = 1; index < rows.length; index += 1) {
      rows[index].allocationRatioPercent = basePercent
    }

    const averageAmount = Number((total / rows.length).toFixed(2))
    const restAmount = Number((averageAmount * (rows.length - 1)).toFixed(2))
    rows[0].allocationAmount = Number((total - restAmount).toFixed(2))

    for (let index = 1; index < rows.length; index += 1) {
      rows[index].allocationAmount = averageAmount
    }
  }

  function buildAllocationPayload() {
    return detailForm.allocationList.map((item) => ({
      allocationId: item.allocationId || undefined,
      reimCompanyId: item.reimCompanyId,
      reimCompanyNo: item.reimCompanyNo,
      reimCompanyName: item.reimCompanyName,
      projectId: item.projectId,
      projectNo: item.projectNo,
      projectName: item.projectName,
      allocationRatio: Number((Number(item.allocationRatioPercent || 0) / 100).toFixed(4)),
      allocationAmount: Number(Number(item.allocationAmount || 0).toFixed(2))
    }))
  }

  function validateAllocationRows() {
    return validateAllocationRowsByRule(
      detailForm.allocationList,
      costSummary.value.subsidyTotal
    )
  }

  return {
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
  }
}
