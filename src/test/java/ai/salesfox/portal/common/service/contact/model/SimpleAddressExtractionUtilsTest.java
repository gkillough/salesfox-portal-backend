package ai.salesfox.portal.common.service.contact.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleAddressExtractionUtilsTest {
    @Test
    public void extractSimpleAddressValidTest() {
        String addressLine1 = "1 Something Street";
        String city = "City";
        String state = "TX";
        String zip = "12345-1234";
        String addressString = String.format("%s %s, %s %s", addressLine1, city, state, zip);

        Optional<PortalAddressModel> optionalExtractedAddress = SimpleAddressExtractionUtils.extractSimpleAddress(addressString);
        assertTrue(optionalExtractedAddress.isPresent());
        PortalAddressModel extractedAddress = optionalExtractedAddress.get();
        assertEquals(addressLine1, extractedAddress.getAddressLine1());
        assertEquals(city, extractedAddress.getCity());
        assertEquals(state, extractedAddress.getState());
        assertEquals(zip, extractedAddress.getZipCode());
    }

    @Test
    public void extractSimpleAddressWithPoundAddressLine2ValidTest() {
        String addressLine1 = "1 Something Street";
        String addressLine2 = "#6";
        String city = "City";
        String state = "TX";
        String zip = "12345-1234";
        String addressString = String.format("%s %s %s, %s %s", addressLine1, addressLine2, city, state, zip);

        Optional<PortalAddressModel> optionalExtractedAddress = SimpleAddressExtractionUtils.extractSimpleAddress(addressString);
        assertTrue(optionalExtractedAddress.isPresent());
        PortalAddressModel extractedAddress = optionalExtractedAddress.get();
        assertEquals(addressLine1, extractedAddress.getAddressLine1());
        assertEquals(addressLine2, extractedAddress.getAddressLine2());
        assertEquals(city, extractedAddress.getCity());
        assertEquals(state, extractedAddress.getState());
        assertEquals(zip, extractedAddress.getZipCode());
    }

    @Test
    public void extractSimpleAddressMultiWordStreetNameTest() {
        String addressLine1 = "1 Something Or Other Street";
        String city = "City";
        String state = "TX";
        String zip = "12345-1234";
        String addressString = String.format("%s %s, %s %s", addressLine1, city, state, zip);

        Optional<PortalAddressModel> optionalExtractedAddress = SimpleAddressExtractionUtils.extractSimpleAddress(addressString);
        assertTrue(optionalExtractedAddress.isEmpty(), "This address should have been too difficult to parse");
    }

    @Test
    public void extractSimpleAddressMultiWordCityNameTest() {
        String addressLine1 = "1 Something Lane";
        String city = "Big City";
        String state = "TX";
        String zip = "12345-1234";
        String addressString = String.format("%s %s, %s %s", addressLine1, city, state, zip);

        Optional<PortalAddressModel> optionalExtractedAddress = SimpleAddressExtractionUtils.extractSimpleAddress(addressString);
        assertTrue(optionalExtractedAddress.isEmpty(), "This address should have been too difficult to parse");
    }

}
