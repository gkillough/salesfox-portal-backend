package ai.salesfox.portal.common.time;

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
        LocalDate currentDate = getCurrentDate();

        int currentDayOfMonth = currentDate.getDayOfMonth();
        if (currentDayOfMonth == dayOfMonth) {
            return currentDate;
        }

        LocalDate targetDayThisMonth = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), dayOfMonth);
        if (currentDayOfMonth > dayOfMonth) {
            return targetDayThisMonth;
        } else {
            return targetDayThisMonth.minusMonths(1L);
        }
    }

}
