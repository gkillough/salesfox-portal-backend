package ai.salesfox.portal.common.service.contact;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.rest.api.contact.model.ContactUploadModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.Set;

public class ContactFieldValidationUtils {
    public static void validateContactUploadModel(ContactUploadModel contactUpdateModel) throws ResponseStatusException {
        Set<String> errors = new LinkedHashSet<>();
        if (StringUtils.isBlank(contactUpdateModel.getFirstName())) {
            errors.add("First Name");
        }

        if (StringUtils.isBlank(contactUpdateModel.getLastName())) {
            errors.add("Last Name");
        }

        if (StringUtils.isBlank(contactUpdateModel.getEmail())) {
            errors.add("Email");
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The following fields cannot be blank: %s", combinedErrors));
        }

        if (!FieldValidationUtils.isValidEmailAddress(contactUpdateModel.getEmail(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The email is in an invalid format");
        }

        if (!FieldValidationUtils.isValidUSAddress(contactUpdateModel.getAddress(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The address is invalid");
        }

        if (!FieldValidationUtils.isValidUSPhoneNumber(contactUpdateModel.getMobileNumber(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The mobile phone number is invalid");
        }

        if (!FieldValidationUtils.isValidUSPhoneNumber(contactUpdateModel.getBusinessNumber(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The business phone number is invalid");
        }
    }

}
