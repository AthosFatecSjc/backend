package com.energia.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_user")
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
    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String name;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String phone;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPersonalDataEntity personalData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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
    @Builder.Default
    private List<UserStatusEntity> statuses = new ArrayList<>();;

    // ONE USER → MANY USER_TERMS
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserTermsEntity> acceptedTerms;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.anonymizationStatus == null) {
            this.anonymizationStatus = AnonymizationStatus.ACTIVE;
        }
        ensurePersonalDataLink();
    }

    public String getName() {
        if (this.name != null) {
            return this.name;
        }
        return this.personalData != null ? this.personalData.getName() : null;
    }

    public void setName(String name) {
        this.name = name;
        if (name == null) {
            if (this.personalData != null) {
                this.personalData.setName(null);
            }
            return;
        }

        ensurePersonalData().setName(name);
    }

    public String getEmail() {
        if (this.email != null) {
            return this.email;
        }
        return this.personalData != null ? this.personalData.getEmail() : null;
    }

    public void setEmail(String email) {
        this.email = email;
        if (email == null) {
            if (this.personalData != null) {
                this.personalData.setEmail(null);
            }
            return;
        }

        ensurePersonalData().setEmail(email);
    }

    public String getPhone() {
        if (this.phone != null) {
            return this.phone;
        }
        return this.personalData != null ? this.personalData.getPhone() : null;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        if (phone == null) {
            if (this.personalData != null) {
                this.personalData.setPhone(null);
            }
            return;
        }

        ensurePersonalData().setPhone(phone);
    }

    public void clearPersonalData() {
        if (this.personalData != null) {
            this.personalData.setUser(null);
            this.personalData = null;
        }
    }

    public void setPersonalData(UserPersonalDataEntity personalData) {
        this.personalData = personalData;

        if (personalData != null && personalData.getUser() != this) {
            personalData.setUser(this);
        }
    }

    private UserPersonalDataEntity ensurePersonalData() {
        if (this.personalData == null) {
            setPersonalData(new UserPersonalDataEntity());
        }

        return this.personalData;
    }

    private void ensurePersonalDataLink() {
        if (this.personalData != null && this.personalData.getUser() != this) {
            this.personalData.setUser(this);
        }
    }
}