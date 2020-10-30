package ai.salesfox.portal.common.service.gift;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserAddressRepository;
import ai.salesfox.portal.database.common.AbstractAddressEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextEntity;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextRepository;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconRepository;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GiftDetailsService {
    public static final int DEFAULT_PAGE_SIZE = 500;

    private final UserAddressRepository userAddressRepository;
    private final OrganizationAccountContactRepository contactRepository;
    private final CustomIconRepository customIconRepository;
    private final CustomBrandingTextRepository customBrandingTextRepository;

    @Autowired
    public GiftDetailsService(
            UserAddressRepository userAddressRepository,
            OrganizationAccountContactRepository contactRepository,
            CustomIconRepository customIconRepository,
            CustomBrandingTextRepository customBrandingTextRepository
    ) {
        this.userAddressRepository = userAddressRepository;
        this.contactRepository = contactRepository;
        this.customIconRepository = customIconRepository;
        this.customBrandingTextRepository = customBrandingTextRepository;
    }

    public AbstractAddressEntity retrieveReturnAddress(GiftEntity gift) throws SalesfoxException {
        UserEntity requestingUser = gift.getRequestingUserEntity();
        return userAddressRepository.findById(requestingUser.getUserId())
                .filter(this::isValidAddress)
                .orElseThrow(() -> new SalesfoxException("The requesting Salesfox user does not have a valid address"));
    }

    public PagedResourceHolder<OrganizationAccountContactEntity> retrieveRecipientHolder(GiftEntity gift) {
        return retrieveRecipientHolder(gift, DEFAULT_PAGE_SIZE);
    }

    public PagedResourceHolder<OrganizationAccountContactEntity> retrieveRecipientHolder(GiftEntity gift, int pageSize) {
        UUID giftId = gift.getGiftId();
        PageRequest pageRequest = PageRequest.of(0, pageSize);
        Page<OrganizationAccountContactEntity> recipients = contactRepository.findGiftRecipientContactsByGiftId(giftId, pageRequest);
        return new PagedResourceHolder<>(recipients, newPageRequest -> contactRepository.findGiftRecipientContactsByGiftId(giftId, newPageRequest));
    }

    public Optional<CustomBrandingTextEntity> retrieveCustomText(GiftEntity gift) {
        GiftCustomTextDetailEntity giftCustomTextDetail = gift.getGiftCustomTextDetailEntity();
        if (null == giftCustomTextDetail) {
            return Optional.empty();
        }
        return customBrandingTextRepository.findById(giftCustomTextDetail.getCustomTextId());
    }

    public Optional<String> retrieveCustomIconUrl(GiftEntity gift) {
        GiftCustomIconDetailEntity giftCustomIconDetail = gift.getGiftCustomIconDetailEntity();
        if (null == giftCustomIconDetail) {
            return Optional.empty();
        }
        return customIconRepository.findById(giftCustomIconDetail.getCustomIconId())
                .map(CustomIconEntity::getIconUrl);
    }

    private boolean isValidAddress(AbstractAddressEntity addressEntity) {
        PortalAddressModel portalAddressModel = PortalAddressModel.fromEntity(addressEntity);
        return FieldValidationUtils.isValidUSAddress(portalAddressModel, false);
    }

}
