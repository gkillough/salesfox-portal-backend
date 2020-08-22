package ai.salesfox.portal.database.customization.icon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "custom_icon_files")
public class CustomIconFileEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @Column(name = "file_name")
    private String fileName;

}
