<script setup>
import { Edit, MoreFilled, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { voidReimbursementAPI } from '@/apis/reimbursement'

const props = defineProps({
  tableData: {
    type: Array,
    default: () => []
  },
  current: {
    type: Number,
    default: 1
  },
  size: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'view',
  'edit',
  'refresh',
  'current-change',
  'size-change'
])

const getStatusTagType = (status) => {
  if (status === '1') return 'success'
  if (status === '2') return 'danger'
  return 'info'
}

const getRowIndex = (index) => {
  return (props.current - 1) * props.size + index + 1
}

const handleEdit = (row) => {
  if (row.billStatus === '1') {
    ElMessage.warning('已完成单据不允许编辑')
    return
  }

  emit('edit', row)
}

const copyBillNo = async (row) => {
  try {
    await navigator.clipboard.writeText(row.billNo || '')
    ElMessage.success('报销单号已复制')
  } catch (error) {
    console.error('复制失败', error)
    ElMessage.warning('复制失败，请手动复制')
  }
}

const voidReimbursement = async (row) => {
  try {
    await ElMessageBox.confirm(`确认作废报销单 ${row.billNo} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await voidReimbursementAPI(row.id)
    ElMessage.success('作废成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('作废失败', error)
      ElMessage.error(error?.response?.data?.message || '作废失败')
    }
  }
}

const handleMoreCommand = (command, row) => {
  if (command === 'copy') {
    copyBillNo(row)
    return
  }

  if (command === 'void') {
    voidReimbursement(row)
  }
}
</script>

<template>
  <el-card shadow="never" class="table-card">
    <el-table
      :data="tableData"
      row-key="id"
      border
      class="list-table"
    >
      <el-table-column label="操作" width="126" fixed="left" align="center">
        <template #default="{ row }">
          <div class="action-cell">
            <el-tooltip content="查看" placement="top">
              <el-icon class="action-icon" @click="emit('view', row)">
                <View />
              </el-icon>
            </el-tooltip>

            <el-tooltip content="编辑" placement="top">
              <el-icon class="action-icon" @click="handleEdit(row)">
                <Edit />
              </el-icon>
            </el-tooltip>

            <el-dropdown @command="(command) => handleMoreCommand(command, row)">
              <el-icon class="action-icon more-icon">
                <MoreFilled />
              </el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="copy">复制单号</el-dropdown-item>
                  <el-dropdown-item command="void">作废</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="序号" width="70" align="center">
        <template #default="{ $index }">
          {{ getRowIndex($index) }}
        </template>
      </el-table-column>

      <el-table-column prop="billNo" label="报销单号" min-width="160">
        <template #default="{ row }">
          <el-button link type="primary" class="link-button" @click="emit('view', row)">
            {{ row.billNo }}
          </el-button>
        </template>
      </el-table-column>

      <el-table-column label="单据状态" width="108" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.billStatus)" effect="light" round>
            {{ row.billStatusName }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="报销标题" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.reimbursementTitle }}
        </template>
      </el-table-column>

      <el-table-column label="报销事由" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.businessTripReason }}
        </template>
      </el-table-column>

      <el-table-column label="报销人" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.reimburserName }}
        </template>
      </el-table-column>

      <el-table-column label="报销部门" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.reimDepartmentName }}
        </template>
      </el-table-column>

      <el-table-column label="费用归属公司" min-width="190" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.reimCompanyName }}
        </template>
      </el-table-column>

      <el-table-column label="业务类型" min-width="130" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.businessTypeName }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="current"
        :page-size="size"
        :page-sizes="[5, 10, 20]"
        :total="total"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="(page) => emit('current-change', page)"
        @size-change="(pageSize) => emit('size-change', pageSize)"
      />
    </div>
  </el-card>
</template>

<style scoped>
.table-card {
  border: 0;
  border-radius: 18px;
  box-shadow: 0 14px 34px rgba(31, 68, 123, 0.08);
}

.list-table {
  --el-table-border-color: #edf1f7;
  --el-table-header-bg-color: #f8fbff;
  --el-table-row-hover-bg-color: #f7fbff;
}

.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.action-icon {
  cursor: pointer;
  color: #72a7ff;
  font-size: 15px;
  transition: color 0.2s ease, transform 0.2s ease;
}

.action-icon:hover {
  color: #3f7cf6;
  transform: translateY(-1px);
}

.action-icon.disabled {
  color: #c2c8d4;
}

.link-button {
  padding: 0;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

:deep(.el-card__body) {
  padding: 18px 18px 16px;
}

:deep(.el-table th.el-table__cell) {
  color: #2d3b55;
  font-weight: 600;
}

@media (max-width: 768px) {
  .pagination-wrapper {
    justify-content: center;
  }
}

:deep(.el-tooltip__trigger:focus-visible) {
  outline: none;
}

:deep(.el-dropdown:focus-visible) {
  outline: none;
}

.more-icon:focus,
.more-icon:focus-visible {
  outline: none;
}
</style>
