package ai.salesfox.portal.common.service.contact.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.rest.api.contact.model.ContactUploadModel;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ContactCSVWrapper implements Closeable {
    public static final String HEADER_FIRST_NAME = "First Name";
    public static final String HEADER_LAST_NAME = "Last Name";
    public static final String HEADER_EMAIL = "Email";
    public static final String HEADER_EMAIL_ADDRESS = "Email Address";

    public static final String HEADER_SINGLE_COLUMN_ADDRESS = "Address";
    public static final String HEADER_ADDRESS_LINE_1 = "Address Line 1";
    public static final String HEADER_ADDRESS_LINE_2 = "Address Line 2";
    public static final String HEADER_ADDRESS_CITY = "City";
    public static final String HEADER_ADDRESS_STATE = "State";
    public static final String HEADER_ADDRESS_ZIP = "Zip";
    public static final String HEADER_ADDRESS_ZIP_CODE = "Zip Code";
    public static final String HEADER_ADDRESS_IS_BUSINESS = "Is Business";

    public static final String HEADER_ORGANIZATION = "Organization";
    public static final String HEADER_TITLE = "Title";
    public static final String HEADER_MOBILE_NUMBER = "Mobile Number";
    public static final String HEADER_BUSINESS_NUMBER = "Business Number";

    private final CSVParser contactCSVParser;
    private final Map<String, Integer> headerMap;

    public ContactCSVWrapper(CSVParser contactCSVParser) {
        this.contactCSVParser = contactCSVParser;
        this.headerMap = contactCSVParser.getHeaderMap();
    }

    public List<String> extractHeaderNames() {
        return contactCSVParser.getHeaderNames();
    }

    public List<ContactUploadModel> parseRecords() throws IOException {
        List<CSVRecord> csvRecords = contactCSVParser.getRecords();
        List<ContactUploadModel> parsedRecords = new ArrayList<>(csvRecords.size());
        for (CSVRecord csvRecord : csvRecords) {
            ContactUploadModel parsedCSVRecord = parseRecord(csvRecord);
            parsedRecords.add(parsedCSVRecord);
        }
        return parsedRecords;
    }

    private ContactUploadModel parseRecord(CSVRecord csvRecord) {
        String firstName = extractTrimmedField(csvRecord, HEADER_FIRST_NAME);
        String lastName = extractTrimmedField(csvRecord, HEADER_LAST_NAME);
        String email = Optional.ofNullable(extractTrimmedField(csvRecord, HEADER_EMAIL)).orElseGet(() -> extractTrimmedField(csvRecord, HEADER_EMAIL_ADDRESS));
        PortalAddressModel address = extractAddress(csvRecord);
        String organization = extractTrimmedField(csvRecord, HEADER_ORGANIZATION);
        String title = extractTrimmedField(csvRecord, HEADER_TITLE);
        String mobileNumber = extractTrimmedField(csvRecord, HEADER_MOBILE_NUMBER);
        String businessNumber = extractTrimmedField(csvRecord, HEADER_BUSINESS_NUMBER);

        return new ContactUploadModel(firstName, lastName, email, address, null, organization, title, mobileNumber, businessNumber);
    }

    private String extractTrimmedField(CSVRecord csvRecord, String fieldName) {
        Integer fieldIndex = headerMap.get(fieldName);
        String fieldValue = csvRecord.get(fieldIndex);
        return StringUtils.trimToNull(fieldValue);
    }

    private PortalAddressModel extractAddress(CSVRecord csvRecord) {
        Boolean isBusiness = Optional.ofNullable(extractTrimmedField(csvRecord, HEADER_ADDRESS_IS_BUSINESS))
                .map(Boolean::parseBoolean)
                .orElse(true);

        if (headerMap.containsKey(HEADER_SINGLE_COLUMN_ADDRESS) && !headerMap.containsKey(HEADER_ADDRESS_LINE_1)) {
            // Candidate for single column address parsing
            String addressString = extractTrimmedField(csvRecord, HEADER_SINGLE_COLUMN_ADDRESS);
            Optional<PortalAddressModel> extractedAddress = SimpleAddressExtractionUtils.extractSimpleAddress(addressString);
            extractedAddress.ifPresent(addr -> addr.setIsBusiness(isBusiness));
            return extractedAddress.orElse(null);
        }

        String addressLine1 = extractTrimmedField(csvRecord, HEADER_ADDRESS_LINE_1);
        String addressLine2 = extractTrimmedField(csvRecord, HEADER_ADDRESS_LINE_2);
        String city = extractTrimmedField(csvRecord, HEADER_ADDRESS_CITY);
        String state = extractTrimmedField(csvRecord, HEADER_ADDRESS_STATE);
        String zip = Optional.ofNullable(extractTrimmedField(csvRecord, HEADER_ADDRESS_ZIP)).orElseGet(() -> extractTrimmedField(csvRecord, HEADER_ADDRESS_ZIP_CODE));

        return new PortalAddressModel(addressLine1, addressLine2, city, state, zip, isBusiness);
    }

    @Override
    public void close() throws IOException {
        contactCSVParser.close();
    }

}
