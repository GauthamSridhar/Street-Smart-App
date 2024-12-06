package com.shopapp.UserService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phoneNumber")
})
public class User implements UserDetails {

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
    private  boolean is_verified;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "User role is mandatory")
    private UserRole role;

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

    // Constructors, getters, and setters are handled by Lombok's @Data

    /**
     * Returns the authorities granted to the user. Maps the user's role to a SimpleGrantedAuthority.
     *
     * @return a collection of GrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user. In this case, we use email as the username.
     *
     * @return the email
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be authenticated.
     *
     * @return true if the user's account is valid (i.e., non-expired), false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        // Implement your logic here. For simplicity, we'll assume accounts never expire.
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be authenticated.
     *
     * @return true if the user is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        // Implement your logic here. For example, you can add a field like 'isLocked' to the User entity.
        // For simplicity, we'll assume accounts are never locked.
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired. Expired credentials prevent authentication.
     *
     * @return true if the user's credentials are valid (i.e., non-expired), false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // Implement your logic here. For simplicity, we'll assume credentials never expire.
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be authenticated.
     *
     * @return true if the user is enabled, false otherwise
     */


    /**
     * Adds a new favorite UUID to the user's favorites list.
     *
     * @param favoriteId the UUID to add
     */
    public void addFavorite(UUID favoriteId) {
        this.favorites.add(favoriteId);
    }

    /**
     * Removes a favorite UUID from the user's favorites list.
     *
     * @param favoriteId the UUID to remove
     */
    public void removeFavorite(UUID favoriteId) {
        this.favorites.remove(favoriteId);
    }

    /**
     * Adds a new rating UUID to the user's ratings list.
     *
     * @param ratingId the UUID to add
     */
    public void addRating(UUID ratingId) {
        this.ratings.add(ratingId);
    }

    /**
     * Removes a rating UUID from the user's ratings list.
     *
     * @param ratingId the UUID to remove
     */
    public void removeRating(UUID ratingId) {
        this.ratings.remove(ratingId);
    }
}
