package ai.salesfox.portal.rest.api.support.email_addresses;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.service.support.SupportEmailAddressesValidationUtils;
import ai.salesfox.portal.database.support.email_addresses.SupportEmailAddressEntity;
import ai.salesfox.portal.database.support.email_addresses.SupportEmailAddressRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.support.email_addresses.model.MultiSupportEmailAddressModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressRequestModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressResponseModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SupportEmailAddressService {
    private final SupportEmailAddressRepository supportEmailAddressRepository;

    @Autowired
    public SupportEmailAddressService(SupportEmailAddressRepository supportEmailAddressRepository) {
        this.supportEmailAddressRepository = supportEmailAddressRepository;
    }

    public MultiSupportEmailAddressModel getAllSupportEmailAddresses(String supportEmailCategory, Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        Page<SupportEmailAddressEntity> supportEmailAddresses = getSupportEmailAddress(supportEmailCategory, pageOffset, pageLimit);
        if (supportEmailAddresses.isEmpty()) {
            return MultiSupportEmailAddressModel.empty();
        }
        List<SupportEmailAddressResponseModel> supportEmailAddressesModels = supportEmailAddresses
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiSupportEmailAddressModel(supportEmailAddressesModels, supportEmailAddresses);
    }

    @Transactional
    public SupportEmailAddressResponseModel createSupportEmailAddress(SupportEmailAddressRequestModel requestModel) {
        validateSupportEmailAddressesRequestModel(requestModel);

        SupportEmailAddressEntity supportEmailAddressToSave = new SupportEmailAddressEntity(null, requestModel.getCategory(), requestModel.getEmailAddress());
        SupportEmailAddressEntity savedSupportEmailAddress = supportEmailAddressRepository.save(supportEmailAddressToSave);

        return convertToResponseModel(savedSupportEmailAddress);
    }

    @Transactional
    public void updateSupportEmailAddresses(UUID supportEmailAddressId, SupportEmailAddressRequestModel requestModel) {
        SupportEmailAddressEntity foundSupportEmailAddress = supportEmailAddressRepository.findById(supportEmailAddressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateSupportEmailAddressesRequestModel(requestModel);

        String newSupportEmailCategory = defaultIfBlank(requestModel.getCategory(), foundSupportEmailAddress.getSupportEmailCategory());
        foundSupportEmailAddress.setSupportEmailCategory(newSupportEmailCategory);

        String newSupportEmailAddress = defaultIfBlank(requestModel.getEmailAddress(), foundSupportEmailAddress.getSupportEmailCategory());
        foundSupportEmailAddress.setSupportEmailAddress(newSupportEmailAddress);

        supportEmailAddressRepository.save(foundSupportEmailAddress);
    }

    private Page<SupportEmailAddressEntity> getSupportEmailAddress(String supportEmailCategory, Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        List<String> errors = new ArrayList<>();
        
        if (StringUtils.isEmpty(supportEmailCategory)) {
            return supportEmailAddressRepository.findAll(pageRequest);
        } else {
            return supportEmailAddressRepository.getSupportEmailAddressesByCategory(supportEmailCategory, pageRequest);
        }
    }

    private void validateSupportEmailAddressesRequestModel(SupportEmailAddressRequestModel supportEmailAddressRequestModel) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isBlank(supportEmailAddressRequestModel.getCategory())) {
            errors.add("The request field 'category' cannot be null");
        } else if (!SupportEmailAddressesValidationUtils.isValidCategory(supportEmailAddressRequestModel.getCategory())) {
            errors.add(String.format("This is not an approved category. Valid categories: %s", SupportEmailAddressesValidationUtils.ALLOWED_CATEGORIES));
        }

        if (StringUtils.isBlank(supportEmailAddressRequestModel.getEmailAddress())) {
            errors.add("The request field 'category' cannot be null");
        } else if (!FieldValidationUtils.isValidEmailAddress(supportEmailAddressRequestModel.getEmailAddress(), false)) {
            errors.add("Must be valid email");
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, combinedErrors);
        }
    }

    private SupportEmailAddressResponseModel convertToResponseModel(SupportEmailAddressEntity entity) {
        return new SupportEmailAddressResponseModel(entity.getSupportEmailId(), entity.getSupportEmailAddress(), entity.getSupportEmailCategory());
    }

    private String defaultIfBlank(String str, String defaultValue) {
        return Optional.ofNullable(str).filter(StringUtils::isNotBlank).orElse(defaultValue);
    }

}
