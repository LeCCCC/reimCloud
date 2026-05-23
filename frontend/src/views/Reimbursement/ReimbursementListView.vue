<script setup>
import { reactive, ref, onMounted } from 'vue'
import { View, Edit, MoreFilled } from '@element-plus/icons-vue'
import {getReimbursementListAPI} from '@/apis/reimbursement'

const getList= async()=>{
  const res= await getReimbursementListAPI(queryForm)
  console.log(res)
}
onMounted(() =>{
  getList()
})
const queryForm = reactive({
  current: 1,
  size: 10,
  billNo: '',
  reimbursementTitle: '',
  businessTripReason: '',
  reimCompanyId: '',
  reimDepartmentId: '',
  reimburserId: '',
  businessTypeId: '',
  billStatus: ''
})

const companyOptions = [
  { label: '胜意科技武汉分公司', value: '1' },
  { label: '胜意科技杭州分公司', value: '2' }
]

const departmentOptions = [
  { label: '企业费控事业部', value: '1' },
  { label: '集采事业部', value: '2' },
  { label: '航旅事业部', value: '3' }
]

const reimburserOptions = [
  { label: '郑雨雪', value: '1' },
  { label: '徐年年', value: '2' },
  { label: '邹薇', value: '3' }
]

const businessTypeOptions = [
  { label: '员工团建', value: '1' },
  { label: '招聘会', value: '2' },
  { label: '客户拜访', value: '3' }
]

const billStatusOptions = [
  { label: '草稿', value: '0' },
  { label: '已完成', value: '1' },
  { label: '已作废', value: '2' }
]

const tableData = ref([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(24)

const handleSearch = () => {
  console.log('点击搜索', queryForm)
}

const handleReset = () => {
  queryForm.billNo = ''
  queryForm.reimbursementTitle = ''
  queryForm.businessTripReason = ''
  queryForm.reimCompanyId = ''
  queryForm.reimDepartmentId = ''
  queryForm.reimburserId = ''
  queryForm.businessTypeId = ''
  queryForm.billStatus = ''
}

const handleAdd = () => {
  console.log('点击新增')
}

const handleView = (row) => {
  console.log('查看', row)
}

const handleEdit = (row) => {
  console.log('编辑', row)
}

const handleMoreCommand = (command, row) => {
  console.log(command, row)
}
</script>

<template>
  <div class="reimbursement-list-page">
    <div class="page-title">报销单列表页</div>

    <el-card shadow="never" class="query-card">
      <el-form :model="queryForm" label-width="100px" class="query-form">
        <div class="query-grid">
          <el-form-item label="报销单号">
            <el-input v-model="queryForm.billNo" placeholder="请输入" clearable />
          </el-form-item>

          <el-form-item label="标题">
            <el-input v-model="queryForm.reimbursementTitle" placeholder="请输入" clearable />
          </el-form-item>

          <el-form-item label="事由">
            <el-input v-model="queryForm.businessTripReason" placeholder="请输入" clearable />
          </el-form-item>

          <el-form-item label="费用归属公司">
            <el-select v-model="queryForm.reimCompanyId" placeholder="请选择" clearable>
              <el-option
                v-for="item in companyOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="报销部门">
            <el-select v-model="queryForm.reimDepartmentId" placeholder="请选择" clearable>
              <el-option
                v-for="item in departmentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="报销人">
            <el-select v-model="queryForm.reimburserId" placeholder="请选择" clearable>
              <el-option
                v-for="item in reimburserOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="业务类型">
            <el-select v-model="queryForm.businessTypeId" placeholder="请选择" clearable>
              <el-option
                v-for="item in businessTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="单据状态">
            <el-select v-model="queryForm.billStatus" placeholder="请选择" clearable>
              <el-option
                v-for="item in billStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="query-actions">
          <el-button @click="handleAdd">新增</el-button>
          <el-button @click="handleReset">清除</el-button>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column label="操作" width="120" fixed="left">
          <template #default="{ row }">
            <div class="action-cell">
              <el-tooltip content="查看详情" placement="top" effect="dark">
                <el-icon class="action-icon" @click="handleView(row)">
                  <View />
                </el-icon>
              </el-tooltip>

              <el-tooltip content="编辑" placement="top" effect="dark">
                <el-icon class="action-icon" @click="handleEdit(row)">
                  <Edit />
                </el-icon>
              </el-tooltip>

              <el-dropdown @command="(command) => handleMoreCommand(command, row)">
                <el-icon class="action-icon">
                  <MoreFilled />
                </el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="void">作废</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>

        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="billNo" label="报销单号" min-width="160" />
        <el-table-column label="单据状态" min-width="100">
          <template #default="{ row }">
            <el-tag v-if="row.billStatus === '1'" type="success">{{ row.billStatusName }}</el-tag>
            <el-tag v-else-if="row.billStatus === '0'" type="info">{{ row.billStatusName }}</el-tag>
            <el-tag v-else type="danger">{{ row.billStatusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reimbursementTitle" label="报销标题" min-width="160" />
        <el-table-column prop="businessTripReason" label="报销事由" min-width="160" />
        <el-table-column prop="reimburserName" label="报销人" min-width="120" />
        <el-table-column prop="reimDepartmentName" label="报销部门" min-width="150" />
        <el-table-column prop="reimCompanyName" label="费用归属公司" min-width="180" />
        <el-table-column prop="businessTypeName" label="业务类型" min-width="120" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.reimbursement-list-page {
  min-height: 100vh;
  padding: 16px;
  background: #f5f7fa;
  box-sizing: border-box;
}

.page-title {
  margin-bottom: 16px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.query-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.query-form {
  width: 100%;
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 8px 20px;
}

.query-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  gap: 12px;
}

.table-card {
  border-radius: 8px;
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-icon {
  cursor: pointer;
  color: #7aa7ff;
  font-size: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input) {
  width: 100%;
}

@media (max-width: 1400px) {
  .query-grid {
    grid-template-columns: repeat(3, minmax(220px, 1fr));
  }
}

@media (max-width: 1100px) {
  .query-grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 768px) {
  .query-grid {
    grid-template-columns: 1fr;
  }

  .query-actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>