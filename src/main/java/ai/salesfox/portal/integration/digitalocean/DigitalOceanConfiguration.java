package ai.salesfox.portal.integration.digitalocean;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@PropertySource(DigitalOceanConfiguration.DIGITAL_OCEAN_CONFIGURATION_FILE_NAME)
public class DigitalOceanConfiguration {
    public static final String DIGITAL_OCEAN_CONFIGURATION_FILE_NAME = "digitalocean.properties";
    public static final String DIGITAL_OCEAN_BUCKET_REGION_NAME = "nyc3";
    public static final String DIGITAL_OCEAN_BUCKET_DOMAIN_NAME = DIGITAL_OCEAN_BUCKET_REGION_NAME + ".digitaloceanspaces.com";
    public static final String AWS_ENDPOINT_OVERRIDE = "https://" + DIGITAL_OCEAN_BUCKET_DOMAIN_NAME;

    @Getter
    @Value("${ai.salesfox.portal.integration.digitalocean.bucket.qualifying.prefix:}")
    private String bucketQualifyingPrefix;

    @Getter
    @Value("${ai.salesfox.portal.integration.digitalocean.catalog.unqualified.bucket.name:}")
    private String catalogBucketName;

    @Getter
    @Value("${ai.salesfox.portal.integration.digitalocean.user.unqualified.bucket.name:}")
    private String userUploadsBucketName;

    @Value("${ai.salesfox.portal.integration.digitalocean.aws.accessKeyId:}")
    private String awsAccessKeyId;

    @Value("${ai.salesfox.portal.integration.digitalocean.aws.secretAccessKey:}")
    private String awsSecretAccessKey;

    public String createQualifiedCatalogBucketName() {
        return getBucketQualifyingPrefix() + getCatalogBucketName();
    }

    public String createQualifiedUserUploadsBucketName() {
        return getBucketQualifyingPrefix() + getUserUploadsBucketName();
    }

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
