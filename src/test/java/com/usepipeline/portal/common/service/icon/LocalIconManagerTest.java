package com.usepipeline.portal.common.service.icon;

import com.usepipeline.portal.common.exception.PortalFileSystemException;
import com.usepipeline.portal.common.file_system.ResourceDirectoryConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class LocalIconManagerTest {
    public static final String TEST_ICON_DIR_NAME = "icons";
    public static final String TEST_ICON_OUTPUT_DIR_NAME = "ignoredTestOutput";
    public static final String TEST_ICON_QUALIFIED_NAME = TEST_ICON_DIR_NAME + "/test_icon01.jpg";

    private static final ClassLoader classLoader = LocalIconManager.class.getClassLoader();
    private static ResourceDirectoryConfiguration mockInputResourceDir = Mockito.mock(ResourceDirectoryConfiguration.class);
    private static ResourceDirectoryConfiguration mockOutputResourceDir = Mockito.mock(ResourceDirectoryConfiguration.class);

    @BeforeAll
    public static void init() {
        URL iconDirUrl = classLoader.getResource(TEST_ICON_DIR_NAME);
        assumeTrue(iconDirUrl != null, "Could not get test icon dir");
        Mockito.when(mockInputResourceDir.getIconDir()).thenReturn(iconDirUrl.toString());

        URL ignoredTestOutputDir = classLoader.getResource(TEST_ICON_OUTPUT_DIR_NAME);
        assumeTrue(ignoredTestOutputDir != null, "Could not get test icon dir");
        Mockito.when(mockOutputResourceDir.getIconDir()).thenReturn(ignoredTestOutputDir.toString());
    }

    @AfterAll
    public static void cleanup() throws URISyntaxException {
        URL ignoredTestOutputDir = classLoader.getResource(TEST_ICON_OUTPUT_DIR_NAME);
        if (ignoredTestOutputDir == null) {
            return;
        }
        
        File outputDir = new File(ignoredTestOutputDir.toURI());
        if (!outputDir.canWrite()) {
            return;
        }

        File[] outputDirContents = outputDir.listFiles();
        if (outputDirContents == null) {
            return;
        }

        for (File outputFile : outputDirContents) {
            outputFile.deleteOnExit();
        }
    }

    @Test
    public void isValidImageFileTest() throws URISyntaxException, PortalFileSystemException {
        LocalIconManager localIconManager = new LocalIconManager(mockInputResourceDir);

        URL iconUrl = classLoader.getResource(TEST_ICON_QUALIFIED_NAME);
        assumeTrue(iconUrl != null, "Could not get test icon");

        File iconFile = new File(iconUrl.toURI());
        assertTrue(localIconManager.isValidImageFile(iconFile), "The image file validation test failed");
    }

    @Test
    public void saveAndDeleteIconTest() throws URISyntaxException, PortalFileSystemException {
        File outputIconDir = new File(mockOutputResourceDir.getIconDir());
        assumeTrue(outputIconDir.canWrite(), "Cannot write to the output directory");

        LocalIconManager localIconManager = new LocalIconManager(mockOutputResourceDir);

        URL iconUrl = classLoader.getResource(TEST_ICON_QUALIFIED_NAME);
        assumeTrue(iconUrl != null, "Could not get test icon");

        File iconFile = new File(iconUrl.toURI());
        File savedIconFile = localIconManager.saveIcon(iconFile);
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

        Optional<File> optionalIcon = localIconManager.retrieveIcon(TEST_ICON_QUALIFIED_NAME);
        assertTrue(optionalIcon.isPresent(), "Expected the icon file to be present");
    }

}
