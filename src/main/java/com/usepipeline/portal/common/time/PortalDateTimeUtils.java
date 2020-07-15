package com.usepipeline.portal.common.time;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class PortalDateTimeUtils {
    public static OffsetDateTime getCurrentDateTimeUTC() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

}
