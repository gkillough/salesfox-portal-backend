package com.getboostr.portal.rest.api.note;

import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.gift.note.GiftNoteDetailEntity;
import com.getboostr.portal.database.gift.note.GiftNoteDetailRepository;
import com.getboostr.portal.database.gift.tracking.GiftTrackingEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingRepository;
import com.getboostr.portal.database.note.NoteEntity;
import com.getboostr.portal.database.note.NoteRepository;
import com.getboostr.portal.database.note.restriction.NoteOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.note.restriction.NoteOrganizationAccountRestrictionRepository;
import com.getboostr.portal.database.note.restriction.NoteUserRestrictionEntity;
import com.getboostr.portal.database.note.restriction.NoteUserRestrictionRepository;
import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.api.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.api.note.model.MultiNoteModel;
import com.getboostr.portal.rest.api.note.model.NoteRequestModel;
import com.getboostr.portal.rest.api.note.model.NoteResponseModel;
import com.getboostr.portal.rest.api.user.common.model.UserSummaryModel;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NoteService {
    public static final int MESSAGE_LENGTH_CHAR_LIMIT = 2500;

    private final NoteRepository noteRepository;
    private final NoteOrganizationAccountRestrictionRepository noteOrgAcctRestrictionRepository;
    private final NoteUserRestrictionRepository noteUserRestrictionRepository;
    private final GiftTrackingRepository giftTrackingRepository;
    private final GiftNoteDetailRepository giftNoteDetailRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public NoteService(NoteRepository noteRepository, NoteOrganizationAccountRestrictionRepository noteOrgAcctRestrictionRepository, NoteUserRestrictionRepository noteUserRestrictionRepository,
                       GiftTrackingRepository giftTrackingRepository, GiftNoteDetailRepository giftNoteDetailRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.noteRepository = noteRepository;
        this.noteOrgAcctRestrictionRepository = noteOrgAcctRestrictionRepository;
        this.noteUserRestrictionRepository = noteUserRestrictionRepository;
        this.giftTrackingRepository = giftTrackingRepository;
        this.giftNoteDetailRepository = giftNoteDetailRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiNoteModel getNotes(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        Page<NoteEntity> accessibleNotes = getAccessibleNotes(pageOffset, pageLimit);
        if (accessibleNotes.isEmpty()) {
            return MultiNoteModel.empty();
        }

        List<NoteResponseModel> noteModels = accessibleNotes
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiNoteModel(noteModels, accessibleNotes);
    }

    public NoteResponseModel getNote(UUID noteId) {
        NoteEntity foundNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateNoteAccess(foundNote);
        return convertToResponseModel(foundNote);
    }

    @Transactional
    public NoteResponseModel createNote(NoteRequestModel requestModel) {
        validateNoteRequestModel(requestModel);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();

        NoteEntity noteToSave = new NoteEntity(null, loggedInUser.getUserId(), PortalDateTimeUtils.getCurrentDateTimeUTC(), requestModel.getMessage());
        NoteEntity savedNote = noteRepository.save(noteToSave);

        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            NoteUserRestrictionEntity noteUserRestrictionToSave = new NoteUserRestrictionEntity(savedNote.getNoteId(), loggedInUser.getUserId());
            NoteUserRestrictionEntity savedNoteUserRestriction = noteUserRestrictionRepository.save(noteUserRestrictionToSave);
            savedNote.setNoteUserRestrictionEntity(savedNoteUserRestriction);
        } else {
            MembershipEntity userMembership = loggedInUser.getMembershipEntity();
            NoteOrganizationAccountRestrictionEntity orgAcctRestrictionToSave = new NoteOrganizationAccountRestrictionEntity(savedNote.getNoteId(), userMembership.getOrganizationAccountId());
            NoteOrganizationAccountRestrictionEntity savedOrgAcctRestriction = noteOrgAcctRestrictionRepository.save(orgAcctRestrictionToSave);
            savedNote.setNoteOrganizationAccountRestrictionEntity(savedOrgAcctRestriction);
        }

        savedNote.setUpdatedByUserEntity(loggedInUser);
        return convertToResponseModel(savedNote);
    }

    @Transactional
    public void updateNote(UUID noteId, NoteRequestModel requestModel) {
        NoteEntity foundNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateNoteAccess(foundNote);
        validateNoteGiftStatus(noteId);
        validateNoteRequestModel(requestModel);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        foundNote.setUpdatedByUserId(loggedInUser.getUserId());
        foundNote.setDateModified(PortalDateTimeUtils.getCurrentDateTimeUTC());
        foundNote.setMessage(requestModel.getMessage());

        noteRepository.save(foundNote);
    }

    private Page<NoteEntity> getAccessibleNotes(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return noteRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return noteRepository.findAccessibleNotes(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private void validateNoteAccess(NoteEntity noteEntity) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();

        NoteOrganizationAccountRestrictionEntity orgAcctRestriction = noteEntity.getNoteOrganizationAccountRestrictionEntity();
        NoteUserRestrictionEntity userRestriction = noteEntity.getNoteUserRestrictionEntity();
        if (orgAcctRestriction != null && orgAcctRestriction.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
            return;
        } else if (userRestriction != null && userRestriction.getUserId().equals(loggedInUser.getUserId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private void validateNoteRequestModel(NoteRequestModel noteRequestModel) {
        if (noteRequestModel.getMessage() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request field 'message' cannot be null");
        }

        if (noteRequestModel.getMessage().length() > MESSAGE_LENGTH_CHAR_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The message cannot exceed %d characters", MESSAGE_LENGTH_CHAR_LIMIT));
        }
    }

    private void validateNoteGiftStatus(UUID noteId) {
        List<UUID> noteGiftIds = giftNoteDetailRepository.findByNoteId(noteId)
                .stream()
                .map(GiftNoteDetailEntity::getGiftId)
                .collect(Collectors.toList());
        if (!noteGiftIds.isEmpty()) {
            boolean hasNoteBeenSubmitted = giftTrackingRepository.findAllById(noteGiftIds)
                    .stream()
                    .anyMatch(GiftTrackingEntity::isSubmitted);
            if (hasNoteBeenSubmitted) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a note that has been submitted with a gift");
            }
        }
    }

    private NoteResponseModel convertToResponseModel(NoteEntity entity) {
        UserSummaryModel updatedByUser = UserSummaryModel.fromEntity(entity.getUpdatedByUserEntity());

        NoteOrganizationAccountRestrictionEntity orgAcctRestriction = entity.getNoteOrganizationAccountRestrictionEntity();
        NoteUserRestrictionEntity userRestriction = entity.getNoteUserRestrictionEntity();

        UUID restrictedOrgAcctId = null != orgAcctRestriction ? orgAcctRestriction.getOrganizationAccountId() : null;
        UUID restrictedUserId = null != userRestriction ? userRestriction.getUserId() : null;
        RestrictionModel restrictionModel = new RestrictionModel(restrictedOrgAcctId, restrictedUserId);

        return new NoteResponseModel(entity.getNoteId(), entity.getMessage(), entity.getDateModified(), updatedByUser, restrictionModel);
    }

}
