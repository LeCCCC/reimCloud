import httpInstance from '@/utils/http'
//查询报销单分页列表
export function getReimbursementListAPI(params) {
  return httpInstance({
    url: '/travel-reimbursements',
    method: 'GET',
    params
  })
}
//新增报销单。
export function createReimbursementAPI(data) {
  return httpInstance({
    url: '/travel-reimbursements',
    method: 'POST',
    data
  })
}
//查询某一张报销单详情
export function getReimbursementDetailAPI(id) {
  return httpInstance({
    url: `/travel-reimbursements/${id}`,
    method: 'GET'
  })
}
//修改报销单
export function updateReimbursementAPI(id, data) {
  return httpInstance({
    url: `/travel-reimbursements/${id}`,
    method: 'PUT',
    data
  })
}
//提交报销单
export function submitReimbursementAPI(id) {
  return httpInstance({
    url: `/travel-reimbursements/${id}/submission`,
    method: 'POST'
  })
}
//作废报销单
export function voidReimbursementAPI(id) {
  return httpInstance({
    url: `/travel-reimbursements/${id}`,
    method: 'DELETE'
  })
}
//查询补助日历。
export function getSubsidyCalendarAPI(id, subsidyId) {
  return httpInstance({
    url: `/travel-reimbursements/${id}/subsidies/${subsidyId}/calendar`,
    method: 'GET'
  })
}
//保存补助日历。
export function saveSubsidyCalendarAPI(id, subsidyId, data) {
  return httpInstance({
    url: `/travel-reimbursements/${id}/subsidies/${subsidyId}/calendar`,
    method: 'PUT',
    data
  })
}
