package com.usepipeline.portal.web.contact;

import com.usepipeline.portal.web.common.model.request.ActiveStatusPatchModel;
import com.usepipeline.portal.web.common.page.PageMetadata;
import com.usepipeline.portal.web.contact.model.ContactUpdateModel;
import com.usepipeline.portal.web.contact.model.MultiContactModel;
import com.usepipeline.portal.web.contact.model.PointOfContactAssignmentModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ContactController.BASE_ENDPOINT)
public class ContactController {
    public static final String BASE_ENDPOINT = "/contacts";

    private ContactService contactService;
    private ContactInteractionsService contactInteractionsService;

    @Autowired
    public ContactController(ContactService contactService, ContactInteractionsService contactInteractionsService) {
        this.contactService = contactService;
        this.contactInteractionsService = contactInteractionsService;
    }

    @GetMapping
    public MultiContactModel getContacts(@RequestParam Boolean active, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return contactService.getContacts(active, offset, limit);
    }

    @PostMapping
    public void createContact(@RequestBody ContactUpdateModel requestModel) {
        contactService.createContact(requestModel);
    }

    @PutMapping("/{contactId}")
    public void updateContact(@PathVariable Long contactId, @RequestBody ContactUpdateModel requestModel) {
        contactService.updateContact(contactId, requestModel);
    }

    @PatchMapping("/{contactId}/active")
    public void setContactActiveStatus(@PathVariable Long contactId, @RequestBody ActiveStatusPatchModel requestModel) {
        contactService.setContactActiveStatus(contactId, requestModel);
    }

    @PostMapping("/{contactId}/assign")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCT_MEMBER_AUTH_CHECK)
    public void assignContact(@PathVariable Long contactId, @RequestBody PointOfContactAssignmentModel requestModel) {
        contactService.assignContactToUser(contactId, requestModel);
    }

    @PostMapping("/{contactId}/interactions/initiation")
    public void incrementContactInitiations(@PathVariable Long contactId) {
        contactInteractionsService.incrementContactInitiations(contactId);
    }

    @PostMapping("/{contactId}/interactions/engagement")
    public void incrementEngagementsGenerated(@PathVariable Long contactId) {
        contactInteractionsService.incrementEngagementsGenerated(contactId);
    }

}
