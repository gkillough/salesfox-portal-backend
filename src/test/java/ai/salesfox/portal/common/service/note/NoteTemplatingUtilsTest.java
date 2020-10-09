package ai.salesfox.portal.common.service.note;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTemplatingUtilsTest {
    public static final String TEST_FIRST_NAME = "Adam";

    public static final String TEST_NOTE_FORMAT = "Hi %s, this is a test.";
    public static final String TEST_NOTE = String.format(TEST_NOTE_FORMAT, NoteTemplatingUtils.FIELD_FIRST_NAME);
    public static final String TEST_NOTE_RESOLVED = String.format(TEST_NOTE_FORMAT, TEST_FIRST_NAME);

    @Test
    public void resolveNoteTest() {
        String resolvedNote = NoteTemplatingUtils.resolveNote(TEST_NOTE, TEST_FIRST_NAME);
        assertEquals(TEST_NOTE_RESOLVED, resolvedNote);
    }

    @Test
    public void resolveNoteNullTest() {
        String resolvedNote = NoteTemplatingUtils.resolveNote(TEST_NOTE, null);
        assertEquals(TEST_NOTE, resolvedNote);
    }

}
