package com.naling.lightstream.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

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
}
