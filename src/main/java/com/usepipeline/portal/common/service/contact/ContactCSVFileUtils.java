package com.usepipeline.portal.common.service.contact;

import com.usepipeline.portal.common.service.contact.model.ContactCSVWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.QuoteMode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ContactCSVFileUtils {
    public static final char DEFAULT_DELIMITER = '|';
    public static final char DEFAULT_ESCAPE_CHAR = '\\';

    // TODO consider allowing some of these fields to be overridden by the end-user
    public static CSVFormat portalCSVFormat() {
        return CSVFormat.DEFAULT
                .withDelimiter(DEFAULT_DELIMITER)
                .withEscape(DEFAULT_ESCAPE_CHAR)
                .withQuoteMode(QuoteMode.NONE)
                .withFirstRecordAsHeader()
                .withAllowDuplicateHeaderNames(false);
    }

    public static ContactCSVWrapper createCSVWrapper(File csvFile, CSVFormat csvFormat) throws IOException {
        CSVParser parser = CSVParser.parse(csvFile, StandardCharsets.UTF_8, csvFormat);
        return new ContactCSVWrapper(parser);
    }

}
