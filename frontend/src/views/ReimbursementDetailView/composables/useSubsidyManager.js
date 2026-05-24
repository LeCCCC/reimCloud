import { computed, reactive, ref } from 'vue'
import {
  clampSubsidyAmount,
  getMealStandardByCityType,
  getTripDays,
  getWeekName
} from './reimbursementDetailShared'
import { validateSubsidyCalendarList } from './reimbursementDetailValidators'

export function useSubsidyManager({
  detailForm,
  cityMap,
  isReadonly,
  normalizeAllocationRows,
  toMoney,
  getSubsidyCalendarAPI,
  saveSubsidyCalendarAPI,
  ElMessage
}) {
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

  function getSelectedStandardAmount(item) {
    return (item.mealSelected ? Number(item.mealStandard || 0) : 0) +
      (item.transportSelected ? Number(item.transportStandard || 0) : 0) +
      (item.phoneSelected ? Number(item.phoneStandard || 0) : 0)
  }

  function getSelectedActualAmount(item) {
    return (item.mealSelected ? Number(item.mealAmount || 0) : 0) +
      (item.transportSelected ? Number(item.transportAmount || 0) : 0) +
      (item.phoneSelected ? Number(item.phoneAmount || 0) : 0)
  }

  function createCalendarRow(overrides = {}) {
    const mealStandard = Number(overrides.mealStandard ?? getMealStandardByCityType(overrides.cityType))
    const transportStandard = Number(overrides.transportStandard ?? 40)
    const phoneStandard = Number(overrides.phoneStandard ?? 40)

    const mealSelected = overrides.mealSelected ?? true
    const transportSelected = overrides.transportSelected ?? true
    const phoneSelected = overrides.phoneSelected ?? true

    return {
      calendarId: overrides.calendarId || '',
      travelDate: overrides.travelDate || '',
      weekName: overrides.weekName || getWeekName(overrides.travelDate),
      subsidyCityName: overrides.subsidyCityName || '',
      cityType: overrides.cityType || '3',
      mealSelected,
      mealStandard,
      mealAmount: clampSubsidyAmount(
        overrides.mealAmount ?? (mealSelected ? mealStandard : 0),
        mealStandard
      ),
      transportSelected,
      transportStandard,
      transportAmount: clampSubsidyAmount(
        overrides.transportAmount ?? (transportSelected ? transportStandard : 0),
        transportStandard
      ),
      phoneSelected,
      phoneStandard,
      phoneAmount: clampSubsidyAmount(
        overrides.phoneAmount ?? (phoneSelected ? phoneStandard : 0),
        phoneStandard
      )
    }
  }

  function buildCalendarListFromTrip(trip) {
    if (!trip?.departureDate || !trip?.arrivalDate) return []

    const arrivalCity = cityMap.value.get(trip.arrivalCityNo) || {}
    const cityType = arrivalCity.cityType || '3'
    const subsidyCityName = trip.arrivalCityName || arrivalCity.cityName || ''
    const calendarList = []
    let current = window.dayjs ? window.dayjs(trip.departureDate) : null
    const dayjsLib = current?.constructor || null
    current = dayjsLib ? current : null
    const localDayjs = dayjsLib || null
    const start = localDayjs ? localDayjs(trip.departureDate) : null
    const end = localDayjs ? localDayjs(trip.arrivalDate) : null

    if (!start || !end) {
      const days = getTripDays(trip.departureDate, trip.arrivalDate)
      for (let offset = 0; offset < days; offset += 1) {
        const travelDate = new Date(trip.departureDate)
        travelDate.setDate(travelDate.getDate() + offset)
        const formatted = travelDate.toISOString().slice(0, 10)
        calendarList.push(
          createCalendarRow({
            travelDate: formatted,
            weekName: getWeekName(formatted),
            subsidyCityName,
            cityType
          })
        )
      }
      return calendarList
    }

    let cursor = start
    while (cursor.isBefore(end) || cursor.isSame(end, 'day')) {
      const travelDate = cursor.format('YYYY-MM-DD')
      calendarList.push(
        createCalendarRow({
          travelDate,
          weekName: getWeekName(travelDate),
          subsidyCityName,
          cityType
        })
      )
      cursor = cursor.add(1, 'day')
    }

    return calendarList
  }

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

  function createSubsidyRowFromCalendar(trip, index, calendarSource = []) {
    const calendarList = calendarSource.length
      ? calendarSource.map((item) => ({ ...createCalendarRow(item) }))
      : buildCalendarListFromTrip(trip)

    const summary = calendarList.reduce(
      (acc, item) => {
        acc.applicationAmount += getSelectedStandardAmount(item)
        acc.subsidyAmount += getSelectedActualAmount(item)
        acc.mealAllowance += item.mealSelected ? Number(item.mealAmount || 0) : 0
        acc.transportationAllowance += item.transportSelected ? Number(item.transportAmount || 0) : 0
        acc.phoneAllowance += item.phoneSelected ? Number(item.phoneAmount || 0) : 0
        return acc
      },
      {
        applicationAmount: 0,
        subsidyAmount: 0,
        mealAllowance: 0,
        transportationAllowance: 0,
        phoneAllowance: 0
      }
    )

    return {
      subsidyId: trip.subsidyId || `LOCAL-SUB-${index + 1}`,
      travelerId: trip.travelerId,
      travelerName: trip.travelerName,
      startDate: trip.departureDate,
      endDate: trip.arrivalDate,
      travelDateRange: `${trip.departureDate} 至 ${trip.arrivalDate}`,
      subsidyDays: calendarList.length,
      tripRoute: `${trip.departureCityName}-${trip.arrivalCityName}`,
      subsidyCityName: trip.arrivalCityName,
      applicationAmount: toMoney(summary.applicationAmount),
      subsidyAmount: toMoney(summary.subsidyAmount),
      mealAllowance: toMoney(summary.mealAllowance),
      transportationAllowance: toMoney(summary.transportationAllowance),
      phoneAllowance: toMoney(summary.phoneAllowance),
      calendarList
    }
  }

  function rebuildSubsidyList() {
    detailForm.subsidyList = detailForm.tripList.map((item, index) =>
      createSubsidyRowFromCalendar(item, index)
    )
    normalizeAllocationRows()
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
    normalizeAllocationRows()
  }

  async function openSubsidyDialog(row, index) {
    if (isReadonly.value || index < 0) return

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
        subsidyCalendarList.value = (row.calendarList?.length ? row.calendarList : buildCalendarListFromTrip(trip))
          .map((item) => createCalendarRow(item))
        syncSubsidyMetaFromSummary()
      }
    } catch (error) {
      subsidyDialogVisible.value = false
      ElMessage.error(error?.response?.data?.message || '获取补助信息失败')
    } finally {
      subsidyDialogLoading.value = false
    }
  }

  async function saveSubsidyDialog() {
    if (editingSubsidyIndex.value < 0) return

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
      ElMessage.success('补助信息保存成功')
    } catch (error) {
      ElMessage.error(error?.response?.data?.message || '补助信息保存失败')
    } finally {
      subsidyDialogSaving.value = false
    }
  }

  return {
    subsidyDialogVisible,
    subsidyDialogLoading,
    subsidyDialogSaving,
    subsidyCalendarList,
    subsidyDialogMeta,
    createSubsidyRowFromCalendar,
    rebuildSubsidyList,
    openSubsidyDialog,
    saveSubsidyDialog
  }
}
