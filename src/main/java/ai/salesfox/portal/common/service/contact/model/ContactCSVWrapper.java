package ai.salesfox.portal.common.service.contact.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.rest.api.contact.model.ContactUploadModel;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class ContactCSVWrapper implements Closeable {
    public static final int MINIMUM_REQUIRED_HEADERS = 3;

    public static final String HEADER_SINGLE_COLUMN_NAME = "Name";
    public static final String HEADER_FIRST_NAME = "First Name";
    public static final String HEADER_LAST_NAME = "Last Name";
    public static final String HEADER_EMAIL = "Email";
    public static final String HEADER_EMAIL_ADDRESS = "Email Address";

    public static final String HEADER_SINGLE_COLUMN_ADDRESS = "Address";
    public static final String HEADER_ADDRESS_LINE_1 = "Address Line 1";
    public static final String HEADER_STREET_ADDRESS_1 = "Street Address 1";
    public static final String HEADER_ADDRESS_LINE_2 = "Address Line 2";
    public static final String HEADER_STREET_ADDRESS_2 = "Street Address 2";
    public static final String HEADER_STREET_ADDRESS_APT_SUITE = "Apt Suite";
    public static final String HEADER_ADDRESS_CITY = "City";
    public static final String HEADER_ADDRESS_STATE = "State";
    public static final String HEADER_ADDRESS_ZIP = "Zip";
    public static final String HEADER_ADDRESS_ZIP_CODE = "Zip Code";
    public static final String HEADER_ADDRESS_POSTAL_CODE = "Postal Code";
    public static final String HEADER_ADDRESS_IS_BUSINESS = "Is Business";

    public static final String HEADER_ORGANIZATION = "Organization";
    public static final String HEADER_TITLE = "Title";
    public static final String HEADER_MOBILE_NUMBER = "Mobile Number";
    public static final String HEADER_CELL = "Cell";
    public static final String HEADER_CELL_PHONE = "Cell Phone";
    public static final String HEADER_BUSINESS_NUMBER = "Business Number";
    public static final String HEADER_WORK_NUMBER = "Work Number";
    public static final String HEADER_WORK_PHONE = "Work Phone";

    private final CSVParser contactCSVParser;
    private final Map<String, Integer> headerMap;

    public ContactCSVWrapper(CSVParser contactCSVParser) {
        this.contactCSVParser = contactCSVParser;
        this.headerMap = sanitizeHeaders(contactCSVParser.getHeaderMap());
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
        if (extractHeaderNames().size() < MINIMUM_REQUIRED_HEADERS) {
            return new ContactUploadModel();
        }

        String firstName;
        String lastName;
        if (headerMap.containsKey(HEADER_SINGLE_COLUMN_NAME)) {
            Pair<String, String> firstAndLastName = extractFirstAndLastName(csvRecord);
            firstName = firstAndLastName.getFirst();
            lastName = firstAndLastName.getSecond();
        } else {
            firstName = extractTrimmedField(csvRecord, HEADER_FIRST_NAME);
            lastName = extractTrimmedField(csvRecord, HEADER_LAST_NAME);
        }

        String email = extractFirstMatchingTrimmedField(csvRecord, HEADER_EMAIL, HEADER_EMAIL_ADDRESS);
        PortalAddressModel address = extractAddress(csvRecord);
        String organization = extractTrimmedField(csvRecord, HEADER_ORGANIZATION);
        String title = extractTrimmedField(csvRecord, HEADER_TITLE);
        String mobileNumber = extractFirstMatchingTrimmedField(csvRecord, HEADER_MOBILE_NUMBER, HEADER_CELL, HEADER_CELL_PHONE);
        String businessNumber = extractFirstMatchingTrimmedField(csvRecord, HEADER_BUSINESS_NUMBER, HEADER_WORK_NUMBER, HEADER_WORK_PHONE);

        return new ContactUploadModel(firstName, lastName, email, address, null, organization, title, mobileNumber, businessNumber);
    }

    private String extractFirstMatchingTrimmedField(CSVRecord csvRecord, String... fieldNames) {
        if (null != fieldNames) {
            for (String fieldName : fieldNames) {
                if (headerMap.containsKey(fieldName)) {
                    return extractTrimmedField(csvRecord, fieldName);
                }
            }
        }
        return null;
    }

    private String extractTrimmedField(CSVRecord csvRecord, String fieldName) {
        Integer fieldIndex = headerMap.get(fieldName);
        if (null != fieldIndex && csvRecord.isSet(fieldIndex)) {
            String fieldValue = csvRecord.get(fieldIndex);
            return StringUtils.trimToNull(fieldValue);
        }
        return null;
    }

    private Pair<String, String> extractFirstAndLastName(CSVRecord csvRecord) {
        String fullName = extractTrimmedField(csvRecord, HEADER_SINGLE_COLUMN_NAME);

        String firstName = "";
        String lastName = "";

        String[] splitFullName = StringUtils.split(fullName, " ");
        if (null != splitFullName && splitFullName.length > 0) {
            if (splitFullName.length == 1) {
                firstName = splitFullName[0];
                lastName = firstName;
            } else if (splitFullName.length == 2) {
                firstName = splitFullName[0];
                lastName = splitFullName[1];
            } else {
                List<String> fullNameTokens = Arrays.asList(splitFullName);
                firstName = fullNameTokens.get(0);

                int lastNameStartingIndex = 2;

                String middleInitialCandidate = fullNameTokens.get(1);
                int middleInitialCandidateLength = middleInitialCandidate.length();
                if (middleInitialCandidateLength == 1 || (middleInitialCandidateLength == 2 && StringUtils.endsWith(middleInitialCandidate, "."))) {
                    firstName = String.format("%s %s", firstName, middleInitialCandidate);
                    lastName = fullNameTokens.get(lastNameStartingIndex);
                    lastNameStartingIndex++;
                } else {
                    lastName = middleInitialCandidate;
                }

                for (int i = lastNameStartingIndex; i < fullNameTokens.size(); i++) {
                    lastName = String.format("%s %s", lastName, fullNameTokens.get(i));
                }
            }
        }

        return Pair.of(firstName, lastName);
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

        String addressLine1 = extractFirstMatchingTrimmedField(csvRecord, HEADER_ADDRESS_LINE_1, HEADER_STREET_ADDRESS_1);
        String addressLine2 = extractFirstMatchingTrimmedField(csvRecord, HEADER_ADDRESS_LINE_2, HEADER_STREET_ADDRESS_2, HEADER_STREET_ADDRESS_APT_SUITE);
        String city = extractTrimmedField(csvRecord, HEADER_ADDRESS_CITY);
        String state = extractTrimmedField(csvRecord, HEADER_ADDRESS_STATE);
        String zip = extractFirstMatchingTrimmedField(csvRecord, HEADER_ADDRESS_ZIP, HEADER_ADDRESS_ZIP_CODE, HEADER_ADDRESS_POSTAL_CODE);

        return new PortalAddressModel(addressLine1, addressLine2, city, state, zip, isBusiness);
    }

    @Override
    public void close() throws IOException {
        contactCSVParser.close();
    }

    private Map<String, Integer> sanitizeHeaders(Map<String, Integer> originalHeaders) {
        Map<String, Integer> sanitizedHeaders = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> headerEntry : originalHeaders.entrySet()) {
            String sanitizedKey = sanitizeInput(headerEntry.getKey());
            if (null != sanitizedKey) {
                sanitizedHeaders.put(sanitizedKey, headerEntry.getValue());
            }
        }
        return sanitizedHeaders;
    }

    private String sanitizeInput(String input) {
        input = StringUtils.trimToEmpty(input);

        StringBuilder sanitizedInput = new StringBuilder();
        for (char inputChar : input.toCharArray()) {
            if (CharUtils.isAsciiAlphanumeric(inputChar) || inputChar == ' ') {
                sanitizedInput.append(inputChar);
            }
        }
        return StringUtils.trimToNull(sanitizedInput.toString());
    }

}
