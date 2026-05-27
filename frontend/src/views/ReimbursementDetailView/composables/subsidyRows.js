//补助/日历纯算法
import {
  clampSubsidyAmount,
  getMealStandardByCityType,
  getTripDays,
  getWeekName
} from './reimbursementDetailShared'

export function getSelectedStandardAmount(item) {
  return (item.mealSelected ? Number(item.mealStandard || 0) : 0) +
    (item.transportSelected ? Number(item.transportStandard || 0) : 0) +
    (item.phoneSelected ? Number(item.phoneStandard || 0) : 0)
}

export function getSelectedActualAmount(item) {
  return (item.mealSelected ? Number(item.mealAmount || 0) : 0) +
    (item.transportSelected ? Number(item.transportAmount || 0) : 0) +
    (item.phoneSelected ? Number(item.phoneAmount || 0) : 0)
}

export function createCalendarRow(overrides = {}) {
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

export function buildCalendarListFromTrip(trip, cityMap) {
  if (!trip?.departureDate || !trip?.arrivalDate) return []

  const arrivalCity = cityMap.get(trip.arrivalCityNo) || {}
  const cityType = arrivalCity.cityType || '3'
  const subsidyCityName = trip.arrivalCityName || arrivalCity.cityName || ''
  const calendarList = []
  const dayjsLib = window.dayjs || null

  if (!dayjsLib) {
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

  let cursor = dayjsLib(trip.departureDate)
  const end = dayjsLib(trip.arrivalDate)

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

export function createSubsidyRowFromCalendar(trip, index, calendarSource = [], cityMap, toMoney) {
  const calendarList = calendarSource.length
    ? calendarSource.map((item) => ({ ...createCalendarRow(item) }))
    : buildCalendarListFromTrip(trip, cityMap)

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

export function buildSubsidyListFromTrips(tripList, cityMap, toMoney) {
  return tripList.map((item, index) =>
    createSubsidyRowFromCalendar(item, index, [], cityMap, toMoney)
  )
}
