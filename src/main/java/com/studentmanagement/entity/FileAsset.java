package com.studentmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "file_assets")
public class FileAsset {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="blob_name",nullable=false,unique=true)
    private String blobName;
    @Column (name = "container_name",nullable = false)
    private String containerName;
    @Column (name = "content_type")
    private String contentType;
    @Column (name = "file_size")
    private Long fileSize;
    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private FileStorageStatus status;
    @Column (name = "original_file_name")
    private String originalFileName;
    public String getOriginalFileName() {
        return originalFileName;
    }
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getBlobName() {
        return blobName;
    }
    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }
    public String getContainerName() {
        return containerName;
    }
    public void setContainerName(String containerName) {
        this.containerName = containerName;
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
    public FileStorageStatus getStatus() {
        return status;
    }
    public void setStatus(FileStorageStatus status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
}
