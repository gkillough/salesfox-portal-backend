package ai.salesfox.portal.integration.digitalocean;

import ai.salesfox.portal.common.enumeration.PortalImageStorageDestination;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.exception.PortalRuntimeException;
import ai.salesfox.portal.common.service.icon.ExternalImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Component
public class DigitalOceanBucketService implements ExternalImageStorageService {
    public static final int DEFAULT_BYTE_BUFFER_UNIT_SIZE = 5242880;

    private final DigitalOceanConfiguration digitalOceanConfig;
    private final S3Client s3Client;

    @Autowired
    public DigitalOceanBucketService(DigitalOceanConfiguration digitalOceanConfig, S3Client s3Client) {
        this.digitalOceanConfig = digitalOceanConfig;
        this.s3Client = s3Client;
    }

    @Override
    public String storeImageAndRetrieveUrl(PortalImageStorageDestination destination, MultipartFile multipartFile) throws PortalException {
        String bucketName = getBucketName(destination);
        String uploadKey = UUID.randomUUID().toString();

        CreateMultipartUploadRequest uploadRequest = createMultipartUploadRequest(bucketName, uploadKey, multipartFile.getContentType());
        CreateMultipartUploadResponse uploadResponse = s3Client.createMultipartUpload(uploadRequest);

        String uploadId = uploadResponse.uploadId();
        List<CompletedPart> completedParts = uploadFileParts(bucketName, uploadKey, uploadId, multipartFile);
        CompleteMultipartUploadResponse completeMultipartUploadResponse = completeMultipartUpload(bucketName, uploadKey, uploadId, completedParts);
        return completeMultipartUploadResponse.location();
    }

    private CreateMultipartUploadRequest createMultipartUploadRequest(String bucketName, String uploadKey, String contentType) {
        return CreateMultipartUploadRequest
                .builder()
                .key(uploadKey)
                .bucket(bucketName)
                .contentType(contentType)
                .build();
    }

    private List<CompletedPart> uploadFileParts(String bucketName, String uploadKey, String uploadId, MultipartFile multipartFile) throws PortalException {
        byte[] multipartFileBytes;
        try {
            multipartFileBytes = multipartFile.getBytes();
        } catch (IOException ioException) {
            throw new PortalException("Failed to get uploaded bytes");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(multipartFileBytes);

        int partNumber = 1;
        LinkedList<CompletedPart> completedParts = new LinkedList<>();
        do {
            UploadPartRequest uploadPartRequest = UploadPartRequest
                    .builder().bucket(bucketName)
                    .key(uploadKey)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();

            ByteBuffer byteBufferSlice = byteBuffer.alignedSlice(DEFAULT_BYTE_BUFFER_UNIT_SIZE);
            RequestBody uploadPartRequestBody = RequestBody.fromByteBuffer(byteBufferSlice);
            UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, uploadPartRequestBody);

            CompletedPart completedPart = CompletedPart
                    .builder()
                    .partNumber(partNumber)
                    .eTag(uploadPartResponse.eTag())
                    .build();
            completedParts.add(completedPart);
            partNumber++;
        } while (byteBuffer.hasRemaining());

        return completedParts;
    }

    private CompleteMultipartUploadResponse completeMultipartUpload(String bucketName, String uploadKey, String uploadId, List<CompletedPart> completedParts) {
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload
                .builder()
                .parts(completedParts)
                .build();
        CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest
                .builder()
                .bucket(bucketName)
                .key(uploadKey)
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build();
        return s3Client.completeMultipartUpload(completeMultipartUploadRequest);
    }

    private String getBucketName(PortalImageStorageDestination bucketType) {
        switch (bucketType) {
            case CATALOG_IMAGES:
                return digitalOceanConfig.getCatalogBucketName();
            case USER_IMAGES:
                return digitalOceanConfig.getUserUploadsBucketName();
            default:
                throw new PortalRuntimeException(String.format("No known buckets for option: %s", bucketType.name()));
        }
    }

}
