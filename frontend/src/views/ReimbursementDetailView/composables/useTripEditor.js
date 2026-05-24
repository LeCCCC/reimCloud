import { reactive, ref, watch } from 'vue'
import {
  validateTripDuplicate,
  validateTripForm
} from './reimbursementDetailValidators'

export function useTripEditor({
  detailForm,
  isReadonly,
  employeeMap,
  cityMap,
  parseDateRange,
  rebuildSubsidyList,
  ElMessage,
  ElMessageBox
}) {
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
    dateRange: [],
    tripDescription: ''
  })

  function resetTripDialogForm() {
    tripDialogForm.travelerId = ''
    tripDialogForm.travelerNo = ''
    tripDialogForm.travelerName = ''
    tripDialogForm.departureCityNo = ''
    tripDialogForm.departureCityName = ''
    tripDialogForm.arrivalCityNo = ''
    tripDialogForm.arrivalCityName = ''
    tripDialogForm.dateRange = []
    tripDialogForm.tripDescription = ''
  }

  function fillTripDialogForm(row) {
    tripDialogForm.travelerId = row.travelerId
    tripDialogForm.travelerNo = row.travelerNo
    tripDialogForm.travelerName = row.travelerName
    tripDialogForm.departureCityNo = row.departureCityNo
    tripDialogForm.departureCityName = row.departureCityName
    tripDialogForm.arrivalCityNo = row.arrivalCityNo
    tripDialogForm.arrivalCityName = row.arrivalCityName
    tripDialogForm.dateRange = [row.departureDate, row.arrivalDate]
    tripDialogForm.tripDescription = row.tripDescription
  }

  function openCreateTripDialog() {
    if (isReadonly.value) return
    tripDialogMode.value = 'create'
    editingTripIndex.value = -1
    resetTripDialogForm()
    tripDialogVisible.value = true
  }

  function openEditTripDialog(row, index) {
    if (isReadonly.value) return
    tripDialogMode.value = 'edit'
    editingTripIndex.value = index
    fillTripDialogForm(row)
    tripDialogVisible.value = true
  }

  function openCopyTripDialog(row) {
    if (isReadonly.value) return
    tripDialogMode.value = 'create'
    editingTripIndex.value = -1
    fillTripDialogForm(row)
    tripDialogVisible.value = true
  }

  watch(
    () => tripDialogForm.travelerId,
    (value) => {
      const matched = employeeMap.value.get(value)
      if (!matched) return
      tripDialogForm.travelerNo = matched.reimburserNo
      tripDialogForm.travelerName = matched.reimburserName
    }
  )

  watch(
    () => tripDialogForm.departureCityNo,
    (value) => {
      const matched = cityMap.value.get(value)
      tripDialogForm.departureCityName = matched?.cityName || ''
    }
  )

  watch(
    () => tripDialogForm.arrivalCityNo,
    (value) => {
      const matched = cityMap.value.get(value)
      tripDialogForm.arrivalCityName = matched?.cityName || ''
    }
  )

  function buildTripPayloadFromDialog() {
    const dateRange = parseDateRange(tripDialogForm.dateRange)
    return {
      tripId:
        tripDialogMode.value === 'edit' && editingTripIndex.value > -1
          ? detailForm.tripList[editingTripIndex.value].tripId
          : '',
      subsidyId:
        tripDialogMode.value === 'edit' && editingTripIndex.value > -1
          ? detailForm.tripList[editingTripIndex.value].subsidyId
          : '',
      travelerId: tripDialogForm.travelerId,
      travelerNo: tripDialogForm.travelerNo,
      travelerName: tripDialogForm.travelerName,
      departureCityNo: tripDialogForm.departureCityNo,
      departureCityName: tripDialogForm.departureCityName,
      arrivalCityNo: tripDialogForm.arrivalCityNo,
      arrivalCityName: tripDialogForm.arrivalCityName,
      departureDate: dateRange.departureDate,
      arrivalDate: dateRange.arrivalDate,
      tripDescription: tripDialogForm.tripDescription
    }
  }

  function validateTripDialog() {
    const trip = buildTripPayloadFromDialog()

    const tripError = validateTripForm(trip)
    if (tripError) return tripError

    return validateTripDuplicate(
      detailForm.tripList,
      trip,
      editingTripIndex.value,
      tripDialogMode.value
    )
  }

  function handleTripDialogSubmit() {
    const errorMessage = validateTripDialog()
    if (errorMessage) {
      ElMessage.warning(errorMessage)
      return
    }

    const payload = buildTripPayloadFromDialog()
    if (tripDialogMode.value === 'edit' && editingTripIndex.value > -1) {
      detailForm.tripList.splice(editingTripIndex.value, 1, payload)
    } else {
      detailForm.tripList.push(payload)
    }

    rebuildSubsidyList()
    tripDialogVisible.value = false
  }

  async function handleDeleteTrip(index) {
    await ElMessageBox.confirm('确定删除当前补录行程吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    detailForm.tripList.splice(index, 1)
    rebuildSubsidyList()
  }

  function buildTripPayloadList() {
    return detailForm.tripList.map((item) => ({
      travelerId: item.travelerId,
      travelerNo: item.travelerNo,
      travelerName: item.travelerName,
      departureCityNo: item.departureCityNo,
      departureCityName: item.departureCityName,
      arrivalCityNo: item.arrivalCityNo,
      arrivalCityName: item.arrivalCityName,
      departureDate: item.departureDate,
      arrivalDate: item.arrivalDate,
      tripDescription: item.tripDescription
    }))
  }

  return {
    tripDialogVisible,
    tripDialogMode,
    tripDialogForm,
    openCreateTripDialog,
    openEditTripDialog,
    openCopyTripDialog,
    handleTripDialogSubmit,
    handleDeleteTrip,
    buildTripPayloadList
  }
}
