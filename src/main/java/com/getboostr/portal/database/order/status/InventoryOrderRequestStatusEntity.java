package com.getboostr.portal.database.order.status;

import com.getboostr.portal.database.order.InventoryOrderRequestEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
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

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false)
    private InventoryOrderRequestEntity inventoryOrderRequestEntity;

    public InventoryOrderRequestStatusEntity(UUID statusId, UUID orderId, UUID changedByUserId, String processingStatus, OffsetDateTime dateSubmitted, OffsetDateTime dateUpdated) {
        this.statusId = statusId;
        this.orderId = orderId;
        this.changedByUserId = changedByUserId;
        this.processingStatus = processingStatus;
        this.dateSubmitted = dateSubmitted;
        this.dateUpdated = dateUpdated;
    }

}
