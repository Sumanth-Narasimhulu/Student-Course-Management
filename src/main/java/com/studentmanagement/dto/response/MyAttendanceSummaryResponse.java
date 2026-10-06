package com.studentmanagement.dto.response;

import java.util.ArrayList;
import java.util.List;

public class MyAttendanceSummaryResponse {
    private List<MyAttendanceSummaryResponseInner> summary = new ArrayList<>();
    private Double overallPercentage;

    public List<MyAttendanceSummaryResponseInner> getSummary() {
        return summary;
    }

    public void setSummary(List<MyAttendanceSummaryResponseInner> summary) {
        this.summary = summary;
    }

    public Double getOverallPercentage() {
        return overallPercentage;
    }

    public void setOverallPercentage(Double overallPercentage) {
        this.overallPercentage = overallPercentage;
    }
}
