package com.getboostr.portal.rest.api.contact;

import com.getboostr.portal.rest.api.contact.model.*;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.api.common.page.PageMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(ContactController.BASE_ENDPOINT)
public class ContactController {
    public static final String BASE_ENDPOINT = "/contacts";

    private ContactService contactService;
    private ContactInteractionsService contactInteractionsService;
    private ContactBulkUploadService contactBulkUploadService;

    @Autowired
    public ContactController(ContactService contactService, ContactInteractionsService contactInteractionsService, ContactBulkUploadService contactBulkUploadService) {
        this.contactService = contactService;
        this.contactInteractionsService = contactInteractionsService;
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

    @PostMapping("/{contactId}/interactions/initiation")
    public void incrementContactInitiations(@PathVariable UUID contactId) {
        contactInteractionsService.incrementContactInitiations(contactId);
    }

    @PostMapping("/{contactId}/interactions/engagement")
    public void incrementEngagementsGenerated(@PathVariable UUID contactId) {
        contactInteractionsService.incrementEngagementsGenerated(contactId);
    }

}
