package com.naling.lightstream.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;


@Configuration
public class StorageConfig {
    @Bean
    public S3Client s3Client(
            @Value("${lightstream.storage.endpoint}") String endpoint,
            @Value("${lightstream.storage.access-key}") String accessKey,
            @Value("${lightstream.storage.secret-key}") String secretKey,
            @Value("${lightstream.storage.region}") String region) {

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();
    }
}
