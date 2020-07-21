package com.getboostr.portal.database.order.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "order_request_statuses")
public class InventoryOrderRequestStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "status_id")
    private UUID statusId;

    @PrimaryKeyJoinColumn
    @Column(name = "order_id")
    private UUID orderId;

    @PrimaryKeyJoinColumn
    @Column(name = "changed_by_user_id")
    private UUID changedByUserId;

    @Column(name = "processing_status")
    private String processingStatus;

    @Column(name = "date_submitted")
    private OffsetDateTime dateSubmitted;

    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;

}
