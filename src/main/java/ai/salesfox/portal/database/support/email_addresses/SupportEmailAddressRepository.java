package ai.salesfox.portal.database.support.email_addresses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface SupportEmailAddressRepository extends JpaRepository<SupportEmailAddressEntity, UUID> {

}
