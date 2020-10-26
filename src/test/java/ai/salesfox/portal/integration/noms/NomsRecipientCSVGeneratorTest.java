package ai.salesfox.portal.integration.noms;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.address.OrganizationAccountContactAddressEntity;
import ai.salesfox.portal.integration.noms.configuration.NomsConfiguration;
import ai.salesfox.portal.integration.noms.csv.NomsRecipientCSVGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NomsRecipientCSVGeneratorTest {
    public static final String TEST_NOMS_TEMP_DIRECTORY = "build/tmp";

    @Test
    public void createRecipientCSVFileWithFormatTest() throws PortalException {
        NomsConfiguration nomsConfiguration = createTestNomsConfiguration();
        NomsRecipientCSVGenerator csvGenerator = new NomsRecipientCSVGenerator(nomsConfiguration);

        UUID fakeGiftId = UUID.randomUUID();
        PagedResourceHolder<OrganizationAccountContactEntity> contactPagedResourceHolder = createContactPagedResourceHolder();
        File recipientCSVFile = csvGenerator.createRecipientCSVFile(fakeGiftId, contactPagedResourceHolder, NomsRecipientCSVGenerator.DEFAULT_CSV_FORMAT);
        assertTrue(recipientCSVFile.exists(), "Expected the recipient CSV file to have been created");
    }

    private NomsConfiguration createTestNomsConfiguration() {
        return new NomsConfiguration() {
            @Override
            public File getNomsTempDirectory() {
                return new File(TEST_NOMS_TEMP_DIRECTORY);
            }
        };
    }

    private PagedResourceHolder<OrganizationAccountContactEntity> createContactPagedResourceHolder() {
        OrganizationAccountContactEntity contact1 = createTestContact("Test 1", "User");
        OrganizationAccountContactEntity contact2 = createTestContact("Test 2", "User");
        Page<OrganizationAccountContactEntity> page = new PageImpl<>(List.of(contact1, contact2));
        return new PagedResourceHolder<>(page, input -> Page.empty());
    }

    private OrganizationAccountContactEntity createTestContact(String firstName, String lastName) {
        UUID contactId = UUID.randomUUID();
        OrganizationAccountContactEntity contact = new OrganizationAccountContactEntity(
                contactId,
                firstName,
                lastName,
                String.format("%s.%s_test_noreply@salesfox.ai", firstName, lastName),
                true
        );

        OrganizationAccountContactAddressEntity address = new OrganizationAccountContactAddressEntity(
                contact.getContactId()
        );
        address.setAddressLine1("123 Not A Real Street");
        address.setCity("Dallas");
        address.setState("TX");
        address.setZipCode("75001");

        contact.setContactAddressEntity(address);
        return contact;
    }

}
