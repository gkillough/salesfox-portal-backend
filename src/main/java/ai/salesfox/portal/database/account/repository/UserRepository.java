package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findFirstByEmail(String email);

    @Query("SELECT user" +
            " FROM UserEntity user" +
            " WHERE user.firstName LIKE %:query%" +
            " OR user.lastName LIKE %:query%" +
            " OR user.email LIKE %:query%" +
            " OR (user.firstName || ' ' || user.lastName) LIKE %:query%"
    )
    Page<UserEntity> findByQuery(@Param("query") String query, Pageable pageable);

}
