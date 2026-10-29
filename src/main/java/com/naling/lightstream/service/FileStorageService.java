package com.naling.lightstream.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStorageService {
    private final S3Client s3Client;
    private final String bucketName;

    public FileStorageService(S3Client s3Client, @Value("${lightstream.storage.bucket}") String bucketName){
        this.s3Client = s3Client;
        this.bucketName = bucketName;

        try{
            s3Client.headBucket(r -> r.bucket(bucketName));
        } catch (NoSuchBucketException e){
            s3Client.createBucket(r -> r.bucket(bucketName));
        }
    }

    public String upload(MultipartFile file) {
        String objectKey = UUID.randomUUID().toString();

        try (InputStream in = file.getInputStream()) {
            s3Client.putObject(
                    r -> r.bucket(bucketName).key(objectKey).contentType(file.getContentType()),
                    RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file", e);
        }

        return objectKey;
    }

    public void delete(String objectKey) {
        s3Client.deleteObject(r -> r.bucket(bucketName).key(objectKey));
    }

    public ResponseInputStream<GetObjectResponse> download(String objectKey) {
        return s3Client.getObject(r -> r.bucket(bucketName).key(objectKey));
    }

    public int deleteObjectsOlderThan(Instant cutoff) {
        int deleted = 0;
        for (S3Object object : s3Client.listObjectsV2Paginator(r -> r.bucket(bucketName)).contents()) {
            if (object.lastModified().isBefore(cutoff)) {
                delete(object.key());
                deleted++;
            }
        }
        return deleted;
    }






}
