package ai.salesfox.portal.database.support.email_addresses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface SupportEmailAddressRepository extends JpaRepository<SupportEmailAddressEntity, UUID> {

    @Query("SELECT supportEmailAddress" +
            " FROM SupportEmailAddressEntity supportEmailAddress" +
            " WHERE (" +
            "   supportEmailCategory!= NULL AND supportEmailAddress.supportEmailCategory = :supportEmailCategory" +
            " )"
    )
    Page<SupportEmailAddressEntity> getSupportEmailAddressesByCategory(@Param("supportEmailCategory") String supportEmailCategory, Pageable pageable);

}
