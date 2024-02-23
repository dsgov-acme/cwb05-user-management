package io.nuvalence.user.management.api.service.entity;

import com.google.common.base.Objects;
import io.nuvalence.auth.access.AccessResource;
import io.nuvalence.user.management.api.service.enums.UserType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a single Role Entity.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@AccessResource("role")
@Table(name = "role")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 255, nullable = false, unique = false)
    private String description;

    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @OneToMany(
            fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.DETACH})
    private List<UserEntity> users;

    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @OneToMany(
            fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.DETACH})
    private List<PermissionEntity> permissions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "default_role", joinColumns = @JoinColumn(name = "role_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private Set<UserType> defaultUserTypes;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleEntity that = (RoleEntity) o;
        return Objects.equal(id, that.id)
                && Objects.equal(name, that.name)
                && Objects.equal(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, description, users, permissions);
    }
}
