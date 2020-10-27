package ai.salesfox.portal.integration.noms.workflow;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.exception.PortalFileSystemException;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.address.OrganizationAccountContactAddressEntity;
import ai.salesfox.portal.integration.noms.configuration.NomsConfiguration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class NomsRecipientCSVGenerator {
    public static CSVFormat DEFAULT_CSV_FORMAT = CSVFormat.EXCEL;
    public static String BULK_ORDER_FILE_NAME_PREFIX = "bulk-order-";
    public static String BULK_ORDER_FILE_NAME_SUFFIX = ".workflow";

    public static List<String> NOMS_REQUIRED_CSV_HEADERS = Collections.unmodifiableList(
            List.of(
                    "First Name",
                    "Last Name",
                    "Street Address 1",
                    "Street Address 2",
                    "City",
                    "State",
                    "Zip Code"
            )
    );

    private final NomsConfiguration nomsConfiguration;

    @Autowired
    public NomsRecipientCSVGenerator(NomsConfiguration nomsConfiguration) {
        this.nomsConfiguration = nomsConfiguration;
    }

    public File createRecipientCSVFile(UUID giftId, PagedResourceHolder<OrganizationAccountContactEntity> recipientHolder) throws PortalException {
        return createRecipientCSVFile(giftId, recipientHolder, DEFAULT_CSV_FORMAT);
    }

    public File createRecipientCSVFile(UUID giftId, PagedResourceHolder<OrganizationAccountContactEntity> recipientHolder, CSVFormat csvFormat) throws PortalException {
        if (recipientHolder.getFirstPage().isEmpty()) {
            throw new PortalException("No contacts supplied; cannot create CSV file");
        }

        String csvFileName = createCSVFileName(giftId);
        File csvOutputFile = new File(nomsConfiguration.getNomsTempDirectory(), csvFileName);
        if (csvOutputFile.exists() && csvOutputFile.getTotalSpace() > 0L) {
            csvOutputFile.delete();
        }

        try {
            FileWriter csvFileWriter = new FileWriter(csvOutputFile);
            try (CSVPrinter printer = new CSVPrinter(csvFileWriter, csvFormat)) {
                printer.printRecord(NOMS_REQUIRED_CSV_HEADERS);
                writeContactsRecords(printer, recipientHolder);
            }
        } catch (IOException ioException) {
            throw new PortalFileSystemException("Could not create NOMS recipient CSV file");
        }
        return csvOutputFile;
    }

    private void writeContactsRecords(CSVPrinter printer, PagedResourceHolder<OrganizationAccountContactEntity> recipientHolder) throws IOException {
        Page<OrganizationAccountContactEntity> pageOfContacts = recipientHolder.getFirstPage();
        while (!pageOfContacts.isEmpty()) {
            for (OrganizationAccountContactEntity contact : pageOfContacts) {
                writeContactRecord(printer, contact);
            }
            pageOfContacts = recipientHolder.retrieveNextPage(pageOfContacts);
        }
    }

    private void writeContactRecord(CSVPrinter printer, OrganizationAccountContactEntity contact) throws IOException {
        OrganizationAccountContactAddressEntity address = contact.getContactAddressEntity();
        printer.printRecord(
                contact.getFirstName(),
                contact.getLastName(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getZipCode()
        );
    }

    private String createCSVFileName(UUID giftId) {
        LocalDate currentDate = PortalDateTimeUtils.getCurrentDate();
        return BULK_ORDER_FILE_NAME_PREFIX + currentDate.toString() + "-" + giftId.toString() + BULK_ORDER_FILE_NAME_SUFFIX;
    }

}
