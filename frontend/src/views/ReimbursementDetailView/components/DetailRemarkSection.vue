//备注模块
<script setup>
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  open: {
    type: Boolean,
    default: true
  },
  isReadonly: {
    type: Boolean,
    default: false
  },
  remarks: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['toggle', 'update:remarks'])

async function handleClearRemarks() {
  if (props.isReadonly || !props.remarks) return

  try {
    await ElMessageBox.confirm('确定删除当前备注内容吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    emit('update:remarks', '')
  } catch {}
}
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">备注信息</div>
      <div class="section-actions" @click.stop>
        <el-button v-if="!isReadonly && remarks" link type="danger" @click="handleClearRemarks">删除备注</el-button>
        <div class="section-switch">{{ open ? '收起' : '展开' }}</div>
      </div>
    </div>
    <div v-show="open" class="section-body">
      <el-input
        :model-value="remarks"
        :disabled="isReadonly"
        type="textarea"
        :rows="5"
        placeholder="请输入备注信息"
        @update:model-value="$emit('update:remarks', $event)"
      />
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

:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dce4ef inset;
}
</style>
