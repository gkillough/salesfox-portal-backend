package com.getboostr.portal.rest.security.common;

public interface SecurityInterface {
    default String createSubDirectoryPattern(String baseDirectory) {
        return baseDirectory + "/**";
    }

}
