<script setup>
import { computed, ref, watch } from 'vue'
import { clampSubsidyAmount } from '../composables/reimbursementDetailShared'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  saving: {
    type: Boolean,
    default: false
  },
  meta: {
    type: Object,
    required: true
  },
  calendarList: {
    type: Array,
    default: () => []
  },
  readonly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'save'])

const subsidyColumns = [
  { key: 'meal', label: '餐费补助' },
  { key: 'transport', label: '交通补助' },
  { key: 'phone', label: '通讯补助' }
]

const localCalendarList = ref([])

function normalizeSelectedValue(value, fallback = true) {
  if (value === undefined || value === null || value === '') {
    return fallback
  }

  if (typeof value === 'boolean') {
    return value
  }

  if (typeof value === 'number') {
    return value === 1
  }

  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (['1', 'true', 'y', 'yes'].includes(normalized)) return true
    if (['0', 'false', 'n', 'no'].includes(normalized)) return false
  }

  return Boolean(value)
}

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const displayDays = computed(() => localCalendarList.value.length || Number(props.meta.days || 0))
const routeText = computed(() => props.meta.route || '-')

const summary = computed(() =>
  localCalendarList.value.reduce(
    (acc, item) => {
      subsidyColumns.forEach(({ key }) => {
        if (!item[`${key}Selected`]) return
        acc.standardAmount += Number(item[`${key}Standard`] || 0)
        acc.subsidyAmount += Number(item[`${key}Amount`] || 0)
      })
      return acc
    },
    {
      standardAmount: 0,
      subsidyAmount: 0
    }
  )
)

const allChecked = computed(() =>
  localCalendarList.value.length > 0 &&
  localCalendarList.value.every((row) => subsidyColumns.every((column) => row[`${column.key}Selected`]))
)

watch(
  () => [props.visible, props.calendarList],
  ([visible]) => {
    if (!visible) return
    localCalendarList.value = cloneCalendarList(props.calendarList)
  },
  { deep: true, immediate: true }
)

function cloneCalendarList(list = []) {
  return list.map((item) => ({
    ...item,
    mealSelected: normalizeSelectedValue(item.mealSelected),
    transportSelected: normalizeSelectedValue(item.transportSelected),
    phoneSelected: normalizeSelectedValue(item.phoneSelected)
  }))
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

function formatStandard(value) {
  return `CNY ${formatMoney(value)} / 天`
}

function isColumnChecked(key) {
  return localCalendarList.value.length > 0 &&
    localCalendarList.value.every((row) => row[`${key}Selected`])
}

function isRowChecked(row) {
  return subsidyColumns.every((column) => row[`${column.key}Selected`])
}

function ensureAmountInRange(row, key) {
  const amountKey = `${key}Amount`
  const standardKey = `${key}Standard`
  row[amountKey] = clampSubsidyAmount(row[amountKey], row[standardKey])
}

function updateSelection(row, key, checked) {
  const selectedKey = `${key}Selected`
  const amountKey = `${key}Amount`
  const standardKey = `${key}Standard`

  row[selectedKey] = checked

  if (!checked) {
    row[amountKey] = clampSubsidyAmount(row[amountKey], row[standardKey])
    return
  }

  const standardAmount = Number(row[standardKey] || 0)
  const currentAmount = Number(row[amountKey] || 0)
  row[amountKey] = currentAmount > 0
    ? clampSubsidyAmount(currentAmount, standardAmount)
    : standardAmount
}

function handleAllChange(checked) {
  localCalendarList.value.forEach((row) => {
    subsidyColumns.forEach((column) => updateSelection(row, column.key, checked))
  })
}

function handleColumnChange(key, checked) {
  localCalendarList.value.forEach((row) => updateSelection(row, key, checked))
  }

function handleRowChange(row, checked) {
  subsidyColumns.forEach((column) => updateSelection(row, column.key, checked))
}

function handleSave() {
  emit('save', cloneCalendarList(localCalendarList.value))
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="补助日历"
    width="1240px"
    destroy-on-close
    class="subsidy-dialog"
  >
    <div v-loading="loading" class="subsidy-dialog__body">
      <aside class="subsidy-dialog__sidebar">
        <section class="meta-card">
          <div class="meta-card__title">出差类型</div>
          <div class="meta-card__value">{{ meta.businessTypeName || '-' }}</div>

          <div class="timeline">
            <div class="timeline__row">
              <span class="timeline__label">开始日期</span>
              <span class="timeline__date">{{ meta.startDate || '-' }}</span>
            </div>
            <div class="timeline__center">
              <span class="timeline__dot"></span>
              <span class="timeline__line"></span>
              <span class="timeline__route">{{ routeText }} {{ displayDays }}天</span>
              <span class="timeline__line"></span>
              <span class="timeline__dot"></span>
            </div>
            <div class="timeline__row">
              <span class="timeline__label">结束日期</span>
              <span class="timeline__date">{{ meta.endDate || '-' }}</span>
            </div>
          </div>
        </section>

        <section class="summary-card">
          <!-- <div class="summary-card__row">
            <span>申请金额</span>
            <strong>CNY {{ formatMoney(summary.standardAmount) }}</strong>
          </div> -->
          <div class="summary-card__row">
            <span>标准总额</span>
            <strong>CNY {{ formatMoney(summary.standardAmount) }}</strong>
          </div>
          <div class="summary-card__row">
            <span>补助金额</span>
            <strong>CNY {{ formatMoney(summary.subsidyAmount) }}</strong>
          </div>
        </section>
      </aside>

      <section class="subsidy-dialog__content">
        <div class="table-toolbar">
          <div class="table-toolbar__title">出差补助</div>
          <el-checkbox
            :model-value="allChecked"
            :disabled="readonly || !localCalendarList.length"
            @change="handleAllChange"
          >
            全选
          </el-checkbox>
        </div>

        <el-table :data="localCalendarList" border class="calendar-table">
          <el-table-column label="出差日期" min-width="170">
            <template #default="{ row }">
              <div class="date-cell">
                <div class="date-cell__text">
                  <div>{{ row.travelDate || '-' }}</div>
                  <div class="date-cell__week">{{ row.weekName || '-' }}</div>
                </div>
                <el-checkbox
                  :model-value="isRowChecked(row)"
                  :disabled="readonly"
                  @change="handleRowChange(row, $event)"
                />
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="subsidyCityName" label="补助城市" min-width="120" />

          <el-table-column
            v-for="column in subsidyColumns"
            :key="column.key"
            min-width="220"
          >
            <template #header>
              <div class="subsidy-header">
                <span>{{ column.label }}</span>
                <el-checkbox
                  :model-value="isColumnChecked(column.key)"
                  :disabled="readonly || !localCalendarList.length"
                  @change="handleColumnChange(column.key, $event)"
                />
              </div>
            </template>

            <template #default="{ row }">
              <div class="subsidy-cell">
                <div class="subsidy-cell__standard">
                  {{ formatStandard(row[`${column.key}Standard`]) }}
                </div>
                <div class="subsidy-cell__editor">
                  <el-checkbox
                    :model-value="row[`${column.key}Selected`]"
                    :disabled="readonly"
                    @change="updateSelection(row, column.key, $event)"
                  />
                  <el-input-number
                    v-model="row[`${column.key}Amount`]"
                    :min="0"
                    :max="Number(row[`${column.key}Standard`] || 0)"
                    :precision="2"
                    :step="1"
                    controls-position="right"
                    :disabled="readonly || !row[`${column.key}Selected`]"
                    @blur="ensureAmountInRange(row, column.key)"
                    @change="ensureAmountInRange(row, column.key)"
                  />
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="readonly" @click="handleSave">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.subsidy-dialog :deep(.el-dialog__body) {
  padding-top: 12px;
}

.subsidy-dialog__body {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  min-height: 520px;
}

.subsidy-dialog__sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.meta-card,
.summary-card {
  border: 1px solid #e6ebf3;
  border-radius: 12px;
  background: #fff;
}

.meta-card {
  padding: 16px;
}

.meta-card__title {
  margin-bottom: 6px;
  color: #5f6b7c;
  font-size: 13px;
}

.meta-card__value {
  margin-bottom: 16px;
  color: #f08a24;
  font-size: 24px;
  font-weight: 700;
}

.timeline {
  display: grid;
  gap: 10px;
}

.timeline__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.timeline__label {
  color: #7b8798;
  font-size: 13px;
}

.timeline__date {
  color: #334155;
  font-weight: 600;
}

.timeline__center {
  display: grid;
  grid-template-columns: 12px 1fr auto 1fr 12px;
  align-items: center;
  gap: 10px;
}

.timeline__dot {
  width: 12px;
  height: 12px;
  border: 3px solid #1d8df2;
  border-radius: 999px;
  box-sizing: border-box;
}

.timeline__line {
  height: 2px;
  background: linear-gradient(90deg, #7cc4ff, #1d8df2);
}

.timeline__route {
  padding: 5px 10px;
  border-radius: 999px;
  background: #1d8df2;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.summary-card {
  padding: 14px 16px;
}

.summary-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  color: #5f6b7c;
}

.summary-card__row + .summary-card__row {
  border-top: 1px dashed #e8edf5;
}

.summary-card__row strong {
  color: #f08a24;
  font-size: 22px;
  font-weight: 700;
}

.subsidy-dialog__content {
  min-width: 0;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.table-toolbar__title {
  color: #334155;
  font-size: 18px;
  font-weight: 700;
}

.calendar-table {
  width: 100%;
}

.date-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.date-cell__text {
  line-height: 1.5;
}

.date-cell__week {
  color: #7b8798;
  font-size: 12px;
}

.subsidy-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.subsidy-cell {
  display: grid;
  gap: 10px;
}

.subsidy-cell__standard {
  color: #ff7a00;
  font-size: 13px;
}

.subsidy-cell__editor {
  display: flex;
  align-items: center;
  gap: 10px;
}

.subsidy-cell__editor :deep(.el-input-number) {
  width: 148px;
}

.subsidy-cell__editor :deep(.el-input-number.is-controls-right .el-input__wrapper) {
  padding-right: 42px;
}

.subsidy-cell__editor :deep(.el-input-number__increase),
.subsidy-cell__editor :deep(.el-input-number__decrease) {
  width: 34px;
  color: #6b7280;
  background: #fff;
  border-left: 1px solid #d7deea;
}

.subsidy-cell__editor :deep(.el-input-number__increase.is-disabled),
.subsidy-cell__editor :deep(.el-input-number__decrease.is-disabled) {
  color: #c0c4cc;
  background: #f5f7fa;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 1280px) {
  .subsidy-dialog__body {
    grid-template-columns: 1fr;
  }
}
</style>
