package org.example.reimcloud.common;

public final class BillStatus {

    public static final String DRAFT = "0";
    public static final String COMPLETED = "1";
    public static final String VOIDED = "2";

    private BillStatus() {
    }

    public static String nameOf(String status) {
        if (DRAFT.equals(status)) {
            return "草稿";
        }
        if (COMPLETED.equals(status)) {
            return "已完成";
        }
        if (VOIDED.equals(status)) {
            return "已作废";
        }
        return "未知";
    }
}
