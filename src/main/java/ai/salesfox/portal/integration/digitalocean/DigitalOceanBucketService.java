package ai.salesfox.portal.integration.digitalocean;

import ai.salesfox.portal.common.enumeration.PortalImageStorageDestination;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.exception.PortalRuntimeException;
import ai.salesfox.portal.common.service.icon.ExternalImageStorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

@Component
public class DigitalOceanBucketService implements ExternalImageStorageService {
    public static final int MAX_BYTE_BUFFER_UNIT_SIZE = 5242880;

    private final DigitalOceanConfiguration digitalOceanConfig;
    private final S3Client s3Client;

    @Autowired
    public DigitalOceanBucketService(DigitalOceanConfiguration digitalOceanConfig, S3Client s3Client) {
        this.digitalOceanConfig = digitalOceanConfig;
        this.s3Client = s3Client;
    }

    @Override
    public String storeImageAndRetrieveUrl(PortalImageStorageDestination destination, MultipartFile multipartFile) throws PortalException {
        byte[] multipartFileBytes = getMultipartFileBytes(multipartFile);
        String uploadKey = createUploadKey(multipartFileBytes, multipartFile.getOriginalFilename());
        String unqualifiedBucketName = getUnqualifiedBucketName(destination);
        String fullyQualifiedBucketName = digitalOceanConfig.getBucketQualifyingPrefix() + unqualifiedBucketName;

        CreateMultipartUploadRequest uploadRequest = createMultipartUploadRequest(fullyQualifiedBucketName, uploadKey, multipartFile.getContentType());
        CreateMultipartUploadResponse uploadResponse = s3Client.createMultipartUpload(uploadRequest);

        String uploadId = uploadResponse.uploadId();
        List<CompletedPart> completedParts = uploadFileParts(fullyQualifiedBucketName, uploadKey, uploadId, multipartFileBytes);
        completeMultipartUpload(fullyQualifiedBucketName, uploadKey, uploadId, completedParts);
        return constructSubdomainUrl(unqualifiedBucketName, uploadKey);
    }

    private CreateMultipartUploadRequest createMultipartUploadRequest(String bucketName, String uploadKey, String contentType) {
        return CreateMultipartUploadRequest
                .builder()
                .key(uploadKey)
                .bucket(bucketName)
                .contentType(contentType)
                .build();
    }

    private List<CompletedPart> uploadFileParts(String bucketName, String uploadKey, String uploadId, byte[] multipartFileBytes) {
        int numberOfParts = multipartFileBytes.length / MAX_BYTE_BUFFER_UNIT_SIZE;
        int numberOfExtraBytes = multipartFileBytes.length % MAX_BYTE_BUFFER_UNIT_SIZE;
        if (numberOfExtraBytes != 0) {
            numberOfParts++;
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

            int offset = MAX_BYTE_BUFFER_UNIT_SIZE * (partNumber - 1);
            ByteBuffer partialByteBuffer;
            if (partNumber == numberOfParts) {
                partialByteBuffer = byteBuffer.get(new byte[numberOfExtraBytes], offset, numberOfExtraBytes);
            } else {
                partialByteBuffer = byteBuffer.get(new byte[MAX_BYTE_BUFFER_UNIT_SIZE], offset, MAX_BYTE_BUFFER_UNIT_SIZE);
            }

            RequestBody uploadPartRequestBody = RequestBody.fromByteBuffer(partialByteBuffer);
            UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, uploadPartRequestBody);

            CompletedPart completedPart = CompletedPart
                    .builder()
                    .partNumber(partNumber)
                    .eTag(uploadPartResponse.eTag())
                    .build();
            completedParts.add(completedPart);
            partNumber++;
        } while (partNumber <= numberOfParts);

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

    private byte[] getMultipartFileBytes(MultipartFile multipartFile) throws PortalException {
        try {
            return multipartFile.getBytes();
        } catch (IOException ioException) {
            throw new PortalException("Failed to get uploaded bytes");
        }
    }

    private String getUnqualifiedBucketName(PortalImageStorageDestination bucketType) {
        switch (bucketType) {
            case CATALOG_IMAGES:
                return digitalOceanConfig.getCatalogBucketName();
            case USER_IMAGES:
                return digitalOceanConfig.getUserUploadsBucketName();
            default:
                throw new PortalRuntimeException(String.format("No known buckets for option: %s", bucketType.name()));
        }
    }

    private String createUploadKey(byte[] multipartFileBytes, String originalFileName) {
        String hashedFileName = DigestUtils.md5DigestAsHex(multipartFileBytes);
        String fileExtension = FilenameUtils.getExtension(originalFileName);
        return String.format("%s.%s", hashedFileName, fileExtension);
    }

    private String constructSubdomainUrl(String unqualifiedBucketName, String uploadKey) {
        // TODO use config variable for portal domain
        return String.format("https://%s.salesfox.ai/%s", unqualifiedBucketName, uploadKey);
    }

}
