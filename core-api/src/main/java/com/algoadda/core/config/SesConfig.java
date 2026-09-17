package com.algoadda.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {

    @Value("${algoadda.aws.ses.region:${algoadda.aws.s3.region:ap-south-1}}")
    private String region;

    @Value("${AWS_ACCESS_KEY_ID:#{null}}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY:#{null}}")
    private String secretKey;

    @Bean
    @ConditionalOnMissingBean(SesClient.class)
    public SesClient sesClient() {
        var builder = SesClient.builder().region(Region.of(region));

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ));
        } else {
            try {
                builder.credentialsProvider(DefaultCredentialsProvider.create());
            } catch (Exception e) {
                builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("dummy-access-key", "dummy-secret-key")
                ));
            }
        }

        return builder.build();
    }
}
