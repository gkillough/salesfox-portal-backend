package com.usepipeline.portal.common.service.icon;

import com.usepipeline.portal.common.exception.PortalFileSystemException;
import com.usepipeline.portal.common.file_system.ResourceDirectoryConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class LocalIconManager {
    private ResourceDirectoryConfiguration resourceDirConfig;

    @Autowired
    public LocalIconManager(ResourceDirectoryConfiguration resourceDirConfig) {
        this.resourceDirConfig = resourceDirConfig;
    }

    public boolean isValidImageFile(File imageFile) throws PortalFileSystemException {
        if (imageFile == null) {
            return false;
        }

        try {
            String contentType = Files.probeContentType(imageFile.toPath());
            return "image".equals(contentType) && ImageIO.read(imageFile) != null;
        } catch (IOException e) {
            throw new PortalFileSystemException(e);
        }
    }

    public File saveIcon(File iconFile) throws PortalFileSystemException {
        if (!isValidImageFile(iconFile)) {
            throw new PortalFileSystemException("The image file is invalid");
        }

        try {
            return writeImageFile(iconFile);
        } catch (IOException e) {
            throw new PortalFileSystemException("Could not write file", e);
        }
    }

    // TODO retrieveIcon

    private File retrieveIconDirAsFile() throws PortalFileSystemException {
        String iconDirName = resourceDirConfig.getIconDir();
        File iconDir = new File(iconDirName);
        if (iconDir.exists() && iconDir.isDirectory()) {
            return iconDir;
        }
        throw new PortalFileSystemException("The image directory could not be loaded");
    }

    private File writeImageFile(File imageFile) throws IOException, PortalFileSystemException {
        String imageExtension = getImageExtension(imageFile);
        BufferedImage bufferedImage = ImageIO.read(imageFile);

        byte[] imageBytes;
        ByteArrayOutputStream imageByteArrayOutputStream = new ByteArrayOutputStream();
        boolean hadAppropriateWriter = ImageIO.write(bufferedImage, imageExtension, imageByteArrayOutputStream);
        if (!hadAppropriateWriter) {
            throw new PortalFileSystemException("No appropriate image writer");
        }

        imageBytes = imageByteArrayOutputStream.toByteArray();
        String hashedImage = MD5Encoder.encode(imageBytes);
        String outputImageName = String.format("%s.%s", hashedImage, imageExtension);

        File iconDir = retrieveIconDirAsFile();
        File outputImageFile = new File(iconDir, outputImageName);
        try (FileOutputStream imageFileOutputStream = new FileOutputStream(outputImageFile)) {
            imageByteArrayOutputStream.writeTo(imageFileOutputStream);
            return outputImageFile;
        }
    }

    private String getImageExtension(File imageFile) {
        return StringUtils.substringAfterLast(imageFile.getName(), ".");
    }

}
