<script setup>
defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  companyOptions: {
    type: Array,
    default: () => []
  },
  departmentOptions: {
    type: Array,
    default: () => []
  },
  employeeOptions: {
    type: Array,
    default: () => []
  },
  businessTypeOptions: {
    type: Array,
    default: () => []
  },
  billStatusOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['search', 'reset', 'add'])
</script>

<template>
  <el-card shadow="never" class="query-card">
    <el-form :model="modelValue" label-width="88px" class="query-form">
      <div class="query-grid">
        <el-form-item label="报销单号">
          <el-input
            v-model="modelValue.billNo"
            placeholder="请输入"
            clearable
            @keyup.enter="emit('search')"
          />
        </el-form-item>

        <el-form-item label="标题">
          <el-input
            v-model="modelValue.reimbursementTitle"
            placeholder="请输入"
            clearable
            @keyup.enter="emit('search')"
          />
        </el-form-item>

        <el-form-item label="事由">
          <el-input
            v-model="modelValue.businessTripReason"
            placeholder="请输入"
            clearable
            @keyup.enter="emit('search')"
          />
        </el-form-item>

        <el-form-item label="费用归属公司">
          <el-select v-model="modelValue.reimCompanyId" placeholder="请选择" clearable >
            <el-option
              v-for="item in companyOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="报销部门">
          <el-select v-model="modelValue.reimDepartmentId" placeholder="请选择" clearable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="报销人">
          <el-select v-model="modelValue.reimburserId" placeholder="请选择" clearable>
            <el-option
              v-for="item in employeeOptions"
              :key="item.reimburserId"
              :label="`${item.reimburserName}/${item.reimburserNo}`"
              :value="item.reimburserId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="业务类型">
          <el-select v-model="modelValue.businessTypeId" placeholder="请选择" clearable>
            <el-option
              v-for="item in businessTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="单据状态">
          <el-select v-model="modelValue.billStatus" placeholder="请选择" clearable>
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
        <el-button @click="emit('add')">新增</el-button>
        <el-button @click="emit('reset')">清除</el-button>
        <el-button type="primary" @click="emit('search')">搜索</el-button>
      </div>
    </el-form>
  </el-card>
</template>

<style scoped>
.query-card {
  margin-bottom: 16px;
  border: 0;
  border-radius: 18px;
  box-shadow: 0 14px 34px rgba(31, 68, 123, 0.08);
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 4px 22px;
}

.query-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
  gap: 12px;
}

:deep(.el-card__body) {
  padding: 18px 18px 16px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: #5a6475;
  white-space: nowrap;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  min-height: 34px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dce3ee inset;
}

@media (max-width: 1380px) {
  .query-grid {
    grid-template-columns: repeat(3, minmax(220px, 1fr));
  }
}

@media (max-width: 1080px) {
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
