//封装报销单列表界面相关接口
import httpInstance from "@/utils/http"
//列表查询接口
 export const getReimbursementListAPI =(params) =>{
     return httpInstance({
       url:'/travel-reimbursements',
       method :'GET',
       params
     })
 }
 //查看报销单
 export const getReimbursementDetailAPI = (id)=> {
  return httpInstance({
    url: `/travel-reimbursements/${id}`,
    method: 'get'
  })
}
//作废报销单
export  const voidReimbursementAPI =(id)=> {
  return httpInstance({
    url: `/travel-reimbursements/${id}`,
    method: 'delete'
  })
}