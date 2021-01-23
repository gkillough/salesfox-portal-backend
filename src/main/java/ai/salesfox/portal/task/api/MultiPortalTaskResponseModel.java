package ai.salesfox.portal.task.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiPortalTaskResponseModel {
    private List<PortalTaskResponseModel> tasks;

}
