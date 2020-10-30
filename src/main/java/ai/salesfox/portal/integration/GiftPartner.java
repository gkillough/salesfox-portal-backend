package ai.salesfox.portal.integration;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.portal.common.enumeration.DistributorName;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;

public interface GiftPartner {
    DistributorName distributorName();

    void submitGift(GiftEntity gift, UserEntity submittingUser) throws SalesfoxException;

}
