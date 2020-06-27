package com.usepipeline.portal.database.gift.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GiftItemDetailRepository extends JpaRepository<GiftItemDetailEntity, GiftItemDetailPK> {

}
