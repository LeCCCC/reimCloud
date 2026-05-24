import { reactive } from 'vue'

export const reimbursementViewState = reactive({
  listRefreshToken: 0
})

export function triggerReimbursementListRefresh() {
  reimbursementViewState.listRefreshToken += 1
}
