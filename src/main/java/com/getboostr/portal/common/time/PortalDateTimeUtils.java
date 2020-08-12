package com.getboostr.portal.common.time;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class PortalDateTimeUtils {
    public static OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now();
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

}
