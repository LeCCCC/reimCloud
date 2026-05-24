//封装下拉控件相关接口
import httpInstance from "@/utils/http"
// 费用归属公司下拉
export const getCompanyOptionsAPI = (params) => {
  return httpInstance({
    url: '/reim-companies',
    method: 'GET',
    params
  })
}

// 报销部门下拉
export const getDepartmentOptionsAPI = (params) => {
  return httpInstance({
    url: '/reim-departments',
    method: 'GET',
    params
  })
}

// 报销人 / 出行人下拉
export const getEmployeeOptionsAPI = (params) => {
  return httpInstance({
    url: '/employees',
    method: 'GET',
    params
  })
}

// 业务类型树形下拉
export const getBusinessTypeOptionsAPI = (params) => {
  return httpInstance({
    url: '/business-types/tree',
    method: 'GET',
    params
  })
}

// 城市下拉，补录行程弹窗用
export const getCityOptionsAPI = (params) => {
  return httpInstance({
    url: '/cities',
    method: 'GET',
    params
  })
}

// 项目下拉，费用分摊用
export const getProjectOptionsAPI = (params) => {
  return httpInstance({
    url: '/projects',
    method: 'GET',
    params
  })
}