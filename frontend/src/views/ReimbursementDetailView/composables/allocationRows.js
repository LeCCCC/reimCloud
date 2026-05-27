//分摊纯算法
export function createAllocationRow(overrides = {}) {
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

export function normalizeAllocationRows(rows, totalAmount) {
  const total = Number(totalAmount || 0)

  if (rows.length === 0) {
    return rows
  }

  if (rows.length === 1) {
    rows[0].allocationRatioPercent = 100
    rows[0].allocationAmount = total
    return rows
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
  return rows
}

export function applyAllocationCompany(row, value, companyMap) {
  const matched = companyMap.get(value)
  row.reimCompanyId = value
  row.reimCompanyNo = matched?.no || ''
  row.reimCompanyName = matched?.name || ''
}

export function buildAllocationPayload(rows) {
  return rows.map((item) => ({
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
