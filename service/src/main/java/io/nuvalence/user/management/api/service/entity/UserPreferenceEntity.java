package io.nuvalence.user.management.api.service.entity;

import io.nuvalence.auth.access.AccessResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents the preferences for a given user, i.e. language, communication, etc.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@AccessResource("preference")
@Table(name = "user_preference")
public class UserPreferenceEntity {

    @Id
    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "preferred_CommunicationMethod")
    private String preferredCommunicationMethod;
}
