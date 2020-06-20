package com.usepipeline.portal.common.time;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PortalDateTimeUtils {
    public static OffsetDateTime getCurrentDateTimeUTC() {
        ZonedDateTime currentUTCTime = ZonedDateTime.now(ZoneOffset.UTC);
        return OffsetDateTime.from(currentUTCTime);
    }
    
}
