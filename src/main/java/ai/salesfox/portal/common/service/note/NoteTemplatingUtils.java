package ai.salesfox.portal.common.service.note;

import org.apache.commons.lang3.StringUtils;

public class NoteTemplatingUtils {
    public static final String FIELD_FIRST_NAME = "{{{first name}}}";
    public static final String[] REPLACEMENT_FIELDS = {FIELD_FIRST_NAME};

    public static String resolveNote(String note, String firstName) {
        return StringUtils.replaceEach(note, REPLACEMENT_FIELDS, new String[] {firstName});
    }

}
