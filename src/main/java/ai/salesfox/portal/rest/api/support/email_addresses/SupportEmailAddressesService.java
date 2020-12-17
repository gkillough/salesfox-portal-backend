package ai.salesfox.portal.rest.api.support.email_addresses;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.service.support.SupportEmailAddressesValidationUtils;
import ai.salesfox.portal.database.support.email_addresses.SupportEmailAddressesEntity;
import ai.salesfox.portal.database.support.email_addresses.SupportEmailAddressesRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.support.email_addresses.model.MultiSupportEmailAddressModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressesRequestModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SupportEmailAddressesService {
    private final SupportEmailAddressesRepository supportEmailAddressesRepository;

    @Autowired
    public SupportEmailAddressesService(SupportEmailAddressesRepository supportEmailAddressesRepository) {
        this.supportEmailAddressesRepository = supportEmailAddressesRepository;
    }

    public MultiSupportEmailAddressModel getSupportEmailAddresses(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        Page<SupportEmailAddressesEntity> supportEmailAddresses = getAllSupportEmailAddresses(pageOffset, pageLimit);
        if (supportEmailAddresses.isEmpty()) {
            return MultiSupportEmailAddressModel.empty();
        }

        List<SupportEmailAddressesResponseModel> supportEmailAddressesModels = supportEmailAddresses
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiSupportEmailAddressModel(supportEmailAddressesModels, supportEmailAddresses);
    }

    public SupportEmailAddressesResponseModel getSupportEmailAddressesById(UUID supportEmailId) {
        SupportEmailAddressesEntity foundSupportEmailAddress = supportEmailAddressesRepository.findById(supportEmailId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return convertToResponseModel(foundSupportEmailAddress);
    }

    @Transactional
    public SupportEmailAddressesResponseModel createSupportEmailAddress(SupportEmailAddressesRequestModel requestModel) {
        validateSupportEmailAddressesRequestModel(requestModel);

        SupportEmailAddressesEntity supportEmailAddressToSave = new SupportEmailAddressesEntity(null, requestModel.getCategory(), requestModel.getEmailAddress());
        SupportEmailAddressesEntity savedSupportEmailAddress = supportEmailAddressesRepository.save(supportEmailAddressToSave);

        return convertToResponseModel(savedSupportEmailAddress);
    }

    @Transactional
    public void updateSupportEmailAddresses(UUID supportEmailAddressId, SupportEmailAddressesRequestModel requestModel) {
        SupportEmailAddressesEntity foundSupportEmailAddress = supportEmailAddressesRepository.findById(supportEmailAddressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateSupportEmailAddressesRequestModel(requestModel);

        supportEmailAddressesRepository.save(foundSupportEmailAddress);
    }

    private Page<SupportEmailAddressesEntity> getAllSupportEmailAddresses(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        return supportEmailAddressesRepository.getSupportEmailAddresses(pageRequest);
    }

    private void validateSupportEmailAddressesRequestModel(SupportEmailAddressesRequestModel supportEmailAddressesRequestModel) {
        List<String> errors = new ArrayList<>();
        if (supportEmailAddressesRequestModel.getCategory() == null) {
            errors.add("The request field 'category' cannot be null");
        } else if (!SupportEmailAddressesValidationUtils.isValidCategory(supportEmailAddressesRequestModel.getCategory())) {
            errors.add(String.format("This is not an approved category", SupportEmailAddressesValidationUtils.ALLOWED_CATEGORIES));
        }

        if (supportEmailAddressesRequestModel.getEmailAddress() == null) {
            errors.add("The request field 'category' cannot be null");
        } else if (!FieldValidationUtils.isValidEmailAddress(supportEmailAddressesRequestModel.getEmailAddress(), false)) {
            errors.add("Must be valid email");
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, combinedErrors);
        }
    }

    private SupportEmailAddressesResponseModel convertToResponseModel(SupportEmailAddressesEntity entity) {
        return new SupportEmailAddressesResponseModel(entity.getSupportEmailId(), entity.getSupportEmailAddress(), entity.getSupportEmailCategory());
    }

}
