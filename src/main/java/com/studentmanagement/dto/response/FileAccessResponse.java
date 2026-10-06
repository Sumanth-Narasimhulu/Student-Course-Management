package com.studentmanagement.dto.response;


import java.time.OffsetDateTime;

public class FileAccessResponse {
    private Long fileAccessId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String accessFile;
    private OffsetDateTime expiresAt;
    public Long getFileAccessId() {
        return fileAccessId;
    }
    public void setFileAccessId(Long fileAccessId) {
        this.fileAccessId = fileAccessId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public String getAccessFile() {
        return accessFile;
    }
    public void setAccessFile(String accessFile) {
        this.accessFile = accessFile;
    }
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
}
