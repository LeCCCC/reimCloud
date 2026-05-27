<script setup>
defineProps({
  open: {
    type: Boolean,
    default: true
  },
  isReadonly: {
    type: Boolean,
    default: false
  },
  form: {
    type: Object,
    required: true
  },
  employeeOptions: {
    type: Array,
    default: () => []
  },
  departmentOptions: {
    type: Array,
    default: () => []
  },
  companyOptions: {
    type: Array,
    default: () => []
  },
  businessTypeOptions: {
    type: Array,
    default: () => []
  }
})

defineEmits(['toggle'])
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">基础信息</div>
      <div class="section-switch">{{ open ? '收起' : '展开' }}</div>
    </div>

    <div v-show="open" class="section-body">
      <el-form label-width="92px" class="detail-form">
        <el-form-item label="报销标题">
          <el-input
            v-model="form.reimbursementTitle"
            :disabled="isReadonly"
            placeholder="请输入报销标题"
          />
        </el-form-item>

        <div class="three-columns">
          <el-form-item label="报销人">
            <el-select
              v-model="form.reimburserId"
              :disabled="isReadonly"
              placeholder="请选择"
            >
              <el-option
                v-for="item in employeeOptions"
                :key="item.reimburserId"
                :label="`${item.reimburserName}/${item.reimburserNo}`"
                :value="item.reimburserId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="报销部门">
            <el-select
              v-model="form.reimDepartmentId"
              :disabled="isReadonly"
              placeholder="请选择"
            >
              <el-option
                v-for="item in departmentOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="费用归属公司">
            <el-select
              v-model="form.reimCompanyId"
              :disabled="isReadonly"
              placeholder="请选择"
            >
              <el-option
                v-for="item in companyOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="three-columns">
          <el-form-item label="业务类型">
            <el-tree-select
              v-model="form.businessTypeId"
              :disabled="isReadonly"
              :data="businessTypeOptions"
              :props="{
                label: 'businessTypeName',
                value: 'businessTypeId',
                children: 'children',
                disabled: 'disabled'
              }"
              check-strictly
              clearable
              filterable
              node-key="businessTypeId"
              placeholder="请选择"
              render-after-expand="false"
            />
          </el-form-item>
        </div>

        <el-form-item label="出差事由">
          <el-input
            v-model="form.businessTripReason"
            :disabled="isReadonly"
            type="textarea"
            :rows="3"
            placeholder="请输入出差事由"
          />
        </el-form-item>
      </el-form>
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

.section-switch {
  color: #95a0b1;
}

.section-body {
  padding: 18px;
}

.three-columns {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  white-space: nowrap;
}

:deep(.el-select),
:deep(.el-tree-select) {
  width: 100%;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-tree-select .el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dce4ef inset;
}

@media (max-width: 980px) {
  .three-columns {
    grid-template-columns: 1fr;
  }
}
</style>
