package com.getboostr.portal.web.note;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.note.NoteEntity;
import com.getboostr.portal.database.note.NoteRepository;
import com.getboostr.portal.web.common.page.PageRequestValidationUtils;
import com.getboostr.portal.web.note.model.MultiNoteModel;
import com.getboostr.portal.web.note.model.NoteRequestModel;
import com.getboostr.portal.web.note.model.NoteResponseModel;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.web.util.HttpSafeUserMembershipRetrievalService;
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

    private NoteRepository noteRepository;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public NoteService(NoteRepository noteRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.noteRepository = noteRepository;
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
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);

        NoteEntity noteToSave = new NoteEntity(null, userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), requestModel.getMessage());
        NoteEntity savedNote = noteRepository.save(noteToSave);
        return convertToResponseModel(savedNote);
    }

    @Transactional
    public void updateNote(UUID noteId, NoteRequestModel requestModel) {
        NoteEntity foundNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateNoteAccess(foundNote);
        validateNoteRequestModel(requestModel);
        foundNote.setMessage(requestModel.getMessage());

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        foundNote.setUpdatedByUserId(loggedInUser.getUserId());

        noteRepository.save(foundNote);
    }

    private Page<NoteEntity> getAccessibleNotes(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return noteRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        String roleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
        if (PortalAuthorityConstants.PIPELINE_BASIC_USER.equals(roleLevel) || PortalAuthorityConstants.PIPELINE_PREMIUM_USER.equals(roleLevel)) {
            return noteRepository.findAllByUpdatedByUserId(loggedInUser.getUserId(), pageRequest);
        }
        return noteRepository.findAllByOrganizationAccountId(userMembership.getOrganizationAccountId(), pageRequest);
    }

    private void validateNoteAccess(NoteEntity noteEntity) {
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (noteEntity.getUpdatedByUserId().equals(loggedInUser.getUserId())) {
            return;
        } else {
            MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
            String roleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
            if (!PortalAuthorityConstants.PIPELINE_BASIC_USER.equals(roleLevel)
                    && !PortalAuthorityConstants.PIPELINE_PREMIUM_USER.equals(roleLevel)
                    && noteEntity.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                return;
            }
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

    private NoteResponseModel convertToResponseModel(NoteEntity entity) {
        return new NoteResponseModel(entity.getNoteId(), entity.getOrganizationAccountId(), entity.getUpdatedByUserId(), entity.getMessage());
    }

}
