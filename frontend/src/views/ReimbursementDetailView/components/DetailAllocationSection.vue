<script setup>
import { Delete, Plus } from '@element-plus/icons-vue'

defineProps({
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

defineEmits([
  'toggle',
  'equalize',
  'update-company',
  'update-project',
  'ratio-change',
  'delete-row',
  'add-row'
])
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">费用归属及分摊（分摊金额：{{ subsidyTotal }}）</div>
      <div class="section-actions" @click.stop>
        <el-button v-if="!isReadonly" type="primary" plain @click="$emit('equalize')">均摊</el-button>
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
              @update:model-value="(value) => $emit('update-company', $index, value)"
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
              @update:model-value="(value) => $emit('update-project', $index, value)"
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
              @update:model-value="(value) => $emit('ratio-change', $index, value)"
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
            <el-icon class="action-icon danger" @click="$emit('delete-row', $index)">
              <Delete />
            </el-icon>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!isReadonly" class="allocation-add-line" @click="$emit('add-row')">
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
