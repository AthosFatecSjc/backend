package com.energia.backend.model;

import com.energia.backend.model.UserStatusEntity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "status")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @OneToMany(mappedBy = "status")
    private List<UserStatusEntity> userStatuses;
}