package com.usepipeline.portal.web.security.common;

public interface SecurityInterface {
    default String createSubDirectoryPattern(String baseDirectory) {
        return baseDirectory + "/**";
    }

}
