package ai.salesfox.portal.rest.api.gift.scheduling;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftScheduleResponseModel {
    private UUID giftId;
    private LocalDate sendDate;

}
