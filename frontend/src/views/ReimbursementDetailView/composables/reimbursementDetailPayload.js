export function buildTripPayloadList(tripList = []) {
  return tripList.map((item) => ({
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

export function buildSubmitPayload(detailForm, allocationList = []) {
  return {
    reimbursementTitle: detailForm.reimbursementTitle,
    businessTripReason: detailForm.businessTripReason,
    reimburserId: detailForm.reimburserId,
    reimburserNo: detailForm.reimburserNo,
    reimburserName: detailForm.reimburserName,
    reimDepartmentId: detailForm.reimDepartmentId,
    reimDepartmentNo: detailForm.reimDepartmentNo,
    reimDepartmentName: detailForm.reimDepartmentName,
    reimCompanyId: detailForm.reimCompanyId,
    reimCompanyNo: detailForm.reimCompanyNo,
    reimCompanyName: detailForm.reimCompanyName,
    businessTypeId: detailForm.businessTypeId,
    businessTypeNo: detailForm.businessTypeNo,
    businessTypeName: detailForm.businessTypeName,
    tripList: buildTripPayloadList(detailForm.tripList),
    allocationList,
    remarks: detailForm.remarks
  }
}
