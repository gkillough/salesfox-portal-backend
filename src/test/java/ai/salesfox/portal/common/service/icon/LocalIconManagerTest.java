package ai.salesfox.portal.common.service.icon;

import ai.salesfox.portal.common.exception.PortalFileSystemException;
import ai.salesfox.portal.common.file_system.ResourceDirectoryConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class LocalIconManagerTest {
    public static final String TEST_RESOURCES_DIR = "src/test/resources";
    public static final String TEST_ICON_DIR_NAME = "icons";
    public static final String TEST_ICON_OUTPUT_DIR_NAME = "ignoredTestOutput";
    public static final String TEST_ICON_UNQUALIFIED_NAME = "test_icon01.jpg";

    private static ResourceDirectoryConfiguration mockInputResourceDir = Mockito.mock(ResourceDirectoryConfiguration.class);
    private static ResourceDirectoryConfiguration mockOutputResourceDir = Mockito.mock(ResourceDirectoryConfiguration.class);

    @BeforeAll
    public static void init() throws IOException {
        File testResources = new File(TEST_RESOURCES_DIR);
        assumeTrue(testResources.exists(), "The test resources dir did not exist");

        File testIconDir = new File(testResources, TEST_ICON_DIR_NAME);
        assumeTrue(testIconDir.exists(), "The test icon dir did not exist");
        Mockito.when(mockInputResourceDir.getIconDir()).thenReturn(testIconDir.getAbsolutePath());

        File testOutputDir = new File(testResources, TEST_ICON_OUTPUT_DIR_NAME);
        if (!testOutputDir.exists()) {
            testOutputDir.mkdir();
        }
        assumeTrue(testOutputDir.exists(), "The test output dir did not exist");
        Mockito.when(mockOutputResourceDir.getIconDir()).thenReturn(testOutputDir.getCanonicalPath());
    }

    @AfterEach
    public void cleanup() {
        File testResources = new File(TEST_RESOURCES_DIR);
        if (!testResources.exists()) {
            return;
        }

        File testOutputDir = new File(testResources, TEST_ICON_OUTPUT_DIR_NAME);
        if (!testOutputDir.canWrite()) {
            return;
        }

        File[] outputDirContents = testOutputDir.listFiles();
        if (outputDirContents == null) {
            return;
        }

        for (File outputFile : outputDirContents) {
            outputFile.deleteOnExit();
        }
    }

    @Test
    public void saveAndDeleteIconTest() throws PortalFileSystemException, FileNotFoundException {
        File outputIconDir = new File(mockOutputResourceDir.getIconDir());
        assumeTrue(outputIconDir.canWrite(), "Cannot write to the output directory");

        LocalIconManager localIconManager = new LocalIconManager(mockOutputResourceDir);

        File inputIconDir = new File(mockInputResourceDir.getIconDir());
        File testIconFile = new File(inputIconDir, TEST_ICON_UNQUALIFIED_NAME);
        assumeTrue(testIconFile.exists(), "The test icon file does not exist");

        InputStream iconInputStream = new FileInputStream(testIconFile);
        String imageExt = FilenameUtils.getExtension(TEST_ICON_UNQUALIFIED_NAME);
        File savedIconFile = localIconManager.saveIcon(iconInputStream, imageExt);
        assertTrue(savedIconFile.exists(), "Expected the saved icon file to exist");
        assertTrue(savedIconFile.getParent().contains(TEST_ICON_OUTPUT_DIR_NAME), "Expected the parent directory to contain the output directory name");

        boolean wasDeleteSuccessful = localIconManager.deleteIcon(savedIconFile.getName());
        assertTrue(wasDeleteSuccessful, "Failed to delete the icon");
    }

    @Test
    public void retrieveIconTest() throws PortalFileSystemException {
        File outputIconDir = new File(mockInputResourceDir.getIconDir());
        assumeTrue(outputIconDir.canRead(), "Cannot read from the input directory");

        LocalIconManager localIconManager = new LocalIconManager(mockInputResourceDir);

        Optional<BufferedImage> optionalIcon = localIconManager.retrieveIcon(TEST_ICON_UNQUALIFIED_NAME);
        assertTrue(optionalIcon.isPresent(), "Expected the icon file to be present");
    }

}
