package com.moviebookingapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * User entity implementing Spring Security UserDetails with MySQL/JPA
 */
@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
           @UniqueConstraint(name = "uk_user_login_id", columnNames = "login_id")
       },
       indexes = {
           @Index(name = "idx_login_id", columnList = "login_id"),
           @Index(name = "idx_email", columnList = "email"),
           @Index(name = "idx_role", columnList = "role")
       })
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @NotBlank(message = "Login ID is mandatory")
    @Size(min = 3, max = 50, message = "Login ID must be between 3 and 50 characters")
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;
    
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(name = "password", nullable = false)
    private String password;
    
    @NotBlank(message = "Contact number is mandatory")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Contact number should be valid")
    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;
    
    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;
    
    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;
    
    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;
    
    @Column(name = "enabled")
    private boolean enabled = true;
    
    // Remove unmapped columns from SQL (keep as transient if needed)
    @Transient
    private Boolean isActive = true;
    
    @Transient
    private Boolean isEmailVerified = false;
    
    @CreatedDate
    @Column(name = "created_date", nullable = true, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime updatedAt;
    
    // Enum for roles
    public enum Role {
        USER, ADMIN
    }
    
    // Constructors
    public User() {}
    
    public User(String firstName, String lastName, String email, String loginId, 
                String password, String contactNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.loginId = loginId;
        this.password = password;
        this.contactNumber = contactNumber;
        this.role = Role.USER;
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() {
        return loginId;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getLoginId() {
        return loginId;
    }
    
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
    
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }
    
    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(loginId, user.loginId) || Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(loginId, email);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", loginId='" + loginId + '\'' +
                ", role=" + role +
                '}';
    }
    
    // Builder Pattern
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    public static class UserBuilder {
        private String firstName;
        private String lastName;
        private String email;
        private String loginId;
        private String password;
        private String contactNumber;
        private String role = "USER";
        private Boolean isActive = true;
        private Boolean isEmailVerified = false;
        
        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder loginId(String loginId) {
            this.loginId = loginId;
            return this;
        }
        
        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserBuilder contactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
            return this;
        }
        
        public UserBuilder role(String role) {
            this.role = role;
            return this;
        }
        
        public UserBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public UserBuilder isEmailVerified(Boolean isEmailVerified) {
            this.isEmailVerified = isEmailVerified;
            return this;
        }
        
        public User build() {
            User user = new User();
            user.firstName = this.firstName;
            user.lastName = this.lastName;
            user.email = this.email;
            user.loginId = this.loginId;
            user.password = this.password;
            user.contactNumber = this.contactNumber;
            user.role = Role.valueOf(this.role.toUpperCase());
            user.isActive = this.isActive;
            user.isEmailVerified = this.isEmailVerified;
            return user;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
