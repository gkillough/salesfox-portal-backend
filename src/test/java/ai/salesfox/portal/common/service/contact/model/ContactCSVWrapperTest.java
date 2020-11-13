package ai.salesfox.portal.common.service.contact.model;

import ai.salesfox.portal.common.service.contact.ContactCSVFileUtils;
import ai.salesfox.portal.common.service.contact.ContactCSVFileUtilsTest;
import ai.salesfox.portal.common.service.contact.ContactFieldValidationUtils;
import ai.salesfox.portal.rest.api.contact.model.ContactUploadModel;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContactCSVWrapperTest {
    @Test
    public void extractHeaderNamesTest() throws IOException {
        File testCSVFile = ContactCSVFileUtilsTest.getTestCSVFile(getClass().getClassLoader());
        try (FileInputStream testCsvFileInputStream = new FileInputStream(testCSVFile)) {
            try (ContactCSVWrapper csvWrapper = ContactCSVFileUtils.createCSVWrapper(testCsvFileInputStream, ContactCSVFileUtils.PORTAL_CSV_FORMAT)) {
                List<String> headerNames = csvWrapper.extractHeaderNames();
                assertHeader(headerNames, ContactCSVWrapper.HEADER_FIRST_NAME);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_LAST_NAME);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_EMAIL);

                assertHeader(headerNames, ContactCSVWrapper.HEADER_ADDRESS_LINE_1);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_ADDRESS_LINE_2);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_ADDRESS_CITY);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_ADDRESS_STATE);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_ADDRESS_ZIP);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_ADDRESS_IS_BUSINESS);

                assertHeader(headerNames, ContactCSVWrapper.HEADER_ORGANIZATION);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_TITLE);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_MOBILE_NUMBER);
                assertHeader(headerNames, ContactCSVWrapper.HEADER_BUSINESS_NUMBER);
            }
        }
    }

    @Test
    public void validateUploadModelTest() throws IOException {
        File testCSVFile = ContactCSVFileUtilsTest.getTestCSVFile(getClass().getClassLoader());
        try (FileInputStream testCsvFileInputStream = new FileInputStream(testCSVFile)) {
            try (ContactCSVWrapper csvWrapper = ContactCSVFileUtils.createCSVWrapper(testCsvFileInputStream, ContactCSVFileUtils.PORTAL_CSV_FORMAT)) {
                List<ContactUploadModel> contactUploadModels = csvWrapper.parseRecords();
                assertFalse(contactUploadModels.isEmpty(), "Expected to find at least one model");

                for (ContactUploadModel contactUploadModel : contactUploadModels) {
                    assertContactUploadModelValid(contactUploadModel);
                }
            }
        }
    }

    private void assertHeader(List<String> headerNames, String headerName) {
        assertTrue(headerNames.contains(headerName), String.format("Expected '%s' header to be present", headerName));
    }

    private void assertContactUploadModelValid(ContactUploadModel contactUploadModel) {
        try {
            ContactFieldValidationUtils.validateContactUploadModel(contactUploadModel);
        } catch (ResponseStatusException e) {
            fail(e.getMessage());
        }
    }

}
