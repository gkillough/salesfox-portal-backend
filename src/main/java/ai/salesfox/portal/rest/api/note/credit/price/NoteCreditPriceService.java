package ai.salesfox.portal.rest.api.note.credit.price;

import ai.salesfox.portal.database.note.credit.NoteCreditPriceEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditPriceRepository;
import ai.salesfox.portal.rest.api.note.credit.price.model.NoteCreditPriceRequestModel;
import ai.salesfox.portal.rest.api.note.credit.price.model.NoteCreditPriceResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
public class NoteCreditPriceService {
    private final NoteCreditPriceRepository noteCreditPriceRepository;

    @Autowired
    public NoteCreditPriceService(NoteCreditPriceRepository noteCreditPriceRepository) {
        this.noteCreditPriceRepository = noteCreditPriceRepository;
    }

    public NoteCreditPriceResponseModel getNoteCreditPrice() {
        NoteCreditPriceEntity currentNoteCreditPrice = findNoteCreditPrice();
        return createResponseModel(currentNoteCreditPrice);
    }

    @Transactional
    public void updateNoteCreditPrice(NoteCreditPriceRequestModel requestModel) {
        NoteCreditPriceEntity foundNoteCreditPrice = findNoteCreditPrice();
        Double requestedPrice = requestModel.getNoteCreditPrice();
        if (null == requestedPrice) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Note Credit Price must not be NULL");
        } else {
            foundNoteCreditPrice.setNoteCreditPrice(requestedPrice);
            noteCreditPriceRepository.save(foundNoteCreditPrice);
        }
    }

    private NoteCreditPriceEntity findNoteCreditPrice() {
        return noteCreditPriceRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The Note Credit Price could not be retrieved"));
    }

    private NoteCreditPriceEntity initializeNoteCreditPrice() {
        NoteCreditPriceEntity noteCreditPriceToSave = new NoteCreditPriceEntity(null, null);
        NoteCreditPriceEntity savedNoteCreditPrice = noteCreditPriceRepository.save(noteCreditPriceToSave);

        return savedNoteCreditPrice;
    }

    private NoteCreditPriceResponseModel createResponseModel(NoteCreditPriceEntity currentNoteCreditPrice) {
        return new NoteCreditPriceResponseModel(currentNoteCreditPrice.getNoteCreditPriceId(), currentNoteCreditPrice.getNoteCreditPrice());
    }

}
