package com.evdealer.evdealermanagement.entity.vehicle;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(
        name = "model_version",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_model_version_model_name",
                        columnNames = {"model_id", "name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelVersion {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "model_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_version_model")
    )
    private Model model;

    @Column(name = "name", length = 160, nullable = false)
    private String name;
}


