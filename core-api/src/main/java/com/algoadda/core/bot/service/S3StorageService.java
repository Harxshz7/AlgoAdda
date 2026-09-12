package com.algoadda.core.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(
        S3Client s3Client,
        @Value("${algoadda.aws.s3.bucket-name:algoadda-bot-artifacts}") String bucketName
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String uploadFile(String key, byte[] content, String contentType) {
        log.info("Uploading file to S3 bucket '{}' with key '{}' ({} bytes)", bucketName, key, content.length);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            log.info("Successfully uploaded key '{}' to bucket '{}'", key, bucketName);
        } catch (Exception e) {
            log.warn("S3 upload for key '{}' encountered: {}. Storing key reference.", key, e.getMessage());
        }
        return key;
    }

    public byte[] downloadFile(String key) {
        log.info("Downloading file from S3 bucket '{}' with key '{}'", bucketName, key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    }

    public String getBucketName() {
        return bucketName;
    }
}
