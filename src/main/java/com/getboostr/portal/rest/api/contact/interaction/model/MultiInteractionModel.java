package com.getboostr.portal.rest.api.contact.interaction.model;

import com.getboostr.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiInteractionModel extends PagedResponseModel {
    private List<ContactInteractionsResponseModel> interactions;

    public static MultiInteractionModel empty() {
        return new MultiInteractionModel(List.of(), Page.empty());
    }

    public MultiInteractionModel(List<ContactInteractionsResponseModel> interactions, Page<?> page) {
        super(page);
        this.interactions = interactions;
    }

}
