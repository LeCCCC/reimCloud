<script setup>
import DetailAllocationSection from './components/DetailAllocationSection.vue'
import DetailBasicInfoSection from './components/DetailBasicInfoSection.vue'
import DetailHeaderBar from './components/DetailHeaderBar.vue'
import DetailRemarkSection from './components/DetailRemarkSection.vue'
import DetailSubsidySection from './components/DetailSubsidySection.vue'
import DetailSummarySection from './components/DetailSummarySection.vue'
import DetailTripSection from './components/DetailTripSection.vue'
import { useReimbursementDetail } from './composables/useReimbursementDetail'

const {
  loading,
  submitLoading,
  companyOptions,
  departmentOptions,
  employeeOptions,
  businessTypeTreeOptions,
  cityOptions,
  projectOptions,
  sectionState,
  detailForm,
  isReadonly,
  pageTitle,
  billDate,
  costSummary,
  allocationTotalAmount,
  allocationTotalPercent,
  toggleSection,
  toMoney,
  rebuildSubsidyList,
  normalizeAllocationRows,
  confirmClose,
  submitAndComplete
} = useReimbursementDetail()
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <DetailHeaderBar
      :page-title="pageTitle"
      :bill-no="detailForm.billNo"
      :bill-date="billDate"
    />

    <div class="detail-shell">
      <DetailBasicInfoSection
        :open="sectionState.basic"
        :is-readonly="isReadonly"
        :form="detailForm"
        :employee-options="employeeOptions"
        :department-options="departmentOptions"
        :company-options="companyOptions"
        :business-type-options="businessTypeTreeOptions"
        @toggle="toggleSection('basic')"
      />

      <DetailTripSection
        :open="sectionState.trip"
        :is-readonly="isReadonly"
        v-model:trip-list="detailForm.tripList"
        :employee-options="employeeOptions"
        :city-options="cityOptions"
        @toggle="toggleSection('trip')"
        @changed="rebuildSubsidyList"
      />

      <DetailSubsidySection
        :open="sectionState.subsidy"
        v-model:subsidy-list="detailForm.subsidyList"
        :trip-list="detailForm.tripList"
        :detail-id="detailForm.id"
        :business-type-name="detailForm.businessTypeName"
        :city-options="cityOptions"
        :trip-count="detailForm.tripList.length"
        :cost-summary="costSummary"
        :is-readonly="isReadonly"
        @toggle="toggleSection('subsidy')"
        @changed="normalizeAllocationRows"
      />

      <DetailSummarySection
        :open="sectionState.summary"
        :cost-summary="costSummary"
        @toggle="toggleSection('summary')"
      />

      <DetailAllocationSection
        :open="sectionState.allocation"
        :is-readonly="isReadonly"
        v-model:allocation-list="detailForm.allocationList"
        :company-options="companyOptions"
        :project-options="projectOptions"
        :subsidy-total="costSummary.subsidyTotal"
        :allocation-total-percent="allocationTotalPercent"
        :allocation-total-amount="allocationTotalAmount"
        :to-money="toMoney"
        @toggle="toggleSection('allocation')"
      />

      <DetailRemarkSection
        :open="sectionState.remark"
        :is-readonly="isReadonly"
        :remarks="detailForm.remarks"
        @toggle="toggleSection('remark')"
        @update:remarks="detailForm.remarks = $event"
      />
    </div>

    <div class="bottom-actions">
      <el-button @click="confirmClose">关闭</el-button>
      <el-button v-if="!isReadonly" type="primary" :loading="submitLoading" @click="submitAndComplete">
        提交
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding: 18px 0 110px;
  background:
    linear-gradient(180deg, rgba(95, 156, 255, 0.1) 0%, rgba(95, 156, 255, 0) 180px),
    #f5f7fb;
}

.detail-shell {
  width: min(1120px, calc(100vw - 48px));
  margin: 0 auto;
}

.bottom-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px 24px 20px;
  background: rgba(245, 247, 251, 0.92);
  backdrop-filter: blur(10px);
}

@media (max-width: 980px) {
  .detail-page {
    padding-top: 14px;
  }

  .detail-shell {
    width: min(1120px, calc(100vw - 24px));
  }

  .bottom-actions {
    gap: 12px;
    padding: 14px 16px 18px;
  }
}
</style>
