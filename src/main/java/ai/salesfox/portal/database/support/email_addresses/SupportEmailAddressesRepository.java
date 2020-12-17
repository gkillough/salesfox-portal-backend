package ai.salesfox.portal.database.support.email_addresses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface SupportEmailAddressesRepository extends JpaRepository<SupportEmailAddressesEntity, UUID> {
    Page<SupportEmailAddressesEntity> getSupportEmailAddressesById(UUID supportEmailAddressId, Pageable pageable);

    Page<SupportEmailAddressesEntity> getSupportEmailAddresses(Pageable pageable);

}
