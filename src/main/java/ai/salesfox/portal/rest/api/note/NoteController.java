package ai.salesfox.portal.rest.api.note;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.note.model.MultiNoteModel;
import ai.salesfox.portal.rest.api.note.model.NoteRequestModel;
import ai.salesfox.portal.rest.api.note.model.NoteResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(NoteController.BASE_ENDPOINT)
public class NoteController {
    public static final String BASE_ENDPOINT = "/notes";

    private NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public MultiNoteModel getNotes(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return noteService.getNotes(offset, limit);
    }

    @GetMapping("/{noteId}")
    public NoteResponseModel getNote(@PathVariable UUID noteId) {
        return noteService.getNote(noteId);
    }

    @PostMapping
    public NoteResponseModel createNote(@RequestBody NoteRequestModel requestModel) {
        return noteService.createNote(requestModel);
    }

    @PutMapping("/{noteId}")
    public void updateNote(@PathVariable UUID noteId, @RequestBody NoteRequestModel requestModel) {
        noteService.updateNote(noteId, requestModel);
    }

}
