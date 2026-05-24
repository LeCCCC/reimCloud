<script setup>
import { CopyDocument, Delete, Edit, Plus } from '@element-plus/icons-vue'

defineProps({
  open: {
    type: Boolean,
    default: true
  },
  isReadonly: {
    type: Boolean,
    default: false
  },
  tripList: {
    type: Array,
    default: () => []
  }
})

defineEmits(['toggle', 'add', 'edit', 'copy', 'delete'])
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">补录行程</div>
      <div class="section-actions" @click.stop>
        <el-button v-if="!isReadonly" link type="primary" @click="$emit('add')">
          <el-icon><Plus /></el-icon>
          补录行程
        </el-button>
        <span class="section-switch">{{ open ? '收起' : '展开' }}</span>
      </div>
    </div>
    <div v-show="open" class="section-body">
      <el-table :data="tripList" border empty-text="暂无行程数据，请点击右上角补录行程">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="出行人员" min-width="150">
          <template #default="{ row }">
            {{ row.travelerName }}<span v-if="row.travelerNo">/{{ row.travelerNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="出差日期" min-width="180">
          <template #default="{ row }">
            {{ row.departureDate }} 至 {{ row.arrivalDate }}
          </template>
        </el-table-column>
        <el-table-column label="行程" min-width="150">
          <template #default="{ row }">
            {{ row.departureCityName }} - {{ row.arrivalCityName }}
          </template>
        </el-table-column>
        <el-table-column prop="tripDescription" label="行程说明" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="!isReadonly" label="操作" width="140" align="center">
          <template #default="{ row, $index }">
            <div class="table-actions">
              <el-icon class="action-icon" @click="$emit('edit', row, $index)">
                <Edit />
              </el-icon>
              <el-icon class="action-icon" @click="$emit('copy', row)">
                <CopyDocument />
              </el-icon>
              <el-icon class="action-icon danger" @click="$emit('delete', $index)">
                <Delete />
              </el-icon>
            </div>
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

.table-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.action-icon {
  cursor: pointer;
  color: #6ea0ff;
  font-size: 15px;
}

.action-icon.danger {
  color: #ff7d7d;
}
</style>
