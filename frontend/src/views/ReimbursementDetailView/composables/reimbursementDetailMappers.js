export function assignBaseDetailForm(detailForm, detail = {}) {
  detailForm.id = detail.id || ''
  detailForm.billNo = detail.billNo || ''
  detailForm.billStatus = detail.billStatus || '0'
  detailForm.creationTime = detail.creationTime || ''
  detailForm.reimbursementTitle = detail.reimbursementTitle || ''
  detailForm.businessTripReason = detail.businessTripReason || ''
  detailForm.reimburserId = detail.reimburserId || ''
  detailForm.reimburserNo = detail.reimburserNo || ''
  detailForm.reimburserName = detail.reimburserName || ''
  detailForm.reimDepartmentId = detail.reimDepartmentId || ''
  detailForm.reimDepartmentNo = detail.reimDepartmentNo || ''
  detailForm.reimDepartmentName = detail.reimDepartmentName || ''
  detailForm.reimCompanyId = detail.reimCompanyId || ''
  detailForm.reimCompanyNo = detail.reimCompanyNo || ''
  detailForm.reimCompanyName = detail.reimCompanyName || ''
  detailForm.businessTypeId = detail.businessTypeId || ''
  detailForm.businessTypeNo = detail.businessTypeNo || ''
  detailForm.businessTypeName = detail.businessTypeName || ''
  detailForm.remarks = detail.remarks || ''
}

export function mapTripListFromDetail(tripList = [], {
  cityOptions,
  employeeOptions
}) {
  return tripList.map((item) => {
    const [departureDate, arrivalDate] = String(item.tripDateRange || '').split('至').map((part) => part.trim())
    const [departureCityName, arrivalCityName] = String(item.tripRoute || '').split('-')
    const departureCity = cityOptions.find((option) => option.cityName === departureCityName)
    const arrivalCity = cityOptions.find((option) => option.cityName === arrivalCityName)
    const traveler = employeeOptions.find((option) => option.reimburserId === item.travelerId)

    return {
      tripId: item.tripId,
      subsidyId: item.subsidyId,
      travelerId: item.travelerId,
      travelerNo: traveler?.reimburserNo || '',
      travelerName: item.travelerName,
      departureCityNo: departureCity?.cityNo || '',
      departureCityName: departureCityName || '',
      arrivalCityNo: arrivalCity?.cityNo || '',
      arrivalCityName: arrivalCityName || '',
      departureDate: departureDate || '',
      arrivalDate: arrivalDate || '',
      tripDescription: item.tripDescription || ''
    }
  })
}

export function mapAllocationListFromDetail(allocationList = [], createAllocationRow) {
  return allocationList.map((item) =>
    createAllocationRow({
      allocationId: item.allocationId,
      reimCompanyId: item.reimCompanyId,
      reimCompanyNo: item.reimCompanyNo,
      reimCompanyName: item.reimCompanyName,
      projectId: item.projectId,
      projectNo: item.projectNo,
      projectName: item.projectName,
      allocationRatioPercent: Number(item.allocationRatio || 0) * 100,
      allocationAmount: Number(item.allocationAmount || 0)
    })
  )
}
