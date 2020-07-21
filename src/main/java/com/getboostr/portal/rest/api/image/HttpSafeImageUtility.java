package com.getboostr.portal.rest.api.image;

import com.getboostr.portal.common.exception.PortalFileSystemException;
import com.getboostr.portal.common.model.IconFileNameProvider;
import com.getboostr.portal.common.service.icon.LocalIconManager;
import com.getboostr.portal.rest.api.image.model.ImageResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class HttpSafeImageUtility {
    private LocalIconManager localIconManager;

    @Autowired
    public HttpSafeImageUtility(LocalIconManager localIconManager) {
        this.localIconManager = localIconManager;
    }

    public ImageResponseModel getImageResponseModel(IconFileNameProvider fileNameProvider) throws PortalFileSystemException {
        return getImageResponseModel(fileNameProvider, MediaType.IMAGE_JPEG);
    }

    public ImageResponseModel getImageResponseModel(IconFileNameProvider fileNameProvider, MediaType mediaType) throws PortalFileSystemException {
        if (!MediaType.IMAGE_JPEG.equals(mediaType) && MediaType.IMAGE_PNG.equals(mediaType)) {
            log.error("Invalid media type: [{}]", mediaType);
            throw new IllegalArgumentException("Invalid media type: " + mediaType);
        }

        BufferedImage bufferedIcon = localIconManager.retrieveIcon(fileNameProvider.getFileName())
                .orElseThrow(() -> new PortalFileSystemException(String.format("Image with file name [%s] not found", fileNameProvider.getFileName())));
        return new ImageResponseModel(bufferedIcon, mediaType);
    }

    public File saveImage(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided file cannot be empty");
        }

        try (InputStream fileInputStream = imageFile.getInputStream()) {
            String iconFileExtension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
            if (StringUtils.isBlank(iconFileExtension)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file extension cannot be blank");
            }

            return localIconManager.saveIcon(fileInputStream, iconFileExtension);
        } catch (PortalFileSystemException fileSystemException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided file was an invalid image");
        } catch (IOException ioException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while saving the image");
        }
    }

    public boolean deleteImageByName(String fileName) {
        try {
            return localIconManager.deleteIcon(fileName);
        } catch (PortalFileSystemException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete existing image");
        }
    }

}
