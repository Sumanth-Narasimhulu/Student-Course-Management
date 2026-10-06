package com.studentmanagement.dto.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class SasAccessDetails {
    private String accessUrl;
    private OffsetDateTime expiry;
    public SasAccessDetails(String accessUrl, OffsetDateTime expiry) {
        this.accessUrl = accessUrl;
        this.expiry = expiry;
    }
    public String getAccessUrl() {
        return accessUrl;
    }
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }
    public OffsetDateTime getExpiry() {
        return expiry;
    }
    public void setExpiry(OffsetDateTime expiry) {
        this.expiry = expiry;
    }
    
}
