// 行程编辑弹窗
<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'create'
  },
  form: {
    type: Object,
    required: true
  },
  employeeOptions: {
    type: Array,
    default: () => []
  },
  cityOptions: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:visible', 'submit'])
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="mode === 'edit' ? '编辑补录行程' : '补录行程'"
    width="640px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="dialog-tip">
      <div>仅可补录未从申请单带入或未产生费用的行程信息</div>
      <div>跨天跨城行程填写说明：出发城市-到达城市：武汉-北京；出发日期-到达日期：1号-5号；1号-5号补助按北京匹配。</div>
    </div>

    <el-form label-width="86px">
      <el-form-item label="出行人">
        <el-select v-model="form.travelerId" filterable placeholder="请选择出行人">
          <el-option
            v-for="item in employeeOptions"
            :key="item.reimburserId"
            :label="`${item.reimburserName}/${item.reimburserNo}`"
            :value="item.reimburserId"
          />
        </el-select>
      </el-form-item>

      <div class="dialog-grid">
        <el-form-item label="出发城市">
          <el-select v-model="form.departureCityNo" filterable placeholder="请选择城市">
            <el-option
              v-for="item in cityOptions"
              :key="item.cityNo"
              :label="item.cityName"
              :value="item.cityNo"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="到达城市">
          <el-select v-model="form.arrivalCityNo" filterable placeholder="请选择城市">
            <el-option
              v-for="item in cityOptions"
              :key="item.cityNo"
              :label="item.cityName"
              :value="item.cityNo"
            />
          </el-select>
        </el-form-item>
      </div>

      <div class="dialog-grid">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="form.departureDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
          />
        </el-form-item>

        <el-form-item label="结束日期">
          <el-date-picker
            v-model="form.arrivalDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择结束日期"
          />
        </el-form-item>
      </div>

      <el-form-item label="行程说明">
        <el-input
          v-model="form.tripDescription"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="请输入行程说明"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button type="primary" @click="$emit('submit')">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-tip {
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff8eb;
  color: #c8841f;
  line-height: 1.6;
  font-size: 13px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-date-editor.el-input) {
  width: 100%;
}

@media (max-width: 980px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
