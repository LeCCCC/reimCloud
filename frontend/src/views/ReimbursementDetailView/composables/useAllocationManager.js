//父页面需要的分摊初始化/校验/payload
import { validateAllocationRows as validateAllocationRowsByRule } from './reimbursementDetailValidators'
import {
  applyAllocationCompany,
  buildAllocationPayload as buildAllocationPayloadByRows,
  createAllocationRow,
  normalizeAllocationRows as normalizeAllocationRowsByRows
} from './allocationRows'

export function useAllocationManager({
  detailForm,
  costSummary,
  companyMap
}) {
  function normalizeAllocationRows() {
    normalizeAllocationRowsByRows(detailForm.allocationList, costSummary.value.subsidyTotal)
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
    const row = detailForm.allocationList[index]
    if (!row) return
    applyAllocationCompany(row, value, companyMap.value)
  }

  function buildAllocationPayload() {
    return buildAllocationPayloadByRows(detailForm.allocationList)
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
    buildAllocationPayload,
    validateAllocationRows
  }
}
