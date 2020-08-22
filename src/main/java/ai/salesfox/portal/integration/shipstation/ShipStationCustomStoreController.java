package ai.salesfox.portal.integration.shipstation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController(ShipStationCustomStoreController.BASE_ENDPOINT)
public class ShipStationCustomStoreController {
    public static final String BASE_ENDPOINT = "/integration/shipstation";

    @GetMapping
    public ResponseEntity<String> getOrdersBetween(
            @RequestParam String action,
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate,
            @RequestParam(required = false) Integer page
    ) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping
    public static void post(
            @RequestParam String action,
            @RequestParam("order_number") String orderNumber,
            @RequestParam("carrier") String carrier,
            @RequestParam(value = "service", required = false) String service,
            @RequestParam("tracking_number") String trackingNumber,
            @RequestBody String xmlRequestBody
    ) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

}
