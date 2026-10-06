package com.studentmanagement.dto.response;

import com.studentmanagement.entity.FileStorageStatus;

public class AssignmentSubmissionUploadResponse {
    private Long submissionId;
    private Long fileAssetId;
    private String blobName;
    private String uploadUrl;
    private FileStorageStatus status;
    private String originalFileName;
    public String getOriginalFileName() {
        return originalFileName;
    }
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    public Long getSubmissionId() {
        return submissionId;
    }
    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }
    public Long getFileAssetId() {
        return fileAssetId;
    }
    public void setFileAssetId(Long fileAssetId) {
        this.fileAssetId = fileAssetId;
    }
    public String getBlobName() {
        return blobName;
    }
    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }
    public String getUploadUrl() {
        return uploadUrl;
    }
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
    public FileStorageStatus getStatus() {
        return status;
    }
    public void setStatus(FileStorageStatus status) {
        this.status = status;
    }
    
}
