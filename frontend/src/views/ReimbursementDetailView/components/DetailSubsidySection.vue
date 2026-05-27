//补助/费用明细
<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { Edit, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getSubsidyCalendarAPI,
  saveSubsidyCalendarAPI
} from '@/apis/reimbursement'
import SubsidyCalendarDialog from './SubsidyCalendarDialog.vue'
import {
  clampSubsidyAmount,
  toMoney
} from '../composables/reimbursementDetailShared'
import { validateSubsidyCalendarList } from '../composables/reimbursementDetailValidators'
import {
  buildCalendarListFromTrip,
  createCalendarRow,
  getSelectedActualAmount,
  getSelectedStandardAmount
} from '../composables/subsidyRows'

const props = defineProps({
  open: {
    type: Boolean,
    default: true
  },
  subsidyList: {
    type: Array,
    default: () => []
  },
  tripList: {
    type: Array,
    default: () => []
  },
  detailId: {
    type: [String, Number],
    default: ''
  },
  businessTypeName: {
    type: String,
    default: ''
  },
  cityOptions: {
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

const emit = defineEmits(['toggle', 'update:subsidyList', 'changed'])

const detailForm = reactive({
  id: '',
  businessTypeName: '',
  tripList: [],
  subsidyList: []
})

const subsidyDialogVisible = ref(false)
const subsidyDialogLoading = ref(false)
const subsidyDialogSaving = ref(false)
const editingSubsidyIndex = ref(-1)
const subsidyCalendarList = ref([])

const subsidyDialogMeta = reactive({
  subsidyId: '',
  businessTypeName: '',
  startDate: '',
  endDate: '',
  days: 0,
  route: '',
  subsidyAmount: '0.00',
  standardAmount: '0.00'
})

const cityMap = computed(() =>
  new Map(props.cityOptions.map((item) => [item.cityNo, item]))
)

const subsidyCalendarSummary = computed(() => {
  return subsidyCalendarList.value.reduce(
    (acc, item) => {
      acc.standardAmount += getSelectedStandardAmount(item)
      acc.subsidyAmount += getSelectedActualAmount(item)
      acc.mealAllowance += item.mealSelected ? Number(item.mealAmount || 0) : 0
      acc.transportationAllowance += item.transportSelected ? Number(item.transportAmount || 0) : 0
      acc.phoneAllowance += item.phoneSelected ? Number(item.phoneAmount || 0) : 0
      return acc
    },
    {
      standardAmount: 0,
      subsidyAmount: 0,
      mealAllowance: 0,
      transportationAllowance: 0,
      phoneAllowance: 0
    }
  )
})

watch(
  () => [props.detailId, props.businessTypeName, props.tripList, props.subsidyList],
  () => {
    detailForm.id = props.detailId
    detailForm.businessTypeName = props.businessTypeName
    detailForm.tripList = props.tripList.map((item) => ({ ...item }))
    detailForm.subsidyList = props.subsidyList.map((item) => ({ ...item }))
  },
  { deep: true, immediate: true }
)

function resetSubsidyDialogMeta() {
  subsidyDialogMeta.subsidyId = ''
  subsidyDialogMeta.businessTypeName = ''
  subsidyDialogMeta.startDate = ''
  subsidyDialogMeta.endDate = ''
  subsidyDialogMeta.days = 0
  subsidyDialogMeta.route = ''
  subsidyDialogMeta.subsidyAmount = '0.00'
  subsidyDialogMeta.standardAmount = '0.00'
}

function syncSubsidyMetaFromRow(row, trip) {
  const routeText =
    row.tripRoute ||
    [trip?.departureCityName, trip?.arrivalCityName].filter(Boolean).join('-')

  subsidyDialogMeta.subsidyId = row.subsidyId || trip?.subsidyId || ''
  subsidyDialogMeta.businessTypeName = detailForm.businessTypeName || ''
  subsidyDialogMeta.startDate = trip?.departureDate || row.startDate || ''
  subsidyDialogMeta.endDate = trip?.arrivalDate || row.endDate || ''
  subsidyDialogMeta.days = Number(row.subsidyDays || 0)
  subsidyDialogMeta.route = routeText || ''
  subsidyDialogMeta.subsidyAmount = toMoney(row.subsidyAmount || 0)
  subsidyDialogMeta.standardAmount = toMoney(row.applicationAmount || 0)
}

function syncSubsidyMetaFromSummary() {
  subsidyDialogMeta.subsidyAmount = toMoney(subsidyCalendarSummary.value.subsidyAmount)
  subsidyDialogMeta.standardAmount = toMoney(subsidyCalendarSummary.value.standardAmount)
}

function applyCalendarToSubsidyRow(index) {
  const row = detailForm.subsidyList[index]
  if (!row) return

  row.calendarList = subsidyCalendarList.value.map((item) => ({ ...item }))
  row.applicationAmount = toMoney(subsidyCalendarSummary.value.standardAmount)
  row.subsidyAmount = toMoney(subsidyCalendarSummary.value.subsidyAmount)
  row.mealAllowance = toMoney(subsidyCalendarSummary.value.mealAllowance)
  row.transportationAllowance = toMoney(subsidyCalendarSummary.value.transportationAllowance)
  row.phoneAllowance = toMoney(subsidyCalendarSummary.value.phoneAllowance)
  row.subsidyDays = subsidyCalendarList.value.length
  row.travelDateRange = `${subsidyDialogMeta.startDate} 至 ${subsidyDialogMeta.endDate}`
  row.tripRoute = subsidyDialogMeta.route
  row.subsidyCityName = subsidyCalendarList.value[0]?.subsidyCityName || row.subsidyCityName
}

async function openSubsidyDialog(row, index) {
  if (props.isReadonly || index < 0) return

  editingSubsidyIndex.value = index
  subsidyDialogVisible.value = true
  subsidyDialogLoading.value = true
  resetSubsidyDialogMeta()

  try {
    const trip = detailForm.tripList[index]
    syncSubsidyMetaFromRow(row, trip)

    const canLoadRemote = detailForm.id && row.subsidyId && !String(row.subsidyId).startsWith('LOCAL-')
    if (canLoadRemote) {
      const res = await getSubsidyCalendarAPI(detailForm.id, row.subsidyId)
      const remoteData = res?.data || {}
      subsidyCalendarList.value = (remoteData.calendarList || []).map((item) => createCalendarRow(item))
      subsidyDialogMeta.subsidyId = remoteData.subsidyId || subsidyDialogMeta.subsidyId
      subsidyDialogMeta.businessTypeName = remoteData.businessTypeName || subsidyDialogMeta.businessTypeName
      subsidyDialogMeta.startDate = remoteData.startDate || subsidyDialogMeta.startDate
      subsidyDialogMeta.endDate = remoteData.endDate || subsidyDialogMeta.endDate
      subsidyDialogMeta.days = Number(remoteData.days || subsidyCalendarList.value.length)
      subsidyDialogMeta.route = remoteData.route || subsidyDialogMeta.route
      subsidyDialogMeta.subsidyAmount = toMoney(remoteData.subsidyAmount || 0)
      subsidyDialogMeta.standardAmount = toMoney(remoteData.standardAmount || 0)
    } else {
      subsidyCalendarList.value = (row.calendarList?.length
        ? row.calendarList
        : buildCalendarListFromTrip(trip, cityMap.value)
      ).map((item) => createCalendarRow(item))
      syncSubsidyMetaFromSummary()
    }
  } catch (error) {
    subsidyDialogVisible.value = false
    ElMessage.error(error?.response?.data?.message || '获取补助信息失败')
  } finally {
    subsidyDialogLoading.value = false
  }
}

async function handleSaveSubsidyDialog(calendarList) {
  if (editingSubsidyIndex.value < 0) return

  subsidyCalendarList.value = calendarList.map((item) => ({ ...item }))

  const errorMessage = validateSubsidyCalendarList(subsidyCalendarList.value)
  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  subsidyDialogSaving.value = true

  try {
    const row = detailForm.subsidyList[editingSubsidyIndex.value]
    const payload = subsidyCalendarList.value.map((item) => ({
      calendarId: item.calendarId || undefined,
      mealSelected: item.mealSelected,
      mealAmount: Number(clampSubsidyAmount(item.mealAmount, item.mealStandard).toFixed(2)),
      transportSelected: item.transportSelected,
      transportAmount: Number(clampSubsidyAmount(item.transportAmount, item.transportStandard).toFixed(2)),
      phoneSelected: item.phoneSelected,
      phoneAmount: Number(clampSubsidyAmount(item.phoneAmount, item.phoneStandard).toFixed(2))
    }))

    if (detailForm.id && row?.subsidyId && !String(row.subsidyId).startsWith('LOCAL-')) {
      await saveSubsidyCalendarAPI(detailForm.id, row.subsidyId, payload)
    }

    applyCalendarToSubsidyRow(editingSubsidyIndex.value)
    syncSubsidyMetaFromSummary()
    subsidyDialogVisible.value = false
    emit('update:subsidyList', detailForm.subsidyList.map((item) => ({ ...item })))
    emit('changed')
    ElMessage.success('补助信息保存成功')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '补助信息保存失败')
  } finally {
    subsidyDialogSaving.value = false
  }
}
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
        content="1、请根据实际出差日期选择补助 2、出差期间当日有用餐安排的请自行核减当日餐补 3、出差期间当日有用车的，请自行核减当日交通补助"
        placement="top-start"
      >
        <div class="tip-banner">
          <el-icon><WarningFilled /></el-icon>
          <span class="tip-text">1、请根据实际出差日期选择补助 2、出差期间当日有用餐安排的请自行核减当日餐补 3、出差期间当日有用车的，请自行核减当日交通补助</span>
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
            <el-icon class="action-icon" :class="{ disabled: isReadonly }" @click="openSubsidyDialog(row, $index)">
              <Edit />
            </el-icon>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <SubsidyCalendarDialog
      v-model:visible="subsidyDialogVisible"
      :loading="subsidyDialogLoading"
      :saving="subsidyDialogSaving"
      :meta="subsidyDialogMeta"
      :calendar-list="subsidyCalendarList"
      :readonly="isReadonly"
      @save="handleSaveSubsidyDialog"
    />
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
