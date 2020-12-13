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

import java.math.BigDecimal;

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
        BigDecimal requestedPrice = requestModel.getNoteCreditPrice();
        if (null == requestedPrice) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Note Credit Price cannot be null");
        } else if (requestedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Note Credit Price must be greater than zero");
        }

        NoteCreditPriceEntity foundNoteCreditPrice = findNoteCreditPrice();
        foundNoteCreditPrice.setNoteCreditPrice(requestedPrice);
        noteCreditPriceRepository.save(foundNoteCreditPrice);
    }

    private NoteCreditPriceEntity findNoteCreditPrice() {
        return noteCreditPriceRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The Note Credit Price could not be retrieved"));
    }

    private NoteCreditPriceResponseModel createResponseModel(NoteCreditPriceEntity currentNoteCreditPrice) {
        return new NoteCreditPriceResponseModel(currentNoteCreditPrice.getNoteCreditPrice());
    }

}
