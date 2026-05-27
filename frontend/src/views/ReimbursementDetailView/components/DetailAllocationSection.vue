//费用分摊
<script setup>
import { Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  applyAllocationCompany,
  createAllocationRow,
  normalizeAllocationRows
} from '../composables/allocationRows'

const props = defineProps({
  open: {
    type: Boolean,
    default: true
  },
  isReadonly: {
    type: Boolean,
    default: false
  },
  allocationList: {
    type: Array,
    default: () => []
  },
  companyOptions: {
    type: Array,
    default: () => []
  },
  projectOptions: {
    type: Array,
    default: () => []
  },
  subsidyTotal: {
    type: String,
    default: '0.00'
  },
  allocationTotalPercent: {
    type: String,
    default: '0.00'
  },
  allocationTotalAmount: {
    type: String,
    default: '0.00'
  },
  toMoney: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['toggle', 'update:allocationList'])

function emitRows(rows) {
  emit('update:allocationList', rows)
}

function updateAllocationCompany(index, value) {
  const nextList = props.allocationList.map((item) => ({ ...item }))
  const row = nextList[index]
  if (!row) return
  applyAllocationCompany(row, value, new Map(props.companyOptions.map((item) => [item.id, item])))
  emitRows(nextList)
}

function updateAllocationProject(index, value) {
  const nextList = props.allocationList.map((item) => ({ ...item }))
  const row = nextList[index]
  if (!row) return

  const matched = new Map(props.projectOptions.map((item) => [item.projectId, item])).get(value)
  row.projectId = value
  row.projectNo = matched?.projectNo || ''
  row.projectName = matched?.projectName || ''
  emitRows(nextList)
}

function handleAllocationRatioInput(index, value) {
  if (index === 0) return

  const nextList = props.allocationList.map((item) => ({ ...item }))
  const currentValue = Number(value || 0)
  const others = nextList.filter((_, rowIndex) => rowIndex !== 0 && rowIndex !== index)
  const othersTotal = others.reduce((sum, item) => sum + Number(item.allocationRatioPercent || 0), 0)

  if (othersTotal + currentValue > 100) {
    nextList[index].allocationRatioPercent = null
    ElMessage.warning('除首行外的分摊比例合计不能超过 100%')
  } else {
    nextList[index].allocationRatioPercent = Number(currentValue.toFixed(2))
  }

  normalizeAllocationRows(nextList, props.subsidyTotal)
  emitRows(nextList)
}

function handleAddAllocationRow() {
  const nextList = [...props.allocationList.map((item) => ({ ...item })), createAllocationRow()]
  normalizeAllocationRows(nextList, props.subsidyTotal)
  emitRows(nextList)
}

async function handleDeleteAllocationRow(index) {
  if (props.allocationList.length === 1) {
    ElMessage.warning('至少保留一条分摊信息')
    return
  }

  await ElMessageBox.confirm('确定删除吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })

  const nextList = props.allocationList.map((item) => ({ ...item }))
  nextList.splice(index, 1)
  normalizeAllocationRows(nextList, props.subsidyTotal)
  emitRows(nextList)
}

function handleEqualAllocation() {
  const nextList = props.allocationList.map((item) => ({ ...item }))
  const total = Number(props.subsidyTotal || 0)

  if (nextList.length === 0) return

  const basePercent = Number((100 / nextList.length).toFixed(2))
  const restPercent = Number((basePercent * (nextList.length - 1)).toFixed(2))
  nextList[0].allocationRatioPercent = Number((100 - restPercent).toFixed(2))

  for (let index = 1; index < nextList.length; index += 1) {
    nextList[index].allocationRatioPercent = basePercent
  }

  const averageAmount = Number((total / nextList.length).toFixed(2))
  const restAmount = Number((averageAmount * (nextList.length - 1)).toFixed(2))
  nextList[0].allocationAmount = Number((total - restAmount).toFixed(2))

  for (let index = 1; index < nextList.length; index += 1) {
    nextList[index].allocationAmount = averageAmount
  }

  emitRows(nextList)
}
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">费用归属及分摊（分摊金额：{{ subsidyTotal }}）</div>
      <div class="section-actions" @click.stop>
        <el-button v-if="!isReadonly" type="primary" plain @click="handleEqualAllocation">均摊</el-button>
        <span class="section-switch">{{ open ? '收起' : '展开' }}</span>
      </div>
    </div>
    <div v-show="open" class="section-body">
      <el-table :data="allocationList" border>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="费用归属" min-width="220">
          <template #default="{ row, $index }">
            <el-select
              :model-value="row.reimCompanyId"
              :disabled="isReadonly"
              filterable
              placeholder="请选择"
              @update:model-value="(value) => updateAllocationCompany($index, value)"
            >
              <el-option
                v-for="item in companyOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="项目" min-width="220">
          <template #default="{ row, $index }">
            <el-select
              :model-value="row.projectId"
              :disabled="isReadonly"
              filterable
              placeholder="请选择"
              @update:model-value="(value) => updateAllocationProject($index, value)"
            >
              <el-option
                v-for="item in projectOptions"
                :key="item.projectId"
                :label="item.projectName"
                :value="item.projectId"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="分摊比例" min-width="150" align="right">
          <template #default="{ row, $index }">
            <el-input-number
              :model-value="row.allocationRatioPercent"
              :disabled="isReadonly || $index === 0"
              :min="0"
              :max="100"
              :precision="2"
              controls-position="right"
              @update:model-value="(value) => handleAllocationRatioInput($index, value)"
            />
            <span class="suffix">%</span>
          </template>
        </el-table-column>
        <el-table-column label="分摊金额" min-width="150" align="right">
          <template #default="{ row }">
            <span>{{ toMoney(row.allocationAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isReadonly" label="操作" width="90" align="center">
          <template #default="{ $index }">
            <el-icon class="action-icon danger" @click="handleDeleteAllocationRow($index)">
              <Delete />
            </el-icon>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!isReadonly" class="allocation-add-line" @click="handleAddAllocationRow">
        <el-icon><Plus /></el-icon>
        添加一行
      </div>

      <div class="allocation-footer">
        <span>合计</span>
        <span>{{ allocationTotalPercent }}%</span>
        <span>CNY {{ allocationTotalAmount }}</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.section-card {
  margin-bottom: 16px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(31, 68, 123, 0.08);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 50px;
  padding: 0 18px;
  background: #f8fbff;
  cursor: pointer;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #314461;
}

.section-title::before {
  content: '';
  width: 3px;
  height: 16px;
  border-radius: 999px;
  background: #4b8cff;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-switch {
  color: #95a0b1;
}

.section-body {
  padding: 18px;
}

.allocation-add-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 14px 0 4px;
  color: #4b8cff;
  cursor: pointer;
}

.allocation-footer {
  display: grid;
  grid-template-columns: 1fr 160px 180px;
  align-items: center;
  margin-top: 10px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #fff8eb;
  color: #ff8b1f;
  font-weight: 700;
}

.action-icon {
  cursor: pointer;
  color: #6ea0ff;
  font-size: 15px;
}

.action-icon.danger {
  color: #ff7d7d;
}

.suffix {
  margin-left: 8px;
  color: #7f8b9d;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-input-number) {
  border-radius: 8px;
}

:deep(.el-input-number) {
  width: 110px;
}
</style>
