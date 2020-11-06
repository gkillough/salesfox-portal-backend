package ai.salesfox.portal.common.service.contact;

import ai.salesfox.portal.common.service.contact.model.ContactCSVWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.QuoteMode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ContactCSVFileUtils {
    public static final char DEFAULT_ESCAPE_CHAR = '\\';

    // TODO consider allowing some of these fields to be overridden by the end-user
    public static CSVFormat portalCSVFormat() {
        return CSVFormat.EXCEL
                .withEscape(DEFAULT_ESCAPE_CHAR)
                .withQuoteMode(QuoteMode.NONE)
                .withFirstRecordAsHeader()
                .withAllowDuplicateHeaderNames(false);
    }

    public static ContactCSVWrapper createCSVWrapper(InputStream csvFileInputStream, CSVFormat csvFormat) throws IOException {
        CSVParser parser = CSVParser.parse(csvFileInputStream, StandardCharsets.UTF_8, csvFormat);
        return new ContactCSVWrapper(parser);
    }

}
