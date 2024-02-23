package io.nuvalence.user.management.api.service.entity;

import io.nuvalence.auth.access.AccessResource;
import io.nuvalence.user.management.api.service.enums.UserType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a single User Entity.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_table")
@AccessResource(value = "user", translator = UserAccessResourceTranslator.class)
@NamedEntityGraph(
        name = "user.complete",
        attributeNodes = {@NamedAttributeNode("roles"), @NamedAttributeNode("userPreference")})
@EntityListeners(UserEntityEventListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@SuppressWarnings("ClassFanOutComplexity")
public abstract class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "identity_provider", nullable = false)
    private String identityProvider;

    @Deprecated
    @Column(name = "first_name")
    private String firstName;

    @Deprecated
    @Column(name = "middle_name")
    private String middleName;

    @Deprecated
    @Column(name = "last_name")
    private String lastName;

    @Generated(event = EventType.INSERT)
    @Column(name = "full_name")
    private String fullName;

    @Deprecated
    @Column(name = "phone_number")
    private String phoneNumber;

    @Deprecated
    @Column(name = "email")
    private String email;

    @Column(name = "created_At", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "user_type", insertable = false, updatable = false, nullable = false)
    private UserType userType;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_On")
    private OffsetDateTime deletedOn;

    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH})
    private List<RoleEntity> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreferenceEntity userPreference;

    /**
     * Gets a list of permissions based on this user's roles.
     *
     * @return list of permissions that this user has
     */
    public Set<PermissionEntity> getPermissions() {
        return this.roles.stream()
                .map(RoleEntity::getPermissions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
