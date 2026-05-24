import { computed, ref } from 'vue'
import {
  getBusinessTypeOptionsAPI,
  getCompanyOptionsAPI,
  getDepartmentOptionsAPI,
  getEmployeeOptionsAPI
} from '@/apis/selectOptions'

function flattenBusinessTypeTree(nodes = []) {
  return nodes.flatMap((node) => {
    if (node.children?.length) {
      return flattenBusinessTypeTree(node.children)
    }

    return [
      {
        businessTypeId: node.businessTypeId,
        businessTypeNo: node.businessTypeNo,
        businessTypeName: node.businessTypeName
      }
    ]
  })
}

export function useBusinessTypeOptions() {
  const loading = ref(false)

  const companyOptions = ref([])
  const departmentOptions = ref([])
  const employeeOptions = ref([])
  const businessTypeOptions = ref([])

  const businessTypeSelectOptions = computed(() =>
    businessTypeOptions.value.map((item) => ({
      label: item.businessTypeName,
      value: item.businessTypeId
    }))
  )

  const businessTypeMap = computed(() =>
    new Map(businessTypeOptions.value.map((item) => [item.businessTypeId, item]))
  )

  async function loadBusinessTypeOptions(params) {
    const res = await getBusinessTypeOptionsAPI(params)
    businessTypeOptions.value = flattenBusinessTypeTree(res?.data || [])
    return businessTypeOptions.value
  }

  async function loadCommonSelectOptions() {
    loading.value = true

    try {
      const [companyRes, departmentRes, employeeRes, businessTypeRes] = await Promise.all([
        getCompanyOptionsAPI(),
        getDepartmentOptionsAPI(),
        getEmployeeOptionsAPI(),
        getBusinessTypeOptionsAPI()
      ])

      companyOptions.value = companyRes?.data || []
      departmentOptions.value = departmentRes?.data || []
      employeeOptions.value = employeeRes?.data || []
      businessTypeOptions.value = flattenBusinessTypeTree(businessTypeRes?.data || [])
    } finally {
      loading.value = false
    }
  }

  function getBusinessTypeById(id) {
    return businessTypeMap.value.get(id) || null
  }

  return {
    loading,
    companyOptions,
    departmentOptions,
    employeeOptions,
    businessTypeOptions,
    businessTypeSelectOptions,
    businessTypeMap,
    loadBusinessTypeOptions,
    loadCommonSelectOptions,
    getBusinessTypeById
  }
}
