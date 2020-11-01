package ai.salesfox.portal.common.time;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class PortalDateTimeUtils {
    public static OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now();
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static LocalDate computeMostRecentDateWithDayOfMonth(int dayOfMonth) {
        if (dayOfMonth < 1 || 31 < dayOfMonth) {
            throw new DateTimeException("Invalid day of month: " + dayOfMonth);
        }

        LocalDate currentDate = getCurrentDate();
        int currentDayOfMonth = currentDate.getDayOfMonth();
        if (currentDayOfMonth == dayOfMonth) {
            return currentDate;
        }

        LocalDate targetDayThisMonth;
        try {
            targetDayThisMonth = currentDate.withDayOfMonth(dayOfMonth);
            if (currentDayOfMonth > dayOfMonth) {
                return targetDayThisMonth;
            }
        } catch (DateTimeException e) {
            // This month does not have the specified day-of-month.
        }

        for (long i = 1L; i < 12; i++) {
            try {
                return currentDate.minusMonths(i).withDayOfMonth(dayOfMonth);
            } catch (DateTimeException e) {
                // The i-th month before this one does not have the specified day-of-month.
            }
        }
        throw new DateTimeException("Could not compute most recent date with day-of-month: " + dayOfMonth);
    }

}
