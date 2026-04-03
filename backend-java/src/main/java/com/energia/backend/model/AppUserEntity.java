package com.energia.backend.model;

import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.model.StatusEntity;
import com.energia.backend.model.RoleEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_user", uniqueConstraints = {
        @UniqueConstraint(name = "uq_app_user_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // BASIC FIELDS
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "anonymization_status", length = 20)
    private AnonymizationStatus anonymizationStatus;

    @Column(name = "anonymized_at")
    private LocalDateTime anonymizedAt;

    // MANY-TO-MANY → ROLE
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), // ✅ FIXED
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<RoleEntity> roles;

    // ONE USER → MANY USER_STATUS
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserStatusEntity> statuses;

    // ONE USER → MANY USER_TERMS
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserTermsEntity> acceptedTerms;

    @PrePersist
    public void prePersist() {
        if (this.anonymizationStatus == null) {
            this.anonymizationStatus = AnonymizationStatus.ACTIVE;
        }
    }

}
