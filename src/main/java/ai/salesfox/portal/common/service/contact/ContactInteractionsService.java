package ai.salesfox.portal.common.service.contact;

import ai.salesfox.portal.common.enumeration.InteractionClassification;
import ai.salesfox.portal.common.enumeration.InteractionMedium;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.contact.interaction.ContactInteractionEntity;
import ai.salesfox.portal.database.contact.interaction.ContactInteractionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class ContactInteractionsService {
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactInteractionRepository contactInteractionRepository;

    public ContactInteractionsService(OrganizationAccountContactRepository contactRepository, ContactInteractionRepository contactInteractionRepository) {
        this.contactRepository = contactRepository;
        this.contactInteractionRepository = contactInteractionRepository;
    }

    public Optional<ContactInteractionEntity> addContactInteraction(UserEntity interactingUser, UUID contactId, InteractionMedium medium, InteractionClassification classification, String note) {
        return contactRepository.findById(contactId)
                .map(foundContact -> addContactInteraction(interactingUser, foundContact, medium, classification, note));
    }

    public ContactInteractionEntity addContactInteraction(UserEntity interactingUser, OrganizationAccountContactEntity contact, InteractionMedium medium, InteractionClassification classification, String note) {
        return addContactInteraction(interactingUser, contact, medium, classification, note, PortalDateTimeUtils.getCurrentDate());
    }

    public ContactInteractionEntity addContactInteraction(UserEntity interactingUser, OrganizationAccountContactEntity contact, InteractionMedium medium, InteractionClassification classification, String note, LocalDate date) {
        ContactInteractionEntity interactionToSave = new ContactInteractionEntity(null, contact.getContactId(), interactingUser.getUserId(), medium.name(), classification.name(), date, note);
        return contactInteractionRepository.save(interactionToSave);
    }

}
