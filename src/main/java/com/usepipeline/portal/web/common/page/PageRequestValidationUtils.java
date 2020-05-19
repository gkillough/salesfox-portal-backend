package com.usepipeline.portal.web.common.page;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PageRequestValidationUtils {
    public static void validatePagingParams(Integer offset, Integer limit) {
        validateOffset(offset);
        validateLimit(limit);
    }

    public static void validateOffset(Integer offset) {
        if (offset < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The page offset cannot be less than zero");
        }
    }

    public static void validateLimit(Integer limit) {
        if (limit < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The page limit cannot be less than one");
        }
        if (limit > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The page limit cannot be greater than one thousand");
        }
    }

}
