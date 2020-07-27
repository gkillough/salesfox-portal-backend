package com.getboostr.portal.common.time;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class PortalDateTimeUtils {
    public static OffsetDateTime getCurrentDateTimeUTC() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static LocalDate getCurrentDateUTC() {
        return LocalDate.now(ZoneOffset.UTC);
    }

}
