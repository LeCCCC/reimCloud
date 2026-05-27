<script setup>
import { reactive, ref, watch } from 'vue'
import { CopyDocument, Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import TripEditorDialog from './TripEditorDialog.vue'
import {
  validateTripDuplicate,
  validateTripForm
} from '../composables/reimbursementDetailValidators'

const props = defineProps({
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

const emit = defineEmits(['toggle', 'update:tripList', 'changed'])

const tripDialogVisible = ref(false)
const tripDialogMode = ref('create')
const editingTripIndex = ref(-1)

const tripDialogForm = reactive({
  travelerId: '',
  travelerNo: '',
  travelerName: '',
  departureCityNo: '',
  departureCityName: '',
  arrivalCityNo: '',
  arrivalCityName: '',
  departureDate: '',
  arrivalDate: '',
  tripDescription: ''
})

watch(
  () => tripDialogForm.travelerId,
  (value) => {
    const matched = props.employeeOptions.find((item) => item.reimburserId === value)
    if (!matched) return
    tripDialogForm.travelerNo = matched.reimburserNo
    tripDialogForm.travelerName = matched.reimburserName
  }
)

watch(
  () => tripDialogForm.departureCityNo,
  (value) => {
    const matched = props.cityOptions.find((item) => item.cityNo === value)
    tripDialogForm.departureCityName = matched?.cityName || ''
  }
)

watch(
  () => tripDialogForm.arrivalCityNo,
  (value) => {
    const matched = props.cityOptions.find((item) => item.cityNo === value)
    tripDialogForm.arrivalCityName = matched?.cityName || ''
  }
)

function resetTripDialogForm() {
  tripDialogForm.travelerId = ''
  tripDialogForm.travelerNo = ''
  tripDialogForm.travelerName = ''
  tripDialogForm.departureCityNo = ''
  tripDialogForm.departureCityName = ''
  tripDialogForm.arrivalCityNo = ''
  tripDialogForm.arrivalCityName = ''
  tripDialogForm.departureDate = ''
  tripDialogForm.arrivalDate = ''
  tripDialogForm.tripDescription = ''
}

function fillTripDialogForm(row) {
  tripDialogForm.travelerId = row.travelerId || ''
  tripDialogForm.travelerNo = row.travelerNo || ''
  tripDialogForm.travelerName = row.travelerName || ''
  tripDialogForm.departureCityNo = row.departureCityNo || ''
  tripDialogForm.departureCityName = row.departureCityName || ''
  tripDialogForm.arrivalCityNo = row.arrivalCityNo || ''
  tripDialogForm.arrivalCityName = row.arrivalCityName || ''
  tripDialogForm.departureDate = row.departureDate || ''
  tripDialogForm.arrivalDate = row.arrivalDate || ''
  tripDialogForm.tripDescription = row.tripDescription || ''
}

function openCreateTripDialog() {
  if (props.isReadonly) return
  tripDialogMode.value = 'create'
  editingTripIndex.value = -1
  resetTripDialogForm()
  tripDialogVisible.value = true
}

function openEditTripDialog(row, index) {
  if (props.isReadonly) return
  tripDialogMode.value = 'edit'
  editingTripIndex.value = index
  fillTripDialogForm(row)
  tripDialogVisible.value = true
}

function openCopyTripDialog(row) {
  if (props.isReadonly) return
  tripDialogMode.value = 'create'
  editingTripIndex.value = -1
  fillTripDialogForm(row)
  tripDialogVisible.value = true
}

function buildTripDraft() {
  return {
    travelerId: tripDialogForm.travelerId,
    travelerNo: tripDialogForm.travelerNo,
    travelerName: tripDialogForm.travelerName,
    departureCityNo: tripDialogForm.departureCityNo,
    departureCityName: tripDialogForm.departureCityName,
    arrivalCityNo: tripDialogForm.arrivalCityNo,
    arrivalCityName: tripDialogForm.arrivalCityName,
    departureDate: tripDialogForm.departureDate,
    arrivalDate: tripDialogForm.arrivalDate,
    tripDescription: tripDialogForm.tripDescription
  }
}

function updateTripList(nextList) {
  emit('update:tripList', nextList)
  emit('changed')
}

function handleTripDialogSubmit() {
  const trip = buildTripDraft()
  const tripError = validateTripForm(trip)
  if (tripError) {
    ElMessage.warning(tripError)
    return
  }

  const duplicateError = validateTripDuplicate(
    props.tripList,
    trip,
    editingTripIndex.value,
    tripDialogMode.value
  )

  if (duplicateError) {
    ElMessage.warning(duplicateError)
    return
  }

  const payload = {
    tripId:
      tripDialogMode.value === 'edit' && editingTripIndex.value > -1
        ? props.tripList[editingTripIndex.value].tripId
        : '',
    subsidyId:
      tripDialogMode.value === 'edit' && editingTripIndex.value > -1
        ? props.tripList[editingTripIndex.value].subsidyId
        : '',
    ...trip
  }

  const nextList = [...props.tripList]
  if (tripDialogMode.value === 'edit' && editingTripIndex.value > -1) {
    nextList.splice(editingTripIndex.value, 1, payload)
  } else {
    nextList.push(payload)
  }

  updateTripList(nextList)
  tripDialogVisible.value = false
}

async function handleDeleteTrip(index) {
  await ElMessageBox.confirm('确定删除当前补录行程吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })

  const nextList = [...props.tripList]
  nextList.splice(index, 1)
  updateTripList(nextList)
}
</script>

<template>
  <section class="section-card">
    <div class="section-header" @click="$emit('toggle')">
      <div class="section-title">补录行程</div>
      <div class="section-actions" @click.stop>
        <el-button v-if="!isReadonly" link type="primary" @click="openCreateTripDialog">
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
              <el-icon class="action-icon" @click="openEditTripDialog(row, $index)">
                <Edit />
              </el-icon>
              <el-icon class="action-icon" @click="openCopyTripDialog(row)">
                <CopyDocument />
              </el-icon>
              <el-icon class="action-icon danger" @click="handleDeleteTrip($index)">
                <Delete />
              </el-icon>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <TripEditorDialog
      v-model:visible="tripDialogVisible"
      :mode="tripDialogMode"
      :form="tripDialogForm"
      :employee-options="employeeOptions"
      :city-options="cityOptions"
      @submit="handleTripDialogSubmit"
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
