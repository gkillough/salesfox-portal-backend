package com.usepipeline.portal.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalDateModel {
    private Integer year;
    private Integer month;
    private Integer day;

    public static PortalDateModel fromLocalDate(LocalDate localDate) {
        return new PortalDateModel(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(year, month, day);
    }

}
