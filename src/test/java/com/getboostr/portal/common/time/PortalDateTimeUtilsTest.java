package com.getboostr.portal.common.time;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PortalDateTimeUtilsTest {
    @Test
    public void getCurrentDateTimeUTCTest() {
        OffsetDateTime currentDateTime = PortalDateTimeUtils.getCurrentDateTime();
        assertEquals(OffsetDateTime.now().getOffset(), currentDateTime.getOffset());
    }

}
