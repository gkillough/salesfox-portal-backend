package ai.salesfox.portal.common.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PortalDateTimeUtilsTest {
    @Test
    public void getCurrentDateTimeUTCTest() {
        OffsetDateTime currentDateTime = PortalDateTimeUtils.getCurrentDateTime();
        assertEquals(OffsetDateTime.now().getOffset(), currentDateTime.getOffset());
    }

    @Test
    public void computeMostRecentDateWithDayOfMonthSameDayTest() {
        LocalDate currentDate = PortalDateTimeUtils.getCurrentDate();
        LocalDate computedDate = PortalDateTimeUtils.computeMostRecentDateWithDayOfMonth(currentDate.getDayOfMonth());
        assertEquals(currentDate, computedDate);
    }

    @Test
    public void computeMostRecentDateWithDayOfMonthPlusDayTest() {
        LocalDate currentDate = PortalDateTimeUtils.getCurrentDate();
        LocalDate tomorrow = currentDate.plusDays(1L);

        // If today is the 28th of February and tomorrow is the 1st of March, then the computed date will be the 1st of February.
        // If today is the 7th of October and tomorrow is the 8th, then the computed date will be the 8th of September.
        LocalDate computedDate = PortalDateTimeUtils.computeMostRecentDateWithDayOfMonth(tomorrow.getDayOfMonth());
        assertTrue(computedDate.isBefore(currentDate), "Expected the computed date to be before the current date");
    }

    @Test
    public void computeMostRecentDateWithDayOfMonthMinusDayTest() {
        LocalDate currentDate = PortalDateTimeUtils.getCurrentDate();
        LocalDate yesterday = currentDate.minusDays(1L);

        // If today is the 1st of March and yesterday was the 28th of February, then the computed date will be the 28th of February.
        // If today is the 8th of October and yesterday was the 7th, then the computed date will be the 7th of October.
        LocalDate computedDate = PortalDateTimeUtils.computeMostRecentDateWithDayOfMonth(yesterday.getDayOfMonth());
        assertTrue(computedDate.isBefore(currentDate), "Expected the computed date to be before the current date");
    }

}
