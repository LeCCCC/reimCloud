<script setup>
defineProps({
  open: {
    type: Boolean,
    default: true
  },
  costSummary: {
    type: Object,
    required: true
  }
})

defineEmits(['toggle'])
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">费用合计</div>
      <div class="section-switch">{{ open ? '收起' : '展开' }}</div>
    </div>
    <div v-show="open" class="section-body">
      <div class="summary-grid">
        <div class="summary-item">
          <span>补助总金额</span>
          <strong>{{ costSummary.subsidyTotal }}</strong>
        </div>
        <div class="summary-item">
          <span>餐费补助</span>
          <strong>{{ costSummary.mealAllowance }}</strong>
        </div>
        <div class="summary-item">
          <span>交通补助</span>
          <strong>{{ costSummary.transportationAllowance }}</strong>
        </div>
        <div class="summary-item">
          <span>通讯补助</span>
          <strong>{{ costSummary.phoneAllowance }}</strong>
        </div>
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

.section-switch {
  color: #95a0b1;
}

.section-body {
  padding: 18px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-item {
  padding: 18px 16px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f9fbff 0%, #f4f7fd 100%);
  color: #657489;
}

.summary-item strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
  color: #2d3d5d;
}

@media (max-width: 980px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
