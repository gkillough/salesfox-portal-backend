package ai.salesfox.portal.rest.api.contact;

import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.contact.interaction.ContactInteractionApiService;
import ai.salesfox.portal.rest.api.contact.interaction.model.ContactInteractionRequestModel;
import ai.salesfox.portal.rest.api.contact.interaction.model.MultiInteractionModel;
import ai.salesfox.portal.rest.api.contact.model.*;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(ContactController.BASE_ENDPOINT)
public class ContactController {
    public static final String BASE_ENDPOINT = "/contacts";

    private final ContactService contactService;
    private final ContactInteractionApiService contactInteractionService;
    private final ContactBulkUploadService contactBulkUploadService;

    @Autowired
    public ContactController(ContactService contactService, ContactInteractionApiService contactInteractionService, ContactBulkUploadService contactBulkUploadService) {
        this.contactService = contactService;
        this.contactInteractionService = contactInteractionService;
        this.contactBulkUploadService = contactBulkUploadService;
    }

    @GetMapping
    public MultiContactModel getContacts(@RequestParam Boolean active, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return contactService.getContacts(active, offset, limit);
    }

    @GetMapping("/{contactId}")
    public ContactResponseModel getContact(@PathVariable UUID contactId) {
        return contactService.getContact(contactId);
    }

    @PostMapping
    public void createContact(@RequestBody ContactUploadModel requestModel) {
        contactService.createContact(requestModel);
    }

    @PostMapping("/bulk")
    public ContactBulkUploadResponse createContactsInBulk(@RequestBody ContactBulkUploadModel contactBulkUploadModel) {
        return contactBulkUploadService.createContactsInBulk(contactBulkUploadModel);
    }

    @PostMapping("/csv")
    public ContactBulkUploadResponse createContactsFromCsv(@RequestParam MultipartFile csvFile) {
        return contactBulkUploadService.createContactsFromCsvFile(csvFile);
    }

    @PutMapping("/{contactId}")
    public void updateContact(@PathVariable UUID contactId, @RequestBody ContactUploadModel requestModel) {
        contactService.updateContact(contactId, requestModel);
    }

    @PatchMapping("/{contactId}/active")
    public void setContactActiveStatus(@PathVariable UUID contactId, @RequestBody ActiveStatusPatchModel requestModel) {
        contactService.setContactActiveStatus(contactId, requestModel);
    }

    @PostMapping("/{contactId}/assign")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_MEMBER_AUTH_CHECK)
    public void assignContact(@PathVariable UUID contactId, @RequestBody PointOfContactAssignmentModel requestModel) {
        contactService.assignContactToUser(contactId, requestModel);
    }

    // Contact Interactions

    @GetMapping("/{contactId}/interactions")
    public MultiInteractionModel getContactInteractions(@PathVariable UUID contactId, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return contactInteractionService.getInteractions(contactId, offset, limit);
    }

    @PostMapping("/{contactId}/interactions")
    public void addContactInteraction(@PathVariable UUID contactId, @RequestBody ContactInteractionRequestModel requestModel) {
        contactInteractionService.addInteraction(contactId, requestModel);
    }

    @PutMapping("/{contactId}/interactions/{interactionId}")
    public void updateContactInteraction(@PathVariable UUID contactId, @PathVariable UUID interactionId, @RequestBody ContactInteractionRequestModel requestModel) {
        contactInteractionService.updateInteraction(contactId, interactionId, requestModel);
    }

}
