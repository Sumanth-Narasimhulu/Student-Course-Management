package com.studentmanagement.service;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.studentmanagement.dto.response.SasAccessDetails;

@Service
public class AzureBlobStorageService {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.container-name}")
    private String containerName;

    public AzureBlobStorageService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    private BlobClient getBlobClient(String blobName) {
        BlobContainerClient blobContainerClient =
                blobServiceClient.getBlobContainerClient(containerName);

        return blobContainerClient.getBlobClient(blobName);
    }

    public String generateUploadSas(String blobName) {

        BlobClient blobClient = getBlobClient(blobName);

        OffsetDateTime startTime =
                OffsetDateTime.now().minusMinutes(1);

        OffsetDateTime expiry =
                OffsetDateTime.now().plusMinutes(10);

        UserDelegationKey userDelegationKey =
                blobServiceClient.getUserDelegationKey(
                        startTime,
                        expiry);

        BlobSasPermission permission =
                new BlobSasPermission()
                        .setCreatePermission(true)
                        .setWritePermission(true);

        BlobServiceSasSignatureValues values =
                new BlobServiceSasSignatureValues(
                        expiry,
                        permission)
                        .setStartTime(startTime);

        String sasToken =
                blobClient.generateUserDelegationSas(
                        values,
                        userDelegationKey);

        return blobClient.getBlobUrl() + "?" + sasToken;
    }

    public BlobProperties getBlobProperies(String blobName) {
        return getBlobClient(blobName).getProperties();
    }

    public Boolean blobExists(String blobName) {
        return getBlobClient(blobName).exists();
    }

    public SasAccessDetails generateReadSas(String blobName) {

        BlobClient blobClient = getBlobClient(blobName);

        OffsetDateTime startTime =
                OffsetDateTime.now().minusMinutes(1);

        OffsetDateTime expiry =
                OffsetDateTime.now().plusMinutes(10);

        UserDelegationKey userDelegationKey =
                blobServiceClient.getUserDelegationKey(
                        startTime,
                        expiry);

        BlobSasPermission permission =
                new BlobSasPermission()
                        .setReadPermission(true);

        BlobServiceSasSignatureValues values =
                new BlobServiceSasSignatureValues(
                        expiry,
                        permission)
                        .setStartTime(startTime);

        String sasToken =
                blobClient.generateUserDelegationSas(
                        values,
                        userDelegationKey);

        return new SasAccessDetails(
                blobClient.getBlobUrl() + "?" + sasToken,
                expiry);
    }

    public Boolean delete(String blobName) {
        return getBlobClient(blobName).deleteIfExists();
    }
}