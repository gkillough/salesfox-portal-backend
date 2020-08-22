package ai.salesfox.portal.rest.security.common;

public interface SecurityInterface {
    static String createSubDirectoryPattern(String baseDirectory) {
        return baseDirectory + "/**";
    }

}
