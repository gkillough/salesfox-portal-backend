package ai.salesfox.portal.rest.api.note;

import ai.salesfox.portal.common.service.note.NoteValidationUtils;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailRepository;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingRepository;
import ai.salesfox.portal.database.note.NoteEntity;
import ai.salesfox.portal.database.note.NoteRepository;
import ai.salesfox.portal.database.note.restriction.NoteOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.note.restriction.NoteOrganizationAccountRestrictionRepository;
import ai.salesfox.portal.database.note.restriction.NoteUserRestrictionEntity;
import ai.salesfox.portal.database.note.restriction.NoteUserRestrictionRepository;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.note.model.MultiNoteModel;
import ai.salesfox.portal.rest.api.note.model.NoteRequestModel;
import ai.salesfox.portal.rest.api.note.model.NoteResponseModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class NoteService {
    public static final String DEFAULT_FONT_SIZE = "Medium";
    public static final String DEFAULT_FONT_COLOR = "black";
    public static final String DEFAULT_HANDWRITING_STYLE = "stafford";

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

        String noteFontSize = defaultIfBlank(requestModel.getFontSize(), DEFAULT_FONT_SIZE);
        String noteFontColor = defaultIfBlank(requestModel.getFontColor(), DEFAULT_FONT_COLOR);
        String noteHandwritingStyle = defaultIfBlank(requestModel.getHandwritingStyle(), DEFAULT_HANDWRITING_STYLE);
        NoteEntity noteToSave = new NoteEntity(null, loggedInUser.getUserId(), PortalDateTimeUtils.getCurrentDateTime(), requestModel.getMessage(), noteFontSize, noteFontColor, noteHandwritingStyle);
        NoteEntity savedNote = noteRepository.save(noteToSave);

        // FIXME determine if notes should be restricted to a specific user
        boolean restrictToUser = false;
        if (restrictToUser) {
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
        foundNote.setDateModified(PortalDateTimeUtils.getCurrentDateTime());
        foundNote.setMessage(requestModel.getMessage());

        String newFontSize = defaultIfBlank(requestModel.getFontSize(), foundNote.getFontSize());
        foundNote.setFontSize(newFontSize);

        String newFontColor = defaultIfBlank(requestModel.getFontColor(), foundNote.getFontColor());
        foundNote.setFontColor(newFontColor);

        String newHandwritingStyle = defaultIfBlank(requestModel.getHandwritingStyle(), foundNote.getHandwritingStyle());
        foundNote.setHandwritingStyle(newHandwritingStyle);

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
        List<String> errors = new ArrayList<>();
        if (noteRequestModel.getMessage() == null) {
            errors.add("The request field 'message' cannot be null");
        } else if (!NoteValidationUtils.isValidMessageSize(noteRequestModel.getMessage())) {
            errors.add(String.format("The message cannot exceed %d characters", NoteValidationUtils.MAX_MESSAGE_CHARS));
        }

        String fontSize = noteRequestModel.getFontSize();
        if (StringUtils.isNotBlank(fontSize) && !NoteValidationUtils.isValidFontSize(fontSize)) {
            errors.add(String.format("Invalid font size. Available sizes: %s", Arrays.toString(NoteValidationUtils.ALLOWED_FONT_SIZES)));
        }

        String fontColor = noteRequestModel.getFontColor();
        if (StringUtils.isNotBlank(fontColor) && !NoteValidationUtils.isValidFontColor(fontColor)) {
            errors.add(String.format("Invalid font color. Available colors: %s", Arrays.toString(NoteValidationUtils.ALLOWED_COLORS)));
        }

        String handwritingStyle = noteRequestModel.getHandwritingStyle();
        if (StringUtils.isNotBlank(handwritingStyle) && !NoteValidationUtils.isValidHandwritingStyle(handwritingStyle)) {
            errors.add(String.format("Invalid handwriting style. Available styles: %s", Arrays.toString(NoteValidationUtils.ALLOWED_HANDWRITING_STYLES)));
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, combinedErrors);
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

        return new NoteResponseModel(entity.getNoteId(), entity.getMessage(), entity.getFontSize(), entity.getFontColor(), entity.getHandwritingStyle(), entity.getDateModified(), updatedByUser, restrictionModel);
    }

    private String defaultIfBlank(String str, String defaultValue) {
        return Optional.ofNullable(str).filter(StringUtils::isNotBlank).orElse(defaultValue);
    }

}
