package com.evdealer.evdealermanagement.entity.account;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 20, unique = true)
    private String phone;

    // === ngày sinh ===
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "national_id", length = 20, unique = true)
    private String nationalId;

    @Column(name = "tax_code", length = 20, unique = true)
    private String taxCode;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "ban_reason", length = 255)
    private String banReason;

    // === giới tính (MALE, FEMALE, OTHER) ===
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    // === vai trò (MEMBER, ADMIN, STAFF) ===
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role = Role.MEMBER;

    // === trạng thái (ACTIVE, INACTIVE, BANNED, PENDING) ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.PENDING;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AuthProvider> providers;


    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum Role {
        MEMBER, ADMIN, STAFF
    }

    public enum Status {
        ACTIVE, INACTIVE, BANNED, PENDING
    }
}
