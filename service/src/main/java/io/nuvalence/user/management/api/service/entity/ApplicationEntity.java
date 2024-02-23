package io.nuvalence.user.management.api.service.entity;

import io.nuvalence.auth.access.AccessResource;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.util.UUID;

/**
 * Represents a single application.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@AccessResource("application")
@Table(name = "application")
public class ApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "display_name", nullable = false, unique = true)
    private String displayName;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @JoinTable(
            name = "application_permission",
            joinColumns = @JoinColumn(name = "application_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PermissionEntity> permissions;

    public void addPermission(PermissionEntity permission) {
        this.permissions.add(permission);
    }
}
