package ai.salesfox.portal.integration;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.portal.common.enumeration.DistributorName;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.external.CatalogueItemExternalDetailsEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.integration.scribeless.workflow.ScribelessSoloNoteManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

@Slf4j
@Component
public class GiftPartnerSubmissionService {
    private final ScribelessSoloNoteManager scribelessSoloNoteManager;
    private final EnumMap<DistributorName, GiftPartner> giftPartnerMap;

    @Autowired
    public GiftPartnerSubmissionService(ScribelessSoloNoteManager scribelessSoloNoteManager, List<GiftPartner> giftPartners) {
        this.scribelessSoloNoteManager = scribelessSoloNoteManager;
        this.giftPartnerMap = initializeGiftPartnerMap(giftPartners);
    }

    public final EnumMap<DistributorName, GiftPartner> initializeGiftPartnerMap(List<GiftPartner> giftPartners) {
        return giftPartners
                .stream()
                .collect(() -> new EnumMap<>(DistributorName.class), (map, partner) -> map.put(partner.distributorName(), partner), EnumMap::putAll);
    }

    public void submitGiftToPartners(GiftEntity gift, UserEntity submittingUser) throws SalesfoxException {
        boolean hasItem = null != gift.getGiftItemDetailEntity();
        boolean hasNote = null != gift.getGiftNoteDetailEntity();

        if (hasItem) {
            submitGift(gift, submittingUser);
        } else if (hasNote) {
            scribelessSoloNoteManager.submitNoteToScribeless(gift, submittingUser);
        } else {
            // TODO consider throwing exception (to trigger email event for end-user)
            log.warn("A gift with id=[{}] was seemingly submitted without an item or a note", gift.getGiftId());
        }
    }

    private void submitGift(GiftEntity gift, UserEntity submittingUser) throws SalesfoxException {
        GiftItemDetailEntity giftItemDetail = gift.getGiftItemDetailEntity();
        CatalogueItemEntity item = giftItemDetail.getCatalogueItemEntity();
        CatalogueItemExternalDetailsEntity externalItemDetails = item.getCatalogueItemExternalDetailsEntity();

        String itemDistributorNameString = externalItemDetails.getDistributor().toUpperCase();
        if (EnumUtils.isValidEnum(DistributorName.class, itemDistributorNameString)) {
            DistributorName itemDistributorName = EnumUtils.getEnum(DistributorName.class, itemDistributorNameString);

            GiftPartner giftPartner = giftPartnerMap.get(itemDistributorName);
            if (null != giftPartner) {
                giftPartner.submitGift(gift, submittingUser);
            } else {
                log.warn("Attempted to send an item with an unsupported gift partner: {}", itemDistributorNameString);
                throw new SalesfoxException(String.format("Unsupported gift partner: %s", itemDistributorNameString));
            }
        } else {
            log.warn("Attempted to send an item with an unknown distributor name: {}", itemDistributorNameString);
            throw new SalesfoxException(String.format("Unknown distributor name: %s", itemDistributorNameString));
        }
    }

}
