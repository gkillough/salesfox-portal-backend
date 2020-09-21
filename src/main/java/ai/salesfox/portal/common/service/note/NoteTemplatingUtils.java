package ai.salesfox.portal.common.service.note;

import org.apache.commons.lang3.StringUtils;

public class NoteTemplatingUtils {
    public static final String FIELD_FIRST_NAME = "{{firstName}}";
    public static final String FIELD_LAST_NAME = "{{lastName}}";
    public static final String[] REPLACEMENT_FIELDS = {FIELD_FIRST_NAME, FIELD_LAST_NAME};

    public static String resolveNote(String note, String firstName) {
        return resolveNote(note, firstName, null);
    }

    public static String resolveNote(String note, String firstName, String lastName) {
        return StringUtils.replaceEach(note, REPLACEMENT_FIELDS, new String[] {firstName, lastName});
    }

}
