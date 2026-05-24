import { createRouter, createWebHistory } from 'vue-router'
import ReimbursementListView from '@/views/ReimbursementListView/ReimbursementListView.vue'
import ReimbursementDetailView from '@/views/ReimbursementDetailView/ReimbursementDetailView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/reimburse/list'
    },
    {
      path: '/reimburse/list',
      name: 'ReimbursementList',
      component: ReimbursementListView,
      meta: {
        keepAlive: true
      }
    },
    {
      path: '/reimburse/detail',
      name: 'ReimbursementCreate',
      component: ReimbursementDetailView
    },
    {
      path: '/reimburse/detail/:id',
      name: 'ReimbursementDetail',
      component: ReimbursementDetailView
    }
  ]
})

export default router
