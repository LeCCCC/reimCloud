import { computed, ref } from 'vue'
import {
  getBusinessTypeOptionsAPI,
  getCompanyOptionsAPI,
  getDepartmentOptionsAPI,
  getEmployeeOptionsAPI
} from '@/apis/selectOptions'

function mapBusinessTypeTree(nodes = []) {
  return nodes.map((node) => ({
    businessTypeId: node.businessTypeId,
    businessTypeNo: node.businessTypeNo,
    businessTypeName: node.businessTypeName,
    disabled: Boolean(node.children?.length),
    children: mapBusinessTypeTree(node.children || [])
  }))
}

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
  const companyOptions = ref([])
  const departmentOptions = ref([])
  const employeeOptions = ref([])
  const businessTypeTreeOptions = ref([])
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
    businessTypeTreeOptions.value = mapBusinessTypeTree(res?.data || [])
    businessTypeOptions.value = flattenBusinessTypeTree(res?.data || [])
    return businessTypeOptions.value
  }

  async function loadCommonSelectOptions() {
      const [companyRes, departmentRes, employeeRes, businessTypeRes] = await Promise.all([
        getCompanyOptionsAPI(),
        getDepartmentOptionsAPI(),
        getEmployeeOptionsAPI(),
        getBusinessTypeOptionsAPI()
      ])
      companyOptions.value = companyRes?.data || []
      departmentOptions.value = departmentRes?.data || []
      employeeOptions.value = employeeRes?.data || []
      businessTypeTreeOptions.value = mapBusinessTypeTree(businessTypeRes?.data || [])
      businessTypeOptions.value = flattenBusinessTypeTree(businessTypeRes?.data || [])
  }

  function getBusinessTypeById(id) {
    return businessTypeMap.value.get(id) || null
  }

  return {
    companyOptions,
    departmentOptions,
    employeeOptions,
    businessTypeTreeOptions,
    businessTypeOptions,
    businessTypeSelectOptions,
    businessTypeMap,
    loadBusinessTypeOptions,
    loadCommonSelectOptions,
    getBusinessTypeById
  }
}
