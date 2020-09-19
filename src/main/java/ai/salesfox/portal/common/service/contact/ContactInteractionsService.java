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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ContactInteractionsService {
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactInteractionRepository contactInteractionRepository;

    public ContactInteractionsService(OrganizationAccountContactRepository contactRepository, ContactInteractionRepository contactInteractionRepository) {
        this.contactRepository = contactRepository;
        this.contactInteractionRepository = contactInteractionRepository;
    }

    public List<ContactInteractionEntity> addContactInteractions(UserEntity interactingUser, Collection<UUID> contactIds, InteractionMedium medium, InteractionClassification classification, String note) {
        List<ContactInteractionEntity> contactInteractions = contactRepository.findAllById(contactIds)
                .stream()
                .map(contact -> createInteractionEntity(interactingUser, contact, medium, classification, note, PortalDateTimeUtils.getCurrentDate()))
                .collect(Collectors.toList());
        return contactInteractionRepository.saveAll(contactInteractions);
    }

    public Optional<ContactInteractionEntity> addContactInteraction(UserEntity interactingUser, UUID contactId, InteractionMedium medium, InteractionClassification classification, String note) {
        return contactRepository.findById(contactId)
                .map(foundContact -> addContactInteraction(interactingUser, foundContact, medium, classification, note));
    }

    public ContactInteractionEntity addContactInteraction(UserEntity interactingUser, OrganizationAccountContactEntity contact, InteractionMedium medium, InteractionClassification classification, String note) {
        return addContactInteraction(interactingUser, contact, medium, classification, note, PortalDateTimeUtils.getCurrentDate());
    }

    public ContactInteractionEntity addContactInteraction(UserEntity interactingUser, OrganizationAccountContactEntity contact, InteractionMedium medium, InteractionClassification classification, String note, LocalDate date) {
        ContactInteractionEntity interactionToSave = createInteractionEntity(interactingUser, contact, medium, classification, note, date);
        return contactInteractionRepository.save(interactionToSave);
    }

    private ContactInteractionEntity createInteractionEntity(UserEntity interactingUser, OrganizationAccountContactEntity contact, InteractionMedium medium, InteractionClassification classification, String note, LocalDate date) {
        return new ContactInteractionEntity(null, contact.getContactId(), interactingUser.getUserId(), medium.name(), classification.name(), date, note);
    }

}
