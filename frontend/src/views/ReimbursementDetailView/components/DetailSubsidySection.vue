<script setup>
import { Edit, WarningFilled } from '@element-plus/icons-vue'

defineProps({
  open: {
    type: Boolean,
    default: true
  },
  subsidyList: {
    type: Array,
    default: () => []
  },
  tripCount: {
    type: Number,
    default: 0
  },
  costSummary: {
    type: Object,
    required: true
  },
  isReadonly: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle', 'edit'])
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">
        补助信息
        <span class="summary-inline">{{ costSummary.subsidyTotal }}（{{ tripCount }}条行程）</span>
      </div>
      <div class="section-switch">{{ open ? '收起' : '展开' }}</div>
    </div>
    <div v-show="open" class="section-body">
      <el-tooltip
        effect="dark"
        content="1、请根据实际出差日期选择补助 2、出差期间当日有用餐安排的请自行核减当日餐补 3、出差期间当日有用车的，请自行核减当日交补"
        placement="top-start"
      >
        <div class="tip-banner">
          <el-icon><WarningFilled /></el-icon>
          <span class="tip-text">1、请根据实际出差日期选择补助 2、出差期间当日有用餐安排的请自行核减当日餐补 3、出差期间当日有用车的，请自行核减当日交补</span>
        </div>
      </el-tooltip>

      <el-table :data="subsidyList" border>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="travelerName" label="出行人" min-width="110" />
        <el-table-column prop="travelDateRange" label="出差日期" min-width="170" />
        <el-table-column prop="subsidyDays" label="补助天数" min-width="90" align="center" />
        <el-table-column prop="tripRoute" label="行程" min-width="140" />
        <el-table-column prop="subsidyCityName" label="补助城市" min-width="110" />
        <el-table-column prop="applicationAmount" label="申请金额" min-width="110" align="right" />
        <el-table-column prop="subsidyAmount" label="补助金额" min-width="110" align="right" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row, $index }">
            <el-icon class="action-icon" :class="{ disabled: isReadonly }" @click="$emit('edit', row, $index)">
              <Edit />
            </el-icon>
          </template>
        </el-table-column>
      </el-table>
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

.summary-inline {
  color: #8e9bb0;
  font-size: 13px;
  font-weight: 500;
}

.tip-banner {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff8eb;
  color: #c8841f;
  font-size: 13px;
}

.tip-text {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.action-icon {
  cursor: pointer;
  color: #6ea0ff;
  font-size: 15px;
}

.action-icon.disabled {
  color: #c2c8d4;
  cursor: not-allowed;
}
</style>
