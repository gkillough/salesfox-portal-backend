package ai.salesfox.portal.common.service.contact.model;

import ai.salesfox.portal.rest.api.contact.model.ContactUploadModel;
import ai.salesfox.portal.common.model.PortalAddressModel;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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

    public static final String HEADER_ADDRESS_STREET_NUMBER = "Street Number";
    public static final String HEADER_ADDRESS_STREET_NAME = "Street Name";
    public static final String HEADER_ADDRESS_APT_SUITE = "Apt Suite";
    public static final String HEADER_ADDRESS_CITY = "City";
    public static final String HEADER_ADDRESS_STATE = "State";
    public static final String HEADER_ADDRESS_ZIP = "Zip";
    public static final String HEADER_ADDRESS_ZIP_CODE = "Zip Code";
    public static final String HEADER_ADDRESS_IS_BUSINESS = "Is Business";

    public static final String HEADER_ORGANIZATION = "Organization";
    public static final String HEADER_TITLE = "Title";
    public static final String HEADER_MOBILE_NUMBER = "Mobile Number";
    public static final String HEADER_BUSINESS_NUMBER = "Business Number";

    private CSVParser contactCSVParser;
    private Map<String, Integer> headerMap;

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

    // TODO consider a single address field and "address lines" headers in the future
    private PortalAddressModel extractAddress(CSVRecord csvRecord) {
        Integer streetNumber = Optional.ofNullable(extractTrimmedField(csvRecord, HEADER_ADDRESS_STREET_NUMBER))
                .map(NumberUtils::createInteger)
                .orElse(null);
        String streetName = extractTrimmedField(csvRecord, HEADER_ADDRESS_STREET_NAME);
        String aptSuite = extractTrimmedField(csvRecord, HEADER_ADDRESS_APT_SUITE);
        String city = extractTrimmedField(csvRecord, HEADER_ADDRESS_CITY);
        String state = extractTrimmedField(csvRecord, HEADER_ADDRESS_STATE);
        String zip = Optional.ofNullable(extractTrimmedField(csvRecord, HEADER_ADDRESS_ZIP)).orElseGet(() -> extractTrimmedField(csvRecord, HEADER_ADDRESS_ZIP_CODE));
        Boolean isBusiness = Optional.ofNullable(extractTrimmedField(csvRecord, HEADER_ADDRESS_IS_BUSINESS))
                .map(Boolean::parseBoolean)
                .orElse(true);

        return new PortalAddressModel(streetNumber, streetName, aptSuite, city, state, zip, isBusiness);
    }

    @Override
    public void close() throws IOException {
        contactCSVParser.close();
    }

}
