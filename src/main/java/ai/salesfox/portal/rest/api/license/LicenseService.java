package ai.salesfox.portal.rest.api.license;

import ai.salesfox.portal.database.account.entity.LicenseEntity;
import ai.salesfox.portal.database.account.repository.LicenseRepository;
import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@Component
@Deprecated
public class LicenseService {
    private final LicenseRepository licenseRepository;

    @Autowired
    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    public void setActiveStatus(UUID licenseId, ActiveStatusPatchModel activeStatusModel) {
        LicenseEntity licenseEntity = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (activeStatusModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'activeStatus' is required");
        }

        if (activeStatusModel.getActiveStatus() && hasLicenseExpired(licenseEntity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot activate an expired license");
        }

        licenseEntity.setIsActive(activeStatusModel.getActiveStatus());
        licenseRepository.save(licenseEntity);
    }

    private boolean hasLicenseExpired(LicenseEntity license) {
        return !LocalDate.now().isBefore(license.getExpirationDate());
    }

}
