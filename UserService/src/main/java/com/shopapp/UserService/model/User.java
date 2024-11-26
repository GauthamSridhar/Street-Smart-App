package com.shopapp.UserService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phoneNumber")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Full name is mandatory")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "User role is mandatory")
    private UserRole role;

    private boolean isVerified;
    private String otpCode;
    private LocalDateTime otpExpiry;
    private String googleAuthId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "favorites")
    private List<UUID> favorites = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_ratings",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "rating_id")
    private List<UUID> ratings = new ArrayList<>();
}
