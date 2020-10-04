package ai.salesfox.portal.integration.digitalocean;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class DigitalOceanConfiguration {
    public static final String DIGITAL_OCEAN_BUCKET_REGION_NAME = "nyc3";
    public static final String DIGITAL_OCEAN_BUCKET_DOMAIN_NAME = DIGITAL_OCEAN_BUCKET_REGION_NAME + ".digitaloceanspaces.com";
    public static final String AWS_ENDPOINT_OVERRIDE = "https://" + DIGITAL_OCEAN_BUCKET_DOMAIN_NAME;

    @Getter
    @Value("${ai.salesfox.portal.integration.digitalocean.catalog.bucket.name:}")
    private String catalogBucketName;

    @Getter
    @Value("${ai.salesfox.portal.integration.digitalocean.user.bucket.name:}")
    private String userUploadsBucketName;

    @Value("${ai.salesfox.portal.integration.digitalocean.aws.accessKeyId:}")
    private String awsAccessKeyId;

    @Value("${ai.salesfox.portal.integration.digitalocean.aws.secretAccessKey:}")
    private String awsSecretAccessKey;

    @Bean
    public S3Client s3Client() {
        return S3Client
                .builder()
                .endpointOverride(URI.create(AWS_ENDPOINT_OVERRIDE))
                .region(Region.of(DIGITAL_OCEAN_BUCKET_REGION_NAME))
                .credentialsProvider(this::createCredentials)
                .build();
    }

    private AwsCredentials createCredentials() {
        return AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey);
    }

}
