package com.getboostr.portal.database.interactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "interactions")
public class InteractionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "interaction_id")
    private UUID interactionId;

    @Column(name = "medium")
    private String medium;

    @Column(name = "classification")
    private String classification;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "note")
    private String note;

}
