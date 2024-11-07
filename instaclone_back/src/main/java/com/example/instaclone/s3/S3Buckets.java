package com.example.instaclone.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3.buckets")
public class S3Buckets {

    private String imgBucket;

    public String getImgBucket() {
        return imgBucket;
    }

    public void setImgBucket(String imgBucket) {
        this.imgBucket = imgBucket;
    }
}
