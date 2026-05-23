import { createRouter, createWebHistory } from "vue-router"
import ReimbursementListView from "@/views/Reimbursement/ReimbursementListView.vue"
  
const router = createRouter (
  {
    history :createWebHistory(),
    routes:[
      {
        path: '/',
        redirect: '/reimburse/list'
      },
      {
        path:'/reimburse/list',
        name: 'ReimbursementList',
        component:ReimbursementListView
      }
    ]
  }
)

export default router