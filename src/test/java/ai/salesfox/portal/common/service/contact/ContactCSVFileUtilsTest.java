package ai.salesfox.portal.common.service.contact;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ContactCSVFileUtilsTest {
    private static final String CONTACT_TEST_CSV_FILE_NAME = "contacts_test.csv";

    public static File getTestCSVFile(ClassLoader classLoader) {
        String filePath = classLoader
                .getResource(CONTACT_TEST_CSV_FILE_NAME)
                .getFile();
        File testCSVFile = new File(filePath);
        Assumptions.assumeTrue(testCSVFile.exists(), "Could not find file to test");
        return testCSVFile;
    }

    @Test
    public void createCSVWrapperTest() {
        CSVFormat csvFormat = ContactCSVFileUtils.PORTAL_CSV_FORMAT;
        File testCSVFile = getTestCSVFile(getClass().getClassLoader());
        try (FileInputStream testCsvFileInputStream = new FileInputStream(testCSVFile)) {
            ContactCSVFileUtils.createCSVWrapper(testCsvFileInputStream, csvFormat);
        } catch (Exception e) {
            fail(e);
        }
    }

}
