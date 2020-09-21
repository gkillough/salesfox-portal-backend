package ai.salesfox.portal.common.service.note;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTemplatingUtilsTest {
    public static final String TEST_FIRST_NAME = "Adam";
    public static final String TEST_LAST_NAME = "Smith";

    public static final String TEST_NOTE_FORMAT = "Hi %s, this is a test. %s is your last name.";
    public static final String TEST_NOTE = String.format(TEST_NOTE_FORMAT, NoteTemplatingUtils.FIELD_FIRST_NAME, NoteTemplatingUtils.FIELD_LAST_NAME);
    public static final String TEST_NOTE_FIRST_NAME_RESOLVED = String.format(TEST_NOTE_FORMAT, TEST_FIRST_NAME, NoteTemplatingUtils.FIELD_LAST_NAME);
    public static final String TEST_NOTE_LAST_NAME_RESOLVED = String.format(TEST_NOTE_FORMAT, NoteTemplatingUtils.FIELD_FIRST_NAME, TEST_LAST_NAME);
    public static final String TEST_NOTE_FULLY_RESOLVED = String.format(TEST_NOTE_FORMAT, TEST_FIRST_NAME, TEST_LAST_NAME);

    @Test
    public void resolveNoteTest() {
        String resolvedNote = NoteTemplatingUtils.resolveNote(TEST_NOTE, TEST_FIRST_NAME, TEST_LAST_NAME);
        assertEquals(TEST_NOTE_FULLY_RESOLVED, resolvedNote);
    }

    @Test
    public void resolveNoteFirstNameTest() {
        String resolvedNote = NoteTemplatingUtils.resolveNote(TEST_NOTE, TEST_FIRST_NAME);
        assertEquals(TEST_NOTE_FIRST_NAME_RESOLVED, resolvedNote);
    }

    @Test
    public void resolveNoteLastNameTest() {
        String resolvedNote = NoteTemplatingUtils.resolveNote(TEST_NOTE, null, TEST_LAST_NAME);
        assertEquals(TEST_NOTE_LAST_NAME_RESOLVED, resolvedNote);
    }

    @Test
    public void resolveNoteNullTest() {
        String resolvedNote = NoteTemplatingUtils.resolveNote(TEST_NOTE, null, null);
        assertEquals(TEST_NOTE, resolvedNote);
    }

}
