package com.usepipeline.portal.web.note.model;

import com.usepipeline.portal.web.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiNoteModel extends PagedResponseModel {
    private List<NoteResponseModel> notes;

    public static MultiNoteModel empty() {
        return new MultiNoteModel(List.of(), Page.empty());
    }

    public MultiNoteModel(List<NoteResponseModel> notes, Page<?> page) {
        super(page);
        this.notes = notes;
    }

}
