package ai.salesfox.portal.common.service.icon;

import ai.salesfox.portal.common.exception.PortalFileSystemException;
import ai.salesfox.portal.common.file_system.ResourceDirectoryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

@Component
public class LocalIconManager {
    private final ResourceDirectoryConfiguration resourceDirConfig;

    @Autowired
    public LocalIconManager(ResourceDirectoryConfiguration resourceDirConfig) {
        this.resourceDirConfig = resourceDirConfig;
    }

    public File saveIcon(InputStream imageFileInputStream, String imageFileExtension) throws PortalFileSystemException {
        try {
            return writeInputStreamToFile(imageFileInputStream, imageFileExtension);
        } catch (IOException e) {
            throw new PortalFileSystemException("Could not read file as image", e);
        }
    }

    public Optional<BufferedImage> retrieveIcon(String iconFileName) throws PortalFileSystemException {
        File iconDir = retrieveIconDirAsFile();
        File iconFile = new File(iconDir, iconFileName);
        if (iconFile.exists()) {
            try {
                BufferedImage iconBufferedImage = ImageIO.read(iconFile);
                return Optional.of(iconBufferedImage);
            } catch (IOException e) {
                throw new PortalFileSystemException(e);
            }
        }
        return Optional.empty();
    }

    public boolean deleteIcon(String iconFileName) throws PortalFileSystemException {
        File iconDir = retrieveIconDirAsFile();

        File iconFile = new File(iconDir, iconFileName);
        if (iconFile.exists() && iconFile.canWrite()) {
            return iconFile.delete();
        }
        return false;
    }

    private File retrieveIconDirAsFile() throws PortalFileSystemException {
        String iconDirName = resourceDirConfig.getIconDir();
        File iconDir = new File(iconDirName);

        if (iconDir.exists() && iconDir.isDirectory()) {
            return iconDir;
        }
        throw new PortalFileSystemException("The image directory could not be loaded");
    }

    private File writeInputStreamToFile(InputStream imageFileInputStream, String imageExtension) throws IOException, PortalFileSystemException {
        BufferedImage bufferedImage = ImageIO.read(imageFileInputStream);

        ByteArrayOutputStream imageByteArrayOutputStream = new ByteArrayOutputStream();
        boolean hadAppropriateWriter = ImageIO.write(bufferedImage, imageExtension, imageByteArrayOutputStream);
        if (!hadAppropriateWriter) {
            throw new PortalFileSystemException("No appropriate image writer");
        }

        byte[] imageBytes = imageByteArrayOutputStream.toByteArray();
        String hashedImage = DigestUtils.md5DigestAsHex(imageBytes);
        String outputImageName = String.format("%s.%s", hashedImage, imageExtension);

        File iconDir = retrieveIconDirAsFile();
        File outputImageFile = new File(iconDir, outputImageName);
        try (FileOutputStream imageFileOutputStream = new FileOutputStream(outputImageFile)) {
            imageByteArrayOutputStream.writeTo(imageFileOutputStream);
            return outputImageFile;
        }
    }

}
