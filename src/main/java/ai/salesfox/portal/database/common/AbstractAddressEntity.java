package ai.salesfox.portal.database.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractAddressEntity implements Serializable {
    @Column(name = "street_number")
    private Integer streetNumber;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "address_line_1")
    private String addressLine1;

    // TODO rename this variable
    @Column(name = "address_line_2")
    private String aptSuite;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "is_business")
    private Boolean isBusiness;

}
