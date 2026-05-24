<script setup>
defineProps({
  pageTitle: {
    type: String,
    default: ''
  },
  billNo: {
    type: String,
    default: ''
  },
  billDate: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <div class="detail-header-shell">
    <div class="detail-header">
      <div class="header-side header-left">
        <div v-if="billNo" class="bill-no">单据编号：{{ billNo }}</div>
      </div>

      <div class="header-center">
        <div class="page-title">{{ pageTitle }}</div>
      </div>

      <div class="header-side header-right">
        <div class="bill-date">单据日期：{{ billDate }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-header-shell {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(245, 247, 251, 0.96);
  backdrop-filter: blur(10px);
  box-shadow: 0 6px 18px rgba(31, 68, 123, 0.08);
}

.detail-header {
  width: min(1120px, calc(100vw - 48px));
  min-height: 88px;
  margin: 0 auto;
  padding: 0 0 8px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
  color: #4d5a73;
}

.header-side {
  min-width: 0;
  font-size: 14px;
  color: #7b8798;
}

.header-left {
  justify-self: start;
}

.header-center {
  justify-self: center;
  text-align: center;
}

.header-right {
  justify-self: end;
  text-align: right;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #30425e;
}

.bill-no,
.bill-date {
  white-space: nowrap;
}

@media (max-width: 980px) {
  .detail-header {
    width: min(1120px, calc(100vw - 24px));
    min-height: 96px;
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 12px 0;
  }

  .header-left,
  .header-center,
  .header-right {
    justify-self: stretch;
    text-align: center;
  }

  .page-title {
    font-size: 22px;
  }
}
</style>
