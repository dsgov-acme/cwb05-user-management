package io.nuvalence.user.management.api.service.entity;

import io.nuvalence.auth.access.AccessResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents a single Language entity.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@AccessResource("language")
@Table(name = "language")
public class LanguageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String languageName;

    /**
     * ISO 639-1 identifier.
     */
    @Column(name = "language_standard_id", nullable = false, unique = true)
    private String languageStandardId;

    @Column(name = "local_name", nullable = false)
    private String localName;
}
